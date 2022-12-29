#!/bin/env python2


import unittest

from pc.rewrite.trs import Bindings, NodeMatchSpec, NodeReplaceSpec
from pc.rewrite.trs import StringSourcePlain, StringSourceBinding
from pc.rewrite.trs import StringSourceGenerator
from pc.rewrite.trs import TRS, RuleParseException
from pc.parsing.AST import Node

class TestBindings(unittest.TestCase):
    '''
    Tests TRS Bindings class.
    '''
    def test_set_get_path(self):
        bindings = Bindings()
        bindings.path = 'a.B.c'
        self.assertEqual(bindings.path, 'a.b.c')


    def test_push_pop_path(self):
        bindings = Bindings()
        bindings.path = ''
        map(bindings.push_path, ('foo', 'bar'))
        self.assertEqual(bindings.pop_path(), 'bar')
        self.assertEqual(bindings.pop_path(), 'foo')
        self.assertEqual(bindings.pop_path(), '')
        self.assertEqual(bindings.pop_path(), '')


    def test_add_get(self):
        bindings = Bindings()
        bindings.add('foo', path='x.Y')
        self.assertEqual(bindings.get(path='X.y'), [ 'foo' ])
        bindings.path = 'f.g'
        bindings.add('bar')
        self.assertEqual(bindings.get()[0], 'bar')


    def test_remove(self):
        bindings = Bindings()
        bindings.add('foo', path='p')
        bindings.add('bar', path='p')
        bindings.add('baz', path='p.q')
        bindings.remove(path='p')
        self.assertEqual(bindings.get(path='p'), [])
        self.assertEqual(bindings.get(path='p.q'), [ 'baz' ])


    def test_marker(self):
        bindings = Bindings()
        bindings.path = 'p'
        bindings.add('foo')
        marker = bindings.marker
        bindings.add('bar')
        self.assertEqual(bindings.get(), [ 'foo', 'bar' ])
        bindings.marker = marker
        self.assertEqual(bindings.get(), [ 'foo' ])


    def test_eq(self):
        bindings1 = Bindings()
        bindings2 = Bindings()
        assert bindings1 == bindings2

        bindings1.add('foo', path='x')
        bindings2.add('foo', path='x')
        assert bindings1 == bindings2

        bindings1.add('bar', path='x.y')
        assert not bindings1 == bindings2

        bindings2.add('bar', path='x.y')
        assert bindings1 == bindings2



class DummyNode(Node):
    def __init__(self, **kwargs):
        self.type = None
        self.children = []
        self.value = None
        for key in kwargs.keys():
            setattr(self, key, kwargs[key])


    def clone(self):
        return self


    def __str__(self):
        return 'DummyNode "%s"' % self.type



class TestNodeReplaceSpec(unittest.TestCase):
    '''
    Tests TRS NodeReplaceSpec class
    '''
    def test_create(self):
        nrs = NodeReplaceSpec(type='nodetype', value='nodevalue')
        self.assertEqual(nrs.type, 'nodetype')
        self.assertEqual(nrs.value, 'nodevalue')

        nrs = NodeReplaceSpec(binding_path='foo.bar', children=[ 'c1', 'c2' ])
        self.assertEqual(nrs.binding_path, 'foo.bar')
        self.assertEqual(nrs.children, [ 'c1', 'c2' ])


    def test_make_new_node(self):
        nrs = NodeReplaceSpec(
            type=StringSourcePlain("'nodetype'"),
            value=StringSourcePlain("'nodevalue'"),
            )
        nodes = nrs.make_nodes()
        self.assertEqual(len(nodes), 1)
        self.assertEqual(nodes[0].type, 'nodetype')
        self.assertEqual(nodes[0].value, 'nodevalue')


    def test_make_old_nodes(self):

        nrs = NodeReplaceSpec(
            binding_path='p',
            children = [ NodeReplaceSpec(binding_path='p.c') ]
            )

        bindings = Bindings()
        bindings.add(( None, DummyNode(type='foo') ), path='p')
        bindings.add(( None, DummyNode(type='bar') ), path='p.c')
        bindings.add(( None, DummyNode(type='baz') ), path='p.c')

        nodes = nrs.make_nodes(bindings)
        self.assertEqual(len(nodes), 1)
        self.assertEqual(nodes[0].type, 'foo')

        children = nodes[0].children
        self.assertEqual(len(children), 2)
        self.assertEqual(children[0].type, 'bar')
        self.assertEqual(children[1].type, 'baz')



