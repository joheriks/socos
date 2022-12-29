
'''
Module containing parser for the IBP language.
'''

from pc.parsing.ParseError import ParseError, ParseException
from pc.parsing.PVSParser import *
from pc.parsing.AST import ParentNode, LeafNode, Node
from pc.parsing.ParserUtil import expandGrammar, typeexpr as pvs_type_expr, make_list
from pc.parsing.IBPLexer import IBPLexer
from pc.parsing.TeXParser import *
from pc.parsing.Token import Token
from pc.util.Utils import *

import sys
import time

def debug( x ):
    sys.stderr.write("["+time.strftime("%a, %d %b %Y %H:%M:%S", time.localtime())+"] ")
    sys.stderr.write(unicode(x).encode("latin-1","replace")+"\n")
    sys.stderr.flush()



class IBPParser(PVSParser):
    '''Parser for the IBP (Invariant Based Programming) language. See grammar
    specification in doc directory.'''


    def __init__( self,
                  lexerclass = IBPLexer,
                  start = 'context_list',
                  tabmodule = 'ibp_parsetab',
                  debug = False,
                  debugfile = 'ibp_parser.out',
                  write_tables = False ):
        PVSParser.__init__(self,lexerclass,start,tabmodule,debug,debugfile,write_tables)

        self.pdebug = 0
        self.texdecimalsep = None
        self.textuplesep = None
        self.texparser = None


    ############### PVS/TEX EXPRESSIONS #######################################

    def p_ibp_expression( self, p ):
        '''ibp_expression : expression
                          | tex_expression'''
        p[0] = p[1]


    def p_tex_expression( self, p ):
        '''tex_expression : TEX'''
        par = GetTeXExprParserClass(self.texparser, self.texdecimalsep, self.textuplesep)
        parser = par(p.slice[1].lineno, p.slice[1].lexpos + 1) # account for $
        #print "Parsing: %s" % p[1] 
        p[0] = parser.parse(p[1])

    def p_ibp_id_list( self, p ):
        '''ibp_id_list : tex_id_list'''
        p[0] = p[1]


    def p_tex_id_list( self, p ):
        '''tex_id_list : TEX'''
        par = GetTeXIdListParserClass(self.texparser, self.texdecimalsep, self.textuplesep)
        parser = par(p.slice[1].lineno, p.slice[1].lexpos + 1, start = "id_list") # account for $
        p[0] = parser.parse(p[1])

    def p_id( self, p ):
        '''id : ID'''
        # __ is reserved for Socos, ? fails with ProofLite
        for forbidden in ('__','?'):
            if forbidden in p[1]:
                self.errors.append(ParseError("'%s' not allowed in identifiers"%forbidden,
                                              p.lineno(1), p.lexpos(1), type='parsing'))
        p[0] = LeafNode.make(p, 1)


    ############### CONTEXTS ##################################################


    def p_context(self, p):
        '''context : id COLON KEY_CONTEXT KEY_BEGIN                   KEY_END id
                   | id COLON KEY_CONTEXT KEY_BEGIN context_part_list KEY_END id'''

        p[0] = ParentNode('context', [ p[1] ])
        if isinstance(p[5], list):
            p[0].children += p[5]

        self._check_id_match(p, 'context')


    def p_context_part(self, p):
        '''context_part : extending
                        | importing
                        | texdecimalsep
                        | textuplesep
                        | texparser
                        | try SEMI_COLON
                        | procedure
                        | const_var_decl
                        | derivation'''
        p[0] = p[1]

        # report an error if top-level derivation has no identifier
        if p[0].type=="derivation" and not p[0]["ID"]:
            self.errors.append(ParseError("identifier expected for top-level derivation",
                                          p[0].start_line(),p[0].start_pos(), type='parsing'))


    def p_const_var_decl( self, p ):
        '''const_var_decl : constdecl
                          | recursivedecl
                          | vardecl'''
        p[0] = p[1]


    def p_extending(self, p):
        '''extending : KEY_EXTENDING context_name_list SEMI_COLON
                     | KEY_EXTENDING context_name_list KEY_EXTENDING'''

        if  p.slice[3].type == 'KEY_EXTENDING':
            self.errors.append(
                ParseError.make_from_p(
                'Missing semicolon between extending declarations',
                p, 3, type='parsing'
                ))
        p[0] = ParentNode('extending', p[2])


    def p_texdecimalsep(self, p): 
        """texdecimalsep : KEY_TEXDECIMALSEP COLON STRING SEMI_COLON""" 
        p[0] = LeafNode("texdecimalsep", p[3], p.lexpos(1), p.lineno(1)) 
        self.texdecimalsep = p[3]

    def p_textuplesep(self, p):
        """textuplesep : KEY_TEXTUPLESEP COLON STRING SEMI_COLON"""
        p[0] = LeafNode("textuplesep", p[3], p.lexpos(1), p.lineno(1))
        self.textuplesep = p[3]

    def p_texparser(self, p):
        """texparser : KEY_TEXPARSER COLON STRING SEMI_COLON"""
        p[0] = LeafNode("texparser", p[3], p.lexpos(1), p.lineno(1))
        self.texparser = p[3]

    def p_context_name_list( self, p ):
        '''context_name_list : context_name
                             | context_name_list COMMA context_name'''

        if len(p) == 4:
            p[1].append(p[3])
            p[0] = p[1]
        else:
            p[0] = [ p[1] ]


    def p_context_name(self, p):
        '''context_name : id'''
        p[0] = ParentNode('context_name',[p[1]])


    def p_importing(self, p):
        '''importing : KEY_IMPORTING theory_name_list SEMI_COLON'''
        p[0] = ParentNode('importing', p[2])


    def p_try_clause( self, p ):
        '''try_clause : context_name
                      | literal'''
        p[0] = p[1]
        

    def p_literal( self, p ):
        '''literal : STRING'''
        p[0] = LeafNode('literal',p[1],p.lexpos(1), p.lineno(1))


    def p_try( self, p ):
        '''try : KEY_BY try_clause
               | KEY_BY try_clause COMMA try_clause'''
        p[0] = ParentNode('try',[pget(p,"try_clause")])
        if len(p)==5:
            p[0].children.append(p[4])


    def p_constdecl_a( self, p ):
        '''constdecl : id               COLON $pvs_type_expr SEMI_COLON
                     | id               COLON $pvs_type_expr EQUAL ibp_expression SEMI_COLON
                     | id_list          COLON $pvs_type_expr SEMI_COLON
                     | id_list          COLON $pvs_type_expr EQUAL ibp_expression SEMI_COLON'''
        p[0] = ParentNode('constdecl_list',ensure_list(p[1])+[p[3]])
        if len(p)==7: p[0].children.append(p[5])
        
    expandGrammar(p_constdecl_a,pvs_type_expr=pvs_type_expr)


    def p_constdecl_b( self, p ):
        '''constdecl : id bindings_list COLON $pvs_type_expr SEMI_COLON
                     | id bindings_list COLON $pvs_type_expr EQUAL ibp_expression SEMI_COLON'''
        p[0] = ParentNode('constdecl_binding',[p[1]]+p[2]+[p[4]])
        if len(p)==8: p[0].children.append(p[6])
        
    expandGrammar(p_constdecl_b,pvs_type_expr=pvs_type_expr)


    def p_recursivedecl( self, p ):
        '''recursivedecl : id               COLON KEY_RECURSIVE $pvs_type_expr EQUAL ibp_expression KEY_MEASURE ibp_expression SEMI_COLON
                         | id               COLON KEY_RECURSIVE $pvs_type_expr EQUAL ibp_expression KEY_MEASURE ibp_expression KEY_BY ibp_expression SEMI_COLON
                         | id_list          COLON KEY_RECURSIVE $pvs_type_expr EQUAL ibp_expression KEY_MEASURE ibp_expression SEMI_COLON
                         | id_list          COLON KEY_RECURSIVE $pvs_type_expr EQUAL ibp_expression KEY_MEASURE ibp_expression KEY_BY ibp_expression SEMI_COLON
                         | id bindings_list COLON KEY_RECURSIVE $pvs_type_expr EQUAL ibp_expression KEY_MEASURE ibp_expression SEMI_COLON
                         | id bindings_list COLON KEY_RECURSIVE $pvs_type_expr EQUAL ibp_expression KEY_MEASURE ibp_expression KEY_BY ibp_expression SEMI_COLON'''
        if type(p[2])==list:
            p[0] = ParentNode('recdecl_binding',[p[1]]+p[2]+[p[5],p[7],p[9]])
            if len(p)==13: p[0].children.append(p[11])
        else:
            if type(p[1])==list: ids = p[1]
            else: ids = [p[1]]
            p[0] = ParentNode('recdecl_list',ids+[p[4],p[6],p[8]])
            if len(p)==12:
                p[0].children.append(p[10])
                
    expandGrammar(p_recursivedecl, pvs_type_expr = pvs_type_expr)


    def p_vardecl( self, p ):
        '''vardecl : id COLON       KEY_VAR $pvs_type_expr SEMI_COLON
                   | id_list COLON  KEY_VAR $pvs_type_expr SEMI_COLON'''
        p[0] = ParentNode("vardecl",ensure_list(p[1])+[p[4]])

    expandGrammar(p_vardecl,pvs_type_expr=pvs_type_expr)


    def p_pvardecl( self, p ):
        '''pvardecl : id COLON       KEY_PVAR $pvs_type_expr SEMI_COLON
                    | id_list COLON  KEY_PVAR $pvs_type_expr SEMI_COLON'''
        p[0] = ParentNode("pvardecl",ensure_list(p[1])+[p[4]])

    expandGrammar(p_pvardecl,pvs_type_expr=pvs_type_expr)


    ############### PROCEDURES ################################################


    def _procedure( self, p, id, signature=[], spec_part_list=[], procedure_contents_list=[] ):
        self._check_id_match(p,'procedure')
        children = [id]
        if signature: children.append(ParentNode('formals',signature))
        if spec_part_list: children.append(ParentNode('spec',spec_part_list))
        if procedure_contents_list: children.append(ParentNode('body', procedure_contents_list))
        p[0] = ParentNode('procedure',children)
        

    def p_procedure_a(self,p):
        '''procedure : id COLON KEY_PROCEDURE KEY_BEGIN KEY_END id'''
        self._procedure(p,p[1])

    def p_procedure_b(self,p):
        '''procedure : id COLON KEY_PROCEDURE spec_part_list KEY_BEGIN KEY_END id'''
        self._procedure(p,p[1],spec_part_list=p[4])

    def p_procedure_c(self,p):
        '''procedure : id COLON KEY_PROCEDURE KEY_BEGIN procedure_contents_list KEY_END id'''
        self._procedure(p,p[1],procedure_contents_list=p[5])

    def p_procedure_d(self,p):
        '''procedure : id COLON KEY_PROCEDURE spec_part_list KEY_BEGIN procedure_contents_list KEY_END id'''
        self._procedure(p,p[1],spec_part_list=p[4],procedure_contents_list=p[6])

    def p_procedure_e(self,p):
        '''procedure : id signature COLON KEY_PROCEDURE KEY_BEGIN KEY_END id'''
        self._procedure(p,p[1],signature=p[2])
        
    def p_procedure_f(self,p):
        '''procedure : id signature COLON KEY_PROCEDURE spec_part_list KEY_BEGIN KEY_END id'''
        self._procedure(p,p[1],signature=p[2],spec_part_list=p[5])

    def p_procedure_g(self,p):
        '''procedure : id signature COLON KEY_PROCEDURE KEY_BEGIN procedure_contents_list KEY_END id'''
        self._procedure(p,p[1],signature=p[2],procedure_contents_list=p[6])

    def p_procedure_h(self,p):
        '''procedure : id signature COLON KEY_PROCEDURE spec_part_list KEY_BEGIN procedure_contents_list KEY_END id'''
        self._procedure(p,p[1],signature=p[2],spec_part_list=p[5],procedure_contents_list=p[7])


    def p_variant(self, p):
        '''variant : STAR_STAR ibp_expression SEMI_COLON'''
        p[0] = ParentNode('variant', [ p[2] ])


    def p_signature( self, p ):
        '''signature : LBRACKET RBRACKET
                     | LBRACKET_RBRACKET
                     | LBRACKET param_list RBRACKET
                     | LBRACKET param_list SEMI_COLON RBRACKET'''
        p[0] = p[2] if len(p)>2 and (type(p[2])==list) else []
       

    def p_param( self, p ):
        '''param : const
                 | valres
                 | result'''
        p[0] = p[1]

    def p_const( self, p ):
        '''const : id_list COLON $pvs_type_expr'''
        p[0] = ParentNode("const",p[1]+[p[3]])
    expandGrammar(p_const,pvs_type_expr=pvs_type_expr)

    def p_valres( self, p ):
        '''valres : id_list COLON KEY_VALRES $pvs_type_expr'''
        p[0] = ParentNode("valres",p[1]+[p[4]])
    expandGrammar(p_valres,pvs_type_expr=pvs_type_expr)

    def p_result( self, p ):
        '''result : id_list COLON KEY_RESULT $pvs_type_expr'''
        p[0] = ParentNode("result",p[1]+[p[4]])
    expandGrammar(p_result,pvs_type_expr=pvs_type_expr)


    def p_procedure_contents_list( self, p ):
        '''procedure_contents_list : constdecl_list
                                   | pvardecl_list
                                   | constdecl_list pvardecl_list
                                   | diagram
                                   | constdecl_list diagram
                                   | pvardecl_list diagram
                                   | constdecl_list pvardecl_list diagram '''
        p[0] = []
        if phas(p,"constdecl_list"): p[0].append(pget(p,"constdecl_list"))
        if phas(p,"pvardecl_list"): p[0].append(pget(p,"pvardecl_list"))
        if phas(p,"diagram"): p[0].append(pget(p,"diagram"))


    def p_spec_part( self, p ):
        '''spec_part : pre
                     | anon_post
                     | named_post
                     | variant'''
        p[0] = p[1]
        

    # The precondition may be given as either:
    #
    # (1) PRE <expr1>; PRE <expr2>; ...
    # (2) PRE BEGIN * <expr1>; * <expr2>; ... END

    def p_pre_part( self, p ):
        '''pre_part : KEY_PRE ibp_expression SEMI_COLON'''
        p[0] = ParentNode('constraint',[p[2]])

    p_pre_part_list = make_list("pre_part_list","pre_part")

    def p_pre_1( self, p ):
        '''pre : pre_part_list'''
        p[0] = ParentNode('pre',p[1])


    def p_pre_2( self, p ):
        '''pre : KEY_PRE KEY_BEGIN KEY_END
               | KEY_PRE KEY_BEGIN constraint_list KEY_END'''
        p[0] = ParentNode('pre',p[3] if len(p)==5 else [],
                          p.lexpos(1),p.lineno(1),p.lexpos(len(p)-1),p.lineno(len(p)-1))
        

    # A postcondition may be given in either of the following forms:
    #
    # (1) [<id> :] POST <expr1>; POST <expr2>; ...            
    # (2) [<id> :] POST BEGIN * <expr1>; * <expr2>; ... END [<id>]
    #
    # If the <id> and colon are omitted, an anonymous postcondition is created.
    # Anon. and names posts must not be mixed, this is caught by the semantic
    # checker

    def p_post_part( self, p ):
        '''post_part : KEY_POST ibp_expression SEMI_COLON'''
        p[0] = ParentNode('constraint',[p[2]])

    p_post_past_list = make_list("post_part_list","post_part")
    

    def p_named_post_1( self, p ):
        '''named_post : id COLON post_part_list'''
        p[0] = ParentNode('post',[p[1]]+p[3])


    def p_named_post_2( self, p ):
        '''named_post : id COLON KEY_POST KEY_BEGIN KEY_END id
                      | id COLON KEY_POST KEY_BEGIN constraint_list KEY_END id'''
        self._check_id_match(p,'post')
        p[0] = ParentNode('post',[p[1]])
        if len(p)==8:
            p[0].children += p[5]

        
    def p_anon_post_1( self, p ):
        '''anon_post : post_part_list'''
        p[0] = ParentNode('post',p[1])


    def p_anon_post_2( self, p ):
        '''anon_post : KEY_POST KEY_BEGIN KEY_END
                     | KEY_POST KEY_BEGIN constraint_list KEY_END'''
        p[0] = ParentNode('post',p[3] if len(p)==5 else [],
                          p.lexpos(1),p.lineno(1),p.lexpos(len(p)-1),p.lineno(len(p)-1))
            

    def p_constraint( self, p ):
        '''constraint : STAR ibp_expression SEMI_COLON'''
        p[0] = ParentNode('constraint', [ p[2] ])


    ############### DIAGRAMS ##################################################


    def p_diagram( self, p ):
        '''diagram : situation_list
                   | trs
                   | situation_list trs'''
        p[0] = ParentNode('diagram',p[1:])


    def p_situation( self, p ):
        '''situation : KEY_SITUATION KEY_BEGIN KEY_END
                     | KEY_SITUATION KEY_BEGIN situation_part_list KEY_END
                     | id COLON KEY_SITUATION KEY_BEGIN KEY_END id
                     | id COLON KEY_SITUATION KEY_BEGIN situation_part_list KEY_END id
        '''
        p[0] = ParentNode('situation', [])
        if phas(p,"id"):
            self._check_id_match(p, 'situation')
            p[0].children.append(pget(p,"id"))
        if phas(p,"situation_part_list"):
            p[0].children += pget(p,"situation_part_list")


    def p_situation_part_1( self, p ):
        '''situation_part : variant
                          | constraint
                          | pvardecl
                          | diagram'''
        p[0] = p[1]


    def p_situation_part_2( self, p ):
        '''situation_part : MINUS pvardecl'''
        p[0] = p[2]
    

    def p_choice(self, p):
        '''choice : KEY_CHOICE KEY_ENDCHOICE
                  | KEY_CHOICE trs_list KEY_ENDCHOICE'''
        p[0] = ParentNode.make('choice',pcan(p,"trs_list"),p)


    def p_if( self, p ):
        '''if : KEY_IF KEY_ENDIF
              | KEY_IF trs_list KEY_ENDIF
              | KEY_IF trs_list KEY_ENDIF proof'''
        p[0] = ParentNode.make('if',pcan(p,"trs_list")+pcan(p,"proof"),p)


    def p_trs(self, p):
        '''trs : stmt_list tail
               | tail'''
        x = []
        if phas(p,"stmt_list"):
            x += pget(p,"stmt_list")
        p[0] = ParentNode('trs',x+[pget(p,"tail")])
           

    def p_label_trs(self, p):
        '''label_trs : id COLON trs
                     | id COLON SEMI_COLON trs'''
        p[0] = ParentNode('labeled_trs',[pget(p,"id"),pget(p,"trs")])


    def p_tail_a(self, p):
        '''tail : goto
                | exit
                | if
                | choice
                | if SEMI_COLON
                | choice SEMI_COLON
                '''
        # allow semicolon terminator for tail statements---this is needed to
        # express e.g. "choice exit; x:=0 endchoice".
        # "choice exit x:=0 endchoice" is syntactically incorrect, since the
        # identifier x is parsed as a parameter to exit.

        p[0] = p[1]


    def p_tail_b(self, p):
        '''tail : KEY_CALL call label_trs_list KEY_ENDCALL
                | KEY_CALL call proof label_trs_list KEY_ENDCALL 
                | KEY_CALL call proof label_trs_list KEY_ENDCALL SEMI_COLON
                | KEY_CALL call SEMI_COLON label_trs_list KEY_ENDCALL
                | KEY_CALL call proof SEMI_COLON label_trs_list KEY_ENDCALL 
                | KEY_CALL call proof SEMI_COLON label_trs_list KEY_ENDCALL SEMI_COLON'''
        p[0] = pget(p,"call")
        p[0].children += pcan(p,"label_trs_list") + pcan(p,"proof")


    def p_exit_a( self, p ):
        '''exit : KEY_EXIT
                | KEY_EXIT SEMI_COLON
                | try SEMI_COLON KEY_EXIT
                | try SEMI_COLON KEY_EXIT SEMI_COLON
                | KEY_EXIT ID
                | KEY_EXIT ID SEMI_COLON
                | try SEMI_COLON KEY_EXIT ID
                | try SEMI_COLON KEY_EXIT ID SEMI_COLON'''
        p[0]=ParentNode('exit',[])
        if phas(p,"ID"): p[0].children.append(LeafNode.make(p,pgeti(p,"ID")))
        p[0].children += pcan(p,"proof")
        if phas(p,"try"): p[0].children.append(pget(p,"try"))
        if not p[0].children:
            p[0].children.append(LeafNode.make(p,1))

    
    def p_goto_a( self, p ):
        '''goto : KEY_GOTO ID
                | KEY_GOTO ID SEMI_COLON
                | try SEMI_COLON KEY_GOTO ID 
                | try SEMI_COLON KEY_GOTO ID SEMI_COLON
                | decreasing KEY_GOTO ID
                | decreasing KEY_GOTO ID SEMI_COLON
                | try SEMI_COLON decreasing KEY_GOTO ID 
                | try SEMI_COLON decreasing KEY_GOTO ID SEMI_COLON'''
        id = LeafNode.make(p,pgeti(p,'ID'))
        p[0] = ParentNode('goto',[id])
        if phas(p,"decreasing"):
            decreasing = pget(p,"decreasing")
            p[0].children.append(decreasing)
            if not decreasing.children: decreasing.children = [id]
        p[0].children += pcan(p,"proof")
        if phas(p,"try"): p[0].children.append(pget(p,"try"))


    def p_decreasing( self, p ):
        '''decreasing : KEY_DECREASING
                      | KEY_DECREASING SEMI_COLON
                      | KEY_DECREASING try SEMI_COLON
                      | KEY_DECREASING id_list 
                      | KEY_DECREASING id_list SEMI_COLON
                      | KEY_DECREASING id_list try SEMI_COLON'''
        p[0] = ParentNode('decreasing',[],p.lexpos(1),p.lineno(1),p.lexpos(len(p)-1),p.lineno(len(p)-1))
        p[0].children += pcan(p,"id_list")
        if phas(p,"try"):
            p[0].children.append(pget(p,"try"))


    ############### STATEMENTS ################################################


    def p_stmt( self, p ):
        '''stmt : assign SEMI_COLON
                | assert SEMI_COLON
                | assert proof SEMI_COLON
                | assume SEMI_COLON
                | havoc SEMI_COLON
                | call SEMI_COLON
                | call proof SEMI_COLON'''
        p[0] = p[1]
        p[0].children += pcan(p,"proof")


    def p_proof_a( self, p ):
        '''proof : derivation_list'''
        p[0] = p[1]


    def p_proof_b( self, p ):
        '''proof : try'''
        p[0] = [p[1]]


    def p_assert(self, p):
        '''assert : LBRACE ibp_expression RBRACE'''
        p[0] = ParentNode('assert', [ p[2] ])


    def p_assign(self, p):
        '''assign : id_list COLON_EQUAL expression_list'''


        left = ParentNode('left', [])
        right = ParentNode('right', [])
        p[0] = ParentNode('assign', [ left, right ])

        left.children = p[1]
        right.children = p[3]


        if len(p[1]) != len(p[3]):
            self.errors.append(
                ParseError.make_from_node(
                'number of assignables does not match number of values',
                left,
                type='parsing'
                ))


    def p_assume(self, p):
        '''assume : LBRACKET ibp_expression RBRACKET'''

        p[0] = ParentNode('assume', [ p[2] ])


    def p_havoc(self, p):
        '''havoc : id_list COLON_EQUAL QUESTIONMARK'''

        p[0] = ParentNode('havoc', p[1])


    def p_call(self, p):
        '''call : id LBRACKET RBRACKET
                | id LBRACKET_RBRACKET
                | id LBRACKET expression_list RBRACKET'''

        p[0] = ParentNode('call', [p[1]])
        if phas(p,"expression_list"):
            p[0].children.append(ParentNode('arguments',[p[3]]))


    def p_theory_name(self, p):
        '''theory_name : ID
                       | ID AT ID
                       | ID actuals
                       | ID AT ID actuals'''

        p[0] = ParentNode('theory_name', [ LeafNode.make(p, 1) ])

        p_len = len(p)

        if p_len == 3:
            # ID actuals
            p[0].children.append(p[2])
        elif p_len == 4:
            # ID AT ID
            p[0].children.append(LeafNode.make(p, 3))
        elif p_len == 5:
            # ID AT ID actuals
            p[0].children += [ LeafNode.make(p, 3) , p[4] ]

    p_context_list = make_list("context_list","context")

    p_id_list = make_list("id_list","id","COMMA")
    
    p_pvardecl_list = make_list("pvardecl_list","pvardecl")
    
    p_constdecl_list = make_list("constdecl_list","constdecl")

    p_theory_name_list = make_list("theory_name_list","theory_name","COMMA")

    p_context_part_list = make_list("context_part_list","context_part")

    p_label_trs_list = make_list("label_trs_list","label_trs")

    p_param_list = make_list("param_list","param","SEMI_COLON")

    p_situation_part_list = make_list("situation_part_list","situation_part")

    p_situation_list = make_list("situation_list","situation")

    p_spec_part_list = make_list("spec_part_list","spec_part")
 
    p_trs_list = make_list("trs_list","trs")

    p_constraint_list = make_list("constraint_list","constraint")

    p_stmt_list = make_list("stmt_list","stmt")


    ############### STRUCTURED DERIVATIONS ####################################


    def p_derivation( self, p ):
        '''derivation : STAR chain derivation_terminator
                      | STAR goal derivation_terminator
                      | STAR id COLON goal derivation_terminator
                      | STAR goal derivation_decls derivation_terminator
                      | STAR id COLON goal derivation_decls derivation_terminator
                      | STAR goal derivation_decls chain derivation_terminator
                      | STAR id COLON goal derivation_decls chain derivation_terminator
                      | STAR goal derivation_decls motivation derivation_terminator
                      | STAR id COLON goal derivation_decls motivation derivation_terminator
                      | STAR goal derivation_decls motivation chain derivation_terminator
                      | STAR id COLON goal derivation_decls motivation chain derivation_terminator'''
        p[0] = ParentNode('derivation',[])
        if phas(p,"id"): p[0].children.append(pget(p,"id"))
        if phas(p,"goal"): p[0].children += pget(p,"goal")
        if phas(p,"derivation_decls"): p[0].children += pget(p,"derivation_decls")
        if phas(p,"motivation"): p[0].children.append(pget(p,"motivation"))
        if phas(p,"chain"): p[0].children.append(pget(p,"chain"))
        if phas(p,"derivation_terminator"): p[0].children.append(pget(p,"derivation_terminator"))
             

    def p_derivation_terminator( self, p ):
        '''derivation_terminator : LBRACKET_RBRACKET SEMI_COLON '''
        p[0] = LeafNode('check',p[1],p.lexpos(1),p.lineno(1))
    

    def p_goal_a( self, p ):
        '''goal : KEY_AXIOM expression_list SEMI_COLON
                | KEY_CHALLENGE expression_list SEMI_COLON
                | KEY_CLAIM expression_list SEMI_COLON
                | KEY_CONJECTURE expression_list SEMI_COLON
                | KEY_COROLLARY expression_list SEMI_COLON
                | KEY_FACT expression_list SEMI_COLON
                | KEY_LAW expression_list SEMI_COLON
                | KEY_LEMMA expression_list SEMI_COLON
                | KEY_POSTULATE expression_list SEMI_COLON
                | KEY_PROPOSITION expression_list SEMI_COLON
                | KEY_SUBLEMMA expression_list SEMI_COLON
                | KEY_THEOREM expression_list SEMI_COLON'''
        p[0] = [ParentNode("goal",[LeafNode.make(p,1)]+p[2])]


    def p_goal_b( self, p ):
        '''goal : KEY_SIMPLIFY ibp_expression SEMI_COLON'''
        p[0] = [ParentNode("simplify",[p[2]])]
        
    def p_goal_c( self, p ):
        '''goal : KEY_SOLVE ibp_expression KEY_IN ibp_id_list SEMI_COLON'''
	#print pget(p, "ibp_id_list")
        x = ParentNode("id_list", pget(p, "ibp_id_list"))
	#print tree_to_str(x)
        p[0] = [ParentNode("solve",[p[2], x])]

    def p_goal_d( self, p ):
        '''goal : KEY_DERIVATION SEMI_COLON'''
        p[0] = []

    def p_motivation_part( self, p ):
        '''motivation_part : literal
                           | try
                           | add
                           | del
                           | use
                           | case'''
        p[0] = p[1]


    def p_add( self, p ):
        '''add : KEY_ADD ass_part_list'''
        p[0] = ParentNode("add",p[2])


    def p_del( self, p ):
        '''del : KEY_DEL ass_part_list'''
        p[0] = ParentNode("del",p[2])
        
        
    def p_ass_part_a( self, p ):
        '''ass_part : id'''
        p[0] = ParentNode("assumption_id",[p[1]])

    def p_ass_part_b( self, p ):
        '''ass_part : MINUS NUMBER'''
        p[0] = ParentNode("assumption_index",[LeafNode.make(p,1),LeafNode.make(p,2)])

    def p_ass_part_c( self, p ):
        '''ass_part : PLUS NUMBER'''
        p[0] = ParentNode("observation_index",[LeafNode.make(p,1),LeafNode.make(p,2)])

    def p_ass_part_d( self, p ):
        '''ass_part : MINUS'''
        p[0] = LeafNode("assumption_all",p[1],p.lexpos(1),p.lineno(1))
        
    def p_ass_part_e( self, p ):
        '''ass_part : PLUS'''
        p[0] = LeafNode("observation_all",p[1],p.lexpos(1),p.lineno(1))


    def p_use( self, p ):
        '''use : KEY_USE ID'''
        p[0] = ParentNode("use",[LeafNode.make(p,2)])


    def p_case_a( self, p ):
        '''case : KEY_CASE ibp_expression'''
        p[0] = ParentNode("case",[p[2]])

    def p_case_b( self, p ):
        '''case : KEY_CASE ibp_expression KEY_BY ID'''
        p[0] = ParentNode("case",[p[2],LeafNode.make(p,4)])
        

    def p_derivation_decls_a( self, p ):
        '''derivation_decls : VBAR_MINUS
                            | assumption_list VBAR_MINUS
                            | observation_list VBAR_MINUS
                            | assumption_list observation_list VBAR_MINUS'''
        p[0] = []
        if phas(p,"assumption_list"): p[0] += pget(p,"assumption_list")
        if phas(p,"observation_list"): p[0] += pget(p,"observation_list")


    def p_assumption( self, p ):
        '''assumption : MINUS expression_decl
                      | MINUS addflag COLON_COLON expression_decl
                      | MINUS id COLON_COLON expression_decl
                      | MINUS id addflag COLON_COLON expression_decl'''
        p[0] = ParentNode("derivation_decl",[])
        if phas(p,"id"): p[0].children.append(pget(p,"id"))
        if phas(p,"addflag"): p[0].children.append(pget(p,"addflag"))
        p[0].children += pget(p,"expression_decl")


    def p_observation( self, p ):
        '''observation : PLUS motivation ibp_expression SEMI_COLON
                       | PLUS addflag COLON_COLON motivation ibp_expression SEMI_COLON
                       | PLUS id COLON_COLON motivation ibp_expression SEMI_COLON
                       | PLUS id addflag COLON_COLON motivation ibp_expression SEMI_COLON'''
        p[0] = ParentNode("derivation_decl",[])
        if phas(p,"id"): p[0].children.append(pget(p,"id"))
        if phas(p,"addflag"): p[0].children.append(pget(p,"addflag"))
        p[0].children.append(pget(p,"motivation"))
        p[0].children.append(ParentNode("observation",[pget(p,"ibp_expression")]))

    
    def p_expression_decl_a( self, p ):
        '''expression_decl : constdecl
                           | recursivedecl
                           | vardecl'''
        p[0] = [p[1]]


    def p_expression_decl_b( self, p ):
        '''expression_decl : ibp_expression SEMI_COLON'''
        p[0] = [ParentNode("assumption",[p[1]])]


    def p_expression_decl_c( self, p ):
        '''expression_decl : TEX SEMI_COLON'''
        parser = GetTeXDeclParserClass(self.texparser, self.texdecimalsep, self.textuplesep)(p.slice[1].lineno,p.slice[1].lexpos+1) # account for $
        p[0] = parser.parse(pget(p,"TEX"))


    def p_addflag( self, p ):
        '''addflag : LBRACKET KEY_ADD RBRACKET'''
        #debug("Type %s, %s" % (p.slice[2].type, p[2]))
        p[0] = LeafNode.make(p,2)


    def p_motivation( self, p ):
        '''motivation : LBRACE RBRACE SEMI_COLON
                      | LBRACE motivation_part_list RBRACE SEMI_COLON
                      | LBRACE RBRACE SEMI_COLON derivation_list
                      | LBRACE motivation_part_list RBRACE SEMI_COLON derivation_list'''
        p[0] = ParentNode("motivation",[])
        if phas(p,"motivation_part_list"): p[0].children += pget(p,"motivation_part_list")
        if phas(p,"derivation_list"): p[0].children += pget(p,"derivation_list")
        
    
    def p_stepdetails( self, p ):
        '''stepdetails : relation motivation'''
        p[0] = ParentNode("stepdetails",[p[1],p[2]])
        #if phas(p,"motivation"): p[0].children.append(pget(p,"motivation"))
        #if phas(p,"derivation_list"): p[0].children.append())


    def p_relation_1( self, p ):
        '''relation : ID'''
        p[0] = ParentNode("relation",[ParentNode("idop",[LeafNode.make(p,1)])])
        

    def p_relation_2( self, p ):
        '''relation : $binop'''
        p[0] = ParentNode("relation",[ParentNode("idop",[LeafNode.make(p,1)])])
    expandGrammar(p_relation_2,binop=binops)

    def p_relation_3( self, p ):
        '''relation : tex_expression'''
        p[0] = ParentNode("relation",[p[1]])


    def p_term( self, p ):
        '''term : ibp_expression SEMI_COLON'''
        p[0] = ParentNode("term",[p[1]])


    def p_chain_a( self, p ):
        '''chain : term stepdetails chain
                 | term stepdetails term'''
        p[0] = ParentNode("chain",[p[1],p[2],p[3]])
                                 
    def p_chain_b( self, p ):
        '''chain : term stepdetails SEMI_COLON'''
        p[0] = ParentNode("chain",[p[1],p[2]])


    p_derivation_list = make_list("derivation_list","derivation")

    p_assumption_list = make_list("assumption_list","assumption")

    p_observation_list = make_list("observation_list","observation")

    p_motivation_part_list = make_list("motivation_part_list","motivation_part","SEMI_COLON")

    p_expression_list = make_list("expression_list","ibp_expression","COMMA")

    p_ass_part_list = make_list("ass_part_list","ass_part","COMMA")


    ############### INTERNAL METHODS ##########################################

    def _check_id_match(self, p, name):
        '''If the production p contains two ids, checks that the first matches the
        second one; if not, an error is appended. Case sensitive.'''
        fst = pget(p,"id",n=0)
        snd = pget(p,"id",n=1)
        if fst and snd and fst.value != snd.value:
            self.errors.append(ParseError("%s id '%s' does not match ending id '%s'"%(name,fst.value,snd.value),
                               snd.start_line(),snd.start_pos(),type="parsing"))



    ############### COMMON ERRORS #############################################

    def p_context_missing_begin(self, p):
        '''context : id COLON KEY_CONTEXT                   KEY_END ID
                   | id COLON KEY_CONTEXT context_part_list KEY_END ID'''

        p[0] = ParentNode('context', [ p[1] ])
        if isinstance(p[4], list):
            p[0].children += p[4]

        self.errors.append(
            ParseError.make_from_p(
            'missing BEGIN keyword in context "%s"' % p[1].value,
            p, 2, type='parsing'
            ))


    def p_procedure_contents_list_var_before_const(self, p):
        '''procedure_contents_list : pvardecl_list constdecl_list
                                   | pvardecl_list constdecl_list diagram'''

        error=ParseError('procedure variable declarations must come after constant declarations')
        self.errors.append(error)


def IBPParserStart(start):
    class IBPParserStart(IBPParser):
        def __init__( self,
                      lexerclass = IBPLexer,
                      start = start,
                      tabmodule = 'ibp_parsetab_%s' % start,
                      debug = False,
                      debugfile = 'ibp_parser_%s.out' % start,
                      write_tables = False ):
            IBPParser.__init__(self, lexerclass, start, tabmodule, debug, debugfile, write_tables)
    return IBPParserStart

