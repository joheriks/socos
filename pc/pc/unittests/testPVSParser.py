#!/usr/bin/env python

from pc.parsing.AST import ParentNode, LeafNode
from pc.parsing.PVSParser import PVSParser
from pc.parsing.Token import Token
from pc.parsing.ParseError import ParseException, ParseError

parser = PVSParser()

def parse(parse_string):
    return parser.parse(parse_string)

def showTree(root, indent=' '*4):
    print indent + str(root)
    if isinstance(root, ParentNode):
        if root.children:
            for child in root.children:
                showTree(child, indent + (' '*4))

def checkNode(node, expected):
    if not (node.type == expected.type
            and (not hasattr(expected, 'value') or node.value == expected.value)
            and node.start_pos() == expected.start_pos()
            and node.end_pos() == expected.end_pos()):
        raise ParseException([ParseError("Bad Tree")])

def checkTree(root, expected, indent=''):
    try:
        checkNode(root, expected)
        if isinstance(root, ParentNode):
            if root.children:
                for n, child in enumerate(root.children):
                    checkTree(child, expected.children[n], indent + (' '*4))
    except ParseException, inst:
        print "Found tree: "
        showTree(root)
        print "Expected tree: "
        showTree(expected)
        raise inst

def testNumber():
    root = parse('42')
    e_root = LeafNode("NUMBER", '42', 0)
    checkTree(root, e_root)

def testString():
    root = parse('"test"')
    e_root = LeafNode("STRING", 'test', 0)
    checkTree(root, e_root)

def testID_excl_number():
    root =  parse('id ! 42')
    e_root = ParentNode("EXCLAMATION_MARK", [])
    e_root.children = [
        LeafNode("ID", 'id', 0),
        LeafNode("NUMBER",'42', 5),
        ]
    checkTree(root, e_root)

def testExpressions():
    root = parse('ff (45, 99)')
    e1 = ParentNode('name', [ ParentNode('idop', [ LeafNode("ID",'ff',0) ]) ])
    e2 = ParentNode('arguments', [
        LeafNode("NUMBER",'45',4),
        LeafNode("NUMBER",'99',8)])
    e_root = ParentNode('expression_arguments', [e1, e2])
    checkTree(root, e_root)

def testBinOp():
    root = parse('"string" => 56')
    e_root = ParentNode('EQUAL_GT', [
        LeafNode("STRING", 'string',0),
        LeafNode("NUMBER", '56', 12)
        ])
    checkTree(root, e_root)

def testUnaryOp():
    root = parse('[] 45')
    e_root = ParentNode('LBRACKET_RBRACKET', [
        LeafNode("NUMBER",'45',3)])
    checkTree(root, e_root)

def testExpressionSQIdOrNumber():
    root = parse('38 `  someid ')
    e_root = ParentNode('SINGLE_QUOTE', [
            LeafNode('NUMBER', '38', 0),
            LeafNode("ID", 'someid', 6)
            ])
    checkTree(root, e_root)

def testExpressionLists():
    contents = '26, "foo", 923'
    e1 = LeafNode("NUMBER", '26', 2)
    e2 = LeafNode("STRING", 'foo', 6)
    e3 = LeafNode("NUMBER", '923',  13)
    e_root = ParentNode('expression_list_1', [ e1, e2, e3 ])

    root = parse(' (' + contents + ')')
    checkTree(root, e_root)

    e_root.type = 'expression_list_2'
    root = parse('(:' + contents + ':)')
    checkTree(root, e_root)

    e_root.type = 'expression_list_3'
    root = parse('[|' + contents + '|]')
    checkTree(root, e_root)

    e_root.type = 'expression_list_4'
    root = parse('(|' + contents + '|)')
    checkTree(root, e_root)

    e_root.type = 'expression_list_5'
    root = parse('{|' + contents + '|}')
    checkTree(root, e_root)

def testAssignmentList():
    root = parse('(# foo  :=  273, bar  |->  "string" #)')
    e1 = ParentNode('assignment_ce',[])
    e1.children = [ ParentNode('assignargs',[LeafNode("ID","foo",3)])]
    e1.children.append(LeafNode("NUMBER","273",12))
    e2 = ParentNode('assignment_vmg',[])
    e2.children = [ParentNode('assignargs',[LeafNode("ID","bar",17)])]
    e2.children.append(LeafNode("STRING",'string',27))
    e_root = ParentNode('assignment_list', [e1,e2])
    checkTree(root, e_root)

def testName():
    parse('someid @ NOT [ 26+82, 99, "string" ] . anotherid')
    # FIXME: check against expected tree

def testAll():
    try:
        test = 'testNumber'
        testNumber()
        
        test = 'testString'
        testString()
        
        test = 'testID_excl_number'
        testID_excl_number()
        
        test = 'testBinOp'
        testBinOp()
        
        test = 'testExpressions'
        testExpressions()
        
        test = 'testExpressionSQIdOrNumber'
        testExpressionSQIdOrNumber()
        
        test = 'testExpressionLists'
        testExpressionLists()
        
        test = 'testAssignmentList'
        testAssignmentList()
        
        test = 'testUnaryOp'
        testUnaryOp()
        
        test = 'testName'
        testName()
        
    except ParseException, e:
        print "\nTest %s failed" % test
        print e
        return 1

    return 0

if __name__=='__main__':
    import sys
    sys.exit(testAll())