class TestNodeMatchSpec(unittest.TestCase):
    '''
    Tests TRS NodeMatchSpec class
    '''
    def test_match_simple(self):
        root = DummyNode(type='!')
        bindings = Bindings()
        nms = NodeMatchSpec('N')
        root.children = [ DummyNode(type='n') ]
        assert (True, 1) == nms.bind_node(bindings, root)
        self.assertEqual(bindings.get(path='n'), [ (root, root.children[0]) ])
        root.children = [ DummyNode(type='m') ]
        assert (False, 0) == nms.bind_node(bindings, root)


    def test_match_children_simple(self):
        root = DummyNode(type='!')
        bindings = Bindings()
        nms = NodeMatchSpec('n', children=[ NodeMatchSpec('o') ] )
        o = DummyNode(type='o')
        n = DummyNode(type='n', children=[ o ])

        root.children = [ n ]
        assert (True, 1) == nms.bind_node(bindings, root)
        self.assertEqual(bindings.get(path='n'),   [ (root, n) ])
        self.assertEqual(bindings.get(path='n.o'), [ (   n, o) ])

        n = DummyNode(type='n', children=[ o, o ])
        root.children = [ n ]
        assert (False, 0) == nms.bind_node(bindings, root)


    def test_match_inverted(self):
        root = DummyNode(type='!')
        nms = NodeMatchSpec(None, pattern='n', repeat='!')

        n = DummyNode(type='n')
        root.children = [ n ]

        assert (False, -1) == nms.bind_node(None, root)


    def test_match_repeat_optional(self):
        root = DummyNode(type='!')
        nms = NodeMatchSpec(None, pattern='n', repeat='?')

        assert (True, 0) == nms.bind_node(None, root)

        root.children.append(DummyNode(type='n'))
        assert (True, 1) == nms.bind_node(None, root)

        root.children.append(DummyNode(type='n'))

        assert (False, 1) == nms.bind_node(None, root)


    def test_match_repeat_zero_or_more(self):
        root = DummyNode(type='!')

        nms = NodeMatchSpec(None, pattern='n', repeat='*')

        for i in range(10):
            assert (True, i) == nms.bind_node(None, root)
            root.children.append(DummyNode(type='n'))
        assert (True, 10) == nms.bind_node(None, root)


    def test_match_repeat_one_or_more(self):
        root = DummyNode(type='!')

        nms = NodeMatchSpec(None, pattern='n', repeat='+')

        assert (False, 0) == nms.bind_node(None, root)

        for i in range(10):
            root.children.append(DummyNode(type='n'))
            assert (True, i + 1) == nms.bind_node(None, root)


    def test_match_regexp(self):
        root = DummyNode(type='!')

        nms = NodeMatchSpec(None, pattern=r'..?')

        root.children = [ DummyNode(type='n') ]
        assert (True, 1) == nms.bind_node(None, root)

        root.children = [ DummyNode(type='nn') ]
        assert (True, 1) == nms.bind_node(None, root)

        root.children = [ DummyNode(type='nnn') ]
        assert (False, 0) == nms.bind_node(None, root)


    def test_child_failure_then_match(self):
        root = DummyNode(type='!')

        nms = NodeMatchSpec('a',children=[
            NodeMatchSpec('b', children=[ NodeMatchSpec('c'), NodeMatchSpec('d') ]),
            NodeMatchSpec(None, pattern='.*')
            ])

        c = DummyNode(type='c', children=[])
        b = DummyNode(type='b', children=[ c, DummyNode(type='d') ])
        a = DummyNode(type='a', children=
                      [ DummyNode(type='b', children=[ c ]), b ] )

        bindings = Bindings()
        root.children = [ a ]
        assert (True, 1) == nms.bind_node(bindings, root)

        self.assertEqual(bindings.get('a'),     [ (root, a) ])
        self.assertEqual(bindings.get('a.b'),   [ (   a, b) ])
        self.assertEqual(bindings.get('a.b.c'), [ (   b, c) ])


    def test_match_zero_children(self):
        root = DummyNode(type='!')

        nms1 = NodeMatchSpec('a', children=[])
        nms2 = NodeMatchSpec('a', children=None)

        a = DummyNode(type='a')

        # Test with no children
        root.children = [ a ]
        bindings = Bindings()
        assert (True, 1) == nms1.bind_node(bindings, root)
        self.assertEqual(bindings.get('a'), [ (root, a) ])

        bindings = Bindings()
        assert (True, 1) == nms2.bind_node(bindings, root)
        self.assertEqual(bindings.get('a'), [ (root, a) ])

        # Test with one child
        a.children = [ DummyNode(type='a') ]

        assert (False, 0) == nms1.bind_node(bindings, root)

        bindings = Bindings()
        assert (True, 1) == nms2.bind_node(bindings, root)
        self.assertEqual(bindings.get('a'), [ (root, a) ])



