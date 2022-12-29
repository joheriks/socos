# Module defines an environment for analyzing/running ibp programs.
# -*- coding: latin-1 -*-

from pc.parsing.ParseError import *
from pc.parsing.IBPParser import *
from pc.semantic.LexicalEnvironment import *
from pc.semantic.Statement import *
from pc.semantic.Context import *
from pc.semantic.Translation import *
from pc.semantic.Message import *
from pc.semantic.ProofChecker import *
from pc.semantic.Debugger import *
from pc.parsing.AST import *
from pc.parsing.UidParser import UidParser
from pc.pp.Uid_PP import *
from pc.parsing.PVSUtils import *

from pc.util.Graph import Digraph
from pc.util.Utils import *

import os
import time
import sys


class IBPEnvironment(object):
    """An environment for analyzing and checking ibp programs through PVS.

    0. Start.

         |
         | __init__
         V
         
    1. Initialized environment. Data structures for the reflexive transitive
       closure of the extends relation with respect to the given files are
       built. At this point the lexical environment mapping identifiers to
       elements is guaranteed to be consistent, i.e., names clashes are
       caught during construction.

         |
         | idref_check 
         V

    2. References are now resolved: E.g., variable identifiers in assignments, 
       goto and exit targets, decreasing assertions and procedure calls.

         |
         | semantic_check
         V

    3. Basic semantic checks have been performed. E.g., procedure parameter
       matching, decreases and termination prerequisites. Also generates
       warning, e.g., for unconnected exits or use of assume statements.

         |
         | semantic_translate
         V

    4. Program is now translated to basic statements for correctness condition
       generation (liveness assertions, conditions on variants, procedure
       calls translated into assert-havoc-assume-assign)

         |
         | generate_pvs
         V

    5. PVS theories written to disk.

         |             |
         | verify      | trace
         V             V

    6. Exit. 
    """

    OPTIONS = [
        "debug",
        "generate-pvs-only",
        "log",
        "noprelude",
        #"positive",
        "process-info",
        "safe",
        "trace"]

    ATTRIBUTES = [
        "check",
        "main",
        "pipe",
        "pvs",
        "strategy",
        "timeout",
        "watchdog"]
    
    DESCRIPTION = {
        "check": "Verify elements specified by ARG only (default: verify all elements)",
        "debug": "Write debug output, including interaction with PVS",
        "generate-pvs-only": "Only generate PVS theories for specified files; do not verify",
        "log": "Generate log file",
        "main": "Name of program entry point (should be a parameterless procedure)",
        "noprelude": "Do not load prelude files",
        "pipe": "Keep PVS process running in the background. Parameter gives path to named pipes used for communication with PVS",
        #"positive": "Print confirmation of discharged conditions",
        "pvs": "Use the specified PVS executable",
        "process-info": "Print process PID, periodic progress, and verification time",
        "safe": "Disallow directives that would allow arbitrary code to be executed (in particular, strategy)",
        "trace": "Generate LISP and execute program in debug mode",
        "strategy": "Default endgame strategy. (skip) is used if unspecified",
        "timeout": "Maximal time spent on proving each theory (seconds)",
        "watchdog": "Watchdog timer: break with status incomplete if checking takes longer (seconds)"
        }

    CHECK, TRACE = range(2)
    
    def __init__( self,
                  socos_directory,
                  context_directory,		# directory including contexts checked
                  output_directory,
                  files,		            # list of inputs (filenames or streams)
                  name_asts = [],           # list of inputs (pairs (filename, asts))
                  options = set(),
                  attributes = {}
                  ):

        self._task = self.TRACE if "trace" in options else self.CHECK

        self._debug_info = "debug" in options
        self._log = "log" in options
        self._pipe = attributes.get("pipe", None)
        self._pvs = attributes.get("pvs", None)
        
        self._socos_directory = socos_directory

        self._context_directory = context_directory
        self._output_directory = output_directory

        self._given_files = files

        self._strata = attributes.get("strategy", "(skip)")
        self._safe = "safe" in options
        self._pvsonly = "generate-pvs-only" in options

        self._timeout = int(attributes.get("timeout")) if str(attributes.get("timeout", "")).isdigit() else None
        self._watchdog = int(attributes.get("watchdog")) if str(attributes.get("watchdog", "")).isdigit() else None
        
        self._lexicalenv = LexicalEnvironment()

        self._contexts = {} # maps identifier to either an AST (unparsed context) or Context object
        self._file_contexts = {} # maps filename to context identifier
        self._context_files = {} # maps context id to file
        self._context_libprefix = {} # maps context id to prefix for PVS import
        self._prelude_contexts = []
        self._given_contexts = []
       
        self._context_topo_order = []

        self._check_pvs_files = {}
        self._check_translators = {}
        self._trace_pvs_files = {}
        self._trace_translators = {}

        self.incomplete = True

        self._report_progress = "process-info" in options
        self._progress = 0
        self._approx_goals = 0

        self._main = attributes.get("main","main")

        #self._positive = "positive" in options

        errors = []

        
        self._verify_uids = None
        self._verify_elems = None
        if "check" in attributes:
            try:
                uids_str = attributes["check"].strip()
                self._verify_uids = UidParser().parse(uids_str) if len(uids_str) else []
            except ParseException, e:
                raise ProgramException([FailureMessage("--check: " + "\n".join(map(lambda x:x.msg,e.errors)))])
            except IllegalSymbolException, e:
                raise ProgramException([FailureMessage(str(e))])


        # Parse prelude and all files to be checked
        errors = []
        prelude_file_asts = []
        context_file_asts = []

        
        # Open all files with extension .ibp in all directories in lib
        preludes = []
        if "noprelude" not in options:
            try:
                prelude_path = os.path.join(socos_directory, "lib")
                prelude_name_lib_file = []
                for prelude_dir in os.listdir(prelude_path):
                    dirpath = os.path.join(prelude_path,prelude_dir)
                    if not prelude_dir.startswith(".") and os.path.isdir(dirpath):
                        for filename in os.listdir(dirpath):
                            try:
                                if filename.endswith(".ibp"):
                                    asts = self.parse_file(os.path.join(dirpath,filename),None,errors)
                                    preludes.append((prelude_dir,filename,asts))
                            except IOError,e:
                                errors.append(FileError(e.strerror,filename))
                #tasks.append((prelude_name_lib_stream,prelude_file_asts))
            except OSError,e:
                errors.append(FailureMessage(str(e)))

        #tasks.append((context_name_stream,context_file_asts))

        for x in files:
            if len(x)==2:
                filename, file = x
            else:
                filename = x
                file = None
            filepath = os.path.join(context_directory, filename)
            asts = self.parse_file(filepath, file, errors)

            if asts and self._debug_info:
                print "--- %s ----------------------------------------"%filename
                for ast in asts:
                    print tree_to_str(ast)

            context_file_asts.append((filename, asts))

        if errors:
            raise ProgramException(errors)
        
        for dir, filename, asts in preludes:
            filepath = os.path.join(context_directory,filename)
            #self._file_contexts[filename] = []
            for ast in asts:
                ctx_id = ast["ID"][0].value
                if ctx_id in self._contexts:
                    errors.append(FileError("prelude error: trying to redeclare context '%s', which is disallowed" % ctx_id,filepath))
                    continue
                self._contexts[ctx_id] = ast
                self._context_files[ctx_id] = filename
            self._prelude_contexts.append(ctx_id)
            self._context_libprefix[ctx_id] = dir

        context_file_asts += name_asts;

        for filename, asts in name_asts:
            self._given_files.append(filename)

        for filename, asts in context_file_asts:
            filepath = os.path.join(context_directory, filename)
            self._file_contexts[filename] = []
            for ast in asts:
                ctx_id = ast["ID"][0].value
                if ctx_id in self._prelude_contexts:
                    errors.append(FileError("context '%s' is a prelude context and must not be redefined"%ctx_id,filepath))
                    continue                              
                elif ctx_id in self._contexts:
                    errors.append(FileError("trying to redeclare context '%s', which is disallowed"%ctx_id,filepath))
                    continue
                self._contexts[ctx_id] = ast
                self._context_files[ctx_id] = filename
                self._file_contexts[filename].append(ctx_id)
                self._given_contexts.append(ctx_id)
                self._context_libprefix[ctx_id] = None


        if errors:
            raise ProgramException(errors)


    def parse_file( self, filepath, f, errors ):
        ibp_parser = IBPParser()
        try:
            if not f: f = open(filepath)
            return ibp_parser.parse(f.read())
        except IOError,e:
            if f: f.close()
            errors.append(FileError(e.strerror,filepath))
            return None
        except ParseException, e:
            if f: f.close()
            errors += map(lambda x:FileError(x.msg,filepath,x.lineno,x.pos), e.errors)
            return None


    def check( self ):
        self.start()
        for c in self.idref_and_semantic_check(): yield c
        self.generate_pvs()           
        if not self._pvsonly:
            if self._task==self.CHECK:
                for c in self.verify(): yield c
            elif self._task==self.TRACE:
                for c in self.trace(): yield c
	

    def start( self ):
        # Initialize all contexts that are to be verified, including
        # their transitive dependencies. Initialization occurs in
        # topological order according to extends relation. This order
        # is recorded in the _context_topo_order attribute.
        for ctx_id in self._given_contexts:
            self.get_context(ctx_id)

        # if the user has indicated explicit set of elements to verify, check only those
        #for uid in self._verify:
        #    pass
        

    def idref_and_semantic_check( self ):
        warnings = []
        for ctx in self._context_topo_order:
            ctx.idref_check()
            ctx.semantic_check(warnings)

        return warnings


    def generate_pvs( self ):
        # if tracing, convert the program into executable PVS *before* semantic translation
        if self._task==self.TRACE:
            for filename in self._given_files:
                ctxs = [self.get_context(cid) for cid in self._file_contexts[filename]]
                pvs_filename = os.path.join(self._output_directory,os.path.splitext(filename)[0]+"_exec.pvs")
                self._trace_pvs_files[filename] = pvs_filename
                pvs_file = open(pvs_filename, "w")
                try:
                    c2pvs = TraceContexts(self, ctxs)
                    self._trace_translators[pvs_filename] = c2pvs
                    c2pvs.translate_and_write(pvs_file)
                finally:
                    pvs_file.close()
                
        for ctx in self._context_topo_order:
            if ctx in self._prelude_contexts:
                continue
            ctx.semantic_translate()

        if self._verify_uids is not None:
            self._verify_elems = {}
            for uid in self._verify_uids:
                elem = self.match_uid_ast(uid)
                if elem==None:
                    raise ProgramException([FailureMessage("No such element, or ambiguous: '%s'"%Uid_PP().output_to_string(uid))])
                self._verify_elems[elem] = True

        self._approx_goals = 0

        for filename in self._given_files:
            ctx_ids = self._file_contexts[filename]
            ctxs = [self.get_context(cid) for cid in ctx_ids]
            pvs_filename = os.path.join(self._output_directory,os.path.splitext(filename)[0]+".pvs")
            self._check_pvs_files[filename] = pvs_filename
            pvs_file = open(pvs_filename, "w")
            try:
                c2pvs = VerifyContexts(self, ctxs)
                self._check_translators[pvs_filename] = c2pvs
                c2pvs.translate_and_write(pvs_file)
                self._approx_goals += c2pvs.goal_count
            finally:
                pvs_file.close()


    def verify_equation_solution(self):
        #verify if for example solutions of equations in x and y have the form: (x = v1 and y = v2) or (x = v3 and y = v4) ... 
        for ctx in self._context_topo_order:
            if ctx in self._prelude_contexts:
                continue
	    # ctx instance of class Context		
	    for d in ctx.derivations:
		# check derivation d (instance of class Derivation)
                x = d.ast["solve"]
                if x:
                    eq_vars = map(lambda t: t.value, x[0]["id_list"][0]["ID"])
                    last_term = d.get_last_term()
                    def get_type_list(tree, type):
                        if(tree.type == "pgroup"):
                            return mapcan(lambda x: get_type_list(x, type), tree.children)
                        if tree.type == type:
                            return mapcan(lambda x: get_type_list(x, type), tree.children)
                        else:
                            return [tree]

                    or_list = get_type_list(last_term, "KEY_OR")
                    or_and_list = mapcan(lambda t: get_type_list(t, "KEY_AND"), or_list)

                    def verify_equation_solution(term, eq_vars):
                        if term.type != "EQUAL":
                            return False
                        if len(term.children) != 2:
                            return False

                        if term.children[0].type != "idop":
                            return False

                        if term.children[0].children[0].type != "ID":
                            return False

                        if not term.children[0].children[0].value in eq_vars:
                            return False

                        var_list = map(lambda x: x.value, term.children[1].descendants_by_type("ID"))
                        for x in eq_vars:
                            if x in var_list:
                                return False
                        return True
                    for x in or_and_list:
                        variable_list = x.descendants_by_type("ID")
                        if not verify_equation_solution(x, eq_vars):
                            msg = 'The solution must be a disjunction of conjunctions of equalities of the form "equation variable = term".\nThe term must not contain the equation variables.'
                            yield msg, x, d
                            return
                        
        


    def verify( self ):
        self.incomplete = False

        if self._report_progress:
            self._progress = 0
        #    yield ProgressMessage("progress:%d/%d"%(0,self._approx_goals),
        #                          {"pnow":0,"pmax":self._approx_goals,"time":0.0})

        # Temporary solution for --positive: collect all steps, remove those that have at
        # least one unproved condition, at end generate all remaining conditions.
        # This should be fixed to work also with program conditions, and to generate the
        # conditions as they are proved.
        allsteps = []
        derivations = mapcan(lambda ctx:ctx.derivations,self._context_topo_order)
        while derivations:
            d = derivations[0]
            derivations = derivations[1:]
            allsteps += [d] + d.steps
            derivations = mapcan(lambda step:step.motivation.subderivations,d.steps) + derivations
        failed = False

        # t0 measures time elapsed since start, t1 since last file
        t0 = t1 = time.time()

        for msg, term, deriv in self.verify_equation_solution():
            self.incomplete = True
            yield ElementError(msg, deriv, term)
            #print msg
            #print tree_to_str(term)

        proofchecker = ProofChecker(self._socos_directory,
                                    self._context_directory,
                                    self._output_directory,
                                    pvs=self._pvs,
                                    pipe=self._pipe,
                                    debug=self._debug_info,
                                    log=self._log,
                                    theory_timeout=self._timeout,
                                    watchdog_timer=self._watchdog)

        for result in proofchecker.check(self._check_pvs_files.values()):

            if result[0]==proofchecker.GENERAL_ERROR:
                _,pvsfile,msg = result
                ibpfile = reverse_lookup(self._check_pvs_files,pvsfile)
                # something went wrong
                yield FileError(msg,os.path.basename(ibpfile))
                self.incomplete = True
                failed = True

            elif result[0]==proofchecker.SUMMARY:
                _,pvsfile, status = result
                ibpfile = self.get_full_path(reverse_lookup(self._check_pvs_files, pvsfile))
                yield FileMessage(status, ibpfile, {"status": status})
                    
            elif result[0] in (proofchecker.TYPECHECK_ERROR,
                               proofchecker.PARSE_ERROR):
                type,pvsfile,line,col,msg = result
                if type==proofchecker.PARSE_ERROR:
                    yield FileError(msg,pvsfile,line,col)
                    
                elif type==proofchecker.TYPECHECK_ERROR:
                    elem = self._check_translators[pvsfile].line2elem(line)
                    if elem:
                        # for type checking errors inside a procedure
                        yield ImpreciseError(msg,elem)
                    else:
                        yield FileError(msg,pvsfile,line,col)

                self.incomplete = True
                failed = True

            elif result[0]==proofchecker.PROGRESS:
                if self._report_progress:
                    _,pvsfile,uid = result
                    ibpfile = self.get_full_path(reverse_lookup(self._check_pvs_files,pvsfile))
                    now = time.time()
                    if uid==None:
                        # we are starting a new file
                        t1 = now
                    if uid is not None and self._progress<self._approx_goals:
                        #elem = self.match_uid_str(uid)
                        #if isinstance(elem,TrsElement):
                        self._progress += 1
                    tall = round(now-t0,3)
                    tfile = round(now-t1,3)
                    yield ProgressMessage("progress:%d/%d, file:%.3fs, total:%.3fs"%(self._progress,
                                                                                     self._approx_goals,
                                                                                     tfile,tall),
                                          ibpfile,
                                          {"pnow":self._progress,
                                           "pmax":self._approx_goals,
                                           "time":tall,
                                           "time_file":tfile})
                
            elif result[0]==proofchecker.UNPROVED:
                _,pvsfile,uid,sequent = result
                elem = self.match_uid_str(uid)
                assert elem

                yield VerificationResult(sequent, elem)
                if elem in allsteps:
                    allsteps.remove(elem)
                self.incomplete = True

            else:
                raise Exception("Can't happen")

                        


        #if self._positive and not failed:
        #    for elem in allsteps:
        #        yield VerificationResult(None, elem)
                
                    
    def trace( self ):
        debugger = Debugger(self._socos_directory,
                            self._context_directory,
                            self._output_directory,
                            pvs=self._pvs,
                            pipe=self._pipe,
                            debug=self._debug_info,
                            log=self._log,
                            theory_timeout=self._timeout,
                            watchdog_timer=self._watchdog)


        # find program entry point
        ctx = None
        proc = None
        for ctxid in self._given_contexts:
            ctx = self._contexts[ctxid]
            proc = ctx.get_procedure_by_name(self._main)
            if proc: break
        if not proc:
            raise ProgramException([FailureMessage("no procedure 'main' found in given contexts")])
            
        pvsfile = self._trace_pvs_files[self._context_files[ctxid]]
        theoryname = self._trace_translators[pvsfile].procedure_exec_theory_id(proc).value

        lastloc = None

        self.incomplete = False
        for result in debugger.trace(pvsfile,theoryname):
            if result[0]==Debugger.ERROR:
                _,msg = result
                self.incomplete = True
                yield FailureMessage(msg)

            elif result[0]==Debugger.STACK:
                _,stack = result
                if len(stack)==0:
                    break
                stack = [ (self.match_uid_str(loc),vars,vals) for (loc,vars,vals) in stack ]
                loc = stack[0][0]
                if loc==lastloc and not isinstance(loc,Call):
                    self.incomplete = True
                    yield ElementError("terminated abnormally",loc)
                    break
                else:
                    lastloc = loc
                    yield TraceMessage(stack)

            elif result[0]==Debugger.SUMMARY:
                status = result[1]
                yield SuccessMessage("terminated normally",{"status":status})
                
        

    def is_prelude( self, ctx ):
        assert ctx in self._contexts.values()
        return ctx.get_id_str() in self._prelude_contexts


    def get_context_prefix( self, ctx ):
        assert ctx in self._contexts.values()
        return self._context_libprefix[ctx.get_id_str()]
        

    def unsafe_commands_allowed( self, ctx ):
        return not self._safe or ctx.get_id_str() in self._prelude_contexts


    def get_context( self, ctx_id ):
        assert type(ctx_id)==str
        ctx = self._contexts.get(ctx_id,None)
        if not ctx: return None
        elif isinstance(ctx,Node):
            ctx = Context(self,ctx,self.get_full_path(self._context_files[ctx_id]))
            self._contexts[ctx_id] = ctx
            self._context_topo_order.append(ctx)
        return ctx


    def get_contexts_inorder( self ):
        return self._given_contexts[:]


    def get_strategy( self ):
        return (self._strata,self._strata)
    

    def get_lexical_environment( self ):
        return self._lexicalenv
    

    def match_uid_ast( self, uid ):
        for x in self._context_topo_order:
            m = None
            try:
                m = x.match_uid(uid)
            except MatchException:
                break
            if m:
                return m
        print "No element matching uid: '%s'"%Uid_PP().output_to_string(uid)
        return None


    def match_uid_str( self, uidstr ):
        # resolves single uid string to element (returns None if no match)
        try:
            uid = UidParser().parse(uidstr)[0]
            return self.match_uid_ast(uid)
        except ParseException,e:
            print "Error parsing uid: '%s'"%uidstr
            return None


    def get_full_path( self, filename ):
        return os.path.join(self._context_directory,filename)


    def should_verify( self, elem ):
        return self._verify_elems is None or elem in self._verify_elems


    def get_all_verifiables( self ):
        # TODO: This method is unfinished. Need to define the verifiables for
        # all element types.
        verifiables = []

        for ctx in self._context_topo_order:
            verifiables += ctx.get_all_verifiables()

        return verifiables
