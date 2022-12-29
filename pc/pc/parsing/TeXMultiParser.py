from pc.parsing.Token import Token
from pc.parsing.TeXMultiLexer import *
from pc.parsing.TeXBaseParser import *

from pc.parsing.ParserUtil import *
from pc.parsing.AST import ParentNode as P, LeafNode as L
from pc.parsing.ParseError import *
from pc.parsing.AbstractParser import AbstractParser
from pc.parsing.PVSUtils import *

from pc.util.Utils import *

import types

def GetTexMultiExprParser(name, decimal_sep = None, tuple_sep = None):
    TeXLexerClass = GetTeXMultiLexer(decimal_sep = decimal_sep, tuple_sep = tuple_sep)
    TexBaseParserClass =  GetTexExprBaseParser("multi_" + name, TeXLexerClass)
    class TeXExprParser( TexBaseParserClass ):
        """Parser for TeX expressions."""

    return TeXExprParser

def GetTexMultiDeclParser(name, TeXExprParser):
    TeXDeclBaseParser = GetTexDeclBaseParser("multi_" + name, TeXExprParser)
    class TeXDeclParser( TeXDeclBaseParser ):
        """Parser for TeX declarations."""

        
    return TeXDeclParser
