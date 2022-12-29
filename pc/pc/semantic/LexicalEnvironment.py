from pc.parsing.AST import *
from pc.util.Utils import *

import itertools


class IllegalSymbolException( Exception ):

    def __init__( self, sym, msg ):
        Exception.__init__(self,msg)
        self.sym = sym


class MatchException( Exception ):
    pass


class Symbol(object):
    ''' Class representing a symbol '''

    def __init__( self, id, type, file="__unnamed__", lineno=-1, pos=-1 ):
        ''' Constructs a new symbol. Id is the identifier which is used to refer
        to the symbol. Type indicates the name space of the symbol.

        file, lineno and pos specify where the symbol is defined

        Only id and type are used to distinguish symbols.
        '''
        self.id = id
        self.type = type
        self.file = file
        self.lineno = lineno
        self.pos = pos

    def from_node(node, type, file="__unnamed__", prefix=None):
        ''' Constructs a symbol from an ID node '''
        assert node.type == 'ID' or node.type == 'FUNCTION', tree_to_str(node)
        id = node.value
        if prefix:
            id = prefix + '_' +id
        return Symbol(id, type, file, node.start_line(), node.start_pos())
    from_node = staticmethod(from_node)

    def make_indexed( sym, index, newtype=None ):
        assert isinstance(sym,Symbol)
        assert type(index)==int
        return Symbol(sym.id+'_'+str(index), newtype if newtype else sym.type, sym.file, sym.lineno, sym.pos)
    make_indexed = staticmethod(make_indexed)

    def __hash__(self):
        return hash((self.id,self.type))

    def __eq__(self, other):
        return other.id == self.id and other.type == self.type

    def __ne__(self, other):
        return not (other.id == self.id and other.type == self.type)

    def __str__(self):
        return str(self.id)
                    
    def __repr__(self):
        return str(self)

    def location_str( self ):
        return self.file + ":" + str(self.lineno) + ":" + str(self.pos)



class LexicalEnvironment(object):
    '''
    Class representing a lexical environment, i.e. the symbols bound in a context
    '''

    def __init__(self, parent=None ):
        '''
        Creates a new lexical environment, with specified parent. Parent is the
        enclosing lexical environment (possibly None, which means this environment
        will be global)
        '''
        assert parent is None or isinstance(parent,LexicalEnvironment)
        self.parent = parent
        self.__symbols = {}
        self.__indices = {}


    def is_defined( self, sym ):
        assert isinstance(sym,Symbol)
        return sym in self.__symbols or (self.parent and self.parent.is_defined(sym))


    def get_binding(self, sym):
        '''
        Returns value bound to symbol (or None if unbound). Raises IllegalSymbolException
        if sym not in environment
        '''
        assert isinstance(sym,Symbol)
        if sym in self.__symbols:
            return self.__symbols[sym]
        elif self.parent:
            return self.parent.get_binding(sym)
        else:
            raise IllegalSymbolException(sym,
                                         "'%s' is not declared as %s "%(sym,
                                                                       prefix_with_ida(sym.type)))


    def bind_symbol( self, sym, value, allow_override=False ):
        assert isinstance(sym,Symbol)
        assert value is not None

        if not allow_override and self.is_defined(sym):
            raise IllegalSymbolException(sym,"%s '%s' already defined, previous definition at "
                                         "%s"%(sym.type,
                                               sym.id,
                                               self.find_defined_symbol(sym).location_str()))
        self.__symbols[sym] = value


    def find_environment( self, sym ):
        assert isinstance(sym,Symbol)
        if sym in self.__symbols: return self
        elif self.parent: return self.parent.find_environment(sym)
        else: raise IllegalSymbolException(sym,'%s not defined'%sym)
   

    def find_defined_symbol( self, sym ):
        env = self.find_environment(sym)
        for s in env.__symbols:
            if s==sym:
                return s
        assert False,"can't happen"


    def get_index( self, sym ):
        if sym in self.__indices:
            index = self.__indices[sym]
        elif self.parent:
            index = self.parent.get_index(sym)
        else:
            index = 0
        self.__indices[sym] = index
        return index


    def find_indexed( self, sym, index, type=None ):
        return self.find_defined_symbol(Symbol.make_indexed(sym,index,type))


    def inc_index(self, sym):
        if sym not in self.__indices:
            self.__indices[sym]=0
        self.__indices[sym]+=1


    def get_locally_defined_symbols( self ):
        return self.__symbols.keys()
    

    def get_defined_symbols( self ):
        if self.parent:
            xs = self.parent.get_defined_symbols()
            for x in self.get_locally_defined_symbols():
                if x in xs: xs.remove(x)
                xs.append(x)
            return xs
        else:
            return self.get_locally_defined_symbols()


    def __str__( self ):
        s = "\n".join(["%-25s : %-16s -> %s"%(str(sym.id),str(sym.type),repr(val)) for (sym,val) in self.__symbols.items()])
        if self.parent:
            parents = []
            x = self.parent
            while x: parents,x = parents+[x],x.parent
            sp = "    "*len(parents)
            return str(self.parent) + "\n" + sp + s.replace("\n","\n"+sp)
        else:
            return s

    def __getitem__( self, sym ):
        try:
            return self.get_binding(sym)
        except IllegalSymbolException,e:
            raise KeyError(str(e))

        
    def __setitem__( self, sym, value ):
        assert isinstance(sym,Symbol)
        if sym in self.__symbols:
            self.__symbols[sym] = value
        elif self.parent:
            self.parent.__setitem__(sym,value)
        else:
            raise KeyError("'%s' not bound"%sym)


    def __iter__( self ):
        return self.get_defined_symbols().__iter__()
