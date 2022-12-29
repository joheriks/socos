from pc.parsing.AbstractLexer import AbstractLexer
from pc.parsing.ParseError import ParseError, ParseException
from pc.parsing.Token import Token

import re
#from array import array
from collections import OrderedDict

def GetTeXBaseLexer(decimal_sep = None, tuple_sep = None):
    class TeXLexer( AbstractLexer ):
        decimal_separator = decimal_sep or "."
        tuple_separator = tuple_sep or ","

        commands = {
            r"BSLASH_Delta":   0,
            r"BSLASH_Gamma":   0,
            r"BSLASH_Lambda":  0,
            r"BSLASH_NN":      0,
            r"BSLASH_Omega":   0,        
            r"BSLASH_Phi":     0,
            r"BSLASH_Pi":      0,
            r"BSLASH_Psi":     0,
            r"BSLASH_QQ":      0,
            r"BSLASH_RR":      0,
            r"BSLASH_Sigma":   0,
            r"BSLASH_Theta":   0,
            r"BSLASH_Upsilon": 0,
            r"BSLASH_Xi":      0,
            r"BSLASH_ZZ":      0,
            r"BSLASH_alpha":   0,
            r"BSLASH_beta":    0,
            r"BSLASH_chi":     0,
            r"BSLASH_delta":   0,
            r"BSLASH_epsilon": 0,
            r"BSLASH_eta":     0,
            r"BSLASH_frac":    2,
            r"BSLASH_gamma":   0,
            r"BSLASH_iota":    0,
            r"BSLASH_kappa":   0,
            r"BSLASH_mu":      0,
            r"BSLASH_nu":      0,
            r"BSLASH_omega":   0,
            r"BSLASH_phi":     0,
            r"BSLASH_pi":      0,
            r"BSLASH_psi":     0,
            r"BSLASH_rho":     0,
            r"BSLASH_sigma":   0,
            r"BSLASH_sqrt":    1,
            r"BSLASH_tau":     0,
            r"BSLASH_theta":   0,
            r"BSLASH_upsilon": 0,
            #r"BSLASH_unit":    1,
            r"BSLASH_xi":      0,
            r"BSLASH_zeta":    0,
        }

        commands_binop = [
            r"Leftarrow",
            r"Leftrightarrow",
            r"Rightarrow",
            r"cdot",
            r"circ",
            r"equiv",
            r"ge",
            r"gt",
            r"iff",
            r"implies",
            r"in",
            r"land",
            r"le",
            r"leftarrow",
            r"leftrightarrow",
            r"lor",
            r"lt",
            r"models",
            r"ne",
            r"neg",
            r"rightarrow",
            r"vdash",
            r"vee",
            r"wedge",
            r"bullet",
            r"exists",
            r"forall",
            r"lambda",
            r"times",
            r"to",
            r"approx",
        ]

        space_regex = re.compile(r"\s")

        # LaTeX math-spacing (\, \: \;) and hard spaces
        ws = r"(?:\\[,:;])|(?:\xa0)|\s"
        ws_any = "(?:%s)*"%ws
        ws_some = "(?:%s)+"%ws
        ws_regex = re.compile(ws)
        ws_some_regex = re.compile("(?:%s)+"%ws)
        ws_endofstring_regex = re.compile("(?:%s)*$"%ws)

        symbol_map = {}
        grouping_symbol_map = {
            "LBRACKET": r"(?:\\left)?(?:\s)*\[%s" % ws_any,
            "RBRACKET": r"%s(?:\\right)?(?:\s)*\]" % ws_any,
            "LPAREN": r"(?:\\left)?(?:\s)*\(%s" % ws_any,
            "RPAREN": r"%s(?:\\right)?(?:\s)*\)" % ws_any,
            "LVBAR": r"\\left(?:\s)*\|%s" % ws_any,
            "RVBAR": r"%s\\right(?:\s)*\|" % ws_any,
        }

        def get_number(self, s):
            if not s:
                return s
            t = ""
            for a in s:
                if '0' <= a and a <= '9':
                    t = t + a
                if a == self.decimal_separator:
                    t = t + "."
            return t

        symbol_bin_op_map = {
            "COMMA": "|".join([re.escape(s) for s in tuple_separator.split('|')]),
            "COLON": re.escape(":"),
            "EQUAL": re.escape("="),
            "PLUS": re.escape("+"),
            "MINUS": re.escape("-"),
            "SLASH": re.escape("/"),
            "GT": re.escape(">"),
            "LT": re.escape("<"),
            "DEF": re.escape("def"),
       }

        symbols = ([ r"(?P<%s>%s)"%(t,r) for (t, r) in grouping_symbol_map.items() ] +
                   [ r"%s(?P<%s>%s)%s"%(ws_any,t, r,ws_any) for (t,r) in symbol_bin_op_map.items() ])

        command_binop_regex = re.compile(r"%s(?P<COMMAND>\\(?:%s))(?:(?:%s)|(?:(?![a-zA-Z])))" % (ws_any, "|".join(commands_binop), ws_some))

        number_ending = "(?!(%s[0-9]))" % ws_any
        nat_number = "(([0-9]{1,3}(%s[0-9]{3})+)|([0-9]+))" % ws_some

        number_ending = "(?!(%s[0-9]))" % ws_any
        nat_number = "(([0-9]{1,3}(%s[0-9]{3})+)|([0-9]+))" % ws_some
        mixfrac = r"(?P<INTEGER>%s)%s\\frac(\s)*((?P<NOMINa>[0-9])|(\{%s(?P<NOMINb>%s)%s\}))(\s)*((?P<DENOMa>[0-9])|(\{%s(?P<DENOMb>%s)%s\}))" % (
            nat_number, ws_any, ws_any, nat_number, ws_any, ws_any, nat_number, ws_any)
        number = "%s(%s[0-9]+)?%s" % (nat_number, re.escape(decimal_separator), number_ending)

        unit = r"\\unit\{%s(?P<UNITNAME>[A-Za-z]+)%s\}" % (ws_any, ws_any)

        terminal_re_map = OrderedDict([
            ("FALSE","False|false|F"),
            ("TRUE","True|true|T"),
            ("ID", "[A-Za-z][A-Za-z0-9?_]*"),
            ("MIXFRAC", mixfrac),
            ("NUMBER", number),
            ("UNIT", unit),
        ])
        terminals = [ r"(?P<%s>%s)" % (s, t) for (s,t) in terminal_re_map.items() ]


        short_terminal_re_map = {
            "ID": "[A-Za-z]",
            "NUMBER": "[0-9]"
        }
        short_terminals = [ r"(?P<%s>%s)"%p for p in short_terminal_re_map.items() ]

        tokens = [
            "ID",
            "NUMBER",
            "MIXFRAC",
            "TRUE",
            "FALSE",
            "UNIT",
            
            "DEF",
            "COLON",
            "COMMA",
            
            "LBRACE",
            "RBRACE",
        
            "CARET",
            "EQUAL",
            "MINUS",
            "PLUS",
            "SLASH",
            "GT",
            "LT",
            "SPACE",
            "WRONG",
        ] + grouping_symbol_map.keys() + commands.keys() + ["BSLASH_" + s for s in commands_binop]

        comment_regex = re.compile(r"((\\[,:;])|(\xa0))*\\text\{(?P<COMMENT>.*?)\}((\\[,:;])|(\xa0))*")
        command_regex = re.compile(r"(?P<COMMAND>\\[A-Za-z]+)")
        shortmatch_regex = re.compile("|".join(symbols+short_terminals))
        longmatch_regex = re.compile("|".join(symbols+terminals))

        BEGINNING, MIDDLE = 0, 1


        def __init__( self, startline=1, startcol=0 ):
            AbstractLexer.__init__(self,startline,startcol)

            self.__stack = []
            self.__push()
            self.__status = self.BEGINNING


        def _skip_whitespace( self ):
            AbstractLexer._skip_whitespace(self)
            m = self.space_regex.match(self.str,self.pos)
            if m:
                self._next_char(len(m.group()))
                self._skip_whitespace()


        def token( self ):
            x = self.pos
            if self._read_end_of_string():
                return None

            if self.__status == self.BEGINNING:
                self.__read_implicit_multiplication()

            self._skip_whitespace()

            self.__status = self.MIDDLE

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

            t = self.__read_identifier_number_symbol()
            t = t or self.__read_command_binop()
            t = t or self.__read_command()
            t = t or self.__read_implicit_multiplication()

            if not t:
                value = self.ch
                self._next_char()
                t = Token(type = "WRONG", value = value, lineno=self.lineno,lexpos=self.linepos)

            assert self.pos > x,"failed to produce token at pos %d" % self.pos

            if t:
                return t

            x = "'%s'"%self.ch if 0x20 <= ord(self.ch) <= 0x7E else "(code 0x%x)"%ord(self.ch)
            self.__error("Unknown symbol %s" % x)

            return None


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


        def __read_implicit_multiplication( self ):
            m = self.ws_some_regex.match(self.str, self.pos)
            if m:
                self._next_char(len(m.group()))
                return Token(type="SPACE",value=r"\;",lineno=self.lineno,lexpos=self.linepos)
            return None

        def _read_end_of_string(self):
            m = self.ws_endofstring_regex.match(self.str,self.pos)
            if m:
                self._next_char(len(m.group()))
                return True
            return False


        def __read_comment( self ):
            match = self.comment_regex.match(self.str, self.pos)
            if not match: return None
            self._next_char(match.span()[1]-match.span()[0])
            return Token(type="COMMENT",value=match.group("COMMENT"),lineno=self.lineno,lexpos=self.linepos)


        def __read_command_binop( self ):
            match = self.command_binop_regex.match(self.str, self.pos)
            if not match: return None
            command = match.group("COMMAND")
            #print command
            token = command.replace("\\","BSLASH_")
            #print token
            self._next_char(match.span()[1]-match.span()[0])
            return Token(type = token, value = match.group(), lineno = self.lineno, lexpos = self.linepos)

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
                t = Token(type=m.lastgroup,value=m.group(),lineno=self.lineno,lexpos=self.linepos)
                if m.lastgroup == "MIXFRAC":
                    t.integer = self.get_number(m.groupdict()["INTEGER"])
                    t.nomin = self.get_number(m.groupdict()["NOMINa"] or m.groupdict()["NOMINb"])
                    t.denom = self.get_number(m.groupdict()["DENOMa"] or m.groupdict()["DENOMb"])
                elif m.lastgroup == "UNIT":
                    t.unitname = m.groupdict()["UNITNAME"]
                elif m.lastgroup == "NUMBER":
                    t.value =  self.get_number(t.value)
                self._next_char(len(m.group()))
                self.__decr_args_count()
                return t
            else:
                return None

    return TeXLexer
