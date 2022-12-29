'''
A module containing classes for performing tree/term rewriting according to
defined TRS (Tree Rewrite System) rules.
'''

import re, sys
from pc.parsing.ply import yacc, lex
from collections import deque
from pc.util.Utils import some, every, mapcan, remove_duplicates
from pc.parsing.AST import LeafNode, ParentNode, tree_to_str


class RuleParseException(Exception):
    '''
    This exception is thrown when the rule parse encounters a syntax error or
    semantical error in a rule definition.
    '''
    def __init__(self, msg, lineno=-1, col=-1):
        Exception.__init__(self, msg)
        self.lineno = lineno
        self.col = col

    def __str__(self):
        lst = [ Exception.__str__(self) ]
        if self.lineno >= 0:
            lst.append(', line %s' % self.lineno)
        if self.col >= 0:
            lst.append(', col %s' % self.col)
        return ''.join(lst)



class RuleParser(object):
    '''
    A class for parsing TRS rules.
    '''

    tokens   = [ 'ID', 'LPAREN', 'RPAREN', 'COMMA', 'ARROW', 'COLON', 'STAR',
                 'QMARK', 'EMARK', 'STRING', 'EQUAL', 'PLUS', 'DOT', 'DOLLAR',
                 'GT', 'AMPERSAND', 'AT' ]

    precedence = [ ('right', 'GT') ]

    def __init__(self):
        self._lexer = lex.lex(module=self, lextab='trs_lextab')
        self._parser = yacc.yacc(
            module=self, tabmodule='trs_parsetab', start='rule_set')
        self.column = 0

    def parse(self, rule_string):
        '''
        Parses the TRS rules in rule_string and returns a list of Rule
        instances.
        '''
        self.column = 0
        self._lexer.lineno = 1
        return self._parser.parse(rule_string, lexer=self._lexer)


    ############################################################################
    # Yacc tokenizing rules

    # Characters ignored by tokenizer
    t_ignore = ' \t'

    # Skip comments
    t_ignore_COMMENT = r'\#.*'

    t_ID        = r'[a-zA-Z_][a-zA-Z_0-9]*'
    t_LPAREN    = r'\('
    t_RPAREN    = r'\)'
    t_COMMA     = r','
    t_ARROW     = r'->'
    t_STAR      = r'\*'
    t_QMARK     = r'\?'
    t_COLON     = r':'
    t_STRING    = r"'[^']*'"
    t_EQUAL     = r'='
    t_EMARK     = r'!'
    t_PLUS      = r'\+'
    t_DOT       = r'\.'
    t_DOLLAR    = r'\$'
    t_GT        = r'>'
    t_AMPERSAND = r'&'
    t_AT        = r'\@'

    def t_error(self, token):
        '''PLY error token rule; called when illegal char found.'''
        print "Warning: ignoring illegal character '%s'" % token.value[0]
        token.lexer.skip(1)


    def t_newline(self, token):
        r'\n'
        token.lexer.lineno += len(token.value)
        self.column = token.lexer.lexpos

    ############################################################################
    # Yacc grammar rules


    def p_rule_set( self, p ):
        '''rule_set : rule_entry
                    | rule_set rule_entry'''
        if len(p)==2:
            p[0] = [ p[1] ]
        elif len(p)==3:
            p[0] = p[1] + [p[2]]
        

    def p_rule_entry( self, p ):
        '''rule_entry : ID COLON COLON rule_match COLON COLON
                      | ID COLON COLON rule_match ARROW rule_replace COLON COLON'''
        if len(p) == 7:
            p[0] = Rule( p[1], p[4], [] )
        else:
            p[0] = Rule( p[1], p[4], p[6] )
        

    ############################################################################
    # Left side of rule

    def p_rule_match_x(self, p):
        '''rule_match : rule_match GT rule_match'''

        p[1].descend = p[3]
        p[0] = p[1]


    def p_rule_match_a(self, p):
        '''rule_match : node_match_pre'''

        p[0] = p[1]


    def p_rule_match_b(self, p):
        '''rule_match : node_match_pre lnode_match_spec'''

        p[0] = p[1]
        p[0].value = p[2]


    def p_rule_match_c(self, p):
        '''rule_match : node_match_pre pnode_match_spec'''

        p[0] = p[1]
        p[0].children = p[2]


    def p_lnode_match_spec(self, p):
        '''lnode_match_spec : EQUAL pattern'''

        p[0] = p[2]


    def p_pnode_match_spec(self, p):
        '''pnode_match_spec : LPAREN                  RPAREN
                            | LPAREN child_match_list RPAREN'''

        if len(p) == 4:
            p[0] = p[2]
        else:
            p[0] = []


    def p_node_match_pre_a(self, p):
        '''node_match_pre : ID               repeat
                          | ID COLON pattern repeat
                          |    COLON pattern repeat'''

        if len(p) == 3:
            p[0] = NodeMatchSpec(p[1], repeat=p[2])
        elif len(p) == 4:
            p[0] = NodeMatchSpec(None, pattern=p[2], repeat=p[3])
        else:
            assert len(p) == 5
            p[0] = NodeMatchSpec(p[1], pattern=p[3], repeat=p[4])


    def p_node_match_pre_b(self, p):
        '''node_match_pre : AMPERSAND ID'''

        match = p[2].lower()
        pattern = '.*'
        binding = match

        if match == 'any':
            repeat = None
        elif match == 'rest':
            repeat = '*'
        else:
            raise RuleParseException('Invalid predefined match "%s"' % p[2],
                                     p.lineno(2),
                                     p.lexpos(2))

        p[0] = NodeMatchSpec(binding, pattern=pattern, repeat=repeat)


    def p_child_match_list(self, p):
        '''child_match_list : rule_match
                            | child_match_list COMMA rule_match'''

        if len(p) == 2:
            p[0] = [ p[1] ]
        else:
            p[0] = p[1]
            p[0].append(p[3])


    def p_repeat(self, p):
        '''repeat :
                  | STAR
                  | QMARK
                  | EMARK
                  | PLUS'''

        if len(p) == 2:
            p[0] = p[1]
        else:
            p[0] = None


    def p_pattern(self, p):
        '''pattern : ID
                   | STRING'''

        if p.slice[1].type == 'ID':
            p[0] = p[1]
        else:
            # Strip quotes
            p[0] = p[1][1:-1]


    ############################################################################
    # Right side of rule

    def p_rule_replace(self, p):
        '''rule_replace : binding_path COLON EQUAL node_spec_list
                        | rule_replace binding_path COLON EQUAL node_spec_list'''

        if len(p) == 6:
            p[0] = p[1] + [ (p[2], p[5]) ]
        else:
            assert len(p) == 5
            p[0] = [ (p[1], p[4]) ]


    def p_binding_path(self, p):
        '''binding_path : ID
                        | binding_path DOT ID'''

        if len(p) == 2:
            p[0] = p[1]
        else:
            p[0] = p[1] + '.' + p[3]


    def p_node_spec(self, p):
        '''node_spec : binding_path
                     | pnode_spec
                     | lnode_spec
                     | macro_spec'''

        if isinstance(p[1], NodeReplaceSpec):
            p[0] = p[1]
        else:
            p[0] = NodeReplaceSpec(binding_path=p[1])


    def p_node_spec_list(self, p):
        '''node_spec_list : node_spec
                          | node_spec_list COMMA node_spec'''

        if len(p) == 2:
            p[0] = [ p[1] ]
        else:
            assert len(p) == 4
            p[0] = p[1]
            p[0].append(p[3])


    def p_macro_spec(self, p):
        '''macro_spec : AT ID LPAREN           RPAREN
                      | AT ID LPAREN node_list RPAREN'''
        if len(p) == 5:
            children = []
        else:
            assert len(p) == 6
            children = p[4]

        p[0] = NodeReplaceSpec(binding_path=p[2], children=children,
                               is_macro=True)


    def p_lnode_spec(self, p):
        '''lnode_spec : string_source EQUAL string_source'''

        p[0] = NodeReplaceSpec(type=p[1], value=p[3])


    def p_pnode_spec_new(self, p):
        '''pnode_spec : string_source LPAREN           RPAREN
                      | string_source LPAREN node_list RPAREN'''

        if len(p) == 4:
            children = []
        else:
            assert len(p) == 5
            children = p[3]

        p[0] = NodeReplaceSpec(type=p[1], children=children)


    def p_pnode_spec_old(self, p):
        '''pnode_spec : binding_path LPAREN           RPAREN
                      | binding_path LPAREN node_list RPAREN'''

        if len(p) == 4:
            children = []
        else:
            assert len(p) == 5
            children = p[3]

        p[0] = NodeReplaceSpec(binding_path=p[1], children=children)


    def p_node_list(self, p):
        '''node_list : node_spec
                     | node_list COMMA node_spec'''

        if len(p) == 2:
            p[0] = [ p[1] ]
        else:
            assert len(p) == 4
            p[0] = p[1]
            p[0].append(p[3])


    def p_string_source_a(self, p):
        '''string_source : STRING'''

        p[0] = StringSourcePlain(p[1])


    def p_string_source_b(self, p):
        '''string_source : DOLLAR DOLLAR binding_path
                         | DOLLAR ID LPAREN                    RPAREN
                         | DOLLAR ID LPAREN string_source_list RPAREN'''

        if len(p) == 4:
            p[0] = StringSourceBinding(p[3])
        else:
            assert 5 <= len(p) <= 6
            if p[2].lower() in StringSourceGenerator.GENERATORS:
                p[0] = StringSourceGenerator(p[2])
            else:
                raise RuleParseException('Invalid string generator "%s"' % p[2],
                                         p.lineno(2),
                                         p.lexpos(2))
            if len(p) == 6:
                p[0].parameters = p[4]


    def p_binding_path_list(self, p):
        '''string_source_list : string_source
                              | string_source_list COMMA string_source'''

        if len(p) == 2:
            p[0] = [ p[1] ]
        else:
            assert len(p) == 4
            p[0] = p[1]
            p[0].append(p[3])


    def p_error(self, p):
        '''Ply error handler.'''
        if p:
            raise RuleParseException('Syntax error at "%s"' % p.value,
                                     p.lineno,
                                     p.lexpos - self.column)
        else:
            raise RuleParseException('Unexpected end of input')



