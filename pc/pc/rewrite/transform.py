from pc.rewrite.AbstractTransformer import AbstractTransformer
from pc.pp.TRS_Pretty_Print import TRS_Pretty_Printer
from pc.parsing.AST import filter_nodes
from pc.pp.IBP_Pretty_Print import IBP_Pretty_Printer

class TransformPrettyPrinter(TRS_Pretty_Printer):
    ''' Prints Spec and Impl trees '''

    def __init__(self,
                 max_line_length=160,
                 uppercase_keywords=True,
                 indent_amount=4,
                 break_at_begin=False):
        ''' Constructor. See TRS_Pretty_Printer '''

        TRS_Pretty_Printer.__init__(self,
                                    max_line_length,
                                    uppercase_keywords,
                                    indent_amount,
                                    break_at_begin)

    def node_TOP(self, node):
        for child in node.children:
            assert child.type == 'module'
            self._process(child)
            self._next_line()

class CallsTransformer(AbstractTransformer):
    '''
    Transforms a tree (which should have been transformed with
    BaseTransformer.Transformer) into /specification/ theories
    for an ibp program
    '''

    def __init__(self):
        ''' Default constructor '''
        AbstractTransformer.__init__(self)
        self.pretty_printer = IBP_Pretty_Printer()
        self.passes.append('calls_rewrite')

    def _pass_calls_rewrite(self):
        ''' Expands procedure calls '''

        for node in filter_nodes(lambda x : x.type.endswith('module'), self.tree):
            node.type = 'module'

class SpecTransformer(AbstractTransformer):
    '''
    Transforms a tree (which should have been transformed with
    BaseTransformer.Transformer) into /specification/ theories
    for an ibp program
    '''

    def __init__(self):
        ''' Default constructor '''
        AbstractTransformer.__init__(self)
        self.pretty_printer = TransformPrettyPrinter()
        self.passes.append('spec_rewrite')

    def _pass_spec_rewrite(self):
        ''' Turns tree into spec components '''
        self._rewrite_with_rules('spec-rules.trs')
        for node in filter_nodes(lambda x : x.type.endswith('module'), self.tree):
            node.type = 'module'

class ImplTransformer(AbstractTransformer):
    '''
    Transforms a tree (which should have been transformed with
    BaseTransformer.Transformer) into /implementation/ theories
    for an ibp program.

    Note: if you intend to use both Impl and Spec transformer on a
    tree, you must first clone the input tree. Both spec and impl
    cannot be applied to the same tree after each other.
    '''

    def __init__(self):
        ''' Default constructor '''
        AbstractTransformer.__init__(self)
        self.pretty_printer = TransformPrettyPrinter()
        self.passes.append('impl_rewrite')

    def _pass_impl_rewrite(self):
        ''' Turns tree into impl components '''
        self._rewrite_with_rules('impl-rules.trs')
        for node in filter_nodes(lambda x : x.type.endswith('module'), self.tree):
            node.type = 'module'
