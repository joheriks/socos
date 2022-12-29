from pc.parsing.Token import Token
from pc.parsing.PVSLexer import PVSLexer
from pc.parsing.AST import Node, ParentNode as P, LeafNode as L
from pc.parsing.AbstractParser import AbstractParser
from pc.parsing.ParseError import ParseError, ParseException
from pc.parsing.ParserUtil import *

class UidLexer( PVSLexer ):
    '''Lexer for the IBP language, using the same interface as pc.PVSLexer'''

    # remove all reserved words
    tokens = filter(lambda x:not x.startswith("KEY_"),PVSLexer.tokens)
    
    keyword_map = {}



class UidParser(AbstractParser):
    """Parser for hierarchical element identifier. The result is a list of lists
    with elements of type uid_part."""

    def __init__( self,
                  tabmodule = 'uid_parsetab',
                  debug = False,
                  debugfile = 'uid_parser.out',
                  write_tables = False ):

        AbstractParser.__init__(self,lexer=UidLexer(),start="uids",tabmodule=tabmodule,
                                debug=debug,debugfile=debugfile,write_tables=write_tables)
        self.tokens = self.lexer.tokens
        self.module = self
        self._yacc()

     
    def p_uids( self, p ):
        """uids : uid_list
                | uid_list SEMI_COLON"""
        p[0] = p[1]

    p_uid = make_list_node("uid","uid","uid_part_list")

    p_uid_list = make_list("uid_list","uid","SEMI_COLON")


    def p_uid_part( self, p ):
        """uid_part : ID
                    | STAR
                    | ID COLON ID"""
        p[0] = P("uidpart",[L.make(p, 1)])
        if len(p)==4:
            p[0].children.append(L.make(p, 3))


    p_uid_part_list = make_list("uid_part_list","uid_part","SLASH")

