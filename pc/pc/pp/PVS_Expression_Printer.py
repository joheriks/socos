# Module defining things that prettyprint PVS expressions

from pc.parsing.AST import Node, LeafNode, tree_to_str
from pc.parsing.ParserUtil import binops, unaryops, typeexpr
from pc.util.Utils import some, position


expr_list_map = { '1' : ('(', ')'),
                  '2' : ('(:', ':)'),
                  '3' : ('[|', '|]'),
                  '4' : ('(|', '|)'),
                  '5' : ('{|', '|}') }

term_sym_map = { 'HASH_HASH' : '##',
                 'HASH_RPAREN' : '#)',
                 'HASH_RBRACKET' : '#]',
                 'HASH' : '#',
                 'PERCENTAGE' : '%',
                 'AMPERSAND_AMPERSAND' : '&&',
                 'AMPERSAND' : '&',
                 'LPAREN_HASH' : '(#',
                 'LPAREN_COLON' : '(:',
                 'LPAREN_VBAR_VBAR_RPAREN' : '(||)',
                 'LPAREN_VBAR' : '(|',
                 'LPAREN' : '(',
                 'RPAREN' : ')',
                 'STAR_STAR' : '**',
                 'STAR' : '*',
                 'PLUS_PLUS' : '++',
                 'PLUS' : '+',
                 'COMMA' : ',',
                 'MINUS_GT' : '->',
                 'MINUS' : '-',
                 'DOT' : '.',
                 'DOT_DOT' : '..',
                 'SLASH_SLASH' : '//',
                 'SLASH_EQUAL' : '/=',
                 'SLASH_BSLASH' : '/\\',
                 'SLASH' : '/',
                 'COLON_RPAREN' : ':)',
                 'COLON_COLON' : '::',
                 'COLON_EQUAL' : ':=',
                 'COLON' : ':',
                 'SEMI_COLON' : ';',
                 'LT_LT_EQUAL' : '<<=',
                 'LT_LT' : '<<',
                 'LT_EQUAL_GT' : '<=>',
                 'LT_EQUAL' : '<=',
                 'LT_GT' : '<>',
                 'LT_VBAR' : '<|',
                 'LT' : '<',
                 'EQUAL_EQUAL' : '==',
                 'EQUAL_GT' : '=>',
                 'EQUAL' : '=',
                 'GT_EQUAL' : '>=',
                 'GT_GT_EQUAL' : '>>=',
                 'GT_GT' : '>>',
                 'GT' : '>',
                 'AT_AT' : '@@',
                 'AT' : '@',
                 'LBRACKET_HASH' : '[#',
                 'LBRACKET_RBRACKET' : '[]',
                 'LBRACKET_VBAR_VBAR_RBRACKET' : '[||]',
                 'LBRACKET_VBAR' : '[|',
                 'LBRACKET' : '[',
                 'BSLASH_SLASH' : '\\/',
                 'BSLASH' : '\\',
                 'RBRACKET_VBAR' : ']|',
                 'RBRACKET' : ']',
                 'CARET_CARET' : '^^',
                 'CARET' : '^',
                 'SINGLE_QUOTE' : '\'',
                 'LBRACE_VBAR_VBAR_RBRACE' : '{||}',
                 'LBRACE_VBAR' : '{|',
                 'LBRACE' : '{',
                 'VBAR_RPAREN' : '|)',
                 'VBAR_MINUS_GT' : '|->',
                 'VBAR_MINUS' : '|-',
                 'VBAR_EQUAL' : '|=',
                 'VBAR_GT' : '|>',
                 'VBAR_LBRACKET' : '|[',
                 'VBAR_RBRACKET' : '|]',
                 'VBAR_VBAR' : '||',
                 'VBAR_RBRACE' : '|}',
                 'VBAR' : '|',
                 'RBRACE' : '}',
                 'TILDE' : '~',
                 'EXCLAMATION_MARK' : '!',
                 'KEY_AND' : 'AND',
                 'KEY_ANDTHEN' : 'ANDTHEN',
                 'KEY_ARRAY' : 'ARRAY',
                 'KEY_ASSUMING' : 'ASSUMING',
                 'KEY_ASSUMPTION' : 'ASSUMPTION',
                 'KEY_AUTOREWRITE' : 'AUTOREWRITE',
                 'KEY_AXIOM' : 'AXIOM',
                 'KEY_BEGIN' : 'BEGIN',
                 'KEY_BUT' : 'BUT',
                 'KEY_BY' : 'BY',
                 'KEY_CASES' : 'CASES',
                 'KEY_CHALLENGE' : 'CHALLENGE',
                 'KEY_CLAIM' : 'CLAIM',
                 'KEY_CLOSURE' : 'CLOSURE',
                 'KEY_COND' : 'COND',
                 'KEY_CONJECTURE' : 'CONJECTURE',
                 'KEY_CONTAINING' : 'CONTAINING',
                 'KEY_CONVERSION' : 'CONVERSION',
                 'KEY_COROLLARY' : 'COROLLARY',
                 'KEY_DATATYPE' : 'DATATYPE',
                 'KEY_ELSE' : 'ELSE',
                 'KEY_ELSIF' : 'ELSIF',
                 'KEY_END' : 'END',
                 'KEY_ENDASSUMING' : 'ENDASSUMING',
                 'KEY_ENDCASES' : 'ENDCASES',
                 'KEY_ENDCOND' : 'ENDCOND',
                 'KEY_ENDIF' : 'ENDIF',
                 'KEY_ENDTABLE' : 'ENDTABLE',
                 'KEY_EXISTS' : 'EXISTS',
                 'KEY_EXPORTING' : 'EXPORTING',
                 'KEY_FACT' : 'FACT',
                 'KEY_FALSE' : 'FALSE',
                 'KEY_FORALL' : 'FORALL',
                 'KEY_FORMULA' : 'FORMULA',
                 'KEY_FROM' : 'FROM',
                 'KEY_FUNCTION' : 'FUNCTION',
                 'KEY_HAS_TYPE' : 'HAS_TYPE',
                 'KEY_IF' : 'IF',
                 'KEY_IFF' : 'IFF',
                 'KEY_IMPLIES' : 'IMPLIES',
                 'KEY_IMPORTING' : 'IMPORTING',
                 'KEY_IN' : 'IN',
                 'KEY_INDUCTIVE' : 'INDUCTIVE',
                 'KEY_JUDGEMENT' : 'JUDGEMENT',
                 'KEY_LAMBDA' : 'LAMBDA',
                 'KEY_LAW' : 'LAW',
                 'KEY_LEMMA' : 'LEMMA',
                 'KEY_LET' : 'LET',
                 'KEY_LIBRARY' : 'LIBRARY',
                 'KEY_MACRO' : 'MACRO',
                 'KEY_MEASURE' : 'MEASURE',
                 'KEY_NONEMPTY_TYPE' : 'NONEMPTY_TYPE',
                 'KEY_NOT' : 'NOT',
                 'KEY_O' : 'O',
                 'KEY_OBLIGATION' : 'OBLIGATION',
                 'KEY_OF' : 'OF',
                 'KEY_OR' : 'OR',
                 'KEY_ORELSE' : 'ORELSE',
                 'KEY_POSTULATE' : 'POSTULATE',
                 'KEY_PROPOSITION' : 'PROPOSITION',
                 'KEY_RECURSIVE' : 'RECURSIVE',
                 'KEY_SUBLEMMA' : 'SUBLEMMA',
                 'KEY_SUBTYPES' : 'SUBTYPES',
                 'KEY_SUBTYPES_OF' : 'SUBTYPES_OF',
                 'KEY_TABLE' : 'TABLE',
                 'KEY_THEN' : 'THEN',
                 'KEY_THEOREM' : 'THEOREM',
                 'KEY_THEORY' : 'THEORY',
                 'KEY_TRUE' : 'TRUE',
                 'KEY_TYPE' : 'TYPE',
                 'KEY_VAR' : 'VAR',
                 'KEY_WHEN' : 'WHEN',
                 'KEY_WHERE' : 'WHERE',
                 'KEY_WITH' : 'WITH',
                 'KEY_XOR' : 'XOR',
                 'KEY_AUTOREWRITEPLUS' : 'AUTOREWRITE+',
                 'KEY_AUTOREWRITEMINUS' : 'AUTOREWRITE-',
                 'KEY_CONVERSIONPLUS' : 'CONVERSION+',
                 'KEY_CONVERSIONMINUS' : 'CONVERSION-',
                 'KEY_TYPEPLUS' : 'TYPE+' }

