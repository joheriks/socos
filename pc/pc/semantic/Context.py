from pc.parsing.IBPParser import IBPParser
from pc.util.Utils import *
from pc.parsing.AST import tree_to_str,tree_equal

from pc.semantic.Message import *
from pc.semantic.Element import *

import os.path


# In PVS, constants can be overloaded. This method is used to keep
# track of the bound constant names, so that we can check for clashes
# with variable names.  We do not care about the same constant being
# defined several times, if there is an ambiguity PVS will detect it.

def bind_constdecls( constdecls, filename, env ):
    for c in constdecls:
        if c.type=="constdecl_list":
            i = 0
            while c.children[i].type=="ID":
                s = Symbol.from_node(c.children[i],"constant",filename)
                if not env.is_defined(s):
                    env.bind_symbol(s,"constant-declaration")
                i += 1
        elif c.type=="constdecl_binding":
            s = Symbol.from_node(c.children[0],"constant",filename)
            if not env.is_defined(s):
                env.bind_symbol(s,"constant-declaration")


def make_variable_bindings( ast, filename, env ):
    assert ast.type in ("vardecl","pvardecl")
    return [VariableBinding(filename,env,ast,
                            Symbol.from_node(x,"variable",filename),ast[-1])
            for x in ast.children[0:-1]]



class StrategyElement( Element ):

    def init_strategy( self, ctx, ast ):
        # Strategy (1st is for primary conditions, 2nd for TCCs)
        if ast and ast["try"]:
            self.trystrategy = Try(ctx,ast["try"][-1],Symbol("context_strategy","command",self.filename))
        else:
            self.trystrategy = None
        
    def get_strategy( self ):
        s = self.get_default_strategy()
        if self.trystrategy:
            s = self.trystrategy.get_updated_strategy(s)
        return s
   
    # return default strategy
    def get_default_strategy( self ):
        raise Exception("Not implemented")


    def idref_check( self ):
        if self.trystrategy:
            self.trystrategy.idref_check()
            


