from init_socos import *
from pc.parsing.TeXParser import *
from pc.parsing.IBPParser import *
import time
import json
import re
import sys
from pc.semantic import Message
#from pc.parsing.ParseError import *
#from pc.parsing.AST import *
#from pc.semantic.Message import *

def debug( x ):
    sys.stderr.write("["+time.strftime("%a, %d %b %Y %H:%M:%S", time.localtime())+"] ")
    #sys.stderr.write(unicode(x).encode("latin-1","replace")+"\n")
    sys.stderr.write(x.decode("latin-1").encode("utf-8","replace")+"\n")
    sys.stderr.flush()

class JSON2ASTException( Exception ):

    def __init__( self, location, msg ):
        Exception.__init__(self, msg)
        self.location = location


class JSON2AST( object ):

    # This class builds an AST derivation from JSON object,
    # ???? and a mapping from line numbers to "location lists".

    def __init__(self, json, session_id = None):
        self.json = json
        self.session_id = session_id
        self.location = []
        self.lines = []
        self.line2location = []

        self.texdecimalsep = None
        self.textuplesep = None
        self.texparser = None

        self.assumption_index = 0
        self.errors = []


    def convert( self ):
        #debug("JSON:")
        #debug(json.dumps(self.json, indent=2))

        return self.process_list(self.json, "context")
        
        # debug("Document:\n%s"%"\n".join(["%-50s %% %s"%(self.lines[i],self.line2location[i]) for i in range(len(self.lines))]))
        # return "\n".join(self.lines)

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
            debug("Ignored message: %s" % msg.get_message())
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
            #print tree_to_str(msg.ast)
            #print msg.ast.json_location
            line = msg.ast.start_line()-1
            column = msg.ast.start_pos()
            if msg.ast.json_location:
                location = msg.ast.json_location
            else:
                if msg.element.get_attribute()=="step":
                    # special location for step: use the motivation AST for line
                    line += 2
                if 0 <= line < len(self.line2location):
                    location = self.line2location[line]

        thm = ""
          
        if msg.type=="ERROR":
            if hasattr(msg, "filename") and msg.filename.endswith(".pvs"): 
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
        debug(ret["thm"])
        debug(ret["message"])
        sys.stderr.write(ret["original_message"])
        debug("\n===========")
        debug(ret["original_message"])
       
        return ret
            

    def _discard_empty_strings( self, list ):
        return [x for x in list if len(x)>0]

    def _get_location( self ):
        return self.location[:]

    def _lookup_value( self, dict, attr ):
        if attr in dict:
            return dict[attr]
        else:
            raise JSON2ASTException(self._get_location(),"no such attribute: '%s'"%attr)

    def _pp( self, string ):
        string_re = re.compile(r'(["\'])((?:\\\1|.)*?)\1')
        return " ".join([x[1].replace(r'\"',r'"') for x in string_re.findall(string)])

    def _push_location( self, loc ):
        self.location.append(loc)

    def _pop_location( self ):
        self.location.pop()

    def _encode_str(self, s):
        # check for iso8859-1 compatibility
        try:
            s.encode("latin-1")
        except UnicodeEncodeError,e:
            raise JSON2ASTException(self._get_location(),"Non ISO-8859-1 symbol '%s' is disallowed."%(e.object[e.start:e.end]))
        
        return s.encode("latin-1")



    def _add_line( self, line ):
        self.lines.append(line)
        self.line2location.append(self._get_location())

        # check for iso8859-1 compatibility
        try:
            line.encode("latin-1")
        except UnicodeEncodeError,e:
            raise JSON2ASTException(self._get_location(),"Non ISO-8859-1 symbol '%s' is disallowed."%(e.object[e.start:e.end]))


    def process_list( self, current, attr ):
        res = []
        f = getattr(self, "_process_%s" % attr)
        self._push_location(attr)
        i = 0
        for x in self._lookup_value(current, attr + "s"):
            self._push_location(i)
            elem = f(x)
            elem.json_location = self._get_location()
            res.append(elem)
            self._pop_location()
            i += 1
        self._pop_location()
        return res
        
    def _process_context( self, context ):
        id = self._lookup_value(context, "name")
        if self.session_id:
            id = id + "%d" % self.session_id

        self._add_line("%s: context" % id)

        ctx = ParentNode('context', [LeafNode(self._encode_str("ID"), self._encode_str(id), 0, len(self.lines))])

        self._add_line("begin")

        decls = self.process_list(context, "declaration")

        derivs = self.process_list(context, "derivation")

        ctx.children += decls + derivs
        self._add_line("end %s" % id)

        return ctx


    def _process_declaration( self, decl ):
        self._add_line(decl + ";")
        ibp_parser = IBPParserStart("context_part")() #len(self.lines), 0
        parsed_decl = ibp_parser.parse(self._encode_str(decl + ";"))
        
        if ibp_parser.texdecimalsep:
            self.texdecimalsep = ibp_parser.texdecimalsep
            
        if ibp_parser.textuplesep:
            self.textuplesep = ibp_parser.textuplesep
            
        if ibp_parser.texparser:
            self.texparser = ibp_parser.texparser

        return parsed_decl

    def _process_motivation( self, motivations ):
        directive = self._pp(" ".join([x for x in motivations if type(x) in (str, unicode)]))                
        subderivations = [x for x in motivations if type(x) == dict]
        self._add_line('{ %s } ;' % directive)
        parser_motivation = self.GetParser("motivation")
        try:
            motivation_ast = parser_motivation.parse(self._encode_str("{ %s } ;" % directive))
        except ParseException, e:
            self.errors += map(lambda x:(Message.FileError)(x.msg, "", x.lineno, x.pos, {"location": self._get_location()}), e.errors)
            motivation_ast = parser_motivation.parse(self._encode_str("{  } ;"))
            motivation_ast.parse_ok = False

        motivation_ast.json_location = self._get_location()
        j = 0
        for sdm in subderivations:
            for sd in sdm["derivations"]:
                self._push_location("subderivation")
                self._push_location(j)
                motivation_ast.children.append(self._process_derivation(sd))
                self._pop_location()
                self._pop_location()
                j += 1
        return motivation_ast


    def _process_derivation( self, der ):
        id = self._lookup_value(der, "name")
        self._add_line("* %s:" % id)

        deriv_ast = ParentNode('derivation',[])
        deriv_ast.json_location = self._get_location()

        deriv_ast.children.append(LeafNode(self._encode_str("ID"), self._encode_str(id), 0, len(self.lines)))

        task_ast = None
        
        task = self._discard_empty_strings(self._lookup_value(der, "task"))
        task_ast = []
        if task: #self.process_list(der,"task")
            i = 0
            for t in self._lookup_value(der, "task"):
                self._push_location("task")
                self._push_location(i)
                task_ast += self._process_task(t)
                self._pop_location()
                self._pop_location()
        else: 
            self._add_line("derivation;")
            task_ast = []

        deriv_ast.children += task_ast
            
        deriv_ast.children += self.process_list(der, "assumption")
        
        deriv_ast.children += self.process_list(der, "observation")
        
        self._add_line("|-")

        chain = self._lookup_value(der, "chain")
        i = 0
        terms = self._lookup_value(chain,"terms")
        relations = self._lookup_value(chain,"relations")
        motivations = self._lookup_value(chain,"motivations")
        
        if terms and not len(terms)==len(relations) + 1:
            raise JSON2ASTException(self._get_location(), "terms/relation count mismatch (got %d relations, expected %d)"%(len(relations),len(terms)-1))

        ibp_parser_term = self.GetParser("term") #len(self.lines), 0
        ibp_parser_relation = self.GetParser("relation") #len(self.lines), 0
        ibp_parser_motivation = self.GetParser("motivation") #len(self.lines), 0

        parse_terms = [];
        parse_steps = [];
        for term in terms:
            self._push_location("term")
            self._push_location(i)
            self._add_line("%s ;" % term)

            try:
                t = ibp_parser_term.parse(self._encode_str("%s ;" % term))
            except ParseException, e:
                self.errors += map(lambda x:(Message.FileError)(x.msg, "", x.lineno, x.pos, {"location": self._get_location()}), e.errors)
                t = ibp_parser_term.parse(self._encode_str("$x$;"))
                t.parse_ok = False
            
            t.json_location = self._get_location()
            
            parse_terms.insert(0, t)
            self._pop_location()
            self._pop_location()
            if i<len(relations):
                self._push_location("relation")
                self._push_location(i)
                self._add_line("%s" % relations[i])
                try:
                    rel = ibp_parser_relation.parse(self._encode_str(relations[i]))
                except ParseException, e:
                    self.errors += map(lambda x:(Message.FileError)(x.msg, "", x.lineno, x.pos, {"location": self._get_location()}), e.errors)
                    rel = ibp_parser_relation.parse(self._encode_str("$=$"))
                    rel.parse_ok = False

                rel.json_location = self._get_location()
                
                self._pop_location()
                self._pop_location()

                self._push_location("motivation")
                self._push_location(i)

                motivation = self._process_motivation(motivations[i])

                step = ParentNode("stepdetails", [rel, motivation])
                step.json_location = self._get_location()

                parse_steps.insert(0, step)


                self._pop_location()
                self._pop_location()
                i += 1

        parse_chain = parse_terms[0]
        i = 0;
        for st in parse_steps:
            parse_chain = ParentNode("chain", [parse_terms[i + 1], st, parse_chain])
            parse_chain.json_location = st.json_location
            i = i + 1
        deriv_ast.children.append(parse_chain)
        
        self._add_line("[] ;")
        check = LeafNode('check', "[]", 0, len(self.lines))
        check.json_location = self._get_location()
        deriv_ast.children.append(check)
        return deriv_ast

    def _process_task( self, task ):
        self._add_line(task + ";")
        ibp_parser = self.GetParser("goal") #len(self.lines), 0
        try:
            t = ibp_parser.parse(self._encode_str(task + ";"))
        except ParseException, e:
            self.errors += map(lambda x:(Message.FileError)(x.msg, "", x.lineno, x.pos, {"location": self._get_location()}), e.errors)
            t = ibp_parser.parse(self._encode_str("Theorem $true$;"))
            for x in t:
                x.parse_ok = False
        for x in t:
            x.json_location = self._get_location()
        return t
            
    def GetParser(self, start):
        parser = IBPParserStart(start)() #len(self.lines), 0
        parser.texdecimalsep = self.texdecimalsep
        parser.textuplesep = self.textuplesep
        parser.texparser = self.texparser
        return parser

    def _process_assumption(self, ass):
        self.assumption_index = self.assumption_index + 1
        assumption = "- %s%d[add] :: "  % (ass[0], self.assumption_index)
        self._add_line(assumption)
        self._add_line("%s;" % ass[1])
        ibp_parser = self.GetParser("assumption") #len(self.lines), 0
        try:
            a = ibp_parser.parse(self._encode_str("%s%s;" % (assumption, ass[1])))
        except ParseException, e:
            self.errors += map(lambda x:(Message.FileError)(x.msg, "", x.lineno, x.pos - len(assumption), {"location": self._get_location()}), e.errors)
            a = ibp_parser.parse(self._encode_str("%s $true$;" % assumption))
            a.parse_ok = False
            
        a.json_location = self._get_location()
        return a

        
    def _process_observation( self, obs ):
        # observations are named 1,2,3,... so we do not write these labels, but use the
        # implicit numbering instead.
        name, motivations, observation = obs

        obs_ast = ParentNode('derivation_decl', [])
        obs_ast.json_location = self._get_location()

        # observation has no name.
        self._add_line("+ [add] ::")
        self._push_location("motivation")

        motivation = self._process_motivation(motivations)

        self._pop_location()

        obs_ast.children.append(LeafNode("KEY_ADD", "add"))
        obs_ast.children.append(motivation)
        

        self._add_line("%s ;" % observation)

        parser_ibp_expression = self.GetParser("ibp_expression")
        
        try:
            obs_pred = parser_ibp_expression.parse(self._encode_str(observation))
        except ParseException, e:
            self.errors += map(lambda x:(Message.FileError)(x.msg, "", x.lineno, x.pos, {"location": self._get_location()}), e.errors)
            obs_pred = parser_ibp_expression.parse(self._encode_str("$true$"))
            obs_pred.parse_ok = False


        obs_pred.json_location = self._get_location()
        obs_stm = ParentNode("observation", [obs_pred])
        obs_stm.parse_ok = obs_pred.parse_ok
        obs_stm.json_location = self._get_location()
        obs_ast.children.append(obs_stm)

        return obs_ast