def _expand_path(path, possible_bindings):
    '''
    Returns a fully qualified (fully expanded) pathname for path.
    Raises RuleParseException if path refers to ambiguous or
    non-existent bindings
    '''
    if path and not path in possible_bindings:
        repl = filter(lambda x : x.endswith(path), possible_bindings)

        if len(remove_duplicates(repl)) > 1:
            raise RuleParseException('Ambiguous bindings for path %s' % (path,))

        if not repl:
            raise RuleParseException('Binding "%s" used but not available'
                                     % path)

        path = repl[0]

    return path



class NodeMatchSpec(object):
    '''
    A specification describing a node or tree to match.
    '''

    # <repeat key>: <min repeat>, <max_repeat> )
    # If <max repeat> is < 0, it is considered unlimited.
    REPEAT = { '!': (0, 0), None: (1, 1), '+': (1, -1),
               '?': (0, 1), '*':(0, -1) }
    MATCH_ORDER = ( '!', None, '+', '?', '*' )


    def __init__(self, binding, pattern=None, repeat=None,
                 value=None, children=None, descend=None):
        '''
        Constructor. Parameters:
            binding:  The identifier to bind this spec to when matched. If no
                      pattern is given, this is also used as the pattern.
            pattern:  Node type pattern. Only nodes with types that match this
                      pattern can match this spec. Pattern should be a regex
                      string.
            repeat:   The type of repetition allowed when matching this spec.
            value:    Node value to check against.
            children: A list of NodeSpec instances that children should match.
            descend:  If True, child matches may appear on any level below
                      the current level in the tree. Default is False, which
                      means that child matches must appear directly below
                      current match.
        '''
        self._pattern = None
        self._pattern_re = None
        self._binding = None
        self._repeat = None
        self._value = None
        self._value_re = None
        self._children = None

        self._matchables = None
        self._is_binding_pattern = False

        self.binding = binding
        self.pattern = pattern
        self.repeat = repeat
        self.value = value
        self.children = children
        self.descend = descend


    def bind_node(self, bindings, parent):
        '''
        Tries to match this spec against the children of parent.
          Parameters:
          bindings: A Bindings instance.
          parent:   The node whose children will be matched and bound.
          Returns:  A tuple containing (<success>, <num_matches>). Success is
                    True if the spec matches, False if not. Num_matches
                    contains the number of times the spec match. Even if success
                    is True this can be zero, in the case of optional matches.
                    Num_matches will be -1 (and success False) if a negative
                    match is found.
        '''
        children = parent.children[:]
        if not bindings:
            bindings = Bindings()

        success, num_matches = self._bind_node(bindings, parent, children)
        success = success and every(lambda x : x is None, children)

        return (success, num_matches)


    def _bind_node(self, bindings, parent, children):
        '''
        Tries to match this spec against the children of parent.
          Parameters:
          bindings: A Bindings instance.
          parent:   The node whose children will be matched and bound.
          children: Should be a COPY of the children list of parent
                    and caller is then responsible for checking if there are
                    children left that are not None.
          Returns:  A tuple containing (<success>, <num_matches>). Success is
                    True if the spec matches, False if not. Num_matches
                    contains the number of times the spec match. Even if success
                    is True this can be zero, in the case of optional matches.
                    Num_matches will be -1 (and success False) if a negative
                    match is found.
        '''
        binding = self._binding
        repeat = self._repeat

        num_matches = 0
        pushed_path = False
        marker = None

        # Get repeat limits for this match
        rmin, rmax = NodeMatchSpec.REPEAT[repeat]

        # Try to match us against valid nodes in children
        for i, child in enumerate(children):

            # Match main rule; first match node type
            if child and self._pattern_re.match(child.type):

                if binding and not pushed_path:
                    bindings.push_path(binding)
                    pushed_path = True

                loop_marker = bindings.marker

                # Note: using the internal vars here as an optimization to
                # avoid function calls.
                if self._children is not None:
                    have_match = self._match_children(bindings, child)
                elif self._value_re:
                    have_match = self._value_re.match(child.value) \
                                 is not None
                else:
                    # Type match was enough.
                    have_match = True

                # Match descending if main rule matched.
                if have_match and self.descend:
                    have_match = self._bind_descend(bindings, child)

                # Update stats on successful match.
                if have_match:
                    num_matches += 1

                    # Deleting while enumerating does not work so just
                    # replace with None.
                    children[i] = None

                    # Add binding if requested.
                    if binding:
                        if marker is None:
                            marker = bindings.marker
                        bindings.add(( parent, child ))

                    # Stop if we have enough matches. Note that num_matches
                    # must be >= 1, but the line above guarantees that.
                    if rmax >= 0 and num_matches >= rmax:
                        break

                else:
                    bindings.marker = loop_marker

        res = True

        # Check that we got enough results for the repeat type.
        if num_matches < rmin:
            res = False

        # Check for too many matches (meaning an inverted match).
        elif rmax >= 0 and num_matches > rmax:
            # Signal inverted match by returning a negative match count.
            num_matches = -1
            res = False

        if pushed_path:
            bindings.pop_path()

        if not (res or marker is None):
            # Reset bindings
            bindings.marker = marker

        return (res, num_matches)


    def _bind_descend(self, bindings, node):
        '''
        Tries to match self.descend against children of node, descending further
        down the tree if no matches are found directly below node. If match,
        bindings is updated and True is returned. If no match, bindings is left
        untouched and False is returned.
        '''

        stack = deque((node, ))

        # Store methods in local vars to avoid expensive lookups in the loop
        popleft = stack.popleft
        extend = stack.extend
        bind_node = self.descend._bind_node

        repeat = self.descend.repeat
        rmin, rmax = NodeMatchSpec.REPEAT[repeat]
        tot_matches = 0
        match_type = self.descend._pattern_re.match

        while stack:
            # Stack holds parents whose children should be checked.
            parent = popleft()

            # Copy children, but pre-screen list to only include children whose
            # type matches the sought type. HUGE speed increase since we don't
            # have to call bind_node if list turns out to be empty.
            children = [ child for child in parent.children \
                         if match_type(child.type) ]

            # We don't call bind_node for an empty list. It is possible that
            # bind_node would return success = True for an empty list; it would
            # if the match was an optional (? or *) match. However, in both
            # these cases the returned num_matches would be 0, no bindings
            # would have been added and tot_matches would be unchanged. This is
            # why we don't need to call it.
            if children:
                _, num_matches = bind_node(bindings, parent, children)
                tot_matches += num_matches

                if (rmax >= 0 and tot_matches >= rmax) or tot_matches < 0:
                    break

            # Grandchildren should also be checked.
            extend(parent.children)

        # Return value depends on whether the number of matches we got is
        # acceptable or not.
        return tot_matches >= rmin and (rmax < 0 or tot_matches <= rmax)


    def _match_children(self, bindings, node):
        '''
        Matches the children of this spec against children of given node. If a
        match is found, bindings are updated and True is returned. If no match,
        bindings are left untouched and False is returned.
        '''

        res = True

        children = node.children[:]

        for matchable in self._matchables:
            res, _ = matchable._bind_node(bindings, node, children)
            if not res:
                break

        # If res is True we have a match UNLESS there are unmatched children.
        return res and every(lambda c: c is None, children)


    def get_valid_bindings(self):
        '''Returns a list of all bindings that can be produced by this match.'''
        if self.binding:
            bindings = [ self.binding ]
        else:
            bindings = []

        nodes = []
        if self.children:
            nodes += self.children
        if self.descend:
            nodes.append(self.descend)

        for node in nodes:
            bindings += [ '%s.%s' % (self.binding, node_binding) for
                          node_binding in node.get_valid_bindings() ]

        return bindings


    def __str__(self):
        lst = []
        if self.binding:
            lst.append(self.binding)

        if not self._is_binding_pattern:
            lst.append(":'" + self._pattern + "'")
        elif not self.binding:
            lst.append(':' + self._pattern)

        if self.repeat:
            lst.append(self.repeat)
        if self.children:
            if self.children:
                lst += [ '(',
                         ', '.join([ str(c) for c in self.children ]),
                         ')' ]
        if self.descend:
            lst += [ ' > ',  str(self.descend) ]
        elif self.value:
            lst += [ '=', str(self.value) ]
        return ''.join(lst)


    ############################################################################
    # Properties

    def get_binding(self):
        '''Returns current binding.'''
        return self._binding

    def set_binding(self, binding):
        '''Updates binding.'''
        self._binding = binding


    def get_pattern(self):
        '''Returns current pattern.'''
        return self._pattern

    def set_pattern(self, pattern):
        '''Updates pattern. If pattern is set to None, pattern is taken from
        self.binding. If self.binding is also None, pattern cannot be unset.'''

        # Note: make sure to add $ to the end of pattern before compiling:
        # re.match will match the beginning of a string, but we need to make
        # sure we match the whole string.

        if pattern:
            try:
                self._pattern_re = re.compile(pattern + '$', re.IGNORECASE)
            except re.error:
                raise ValueError('Invalid pattern %s' % pattern)
            self._is_binding_pattern = False
            self._pattern = pattern

        elif self.binding:
            try:
                self._pattern_re = re.compile(self.binding + '$', re.IGNORECASE)
            except re.error:
                raise ValueError('Binding %s is not a valid pattern' % \
                                 self.binding)
            self._is_binding_pattern = True
            self._pattern = self.binding

        else:
            raise ValueError('At least one of pattern or binding must be set.')


    def get_repeat(self):
        '''Returns current repeat.'''
        return self._repeat

    def set_repeat(self, repeat):
        '''Updates repeat. Raises ValueError on invalid repeat type.'''
        if not repeat in ( None, '+', '?', '*', '!' ):
            raise ValueError('Invalid repeat type "%s"' % repeat)
        self._repeat = repeat
        if repeat == '!':
            self.binding = None


    def get_value(self):
        '''Returns current value.'''
        return self._value

    def set_value(self, value):
        '''Updates value. If value is not None, self.children must be.'''
        if value and self.children:
            raise ValueError('Both children and value attributes cannot be set')
        self._value = value
        if value:
            self._value_re = re.compile(value + '$', re.I)
        else:
            self._value_re = None


    def get_children(self):
        '''Returns current children.'''
        return self._children

    def set_children(self, children):
        '''Updates children. If children is not None, self.value must be.'''
        if children and self.value:
            raise ValueError('Both children and value attributes cannot be set')

        self._children = children

        match_level = { '!':  [], None: [], '+':  [], '?':  [], '*':  [] }
        if children:
            for matchable in children:
                match_level[matchable.repeat].append(matchable)

        self._matchables  = match_level['!']
        self._matchables += match_level[None]
        self._matchables += match_level['+']
        self._matchables += match_level['?']
        self._matchables += match_level['*']


    binding     = property(get_binding, set_binding)
    pattern     = property(get_pattern, set_pattern)
    repeat      = property(get_repeat, set_repeat)
    value       = property(get_value, set_value)
    children    = property(get_children, set_children)