class Context( StrategyElement ):

    def __init__( self, ibpenv, ast, filename ):
        self.ast = ast
        self.ibpenv = ibpenv
        self.nestedenv = LexicalEnvironment(ibpenv.get_lexical_environment())
        super(Context,self).__init__(filename,ibpenv.get_lexical_environment())
        
        # bind id of context, if id is already bound there may be an
        # extends cycle
        idast = self.ast["ID"][0]
        try:
            self.set_id_from_node(idast,False)
        except IllegalSymbolException,e:
            raise ProgramException([FileError("context '%s' already defined, "
                                              "or there is an extension "
                                              "loop"%idast.value,
                                              filename,
                                              idast.start_line(),
                                              idast.start_pos())])

        # find extended contexts
        self.extends = []
        for e in self.ast["extending","context_name"]:
            ctx = Symbol.from_node(e["ID"][0],"context",filename)
            if not ibpenv.get_context(ctx.id):
                raise ProgramException([FileError("context '%s' not found"%ctx.id,
                                                  filename,
                                                  e["ID"][0].start_line(),
                                                  e["ID"][0].start_pos())])
            self.extends.append(ibpenv.get_context(ctx.id))

        constdecls = []
        self.declarations = []
        self.logicals = []
        for x in self.ast.children:
            if x.type=="importing":
                self.declarations.append(x)
            if x.type in ("constdecl_list","constdecl_binding",
                          "recdecl_list","recdecl_binding"):
                self.declarations.append(x)
                constdecls.append(x)
            if x.type in ("vardecl","pvardecl"):
                self.declarations.append(x)
                self.logicals += make_variable_bindings(x,self.filename,self.nestedenv)
        bind_constdecls(constdecls,self.filename,self.nestedenv)
        # process procedures
        self.procedures = map(lambda p:Procedure(self,p),self.ast["procedure"])

        # process derivations
        from pc.semantic.Derivation import Derivation
        self.derivations = map(lambda d:Derivation(self,d),self.ast["derivation"])

        # recursive components will be recorded in these attributes during semantic check
        self._reccomps = []
        self._verify_reccomp = []

        self.init_strategy(self,ast)
        

    # get_strategy provided by StrategyElement

    def get_default_strategy( self ):
        if self.extends: return self.extends[0].get_strategy()
        else: return self.ibpenv.get_strategy()

    def is_prelude( self ):
        return self.ibpenv.is_prelude(self)


    def get_idtype( self ):
        return "context"


    def get_attribute( self ):
        return "context"


    def get_next_uid_env( self ):
        return self.nestedenv


    def idref_check( self ):
        # Custom strategies
        if self.trystrategy:
            self.trystrategy.idref_check()

        for p in self.procedures:
            if filter(lambda x:x is not None,
                      map(lambda x:x.get_procedure_by_name(p.get_id_str()),
                          self.get_extends_transitive())):
                raise ProgramException([ElementError("procedure '%s' already "
                                                     "declared"%(p.get_id_str()),
                                                     p)])
            p.idref_check()

        for d in self.derivations: d.idref_check()


    def semantic_check( self, warnings ):
        # Check procedues
        for p in self.procedures: p.semantic_check(warnings)

        # Find call components
        self._reccomps = find_sccs(self.procedures,
                                   lambda x:x.get_called_procedures())

        # Check that mutually recursive procedures have a common signature
        errors = []
        for cc in self._reccomps:
            reclist = ",".join(map(lambda x:"'%s'"%x.get_id_str(),cc))
            if some(lambda x:x.variant,cc):
                self._verify_reccomp.append(True)
                if len(cc)>1:
                    for proc in cc[1:]:
                        if not proc.compare_signature(cc[0]):
                            errors.append(ImpreciseError("mutually recursive procedures "
                                                         "[%s] must have syntactically "
                                                         "identical signatures "
                                                         "(parameters and variant)"%reclist,
                                                         self))
                            break
            else:
                self._verify_reccomp.append(False)
                if len(cc)>1:
                    msg = "mutually recursive procedures [%s] may not be terminating"%reclist
                else:
                    msg = "recursive procedure '%s' may not be terminating"%cc[0].get_id_str()
                warnings.append(ElementWarning(msg,self))
        if errors:
            raise ProgramException(errors)

        # Check derivation
        for der in self.derivations: der.semantic_check(warnings)


    def in_same_component( self, p1, p2 ):
        return some(lambda cc:p1 in cc and p2 in cc,self._reccomps)


    def semantic_translate( self ):
        for p in self.procedures: p.semantic_translate()


    def get_extends_transitive( self ):
        return self.extends + mapcan(Context.get_extends_transitive,self.extends)
    

    def get_extends_reflexive_transitive( self ):
        return [self] + self.get_extends_transitive()


    def get_logicals( self ):
        return filter(lambda x:not some(lambda y:y.id==x.id,self.logicals),self.get_inherited_logicals()) + self.logicals


    def get_inherited_logicals( self ):
        logicals = []
        for ctx in self.extends:
            logicals = filter(lambda x:not some(lambda y:y.id==x.id,ctx.get_logicals()),logicals)
            logicals += ctx.get_logicals()
        return filter(lambda x:not some(lambda y:y.id==x.id,self.logicals),logicals)
    

    def get_procedure_by_name( self, name ):
        # returns None if no procedure by this name
        p = filter(lambda x:x.get_id_str()==name,
                   mapcan(lambda x:x.procedures,
                          self.get_extends_reflexive_transitive()))
        return p[0] if p else None


    def unsafe_commands_allowed( self ):
        return self.ibpenv.unsafe_commands_allowed(self)



class ContextItem( Element ):
    def __init__( self, ctx, ast ):
        Element.__init__(self,ctx.filename,ctx.nestedenv)
        self.ctx = ctx
        self.ast = ast

    def get_strategy( self ):
        return self.ctx.get_strategy()



