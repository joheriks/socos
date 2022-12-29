# This module defines an abstract baseclass for parsers
from pc.parsing.ParseError import *
from pc.parsing.ply import yacc

from pc import config

import os

class AbstractParser(object):
    """ Abstract parser class, designed to be used with the PLY parser
    generator. Typically, a parser extending this class will only need
    to define the grammar rules and actions in methods starting with
    p_, and set the instance variables module, start, lexer and
    tabmodule.

    Other instance variables that can be set:
    method, debug, check_recursion, optimize, write_tables,
    debugfile, outputdir, pdebug

    See yacc.yacc for a description of these variables.

    Contract that must be obeyed by inheriting classes:

    Parse errors must be reported in one (or both) of the following
    ways:

    1. The parser raises ParseException
    2. The parser sets self.errors to a list of ParseErrors.

    In either case, the parse method will raise an ParseException on
    parse error. In case 2, self.errors will be the errors in the
    exception, and the exception will be raised /after/ the parsing.

    If you override the parse method you must respect this behaviour.
    """

    def __init__( self,
                  lexer, start, tabmodule,
                  debug=False, debugfile=None,
                  write_tables=False ):
        """ Default constructor. Typically, and inheriting class will
        call this method in its __init__, followed by setting the
        instance variables module,start,lexer and tabmodule to custom
        values and a call to self._yacc"""
        self.lexer = lexer
        self.tokens = None
        self.tabmodule = tabmodule
        self.start = start
        self.module = self
        self.method = yacc.default_lr
        self.debug = debug
        self.check_recursion = 1
        self.optimize = config.OPTIMIZE_PARSER
        self.write_tables = write_tables
        self.outputdir = "parsetabs"
        self.debugfile = os.path.join(self.outputdir, debugfile)
        self.errors = []
        self.parser = None
        self.pdebug = 0

    def _yacc(self):
        """ Calls yacc.yacc with arguments taken from instance
        variables."""
        self.parser = yacc.yacc(method = self.method,
                                debug = self.debug,
                                module = self.module,
                                tabmodule  = self.tabmodule,
                                start = self.start,
                                check_recursion = self.check_recursion,
                                optimize = self.optimize,
                                write_tables = self.write_tables,
                                debugfile = self.debugfile,
                                outputdir = self.outputdir)

    def parse(self, parse_string):
        """ Parses parse_string and returns a parse tree (consisting
        of AST.Nodes). Raises ParseException on parse
        errors. The instance variable pdebug can be used to control
        debug output."""
        self.errors = []
        try:
            try:
                result = self.parser.parse(input = parse_string,
                                           lexer = self.lexer,
                                           debug = self.pdebug)
                if self.errors:
                    raise ParseException([])

                return result
            except ParseException, inst:
                if self.errors:
                    inst.errors += self.errors
                raise
        finally:
            self.errors = []


    def p_error(self, t):
        #print self.parser.statestack
        #print '***'
        #print self.parser.symstack
        if t:
            err = ParseError("syntax error at '%s'"%t.value,t.lineno,t.lexpos,type='parsing')
        else:
            err = ParseError("unexpected EOF")
        raise ParseException([err])