class Rule(object):
    '''
    A rewriting rule.
    Consists of node match specs and node replacement specs.
    Rules with only match nodes should have replace=[].
    '''

    def __init__(self, name, match, replace):
        '''
        Constructor. Parameters:
            match:   A NodeMatchSpec instance, specifying what to match.
            replace: A list of tuples, where each tuple is one replacement to
                     perform: (binding_path, [ NodeReplaceSpec, ... ])
        '''
        self.name    = name
        self.match   = match
        self.replace = replace
        self.valid_bindings = match.get_valid_bindings()
        self._expand_bindings()


    def _expand_bindings(self):
        '''
        Expands contracted binding paths in self.replace to full binding paths.
        Raises RuleParseException on ambiguous or missing bindings.
        '''
        used_bindings = [ path for path, _ in self.replace ]

        # Check that all used bindings exist either as a full path or as the end
        # of a path in possible bindings.
        for i, path in enumerate(used_bindings):
            tmp = _expand_path(path, self.valid_bindings)
            self.replace[i] = (tmp, self.replace[i][1])

        # Expand bindings in all NodeReplaceSpecs
        for _, nrs_list in self.replace:
            for nrs in nrs_list:
                nrs.expand_bindings(self.valid_bindings)



    def apply(self, root):
        """
        Apply this rule to node <root>. If rule matches, the replacements are
        performed and the number of replacements that were performed is
        returned. If rule did not match or if the replacement specification did
        not generate any nodes, the number will be 0.
        """

        bindings = Bindings()
        num_replacements = 0

        # Do matching
        res, _ = self.match.bind_node(bindings, root)
        if res:
            # Add empty default binding for unbound matches.
            bindings.add_default_bindings((None, None), self.valid_bindings)

            # Match succeded; perform replacements one by one.
            # A replacement consists of a binding_path and a list of
            # NodeReplaceSpec instances.

            for binding_path, repl_spec_list in self.replace:
                # Find the node(s) bound to path.
                bindings.path = binding_path
                old = bindings.get()

                replacements = mapcan(lambda x : x.make_nodes(bindings),
                                      repl_spec_list)

                # The old bindings can now be removed; make_nodes may
                # use them so they can't be removed before this.
                bindings.remove()

                for repl_parent, repl_node in old:
                    if repl_node and replacements:
                        num_replacements += len(replacements)

                        if len(old) > 1:
                            # Clone the replacement nodes. We can't add the
                            # same nodes in several places in the tree.
                            new = [ node.clone() for node in replacements ]
                        else:
                            new = replacements

                        self._replace_node(repl_parent, repl_node, new)

                        # Add bindings to the new nodes as replacements for
                        # the bindings removed above.
                        for new_node in new:
                            bindings.add( (repl_parent, new_node) )

        return num_replacements


    def _replace_node(self, parent, node, new_nodes):
        '''
        Replaces <node> in <parent> with nodes in <new_nodes> list.
        '''
        # We do a manual search because list.index uses node.__eq__ which
        # is a weak equality, whereas we need the exact same object.
        for i, child in enumerate(parent.children):
            if child is node:
                parent.children = \
                                parent.children[:i] + \
                                new_nodes + \
                                parent.children[i+1:]
                return

        raise ValueError('Node not found in parent\n  parent: %s\n  node:   %s'
                         % (parent, node))


    def __str__(self):
        replace = []
        for binding_path, repl_spec in self.replace:
            replace.append('%s := %s\n' % \
                           (binding_path, ', '.join(map(str, repl_spec))))
        return '%s\n::\n%s\n->\n%s::\n' % (
            self.name, self.match, ''.join(replace))



