from pc.util.Utils import *

import re


class Node(object):
    '''Common superclass of nodes.'''

    def __nonzero__( self ):
        return True

    def descendants_by_type(self, type):
        x = mapcan(lambda x: x.descendants_by_type(type), self.children)
        if self.type == type:
            return [self] + x
        else:
            return x


class ParentNode(Node):
    '''Class representing a parent (non-terminal node) in a parse tree.'''

    def __init__( self, typ, children, start_pos=-1, start_line=-1, end_pos=-1, end_line=-1 ):
        self.type = typ

        assert type(children)==list,"children argument not of type list: "+repr(children)

        self.children = []
        self.sage = "" #sage checking
        for kid in flatten(children):
            assert isinstance(kid, Node),"child list element not of type Node: "+repr(kid)
            self.children.append(kid)
            
        self._start_pos = start_pos
        self._end_pos = end_pos
        self._start_line = start_line
        self._end_line = end_line
        self.json_location = []
        self.parse_ok = True

    def make( typ, chld, p ):
        assert len(p)>=2

        startidx = 1
        if isinstance(p[startidx],Node):
            start_pos = p[startidx].start_pos()
            start_line = p[startidx].start_line()
        else:
            start_pos = p.lexpos(startidx)
            start_line = p.lineno(startidx)
            
        endidx = len(p)-1
        if isinstance(p[endidx],Node):
            end_pos = p[endidx].start_pos()
            end_line = p[endidx].start_line()
        else:
            end_pos = p.lexpos(endidx)
            end_line = p.lineno(endidx)
            
        return ParentNode(typ,chld,start_pos,start_line,end_pos,end_line)

    make = staticmethod(make)
                          

    def start_pos(self):
        if self._start_pos>=0: return self._start_pos
        elif self.children: return self.children[0].start_pos()
        else: return -1

    def end_pos(self):
        if self._end_pos>=0: return self._end_pos
        elif self.children:return self.children[-1].end_pos()
        else: return -1

    def start_line(self):
        if self._start_line>=0: return self._start_line
        elif self.children: return self.children[0].start_line()
        else: return -1

    def end_line(self):
        if self._end_line>=0: return self._end_line
        elif self.children: return self.children[-1].end_line()
        else: return -1

    def children_by_type(self, type):
        '''
        Returns a list of children of self which have specified type
        '''
        return filter(lambda x : x.type==type, self.children)

    def grandchildren_by_type(self, type):
        '''
        Returns a list of the children of children of self which have specified type
        '''
        return mapcan(lambda x : x.children, self.children_by_type(type))

    def __len__( self ):
        return self.children.__len__()


    def _match_apply( self, key, action ):
        '''
        Indexing/slicing of children. Also supports path specifiers based on node type,
        e.g. x["t1.t2"] returns the children of type "t2" of all children of type "t1" of
        x.
        '''
        head,key = key[0],key[1:]

        if isinstance(head,int):
            m = [self.children[head]]
        elif isinstance(head,slice):
            m = self.children[head]
        elif type(head) in (str,unicode):
            assert "." not in head
            if "*" in head: s = lambda k,x: re.match("^"+k.replace('*','(.*)')+"$",x) != None
            else: s = lambda k,x: x==k
            if head[0]=="@": f = lambda x: isinstance(x,LeafNode) and s(head[1:],x.value)
            else: f = lambda x: s(head,x.type)
            m = filter(f,self.children)
        else:
            raise Exception("unsupported key type: %r"%type(head))

        if key:
            for x in m: x._match_apply(key,action)
        else:
            for x in m: action(self,x)
        
        
    def __getitem__( self, key ):
        if type(key) in (int,slice):
            return self.children[key]
        else:
            if not isinstance(key,tuple): key = (key,)
            m = []
            self._match_apply(key,lambda p,c:m.append(c))
            return m

    def __delitem__( self, key ):
        self._match_apply(key,lambda p,c:p.children.remove(c))
    
    def clone(self):
        '''
        Makes a deep copy of this node.
        '''
        return ParentNode(self.type,[child.clone() for child in self.children])

    def __eq__(self, other):
        # Only require type to match, children may be unequal
        # Is this really useful?
        return isinstance(other, ParentNode) and self.type == other.type

    def __str__(self):
        #return 'T: %s  [%s:%s|%s:%s]' % \
        #       (self.type,
        #        self.start_line(), self.start_pos(),
        #        self.end_line(), self.end_pos())
        return 'T: %s' % self.type
    