logical_ops = () # ('AND', 'OR', 'XOR', 'IMPLIES', '=>', 'WHEN','/\\','\\/')

# This table is probably borked for things other than binops,
# so be careful about using it elsewhere.

precedence_table = (
    ('nonassoc', 'KEY_FORALL', 'KEY_EXISTS', 'KEY_LAMBDA', 'KEY_IN'),
    ('left',     'VBAR'),
    ('right',    'VBAR_MINUS', 'VBAR_EQUAL'),
    ('right',    'KEY_IFF', 'LT_EQUAL_GT'),
    ('right',    'KEY_IMPLIES', 'EQUAL_GT', 'KEY_WHEN'),
    ('right',    'KEY_OR', 'BSLASH_SLASH', 'KEY_XOR', 'KEY_ORELSE'),
    ('right',    'KEY_AND', 'AMPERSAND', 'AMPERSAND_AMPERSAND',
                 'SLASH_BSLASH', 'KEY_ANDTHEN'),
    ('left',     'KEY_NOT', 'TILDE', 'UTILDE'),
    ('left',     'EQUAL', 'SLASH_EQUAL', 'EQUAL_EQUAL',
                 'LT', 'LT_EQUAL', 'GT', 'GT_EQUAL', 'LT_LT', 'GT_GT',
                 'LT_LT_EQUAL', 'GT_GT_EQUAL', 'LT_VBAR', 'VBAR_GT'),
    ('left',     'KEY_WITH'),
    ('left',     'KEY_WHERE'),
    ('left',     'AT', 'HASH'),
    ('left',     'AT_AT', 'HASH_HASH', 'VBAR_VBAR'),
    ('left',     'PLUS', 'MINUS', 'PLUS_PLUS'),
    ('left',     'COMMA', 'SLASH', 'STAR', 'STAR_STAR', 'SLASH_SLASH'),
    ('nonassoc', 'DOT_DOT',
    ('left',     'KEY_O'),
    ('left',     'COLON', 'COLON_COLON', 'HAS_TYPE'),
    ('nonassoc', 'LBRACKET_RBRACKET', 'LT_GT'),
    ('left',     'CARET', 'CARET_CARET'),
    ('left',     'SINGLE_QUOTE')))