class NodeReplaceSpec(object):
    '''
    A class representing a replacement node. A replacement node can either be
    a clone of an old node, or a new node.
    '''
    def __init__(self, type=None, binding_path=None,
                 value=None, children=None, is_macro=False):

        if (type is None) == (binding_path is None):
            raise ValueError('Either type or binding_path must be given.')

        if value and children:
            raise ValueError('Both value and children cannot be given.')

        self.type = type
        self.binding_path = binding_path
        self.value = value
        self.children = children
        self.is_macro = is_macro


    def make_nodes(self, bindings=None):
        '''
        Creates new node(s) or clones old ones, based on this replacement spec.
        Param <bindings> can be None if a new node is to be created, otherwise
        it must be a Binding instance.
        Returns a list of Node instances.
        '''
        # Check class invariant
        assert (self.binding_path is None) != (self.type is None)
        assert not self.value or not self.children

        if self.children and not bindings:
            raise ValueError('Must have bindings to clone nodes')

        if self.is_macro:
            return self._macro_make_nodes(bindings)

        if self.type:
            nodes = []

            # Make new node...
            if self.value:
                # ...of type LeafNode
                nodes.append(LeafNode(
                    self.type.get(bindings),
                    self.value.get(bindings),
                    start_pos=-2**31))
            else:
                # ...of type ParentNode
                nodes.append(ParentNode(self.type.get(bindings), []))
        elif self.children is None:
            # Make deep clone(s) of old node(s)
            matches = bindings.get(self.binding_path)

            if matches:
                nodes = [ node.clone() for _, node in matches
                          if not node is None ]
            else:
                nodes = []
        else:
            # Since we have a (possibly empty) list of children we must make
            # ParentNodes. The existing nodes may be parent nodes of leaf nodes.
            # Just make new ParentNodes with the same type.
            matches = bindings.get(self.binding_path)
            if matches:
                nodes = [ ParentNode(node.type, []) for _, node in matches
                          if not node is None]
            else:
                nodes = []

        # Add children according to specs, if given
        if nodes and self.children:
            for node in nodes:
                node.children = mapcan(lambda c : c.make_nodes(bindings),
                                       self.children)
        return nodes


    def _macro_make_nodes(self, bindings):
        ''' Makes nodes using a macro '''
        nodes = []
        macro_name = self.binding_path.lower()

        if macro_name == 'foreach':
            if not self.children or len(self.children) != 2:
                raise ValueError('@foreach takes exactly two arguments')
            if not self.children[0].binding_path:
                raise ValueError('No binding path for @foreach')

            bound_path = self.children[0].binding_path
            expression = self.children[1]
            macro_binds = bindings.get(bound_path)

            if macro_binds:
                bindings.remove(bound_path)

                # Rebind bound_path to each match in turn, and repeat make_nodes
                for p, node in macro_binds:
                    if p and node is not None:
                        bindings.add((p, node), bound_path)
                        nodes += expression.make_nodes(bindings)
                        bindings.remove(bound_path)

                # Revert to old bindings
                for p, node in macro_binds:
                    bindings.add((p, node), bound_path)

        elif macro_name == 'fst':
            if not self.children or len(self.children) != 1:
                raise ValueError('@fst takes exactly one argument')

            nodes = self.children[0].make_nodes(bindings)
            if nodes and len(nodes[0].children) > 0:
                nodes = nodes[0].children[0:1]
            else:
                nodes = []

        elif macro_name == 'snd':
            if not self.children or len(self.children) != 1:
                raise ValueError('@snd takes exactly one argument')

            nodes = self.children[0].make_nodes(bindings)
            if nodes and len(nodes[0].children) > 1:
                nodes = nodes[0].children[1:2]
            else:
                nodes = []

        elif macro_name == 'children':
            if not self.children or len(self.children) != 1:
                raise ValueError('@children takes exactly one argument')

            nodes = self.children[0].make_nodes(bindings)
            nodes = mapcan(lambda x : x.children, nodes)

        else:
            raise ValueError('No such macro defined: %s' % (macro_name,))

        return nodes


    def get_used_bindings(self):
        '''
        Returns a list of all bindings that are used by this spec to build the
        replacement nodes.
        '''
        if self.binding_path:
            if self.is_macro:
                bindings = []
            else:
                bindings = [ self.binding_path ]

            if self.children:
                bindings += mapcan(lambda c : c.get_used_bindings(),
                                   self.children)

            return bindings
        else:
            return []


    def expand_bindings(self, possible_bindings):
        '''
        Expands contracted binding paths in self (and self.children) to full
        binding paths.  Raises RuleParseException on ambiguous or missing
        bindings.
        '''
        if self.binding_path and not self.is_macro:
            self.binding_path = _expand_path(self.binding_path,
                                             possible_bindings)
        elif self.type:
            self.type.expand_bindings(possible_bindings)

        if self.children:
            for child in self.children:
                child.expand_bindings(possible_bindings)
        elif self.value:
            self.value.expand_bindings(possible_bindings)


    def __str__(self):
        lst = []
        if self.is_macro:
            lst.append('@')
        if self.type:
            lst.append(str(self.type))
        if self.binding_path:
            lst.append(self.binding_path)
        if self.value:
            lst.append('=%s' % self.value)
        if not self.children is None:
            lst.append('(%s)' % ', '.join(map(str, self.children)))
        return ''.join(lst)