class Procedure( ContextItem ):

    def __init__( self, ctx, ast ):
        ContextItem.__init__(self,ctx,ast)

        self.nestedenv = LexicalEnvironment(self.env)

        # for anonymous situations
        self.init_id_counter("situation","situation","unnamed__")

        self.set_id_from_node(ast["ID"][0])

        # parameters
        self.params = []  # parameters in declared order
        self.consts = []
        self.valreses = []
        self.results = []
        for param in self.ast["formals","*"]:
            for s in param[0:-1]:
                pid = Symbol(s.value,"variable",self.ctx.filename,
                             s.start_line(),s.start_pos())
                vb = VariableBinding(self.filename,self.nestedenv,param,pid,param[-1])
                if param.type=="const": self.consts.append(vb)
                elif param.type=="valres": self.valreses.append(vb)
                elif param.type=="result": self.results.append(vb)
                self.params.append(vb)
        self.initconsts = [ vr.get_suffixed_copy("__0") for vr in self.valreses ]

        # Specification. The various syntax alternatives for specifying pre- and
        # postconditions makes this a bit complicated, since the declarations may be
        # spread out in any order over the spec subtree.
        self.pre = None
        post = None
        named_posts = []
        for spec in self.ast["spec","*"]:
            if spec.type=="pre":
                #if not self.pre: self.pre = Precondition(self,spec[0] if spec.children else None)
                if not self.pre: self.pre = Precondition(self,spec)
                for x in spec["constraint"]: self.pre.add_constraint_ast(x[0])
            elif spec.type=="post":
                # Processing postconditions. If the node has an identifier create a
                # named postcondition, otherwise assume that all nodes belong to an
                # anonymous postcondition. A mix of anonymous and named postconditions
                # is not allowed and will be flagged as an error below.
                if spec["ID"]:
                    newpost = Postcondition(self,spec[0],
                                            Symbol.from_node(spec["ID"][0],"specification",
                                                             self.ctx.filename))
                    named_posts.append(newpost)
                    for x in spec["constraint"]: newpost.add_constraint_ast(x[0])
                else:
                    #post = Postcondition(self,spec[0] if spec.children else None)
                    post = Postcondition(self,spec)

                    for x in spec["constraint"]: post.add_constraint_ast(x[0])
        if post and named_posts:
            raise ProgramException([ElementError("procedure '%s' declares both anonymous "
                                                 "and named postconditions, which is "
                                                 "disallowed"%self.get_id_str(),self)])
        self.posts = []
        if post: self.posts.append(post)
        self.posts += named_posts

        if not self.pre: self.pre = Precondition(self,None)
        if not self.posts: self.posts.append(Postcondition(self,None))

        self.variant = None
        if self.ast["spec","variant"]:
            if len(self.ast["spec","variant"])>1:
                raise ProgramException([ElementError("procedure '%s' declares superfluous "
                                                       "variant, which is "
                                                       "disallowed"%self.get_id_str(),self)])
            else:
                self.variant = self.ast["spec","variant"][0][0]

        # local vars
        self.locals = []
        for var in self.ast["body","pvardecl"]:
            tdecl = var[-1]
            for vid in var["ID"]:
                self.locals.append(VariableBinding(self.filename,self.nestedenv,var,
                                                   Symbol.from_node(vid,
                                                                    "variable",
                                                                    self.ctx.filename),
                                                   tdecl))
        # local constants
        self.local_consts = []
        for c in self.ast["body","constdecl*"]:
            self.local_consts.append(c)
        bind_constdecls(self.local_consts,self.filename,self.nestedenv)


        # diagram
        self.diagram = Diagram(self,self.ast["body","diagram"][0] if self.ast["body","diagram"]
                               else None)


    def idref_check( self ):
        if self.diagram: self.diagram.idref_check()


    def semantic_check( self, warnings ):
        # check for name clashes between constants and program variables
        for vb in self.consts + self.get_variables():
            if self.nestedenv.is_defined(Symbol(vb.id.id,"constant",self.filename)):
                raise ProgramException([ElementError("identifier '%s' declared as both "
                                                     "constant and variable, which is "
                                                     "ambiguous"%vb.id.id,vb)])

        if self.diagram: self.diagram.semantic_check(warnings)


    def semantic_translate( self ):
        if self.diagram: self.diagram.semantic_translate()


    def get_idtype( self ):
        return "procedure"


    def get_attribute( self ):
        return "procedure"


    def get_next_uid_env( self ):
        return self.nestedenv


    def get_prev_uid_part( self ):
        return self.ctx


    def get_params( self ):
        return self.consts + self.valreses + self.results


    def get_constants( self ):
        return self.consts
    

    def get_variables( self ):
        return self.valreses + self.results + self.locals


    def get_called_procedures( self ):
        return self.diagram.get_called_procedures()


    def compare_signature( self, other ):
        if ((self.variant and not other.variant) or (other.variant and not self.variant)):
            return False
        if self.variant and not tree_equal(self.variant,other.variant):
            return False
        if (len(other.consts) != len(self.consts) or
            len(other.valreses) != len(self.valreses) or
            len(other.results) != len(self.results)):
            return False
        for a,b in zip(self.get_params(),other.get_params()):
            if not a.syntactically_equal(b):
                return False
        return True


    def has_unnamed_post( self ):
        return self.posts[0].get_id_str()=="post__"



