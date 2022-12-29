from pc.pp.SEXP_PP import *


class PVS_Theory_PP( SEXP_PP ):

    def node_theory( self, node ):
        self._process(node.children[0])
        self._add_to_buffer(
            ':', LBV, self._format('THEORY'), LB_begin, self._format('BEGIN'))
        self._next_line()
        if node.children:
            self.indent_more()
            map(self._process, node.children[1:])
            self.indent_less()
        
        self._add_to_buffer(self._format('END'), LBV)
        self._process(node.children[0])
        self._add_to_buffer(LBV)
        self._next_line()


    def node_var( self, node ):
        self.node_list_of_items(node,end=-1)
        self._add_to_buffer(':', LBV,self._format("VAR"), LBV)
        self._process(node.children[-1])
        self._add_to_buffer(';')
        self._next_line()


    def node_ID( self, node ):
        self._add_to_buffer(node.value)


    def node_importing( self, node ):
        self.block = 'importing'
        self._add_to_buffer(self._format('IMPORTING'), LBV)
        self.node_list_of_items(node)
        self._add_to_buffer(';')
        self._next_line()


    def node_theory_name( self, node ):
        self._process(node.children[0])
        assert 1 <= len(node.children) <= 3
        if len(node.children) > 1:
            if node.children[1].type == 'ID':
                self._add_to_buffer('@')
                self._process(node.children[1])
            else:
                self._process(node.children[1])
            if len(node.children) > 2:
                self._process(node.children[2])


    def node_vardecl( self, node ):
        if self.block != 'vardecl':
            self._next_line()
            self.block = 'vardecl'
        self.node_list_of_items(node,end=-1)
        self._add_to_buffer(':', LBV, "var",LBV)
        self._process(node.children[-1])
        self._add_to_buffer(';')
        self._next_line()


    def node_constdecl_binding( self, node ):
        self._next_line()

        self.block = 'constdecl_binding'
        self._process(node.children[0])
        
        i = 1
        while node.children[i].type=='bindings':
            self._process(node.children[i])
            i += 1

        self._add_to_buffer(':',LBV)
        self._process(node.children[i])
        i += 1

        if i<len(node.children):
            self._add_to_buffer(LBV, '=', LBF)
            self.indent_more()
            self._process(node.children[-1])
            self._add_to_buffer(';')
            self._next_line()
            self.indent_less()
        else:
            self._add_to_buffer(';')
            self._next_line()


    def node_constdecl_list( self, node ):
        self._next_line()

        self.block = 'constdecl_list'

        i = 0
        while node.children[i].type=='ID':
            i += 1
        self.node_list_of_items(node,0,i)

        self._add_to_buffer(':',LBV)
        self._process(node.children[i])
        i += 1

        if i<len(node.children):
            self._add_to_buffer(LBV, '=', LBF)
            self.indent_more()
            self._process(node.children[-1])
            self._add_to_buffer(';')
            self._next_line()
            self.indent_less()
        else:
            self._add_to_buffer(';')
            self._next_line()


    def node_recdecl_binding( self, node ):
        self._next_line()
        self.block = 'recdecl_binding'
        self._process(node.children[0])
        i = 1
        while node.children[i].type=='bindings':
            self._process(node.children[i])
            i += 1
        self._add_to_buffer(':',LBV)
        self._add_to_buffer('RECURSIVE',LBV)
        self._process(node.children[i])
        i += 1
        self._add_to_buffer(LBV, '=', LBF)
        self.indent_more()
        self._process(node.children[i])
        i += 1
        self._next_line()
        self._add_to_buffer('MEASURE',LBV)
        self._process(node.children[i])
        i += 1
        if i<len(node.children):
            self._add_to_buffer(LBF,'BY',LBV)
            self._process(node.children[i])
            i += 1
        self._add_to_buffer(';')
        self._next_line()
        self.indent_less()


    def node_recdecl_list( self, node ):
        self._next_line()
        self.block = 'recdecl_list'

        i = 0
        while node.children[i].type=='ID':
            i += 1
        self.node_list_of_items(node,0,i)
        
        self._add_to_buffer(':',LBV)
        self._add_to_buffer('RECURSIVE',LBV)
        self._process(node.children[i])
        i += 1
        self._add_to_buffer(LBV, '=', LBF)
        self.indent_more()
        self._process(node.children[i])
        i += 1
        self._next_line()
        self._add_to_buffer('MEASURE',LBV)
        self._process(node.children[i])
        i += 1
        if i<len(node.children):
            self._add_to_buffer(LBF,'BY',LBV)
            self._process(node.children[i])
            i += 1
        self._add_to_buffer(';')
        self._next_line()
        self.indent_less()
        

    def node_lemma( self, node ):
        self._next_line()
        self.block = 'lemma'
        self._process(node[0])
        self._add_to_buffer(':',LBV)
        kw = node.type.upper()
        self._add_to_buffer(kw, LBV)
        #self._next_line()
        self.indent_more()
        self._process(node[1])
        self.indent_less()
        self._next_line()

    node_axiom = node_lemma
    node_challenge = node_lemma
    node_claim = node_lemma
    node_conjecture = node_lemma
    node_corollary = node_lemma
    node_fact = node_lemma
    node_law = node_lemma
    node_postulate = node_lemma
    node_proposition = node_lemma
    node_sublemma = node_lemma
    node_theorem = node_lemma


    def node_proof( self, node ):
        self.block = 'proof'
        self.begin_prefixed_region("%|-")
        self._process(node["ID"][0])
        self._add_to_buffer(':')
        self._add_to_buffer('proof')
        self._next_line()
        self.indent_more()
        self.node_list_of_items(node,start=1,separator=" ")
        self._add_to_buffer(' ')
        self._add_to_buffer('QED')
        self.indent_less()
        self.end_prefixed_region()
    


    def node_datatype( self, node ):
        self.block = 'datatype'
        self._process(node.children[0])
        self._add_to_buffer(
            ':', LBV, self._format('DATATYPE'), LB_begin, self._format('BEGIN'))
        self._next_line()
        if node.children:
            self.indent_more()
            map(self._process, node.children[1:])
            self.indent_less()
        
        self._add_to_buffer(self._format('END'), LBV)
        self._process(node.children[0])
        self._add_to_buffer(LBV)
        self._next_line()


    def node_constructor( self, node ):
        self._process(node.children[0])
        i = 1
        while node.children[i].type=='bindings':
            self._process(node.children[i])
            i += 1
        self._add_to_buffer(':',LBV)
        self._process(node.children[i])
        self._add_to_buffer(LBV)
        self._next_line()