class Bindings(object):
    '''
    Class representing the nodes and bindings for one matching rule.
    Associates items with paths. Paths are case insensitive.

    Example usage:
        bindings = Bindings()
        bindings.path = "x.y"
        bindings.add("foo")
        bindings.add(SomeObject())
        bindings.add("bar", path="x.y.z")
        bindings.pop_path()   # returns "z"
    '''

    def __init__(self):
        self._path = []
        self._bindings = []


    def get_path(self):
        '''Returns the current binding path.'''
        return '.'.join(self._path)


    def set_path(self, path):
        '''Sets the current binding path to <path>.'''
        self._path = path.lower().split('.')


    def push_path(self, path_component):
        '''Adds <path_component> to current binding path.'''
        self._path.append(path_component.lower())


    def pop_path(self):
        '''
        Removes the last path component from path and returns it.
        Returns empty string if path is empty.
        '''
        try:
            path_component = self._path.pop()
        except IndexError:
            # pop from empty list
            path_component = ''
        return path_component


    def add(self, binding, path=None):
        '''Adds a binding at <path> if given, otherwise for current path.'''
        if path:
            path = path.lower()
        else:
            path = self.path
        self._bindings.append( (path, binding) )


    def remove(self, path=None):
        '''
        Removes all bindings for <path> if given, otherwise for current path.
        '''
        if path:
            path = path.lower()
        else:
            path = self.path
        # Filter list and remove bindings that match path
        self._bindings = filter(lambda b: b[0] != path, self._bindings)


    def get_marker(self):
        '''Returns a marker representing the current state of the bindings.'''
        return len(self._bindings)


    def set_marker(self, marker):
        '''Restores state of bindings to state represented by marker.'''
        if marker < 0:
            raise ValueError('Invalid marker %i.' % marker)
        bindings = self._bindings
        if marker < len(bindings):
            self._bindings = bindings[ : marker]


    def get(self, path=None):
        '''
        Returns a list of all bindings for <path> if given, otherwise for
        the current path. If there are no bindings for <path> then bindings for
        paths ending with <path> will be returned. However, if there are
        bindings for _different_ paths that end in <path> a ValueError is raised
        since the referred binding cannot be determined unambiguously in such
        cases.
        '''
        if path:
            path = path.lower()
        else:
            path = self.path

        return [ bound for binding, bound in self._bindings
                 if binding == path ]


    def add_default_bindings(self, def_value, lst):
        '''
        Adds a binding to <def_value> for each binding path in lst that did not
        already have a binding.
        '''
        for binding in lst:
            found = some(lambda x : x[0] == binding,
                         self._bindings)
            if not found:
                self.add(def_value, path=binding)


    def __str__(self):
        bindings = self._bindings[:]
        bindings.sort()
        out = [ 'Bindings:' ]
        for path, item in bindings:
            if hasattr(item, '__iter__'):
                item = ', '.join(map(str, item))
            out.append('%s: %s' % (path, item))
        return '\n'.join(out)


    def __eq__(self, other):
        if isinstance(other, Bindings):
            for path, _ in self._bindings:
                bindings = other.get(path=path)
                if not self.get(path=path) == bindings:
                    return False
            return True
        return False


    path   = property(get_path,   set_path)
    marker = property(get_marker, set_marker)



