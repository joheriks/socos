from pc.parsing.AST import tree_to_str
from pc.rewrite.trs import TRS
from pc.config import RULE_DIR
import os.path

class AbstractTransformer(object):
    ''' Class that applies different transformation operations to a tree '''

    def __init__(self):
        ''' Default constructor '''

        # Passes is a list of strings (names of passes to do)
        # transform() will call methods named _pass_$passname on self
        # in the order specified in passes.
        self.passes = []
        # Current program tree
        self.tree = None
        # Internal pretty printer
        self.pretty_printer = None
        # trs instance
        self.trs = TRS()

        self._init_passes()

    def _init_passes(self):
        ''' Initializes self.passes. You may override this in subclasses '''
        pass

    def transform(self, tree):
        '''
        Performs transformations specified in this class on tree.
        Returns the transformed tree.
        '''
        self.tree = tree

        methods = map(lambda x : '_pass_' + x, self.passes)
        try:
            for method in methods:
                # check that we actually have this method
                if not hasattr(self, method):
                    msg = 'Internal error, no such method in transformer: %s' % method
                    raise AttributeError(msg)
                else:
                    # call method on self
                    getattr(self, method)()
            return self.tree
        finally:
            self.tree = None

    def transform_and_print(self, tree, outf):
        '''
        Calls transform, and pretty prints the result tree, with each
        outputline passed to outf. If self.pretty_printer is not set,
        the tree is printed with a generic tree printer.
        '''
        if self.pretty_printer:
            self.pretty_printer.output(self.transform(tree), outf)
        else:
            tree_str = tree_to_str(self.transform(tree))

            for line in tree_str.split('\n'):
                outf(line)

    def _rewrite_with_rules(self, filename):
        '''
        Rewrites self.tree with trs rules read from RULE_DIR/filename.
        '''
        rule_file = os.path.join(RULE_DIR, filename)
        self.trs.load_rules_from_file(rule_file)
        self.tree = self.trs.rewrite_tree(self.tree)

