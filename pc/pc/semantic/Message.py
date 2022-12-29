from pc.parsing.AST import tree_to_str
from pc.parsing.PVSParser import *
from pc.pp.Derivation_PP import Derivation_PP
from pc.util.Utils import *

import json
import os
import textwrap


DESCRIPTION = {"color":"Use ANSI color sequences in messages",
               "json" : "Produce JSON-formatted output (for machines)",
               "nowrap":"Do not line-wrap messages",
               "fullpath":"Show full file paths in messages",
               "detailed":"Show more detail in error reports (for humans)"}

OPTIONS = DESCRIPTION.keys()

# selected options
opts = set()

TERM_WIDTH = 78


class Message( object ):

    ansi_tags = None
    type = None        # NOTE: must be set by subclasses

    
    def __init__( self, msg, attributes={} ):
        assert type(msg)==str,"%s not of type str"%type(msg)
        assert self.__class__.type
        self.msg = msg
        self.attributes = attributes.copy()

    def _indent( self, msg, prefix ):
        if type(prefix)==int: prefix = " "*prefix
        return prefix_lines(msg,prefix)

    def _indented_msg( self, header, msg, wrap=True ):
        if wrap and not ("nowrap" in opts):
            msglines = textwrap.wrap(msg,max(15,TERM_WIDTH-len(header)),
                                     break_long_words=False)
        else:
            msglines = msg.split("\n")
        s = header + ": " +msglines[0]
        for x in msglines[1:]:
            s += "\n" + " "*(len(header)+2) + x
        return s

    def _filename_str( self, filename ):
        if "fullpath" in opts:
            return filename
        else:
            return os.path.basename(filename)

    def location( self ):
        return ()
    
    def location_identifier( self, loc ):
        if not loc: return "socos"
        elif len(loc)==1: return "%s"%(loc[0])
        elif len(loc)==2: return "%s:%d"%(loc[0],loc[1])
        elif len(loc)==3: return "%s:%d:%d"%(loc[0],loc[1],loc[2])
        else: raise Exception("unsupported location identifier")

    def json_str( self ):
        x = { "message": self.msg,
              "type": self.__class__.type }
        x.update(self._json_location(self.location()))
        x.update(self.attributes)
        return json.dumps(x)

    def _json_location( self, loc ):
        return dict(zip(("file","line","column"),loc))
        
    def human_readable_str( self ):
        return self._indented_msg(self.location_identifier(self.location()),self.msg)

    def get_message( self ):
        if "json" in opts:
            return self.json_str()
        else:
            if "color" in opts and self.ansi_tags:
                return self.ansi_tags[0] + self.human_readable_str() + self.ansi_tags[1]
            else:
                return self.human_readable_str()


class FailureMessage( Message ):
    type = "ERROR"
    ansi_tags = "\033[0;1;31m","\033[0m"

    def human_readable_str( self ):
        return self._indented_msg("socos",self.msg)

        

class SuccessMessage( Message ):
    type = "SUMMARY"
    ansi_tags = "\033[0;1;32m","\033[0m"



class FileMessage( Message ):
    type = "SUMMARY"
    ansi_tags = "\033[0;1;37m","\033[0m"
            
    def __init__( self, msg, filename, attributes={} ):
        Message.__init__(self, msg, attributes)
        self.filename = filename

    def location( self ):
        return (self._filename_str(self.filename),)


class ProgressMessage( FileMessage ):
    type = "PROGRESS"
    ansi_tags = "\033[0;1;35m","\033[0m"

    def human_readable_str( self ):
        pnow,pmax = int(self.attributes["pnow"]),int(self.attributes["pmax"])
        return "socos: [%s%s]"%("#"*pnow,"."*(pmax-pnow))



class FileError( FileMessage ):
    # an error message that indicates a file location

    type = "ERROR"
    ansi_tags = "\033[0;1;31m","\033[0m"

    def __init__( self, msg, filename, line=-1, col=-1, attributes={} ):
        FileMessage.__init__(self,msg,filename,attributes)
        self.line = line
        self.col = col

            
    def location( self ):
        if self.line!=-1 and self.col!=-1:
            return (self._filename_str(self.filename),self.line,self.col)
        elif self.line!=-1:
            return (self._filename_str(self.filename),self.line)
        else:
            return FileMessage.location(self)