class StringSourcePlain(object):
    '''
    A string source that just returns the string it was given.
    '''

    qstring_re = re.compile(r'^["\'](.*)["\']$')

    def __init__(self, string):
        '''Constructor. Parameters:
             string: a quoted string. The get-method will return this string
                     with quotes removed.
        '''
        match = StringSourcePlain.qstring_re.match(string)
        if match:
            self._string = match.group(1)
        else:
            raise ValueError('Invalid quoted string %s' % string)


    def get(self, bindings, separator=''):
        '''Generates the string.'''
        return self._string


    def expand_bindings(self, _):
        '''Does nothing for this class.'''
        pass


    def __str__(self):
        return "'" + self.get(None) + "'"



class StringSourceBinding(object):
    '''
    A string source that returns the value of a bound node/nodes.
    '''
    def __init__(self, binding_path):
        self._binding_path = binding_path


    def get(self, bindings, separator=''):
        '''Generates the string.'''
        bound = bindings.get(self._binding_path)
        if bound:
            values = [ node.value for _, node in bound
                       if node is not None ]
            return separator.join(values)
        return ''


    def expand_bindings(self, possible_bindings):
        '''Expands contracted binding path using <possible_bindings>.'''
        self._binding_path = _expand_path(self._binding_path, possible_bindings)


    def __str__(self):
        return self._binding_path



