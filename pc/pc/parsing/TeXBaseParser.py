from pc.parsing.Token import Token
from pc.parsing.TeXBaseLexer import *

from pc.parsing.ParserUtil import *
from pc.parsing.AST import ParentNode as P, LeafNode as L
from pc.parsing.ParseError import *
from pc.parsing.AbstractParser import AbstractParser
from pc.parsing.PVSUtils import *

from pc.util.Utils import *

import types
import sys
import time

def debug( x ):
    sys.stderr.write("["+time.strftime("%a, %d %b %Y %H:%M:%S", time.localtime())+"] ")
    sys.stderr.write(unicode(x).encode("latin-1","replace")+"\n")
    #sys.stderr.write(unicode(x.decode("utf-8")).encode("utf-8","replace")+"\n")
    sys.stderr.flush()

#
# TODO: Detect double superscript and throw an error
#

# TODO: The pretty-printer relies on the way ASTs are constructed by
# the parser wrt. to operator precedence. E.g., the AST:
# 
# T: CARET
#     T: idop
#         T: ID, V: x
#     T: SLASH
#         T: idop
#             T: ID, V: y
#         T: NUMBER, V: 2
#
# is (incorrectly) pretty-printed as:
#
# x ^ y / 2
#
# The pretty-printer should manage operator precedences correctly.
# The below solution (wrapping operands with expression_list_1) adds
# unecessary parentheses.


def explist( arg ):
    if arg.type in ("ID","NUMBER","idop"):
        return arg
    else:
        return PGROUP(arg)
        #return P("expression_list_1",[arg])

