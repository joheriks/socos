from pc.parsing.ParseError import ParseError, ParseException
from pc.rewrite.AbstractTransformer import AbstractTransformer
from pc.semantic.SemanticChecker import SemanticChecker
from pc.parsing.AST import ParentNode, LeafNode
from pc.parsing.AST import filter_nodes, find_nodes_of_type
from pc.rewrite.trs import StringSourceGenerator

from pc.parsing.AST import tree_to_str
from pc.pp.IBP_Pretty_Print import IBP_Pretty_Printer 

class Transformer(AbstractTransformer):
    '''
    Class that does most of the work in transforming
    ibp programs to pvs theories.
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
        
        self.passes.append('trs_pre_wp')
        self.passes.append('trs_wp')
        self.passes.append('old_rewrite')
        self.passes.append('trs_invariants')
        self.passes.append('inv_rewrite')
        self.passes.append('trs_clean')
        self.passes.append('sanity_check')

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
        
    def _pass_trs_pre_wp(self):
        ''' Do wp transformation with trs '''
        self._rewrite_with_rules('pre-wp-rules.trs')

    def _pass_trs_wp(self):
        ''' Do wp transformation with trs '''
        self._rewrite_with_rules('wp-rules.trs')

    def _pass_old_rewrite(self):
        """ Rewrite OLD(x) to use a variable x' """
        rewrite_old(self.tree)

    def _pass_trs_invariants(self):
        ''' Do invariant transformation with trs '''
        self._rewrite_with_rules('inv-rules.trs')

    def _pass_inv_rewrite(self):
        ''' Rewrite inv stuff in proofconditions '''
        rewrite_invariants(self.tree)

    def _pass_trs_clean(self):
        ''' Do clean transformation with trs '''
        self._rewrite_with_rules('clean-rules.trs')

    def _pass_sanity_check(self):
        ''' Checks certain facts about previous transformation '''

# TODO: here comes the transformation for calls... 
# or maybe in a previous stage... either way this has to be removed
        # check that we don't have procedure calls

        calls = find_nodes_of_type(self.tree, 'call_statement', True)
        if calls:
            error = ParseError.make_from_node(
                'CALL translation not implemented yet',
                calls[0])
            raise ParseException([error])

# END Transformer

# Hand-written transformation functions used in transformer:

def rewrite_old(module):
    ''' Replace OLD(x) with a variable x' '''

    # Create a map from variable names x to x'
    name_map = { }

    # First, do module level variables
    for inits in module.children_by_type('init_progvar'):
        original, new = inits.children

        assert len(original.children) == len(new.children)

        for var, new_var in zip(original.children, new.children):
            original_name = var.children[0].value
            new_name = new_var.children[0].value
            name_map[original_name] = new_name

    # For each procedure, add value/valres arguments to name_map
    # and rewrite OLD(x) using name_map to x'

    for proc in filter_nodes(lambda x : x.type in ('procedure', 'pprocedure'),
                             module):
        # Create copy of name_map for each procedure
        proc_map = name_map.copy()

        # Process args

        args = proc.children_by_type('args')
        if args:
            val_args = filter_nodes(lambda x : x.type in ('parg', 'pparg'),
                                    args[0])
            for arg in val_args:
                original, new = arg.children

                original_names = original.children_by_type('ID')
                new_names = new.children_by_type('ID')

                assert len(original_names) == len(new_names)

                for o_name, n_name in zip(original_names, new_names):
                    proc_map[o_name.value] = n_name.value

        def old_matcher(node):
            return (node.type == 'expression_arguments'
                    and node.children[0].type == 'name'
                    and len(node.children[0].children) == 1
                    and node.children[0].children[0].type == 'idop'
                    and node.children[0].children[0].children[0].type == 'ID'
                    and node.children[0].children[0].children[0].value == 'OLD')

        # Do replacement

        applications = filter_nodes(old_matcher, proc, True)
        for application in applications:
            app_arg = application.children[1].children[0]
            app_arg_name = ''

            if (app_arg.type != 'name'
                or app_arg.children[0].type != 'idop'
                or app_arg.children[0].children[0].type != 'ID'):
                error = ParseError.make_from_node('OLD not applied to progvar',
                                                      application)
                raise ParseException([error])
            else:
                app_arg_name = app_arg.children[0].children[0].value

            if not proc_map.has_key(app_arg_name):
                error = ParseError.make_from_node(
                            'OLD not applied to module/value/valres progvar',
                            application)
                raise ParseException([error])

            application.type = 'name'
            application.children = app_arg.children
            app_arg.children[0].children[0].value = proc_map[app_arg_name]