class StringSourceGenerator(object):
    '''
    A string source that generates a string using a specified method +
    parameters and the provided bindings.
    '''
    GENERATORS = ( 'concat', 'join', 'gensym' )
    _GENSYM_DICT = {}


    def __init__(self, method, parameters=None):
        self.method = method
        self.parameters = parameters


    def get(self, bindings, separator=''):
        '''Generates the string.'''
        method = self.method.lower()

        if method == 'concat':
            if self.parameters:
                return ''.join([ src.get(bindings) for src in self.parameters ])
            return ''

        elif method == 'join':
            if self.parameters:
                sep = self.parameters[0].get(bindings)
                return sep.join(
                    [ src.get(bindings, sep) for src in self.parameters[1:] ])
            return ''

        elif method == 'gensym':
            if self.parameters:
                prefix = ''.join(
                    [ src.get(bindings) for src in self.parameters ])
            else:
                prefix = 'gensym_'

            dct = StringSourceGenerator._GENSYM_DICT
            i = dct.setdefault(prefix, -1) + 1
            dct[prefix] = i

            if i == 0:
                return prefix + str(i)
            else:
                return prefix + str(i) + '0'

        else:
            raise ValueError('Invalid string generator "%s"' % self.method)


    def expand_bindings(self, possible_bindings):
        '''
        Expands contracted binding paths in parameters using
        <possible_bindings>.
        '''
        if self.parameters:
            for param in self.parameters:
                param.expand_bindings(possible_bindings)


    def reset_gensyms():
        ''' Clears the gensym table '''
        StringSourceGenerator._GENSYM_DICT = { }

    reset_gensyms = staticmethod(reset_gensyms)

    def __str__(self):
        return '$%s(%s)' % (self.method, ', '.join(map(str, self.parameters)))



