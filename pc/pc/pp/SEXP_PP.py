from pc.pp.PP import *
from pc.util.Utils import *


class SEXP_PP( PP ):

    def node_sexp_list( self, node ):
        if not node.children:
            self._add_to_buffer("()")
        else:
            ml = False
            self._add_to_buffer("(")
            self._process(node.children[0])
            if len(node.children)>1:
                self._add_to_buffer(" ")
                ml = some(lambda n:len(n.children)>1,node.children)
                if ml:
                    sep = (" ",LBF)
                    self._next_line()
                    self.indent_more()
                else:
                    sep = (" ",LBV)
                self.node_list_of_items(node,start=1,separator=sep)
            self._add_to_buffer(")")
            if ml: self.indent_less()


    def node_sexp_symbol( self, node ):
        self._add_to_buffer(node.value)


    def node_sexp_number( self, node ):
        self._add_to_buffer(str(node.value))

        
    def node_sexp_string( self, node ):
        self._add_to_buffer('"')
        self._add_to_buffer(node.value.replace('"',r'\"'))
        self._add_to_buffer('"')


    def node_sexp_pvs( self, node ):
        self._add_to_buffer('"')
        self._add_to_buffer(self.sexp_pvs_string_pp.output_to_string(node.children[0]).replace('"',r'\"').replace("\n",""))
        self._add_to_buffer('"')
