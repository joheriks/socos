from pc.util.Utils import *

from pc.semantic.LexicalEnvironment import *
from pc.semantic.Message import *

from pc.parsing.PVSUtils import *

from pc.pp.Uid_PP import *


# Default value for verify attribute.
verify_default = True



class Element( object ):

    """
    Base class for elements. Represents a program element that may be
    associated with an ID in a lexical environment.
    """

    def __init__( self, filename, env ):
        self.filename = filename
        self.env = env
        self.id = None
        self.verify = verify_default
        self._idcounters = {}


    def get_idtype( self ):
        raise Exception("Not implemented for "+repr(self))


    def set_id( self, id, raise_programexception=True, ast=None, override=False ):
        assert isinstance(id,Symbol)
        self.id = id
        try:
            self.env.bind_symbol(self.id,self,override)
        except IllegalSymbolException,e:
            if raise_programexception:
                line,pos = (ast.start_line(),ast.start_pos()) if ast else (-1,-1)
                raise ProgramException([FileError(str(e),self.filename,line,pos)])
            else:
                raise


    def set_id_from_node( self, node, raise_programexception=True, override=False ):
        assert isinstance(node,LeafNode) and node.type=='ID'
        self.set_id(Symbol.from_node(node,self.get_idtype(),self.filename),
                    raise_programexception=raise_programexception,override=override)


    def set_id_from_str( self, idstr, raise_programexception=True, override=False ):
        assert type(idstr)==str
        self.set_id(Symbol(idstr,self.get_idtype(),self.filename),
                    raise_programexception=raise_programexception,override=override)
      

    def get_id_str( self ):
        assert self.id
        return self.id.id


    def get_ID( self ):
        return ID(self.get_id_str())


    def idref( self, idnode, typ ):
        assert isinstance(idnode,LeafNode) and idnode.type=="ID"
        try:
            ref = self.env.get_binding(Symbol.from_node(idnode,typ,self.filename))
        except IllegalSymbolException,e:
            raise ProgramException([ElementError(str(e),self)])
        return ref


    def idref_str( self, idstr, typ ):
        assert type(idstr) in (str,unicode)
        try:
            ref = self.env.get_binding(Symbol(idstr,typ,self.filename))
        except IllegalSymbolException,e:
            raise ProgramException([ElementError(str(e),self)])
        return ref 


    def get_prev_uid_part( self ):
        # Returns the element which binds this element to a symbol
        return None


    def get_next_uid_env( self ):
        # Returns the environment in which this element binds nested elements to symbols
        return None


    def get_uid( self ):
        return Uid_PP().output_to_string(self.get_uid_ast())


    def get_uid_ast( self ):
        asts = [ParentNode("uidpart",[LeafNode("ID",self.get_id_str()),
                                      LeafNode("ID",self.id.type)])]
        if self.get_prev_uid_part():
            xs = self.get_prev_uid_part().get_uid_ast()
            asts = xs.children + asts
        return ParentNode("uid",asts)

        
    def match_uid( self, uid ):
        assert uid.type=="uid"
        elem = self
        env = self.env
        for uidpart in uid.children:
            if env==None:
                raise MatchException("no match for '%s'"%id.value)

            #uidpart = uid.children[i]
            if len(uidpart.children)==2: id,ns = uidpart.children
            elif len(uidpart.children)==1: id,ns = uidpart.children[0],None
            candidates = [s for s in env if (s.id==id.value and (ns==None or s.type==ns.value)) ]
            if len(candidates)!=1:
                raise MatchException("no match for '%s', or ambiguous"%id.value)
            elem = env[candidates[0]]
            assert isinstance(elem,Element)

            env = elem.get_next_uid_env()
            
        return elem


    def init_id_counter( self, key, idtype, prefix ):
        self._idcounters[key] = (idtype,prefix,1)


    def get_next_id( self, key ):
        idtype,prefix,idx = self._idcounters[key]
        self._idcounters[key] = idtype,prefix,idx+1
        return Symbol("%s%s"%(prefix,idx),idtype,self.filename)


    def get_strategy( self ):
        raise Exception("Not implemented for "+repr(self))
       

    def __repr__( self ):
        return self.get_uid()