class ProcedureItem( Element ):

    def __init__( self, proc, ast ):
        Element.__init__(self,proc.ctx.filename,proc.nestedenv)
        self.proc = proc
        self.ast = ast

    def get_procedure( self ):
        return self.proc

    def get_strategy( self ):
        return self.proc.get_strategy()



class VariableBinding( Element ):

    """A variable that is bound to a type in an environment"""

    def __init__( self, filename, env, ast, id, type ):
        #Binding.__init__(self,filename,env,ast,id,None,type)
        Element.__init__(self,filename,env)
        assert ast==None or isinstance(ast,Node)
        self.type = type
        self.ast = ast
        self.set_id(id,ast=self.ast)


    def syntactically_equal( self, other ):
        return self.id==other.id and tree_equal(self.type,other.type)


    def get_suffixed_copy( self, suffix ):
        new_id = Symbol(self.id.id+suffix,self.id.type,self.id.file,self.id.lineno,self.id.pos)
        return VariableBinding(self.filename,self.env,None,new_id,self.type)


    def __eq__(self, other):
        #self.syntactically_equal(other)
        return isinstance(other, VariableBinding) and self.id==other.id and self.type==other.type



class ConstraintSet( ProcedureItem ):

    def __init__( self, proc, ast, id ):
        ProcedureItem.__init__(self,proc,ast)
        self.set_id(id)
        self.init_id_counter("constraint","constraint",self.get_id_str()+"_")
        self.nestedenv = LexicalEnvironment(self.env)

    def get_constraints( self ):
        raise "Not implemented"

    def get_prev_uid_part( self ):
        return self.proc



class Constraint( Element ):

    def __init__( self, cs, ast ):
        Element.__init__(self,cs.proc.filename,cs.nestedenv)
        self.ast = ast
        self.constraintset = cs
        self.set_id(cs.get_next_id("constraint"))

    def get_idtype( self ):
        return "constraint"

    def get_prev_uid_part( self ):
        return self.constraintset



class SpecItem( ConstraintSet ):

    def __init__( self, proc, ast, id ):
        ConstraintSet.__init__(self,proc,ast,id)
        self.constraints = []

    def get_constraints( self ):
        return self.constraints[:]

    def add_constraint_ast( self, ast ):
        self.constraints.append(Constraint(self,ast))

    def get_idtype( self ):
        return "specification"
   
        

class Precondition( SpecItem ):

    def __init__( self, proc, ast ):
        SpecItem.__init__(self,proc,ast,Symbol("pre__",self.get_idtype(),proc.filename))



class Postcondition( SpecItem ):

    def __init__( self, proc, ast, id=None ):
        SpecItem.__init__(self,proc,ast,id if id else Symbol("post__",self.get_idtype(),proc.filename))


    def is_anonymous( self ):
        return self.get_id_str()=="post__"