class TRS(object):
    '''
    Main class for the Tree Rewriting System.
    '''
    def __init__(self):
        self.rules = []


    def load_rules_from_string(self, rule_string):
        '''Loads rule set from <rule_string>.'''
        self.rules = RuleParser().parse(rule_string)


    def load_rules_from_file(self, filename):
        '''Loads rule set from file <filename>.'''
        f_handle = open(filename, 'rt')
        try:
            self.load_rules_from_string(f_handle.read())
        finally:
            f_handle.close()


    def rewrite_tree(self, root):
        '''
        Rewrites tree starting at <root> according to currently loaded rule set.
        Returns the root of the rewritten tree. If the root node has been
        replaced with multiple nodes, a list of the new roots is returned.
        '''
        fake_root = ParentNode('!fake root node!', children=[ root ])

        # Apply rules until no more changes
        num_repl = 1

        while num_repl > 0:
            num_repl = 0
            for rule in self.rules:
                while rule.apply(fake_root):
                    num_repl += 1

        if len(fake_root.children) == 1:
            return fake_root.children[0]
        else:
            return fake_root.children



def main(rule_files, ibp_file):
    '''
    Loads rule set from file <rule_file> and parses the IBP program in file
    <ibp_file>. The rules are then applied to the resulting IBP tree, and
    the rewritten tree is printed to stdout.
    '''
    from pc.parsing.IBP_Parser import IBP_Parser

    root = IBP_Parser().parse(open(ibp_file, 'rt').read())
    trs = TRS()
    try:
        for rule_file in rule_files:
            trs.load_rules_from_file(rule_file)
            root = trs.rewrite_tree(root)
    except RuleParseException, error:
        print "Error in rules: %s" % error
        return

    print tree_to_str(root)



if __name__ == '__main__':
    if len(sys.argv) >= 3:
        main(sys.argv[1:-1], sys.argv[-1])
    else:
        print 'Usage:\n  %s <rule file> <ibp file>' % sys.argv[0]

