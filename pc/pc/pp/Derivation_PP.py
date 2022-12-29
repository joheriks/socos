from pc.pp.PP import *
from pc.util.Utils import *


class Derivation_PP( PP ):

    def node_derivation( self, node ):
        self._add_to_buffer("...")


    def node_term( self, node ):
        self._process(node.children[0])


    def node_stepdetails( self, node ):
        self._process(node["relation"][0][0])
        self._add_to_buffer(" { ",LBV,LBV," } ")
        print 

    def node_chain( self, node ):
        terms = node["term"]
        term1 = terms[0]
        stepdetails = node["stepdetails"][0]
        relation = node["stepdetails.relation"][0][0]

        term2 = None
        if len(terms)==2: term2 = terms[1]
        if node["chain.term"]: term2 = node["chain.term"][0]

        relation_string = self.pvs_pp.output_to_string(relation)
        indent = len(relation_string)+1
        
        self.indent_more(indent)
        self._process(term1)
        self._next_line()
        self.indent_less(indent)
        
        self._process(stepdetails)
        self._next_line()

        if term2:
            self.indent_more(indent)
            self._process(term2)
            self._next_line()
            self.indent_less(indent)
