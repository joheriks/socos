from pc.semantic.Message import *
from pc.util.ShellUtils import find_executable
from pc.util.FileUtils import *
from pc.parsing.SEXPParser import *
from pc.parsing.PVSUtils import *
from pc.pp.SEXP_PP import *

import fcntl
import subprocess
import re
import os
import sys
import signal
import time

class PVSInterface( object ):

    STOPPED,STARTED = range(2)
    
    def __init__( self,
                  socos_directory,
                  file_directory,
                  output_directory,
                  pvs=None,
                  pipe=None,
                  debug=False,
                  log=False,
                  theory_timeout=None,
                  watchdog_timer=None):

        self._socos_directory = socos_directory
        self._pvslib_directory = os.path.join(socos_directory,"lib")

        self._debug = debug
        
        self._directory = file_directory
        self._output_directory = output_directory

        self._current_file = None

        self._pvs = pvs if pvs else "pvs"
        self._fifo_in = None
        self._fifo_out = None
        self._process = None
        self._process_id = None
        self._state = self.STOPPED
        
        # logging
        self._dolog = log
        self._logfile = None
        self._logfilename = None

        # communication
        self._use_named_pipes = bool(pipe)
        self._fifo_in_name = (pipe + ".in") if pipe else None
        self._fifo_out_name = (pipe + ".out") if pipe else None

        # caught signal
        self._signal = None

        self._theory_timeout = theory_timeout
        self._watchdog_timer = watchdog_timer


    def setup( self ):
        # catch Ctl+Brk!
        signal.signal(signal.SIGINT,self._sighandler)
        signal.signal(signal.SIGCHLD,self._sighandler)

        self._init_pvs()

        # set optional watchdog timer
        if self._watchdog_timer is not None:
            signal.signal(signal.SIGALRM,self._sighandler)
            signal.alarm(self._watchdog_timer)
        

        # write context directory to checker
        self._write_sexp(SEXP_LIST("socos-set-context",
                                   SEXP_STRING("%s%s" % (self._output_directory,os.path.sep))))


    def teardown( self ):
        if self._state==self.STOPPED or not self._use_named_pipes:
            self._cleanup_process()
        else:
            self._close_pipes()

        signal.signal(signal.SIGINT,signal.SIG_DFL)
        signal.signal(signal.SIGCHLD,signal.SIG_DFL)
        if self._watchdog_timer is not None:
            signal.alarm(0)
            signal.signal(signal.SIGALRM,signal.SIG_DFL)


    def _sighandler( self, sign, handler ):
        sys.stderr.flush()
        self._state = self.STOPPED
        self._signal = sign
            

    def _unescape_msg( self, msg ):
        # removes escapes and empty lines from PVS error messages
        return re.sub("\n+","\n",msg.replace("\\\\","\\").replace("\\n","\n"))


    def _make_named_pipes( self ):
        os.mkfifo(self._fifo_in_name)
        os.mkfifo(self._fifo_out_name)

        
    def _delete_named_pipes( self ):
        if os.path.exists(self._fifo_in_name): os.unlink(self._fifo_in_name)
        if os.path.exists(self._fifo_out_name): os.unlink(self._fifo_out_name)


    def get_subprocess( self ):
        return self._process
    

    def _find_tool( self, tool ):
        path = find_executable(tool)
        if path: return path
        else: raise ProgramException([FailureMessage("unable to launch '%s'"%tool)])


    def _get_pvs_environ(self):
        env = dict(os.environ)
        pvspath = self._find_tool(self._pvs)

        # PVSIMAGE = pvs-allegro
        # PVSPATCHLEVEL = 2
        # PVSLISP = allegro
        # PVS_LIBRARY_PATH = 
        # PVSARCH = ix86
        # ALLEGRO_CL_HOME = /opt/pvs-4.2/bin/ix86-Linux/home
        # PATH = /opt/pvs-4.2/bin/ix86-Linux/runtime:/opt/pvs-4.2/bin/ix86-Linux:/opt/pvs-4.2/bin: + env['PATH']
        # PVSPATH = /opt/pvs-4.2
        # LD_LIBRARY_PATH = /opt/pvs-4.2/bin/ix86-Linux/runtime: + env['LD_LIBRARY_PATH']

        # environment variables needed to be set before running pvs-allegro
        env_vars = ["LD_LIBRARY_PATH","PVSIMAGE","PVSLISP","PATH","PVSARCH",
                    "ALLEGRO_CL_HOME","PVSPATH","PVSPATCHLEVEL","PVS_LIBRARY_PATH", "SHELL"]

        p = subprocess.Popen("/bin/bash -c '. %s --version ; %s'" % (pvspath, ";".join(["echo %s=$%s"%(x,x) for x in env_vars])),
                             shell=True,
                             stdout=subprocess.PIPE,
                             stderr=subprocess.PIPE)
        out,_ = p.communicate()
        lines = out.split("\n")
        env = {}
	for l in lines:
	    varval = l.split("=",1)
	    if len(varval)==2:
	        var,val = varval
		if var in env_vars: env[var] = val    	    
        if not env.get("PVSIMAGE"):
            raise ProgramException([FailureMessage("%s is the wrong PVS (get pvs.csl.sri.com)" % pvspath)])
        i = 0
        
        img = env["PVSIMAGE"]
        
        if env["PVS_LIBRARY_PATH"].strip():
            env["PVS_LIBRARY_PATH"] = env["PVS_LIBRARY_PATH"] + ":" + self._pvslib_directory
        else:
            env["PVS_LIBRARY_PATH"] = self._pvslib_directory

        return img,env
    

    def _init_pvs( self ):
        try:
            if self._use_named_pipes:
                found_fifos = (os.path.exists(self._fifo_in_name) and
                               os.path.exists(self._fifo_out_name))
                if not found_fifos:
                    self._make_named_pipes()

                self._fifo_in = open(self._fifo_in_name,"w+",0)
                self._fifo_out = open(self._fifo_out_name,"r+",0)

                if found_fifos:
                    if self._handshake():
                        return

                    print >> sys.stderr, "unable to connect to existing process, relaunching PVS..."
                    self._close_pipes()
                    self._delete_named_pipes()
                    self._make_named_pipes()
                    self._fifo_in = open(self._fifo_in_name,"w+",0)
                    self._fifo_out = open(self._fifo_out_name,"r+",0)

                stdin = self._fifo_in
                stdout = self._fifo_out

            else:
                stdin,stdout = subprocess.PIPE,subprocess.PIPE

            
            pvs_img_path,pvs_env = self._get_pvs_environ()

            args = [pvs_img_path, "-qq", "-e", "(pvs::pvs-init)",
                    "-L", os.path.join(self._socos_directory,"lib","socos","pvs-interface.lisp")]

            #print args
            #print pvs_env

            self._process = subprocess.Popen(args,
                                             stdin = stdin,
                                             stdout = stdout,
                                             stderr = None,
                                             cwd = self._output_directory,
                                             env = pvs_env,
                                             bufsize = 0)
            self._process_id = self._process.pid

            #print self._process_id

            if not self._use_named_pipes:
                self._fifo_in = self._process.stdin
                self._fifo_out = self._process.stdout

            if not self._handshake():
                raise ProgramException([FailureMessage("unable to connect to checking interface")])
                
        except (IOError,OSError),e:
            self._close_log()
            self._close_pipes()
            raise ProgramException([FailureMessage("checking interface error: %s"%str(e))])

        except ProgramException:
            self._close_log()
            self._close_pipes()
            raise


    def _readline( self ):
        try:
            r = self._fifo_out.readline()
        except IOError:
            r = ""
        if r=="":
            #self._process.poll()
            #if self._process.returncode!=None:
            self._state = self.STOPPED
        if self._logfile and r:
            self._logfile.write(r)
        if self._debug and r:
            print r,
        return r


    bracketed_sexp_begin_re = re.compile("^-\*-SOCOS-\*-$")
    bracketed_sexp_end_re = re.compile("^-\*-\*-\*-\*-\*-$")


    def _write_sexp( self, sexp ):
        self._fifo_in.write(SEXP_PP().output_to_string(sexp) + "\n")
        

    def _read_sexp( self ):
        s = ""
        # read until next empty line
        while not self._state==self.STOPPED:
            l = self._readline()
            if self.bracketed_sexp_end_re.match(l):
                break
            s += l
        if self._state==self.STOPPED:
            return None

        try:
            parser = SEXPParser()
            sexp = parser.parse(s)
            #print tree_to_str(sexp)
            return sexp
        
        except ParseException,e:
            print "Parse error. This is not supposed to happen."
            print s
            self._state = self.STOPPED
            return None


    def _read_next_sexp( self ):
        while self._state!=self.STOPPED and not self.bracketed_sexp_begin_re.match(self._readline()):
            pass
        return self._read_sexp()
    

    def _handshake( self ):
        if not write_with_timeout(self._fifo_in, "(socos-hello)\n", 5):
            return False
        timeleft = 25.0

        self._state = self.STARTED

        s = ""
        while timeleft>0:
            t0 = time.time()
            x = read_with_timeout(self._fifo_out,timeleft)
            if not x: return False
            else: s += x
            if x=="\n":
                m = self.bracketed_sexp_begin_re.match(s)
                if m:
                    sexp = self._read_sexp()
                    if sexp and len(sexp.children)==2 and sexp[0].value=="hello":
                        if not self._process_id:
                            self._process_id = int(sexp.children[1].value)
                        return True
                    else:
                        return False
                s = ""
            timeleft -= (time.time()-t0)
                
        return False


    def _open_log( self, filename ):
        if self._dolog:
            self._logfile = open(filename, "w")


    def _close_log( self ):
        if self._logfile:
            self._logfile.close()
            self._logfile = None


    def _close_pipes( self ):
        if self._fifo_in:
            self._fifo_in.close()
            self._fifo_in = None
        if self._fifo_out:
            self._fifo_out.close()
            self._fifo_out = None


    def _cleanup_process( self ):
        if self._process_id:
            try:
                os.kill(self._process_id, signal.SIGKILL)
                os.wait()
            except OSError:
                pass
            self._close_pipes()

            self._process = None

        
