from pc.parsing.Token import Token

from pc.parsing.ParserUtil import *
from pc.parsing.AST import Node, ParentNode, LeafNode
from pc.parsing.AbstractParser import AbstractParser
from pc.parsing.AbstractLexer import AbstractLexer
from pc.parsing.ParseError import ParseError, ParseException
from pc.parsing.Token import Token

import re
from array import *


class SEXPLexer( AbstractLexer ):
    """Lexer for the Lisp S-expressions."""

    tokens = [
        "LPAREN",
        "RPAREN",
        "DOT",
        "SYMBOL",
        "STRING",
        "NUMBER"
        ]


    separator = re.compile(r"(?P<LPAREN>\()|(?P<RPAREN>\))|(?P<DOT>\.)")
    number = re.compile(r"(?P<NUMBER>-?([0-9]+|[0-9]+\.[0-9]*|\.[0-9]+))")
    symbol = re.compile(r"(?P<SYMBOL>[A-Za-z+\-*/.?:_][A-Za-z+\-*/.?:_0-9]*)")


    def token( self ):
        self._skip_whitespace()
        t = (self.__read_string() or
             self.__read_token(self.separator) or
             self.__read_token(self.number) or
             self.__read_token(self.symbol))
        if t: return t
        elif not self.ch: return None
        else: self.__error("Unexpected SEXP character '%s'" % (self.ch))


    def __read_token( self, regex ):
        t = Token(lineno=self.lineno,lexpos=self.linepos)
        match = regex.match(self.str,self.pos)
        if match:
            t.type = match.lastgroup
            t.value = match.group()
            self._next_char(len(t.value))
            return t
        else:
            return None


    def __read_string(self):
        if self.ch!='"':
            return None
        t = Token(lineno=self.lineno,lexpos=self.linepos)
        t.type = 'STRING'
        self._next_char()
        sb = array('c')
        while self.ch and self.ch != '"':
            # Should really check info on escaping further
            if self.ch == '\\':
                self._next_char()
            if self.ch:
                sb.append(self.ch)
            # Remember to update lineno
            if self.ch == '\n':
                self.lineno = self.lineno + 1
                self.linepos = -1
            self._next_char()

        if self.ch=='"':
            t.value = sb.tostring()
            self._next_char()
        else:
            err = ParseError("Unclosed string literal", t.lineno, t.lexpos)
            raise ParseException([err])
        return t


    def __error( self, msg ):
        raise ParseException([ParseError(msg,self.lineno,self.linepos)])



class SEXPParser(AbstractParser):
    '''Parser for S-expressions.'''

    def __init__( self,
                  lexerclass = SEXPLexer,
                  start = 'sexp',
                  tabmodule = 'sexp_parsetab',
                  debug = False,
                  debugfile = 'sexp_parser.out',
                  write_tables = False ):
        AbstractParser.__init__(self,lexerclass(),start,tabmodule,debug,debugfile,write_tables)
        self.tokens = lexerclass.tokens
        self.module = self
        self._yacc()

    def p_sexp( self, p ):
        """sexp : atom
                | list"""
        p[0] = p[1]

    def p_list( self, p ):
        """list : LPAREN sexp_seq RPAREN"""
        p[0] = ParentNode("sexp_list",p[2])

    def p_atom_a( self, p ):
        """atom : STRING"""
        p[0] = LeafNode("sexp_string",p[1],p.lexpos(1),p.lineno(1))

    def p_atom_b( self, p ):
        """atom : NUMBER"""
        p[0] = LeafNode("sexp_number",p[1],p.lexpos(1),p.lineno(1))

    def p_atom_c( self, p ):
        """atom : SYMBOL
                | DOT"""
        p[0] = LeafNode("sexp_symbol",p[1],p.lexpos(1),p.lineno(1))

    p_sexp_seq = make_list("sexp_seq","sexp")


## s = r'(a b c "a\"bba" (d nil))'

## l = SEXPLexer()
## l.input(s)
## x = l.token()
## while x:
##     print x
##     x = l.token()

## x = SEXPParser().parse(s)
## print tree_to_str(x)

## from pc.pp.PVS_Theory_PP import PVS_Theory_PP

## p = PVS_Theory_PP()
## print p.output_to_string(x)