def GetTexExprBaseParser(name, TeXLexerClass):
    class TeXExprParser( AbstractParser ):
        """Parser for TeX expressions."""

        unaryops = [
            "MINUS",
            "BSLASH_sqrt",
            "BSLASH_neg",
        ]

        bindingops =[
            "BSLASH_exists",
            "BSLASH_forall",
            "BSLASH_lambda"
        ]

        binops = [
            "CARET",
            "EQUAL",
            "MINUS",
            "PLUS",
            "SLASH",

            "GT",
            "LT",
            "BSLASH_lt",
            "BSLASH_gt",
            "BSLASH_le",
            "BSLASH_ge",

            "BSLASH_cdot",
            "BSLASH_equiv",
            "BSLASH_implies",
            "BSLASH_iff",
            "BSLASH_Rightarrow",
            "BSLASH_rightarrow",
            "BSLASH_times",
            "BSLASH_to",
            "BSLASH_Leftrightarrow",
            "BSLASH_leftrightarrow",
            "BSLASH_Leftarrow",
            "BSLASH_leftarrow",
            "BSLASH_vee",
            "BSLASH_lor",
            "BSLASH_wedge",
            "BSLASH_land",
            "BSLASH_models",
            "BSLASH_vdash",
            "BSLASH_circ",
            "BSLASH_in",
        ]

        texids = [
            "BSLASH_alpha",
            "BSLASH_beta",
            "BSLASH_gamma",
            "BSLASH_delta",
            "BSLASH_epsilon",
            "BSLASH_zeta",
            "BSLASH_eta",
            "BSLASH_theta",
            "BSLASH_iota",
            "BSLASH_kappa",
            "BSLASH_mu",
            "BSLASH_nu",
            "BSLASH_xi",
            "BSLASH_pi",
            "BSLASH_rho",
            "BSLASH_sigma",
            "BSLASH_tau",
            "BSLASH_upsilon",
            "BSLASH_phi",
            "BSLASH_chi",
            "BSLASH_psi",
            "BSLASH_omega",
            "BSLASH_Gamma",
            "BSLASH_Delta",
            "BSLASH_Theta",
            "BSLASH_Lambda",
            "BSLASH_Xi",
            "BSLASH_Pi",
            "BSLASH_Sigma",
            "BSLASH_Upsilon",
            "BSLASH_Phi",
            "BSLASH_Psi",
            "BSLASH_Omega",
            "BSLASH_NN",
            "BSLASH_QQ",
            "BSLASH_RR",
            "BSLASH_ZZ",
            "BSLASH_approx",
        ]

        # symbol translation map
        trs = {
            r"BSLASH_Rightarrow" : "EQUAL_GT",
            r"BSLASH_Leftarrow" : "KEY_WHEN",
            r"BSLASH_Leftrightarrow" : "LT_EQUAL_GT",
            r"BSLASH_cdot": "STAR",
            r"BSLASH_circ": "KEY_O",
            r"BSLASH_equiv": "KEY_IFF",
            r"BSLASH_exists": "KEY_EXISTS",
            r"BSLASH_forall": "KEY_FORALL",
            r"BSLASH_frac": "SLASH",
            r"BSLASH_ge": "GT_EQUAL",
            r"BSLASH_gt": "GT",
            r"BSLASH_iff" : "LT_EQUAL_GT",
            r"BSLASH_implies" : "KEY_IMPLIES",
            r"BSLASH_lambda" : "KEY_LAMBDA",
            r"BSLASH_land": "KEY_AND",
            r"BSLASH_le": "LT_EQUAL",
            r"BSLASH_lor": "KEY_OR",
            r"BSLASH_lt": "LT",
            r"BSLASH_models": "VBAR_EQUAL",
            r"BSLASH_ne": "SLASH_EQUAL",
            r"BSLASH_neg": "KEY_NOT",
            r"BSLASH_vdash": "VBAR_MINUS",
            r"BSLASH_vee": "KEY_OR",
            r"BSLASH_wedge": "KEY_AND",
            
            r"BSLASH_approx": "approx",
            r"BSLASH_alpha" : "alpha",
            r"BSLASH_beta" : "beta",
            r"BSLASH_gamma" : "gamma",
            r"BSLASH_delta" : "delta",
            r"BSLASH_epsilon" : "epsilon",
            r"BSLASH_zeta" : "zeta",
            r"BSLASH_eta" : "eta",
            r"BSLASH_theta" : "theta",
            r"BSLASH_iota" : "iota",
            r"BSLASH_kappa" : "kappa",
            r"BSLASH_mu" : "mu",
            r"BSLASH_nu" : "nu",
            r"BSLASH_xi" : "xi",
            r"BSLASH_pi" : "pi",
            r"BSLASH_rho" : "rho",
            r"BSLASH_sigma" : "sigma",
            r"BSLASH_tau" : "tau",
            r"BSLASH_upsilon" : "upsilon",
            r"BSLASH_phi" : "phi",
            r"BSLASH_chi" : "chi",
            r"BSLASH_psi" : "psi",
            r"BSLASH_omega" : "omega",
            r"BSLASH_Gamma" : "Gamma",
            r"BSLASH_Delta" : "Delta",
            r"BSLASH_Theta" : "Theta",
            r"BSLASH_Lambda" : "Lambda",
            r"BSLASH_Xi" : "Xi",
            r"BSLASH_Pi" : "Pi",
            r"BSLASH_Sigma" : "Sigma",
            r"BSLASH_Upsilon" : "Upsilon",
            r"BSLASH_Phi" : "Phi",
            r"BSLASH_Psi" : "Psi",
            r"BSLASH_Omega" : "Omega",

            #r"BSLASH_unit" : (lambda x: "unit_"+x),

            r"BSLASH_RR": "real",
            r"BSLASH_QQ": "rational",
            r"BSLASH_ZZ": "int",
            r"BSLASH_NN": "nat"

        }

        def __init__( self,
                      startline = 1,
                      startcol = 0,
                      start = "expr",
                      tabmodule = "texexpr_" + name + '_parsetab',
                      debug = False,
                      debugfile = "texexpr_" + name + '_parser.out',
                      write_tables = False ):
            AbstractParser.__init__(self,
                                    lexer = TeXLexerClass(startline = startline, startcol = startcol),
                                    start = start,
                                    tabmodule = tabmodule,
                                    debug = debug,
                                    debugfile = debugfile,
                                    write_tables = write_tables)
            self.tokens = self.lexer.tokens
            self.module = self
            self._yacc()


        def __tex2pvs( self, t, p=None ):
            assert isinstance(t,Token)
            if t.type in self.trs:
                t2 = self.trs[t.type]
                if type(t2)==types.StringType: return t2
                elif type(t2)==types.FunctionType: return t2(p)
            return t.type

        def get_production_string( self, name ):
            prods = [p for p in self.parser.productions if p.name==name]
            prod_tokens = [p.str.split("->")[1].split(" ") for p in prods]
            s = "%20s ::= "%name
            first = True
            for tokens in prod_tokens:
                if not first:
                    s += "\n%20s  |  "%""
                for t in tokens:
                    if t.startswith("BSLASH_"): t = "'%s'" % t.replace("BSLASH_","\\")
                    elif t in self.lexer.symbol_map: t = "'%s'" % self.lexer.symbol_map[t]
                    elif t in self.lexer.terminal_re_map: t = "%s" % self.lexer.terminal_re_map[t]
                    s += t + " "
                first = False
            return s

        def get_grammar_string( self ):
            productions = remove_duplicates([x.name for x in self.parser.productions])
            return "\n\n".join(map(self.get_production_string,productions))



        ############### EXPRESSIONS ###############################################


        def p_expr( self, p ):
            """expr : expr_1"""
            p[0] = p[1]

        def p_expr_1_1( self, p ):
            """expr_1 : expr_2 BSLASH_vdash expr_1
                | expr_2 BSLASH_models expr_1"""
            p[0] = P(self.__tex2pvs(p.slice[2]),map(explist,[p[1],p[3]]))

        def p_expr_1_n( self, p ):
            """expr_1 : expr_2"""
            p[0] = p[1]


        def p_expr_2_1( self, p ):
            """expr_2 : expr_3 BSLASH_Leftrightarrow expr_2
                | expr_3 BSLASH_iff expr_2
                | expr_3 BSLASH_equiv expr_2"""
            p[0] = P(self.__tex2pvs(p.slice[2]),map(explist,[p[1],p[3]]))

        def p_expr_2_n( self, p ):
            """expr_2 : expr_3"""
            p[0] = p[1]


        def p_expr_3_1( self, p ) :
            """expr_3 : expr_4 BSLASH_implies expr_3
                | expr_4 BSLASH_Rightarrow expr_3
                | expr_4 BSLASH_Leftarrow expr_3"""
            p[0] = P(self.__tex2pvs(p.slice[2]),map(explist,[p[1],p[3]]))

        def p_expr_3_n( self, p ):
            """expr_3 : expr_4"""
            p[0] = p[1]


        def p_expr_4_1( self, p ):
            """expr_4 : expr_5 BSLASH_vee expr_4
                | expr_5 BSLASH_lor expr_4"""
            p[0] = P(self.__tex2pvs(p.slice[2]),map(explist,[p[1],p[3]]))

        def p_expr_4_n( self, p ):
            """expr_4 : expr_5"""
            p[0] = p[1]


        def p_expr_5_1( self, p ):
            """expr_5 : expr_6 BSLASH_wedge expr_5
                | expr_6 BSLASH_land expr_5"""
            p[0] = P(self.__tex2pvs(p.slice[2]),map(explist,[p[1],p[3]]))

        def p_expr_5_n( self, p ):
            """expr_5 : expr_6"""
            p[0] = p[1]


        def p_expr_6_1( self, p ):
            """expr_6 : BSLASH_neg expr_6"""
            p[0] = P(self.__tex2pvs(p.slice[1]),map(explist,[p[2]]))

        def p_expr_6_n( self, p ):
            """expr_6 : expr_7"""
            p[0] = p[1]


        def p_expr_7_1( self, p ):
            """expr_7 : expr_7 EQUAL expr_8
                | expr_7 LT expr_8
                | expr_7 GT expr_8
                | expr_7 BSLASH_lt expr_8
                | expr_7 BSLASH_gt expr_8
                | expr_7 BSLASH_ne expr_8
                | expr_7 BSLASH_le expr_8
                | expr_7 BSLASH_ge expr_8
                """
            p[0] = P(self.__tex2pvs(p.slice[2]),map(explist,[p[1],p[3]]))
            
        def p_expr_7_2( self, p ):
            """expr_7 : expr_7 BSLASH_approx expr_8"""
            p[0] = APPLY(NAME_IDOP(ID("approx",p.lexpos(1),p.lineno(1))),[p[1], p[3]])

        def p_expr_7_n( self, p ):
            """expr_7 : expr_8"""
            p[0] = p[1]


        def p_expr_8_1( self, p ):
            """expr_8 : expr_8 PLUS expr_9
                | expr_8 MINUS expr_9"""
            p[0] = P(self.__tex2pvs(p.slice[2]),map(explist,[p[1],p[3]]))

        def p_expr_8_n( self, p ):
            """expr_8 : expr_9"""
            p[0] = p[1]

        def p_expr_9_1( self, p ):
            """expr_9 : expr_9 BSLASH_cdot expr_10
                | expr_9 SLASH expr_10"""
            p[0] = P(self.__tex2pvs(p.slice[2]),map(explist,[p[1],p[3]]))

        def p_expr_9_2( self, p ):
            """expr_9 : expr_9 SPACE expr_10"""
            p[0] = P("STAR", map(explist,[p[1],p[3]]))

        def p_expr_9_n( self, p ):
            """expr_9 : expr_10"""
            p[0] =  p[1]


        def p_expr_10_1( self, p ):
            """expr_10 : MINUS expr_10"""
            p[0] = P(self.__tex2pvs(p.slice[1]),map(explist,[p[2]]))

        def p_expr_10_n( self, p ):
            """expr_10 : expr_11"""
            p[0] = p[1]

        def p_expr_11_1( self, p ):
            """expr_11 : expr_11 BSLASH_circ expr_14"""
            p[0] = P(self.__tex2pvs(p.slice[2]),map(explist,[p[1],p[3]]))

        def p_expr_11_n( self, p ):
            """expr_11 : expr_12"""
            p[0] = p[1]


        def p_expr_12_1( self, p ):
            """expr_12 : expr_12 CARET expr_latex"""
            p[0] = APPLY(NAME_IDOP(ID("power",p.lexpos(2),p.lineno(2))),[p[1],p[3]])

        def p_expr_12_n( self, p ):
            """expr_12 : expr_13"""
            p[0] = p[1]


        def p_expr_13_1( self, p ):
            """expr_13 : BSLASH_frac expr_latex expr_latex"""
            p[0] = P(self.__tex2pvs(p.slice[1]),map(explist,[p[2],p[3]]))

        def p_expr_13_2( self, p ):
            """expr_13 : BSLASH_sqrt expr_latex"""
            p[0] = APPLY(NAME_IDOP(ID("square_root",p.lexpos(1),p.lineno(1))),[p[2]])

        def p_expr_13_3( self, p ):
            """expr_13 : BSLASH_sqrt LBRACKET expr RBRACKET expr_latex"""
            p[0] = APPLY(NAME_IDOP(ID("nth_root",p.lexpos(1),p.lineno(1))),[p[5],p[3]])

        def p_expr_13_n( self, p ):
            """expr_13 : expr_14"""
            p[0] = p[1]


        def p_expr_14_1( self, p ):
            """expr_14 : LVBAR expr RVBAR"""
            p[0] = APPLY(NAME_IDOP(ID("abs",p.lexpos(1),p.lineno(1))),[pget(p,"expr")])

        def p_expr_14_n( self, p ):
            """expr_14 : expr_n"""
            p[0] = p[1]


        def p_expr_latex_1( self, p ):
            """expr_latex : number
                | bool
                | idop"""
            p[0] = p[1]

        def p_expr_latex_2( self, p ):
            """expr_latex : LBRACE expr RBRACE"""
            p[0] = p[2]


        def p_expr_n_1( self, p ):
            """expr_n : number
                | bool
                | idop"""
            p[0] = p[1]

        def p_expr_n_2( self, p ):
            """expr_n : LBRACE expr RBRACE"""
            p[0] = p[2]

        def p_expr_n_3( self, p ):
            """expr_n : expr_n arguments"""
            p[0] = P('expression_arguments',[explist(p[1]),p[2]])

        def p_expr_n_4( self, p ):
            """expr_n : expr_12 arguments"""
            p[0] = P('expression_arguments',[explist(p[1]),p[2]])

        def p_expr_n_5( self, p ):
            """expr_n : BSLASH_exists binding_list BSLASH_bullet expr
                | BSLASH_forall binding_list BSLASH_bullet expr
                | BSLASH_lambda binding_list BSLASH_bullet expr"""
            p[0] = P("bindingexpr",
                              [L(self.__tex2pvs(p.slice[1]),p[1],p.lexpos(1),p.lineno(1)),
                               P("lambdabindings",[P("bindings",pget(p,"binding_list"))]),
                               pget(p,"expr")])

        def p_expr_n_6( self, p ):
            """expr_n : LPAREN expr RPAREN"""
            p[0] = pget(p,"expr")

        def p_mixfrac_a(self, p):
            """mixfrac : MIXFRAC"""
            p[0] = P("PLUS", [L("NUMBER",p.slice[1].integer,p.lexpos(1), p.lineno(1)),
                              P("SLASH",[L("NUMBER", p.slice[1].nomin, p.lexpos(1), p.lineno(1)),
                                         L("NUMBER", p.slice[1].denom, p.lexpos(1), p.lineno(1))])])


        def p_number_1( self, p ):
            """number : mixfrac"""
            p[0] = p[1]

        def p_number_2( self, p ):
            """number : NUMBER"""
            p[0] = L("NUMBER",p[1],p.lexpos(1), p.lineno(1))

        def p_bool_1( self, p ):
            '''bool : FALSE'''
            p[0] = L("KEY_FALSE",p[1],p.lexpos(1),p.lineno(1))

        def p_bool_2( self, p ):
            '''bool : TRUE'''
            p[0] = L("KEY_TRUE",p[1],p.lexpos(1),p.lineno(1))

        def p_arguments( self, p ):
            """arguments : LPAREN expr_list RPAREN"""
            p[0] = P("arguments",pget(p,"expr_list"))

        def p_binding_1( self, p ):
            """binding : id
                | id COLON typeexpr
                | id BSLASH_in typeexpr"""
            p[0] = P("binding",
                     [P("typedids",
                        [P("typedids_pre",[P("idop",[p[1]])]+([pget(p,"typeexpr")] if phas(p,"typeexpr") else []))])])

        def p_binding_2( self, p ):
            """binding : LPAREN id_list COLON typeexpr RPAREN
                | LPAREN id_list BSLASH_in typeexpr RPAREN"""
            p[0] = P("binding",
                     [P("typedids",
                        [P("typedids_pre",map(lambda x:P("idop",[x]),pget(p,"id_list"))+[pget(p,"typeexpr")])])])

        p_binding_list = make_list("binding_list","binding","")


        def p_id_1( self, p ):
            """id : ID"""
            p[0] = L.make(p, 1)

        def p_id_2( self, p ):
            """id : $texid"""
            p[0] = L("ID",self.__tex2pvs(p.slice[1]),p.lexpos(1),p.lineno(1))

        expandGrammar(p_id_2,texid=texids)

        #def p_id_3( self, p ):
        #    """id : BSLASH_unit LBRACE ID RBRACE"""
        #    p[0] = L("ID",self.__tex2pvs(p.slice[1],p.slice[3].value),p.lexpos(1),p.lineno(1))

        def p_id_3( self, p ):
            """id : UNIT"""
            p[0] = L("ID", "unit_"+ p.slice[1].unitname, p.lexpos(1), p.lineno(1))

        #def p_mixfrac_a(self, p):
        #    """mixfrac : MIXFRAC"""
        #    p[0] = P("PLUS", [L("NUMBER",p.slice[1].integer,p.lexpos(1), p.lineno(1)),
        #                      P("SLASH",[L("NUMBER", p.slice[1].nomin, p.lexpos(1), p.lineno(1)),
        #                                 L("NUMBER", p.slice[1].denom, p.lexpos(1), p.lineno(1))])])


        def p_idop_1( self, p ):
            """idop : id"""
            p[0] = P("idop",[p[1]])

        def p_idop_2(self, p):
            """idop : $opsym"""
            p[0] = P("idop", [ L(self.__tex2pvs(p.slice[1]),p.slice[1].value,p.lexpos(1),p.lineno(1))])

        expandGrammar(p_idop_2,opsym=remove_duplicates(binops + unaryops))


        p_id_list = make_list("id_list","id","COMMA")

        p_expr_list = make_list("expr_list","expr","COMMA")


        ############### TYPE EXPRESSIONS ##########################################


        # The PVS type constructors are 'name', 'subtype', 'enumerationtype', 'typeapplication',
        # 'functiontype', 'tupletype', and 'recordtype'.
        def p_typeexpr( self, p ):
            """typeexpr : typeexpr_fun"""
            p[0] = p[1]

        def p_typeexpr_fun_1( self, p ):
            """typeexpr_fun : typeexpr_prod"""
            p[0] = p[1]

        def p_typeexpr_fun_2( self, p ):
            """typeexpr_fun : typeexpr_prod BSLASH_rightarrow typeexpr_fun
                  | typeexpr_prod BSLASH_to typeexpr_fun"""
            p[0] = P("functiontype", [pget(p,"typeexpr_prod"), p[3]])

        def p_typeexpr_prod_1( self, p ):
            """typeexpr_prod : typeexpr_atom"""
            p[0] = p[1]

        def p_typeexpr_prod_2( self, p ):
            """typeexpr_prod : typeexpr_atom BSLASH_times typeexpr_prodlist"""
            p[0] = P("tupletype", [p[1]] + pget(p,"typeexpr_prodlist"))

        p_typeexpr_prodlist = make_list("typeexpr_prodlist","typeexpr_atom","BSLASH_times")

        def p_typeexpr_atom_1( self, p ):
            """typeexpr_atom : typeexpr_name"""
            p[0] = p[1]

        def p_typeexpr_atom_2( self, p ):
            """typeexpr_atom : LPAREN typeexpr RPAREN"""
            p[0] = p[2]

        def p_typeexpr_name( self, p ):
            """typeexpr_name : id"""
            p[0] = P("name",[P("idop",[p[1]])])


        ############### COMMON ERRORS #############################################


        def p_error(self, t):
            #print self.parser.statestack
            #print '***'
            #print self.parser.symstack
            
            if t:
                s = t.value.decode("latin-1").encode("utf-8")
                err = ParseError("syntax error at '%s'" % s, t.lineno, t.lexpos, type="parsing")
            else:
                err = ParseError("TeX expression empty or incomplete",
                                 self.lexer.startline, self.lexer.startcol, type="parsing")
            raise ParseException([err])
            
    return TeXExprParser