class ElementMessage( Message ):

    # NB. Element must have an identifier before this message can
    # be issued

    def __init__( self, msg, element, precise_ast=None, attributes={} ):
        Message.__init__(self,msg,attributes)
        self.element = element
        self.ast = precise_ast if precise_ast else element.ast
        assert element.id,"Element %s has no id!"%repr(element)
        #assert self.ast
        
    def location( self ):
        if not self.ast or self.ast.start_line()==-1:
            return self._filename_str(self.element.filename),
        elif self.ast.start_pos()==-1:
            return (self._filename_str(self.element.filename),self.ast.start_line())
        else:
            return (self._filename_str(self.element.filename),self.ast.start_line(),self.ast.start_pos())

    

class ElementError( ElementMessage ):

    type = "ERROR"
    ansi_tags = "\033[0;1;31m","\033[0m"



class ElementWarning( ElementMessage ):

    type = "WARNING"
    ansi_tags = "\033[0;1;33m","\033[0m"



class ImpreciseError( ElementMessage ):
    # An error message for reporting errors related to a context or
    # procedure. Used for typechecking and other errors that cannot be
    # connected to an exact location.

    type = "ERROR"
    ansi_tags = "\033[0;1;31m","\033[0m"

    def __init__( self, msg, element, attributes={} ):
        ElementMessage.__init__(self,msg,element,attributes)
        assert element.get_attribute() in ("procedure","context")

    def human_readable_str( self ):
        if self.element.get_attribute()=="procedure":
            s = "procedure '%s'"%self.element.get_id_str()
        else:
            s = "context '%s'" %self.element.get_id_str()
        return self._indented_msg("%s: %s"%(self._filename_str(self.element.filename),s),
                                  self.msg,False)
    
    
class VerificationResult( ElementMessage ):
    # A message for reporting an unproved correctness condition.
    # sequent is either None, or a tuple (antecedents,consequents)

    type = "CONDITION"
    ansi_tags = "\033[0;1;36m","\033[0m" # cyan

    def __init__( self, sequent, elem, attributes={} ):
        ElementMessage.__init__(self,self._create_msg(elem),elem,attributes)
        self.sequent = sequent
        self.attributes["assumptions"] = sequent[0]
        self.attributes["goals"] = sequent[1]
        

    def _sequent_string( self ):
        if not self.sequent: return "proved"
        f = (lambda x: "\033[0;1;33m"+x+self.ansi_tags[0]) if "color" in opts else (lambda x:x)

        (ants,cons) = self.sequent