class TestStringSources(unittest.TestCase):
    '''
    Tests the various StringSource* classes.
    '''
    def test_string_source_plain(self):
        src = StringSourcePlain('"mystring"')
        self.assertEqual(src.get(None), 'mystring')
        self.assertRaises(ValueError, StringSourcePlain, 'unquoted')
        self.assertRaises(ValueError, StringSourcePlain, '')


    def test_string_source_binding(self):
        src = StringSourceBinding('foo.bar')
        bindings = Bindings()
        bindings.add(
            (None, DummyNode(type='ntype', value='baz')),
            path='foo.bar')
        self.assertEqual(src.get(bindings), 'baz')

        bindings.add(
            (None, DummyNode(type='ntype', value='buh')),
            path='foo.bar')
        self.assertEqual(src.get(bindings), 'bazbuh')
        self.assertEqual(src.get(bindings, ','), 'baz,buh')


    def test_string_source_generator_concat(self):
        src = StringSourceGenerator(
            method='concat',
            parameters = [ StringSourcePlain("'a'"), StringSourcePlain("'b'") ] )
        self.assertEqual(src.get(None), 'ab')


    def test_string_source_generator_join(self):
        src = StringSourceGenerator(
            method='join', parameters=[
                StringSourcePlain("','"),
                StringSourcePlain("'a'"),
                StringSourcePlain("'b'")
                ] )
        self.assertEqual(src.get(None), 'a,b')


    def test_string_source_generator_gensym(self):
        src = StringSourceGenerator(
            method='gensym', parameters=[ StringSourcePlain("'foo_'") ])
        s1 = src.get(None)
        s2 = src.get(None)
        self.assertNotEqual(s1, s2)
        assert s1.startswith('foo_')
        assert s2.startswith('foo_')



class TestTRS(unittest.TestCase):
    '''
    Tests main TRS class and rule parser
    '''
    def test_load_rules(self):
        trs = TRS()
        trs.load_rules_from_file('sample_rules.trs')
        self.assertEqual(len(trs.rules), 4)

        trs.rules = []
        trs.load_rules_from_string(open('sample_rules.trs', 'rt').read())
        self.assertEqual(len(trs.rules), 4)


    def test_invalid_generator(self):
        trs = TRS()
        self.assertRaises(
            RuleParseException,
            trs.load_rules_from_string,
            "sample_rule :: node+() -> node := $thisisnotagenerator()='x'::"
            )


    def test_apply(self):
        trs = TRS()
        trs.load_rules_from_string('''
        sample_rule
        ::
        node(c1, c2='bar')
        ->
        node.c1 := 'a'='1'
        node.c2 := 'b'='2'
        node := 'newnode'(node.c1, node.c2)
        ::
        sample_rule2 :: root > node -> node := node(), 'new2'='v' ::
        ''')
        root = DummyNode(type='node', children=[
            DummyNode(type='c1', value='foo'),
            DummyNode(type='c2', value='bar')
            ])
        root = trs.rewrite_tree(root)
        self.assertEqual(root.type, 'newnode')
        self.assertEqual(len(root.children), 2)
        c1, c2 = root.children
        self.assertEqual(c1.type, 'a')
        self.assertEqual(c1.value, '1')
        self.assertEqual(c2.type, 'b')
        self.assertEqual(c2.value, '2')


    def test_bad_rule(self):
        trs = TRS()
        self.assertRaises(
            RuleParseException,
            trs.load_rules_from_string,
            'rule_name -> foo'
            )

        self.assertRaises(
            RuleParseException,
            trs.load_rules_from_string,
            'rule_name :: node -> node := node'
            )


    def test_string_and_parse(self):
        trs = TRS()
        trs.load_rules_from_string("""
        rule_name
        ::
        root > node(c1, c2) > node2='value' > :'node4'
        ->
        node.c1 := node.c2
        node := node.c1, node.c2
        ::
        """)
        self.assertEqual(len(trs.rules), 1)
        rule_string_1 = str(trs.rules[0])
        trs.load_rules_from_string(rule_string_1)
        self.assertEqual(len(trs.rules), 1)
        rule_string_2 = str(trs.rules[0])
        self.assertEqual(rule_string_1, rule_string_1)


    def test_predefined_matches(self):
        trs = TRS()
        trs.load_rules_from_string('name :: node(&any, &rest) -> node := rest, any ::')
        self.assertEqual(len(trs.rules), 1)
        node = DummyNode(type='node', children=[
            DummyNode(type='c1', value='foo'),
            DummyNode(type='c2', value='bar'),
            DummyNode(type='c3', value='baz')
            ])
        root = DummyNode(type='root', children=[ node ])
        i = trs.rules[0].apply(root)
        self.assertEqual(i, 3)
        self.assertEqual(len(root.children), 3)
        self.assertEqual(root.children[0].type, 'c2')
        self.assertEqual(root.children[1].type, 'c3')
        self.assertEqual(root.children[2].type, 'c1')

        self.assertRaises(RuleParseException,
                          trs.load_rules_from_string,
                          'name :: node(&bamboozle) -> node := node() ::'
                          )


if __name__ == '__main__':
    unittest.main()

