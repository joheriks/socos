
'''
Module containing PVS expression parser.
'''

from pc.parsing.Token import Token
from pc.parsing.PVSLexer import PVSLexer

from pc.parsing.ParserUtil import *
from pc.parsing.AST import Node, ParentNode, LeafNode
from pc.parsing.ParseError import ParseException, ParseError
from pc.parsing.AbstractParser import AbstractParser


class PVSParser(AbstractParser):
    '''Parser for PVS expressions.'''

    def __init__( self,
                  lexerclass = PVSLexer,
                  start = 'expression',
                  tabmodule = 'pvs_parsetab',
                  debug = False,
                  debugfile = 'pvs_parser.out',
                  write_tables = False ):
        '''Constructor. Parameter lexer should be the lexer class to use,
        start is the start symbol and tabmodule is
        the name to use for the stored parser tables.'''
        AbstractParser.__init__(self,lexerclass(),start,tabmodule,debug,debugfile,write_tables)
        self.tokens = lexerclass.tokens
        self.module = self
        self._yacc()


    def p_expression(self, p):
        '''expression : binop_1'''

        p[0] = p[1]


    def p_binop_expression_1(self, p):
        '''binop_1 : binop_2 VBAR_MINUS binop_1
                   | binop_2 VBAR_EQUAL binop_1
                   | binop_2'''

        if len(p) > 2:
            p[0] = ParentNode(p.slice[2].type, [ p[1], p[3] ])
        else:
            p[0] = p[1]


    def p_binop_expression_2(self, p):
        '''binop_2 : binop_3 KEY_IFF     binop_2
                   | binop_3 LT_EQUAL_GT binop_2
                   | binop_3'''

        if len(p) > 2:
            p[0] = ParentNode(p.slice[2].type, [ p[1], p[3] ])
        else:
            p[0] = p[1]


    def p_binop_expression_3(self, p):
        '''binop_3 : binop_4 KEY_IMPLIES binop_3
                   | binop_4 EQUAL_GT    binop_3
                   | binop_4 KEY_WHEN    binop_3
                   | binop_4'''

        if len(p) > 2:
            p[0] = ParentNode(p.slice[2].type, [ p[1], p[3] ])
        else:
            p[0] = p[1]


    def p_binop_expression_4(self, p):
        '''binop_4 : binop_5 KEY_OR       binop_4
                   | binop_5 BSLASH_SLASH binop_4
                   | binop_5 KEY_XOR      binop_4
                   | binop_5 KEY_ORELSE   binop_4
                   | binop_5'''

        if len(p) > 2:
            p[0] = ParentNode(p.slice[2].type, [ p[1], p[3] ])
        else:
            p[0] = p[1]


    def p_binop_expression_5(self, p):
        '''binop_5 : binop_6 KEY_AND      binop_5
                   | binop_6 AMPERSAND    binop_5
                   | binop_6 SLASH_BSLASH binop_5
                   | binop_6 KEY_ANDTHEN  binop_5
                   | binop_6'''

        if len(p) > 2:
            p[0] = ParentNode(p.slice[2].type, [ p[1], p[3] ])
        else:
            p[0] = p[1]



    def p_binop_expression_6(self, p):
        '''binop_6 : TILDE   binop_6
                   | KEY_NOT binop_6
                   | binop_6 TILDE binop_6
                   | binop_7'''

        if len(p) > 3:
            p[0] = ParentNode(p.slice[2].type, [ p[1], p[3] ])
        elif len(p) > 2:
            p[0] = ParentNode(p.slice[1].type, [ p[2] ])
        else:
            p[0] = p[1]


    def p_binop_expression_7(self, p):
        '''binop_7 : binop_7 EQUAL       binop_7_1
                   | binop_7 SLASH_EQUAL binop_7_1
                   | binop_7 EQUAL_EQUAL binop_7_1
                   | binop_7 LT          binop_7_1
                   | binop_7 LT_EQUAL    binop_7_1
                   | binop_7 GT          binop_7_1
                   | binop_7 GT_EQUAL    binop_7_1
                   | binop_7 LT_LT       binop_7_1
                   | binop_7 GT_GT       binop_7_1
                   | binop_7 LT_LT_EQUAL binop_7_1
                   | binop_7 GT_GT_EQUAL binop_7_1
                   | binop_7 LT_VBAR     binop_7_1
                   | binop_7 VBAR_GT     binop_7_1
                   | binop_7_1'''

        if len(p) > 2:
            p[0] = ParentNode(p.slice[2].type, [ p[1], p[3] ])
        else:
            p[0] = p[1]


    def p_binop_expression_7_1(self, p):
        '''binop_7_1 : binop_7_1 KEY_WHERE letbinding_list
                     | binop_7_2'''

        if len(p) > 2:
            p[0] = ParentNode('where', [ p[1] ] + p[3])
        else:
            p[0] = p[1]


    def p_binop_expression_7_2(self, p):
        '''binop_7_2 : binop_7_2 KEY_WITH LBRACKET assignment_list RBRACKET
                     | binop_8'''

        if len(p) > 2:
            p[0] = ParentNode('with', [ p[1] ] + p[4])
        else:
            p[0] = p[1]


    def p_binop_expression_8(self, p):
        '''binop_8 : binop_8 HASH binop_9
                   | binop_9'''

        if len(p) > 2:
            p[0] = ParentNode(p.slice[2].type, [ p[1], p[3] ])
        else:
            p[0] = p[1]


    def p_binop_expression_9(self, p):
        '''binop_9 : binop_9 AT_AT     binop_10
                   | binop_9 HASH_HASH binop_10
                   | binop_10'''

        if len(p) > 2:
            p[0] = ParentNode(p.slice[2].type, [ p[1], p[3] ])
        else:
            p[0] = p[1]


    def p_binop_expression_10(self, p):
        '''binop_10 : binop_10 PLUS      binop_11
                    | binop_10 MINUS     binop_11
                    | binop_10 PLUS_PLUS binop_11
                    | binop_11'''

        if len(p) > 2:
            p[0] = ParentNode(p.slice[2].type, [ p[1], p[3] ])
        else:
            p[0] = p[1]


    def p_binop_expression_11(self, p):
        '''binop_11 : binop_11 SLASH       binop_11_1
                    | binop_11 SLASH_SLASH binop_11_1
                    | binop_11 STAR        binop_11_1
                    | binop_11 STAR_STAR   binop_11_1
                    | binop_11_1'''

        if len(p) > 2:
            p[0] = ParentNode(p.slice[2].type, [ p[1], p[3] ])
        else:
            p[0] = p[1]


    def p_binop_expression_11_1(self, p):
        '''binop_11_1 : MINUS binop_11_1
                      | binop_12'''

        if len(p) == 3:
            p[0] = ParentNode(p.slice[1].type, [ p[2] ])
        else:
            p[0] = p[1]


    def p_binop_expression_12(self, p):
        '''binop_12 : binop_12 KEY_O binop_12_1
                    | binop_12_1'''

        if len(p) > 2:
            p[0] = ParentNode(p.slice[2].type, [ p[1], p[3] ])
        else:
            p[0] = p[1]


    def p_binop_expression_12_1(self, p):
        '''binop_12_1 : expr_cc_typexpr
                      | binop_12_2'''

        p[0] = p[1]


    def p_expression_typeexpr(self, p):
        '''expr_cc_typexpr : binop_12_1 COLON_COLON $typeexpr '''

        p[0] = ParentNode('COLON_COLON', [ p[1], p[3] ])
    expandGrammar(p_expression_typeexpr, typeexpr=typeexpr)


    def p_binop_expression_12_2(self, p):
        '''binop_12_2 : LT_GT binop_12_2
                      | LBRACKET_RBRACKET binop_12_2
                      | binop_13'''

        if len(p) > 2:
            p[0] = ParentNode(p.slice[1].type, [ p[2] ])
        else:
            p[0] = p[1]


    def p_binop_expression_13(self, p):
        '''binop_13 : binop_13 CARET binop_14
                    | binop_13 CARET_CARET binop_14
                    | binop_14'''

        if len(p) > 2:
            p[0] = ParentNode(p.slice[2].type, [ p[1], p[3] ])
        else:
            p[0] = p[1]


    def p_binop_expression_14(self, p):
        '''binop_14 : binop_14 SINGLE_QUOTE ID
                    | binop_14 SINGLE_QUOTE NUMBER
                    | binop_15'''

        if len(p) > 2:
            p[0] = ParentNode('SINGLE_QUOTE', [ p[1], LeafNode.make(p, 3) ])
        else:
            p[0] = p[1]


    def p_binop_expression_15(self, p):
        '''binop_15 : binop_15 arguments
                    | binop_101'''

        if len(p) > 2:
            p[0] = ParentNode('expression_arguments', [ p[1], p[2] ])
        else:
            p[0] = p[1]


    def p_binop_expression_101(self, p):
        '''binop_101 : subtype
                     | expr_list_2
                     | expr_list_3
                     | expr_list_4
                     | expr_list_5
                     | expr_let
                     | expr_if
                     | expr_cases
                     | expr_cond
                     | expr_binding
                     | lp_hash_assignment
                     | tableexpr
                     | name
                     | STRING
                     | id_excl_number
                     | NUMBER
                     | decimal'''

        if isinstance(p[1], Node):
            if p[1].type == 'subtype':
                p[0] = p[1].children[0]
            else:
                p[0] = p[1]
        else:
            p[0] = LeafNode.make(p, 1)


    def p_id_excl_number(self, p):
        '''id_excl_number : id_excl NUMBER'''

        p[0] = p[1]
        p[0].children.append(LeafNode.make(p, 2))


    def p_id_excl(self, p):
        '''id_excl : ID EXCLAMATION_MARK'''

        p[0] = ParentNode('EXCLAMATION_MARK',[ LeafNode.make(p, 1) ])


    def p_expression_list_1(self, p):
        '''expr_list_1 : LPAREN expression_list RPAREN'''

        p[0] = ParentNode('expression_list_1', p[2])


    def p_expression_list_2(self, p):
        '''expr_list_2 : LPAREN_COLON                 COLON_RPAREN
                       | LPAREN_COLON expression_list COLON_RPAREN'''

        p[0] = ParentNode('expression_list_2', [])
        if len(p) == 4:
            p[0].children = p[2]


    def p_expression_list_3(self, p):
        '''expr_list_3 : LBRACKET_VBAR                 VBAR_RBRACKET
                       | LBRACKET_VBAR expression_list VBAR_RBRACKET'''

        p[0] = ParentNode('expression_list_3', [])
        if len(p) == 4:
            p[0].children = p[2]


    def p_expression_list_4(self, p):
        '''expr_list_4 : LPAREN_VBAR                 VBAR_RPAREN
                       | LPAREN_VBAR expression_list VBAR_RPAREN'''

        p[0] = ParentNode('expression_list_4', [])
        if len(p) == 4:
            p[0].children = p[2]


    def p_expression_list_5(self, p):
        '''expr_list_5 : LBRACE_VBAR                 VBAR_RBRACE
                       | LBRACE_VBAR expression_list VBAR_RBRACE'''

        p[0] = ParentNode('expression_list_5', [])
        if len(p) == 4:
            p[0].children = p[2]


    def p_arguments(self, p):
        '''arguments : LPAREN expression_list RPAREN'''

        p[0] = ParentNode('arguments', p[2])


    def p_expression_list(self, p):
        '''expression_list : expression
                           | expression_list COMMA expression'''

        if isinstance(p[1], list):
            p[1].append(p[3])
            p[0] = p[1]
        else:
            p[0] = [ p[1] ]


    def p_assignarg(self, p):
        '''assignarg : expr_list_1
                     | SINGLE_QUOTE ID
                     | SINGLE_QUOTE NUMBER'''

        if len(p) == 3:
            p[0] = ParentNode('SINGLE_QUOTE', [ LeafNode.make(p, 2) ])
        else:
            p[0] = p[1]


    def p_assignarg_plus(self, p):
        '''assignargplus : assignarg
                         | assignargplus assignarg'''

        p[0] = ParentNode('ass_arg_plus', [])
        if len(p) >= 2:
            p[0].children.append(p[1])
        if len(p) >= 3:
            p[0].children.append(p[2])


    def p_assignargs(self, p):
        '''assignargs : ID
                      | id_excl_number
                      | NUMBER
                      | assignargplus'''

        p[0] = ParentNode('assignargs', [])

        if isinstance(p[1], ParentNode):
            # Must be id_excl_number or assignargplus
            p[0].children = [ p[1] ]
        else:
            # Must be ID or NUMBER
            p[0].children = [ LeafNode.make(p, 1) ]


    def p_assignment(self, p):
        '''assignment : assignargs COLON_EQUAL expression
                      | assignargs VBAR_MINUS_GT expression '''

        if p.slice[2].type == 'COLON_EQUAL':
            p[0] = ParentNode('assignment_ce', [])
        else:
            p[0] = ParentNode('assignment_vmg', [])

        p[0].children = [ p[1], p[3] ]


    def p_assignment_list(self, p):
        '''assignment_list : assignment
                           | assignment_list COMMA assignment'''

        if isinstance(p[1], list):
            p[1].append(p[3])
            p[0] = p[1]
        else:
            p[0] = [ p[1] ]


    def p_expression_assignment_list(self, p):
        '''lp_hash_assignment : LPAREN_HASH assignment_list HASH_RPAREN'''

        p[0] = ParentNode('assignment_list', p[2])


    def p_ifexpr(self, p):
        '''expr_if : KEY_IF expression KEY_THEN expression KEY_ELSE expression KEY_ENDIF
                   | KEY_IF expression KEY_THEN expression elsif_list KEY_ELSE expression KEY_ENDIF'''

        p[0] = ParentNode('ifthen', [ p[2], p[4] ])
        if len(p) == 9:
            parent = p[0]
            for i in p[5]:
                node = ParentNode('elsif', [ i[0], i[1] ])
                parent.children.append(node)
                #parent = node
            parent.children.append(p[7])
        else:
            p[0].children.append(p[6])


    def p_elsif_list(self, p):
        '''elsif_list : KEY_ELSIF expression KEY_THEN expression
                      | KEY_ELSIF expression KEY_THEN expression elsif_list'''

        p[0] = [ (p[2], p[4]) ]
        if len(p) == 6:
            p[0] += p[5]


    def p_expression_bindingexpr(self, p):
        '''expr_binding : $bindingop lambdabindings COLON expression '''
        #
        # Note: we do not implement the rule where bindingop = opsym!, since
        # is is unclear how it is supposed to work. It appears to be unsupported
        # in the reference implementation of PVS as well.
        #
        if isinstance(p[1], Node): op = p[1]
        else: op = LeafNode.make(p,1)
        p[0] = ParentNode('bindingexpr',[op]+p[2]+[p[4]])
        ##p[0].children.append(p[1].children[0])
        ##p[0].children.append(ParentNode('lambdabindings', p[1].children[1:]))
        ##p[0].children.append(p[3])

    expandGrammar(p_expression_bindingexpr,
                  bindingop=['KEY_LAMBDA','KEY_FORALL','KEY_EXISTS', 'id_excl'])


    def p_lambdabindings_1( self, p ):
        '''lambdabindings : lambdabinding_list'''
        p[0] = [ParentNode("lambdabindings",p[1])]

    def p_lambdabindings_2( self, p ):
        '''lambdabindings : bindings_list'''
        p[0] = [ParentNode("lambdabindings",[x]) for x in p[1]]


    def p_lambdabinding_list_1(self, p):
        '''lambdabinding_list : $lambdabinding'''
        p[0] = [ p[1] ]
    expandGrammar(p_lambdabinding_list_1, lambdabinding=['idop', 'bindings'])

    def p_lambdabinding_list_2(self, p):
        '''lambdabinding_list : $lambdabinding COMMA lambdabinding_list'''
        p[0] = [ p[1] ]
        p[0] += p[len(p)-1]
    expandGrammar(p_lambdabinding_list_2, lambdabinding=['idop', 'bindings'])


    def p_set_expression(self, p):
        '''setexpr : LBRACE setbindings VBAR expression RBRACE'''

        p[0] = ParentNode('setexpr', [])
        p[0].children.append(p[2])
        p[0].children.append(p[4])


    def p_typedids(self, p):
        '''typedids : typedids_pre
                    | typedids_pre VBAR expression'''

        p[0] = ParentNode('typedids', [ p[1] ])
        if len(p) > 2:
            p[0].children.append(p[3])


    def p_typedids_pre(self, p):
        '''typedids_pre : idop_list
                        | idop_list COLON $typeexpr'''

        assert type(p[1]) == list
        p[0] = ParentNode('typedids_pre', p[1])
        if len(p) > 2:
            p[0].children.append(p[3])

    expandGrammar(p_typedids_pre, typeexpr=typeexpr)


    def p_binding(self, p):
        '''binding : typedids
                   | LPAREN typedids RPAREN'''

        p[0] = ParentNode('binding', [])
        if len(p) == 2:
            p[0].children = [ p[1] ]
        else:
            p[0].children = [ ParentNode('pgroup', [ p[2] ]) ]


    def p_binding_list(self, p):
        '''binding_list : binding
                        | binding_list COMMA binding'''

        if isinstance(p[1], Node):
            p[0] = [ p[1] ]
        else:
            p[0] = p[1] + [ p[3] ]


    def p_bindings(self, p):
        '''bindings : LPAREN binding_list RPAREN'''

        p[0] = ParentNode('bindings', p[2])


    def p_setbindings(self, p):
        '''setbindings : setbinding
                       | setbinding setbindings
                       | setbinding COMMA setbindings'''

        p[0] = ParentNode('setbindings', [ p[1] ])
        if len(p) > 2:
            p[0].children +=  p[len(p) - 1].children


    def p_setbinding(self, p):
        '''setbinding : idop COLON $typeexpr
                      | idop
                      | bindings'''

        p[0] = ParentNode('setbinding', [ p[1] ])
        if len(p) > 3:
            p[0].children.append(p[3])

    expandGrammar(p_setbinding, typeexpr=typeexpr)


    def p_bindings_list(self, p):
        '''bindings_list : bindings
                         | bindings_list bindings'''

        if isinstance(p[1], Node):
            p[0] = [ p[1] ]
        else:
            p[1].append(p[2])
            p[0] = p[1]


    def p_letbind(self, p):
        '''letbind : idop
                   | idop COLON $typeexpr
                   | idop bindings_list
                   | idop bindings_list COLON $typeexpr'''

        p[0] = ParentNode('letbind', [ p[1] ])
        if len(p) > 2:
            if isinstance(p[2], list):
                p[0].children += p[2]
            if p.slice[len(p) - 2].type == 'COLON':
                p[0].children.append(p[len(p)-1])
    expandGrammar(p_letbind, typeexpr=typeexpr)


    def p_letbind_list(self, p):
        '''letbind_list : letbind
                        | letbind_list COMMA letbind'''

        if isinstance(p[1], Node):
            p[0] = [ p[1] ]
        else:
            p[1].append(p[3])
            p[0] = p[1]


    def p_letbinding(self, p):
        '''letbinding : letbinding_prelude EQUAL expression'''

        p[0] = p[1]
        p[0].type = 'letbinding'
        p[0].children.append(p[3])


    def p_letbinding_prelude(self, p):
        '''letbinding_prelude : letbind
                              | LPAREN letbind_list RPAREN'''

        p[0] = ParentNode('letbinding_prelude', [])
        if len(p) == 2:
            p[0].children = [ p[1] ]
        else:
            p[0].children = p[2]


    def p_letbinding_list(self, p):
        '''letbinding_list : letbinding
                           | letbinding_list COMMA letbinding'''

        if isinstance(p[1], Node):
            p[0] = [ p[1] ]
        else:
            p[1].append(p[3])
            p[0] = p[1]


    def p_expression_let(self, p):
        '''expr_let : let_prelude KEY_IN expression'''

        p[0] = p[1]
        p[0].type = 'let'
        p[0].children.append(p[3])


    def p_let_prelude(self, p):
        '''let_prelude : KEY_LET letbinding_list'''

        p[0] = ParentNode('letbinding_list', p[2])


    def p_expression_cases(self, p):
        '''expr_cases : KEY_CASES expression KEY_OF selection_list KEY_ENDCASES
                      | KEY_CASES expression KEY_OF selection_list KEY_ELSE expression KEY_ENDCASES'''

        p[0] = ParentNode('cases', [ p[2] ] + p[4])
        if len(p) == 8:
            p[0].children.append(p[6])


    def p_selection(self, p):
        '''selection : idop COLON expression
                     | idop LPAREN idop_list RPAREN COLON expression'''

        p[0] = ParentNode('selection', [ p[1] ])
        if len(p) == 7:
            p[0].children += p[3]

        p[0].children.append(p[len(p)-1])


    def p_selection_list(self, p):
        '''selection_list : selection
                          | selection_list COMMA selection'''

        if isinstance(p[1], Node):
            p[0] = [ p[1] ]
        else:
            p[0] = p[1] + [ p[3] ]


    def p_expression_cond(self, p):
        '''expr_cond  : KEY_COND expression_arrow_list KEY_ENDCOND
                      | KEY_COND expression_arrow_list COMMA KEY_ELSE MINUS_GT expression KEY_ENDCOND'''

        p[0] = ParentNode('cond', p[2])
        if len(p) == 8:
            p[0].children.append(p[6])


    def p_expression_arrow_list(self, p):
        '''expression_arrow_list : expression MINUS_GT expression
                                 | expression_arrow_list COMMA expression MINUS_GT expression'''

        if isinstance(p[1], Node):
            p[0] = [ ParentNode('MINUS_GT', [ p[1], p[3] ]) ]
        else:
            p[1].append(ParentNode('MINUS_GT', [ p[3], p[5] ]))
            p[0] = p[1]


    def p_tableexpr(self, p):
        '''tableexpr : KEY_TABLE tableentry_list KEY_ENDTABLE
                     | KEY_TABLE expression tableentry_list KEY_ENDTABLE
                     | KEY_TABLE expression COMMA expression tableentry_list KEY_ENDTABLE
                     | KEY_TABLE colheading tableentry_list KEY_ENDTABLE
                     | KEY_TABLE expression colheading tableentry_list KEY_ENDTABLE
                     | KEY_TABLE expression COMMA expression colheading tableentry_list KEY_ENDTABLE'''

        p[0] = ParentNode('tableexpr', [])
        if len(p) == 5:
            p[0].children += [ p[2] ]
        elif len(p) == 6:
            p[0].children += [ p[2], p[3] ]
        elif len(p) == 7:
            p[0].children += [ p[2], p[4] ]
        elif len(p) == 8:
            p[0].children += [ p[2], p[4], p[5] ]
        p[0].children.append(ParentNode('tableentries', p[len(p)-2]))


    def p_tableentry(self, p):
        '''tableentry : expression_or_else_list VBAR_VBAR'''

        p[0] = ParentNode('tableentry', p[1])


    def p_colheading(self, p):
        '''colheading : VBAR_LBRACKET expression RBRACKET_VBAR
                      | VBAR_LBRACKET expression expression_or_else_list RBRACKET_VBAR'''

        p[0] = ParentNode('colheading', [ p[2] ])
        if len(p) == 5:
            p[0].children += p[3]


    def p_tableentry_list(self, p):
        '''tableentry_list : tableentry
                           | tableentry_list tableentry'''

        if isinstance(p[1], Node):
            p[0] = [ p[1] ]
        else:
            p[1].append(p[2])
            p[0] = p[1]


    def p_expression_or_else_list(self, p):
        '''expression_or_else_list : VBAR
                                   | VBAR expression
                                   | VBAR KEY_ELSE
                                   | expression_or_else_list VBAR
                                   | expression_or_else_list VBAR expression
                                   | expression_or_else_list VBAR KEY_ELSE'''

        if len(p) == 3:
            if type(p[1]) == list:
                p[0] = p[1]
            else:
                if isinstance(p[2], Node):
                    p[0] = [ p[2] ]
                else:
                    p[0] = [ LeafNode.make(p, 2) ]
        elif len(p) == 4:
            p[0] = p[1]
            if isinstance(p[3], Node):
                p[0].append(p[3])
            else:
                p[0].append(LeafNode.make(p, 3))
        else:
            p[0] = []


    def p_decimal( self, p ):
        '''decimal : NUMBER DOT NUMBER'''
        p[0] = LeafNode("NUMBER", p[1]+"."+p[3],p.lexpos(1), p.lineno(1))

        # decimal is undocumented in the PVS language reference, but PVS
        # 4.2 seems to support it


    ############################################################################
    # Type expressions
    ############################################################################

    def p_enumerationtype(self, p):
        '''enumerationtype : LBRACE idop_list RBRACE'''

        assert type(p[2]) == list
        p[0] = ParentNode('enumerationtype', p[2])


    def p_subtype(self, p):
        '''subtype : setexpr
                   | expr_list_1'''

        p[0] = ParentNode('subtype', [ p[1] ])


    def p_typeapplication(self, p):
        '''typeapplication : name arguments'''

        p[0] = ParentNode('typeapplication', [ p[1], p[2] ])


    def p_functiontype(self, p):
        '''functiontype :              LBRACKET idoptypeexpr_list MINUS_GT $typeexpr RBRACKET
                        | KEY_FUNCTION LBRACKET idoptypeexpr_list MINUS_GT $typeexpr RBRACKET
                        | KEY_ARRAY    LBRACKET idoptypeexpr_list MINUS_GT $typeexpr RBRACKET'''

        p[0] = ParentNode('functiontype', [])
        if len(p) == 6:
            p[0].children = p[2]
            p[0].children.append(p[4])
        else:
            p[0].children = [ LeafNode.make(p, 1) ]
            p[0].children += p[3]
            p[0].children.append(p[5])

    expandGrammar(p_functiontype, typeexpr=typeexpr)


    def p_idoptypeexpr_list(self, p):
        '''idoptypeexpr_list : idop COLON $typeexpr
                             | idop COLON $typeexpr COMMA idoptypeexpr_list
                             | $typeexpr
                             | $typeexpr COMMA idoptypeexpr_list'''

        p[0] = [ ParentNode('idoptypeexpr', [ p[1] ]) ]
        if len(p) > 2:
            if p.slice[2].type == 'COLON':
                p[0][0].children.append(p[3])
            if p.slice[len(p)-2].type == 'COMMA':
                p[0] += p[len(p)-1]

    expandGrammar(p_idoptypeexpr_list, typeexpr=typeexpr)


    def p_tupletype(self, p):
        '''tupletype : LBRACKET idoptypeexpr_list RBRACKET'''

        p[0] = ParentNode('tupletype', p[2])


    def p_fielddecls(self, p):
        '''fielddecls : ID_list COLON $typeexpr'''

        p[0] = ParentNode('fielddecls', p[1])
        p[0].children.append(p[3])

    expandGrammar(p_fielddecls, typeexpr=typeexpr)


    def p_ID_list(self, p):
        '''ID_list : ID
                   | ID COMMA ID_list'''

        node = LeafNode.make(p, 1)
        if len(p) > 2:
            p[3].insert(0, node)
            p[0] = p[3]
        else:
            p[0] = [ node ]


    def p_fielddecls_list(self, p):
        '''fielddecls_list : fielddecls
                           | fielddecls COMMA fielddecls_list'''

        if len(p) > 2:
            p[3].insert(0, p[1])
            p[0] = p[3]
        else:
            p[0] = [ p[1] ]


    def p_recordtype(self, p):
        '''recordtype : LBRACKET_HASH fielddecls_list HASH_RBRACKET'''

        p[0] = ParentNode('recordtype', p[2])


    ############################################################################
    # Names
    ############################################################################

    def p_name(self, p):
        '''name : idop
                | ID AT idop
                | idop actuals
                | idop DOT idop
                | ID AT idop actuals
                | ID AT idop DOT idop
                | idop actuals DOT idop
                | ID AT idop actuals DOT idop'''

        p[0] = ParentNode('name', [])

        if isinstance(p[1], Node):
            p[0].children = [ p[1] ]
        else:
            p[0].children = [ LeafNode.make(p, 1) ]

        p_len = len(p)

        if p_len == 3:
            # idop actuals
            p[0].children.append(p[2])
        elif p_len == 4:
            # ID AT idop
            # idop DOT idop
            p[0].children.append(p[3])
        elif p_len == 5:
            if isinstance(p[2], Node):
                # idop actuals DOT idop
                p[0].children += [ p[2], p[4] ]
            else:
                # ID AT idop actuals
                p[0].children += [ p[3], p[4] ]
        elif p_len == 6:
            # ID AT idop DOT idop
            p[0].children += [ p[3], p[5] ]
        elif p_len == 7:
            # ID AT idop actuals DOT idop
            p[0].children += [ p[3], p[4], p[6] ]


    def p_idop(self, p):
        '''idop : ID
                | $opsym'''

        p[0] = ParentNode('idop', [ LeafNode.make(p, 1) ])

    # Generate grammar using the list of operations
    opsyms = binops + unaryops + [ 'KEY_IF', 'KEY_TRUE', 'KEY_FALSE',
                                   'LBRACKET_VBAR_VBAR_RBRACKET',
                                   'LPAREN_VBAR_VBAR_RPAREN',
                                   'LBRACE_VBAR_VBAR_RBRACE' ]
    opsyms.remove('MINUS') # duplicate
    opsyms.remove('TILDE') # duplicate
    expandGrammar(p_idop, opsym=opsyms)


    def p_idop_list(self, p):
        '''idop_list : idop
                     | idop COMMA idop_list'''

        if len(p) > 2:
            p[3].insert(0, p[1])
            p[0] = p[3]
        else:
            p[0] = [ p[1] ]


    def p_actuals(self, p):
        '''actuals : LBRACKET actual_list RBRACKET'''

        p[0] = ParentNode('actuals', p[2])

    expandGrammar(p_actuals, actual=actual)


    def p_actual_list(self, p):
        '''actual_list : $actual
                       | $actual COMMA actual_list'''

        if len(p) == 4:
            p[3].insert(0, p[1])
            p[0] = p[3]
        else:
            p[0] = [ p[1] ]

    expandGrammar(p_actual_list, actual=actual)


