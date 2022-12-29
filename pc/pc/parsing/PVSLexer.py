from pc.parsing.AbstractLexer import AbstractLexer
from pc.parsing.ParseError import ParseError, ParseException
from pc.parsing.Token import Token

import re
from array import *

class PVSLexer( AbstractLexer ):
    """ Lexer for the PVS language, using the same interface as the Lexer
    class in PLY, allowing integration with PLY parsers."""

    tokens = [
        'NUMBER',
        'STRING',
        'ID',
        'KEY_AND',
        'KEY_ANDTHEN',
        'KEY_ARRAY',
        'KEY_ASSUMING',
        'KEY_ASSUMPTION',
        'KEY_AUTOREWRITE',
        'KEY_AXIOM',
        'KEY_BEGIN',
        'KEY_BUT',
        'KEY_BY',
        'KEY_CASES',
        'KEY_CHALLENGE',
        'KEY_CLAIM',
        'KEY_CLOSURE',
        'KEY_COND',
        'KEY_CONJECTURE',
        'KEY_CONTAINING',
        'KEY_CONVERSION',
        'KEY_COROLLARY',
        'KEY_DATATYPE',
        'KEY_ELSE',
        'KEY_ELSIF',
        'KEY_END',
        'KEY_ENDASSUMING',
        'KEY_ENDCASES',
        'KEY_ENDCOND',
        'KEY_ENDIF',
        'KEY_ENDTABLE',
        'KEY_EXISTS',
        'KEY_EXPORTING',
        'KEY_FACT',
        'KEY_FALSE',
        'KEY_FORALL',
        'KEY_FORMULA',
        'KEY_FROM',
        'KEY_FUNCTION',
        'KEY_HAS_TYPE',
        'KEY_IF',
        'KEY_IFF',
        'KEY_IMPLIES',
        'KEY_IMPORTING',
        'KEY_IN',
        'KEY_INDUCTIVE',
        'KEY_JUDGEMENT',
        'KEY_LAMBDA',
        'KEY_LAW',
        'KEY_LEMMA',
        'KEY_LET',
        'KEY_LIBRARY',
        'KEY_MACRO',
        'KEY_MEASURE',
        'KEY_NONEMPTY_TYPE',
        'KEY_NOT',
        'KEY_O',
        'KEY_OBLIGATION',
        'KEY_OF',
        'KEY_OR',
        'KEY_ORELSE',
        'KEY_POSTULATE',
        'KEY_PROPOSITION',
        'KEY_RECURSIVE',
        'KEY_SUBLEMMA',
        'KEY_SUBTYPES',
        'KEY_SUBTYPES_OF',
        'KEY_TABLE',
        'KEY_THEN',
        'KEY_THEOREM',
        'KEY_THEORY',
        'KEY_TRUE',
        'KEY_TYPE',
        'KEY_TYPEPLUS',
        'KEY_VAR',
        'KEY_WHEN',
        'KEY_WHERE',
        'KEY_WITH',
        'KEY_XOR',
        'KEY_AUTOREWRITEPLUS',
        'KEY_AUTOREWRITEMINUS',
        'KEY_CONVERSIONPLUS',
        'KEY_CONVERSIONMINUS',
        'DOT_DOT',
        'HASH_HASH',
        'HASH_RPAREN',
        'HASH_RBRACKET',
        'HASH',
        'PERCENTAGE',
        'AMPERSAND_AMPERSAND',
        'AMPERSAND',
        'LPAREN_HASH',
        'LPAREN_COLON',
        'LPAREN_VBAR_VBAR_RPAREN',
        'LPAREN_VBAR',
        'LPAREN',
        'RPAREN',
        'STAR_STAR',
        'STAR',
        'PLUS_PLUS',
        'PLUS',
        'COMMA',
        'MINUS_GT',
        'MINUS',
        'DOT',
        'SLASH_SLASH',
        'SLASH_EQUAL',
        'SLASH_BSLASH',
        'SLASH',
        'COLON_RPAREN',
        'COLON_COLON',
        'COLON_EQUAL',
        'COLON',
        'SEMI_COLON',
        'LT_LT_EQUAL',
        'LT_LT',
        'LT_EQUAL_GT',
        'LT_EQUAL',
        'LT_GT',
        'LT_VBAR',
        'LT',
        'EQUAL_EQUAL',
        'EQUAL_GT',
        'EQUAL',
        'GT_EQUAL',
        'GT_GT_EQUAL',
        'GT_GT',
        'GT',
        'AT_AT',
        'AT',
        'LBRACKET_HASH',
        'LBRACKET_RBRACKET',
        'LBRACKET_VBAR_VBAR_RBRACKET',
        'LBRACKET_VBAR',
        'LBRACKET',
        'BSLASH_SLASH',
        'BSLASH',
        'RBRACKET_VBAR',
        'RBRACKET',
        'CARET_CARET',
        'CARET',
        'SINGLE_QUOTE',
        'LBRACE_VBAR_VBAR_RBRACE',
        'LBRACE_VBAR',
        'LBRACE',
        'VBAR_RPAREN',
        'VBAR_MINUS_GT',
        'VBAR_MINUS',
        'VBAR_EQUAL',
        'VBAR_GT',
        'VBAR_LBRACKET',
        'VBAR_RBRACKET',
        'VBAR_VBAR',
        'VBAR_RBRACE',
        'VBAR',
        'RBRACE',
        'TILDE',
        'EXCLAMATION_MARK' ]
    

    keyword_map = {
        'AND' : 'KEY_AND',
        'ANDTHEN' : 'KEY_ANDTHEN',
        'ARRAY' : 'KEY_ARRAY',
        'ASSUMING' : 'KEY_ASSUMING',
        'ASSUMPTION' : 'KEY_ASSUMPTION',
        'AUTOREWRITE' : 'KEY_AUTOREWRITE',
        'AXIOM' : 'KEY_AXIOM',
        'BEGIN' : 'KEY_BEGIN',
        'BUT' : 'KEY_BUT',
        'BY' : 'KEY_BY',
        'CASES' : 'KEY_CASES',
        'CHALLENGE' : 'KEY_CHALLENGE',
        'CLAIM' : 'KEY_CLAIM',
        'CLOSURE' : 'KEY_CLOSURE',
        'COND' : 'KEY_COND',
        'CONJECTURE' : 'KEY_CONJECTURE',
        'CONTAINING' : 'KEY_CONTAINING',
        'CONVERSION' : 'KEY_CONVERSION',
        'COROLLARY' : 'KEY_COROLLARY',
        'DATATYPE' : 'KEY_DATATYPE',
        'ELSE' : 'KEY_ELSE',
        'ELSIF' : 'KEY_ELSIF',
        'END' : 'KEY_END',
        'ENDASSUMING' : 'KEY_ENDASSUMING',
        'ENDCASES' : 'KEY_ENDCASES',
        'ENDCOND' : 'KEY_ENDCOND',
        'ENDIF' : 'KEY_ENDIF',
        'ENDTABLE' : 'KEY_ENDTABLE',
        'EXISTS' : 'KEY_EXISTS',
        'EXPORTING' : 'KEY_EXPORTING',
        'FACT' : 'KEY_FACT',
        'FALSE' : 'KEY_FALSE',
        'FORALL' : 'KEY_FORALL',
        'FORMULA' : 'KEY_FORMULA',
        'FROM' : 'KEY_FROM',
        'FUNCTION' : 'KEY_FUNCTION',
        'HAS_TYPE' : 'KEY_HAS_TYPE',
        'IF' : 'KEY_IF',
        'IFF' : 'KEY_IFF',
        'IMPLIES' : 'KEY_IMPLIES',
        'IMPORTING' : 'KEY_IMPORTING',
        'IN' : 'KEY_IN',
        'INDUCTIVE' : 'KEY_INDUCTIVE',
        'JUDGEMENT' : 'KEY_JUDGEMENT',
        'LAMBDA' : 'KEY_LAMBDA',
        'LAW' : 'KEY_LAW',
        'LEMMA' : 'KEY_LEMMA',
        'LET' : 'KEY_LET',
        'LIBRARY' : 'KEY_LIBRARY',
        'MACRO' : 'KEY_MACRO',
        'MEASURE' : 'KEY_MEASURE',
        'NONEMPTY_TYPE' : 'KEY_NONEMPTY_TYPE',
        'NOT' : 'KEY_NOT',
        'O' : 'KEY_O',
        'OBLIGATION' : 'KEY_OBLIGATION',
        'OF' : 'KEY_OF',
        'OR' : 'KEY_OR',
        'ORELSE' : 'KEY_ORELSE',
        'POSTULATE' : 'KEY_POSTULATE',
        'PROPOSITION' : 'KEY_PROPOSITION',
        'RECURSIVE' : 'KEY_RECURSIVE',
        'SUBLEMMA' : 'KEY_SUBLEMMA',
        'SUBTYPES' : 'KEY_SUBTYPES',
        'SUBTYPES_OF' : 'KEY_SUBTYPES_OF',
        'TABLE' : 'KEY_TABLE',
        'THEN' : 'KEY_THEN',
        'THEOREM' : 'KEY_THEOREM',
        'THEORY' : 'KEY_THEORY',
        'TRUE' : 'KEY_TRUE',
        'TYPE' : 'KEY_TYPE',
        'VAR' : 'KEY_VAR',
        'WHEN' : 'KEY_WHEN',
        'WHERE' : 'KEY_WHERE',
        'WITH' : 'KEY_WITH',
        'XOR' : 'KEY_XOR' }

    symbols = [
        r'(?P<HASH_HASH>##)',
        r'(?P<HASH_RPAREN>#\))',
        r'(?P<HASH_RBRACKET>#\])',
        r'(?P<HASH>#)',
        r'(?P<PERCENTAGE>%)',
        r'(?P<AMPERSAND_AMPERSAND>\&\&)',
        r'(?P<AMPERSAND>\&)',
        r'(?P<LPAREN_HASH>\(\#)',
        r'(?P<LPAREN_COLON>\(\:)',
        r'(?P<LPAREN_VBAR_VBAR_RPAREN>\(\|\|\))',
        r'(?P<LPAREN_VBAR>\(\|)',
        r'(?P<LPAREN>\()',
        r'(?P<RPAREN>\))',
        r'(?P<STAR_STAR>\*\*)',
        r'(?P<STAR>\*)',
        r'(?P<PLUS_PLUS>\+\+)',
        r'(?P<PLUS>\+)',
        r'(?P<COMMA>\,)',
        r'(?P<MINUS_GT>\-\>)',
        r'(?P<MINUS>\-)',
        r'(?P<DOT>\.)',
        r'(?P<SLASH_SLASH>\/\/)',
        r'(?P<SLASH_EQUAL>\/\=)',
        r'(?P<SLASH_BSLASH>\/\\)',
        r'(?P<SLASH>\/)',
        r'(?P<COLON_RPAREN>\:\))',
        r'(?P<COLON_COLON>\:\:)',
        r'(?P<COLON_EQUAL>\:\=)',
        r'(?P<COLON>\:)',
        r'(?P<SEMI_COLON>\;)',
        r'(?P<LT_LT_EQUAL>\<\<\=)',
        r'(?P<LT_LT>\<\<)',
        r'(?P<LT_EQUAL_GT>\<\=\>)',
        r'(?P<LT_EQUAL>\<\=)',
        r'(?P<LT_GT>\<\>)',
        r'(?P<LT_VBAR>\<\|)',
        r'(?P<LT>\<)',
        r'(?P<EQUAL_EQUAL>\=\=)',
        r'(?P<EQUAL_GT>\=\>)',
        r'(?P<EQUAL>\=)',
        r'(?P<GT_EQUAL>\>\=)',
        r'(?P<GT_GT_EQUAL>\>\>\=)',
        r'(?P<GT_GT>\>\>)',
        r'(?P<GT>\>)',
        r'(?P<AT_AT>\@\@)',
        r'(?P<AT>\@)',
        r'(?P<LBRACKET_HASH>\[\#)',
        r'(?P<LBRACKET_RBRACKET>\[\])',
        r'(?P<LBRACKET_VBAR_VBAR_RBRACKET>\[\|\|\])',
        r'(?P<LBRACKET_VBAR>\[\|)',
        r'(?P<LBRACKET>\[)',
        r'(?P<BSLASH_SLASH>\\\/)',
        r'(?P<BSLASH>\\)',
        r'(?P<RBRACKET_VBAR>\]\|)',
        r'(?P<RBRACKET>\])',
        r'(?P<CARET_CARET>\^\^)',
        r'(?P<CARET>\^)',
        r'(?P<SINGLE_QUOTE>`)',
        r'(?P<LBRACE_VBAR_VBAR_RBRACE>\{\|\|\})',
        r'(?P<LBRACE_VBAR>\{\|)',
        r'(?P<LBRACE>\{)',
        r'(?P<VBAR_RPAREN>\|\))',
        r'(?P<VBAR_MINUS_GT>\|\-\>)',
        r'(?P<VBAR_MINUS>\|\-)',
        r'(?P<VBAR_EQUAL>\|\=)',
        r'(?P<VBAR_GT>\|\>)',
        r'(?P<VBAR_LBRACKET>\|\[)',
        r'(?P<VBAR_RBRACKET>\|\])',
        r'(?P<VBAR_VBAR>\|\|)',
        r'(?P<VBAR_RBRACE>\|\})',
        r'(?P<VBAR>\|)',
        r'(?P<RBRACE>\})',
        r'(?P<TILDE>\~)',
        r'(?P<EXCLAMATION_MARK>\!)' ]
    
    symbol_regex = re.compile("|".join(symbols))

    id_regex = re.compile('([A-Za-z]|_|\?|[0-9])+')
    
    # preferrably fix with something that looks a bit nicer
    table_hack = { "+" : "PLUS", "-" : "MINUS" }


    def token(self):
        """ Returns next token in input, or None if no
        more input. Conforms to PLY lexer interface"""

        if (self.pos >= self.str_len):
            return None

        self._skip_whitespace()

        if not self.ch:
            return None

        return self._next_token()


    def _next_token( self ):
        t = Token(lineno=self.lineno,lexpos=self.linepos)
        if (re.match(r'\d', self.ch)):
            self.__read_number(t)
        elif (self.ch == '"'):
            self.__read_string(t)
        elif (re.match(r'[A-Za-z]', self.ch)):
            self.__read_id_or_keyword(t)
        else:
            self.__read_special_symbol(t)
        return t


    def __read_special_symbol(self, t):
        """ Reads a 'special symbol' (see page 8, PVS language reference) storing
        information in token t."""
        match = self.symbol_regex.match(self.str, self.pos)
        if (match):
            t.type = match.lastgroup
            t.value = match.group()
            self._next_char(len(t.value))
        else:
            x = "'%s'"%self.ch if 0x20 <= ord(self.ch) <= 0x7E else "(code 0x%x)"%ord(self.ch)
            err = ParseError("Unknown symbol %s"%x, t.lineno, t.lexpos)
            raise ParseException([err])

    def __read_number(self, t):
        """ Reads a number, storing information in token t. Assumes we are already looking
        at [0-9] before call."""
        t.type = 'NUMBER'
        start_pos = self.pos

        while (self.ch and re.match(r'\d', self.ch)):
            self._next_char()

        t.value = self.str[start_pos:self.pos]

    def __read_id_or_keyword(self, t):
        """ Reads an identifier or keyword, storing information in token t.
        Assumes we are already looking at [A-Za-z] before call. """
        match = self.id_regex.match(self.str, self.pos)
        tmp_str = match.group()
        self._next_char(len(tmp_str))
        type = self.keyword_map.get(tmp_str.upper())

        if (not type):
            type = 'ID'
        elif (((type == 'KEY_AUTOREWRITE' or type == 'KEY_CONVERSION')
               and self.ch and (self.ch == '+' or self.ch == '-'))
              or (type == 'KEY_TYPE' and self.ch and self.ch == '+')):
            type = type + self.table_hack[self.ch]
            tmp_str = tmp_str + self.ch
            self._next_char()

        t.type = type
        t.value = tmp_str

    def __read_string(self, t):
        """ Tokenizes a string type and stores the information in token t.
        Assumes we are already looking at first double-quote before call"""
        t.type = 'STRING'
        self._next_char()
        sb = array('c')
        while self.ch and self.ch != '"':
            # Should really check info on escaping further
            if self.ch == '\\':
                self._next_char()
                if self.ch=='\\' or self.ch=='"':
                    sb.append(self.ch)
                else:
                    raise ParseException([ParseError("Illegal character after backslash", self.lineno, self.linepos)])
            else:
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

    def _skip_whitespace(self):
        """ Skips whitespace and comments in input until next non-whitespace character"""
        while True:
            if (self.ch and (self.ch == ' ' or self.ch == '\t')):
                self._next_char()
            elif (self.ch and self.ch == '\n'):
                self._next_char()
                self.lineno = self.lineno + 1
                self.linepos = 0
            elif (self.ch and self.ch == '%'):
                while (self.ch and self.ch != '\n'):
                    self._next_char()
            else:
                break

