from pc.parsing.AbstractLexer import AbstractLexer
from pc.parsing.ParseError import ParseError, ParseException
from pc.parsing.Token import Token

import re
from array import array
from collections import OrderedDict

class TeXLexer( AbstractLexer ):

    commands = {
        r"BSLASH_Delta" : 0,
        r"BSLASH_Gamma" : 0,
        r"BSLASH_Lambda" : 0,
        r"BSLASH_Leftarrow" : 0,
        r"BSLASH_Leftrightarrow" : 0,
        r"BSLASH_NN" : 0,
        r"BSLASH_Omega" : 0,        
        r"BSLASH_Phi" : 0,
        r"BSLASH_Pi" : 0,
        r"BSLASH_Psi" : 0,
        r"BSLASH_QQ" : 0,
        r"BSLASH_Rightarrow" : 0,
        r"BSLASH_RR" : 0,
        r"BSLASH_Sigma" : 0,
        r"BSLASH_Theta" : 0,
        r"BSLASH_Upsilon" : 0,
        r"BSLASH_Xi" : 0,
        r"BSLASH_ZZ" : 0,
        r"BSLASH_alpha" : 0,
        r"BSLASH_beta" : 0,
        r"BSLASH_bullet": 0,
        r"BSLASH_cdot": 0,
        r"BSLASH_chi" : 0,
        r"BSLASH_circ": 0,
        r"BSLASH_delta" : 0,
        r"BSLASH_epsilon" : 0,
        r"BSLASH_equiv": 0,
        r"BSLASH_eta" : 0,
        r"BSLASH_exists": 0,
        r"BSLASH_forall": 0,
        r"BSLASH_frac": 2,
        r"BSLASH_gamma" : 0,
        r"BSLASH_ge": 0,
        r"BSLASH_gt": 0,
        r"BSLASH_iff" : 0,
        r"BSLASH_implies" : 0,
        r"BSLASH_in" : 0,
        r"BSLASH_iota": 0,
        r"BSLASH_kappa" : 0,
        r"BSLASH_lambda" : 0,
        r"BSLASH_land" : 0,
        r"BSLASH_le": 0,
        r"BSLASH_left": 1,
        r"BSLASH_leftarrow": 0,
        r"BSLASH_leftrightarrow": 0,
        r"BSLASH_lor" : 0,
        r"BSLASH_lt": 0,
        r"BSLASH_models": 0,
        r"BSLASH_mu" : 0,
        r"BSLASH_ne": 0,
        r"BSLASH_neg": 0,
        r"BSLASH_nu" : 0,
        r"BSLASH_omega" : 0,
        r"BSLASH_phi" : 0,
        r"BSLASH_pi" : 0,
        r"BSLASH_psi" : 0,
        r"BSLASH_rho" : 0,
        r"BSLASH_right": 1,
        r"BSLASH_rightarrow": 0,
        r"BSLASH_sigma" : 0,
        r"BSLASH_sqrt": 1,
        r"BSLASH_tau" : 0,
        r"BSLASH_theta" : 0,
        r"BSLASH_upsilon" : 0,
        r"BSLASH_unit" : 1,
        r"BSLASH_vdash": 0,
        r"BSLASH_vee" : 0,
        r"BSLASH_wedge" : 0,
        r"BSLASH_xi" : 0,
        r"BSLASH_zeta" : 0,
    }

    symbol_map = {
        "CARET": "^",
        "COMMA": ",",
        "COLON": ":",
        "DOT": ".",
        "EQUAL": "=",
        "PLUS": "+",
        "MINUS": "-",
        "SLASH": "/",
        "GT": ">",
        "LT": "<",
        "LBRACE": "{",
        "LBRACKET": "[",
        "RBRACE": "}",
        "RBRACKET": "]",
        "LPAREN": "(",
        "RPAREN": ")",
        "VBAR": "|"
    }
    symbols = [ r"(?P<%s>%s)"%(t,re.escape(r)) for (t,r) in symbol_map.items() ]

    terminal_re_map = OrderedDict([
        ("FALSE","False|false|F"),
        ("TRUE","True|true|T"),
        ("ID", "[A-Za-z][A-Za-z0-9\?_]*"), # Old one: ("ID", "[A-Za-z][A-Za-z0-9?_]*"),
        ("NUMBER", "[0-9]+")
    ])
    terminals = [ r"(?P<%s>%s)"%p for p in terminal_re_map.items() ]


    short_terminal_re_map = {
        "ID": "[A-Za-z]",
        "NUMBER": "[0-9]"
    }
    short_terminals = [ r"(?P<%s>%s)"%p for p in short_terminal_re_map.items() ]
    
    tokens = [
        "ID",
        "NUMBER",
        "TRUE",
        "FALSE",
        
        "COLON",
        "COMMA",
        "DOT",

        "LBRACE",
        "RBRACE",
        "LPAREN",
        "RPAREN",
        "LBRACKET",
        "RBRACKET",
        "VBAR",
        
        "CARET",
        "EQUAL",
        "MINUS",
        "PLUS",
        "SLASH",
        "GT",
        "LT",
    ] + commands.keys()

    # LaTeX math-spacing commands (\, \: \;) and hard spaces are
    # treated as whitespace
    ws_regex = re.compile(r"(?P<COMMAND>\\[,:;])|(\xa0)")
    
    comment_regex = re.compile(r"\\text\{(?P<COMMENT>.*?)\}")
    command_regex = re.compile(r"(?P<COMMAND>\\[A-Za-z]+)")
    shortmatch_regex = re.compile("|".join(symbols+short_terminals))
    longmatch_regex = re.compile("|".join(symbols+terminals))
    

    def __init__( self, startline=1, startcol=0 ):
        AbstractLexer.__init__(self,startline,startcol)

        self.__stack = []
        self.__push()


    def _skip_whitespace( self ):
        AbstractLexer._skip_whitespace(self)
        m = self.ws_regex.match(self.str, self.pos)
        if m:
            self._next_char(len(m.group()))
            self._skip_whitespace()


    def token( self ):
        self._skip_whitespace()

        if self.__read_comment():
            return self.token()

        if (self.ch==None or self.pos >= self.str_len):
            if not len(self.__stack)==1:
                self.__error("Missing } in TeX expression")
            elif self.__stack[-1]!=0:
                self.__error("Missing arguments at end of TeX expression")
            return None

        if self.ch=="{":
            self.__decr_args_count()
            self.__push()
            self._next_char()
            return Token(type="LBRACE",value="{",lineno=self.lineno,lexpos=self.linepos)
            
        elif self.ch=="}":
            self.__pop()
            self._next_char()
            return Token(type="RBRACE",value="}",lineno=self.lineno,lexpos=self.linepos)

        elif self.ch=="^":
            #if self.__get_args_count()!=0:
            #    self.__error("Missing arguments before superscript in TeX expression")
            self._next_char()
            self.__set_args_count(1)
            return Token(type="CARET",value="^",lineno=self.lineno,lexpos=self.linepos)

        # TODO: subscript

        t = self.__read_command() or self.__read_identifier_number_symbol()
        if t:
            return t
        else:
            x = "'%s'"%self.ch if 0x20 <= ord(self.ch) <= 0x7E else "(code 0x%x)"%ord(self.ch)
            self.__error("Unknown symbol %s" % x)
        

    def __error( self, msg ):
        raise ParseException([ParseError(msg,self.lineno,self.linepos)])
    

    def __set_args_count( self, c ):
        self.__stack[-1] = c


    def __get_args_count( self ):
        return self.__stack[-1]


    def __decr_args_count( self ):
        if self.__stack[-1]>0:
            self.__stack[-1] -= 1


    def __push( self ):
        self.__stack.append(0)


    def __pop( self ):
        if len(self.__stack)<=1:
            self.__error("Extraneous } in TeX expression")
        if self.__stack[-1]>0:
            self.__error("Missing an argument in TeX expression")
        self.__stack.pop()


    def __read_comment( self ):
        match = self.comment_regex.match(self.str, self.pos)
        if not match: return None
        self._next_char(match.span()[1]-match.span()[0])
        return Token(type="COMMENT",value=match.group("COMMENT"),lineno=self.lineno,lexpos=self.linepos)
        

    def __read_command( self ):
        match = self.command_regex.match(self.str, self.pos)
        if not match: return None
        command = match.group("COMMAND")
        token = command.replace("\\","BSLASH_")
        if token not in self.commands:
            self.__error("Unsupported TeX command '%s'"%command)
        self._next_char(match.span()[1]-match.span()[0])
        self.__set_args_count(self.commands[token])
        return Token(type=token,value=command,lineno=self.lineno,lexpos=self.linepos)
    

    def __read_identifier_number_symbol( self ):
        r = self.shortmatch_regex if self.__get_args_count() else self.longmatch_regex
        m = r.match(self.str,self.pos)
        if m:
            self._next_char(len(m.group()))
            self.__decr_args_count()
            return Token(type=m.lastgroup,value=m.group(),lineno=self.lineno,lexpos=self.linepos)
        else:
            return None
    