class Diagram( ProcedureItem ):

    def __init__( self, proc, ast ):
        ProcedureItem.__init__(self,proc,ast)
        
        self.initial_situation = InitialSituation(proc)
        if self.ast:
            self.toplevels = map(lambda s:IntermediateSituation(proc,s,None),
                                 self.ast["situation"])
        else:
            self.toplevels = []

        self.final_situations = [FinalSituation(proc,p) for p in proc.posts]

        # stores reachability relation, populated during semantic check and used in
        # semantic_translation
        self._reachable = {}
        

    def idref_check( self ):
        for s in self.get_toplevel_situations(): s.idref_check()


    def semantic_check( self, warnings ):
        for s in self.get_toplevel_situations(): s.semantic_check(warnings)

        # reachability checks 
        situations = self.get_all_situations()

        reachable = dict([(x,x.get_reachable_situations()) for x in situations])
        
        # warn about unreachable postconditions 
        for x in situations:
            if x==self.initial_situation or x in reachable[self.initial_situation]:
                continue
            if isinstance(x,FinalSituation):
                if not x.post.is_anonymous():
                    msg = ("unreachable postcondition '%s' in "
                           "procedure '%s'"%(x.post.get_id_str(),
                                             self.proc.get_id_str()))
                else:
                    msg = ("unreachable postcondition in "
                           "procedure '%s'"%self.proc.get_id_str())
                warnings.append(ElementWarning(msg,self.proc))
            else:
                # We do not warn about unreachable situations (cf. uncalled procedures)
                pass
                        

        # warn about reachable situations from which no final situation is reachable
        for x in reachable[self.initial_situation]:
            if x not in self.final_situations and not reachable[x].intersection(self.final_situations):
                warnings.append(ElementWarning("situation '%s' in procedure '%s' is reachable, "
                                               "but no final situation can be reached "
                                               "from it"%(x.get_id_str(),self.proc.get_id_str()),
                                               x))


        # check if the program has cycles
        cyclic = False
        for r in reachable:
            if r in reachable[r]:
                cyclic = True
                break

        # if diagram is cyclic and has decreasing conditions and/or variants, termination
        # should be checked
        self._decomposition = None
        if cyclic:
            if (mapcan(lambda s:[x for x in s.get_trs_leaves() if x.decreasing], situations)
                or [s for s in situations
                    if isinstance(s,IntermediateSituation) and s.variant]):

                self._decomposition = self._find_decomposition(situations,set())

                if self._decomposition==None:
                    raise ProgramException([ElementError("insufficient variants: there is a "
                                                         "cycle in procedure '%s' which does "
                                                         "not necessarily decrease a "
                                                         "variant"%(self.proc.get_id_str()),
                                                         self.proc)])
            else:
                warnings.append(ElementWarning("cycle found, procedure '%s' may not be "
                                               "terminating"%(self.proc.get_id_str()),
                                               self.proc))
        else:
            self._decomposition = None



    def semantic_translate( self ):
        #print "%s: mark as non-increasing:"%self.proc.get_id_str()
        if self._decomposition!=None:
            for g,ni in self._decomposition.items():
                #print "(%s -> %s): {%s}"%(g.parent.get_source_situation().get_id_str(),
                #                          g.target.get_id_str(),
                #                          ",".join(map(lambda a:a.get_id_str(),ni)))
                g.set_nonincreasing(ni)

        for s in self.get_toplevel_situations(): s.semantic_translate()



    def _find_decomposition( cls, sits, decr ):
        # The method is based on the following : suppose that in a
        # strongly connected component there is a situation S such
        # that every cycle through S strictly decreases the associated
        # variant V(S). As V(S) is bounded from below, this precludes
        # any computation in which the situations S or any of the
        # transitions that decrease V(S) occur infinitely
        # often. Having established this, we can reduce the problem of
        # showing that the component terminates by pruning the
        # transitions that strictly decreases V(S), and again
        # decomposing the resulting graph into its strongly connected
        # components.

        # This algorithm recursively looks for a decomposition of the
        # situations in 'sits', considering transitions marked
        # decreasing any of the situations in 'decr' as beeing
        # "cut". The return value is a dictionary representing the
        # "non-increasing" labeling, mapping transitions leaves
        # (goto's) to situations.
        
        # find all strongly connected components, restricting the
        # transition relation within sits and ignoring already cut
        # transitions (has decreasing in decr)
        components = find_sccs(sits,
                               lambda x:[g.target for g in x.get_trs_leaves()
                                         if (g.target in sits and
                                             not decr.intersection(g.get_decreased_situations()))])

        if not components:
            # graph has no cycles, return empty set
            return {}
        nincr = {}

        for comp in components:
            candidates = []
            # loop for every situation
            for s1 in comp:
                # check if every cycle back to s1 is cut by "decreasing s1"
                if s1 not in transitive_closure(lambda x:[g.target for g in x.get_trs_leaves()
                                                          if (g.target in comp) and
                                                          (s1 not in g.get_decreased_situations()) and
                                                          (not decr.intersection(g.get_decreased_situations()))],
                                                s1):
                    candidates.append(s1)
            if not candidates:
                # signal failure
                return None
            for choice in candidates:
                # recurse
                nincr2 = cls._find_decomposition(comp,decr.union([choice]))
                if nincr2 is not None:
                    # we have found a decomposition, merge non-increasing marking
                    for (g,ni) in nincr2.items():
                        if g in nincr: nincr[g].update(ni)
                        else: nincr[g] = ni
                    # update nonincreasing marking for component
                    for s in comp:
                        for g in s.trs.get_leaves():
                            if (g.target in comp) and (choice not in g.get_decreased_situations()):
                                if g in nincr: nincr[g].add(choice)
                                else: nincr[g] = set([choice])
                    return nincr
        # if we reach this point, a cycle that does not decrease a variant has been found
        return None

    _find_decomposition = classmethod(_find_decomposition)
            

    def get_toplevel_situations( self ):
        return [self.initial_situation] + self.toplevels + self.final_situations


    def get_all_situations( self ):
        return mapcan(lambda x:x.get_all_situations(),self.get_toplevel_situations())
    

    def get_called_procedures( self ):
        return remove_duplicates(mapcan(lambda x:x.trs.get_called_procedures() if x.trs else [],
                                        self.get_all_situations()))


    def get_derivations(self):
        # first get all leaves (goto statements)
        trslist=[s.trs for s in self.get_all_situations() if s.trs]
        leaves=[trs.get_leaves() for trs in trslist]
        leaves=sum(leaves, []) # flatten list

        # then get derivations of all gotos
        gotoderivations=[l.derivations for l in leaves]
        gotoderivations=sum(gotoderivations, []) # flatten list

        # then get derivations of every statement
        statements=[l.parent.get_stmtseq_chain() for l in leaves]
        statements=sum(statements, []) # flatten list
        stmtderivations=[s.derivations for s in statements]
        stmtderivations=sum(stmtderivations, []) # flatten list
        stmtderivations=list(set([s for s in stmtderivations])) # remove duplicates

        return gotoderivations + stmtderivations