def rewrite_invariants(module):
    ''' Rewrites dummy invariant references (inv nodes) in proof conditions '''

    inv_decls = find_nodes_of_type(module, 'situation_invariant', True)

    def get_invariant_decl(proc, situation):
        sources = map(lambda x : (x.children[0], x), inv_decls)

        invs = [ inv for source, inv in sources
                 if source.children[0].value == 'inv' \
                    and source.children[1].value == proc \
                    and source.children[2].value == situation ]

        assert invs
        assert len(invs) == 1

        return invs[0]

    def get_module_invariant_decl():
        sources = map(lambda x : (x.children[0], x), inv_decls)

        invs = [ inv for source, inv in sources
                 if source.children[0].value == 'modinv' ]

        assert invs
        assert len(invs) == 1

        return invs[0]

    def get_invariant_refs(node):
        parents = filter_nodes(lambda n : n.children and n.children_by_type('inv'),
                               node,
                               True)
        refs = []

        for parent in parents:
            for child in parent.children:
                if child.type == 'inv':
                    refs.append((parent, child))

        return refs

    def get_invariant_appl_node(inv_node):
        expr = ParentNode('expression_arguments', [])

        appl_id = LeafNode('ID', '')
        appl_id_lst = []

        for id in inv_node.children[0].children:
            appl_id_lst.append(id.value)

        appl_id.value = '_'.join(appl_id_lst)

        name = ParentNode('name', [ ParentNode('idop', [ appl_id ]) ])
        expr.children.append(name)

        args = ParentNode('arguments', [])
        expr.children.append(args)

        arglist = []
        for id in inv_node.grandchildren_by_type('args'):
            arglist.append(ParentNode('name',
                                      [ ParentNode('idop',
                                                   [ LeafNode('ID', id.value) ]) ]))

        if arglist:
            args.children = arglist
        else:
            # No args, just use the name
            expr = name

        if appl_id_lst[-1][:4] in ('POST', 'PRE'):
            # include module invariant if POST or PRE
            mod_inv = get_module_invariant_decl()
            mod_inv_node = get_invariant_appl_node(mod_inv)
            expr = ParentNode('pgroup',
                              [ ParentNode('KEY_AND', [ expr, mod_inv_node ]) ] )
        return expr

    def replace_inv_in_pc(proc_id, pc_node):
        for parent, inv_ref in get_invariant_refs(pc_node):
            inv = get_invariant_decl(proc_id, inv_ref.value)
            inv_repl = get_invariant_appl_node(inv)

            for i, child in enumerate(parent.children):
                if child is inv_ref:
                    parent.children[i] = inv_repl

    procedures = filter_nodes(lambda x : x.type in ('procedure', 'pprocedure'),
                              module)

    for proc in procedures:
        proc_id = proc.children[0].value

        body = proc.children_by_type('procedure_body')[0]
        body_pc = body.children_by_type('proof_condition')
        if body_pc:
            assert len(body_pc) == 1
            replace_inv_in_pc(proc_id, body_pc[0])

        situations = filter_nodes(lambda x : x.type in ('situation', 'psituation'),
                                  proc,
                                  True)

        for situation in situations:
            for pc in situation.children_by_type('proof_condition'):
                replace_inv_in_pc(proc_id, pc)
