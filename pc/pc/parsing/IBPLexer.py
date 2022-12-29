from pc.parsing.PVSLexer import PVSLexer
from pc.parsing.Token import Token
from pc.parsing.ParseError import ParseError,ParseException

import re
from array import *

class IBPLexer(PVSLexer):
    '''Lexer for the IBP language, using the same interface as pc.PVSLexer'''

    tokens = PVSLexer.tokens + [
        'KEY_ADD',
        'KEY_CALL',
        'KEY_CASE',
        'KEY_CHOICE',
        'KEY_CONTEXT',
        'KEY_DEL',
        'KEY_DECREASING',
        'KEY_DERIVATION',
        'KEY_ENDCALL',
        'KEY_ENDCHOICE',
        'KEY_ENDPROOF',
        'KEY_EXTENDING',
        'KEY_EXIT',
        'KEY_GOTO',
        'KEY_POST',
        'KEY_PRE',
        'KEY_PROCEDURE',
        'KEY_PVAR',
        'KEY_RESULT',
        'KEY_SIMPLIFY',
        'KEY_SOLVE',
        'KEY_SITUATION',
        'KEY_USE',
        'KEY_VALRES',
        'QUESTIONMARK',
        'TEX',
        'KEY_TEXDECIMALSEP',
        'KEY_TEXTUPLESEP',
        'KEY_TEXPARSER',
        ]

    keyword_map = PVSLexer.keyword_map.copy()
    keyword_map.update({
        'ADD' : 'KEY_ADD',
        'CALL' : 'KEY_CALL' ,
        'CASE' : 'KEY_CASE',
        'CHOICE' : 'KEY_CHOICE',
        'CONTEXT' : 'KEY_CONTEXT',
        'DEL' : 'KEY_DEL',
        'DECREASING' : 'KEY_DECREASING',
        'DERIVATION' : 'KEY_DERIVATION',
        'ENDCALL' : 'KEY_ENDCALL',
        'ENDCHOICE' : 'KEY_ENDCHOICE',
        'ENDPROOF' : 'KEY_ENDPROOF',
        'EXTENDING' : 'KEY_EXTENDING',
        'EXIT' : 'KEY_EXIT',
        'GOTO' : 'KEY_GOTO',
        'POST' : 'KEY_POST',
        'PRE': 'KEY_PRE',
        'PROCEDURE' : 'KEY_PROCEDURE',
        'PROOF' : 'KEY_PROOF',
        'PVAR' : 'KEY_PVAR',
        'RESULT' : 'KEY_RESULT',
        'SITUATION' : 'KEY_SITUATION',
        'SIMPLIFY' : 'KEY_SIMPLIFY',
        'SOLVE' : 'KEY_SOLVE',
        'THEN' : 'KEY_THEN',
        'STRATEGY' : 'KEY_STRATEGY',
        'USE' : 'KEY_USE',
        'VALRES' : 'KEY_VALRES',
        '?' : 'QUESTIONMARK',
        'TEXDECIMALSEP' : 'KEY_TEXDECIMALSEP',
        'TEXTUPLESEP' : 'KEY_TEXTUPLESEP',
        'TEXPARSER' : 'KEY_TEXPARSER',
        })
    
    symbols = PVSLexer.symbols + [ r'(?P<QUESTIONMARK>\?)' ] 
    
    symbol_regex = re.compile("|".join(symbols))


    def _next_token( self ):
        if self.ch=="$":
            return self.__read_tex_expression()
        else:
            return PVSLexer._next_token(self)

            
    def __read_tex_expression( self ):
        t = Token(type="TEX",value="",lineno=self.lineno,lexpos=self.linepos)
        start_pos = self.pos
        self._next_char()
        sb = array('c')
        lastchar = None
        while self.ch:
            # Ignore escaped dollar-sign
            if self.ch == "$" and lastchar!="\\":
                break
            else:
                sb.append(self.ch)
                
            # Remember to update lineno
            if self.ch == '\n':
                self.lineno = self.lineno + 1
                self.linepos = -1

            lastchar = self.ch
            self._next_char()

        if self.ch=="$":
            t.value = sb.tostring()
            self._next_char()
            return t
        else:
            err = ParseError("Unclosed TeX expression", t.lineno, t.lexpos)
            raise ParseException([err])