class Situation( ConstraintSet ):

    def __init__( self, proc, ast, id ):
        ConstraintSet.__init__(self,proc,ast,id)
        self.init_id_counter("transition","transition","trs")
        self.trs = None
        self.nestedenv = LexicalEnvironment(self.env)

    def get_next_uid_env( self ):
        return self.nestedenv

    def get_trs_leaves( self ):
        return self.trs.get_leaves() if self.trs else []

    def get_reachable_situations( self ):
        return transitive_closure(lambda y:[z.target for z in y.get_trs_leaves()],self)

    def idref_check( self ):
        if self.trs: self.trs.idref_check()

    def semantic_check( self, warnings ):
        pass

    def semantic_translate( self ):
        if self.trs:
            self.trs = self.trs.semantic_translate()
            self.trs = self.trs.perform_indexing()

    def get_constants( self ):
        return self.proc.get_constants()
    
    def get_variables( self ):
        return []

    def get_idtype( self ):
        return "situation"

    def get_attribute( self ):
        return "situation"
    
    def get_stmtseq_chain(self):
        return []

    def get_stmtseq(self):
        return None



class InitialSituation( Situation ):

    def __init__( self, proc ):
        # Note: ast = precondition ast
        Situation.__init__(self,proc, proc.pre.ast,
                           Symbol("ini__",self.get_idtype(),proc.filename))

        self.pre = proc.pre

        from pc.semantic.Trs import Trs
        if proc.ast["body","diagram","trs"]:
            self.trs = Trs(self,proc.ast["body","diagram","trs"][0])
        else: self.trs = None


    def get_variables( self ):
        return self.proc.valreses[:]


    def get_constraints( self ):
        return self.proc.pre.get_constraints()


    get_combined_constraints = get_constraints


    def semantic_check( self, warnings ):
        if self.trs: self.trs.semantic_check(warnings)


    def get_all_situations( self ):
        return [self]



class FinalSituation( Situation ):

    def __init__( self, proc, post ):
        # Note: ast = postcondition ast
        Situation.__init__(self,proc, post.ast,
                           Symbol("fin__"+post.get_id_str(),self.get_idtype(),proc.filename))

        self.trs = None
        self.post = post

    def get_variables( self ):
        return self.proc.valreses + self.proc.results
    
    def get_all_situations( self ):
        return [self]

    def get_constraints( self ):
        return self.post.get_constraints()

    get_combined_constraints = get_constraints



