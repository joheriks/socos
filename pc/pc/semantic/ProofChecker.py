from pc.semantic.Message import *
from pc.semantic.PVSInterface import *
from pc.util.ShellUtils import find_executable
from pc.util.FileUtils import *
from pc.parsing.SEXPParser import *


# There is a bug here: if the prelude directory contains a file with the same name
# as a prelude file, the latter seems to be loaded by PVS first.


class ProofChecker( PVSInterface ):

    (GENERAL_ERROR,
     TYPECHECK_ERROR,
     PARSE_ERROR,
     UNPROVED,
     PROGRESS,
     SUMMARY) = range(6)


    def check( self, pvsfiles ):
        # Yields a tuple of one of the following forms:
        #
        # (GENERAL_ERROR,<fname>,<msg>)
        # (TYPECHECK_ERROR,<fname>,<line>,<col>,<msg>)
        # (PARSE_ERROR,<fname>,<line>,<col>,<msg>)
        # (UNPROVED,<fname>,<unique_id>,<sequent>)
        # (PROGRESS,<fname>,[<unique_id>|None])
        # (SUMMARY,<fname>,<status>)
        #
        
        self.setup()
        
        # typecheck and prove
        for pvsfile in pvsfiles:
            filepath = os.path.join(self._output_directory,pvsfile)
            complete = True

            yield (self.PROGRESS,filepath,None)

            self._open_log(os.path.splitext(filepath)[0] + ".log")

            self._write_sexp(SEXP_LIST("socos-check",
                                       SEXP_STRING(os.path.splitext(os.path.basename(pvsfile))[0]),
                                       str(self._theory_timeout) if self._theory_timeout else "nil"))

            self._state = self.STARTED
            while self._state != self.STOPPED:
                sexp = self._read_next_sexp()
                if not sexp:
                    self._state = self.STOPPED
                else:
                    if sexp["@check-finished"]:
                        yield (self.SUMMARY,filepath,"complete" if complete else "incomplete")
                        break

                    elif sexp["@error"]:
                        complete = False
                        yield (self.GENERAL_ERROR,
                               pvsfile,
                               self._unescape_msg(sexp[1].value))

                    elif sexp["@parse-error"]:
                        complete = False
                        yield (self.PARSE_ERROR,
                               filepath,
                               int(sexp[1].value),int(sexp[2].value),
                               self._unescape_msg(sexp[3].value))

                    elif sexp["@typecheck-error"]:
                        complete = False
                        yield (self.TYPECHECK_ERROR,
                               filepath,
                               int(sexp[1].value),int(sexp[2].value),
                               self._unescape_msg(sexp[3].value))

                    elif sexp["@check-condition"]:
                        cond = sexp[1].value
                        yield (self.PROGRESS,filepath,cond)

                    elif sexp["@left-condition"]:
                        complete = False
                        cond = sexp[1].value
                        loc = sexp[1].value
                        antecedents = map(lambda y:y.value,sexp[3].children)
                        consequents = map(lambda y:y.value,sexp[4].children)
                        yield (self.UNPROVED,filepath,cond,(antecedents,consequents))

                    #else:
                    #    print "Unhandled:"
                    #    print tree_to_str(sexp)


            if self._state==self.STOPPED:
                yield (self.GENERAL_ERROR,
                       pvsfile,
                       "error/break during checking" 
                       + (" (reason: signal %d)"%self._signal if self._signal else ""))
                continue

            self._close_log()

        self.teardown()
