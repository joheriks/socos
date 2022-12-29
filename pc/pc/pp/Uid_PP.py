from pc.pp.PP import *


class Uid_PP( PP ):

    def __init__( self, separator="/" ):
        PP.__init__(self,max_line_length=32000)
        self._separator = separator


    def node_uids( self, uids ):
        for i in range(len(root.children)):
            self._process(root.children[i])
            if i<len(root.children)-1:
                self._add_to_buffer(";")
                self._next_line()


    def node_uid( self, uid ):
        self.node_list_of_items(uid, separator=self._separator)


    def node_uidpart( self, uidpart ):
        self.node_list_of_items(uidpart, separator=":")

            
    def node_ID( self, node ):
        self._add_to_buffer(node.value)


    def node_STAR( self, node ):
        self._add_to_buffer(node.value)
        