def GetTexDeclBaseParser(name, TeXExprParser):
    class TeXDeclParser( TeXExprParser ):

        """Parser for TeX declarations."""

        def __init__( self,
                      startline = 1,
                      startcol = 0,
                      start = "decl_expr",
                      tabmodule = "texdecl_" + name + "_parsetab",
                      debug = False,
                      debugfile = "texdecl_" + name + "_parser.out",
                      write_tables = False ):
            TeXExprParser.__init__(self,
                                   startline=startline,
                                   startcol=startcol,
                                   start=start,
                                   tabmodule=tabmodule,
                                   debug=debug,
                                   debugfile=debugfile,
                                   write_tables=write_tables)

        ############### DECLARATIONS ##############################################


        def p_decl_expr_1( self, p ):
            """decl_expr : decl"""
            p[0] = p[1]

        def p_decl_expr_2( self, p ):
            """decl_expr : expr"""
            p[0] = [P('assumption',[p[1]])]

        def p_decl_1( self, p ):
            """decl : DEF id_list COLON typeexpr
                | DEF id_list BSLASH_in typeexpr
                | DEF id_list COLON typeexpr EQUAL expr"""
            p[0] = [P('vardecl',pget(p,"id_list")+[pget(p,"typeexpr")])]
            if phas(p,"expr"):
                p[0].apped(P('assumption',
                              [AND([EQUALS(x,PGROUP(pget(p,"expr"))) for x in pget(p,"id_list")])]))


        def p_decl_2( self, p ):
            """decl : DEF id typed_bindings_list COLON typeexpr
                | DEF id typed_bindings_list COLON typeexpr EQUAL expr"""
            p[0] = []

            vardecl = P('vardecl',[pget(p,"id")])
            k = vardecl.children
            for bindings in pget(p,"typed_bindings_list"):
                t = P('functiontype',[])
                for binding in bindings:
                    typedid = binding["typedids","typedids_pre"][0]
                    typ = typedid.children[-1]
                    for id in typedid.children[:-1]:
                        t.children.append(P("idoptypeexpr",[P("idop",[id]),typ]))
                k.append(t)
                k = t.children
            k.append(pget(p,"typeexpr"))
            p[0].append(vardecl)

            if phas(p,"expr"):
                x = P("idop",[pget(p,"id")])
                for bindings in pget(p,"typed_bindings_list"):
                    arguments = P('arguments',[])
                    for binding in bindings:
                        typedid = binding["typedids","typedids_pre"][0]
                        for id in typedid.children[:-1]:
                            arguments.children.append(P("idop",[id]))
                    x = P("expression_arguments",[x,arguments])

                p[0].append(P('assumption',[FORALL(pget(p,"typed_bindings_list"),EQUALS(x,pget(p,"expr")))]))
                #print tree_to_str(p[0][-1])

        def p_typed_bindings( self, p ):
            """typed_bindings : LPAREN typed_binding_list RPAREN"""
            p[0] = P("bindings",pget(p,"typed_binding_list"))

        p_typed_bindings_list = make_list("typed_bindings_list","typed_bindings")

        def p_typed_binding( self, p ):
            """typed_binding : id COLON typeexpr
                | id BSLASH_in typeexpr"""
            p[0] = P("binding",
                     [P("typedids",
                        [P("typedids_pre",[P("idop",[p[1]]),pget(p,"typeexpr")])])])

        p_typed_binding_list = make_list("typed_binding_list","typed_binding","COMMA")

    return TeXDeclParser
