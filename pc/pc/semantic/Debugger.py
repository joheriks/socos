from pc.semantic.PVSInterface import PVSInterface
from pc.parsing.PVSUtils import *
from pc.parsing.AST import *

import os


class Debugger( PVSInterface ):

    ERROR,STACK,SUMMARY = range(3)
    

    def trace( self, filename, theoryname ):
        self.setup()
        self._open_log("exec.log")
        self._write_sexp(SEXP_LIST("socos-start-trace",
                                   SEXP_STRING(os.path.splitext(os.path.basename(filename))[0]),
                                   SEXP_STRING(theoryname)))

        terminated_normally = False
        while not terminated_normally:
            if self._state==self.STOPPED:
                yield (self.ERROR,
                       "error/break during tracing" 
                       + (" (reason: signal %d)"%self._signal if self._signal else ""))
                break
            self._write_sexp(SEXP_LIST("socos-get-state"))
            sexp = self._read_next_sexp()

            if sexp:
                if sexp["@error"]:
                    yield (self.ERROR,sexp[1].value)
                    break

                elif sexp["@parse-error"]:
                    yield (self.ERROR,sexp[3].value)
                    break

                elif sexp["@typecheck-error"]:
                    yield (self.ERROR,sexp[3].value)
                    break

                elif sexp["@state"]:
                    # yields a tuple of the form:
                    # (self.STACK,[ [uid, <vars1>,<values1>], ... ])
                    if sexp["@nil"]:
                        terminated_normally = True
                        break
                    state = []
                    for x in sexp[1].children:
                        uid = None
                        vals = []
                        vars = []
                        for i in range(len(x[0].children)):
                            var = x[0].children[i].value
                            val = x[1].children[i].value
                            if "__" not in var:
                                vars.append(var)
                                vals.append(val)
                            elif var=="uid__":
                                uid = val
                        state.append((uid,vars,vals))
                            
                    yield (self.STACK,state)
                    
                else:
                    print sexp

            self._write_sexp(SEXP_LIST("socos-step"))

        if terminated_normally:
            yield (self.SUMMARY,"complete")

        self._close_log()
        
        self.teardown()