class IntermediateSituation( Situation ):

    def __init__( self, proc, ast, parent ):
        if ast["ID"]: id = Symbol.from_node(ast["ID"][0],self.get_idtype(),proc.filename)
        else: id = proc.get_next_id("situation")
        Situation.__init__(self,proc,ast,id)

        self.parent = parent
        # local vars
        self.locals = []
        for var in self.ast["pvardecl"]:
            tdecl = var[-1]
            for vid in var["ID"]:
                self.locals.append(VariableBinding(self.filename,self.env,var,
                                                   Symbol.from_node(vid,
                                                                    "variable",
                                                                    proc.filename),
                                                   tdecl))

        # constraints
        self.constraints = [Constraint(self,c[0]) for c in ast["constraint"]]
        
        # variant
        if ast["variant"]:
            self.variant = ast["variant"][0]
            VariableBinding(self.filename,self.env,self.variant,
                            Symbol("v__"+self.get_id_str()+"__0","variable",
                                   proc.ctx.filename),
                            NAME_IDOP(ID("int")))
        else:
            self.variant = None

        # nested situations
        self.nested = map(lambda x:IntermediateSituation(self.proc,x,self),
                          self.ast["diagram","situation"])

        # transition tree
        from pc.semantic.Trs import Trs

        if ast["diagram","trs"]: self.trs = Trs(self,ast["diagram","trs"][0])
        else: self.trs = None


    def get_locals( self ):
        return self.locals[:]


    def get_variables( self ):
        return (self.parent.get_variables() if self.parent
                else self.proc.get_variables()) + self.locals
    

    def idref_check( self ):
        Situation.idref_check(self)
        for s in self.nested: s.idref_check()


    def semantic_check( self, warnings ):
        if self.trs: self.trs.semantic_check(warnings)
        for s in self.nested: s.semantic_check(warnings)


    def semantic_translate( self ):
        Situation.semantic_translate(self)
        for s in self.nested: s.semantic_translate()


    def get_constraints( self ):
        return self.constraints[:]


    def get_all_situations( self ):
        return [self] + mapcan(lambda x:x.get_all_situations(),self.nested)


    def get_combined_constraints( self ):
        cs = self.parent.get_combined_constraints() if self.parent else []
        return cs + self.get_constraints()


    def get_topmost( self ):
        return self.parent.get_topmost() if self.parent else self



class Try( ContextItem ):

    def __init__( self, ctx, ast, id ):
        super(Try,self).__init__(ctx,ast)
        assert ast.type=="try"
        self.set_id(id,override=True)
        self.strategy = None
        self.tcc_strategy = None
        

    def idref_check( self ):
        if not self.ctx.unsafe_commands_allowed():
            raise ProgramException([ElementError("strategy directive in unsafe mode disallowed",self)])
        
        strata = [None,None]
        i = 0
        for s in self.ast.children:
            if s.type=="literal":
                # TODO: parse s-expression; for now, just pass the string literally
                # An empty string is not accepted, as it does not constitute a
                # meaningful s-expression.
                if s.value.strip():
                    strata[i] = s.value.strip().replace("\n"," ")
                else:
                    raise ProgramException([ElementError("empty strategy",self,s)])
            else:
                assert s.type=="context_name"
                ctx_id = s["ID"][0]
                ctx = self.idref(ctx_id,"context")
                if not ctx:
                    raise ProgramException([ElementError("context '%s' not found"%ctx_id.value,
                                                         self,s)])
                if not ctx in self.ctx.get_extends_reflexive_transitive():
                    raise ProgramException([ElementError("context '%s' is not an "
                                                         "extension of context "
                                                        "'%s'"%(self.ctx.get_id_str(),ctx_id.value),
                                                         self,s)])
                strata[i] = ctx.get_strategy()[i]
            i += 1

        if i==1: strata[1] = strata[0]
        self.strategy,self.tcc_strategy = strata


    def get_updated_strategy( self, strata ):
        return ( (self.strategy if self.strategy else strata[0]),
                 (self.tcc_strategy if self.tcc_strategy else strata[1]) )

    def semantic_check( self, warnings ):
        pass

    def get_idtype( self ):
        return "command"

    def get_attribute( self ):
        return "command"
