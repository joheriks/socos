from pc.parsing.TeXBaseLexer import *

import re
from array import array
from collections import OrderedDict

def GetTeXMultiLexer(decimal_sep = None, tuple_sep = None):
    TexBaseLexerClass =  GetTeXBaseLexer(decimal_sep, tuple_sep)
    class TeXLexer( TexBaseLexerClass ):
        """Tex multi lexer"""

    return TeXLexer
 
