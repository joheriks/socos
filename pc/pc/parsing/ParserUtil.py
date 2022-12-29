import re, os

from pc.parsing.AST import *


binops = [ 'KEY_O', 'KEY_IFF', 'LT_EQUAL_GT',
           'KEY_IMPLIES', 'EQUAL_GT', 'KEY_WHEN',
           'KEY_OR', 'BSLASH_SLASH', 'KEY_AND',
           'SLASH_BSLASH', 'AMPERSAND', 'KEY_XOR',
           'KEY_ANDTHEN', 'KEY_ORELSE', 'CARET',
           'PLUS', 'MINUS', 'STAR', 'SLASH',
           'PLUS_PLUS', 'TILDE', 'STAR_STAR',
           'SLASH_SLASH', 'CARET_CARET', 'VBAR_MINUS',
           'VBAR_EQUAL', 'LT_VBAR', 'VBAR_GT',
           'EQUAL', 'SLASH_EQUAL', 'EQUAL_EQUAL',
           'LT', 'LT_EQUAL', 'GT', 'GT_EQUAL', 'LT_LT',
           'GT_GT', 'LT_LT_EQUAL',
           'GT_GT_EQUAL', 'HASH', 'AT_AT', 'HASH_HASH' ]

unaryops = [ 'KEY_NOT', 'TILDE', 'LBRACKET_RBRACKET', 'LT_GT', 'MINUS' ]

typeexpr = [ 'name', 'subtype', 'enumerationtype', 'typeapplication',
             'functiontype', 'tupletype', 'recordtype' ]

# "actual: name | subtype" not needed since that is included in expression
actual = [ 'expression' ] + typeexpr[2:]

# Whitespace-splitter regexp
ws_re = re.compile(r'\s+')



def pgeti( p, type, n=0 ):
    '''Returns the index of the n:th production of "type"'''
    for i in range(len(p.slice)):
        if p.slice[i].type==type:
            if n>0: n = n-1
            else: return i
    return -1


def phas( p, type, n=0 ):
    '''Returns True if p has the production "type", otherwise False'''
    return pgeti(p,type,n)>=0


def pget( p, type, n=0 ):
    '''Returns the n:th production of "type"'''
    i = pgeti(p,type,n)
    if i==-1:
        raise ValueError('Not found: %s(%d)' % (type,n))
    else:
        return p[i]

def pcan( p, typ ):
    '''Returns the concatenation of all productions "type" (which must return list)'''
    return mapcan(lambda i: p[i] if p.slice[i].type==typ else [],
                  range(len(p.slice)))


def expandGrammar(p_func, **kwargs):
    '''Expands a ply grammar docstring for the given function, replacing any
    $id elements with each element from the list with the same name given as
    a keyword argument.

    Example:
    def p_expr(p):
        """expr : expr
                | expr $op expr"""
        pass

    expandGrammar(p_expr, op=[ '+', '-' ])
    '''
    # Repeat expansion until no more $id tokens found
    lastdoc = None
    while p_func.__doc__ != lastdoc:
        lastdoc = p_func.__doc__
        grammars = []
        production = ''
        for line in lastdoc.split('\n'):
            line = line.strip()
            # Ignore empty lines
            if line:
                parts = ws_re.split(line)
                # Replace any $id with value of id from kwargs;
                # kwargs[id] should be a caller-provided list
                for i, part in enumerate(parts):
                    if part.startswith('$'):
                        parts[i] = kwargs[ part[1:] ]
                # Check type of line
                if len(parts) >= 2 and parts[1] == ':':
                    # first line of a production
                    production = parts[0]
                    grammars.append(_makeGrammar(production, parts[2:], first=True))
                elif parts and parts[0] == '|':
                    # production alternative
                    grammars.append(_makeGrammar(production, parts[1:], first=False))
                else:
                    # Neither of the above nor an empty line - not good
                    raise ValueError('Grammar line "%s" not understood.' % line)
        p_func.__doc__ = '\n'.join(grammars)


def _makeGrammar(production, parts, first):
    '''Accepts a production name, a list containing strings and lists which
    will be used to build the grammar lines and a bool saying if this is the
    first rule in the production or not. Returns a grammar string with the
    given production (if first) and the combinations of all the strings with
    each of the list elements.'''
    # Figure out properly indented linestart for production alternatives.
    linestart = ' ' * len(production) + ' | '
    # Determine start of first line
    if first:
        start = production + ' : '
    else:
        start = linestart
    return start + ('\n' + linestart).join(_iterateGrammar(*parts))


def _iterateGrammar(*args):
    '''Accepts any combination of strings and lists as arguments. Returns a
    list of all the combinations of all the strings with each of the list
    elements.'''
    rows = []
    for i, arg in enumerate(args):
        if hasattr(arg, '__iter__'):
            # We found an iterable argument. Replace this argument with each
            # of its elements and recursively call us.
            rows = []
            newargs = list(args)
            for item in arg:
                newargs[i] = item
                rows += _iterateGrammar(*newargs)
            return rows
    # Recursion base case: no iterable arguments.
    # Simply return a one-element list with the joined but space-separated
    # string(s).
    return [ ' '.join(args) ]


def grammar_from_class(clazz):
    ''' Returns production rules as a string from functions with name p_ in clazz '''
    return '\n\n'.join(filter(None,
                              (map(lambda y : getattr(clazz, y).__doc__,
                                   filter(lambda z : z.startswith('p_'), dir(clazz))))))



def make_list( prodname, elementname, separator=None ):
    '''Returns a list production method, with optional separator'''
    if separator:
        def m(s,p): p[0] = (p[1]+[p[3]]) if len(p)==4 else [p[1]]
        m.__doc__ = "%s : %s \n | %s %s %s"%(prodname,elementname,prodname,separator,elementname)
    else:
        def m(s,p): p[0] = (p[1]+[p[2]]) if len(p)==3 else [p[1]]
        m.__doc__ = "%s : %s \n | %s %s"%(prodname,elementname,prodname,elementname)
    m.func_name = "p_"+prodname
    return m


def make_list_node( prodname, nodetype, listname ):
    def m(s,p): p[0] = ParentNode(nodetype,p[1])
    m.__doc__ = "%s : %s"%(prodname,listname)
    m.func_name = "p_"+prodname
    return m


def make_semicolon_terminated(func):
    '''Returns a semicolon terminated production function of the given
    production function. Works only for rules with no alternatives
    (ie no '|')'''

    def m(self, p): func(self, p)
    m.__doc__='%s'%(func.__doc__+' SEMI_COLON')
    m.func_name=func.func_name+'_semicolon'

    return m
