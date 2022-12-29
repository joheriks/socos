from pc.parsing.ParseError import ParseError, ParseException
from pc.parsing.Token import Token

from pc.parsing.TeXBaseLexer import *

import re
from array import array
from collections import OrderedDict

def GetTeXSingleLexer(decimal_sep = None, tuple_sep = None):
    TexBaseLexerClass =  GetTeXBaseLexer(decimal_sep, tuple_sep)
    class TeXLexer( TexBaseLexerClass ):
        functions = [
            "sin",
            "cos",
            "tan",
            "cot",
            "sec",
            "csc",
            "arcsin",
            "arccos",
            "arctan",
            "sinh",
            "cosh",
            "tanh",
            "sqrt",
            "log",
            "ln",
            "f",
            "g",
            "even",
            "odd",
        ]

        multi_ids = [
            "real",
            "bool",
            "nat",
            "int",
            "posreal",
        ]

        symbols = TexBaseLexerClass.symbols
        mixfrac = TexBaseLexerClass.mixfrac
        number = TexBaseLexerClass.number
        unit = TexBaseLexerClass.unit

        terminal_re_map = OrderedDict([
            ("FALSE","False|false|F"),
            ("TRUE","True|true|T"),
            ("FUNCTION", "|".join(functions)),
            ("ID", "|".join(multi_ids + ["[A-Za-z]"])),
            ("MIXFRAC", mixfrac),
            ("NUMBER", number),
            ("UNIT", unit)
        ])
        terminals = [ r"(?P<%s>%s)" % (s, t) for (s,t) in terminal_re_map.items() ]

        tokens = TexBaseLexerClass.tokens + ["FUNCTION"]
        longmatch_regex = re.compile("|".join(symbols + terminals))
    return TeXLexer