##         p = PVSParser()
##         ants = map(tree_to_str,map(p.parse,ants))
##         cons = map(tree_to_str,map(p.parse,cons))

        # if the consequents are empty, add false
        if not cons:
            cons = ["false"]
        msg = "-  " if ants else ""
        msg += "\n-  ".join(map(f,ants))
        msg += "\n|- " if ants else "|- "
        msg += "\n\\/ ".join(map(f,cons))

        return msg
                

    def _constraintset_locator( self, target ):
        from pc.semantic.Context import InitialSituation,FinalSituation
        if isinstance(target,InitialSituation):
            t = "precondition"
        elif isinstance(target,FinalSituation):
            t = "postcondition '%s'"%target.post.get_id_str() if not target.post.is_anonymous() else "postcondition"
        else:
            t = "situation '%s'"%target.get_id_str()
        return t


    def _create_msg( self, elem ):
        assert elem.get_attribute() in ("context",
                                        "procedure",
                                        "check",
                                        "assert",
                                        "assert_precondition",
                                        "assert_recursive_decreases",
                                        "derivation",
                                        "step",
                                        "goal",
                                        "observation"), "Unsupported elem: %s"%elem.get_attribute()

        if elem.get_attribute()=="context":
            # context-level TCC
            msg = "TCC in context '%s'"%elem.get_id_str()

        elif elem.get_attribute()=="procedure":
            # procedure-level TCC
            msg = "TCC in procedure '%s'"%elem.get_id_str()

        elif elem.get_attribute() in ("step","observation"):
            msg = "%s '%s'"%(elem.get_attribute(),elem.get_id_str())
                               
        elif elem.get_attribute()=="derivation":
            msg = "derivation '%s'"%elem.get_id_str()

        elif elem.get_attribute()=="goal":
            msg = "conclusion of derivation '%s'"%elem.derivation.get_id_str()

        elif elem.get_attribute() in ("check","assert","assert_precondition","assert_recursive_decreases"):
            if elem.get_attribute()=="check":
                if elem.parent.get_attribute()=="assert_precondition":
                    source = elem.parent.parent.get_source_situation()
                elif elem.parent.get_attribute()=="goto":
                    source = elem.parent.get_source_situation()
                else:
                    raise "Can't happen"
            else:
                source = elem.parent.get_source_situation()

            s = "transition from %s"%self._constraintset_locator(source)
            
            if elem.get_attribute()=="check":
                if elem.parent.get_attribute()=="goto":
                    target = elem.parent.target
                    c = elem.constraint
                    cs = c.constraintset
                    admonition = "constraint"
                    locator = "%s to %s"%(s,self._constraintset_locator(target))

                elif elem.parent.get_attribute()=="assert_precondition":
                    admonition = "procedure call"
                    locator = s

            elif elem.get_attribute()=="assert":
                if elem.ast and elem.ast.type=="if":
                    admonition = "liveness assertion"
                else:
                    admonition = "assertion"
                locator = s

            elif elem.get_attribute()=="assert_recursive_decreases":
                admonition = "recursive call of '%s' decreases"%(elem.callee.get_id_str())
                locator = s

            msg = "procedure '%s', %s in %s"%(elem.get_procedure().get_id_str(),admonition,locator)

        return msg 


    def human_readable_str( self ):
        # TODO: this method needs to be refactored
        locstr = "%s"%self._filename_str(self.element.filename)

        if self.ast:
            locstr += ":%d:%d"%(self.ast.start_line(),self.ast.start_pos())

        retval = self._indented_msg(locstr,self.msg) + ":\n" + self._indent(self._sequent_string(),len(locstr)+2)
        #if retval.count("\n")>1 and retval[-1]!="\n": retval = retval + "\n"
        return retval



class TraceMessage( Message ):

    type = "TRACE"
    ansi_tags = "\033[0;1;35m","\033[0m" # magenta

    def __init__( self, stack ):
        assert stack
        Message.__init__(self,self._create_msg(stack))

        self.stack = stack
        self.attributes["stack"] = [ dict(self._json_location(self.frame_location(elem)),
                                          **{ "name": elem.get_id_str(),
                                              "vars": vars,
                                              "vals": vals }) for (elem,vars,vals) in stack ]


    def _create_msg( self, stack ):
        lines = []
        for (elem,vars,vals) in stack:
            locstr = self.location_identifier(self.frame_location(elem))
            lines.append(locstr + ": %s<"%elem.get_id_str() + ",".join(["%s='%s'"%(vars[i],vals[i]) for i in range(len(vars))]) + ">")
        return "\n".join(lines)


    def location( self ):
        return self.frame_location(self.stack[0][0])


    def frame_location( self, elem ):
        if not elem: 
            loc = None
        elif not elem.ast or elem.ast.start_line()==-1:
            loc = self._filename_str(elem.filename),
        elif elem.ast.start_pos()==-1:
            loc = (self._filename_str(elem.filename),elem.ast.start_line())
        else:
            loc = (self._filename_str(elem.filename),elem.ast.start_line(),elem.ast.start_pos())
        return loc

            
    def human_readable_str( self ):
        return self._indented_msg(self.location_identifier(None),self.msg,False)


#
# Exceptions
#

class ProgramException( Exception ):

    def __init__( self, msgs ):
        Exception.__init__(self,'\n'.join(map(str,msgs)))
        self.msgs = msgs


    def get_message(self):
        s = '\n'.join(map(lambda x:x.get_message(), self.msgs))
        if "color" in opts:
            s = "\033[0;1;31m"+s+"\033[0m"
        return s
