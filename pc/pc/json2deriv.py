import json
import re
import sys
import time

def debug( x ):
    sys.stderr.write("["+time.strftime("%a, %d %b %Y %H:%M:%S", time.localtime())+"] ")
    sys.stderr.write(unicode(x).encode("latin-1","replace")+"\n")
    sys.stderr.flush()

from pc.semantic import Message
#from pc.semantic.IBPEnvironment import IBPEnvironment
#from pc.z3check import Z3Environment
#Checkers = {"pvs": IBPEnvironment, "z3": Z3Environment}



class JSON2DerivException( Exception ):

    def __init__( self, location, msg ):
        Exception.__init__(self, msg)
        self.location = location


class JSON2Deriv( object ):

    # This class builds a textual derivation from JSON object, and a mapping
    # from line numbers to "location lists".

    def __init__( self, json, session_id = None ):
        self.json = json
        self.session_id = session_id
        self.location = []
        self.lines = []
        self.line2location = []
        self.assumption_index = 0
        self.errors = []

    def convert( self ):
        debug("JSON:")
        debug(json.dumps(self.json, indent = 2))
        self.process_list(self.json, "context")
        debug("Document:\n%s"%"\n".join(["%-50s %% %s"%(self.lines[i],self.line2location[i]) for i in range(len(self.lines))]))
        return "\n".join(self.lines)

    def get_msg_thm(self, goals, assumptions):
        thm = "Prove\n"
        b = False
        for s in goals:
            if b:
                thm += "    or " + s + "\n"
            else:
                thm += "    " + s + "\n"
            b = True

        if assumptions:
            thm += "when\n"
            b = False
            for s in assumptions:
                if b:
                    thm += "    and " + s + "\n"
                else:
                    thm += "    " + s + "\n"
                b = True

        return thm


    def get_message_json( self, msg ):
        # ignore summary and progress messages for now
        if msg.type in ("PROGRESS","SUMMARY","WARNING"):
            debug("Ignored message: %s"%msg.get_message())
      	    return None

        location = []
        column = 0
        typ = "progress"
        msgtext = ""
        if isinstance(msg, Message.FileError):
            column = msg.col
            line = msg.line-1
            if 0<line<len(self.line2location):
                location = self.line2location[line]
        elif isinstance(msg, Message.ElementMessage):
            line = msg.ast.start_line()-1
            column = msg.ast.start_pos()
            if msg.element.get_attribute()=="step":
                # special location for step: use the motivation AST for line
                line += 2
            if 0 <= line < len(self.line2location):
                location = self.line2location[line]

        thm = ""
          
        if msg.type=="ERROR":
            if hasattr(msg,"filename") and msg.filename.endswith(".pvs"): 
                typ = "TYPE_ERROR"
                msgtext = "Type error"
            else:   
                typ = "SYNTAX_ERROR"
                msgtext = "Syntax error"
        elif msg.type=="CONDITION":
            typ = "PROVE_ERROR"
            msgtext = "Unproved condition"
            thm = self.get_msg_thm(msg.attributes["goals"], msg.attributes["assumptions"])

        ret = { "location": location,
                 "type": typ,
                 "original_message": msg.msg,
                 "original_type": msg.type,
                 "message": msgtext,
                 "column": column,
                 "thm": thm }
        ret.update(msg.attributes)
        return ret
            

    def _discard_empty_strings( self, list ):
        return [x for x in list if len(x)>0]

    def _get_location( self ):
        return self.location[:]

    def _lookup_value( self, dict, attr ):
        if attr in dict:
            return dict[attr]
        else:
            raise JSON2DerivException(self._get_location(),"no such attribute: '%s'"%attr)

    def _pp( self, string ):
        string_re = re.compile(r'(["\'])((?:\\\1|.)*?)\1')
        return " ".join([x[1].replace(r'\"',r'"') for x in string_re.findall(string)])

    def _push_location( self, loc ):
        self.location.append(loc)

    def _pop_location( self ):
        self.location.pop()

    def _add_line( self, line ):
        self.lines.append(line)
        self.line2location.append(self._get_location())

        # check for iso8859-1 compatibility
        try:
            line.encode("latin-1")
        except UnicodeEncodeError,e:
            raise JSON2DerivException(self._get_location(),"Non ISO-8859-1 symbol '%s' is disallowed."%(e.object[e.start:e.end]))


    def process_list( self, current, attr ):
        f = getattr(self,"_process_%s"%attr)
        self._push_location(attr)
        debug("attr: %s : %s"%(attr,self._lookup_value(current,attr+"s")))
        i = 0
        for x in self._lookup_value(current,attr+"s"):
            self._push_location(i)
            f(x)
            self._pop_location()
            i += 1
        self._pop_location()
        
    def _process_context( self, context ):
        id = self._lookup_value(context,"name")
        if self.session_id:
            id = id+"%d"%self.session_id
        self._add_line("%s: context"%id)
        self._add_line("begin")

        self.process_list(context,"declaration")

        self.process_list(context,"derivation")
        self._add_line("end %s"%id)


    def _process_declaration( self, decl ):
        self._add_line(decl+";")

    def _process_derivation( self, der ):
        id = self._lookup_value(der,"name")
        
        self._add_line("* %s:" % id)
        task = self._discard_empty_strings(self._lookup_value(der,"task"))
        if task: #self.process_list(der,"task")
            i = 0
            for t in self._lookup_value(der,"task"):
                self._push_location("task")
                self._push_location(i)
                self._process_task(t)
                self._pop_location()
                self._pop_location()
        else: self._add_line("derivation;")
        self.process_list(der,"assumption")
        self.process_list(der,"observation")
        self._add_line("|-")

        chain = self._lookup_value(der,"chain")
        i = 0
        terms = self._lookup_value(chain,"terms")
        relations = self._lookup_value(chain,"relations")
        motivations = self._lookup_value(chain,"motivations")
        if terms and not len(terms)==len(relations)+1:
            raise JSON2DerivException(self._get_location(),"terms/relation count mismatch (got %d relations, expected %d)"%(len(relations),len(terms)-1))
        for term in terms:
            self._push_location("term")
            self._push_location(i)
            self._add_line("%s ;"%term)
            self._pop_location()
            self._pop_location()
            if i<len(relations):
                self._push_location("relation")
                self._push_location(i)
                self._add_line("%s"%relations[i])
                self._pop_location()
                self._pop_location()

                self._push_location("motivation")
                self._push_location(i)

                directive = self._pp(" ".join([x for x in motivations[i] if type(x) in (str,unicode)]))
                subderivations = [x for x in motivations[i] if type(x)==dict]

                self._add_line('{ %s } ;'%directive)

                j = 0
                for sdm in subderivations:
                    for sd in sdm["derivations"]:
                        self._push_location("subderivation")
                        self._push_location(j)
                        self._process_derivation(sd)
                        self._pop_location()
                        self._pop_location()
                        j += 1

                self._pop_location()
                self._pop_location()
                i += 1
        
        self._add_line("[] ;")

    def _process_task( self, task ):
        self._add_line(task + ";")

    def _process_assumption( self, ass ):
        self.assumption_index = self.assumption_index + 1
        self._add_line("- %s%d[add] :: " % (ass[0], self.assumption_index))
        self._add_line("%s;"%ass[1])
        
    def _process_observation( self, obs ):
        # observations are named 1,2,3,... so we do not write these labels, but use the
        # implicit numbering instead.
        name,motivations,observation = obs

        self._add_line("+ [add] ::")

        directive = self._pp(" ".join([x for x in motivations if type(x) in (str,unicode)]))
        subderivations = [x for x in motivations if type(x)==dict]

        debug("Motivations are %s"%motivations)

        self._push_location("motivation")
        self._add_line("{ %s } ;"%directive)

        j = 0
        for sdm in subderivations:
            for sd in sdm["derivations"]:
                self._push_location("subderivation")
                self._push_location(j)
                self._process_derivation(sd)
                self._pop_location()
                self._pop_location()
            j += 1
        
        self._pop_location()

        self._add_line("%s ;"%observation)

