from pc.parsing.Token import Token
from pc.parsing.TeXSingleLexer import *
from pc.parsing.TeXBaseParser import *

from pc.parsing.ParserUtil import *
from pc.parsing.AST import ParentNode as P, LeafNode as L
from pc.parsing.ParseError import *
from pc.parsing.AbstractParser import AbstractParser
from pc.parsing.PVSUtils import *

from pc.util.Utils import *

import types

def GetTexSingleExprParser(name, decimal_sep = None, tuple_sep = None):
    TeXLexerClass = GetTeXSingleLexer(decimal_sep = decimal_sep, tuple_sep = tuple_sep)
    TexBaseParserClass =  GetTexExprBaseParser("single_" + name, TeXLexerClass)
    class TeXExprParser( TexBaseParserClass ):
        """Parser for TeX expressions."""


        binops = TexBaseParserClass.binops
        unaryops = TexBaseParserClass.unaryops
        ############### EXPRESSIONS ###############################################

        def p_expr_b( self, p ):
            """expr : op"""
            p[0] = p[1]

        def p_expr_10_n( self, p ):
            """expr_10 : expr_10a"""
            p[0] = p[1]

        def p_expr_10a_1( self, p ):
            """expr_10a : expr_11"""
            p[0] = p[1]

        def p_expr_10a_2( self, p ):
            """expr_10a : expr_10a expr_11"""
            p[0] = P("STAR", map(explist,[p[1],p[2]]))

        def p_expr_n_3( self, p ):
            """expr_n : expr_function arguments"""
            p[0] = P('expression_arguments',[explist(p[1]),p[2]])

        def p_expr_n_4( self, p ):
            """expr_n : expr_function SPACE arguments"""
            p[0] = P('expression_arguments',[explist(p[1]),p[3]])

        def p_expr_function( self, p ):
            """expr_function : functionid"""
            p[0] = p[1]

        def p_functionid( self, p ):
            """functionid : FUNCTION"""
            p[0] = L.make(p, 1)

        def p_idop_2(self, p):
            """op : $opsym"""
            symbol = self.__tex2pvs(p.slice[1])
            p[0] = P("idop", [ L(symbol, p.slice[1].value, p.lexpos(1), p.lineno(1))])

        expandGrammar(p_idop_2,opsym=remove_duplicates(binops + unaryops))

            
    return TeXExprParser


def GetTexSingleDeclParser(name, TeXExprParser):
    TeXDeclBaseParser = GetTexDeclBaseParser("single_" + name, TeXExprParser)
    class TeXDeclParser( TeXDeclBaseParser ):
        """Parser for TeX declarations."""

        def p_decl_2( self, p ):
            """decl : DEF functionid typed_bindings_list COLON typeexpr
                | DEF functionid typed_bindings_list COLON typeexpr EQUAL expr"""
            p[0] = []

            vardecl = P('vardecl',[pget(p,"functionid")])
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
                x = P("idop",[pget(p,"functionid")])
                for bindings in pget(p,"typed_bindings_list"):
                    arguments = P('arguments',[])
                    for binding in bindings:
                        typedid = binding["typedids","typedids_pre"][0]
                        for id in typedid.children[:-1]:
                            arguments.children.append(P("idop",[id]))
                    x = P("expression_arguments",[x,arguments])

                p[0].append(P('assumption',[FORALL(pget(p,"typed_bindings_list"),EQUALS(x,pget(p,"expr")))]))
                #print tree_to_str(p[0][-1])

    return TeXDeclParser