class LeafNode(Node):
    ''' Class representing a leaf (terminal) node in a parse tree.'''

    def __init__(self, typ, value, start_pos=-1, lineno=-1):
        assert isinstance(typ,str),"expected <str>, got %s" % type(typ)
        assert isinstance(value,str),"expected <str>, got %s" % type(value)
        self.type = typ
        self.value = value
        self._start_pos = start_pos
        self._end_pos = start_pos + len(value)
        self._lineno = lineno
        self.children = ()
        self.json_location = []
        self.parse_ok = True

        
    def start_pos(self):
        return self._start_pos

    def end_pos(self):
        return self._end_pos

    def start_line(self):
        return self._lineno

    def end_line(self):
        return self._lineno

    def make(p, index):
        #print "Toke: %s, Type: %s" % (p[index], p.slice[index].type)
        return LeafNode(p.slice[index].type, p[index], p.lexpos(index), p.lineno(index))
    make = staticmethod(make)

    def clone(self):
        '''Makes an identical copy of this node.'''
        return LeafNode(self.type, self.value, self._start_pos, self._lineno)

    def clone_with_suffix(self, suffix):
        '''Makes a suffix appended copy of this node.'''
        return LeafNode(self.type, self.value+suffix, self._start_pos, self._lineno)

    def __getitem__( self, key ):
        return []

    def __eq__(self, other):
        # Structural equality; only require type and value to match.
        return (isinstance(other, LeafNode)
                and self.type == other.type
                and self.value == other.value)

    def __str__(self):
        return 'T: %s, V: %s' % (self.type, self.value)
        #return 'T: %s, V: %s  [%s:%s|%s:%s]' % \
        #       (self.type, self.value,
        #        self.start_line(), self.start_pos(),
        #        self.end_line(), self.end_pos())



def tree_to_str(root):
    """ Converts tree starting at root to a string. Useful for debug output """

    def tree_to_str_rec(root, indent, str_list):
        str_list.append(indent + str(root))
        assert None not in root.children,root
        for child in root.children:
            assert type(child)!=list,root
            tree_to_str_rec(child, indent + ' ' * 4, str_list)

    lst = []
    tree_to_str_rec(root, '', lst)
    return '\n'.join(lst)


def tree_equal(left, right):
    """ Return True if left tree matches right tree (recursively) """
    if left == right:
        if isinstance(left, ParentNode):
            if len(left.children) != len(right.children):
                return False
            else:
                return every(tree_equal, left.children, right.children)
        else:
            return True
    else:
        return False


def find_nodes_of_type(node, node_type, recursive=False):
    '''
    Returns a list of all nodes of type <node_type> found at <node>. Checks
    <node> and any children it has. If recursive, all nodes in the tree
    starting at node are searched.

    Note: it is usually not a good idea to use this with recursive =
    False. In most cases, you can use node.children_by_type
    instead. The only difference is that children by type only scans
    the children, while a non recursive find_nodes_of_type checks the
    node itself (+ its direct children)
    '''
    found = []

    if node.type == node_type:
        found.append(node)

    for child in node.children:
        if not recursive and child.type == node_type:
            found.append(child)
        elif recursive:
            found += find_nodes_of_type(child, node_type, recursive)

    return found


def filter_nodes(predicate, node, recursive=False):
    '''
    Returns a list of all nodes for which predicate is true. Checks
    <node> and any children it has. If recursive, all nodes in the tree
    starting at node are searched.
    '''
    found = []

    if predicate(node):
        found.append(node)

    for child in node.children:
        if not recursive and predicate(child):
            found.append(child)
        elif recursive:
            found += filter_nodes(predicate, child, recursive)

    return found
