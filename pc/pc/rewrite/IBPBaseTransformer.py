from pc.parsing.ParseError import ParseError, ParseException
from pc.rewrite.AbstractTransformer import AbstractTransformer
from pc.semantic.SemanticChecker import SemanticChecker
from pc.parsing.AST import ParentNode, LeafNode
from pc.parsing.AST import filter_nodes, find_nodes_of_type
from pc.rewrite.trs import StringSourceGenerator

from pc.parsing.AST import tree_to_str
from pc.pp.IBP_Pretty_Print import IBP_Pretty_Printer 

class IBPTransformer(AbstractTransformer):
    '''
    Class that does some prliminary work in transforming
    ibp programs. The outcome remains a valid ibp program.
    '''

    def __init__(self):
        ''' Default constructor. '''
        AbstractTransformer.__init__(self)

    def _init_passes(self):
        ''' Overridden '''
        self.passes.append('reset_symtables')
        self.passes.append('semantic')
        self.passes.append('trs_pre_wp_ibp')
        self.passes.append('trs_invariants_ibp')
        
    def _pass_reset_symtables(self):
        ''' Resets gensym tables. This is done once per transform '''
        StringSourceGenerator.reset_gensyms()

    def _pass_semantic(self):
        ''' Semantic check pass '''
        checker = SemanticChecker()
        errs = checker.check(self.tree)
        if errs:
            raise ParseException(errs)

    def _pass_trs_pre_wp_ibp(self):
        ''' Do wp transformation with trs '''
        self._rewrite_with_rules('pre-wp-rules_ibp.trs')

    def _pass_trs_invariants_ibp(self):
        ''' Do invariant transformation with trs '''
        self._rewrite_with_rules('inv-rules_ibp.trs')
        #print '1.'
        #print tree_to_str(self.tree)
        #print IBP_Pretty_Printer().output_to_string(self.tree)
