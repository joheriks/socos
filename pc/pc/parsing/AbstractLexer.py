from pc.parsing.ParseError import ParseError, ParseException
from pc.parsing.Token import Token

import re
from array import array

class AbstractLexer( object ):

    def __init__( self, startline=1, startcol=0 ):
        self.str = None
        self.str_len = 0
        self.pos = 0
        self.startline = startline
        self.startcol = startcol
        self.ch = None 


    def token( self ):
        """ Return next token (implement in subclasses). """
        assert False


    def input(self, str):
        """ Sets specified string as input to the lexer. Resets linenumber and position info """
        self.str = str
        self.str_len = len(str)
        self.pos = 0
        self.lineno = self.startline
        self.linepos = self.startcol
        if (self.str_len > 0):
            self.ch = str[0:1]
        else:
            self.ch = None


    def slurp(self):
        """ Consumes all input using token(). """
        while self.token(): pass


    def _next_char(self, amount = 1):
        """ Increments self.pos and sets ch to char at new self.pos"""
        if (self.pos > self.str_len - amount):
            self.ch = None
        else:
            self.pos = self.pos + amount
            self.ch = self.str[self.pos:self.pos + 1]
            self.linepos = self.linepos + amount 


    def _skip_whitespace( self ):
        while self.ch:
            if self.ch in (' ','\t'):
                self._next_char()
            elif self.ch == '\n':
                self._next_char()
                self.lineno += 1
                self.linepos = 0
            else:
                break
    