def is_higher_prec(a_op, b_op):
    '''
    Returns True if a_op has higher precedence than b_op, else
    return False.

    If any of the operator precedences are unknown, return False.
    '''
    a_pos = position(lambda x : a_op in x[1:], precedence_table)
    b_pos = position(lambda x : b_op in x[1:], precedence_table)

    return a_pos is not None and b_pos is not None and a_pos > b_pos


class PVS_Expression_Printer( object ):
    """ Class that pretty prints parse trees from PVSParser
    (this class is not thread-safe)"""

    def __init__(self, max_line_length = 120, upcase_keywords = True ):
        """ Constructs a new pretty printer """
        self.option_keywords_upcase = upcase_keywords
        self.option_line_length = max_line_length
        self._line_len = 0
        self._break_cols = []
        self._line_segments = []
        self._outf = None
        self._break_next = False

    def output(self, node, outf):
        """Calls outf for each new prettyprinted line, with the line
        as argument. Node is assumed to be a PVS expression
        tree. Garbage in = Garbage out"""

        try:
            self._outf = outf
            self._pp(node)
            if self._get_line_out().strip():
                self._outf(self._get_line_out())
        finally:
#            assert not self._break_cols
            self._outf = None
            self._line_len = 0
            self._line_segments = []
            self._break_cols = []
            self._break_next = False

    def output_to_string(self, node):
        """ Returns pretty printed tree as a string """
        lst = []
        self.output(node, lst.append)
        return '\n'.join(lst)

    # Implementation begins here

    # Most of this code is pretty thorny, but the basic principle is
    # to recurse down the trees, and output symbols with
    # self._out. push_stop and pop_stop are used to remember tabstops
    # to indent to after breaking a line. See the function for
    # printing ifthen expressions for an example of how to use the
    # stops functions.

    def _collect( self, str ):
        self._line_len += len(str)
        self._line_segments.append(str)

    def _get_line_out(self):
        s = ''.join(self._line_segments)
        icols = len(s) - len(s.lstrip())
        if icols:
            s = ' '*icols + s.lstrip()
        return s

    def _push_stop(self):
        if self._near_break():
            self._break_next = True

        self._break_cols.append(self._line_len)

    def _push_stop2(self, mod):
        if self._near_break():
            self._break_next = True

        self._break_cols.append(self._line_len + mod)

    def _pop_stop(self):
        self._break_cols.pop()

    def _make_stop(self):
        col = 0
        if self._break_cols:
            col = self._break_cols[-1]

        return ' ' * col

    def _flush(self, str):
        line_out = self._get_line_out()

        if line_out.strip():
            self._outf(line_out)

        newstop = self._make_stop()
        self._line_segments = [ newstop ]
        self._line_len = len(newstop)
        self._collect(str.lstrip())

    def _force_break(self, str ):
        self._flush(str)

    def _near_break(self):
        return self._line_len > self.option_line_length - 8

    hanging_syms = [ ',', ')', ']', '}', ':',
                     '#]', ':)', '|)', '|]', ']|', '||',
                     '|}', 'then', '' ]
    non_hanging_syms = [ '(', '[', '{', '[#', '(:', '(|', '[|', '{|' ]

    def _out(self, str):
        if self._break_next:
            if (str.strip() in PVS_Expression_Printer.hanging_syms
                or some(self._get_line_out().rstrip().endswith,
                        PVS_Expression_Printer.non_hanging_syms)):
                self._collect(str)
            else:
                self._flush(str)
                self._break_next = False
        elif (self._line_len + len(str) > self.option_line_length
              or (self._near_break()
                  and str.strip() in PVS_Expression_Printer.hanging_syms)):
            self._collect(str)
            self._break_next = True
        else:
            self._collect(str)

    def _out_join(self, nodes, sep):
        if nodes:
            for n in nodes[:-1]:
                self._pp(n)
                self._out(sep)
            self._pp(nodes[-1])

    def _out_join_break(self, nodes, sep):
        if nodes:
            for n in nodes[:-1]:
                self._pp(n)
                self._out(sep)
                self._force_break('')

            self._pp(nodes[-1])

    def _pp(self, node):
        assert (isinstance(node, Node))

        if isinstance(node, LeafNode):
            self._pp_terminal(node)

        elif node.type=='theory':
            self._pp_theory(node)

        elif node.type in binops or node.type in unaryops:
            self._pp_binop_unaryop(node)
        elif node.type == 'idop':
            self._pp(node.children[0])
        elif node.type == 'name':
            self._pp_name(node)
        elif node.type == 'actuals':
            self._pp_actuals(node)
        elif node.type.startswith("expression_list"):
            self._pp_expr_list(node)
        elif node.type == 'SINGLE_QUOTE':
            self._pp_single_quote(node)
        elif node.type == 'expression_arguments':
            self._pp_expression_arguments(node)
        elif node.type == 'arguments':
            self._pp_arguments(node)
        elif node.type == 'EXCLAMATION_MARK':
            self._pp_id_excl_number(node)
        elif node.type == 'ass_arg_plus':
            self._pp_ass_arg_plus(node)
        elif node.type == 'assignargs':
            self._pp_assignargs(node)
        elif node.type == 'assignment_ce':
            self._pp_assignment(node, ':=')
        elif node.type == 'assignment_vmg':
            self._pp_assignment(node, '|->')
        elif node.type == 'assignment_list':
            self._pp_assignment_list(node)
        elif node.type == 'with':
            self._pp_with(node)
        elif node.type == 'ifthen':
            self._pp_ifthen(node)
        elif node.type == 'elsif':
            self._pp_elsif(node)
        elif node.type == 'letbind':
            self._pp_letbind(node)
        elif node.type == 'letbinding':
            self._pp_letbinding(node)
        elif node.type == 'let':
            self._pp_let(node)
        elif node.type == 'where':
            self._pp_where(node)
        elif node.type == 'COLON_COLON':
            self._pp_colon_colon(node)
        elif node.type == 'typedids':
            self._pp_typedids(node)
        elif node.type == 'typedids_pre':
            self._pp_typedids_pre(node)
        elif node.type == 'binding':
            self._pp_binding(node)
        elif node.type == 'pgroup':
            self._pp_pgroup(node)
        elif node.type == 'bindings':
            self._pp_bindings(node)
        elif node.type == 'lambdabindings':
            self._pp_lambdabindings(node)
        elif node.type == 'setbindings':
            self._pp_setbindings(node)
        elif node.type == 'setbinding':
            self._pp_setbinding(node)
        elif node.type == 'bindingexpr':
            self._pp_bindingexpr(node)
        elif node.type == 'setexpr':
            self._pp_setexpr(node)
        elif node.type == 'cases':
            self._pp_cases(node)
        elif node.type == 'selection':
            self._pp_selection(node)
        elif node.type == 'cond':
            self._pp_cond(node)
        elif node.type == 'MINUS_GT':
            self._pp_minus_gt(node)
        elif node.type == 'enumerationtype':
            self._pp_enumeration_type(node)
        elif node.type == 'subtype':
            self._pp_subtype(node)
        elif node.type == 'typeapplication':
            self._pp_typeapplication(node)
        elif node.type == 'idoptypeexpr':
            self._pp_idoptypeexpr(node)
        elif node.type == 'functiontype':
            self._pp_functiontype(node)
        elif node.type == 'tupletype':
            self._pp_tupletype(node)
        elif node.type == 'fielddecls':
            self._pp_fielddecls(node)
        elif node.type == 'recordtype':
            self._pp_recordtype(node)
        elif node.type == 'tableexpr':
            self._pp_table(node)
        elif node.type == 'tableentries':
            self._pp_tableentries(node)
        elif node.type == 'tableentry':
            self._pp_tableentry(node)
        elif node.type == 'colheading':
            self._pp_colheading(node)
        else:
            assert False,"Unknown node type for node %s" % node

    def _pp_terminal(self, node):
        if node.type=='STRING':
            r = '"' + node.value.replace('\\','\\\\').replace('"','\\"') + '"'
        elif node.type in term_sym_map:
            r = term_sym_map[node.type]
            if node.type.startswith('KEY_'):
                if self.option_keywords_upcase: r = r.upper()
                else: r = r.lower()
        else:
            r = node.value
        self._out(r)

    def _pp_binop_unaryop(self, node):
        num_children = len(node.children)

        opsym = term_sym_map[node.type]
        if not self.option_keywords_upcase:
            opsym = opsym.lower()

        if (num_children == 1):
            self._out(opsym)
            if node.type in ('KEY_NOT', 'TILDE'):
                self._out(' ')
            self._pp(node.children[0])
        else:
            left = node.children[0]
            right = node.children[1]

            if (left.type == 'pgroup'
                and is_higher_prec(left.children[0].type,node.type)
                and not term_sym_map[left.children[0].type] in logical_ops):
                left = left.children[0]

            if (right.type == 'pgroup'
                and is_higher_prec(right.children[0].type,node.type)
                and not term_sym_map[right.children[0].type] in logical_ops):
                right = right.children[0]

            self._pp(left)
            if term_sym_map[node.type] in logical_ops:
                self._force_break(" %s " % opsym)
            else:
                self._out(" %s " % opsym)
            self._pp(right)

    def _pp_expr_list(self, node):
        if node.children:
            num_children = len(node.children)
        else:
            num_children = 0

        parens = expr_list_map[node.type[-1:]]

        if (num_children == 0):
            self._out("%s %s" % (parens[0], parens[1]))
        else:
            self._out(parens[0])
            self._push_stop()
            self._out_join(node.children, ', ')
            self._out(parens[1])
            self._pop_stop()

    def _pp_name(self, node):
        # this method is more than a bit messy
        num_children = len(node.children)

        id_child = node.children[0]
        self._pp(id_child)

        if (num_children == 1):
            pass
        elif (num_children == 2):
            if id_child.type == 'ID' and node.children[1].type == 'idop':
                self._out('@')
                self._pp(node.children[1])
            elif id_child.type == 'idop' and node.children[1].type == 'idop':
                self._out('.')
                self._pp(node.children[1])
            elif id_child.type == 'idop' and node.children[1].type == 'actuals':
                self._pp(node.children[1])
            else:
                print "Unknown name node %s" % (tree_to_str(node))
                assert False
        elif (num_children == 3):
            if (id_child.type == 'idop'
                and node.children[1].type == 'actuals'
                and node.children[2].type == 'idop'):

                self._pp(node.children[1])
                self._out('.')
                self._pp(node.children[2])
            elif id_child.type == 'ID' and node.children[1].type == 'idop':
                if node.children[2].type == 'idop':
                    self._out('@')
                    self._pp(node.children[1])
                    self._out('.')
                    self._pp(node.children[2])
                elif node.children[2].type == 'actuals':
                    self._out('@')
                    self._pp(node.children[1])
                    self._pp(node.children[2])
                else:
                    print "Unknown name node %s" % (tree_to_str(node))
                    assert False
        elif (num_children == 4):
            self._out('@')
            self._pp(node.children[1])
            self._pp(node.children[2])
            self._out('.')
            self._pp(node.children[3])
        else:
            print "Unknown name node %s" % (tree_to_str(node))
            assert False

    def _pp_actuals(self, node):
        self._out('[')
        self._push_stop()
        self._out_join(node.children, ', ')
        self._pop_stop()
        self._out(']')

    def _pp_single_quote(self, node):
        if (len(node.children) == 2):
            self._out_join(node.children, "`")
        else:
            self._out("`")
            self._pp(node.children[0])

    def _pp_expression_arguments(self, node):
        self._pp(node.children[0])
        self._pp(node.children[1])

    def _pp_arguments(self, node):
        self._out('(')
        self._push_stop()
        self._out_join(node.children, ', ')
        self._pop_stop()
        self._out(')')

    def _pp_id_excl_number(self, node):
        self._pp(node.children[0])
        self._out("!")
        if (len(node.children) == 2):
            self._pp(node.children[1])

    def _pp_ass_arg_plus(self, node):
        self._out_join(node.children, ' ')

    def _pp_assignargs(self, node):
        self._out_join(node.children, ' ')

    def _pp_assignment(self, node, sym):
        self._out_join(node.children, ' %s ' % (sym,))

    def _pp_assignment_list(self, node):
        self._out('(#')
        self._push_stop()
        self._out_join(node.children, ', ')
        self._pop_stop()
        self._out('#)')

    def _pp_with(self, node):
        self._pp(node.children[0])
        self._push_stop()
        self._out(self._fmt_key(' with '))
        self._push_stop()
        self._out('[')
        self._push_stop()
        self._out_join(node.children[1:], ', ')
        self._pop_stop()
        self._out(']')
        self._pop_stop()
        self._pop_stop()

    def _pp_ifthen(self, node):
        self._push_stop()
        self._out(self._fmt_key('if '))
        self._push_stop()

        self._pp(node.children[0])
        self._out(self._fmt_key(' then '))
        self._force_break('')
        self._pp(node.children[1])
        self._pop_stop()

        # pp all elsif branches
        self._force_break('')
        if node.children[2:-1]:
            self._out_join_break(node.children[2:-1],' ')

        self._force_break(self._fmt_key(' else '))
        self._push_stop2(-2)
        self._force_break('')
        self._pp(node.children[-1])
        self._pop_stop()
        self._force_break(self._fmt_key(' endif'))
        self._pop_stop()

    def _pp_elsif(self,node):
        self._push_stop()
        self._out(self._fmt_key('elsif '))
        self._push_stop()
        self._pp(node.children[0])
        self._out(self._fmt_key(' then '))
        self._force_break('')
        self._pp(node.children[1])
        self._pop_stop()
        self._pop_stop()


    def _pp_letbind(self, node):
        self._pp(node.children[0])
        bind_list = node.children[1:-1]

        bind_str = False
        if bind_list:
            bind_str = True

        type_str = False
        if len(node.children) > 1:
            if node.children[-1].type in typeexpr:
                type_str = True
            else:
                bind_str = True

        if type_str and bind_str:
            self._out(' ')
            self._out_join(node.children[1:-1], ' ')
            self._out(' : ')
            self._pp(node.children[-1])
        elif type_str:
            self._out(' : ')
            self._pp(node.children[-1])
        elif bind_str:
            self._out(' ')
            self._out_join(node.children[1:], ' ')

    def _pp_letbinding(self, node):
        if len(node.children) == 2:
            self._pp(node.children[0])
            self._out(' = ')
            if (node.children[1].type == 'expression_list_1'
                and len(node.children[1].children) == 1):
                # Skip extra parens
                self._pp(node.children[1].children[0])
            else:
                self._pp(node.children[1])
        else:
            self._out('(')
            self._out_join(node.children[:-1], ', ')
            self._out(')')
            self._out(' = ')
            self._pp(node.children[-1])

    def _pp_let(self, node):
        self._out('(')
        self._out(self._fmt_key('let '))
        self._push_stop()
        self._out_join_break(node.children[:-1], ', ')
        self._force_break(self._fmt_key(' in '))
        self._push_stop()
        self._pp(node.children[-1])
        self._out(')')
        self._pop_stop()
        self._pop_stop()

    def _pp_where(self, node):
        self._pp(node.children[0])
        self._force_break(self._fmt_key(' where '))
        self._push_stop()
        self._out_join(node.children[1:], ', ')
        self._pop_stop()

    def _pp_colon_colon(self, node):
        self._out_join(node.children, '::')

    def _pp_typedids(self, node):
        self._out_join(node.children, ' | ')

    def _pp_typedids_pre(self, node):
        if len(node.children) == 1:
            self._pp(node.children[0])
        elif node.children[-1].type in typeexpr:
            self._out_join(node.children[:-1], ', ')
            self._out(' : ')
            self._pp(node.children[-1])
        else:
            self._out_join(node.children, ', ')

    def _pp_binding(self, node):
        self._pp(node.children[0])

    def _pp_pgroup(self, node):
        self._out('(')
        self._push_stop()
        self._pp(node.children[0])
        self._pop_stop()
        self._out(')')

    def _pp_bindings(self, node):
        self._out('(')
        self._push_stop()
        self._out_join(node.children, ', ')
        self._pop_stop()
        self._out(')')

    def _pp_lambdabindings(self, node):
        self._out_join(node.children, ', ')

    def _pp_setbindings(self, node):
        self._out_join(node.children, ', ')

    def _pp_setbinding(self, node):
        self._out_join(node.children, ' : ')

    def _pp_bindingexpr(self, node):
        self._push_stop2(2)
        self._out('(')
        self._pp(node.children[0])
        self._out(' ')
        self._out_join(node.children[1:-1],' ')
        self._out(' : ')
        #self._force_break('')
        self._pp(node.children[-1])
        self._out(')')
        self._pop_stop()

    def _pp_setexpr(self, node):
        self._out('{ ')
        self._push_stop()
        self._pp(node.children[0])
        self._out(' | ')
        self._push_stop()
        self._pp(node.children[1])
        self._pop_stop()
        self._pop_stop()
        self._out(' }')

    def _pp_cases(self, node):
        case_sym = self._fmt_key('cases ')
        of_sym = self._fmt_key(' of ')
        endcases_sym = self._fmt_key(' endcases')
        else_sym = self._fmt_key(' else ')

        if not node.children[-1].type == 'selection':
            self._out(case_sym)
            self._push_stop()
            self._pp(node.children[0])
            self._out(of_sym)
            self._force_break('')
            self._push_stop()
            self._out_join_break(node.children[1:-1], ', ')
            self._force_break(else_sym)
            self._push_stop()
            self._pp(node.children[-1])
            self._pop_stop()
            self._pop_stop()
            self._pop_stop()
            self._force_break(endcases_sym)
        else:
            self._out(case_sym)
            self._push_stop()
            self._pp(node.children[0])
            self._out(of_sym)
            self._force_break('')
            self._push_stop()
            self._out_join_break(node.children[1:], ', ')
            self._pop_stop()
            self._pop_stop()
            self._force_break(endcases_sym)

    def _pp_selection(self, node):
        if len(node.children) > 2:
            self._pp(node.children[0])
            self._out(' (')
            self._push_stop()
            self._out_join(node.children[1:-1], ', ')
            self._pop_stop()
            self._out(') : ')
            self._push_stop()
            self._pp(node.children[-1])
            self._pop_stop()
        else:
            self._pp(node.children[0])
            self._out(' : ')
            self._push_stop()
            self._pp(node.children[1])
            self._pop_stop()

    def _pp_cond(self, node):
        cond_sym = self._fmt_key('cond ')
        endcond_sym = self._fmt_key(' endcond')
        else_sym = self._fmt_key(', else -> ')

        if not node.children[-1].type == 'MINUS_GT':
            self._push_stop()
            self._out(cond_sym)
            self._push_stop()
            self._out_join(node.children[0:-1], ', ')
            self._out(else_sym)
            self._push_stop()
            self._pp(node.children[-1])
            self._pop_stop()
            self._pop_stop()
            self._force_break(endcond_sym)
            self._pop_stop()
        else:
            self._push_stop()
            self._out(cond_sym)
            self._push_stop()
            self._out_join(node.children[0:], ', ')
            self._pop_stop()
            self._force_break(endcond_sym)
            self._pop_stop()

    def _pp_minus_gt(self, node):
        self._pp(node.children[0])
        self._out(' -> ')
        self._push_stop()
        self._pp(node.children[1])
        self._pop_stop()

    def _pp_enumeration_type(self, node):
        self._push_stop()
        self._out('{ ')
        self._push_stop()
        self._out_join(node.children, ', ')
        self._pop_stop()
        self._out(' }')
        self._pop_stop()

    def _pp_subtype(self, node):
        self._pp(node.children[0])

    def _pp_typeapplication(self, node):
        self._pp(node.children[0])
        self._pp(node.children[1])

    def _pp_idoptypeexpr(self, node):
        self._out_join(node.children, ' : ')

    def _pp_functiontype(self, node):
        if isinstance(node.children[0], LeafNode):
            self._out(self._fmt_key(term_sym_map[node.children[0].type]))
            self._out('[')
            self._push_stop()
            self._out_join(node.children[1:-1], ', ')
            self._out(' -> ')
            self._push_stop()
            self._pp(node.children[-1])
            self._pop_stop()
            self._pop_stop()
            self._out(']')
        else:
            self._out('[')
            self._push_stop()
            self._out_join(node.children[:-1], ', ')
            self._out(' -> ')
            self._push_stop()
            self._pp(node.children[-1])
            self._pop_stop()
            self._pop_stop()
            self._out(']')

    def _pp_tupletype(self, node):
        self._push_stop()
        self._out('[')
        self._push_stop()
        self._out_join(node.children, ', ')
        self._pop_stop()
        self._out(']')
        self._pop_stop()

    def _pp_fielddecls(self, node):
        self._out_join(node.children[:-1], ', ')
        self._out(' : ')
        self._push_stop()
        self._pp(node.children[-1])
        self._pop_stop()

    def _pp_recordtype(self, node):
        self._push_stop()
        self._out('[# ')
        self._push_stop()
        self._out_join(node.children, ', ')
        self._pop_stop()
        self._out(' #]')
        self._pop_stop()

    def _fmt_key(self, k):
        if self.option_keywords_upcase:
            return k.upper()
        else:
            return k.lower()

    def _pp_table(self, node):
        table_entries = node.children[-1]
        exprs = None
        colhead = node.children[:-1]

        if colhead:
            if colhead[-1].type == 'colheading':
                exprs = node.children[:-2]
                colhead = colhead[-1]
            else:
                exprs = colhead
                colhead = None

        self._force_break(self._fmt_key('table '))
        self._push_stop()

        if exprs:
            self._out_join(exprs, ', ')
        if colhead:
            self._pp(colhead)

        self._pp(table_entries)

        self._pop_stop()
        self._force_break(self._fmt_key(' endtable'))

    def _pp_tableentries(self, node):
        map(self._pp, node.children)

    def _pp_tableentry(self, node):
        self._out('| ')
        self._out_join(node.children, ' | ')
        self._out(' |')
        self._out(' || ')
        self._force_break('')

    def _pp_colheading(self, node):
        self._out('|[ ')

        self._pp(node.children[0])

        if len(node.children) > 1:
            self._out(' | ')
            self._out_join(node.children[1:], ' | ')
            self._out(' |')

        self._out(' ]| ')
        self._force_break('')



def expr_to_string( expr ):
    return PVS_Expression_Printer().output_to_string(expr)
