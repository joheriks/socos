from pc.semantic.Context import *
from pc.semantic.Trs import *
from pc.semantic.Statement import *
from pc.semantic.Derivation import *
from pc.semantic.Call import *
from pc.util.Utils import *
from pc.parsing.PVSUtils import *

import re


class PP( PVS_Theory_PP ):

    # HACK: We use a specialized pretty-printer for keeping track of
    # context and procedure theory line numbers until we get proper
    # position tracking in the pretty-printer.

    def __init__( self, outfile, theory_lines ):
        PVS_Theory_PP.__init__(self)
        self.theory_lines = theory_lines
        self._outfile = outfile
        self._current_line = 1

    def node_theory( self, node ):
        start = self._current_line
        PVS_Theory_PP.node_theory(self,node)
        self.theory_lines.append((node.children[0].value,
                                  start,
                                  self._current_line))

    def _write_line( self, x ):
        self._outfile.write(x+"\n")
        self._current_line += (1 + x.count("\n"))

    def write_to_file( self, p ):
        self.output(p,self._write_line)
        
            

class Contexts2PVS( object ):

    def __init__( self, ibpenv, ctxs ):
        self._ibpenv = ibpenv
        self._ctxs = ctxs
        self._current_ctx = None
        self._theory_lines = []
        self._debug_info = True
        self.goal_count = 0
        

    def translate_and_write( self, outf ):
        p = P("root",[])
        for ctx in self._ctxs:
            self.trs_context(p,ctx)
        pp = PP(outf,self._theory_lines)
        for c in p.children: pp.write_to_file(c)
        return p


    def line2elem( self, line ):
        for thid,start,end in self._theory_lines:
            if start<=line<=end:
                if thid.startswith("ctx__"):
                    return [x for x in self._ctxs if x.get_id_str()==thid[5:]][0]
                elif thid.startswith("spec__") or thid.startswith("impl__"):
                    m = re.match("(spec|impl)__(?P<ctxid>[A-Za-z0-9_?]+)__(?P<procid>[A-Za-z0-9_?]+)",thid)
                    assert m
                    for ctx in self._ctxs:
                        if ctx.get_id_str()==m.group("ctxid"):
                            return ctx.get_procedure_by_name(m.group("procid"))
        return None



class VerifyContexts( Contexts2PVS ):

    def trs_context( self, p, ctx ):
        self._current_ctx = ctx
        self.trs_context_spec(p,ctx)

        for der in ctx.derivations:
            self.trs_toplevel_derivation(p,der)

        for proc in ctx.procedures:
            self.trs_procedure_spec(p,proc)
        for proc in ctx.procedures:
            self.trs_procedure_impl(p,proc)


    def pvs_id( elem ):
        return ID("__".join([x[0].value for x in elem.get_uid_ast()]))
    pvs_id = staticmethod(pvs_id)


    def context_theory_id( ctx ):
        return ID("ctx__" + ctx.get_id_str())
    context_theory_id = staticmethod(context_theory_id)


    def context_theory_name( ctx ):
        prefix = ctx.ibpenv.get_context_prefix(ctx)
        if prefix: return P("theory_name",[ID(prefix),VerifyContexts.context_theory_id(ctx)])
        else: return P("theory_name",[VerifyContexts.context_theory_id(ctx)])
    context_theory_name = staticmethod(context_theory_name)


    def derivation_theory_id( der ):
        return ID("der__%s__%s"%(der.get_context().get_id_str(),der.get_id_str()))
    derivation_theory_id = staticmethod(derivation_theory_id)


    def procedure_spec_theory_id( proc ):
        return ID("spec__%s__%s"%(proc.ctx.get_id_str(),proc.get_id_str()))
    procedure_spec_theory_id = staticmethod(procedure_spec_theory_id)


    def procedure_impl_theory_id( proc ):
        return ID("impl__%s__%s"%(proc.ctx.get_id_str(),proc.get_id_str()))
    procedure_impl_theory_id = staticmethod(procedure_impl_theory_id)


    def sit_id( sit ):
        return ID("sit__"+sit.get_id_str())
    sit_id = staticmethod(sit_id)

    theorem_id = pvs_id
            

    def pre_name( proc, qft=True ):
        if qft: return NAME_IDOP(VerifyContexts.procedure_spec_theory_id(proc),
                                 proc.pre.get_ID())
        else: return proc.pre.get_ID()
    pre_name = staticmethod(pre_name)


    def post_name( post, qft=True ):
        if qft: return NAME_IDOP(VerifyContexts.procedure_spec_theory_id(post.proc),post.get_ID())
        else: return post.get_ID()
    post_name = staticmethod(post_name)
    
        
    def variant_name( proc, qft=True ):
        if qft: return NAME_IDOP(VerifyContexts.procedure_spec_theory_id(proc),ID("var__"))
        else: return ID("var__")
    variant_name = staticmethod(variant_name)


    def _name_theorem( self, theorem_type, name, vars, assumptions, goal ):
        theorem = goal
        if assumptions: theorem = IMPLIES(AND(assumptions),theorem)
        if vars: theorem = FORALL(P("lambdabindings",[TYPED_VBS(vars)]),theorem)
        return P(theorem_type,[name,theorem])

    def _theorem( self, theorem_type, elem, vars, assumptions, goal ):
        return self._name_theorem(theorem_type, self.theorem_id(elem), vars, assumptions, goal)

    def trs_toplevel_derivation( self, p, der ):

        th = P("theory",[self.derivation_theory_id(der)])

        # add context imports
        th.children.append(P("importing",[VerifyContexts.context_theory_name(der.get_context())]))

        for b in der.get_inherited_logicals():
            th.children.append(P("var",[b.get_ID(),b.type]))
        th.children += [x for x in der.declarations]

        self.trs_derivation(th, der)

        p.children.append(th)


    def trs_context_spec( self, p, ctx ):
        th = P("theory",[VerifyContexts.context_theory_id(ctx)])

        for ext in ctx.extends:
            th.children.append(P("importing",
                                 [P("theory_name",
                                    [VerifyContexts.context_theory_name(ext)])]))

        for b in ctx.get_inherited_logicals():
            th.children.append(P("var",[b.get_ID(),b.type]))
        th.children += [x for x in ctx.declarations]

        th.children.append(P("proof",
                             [ID("*_TCC*"),
                              self.tcc_endgame_strategy(ctx,self.defstruct_context(ctx))]))

        p.children.append(th)

        
    def trs_procedure_spec( self, p, proc ):
        th = P("theory",[VerifyContexts.procedure_spec_theory_id(proc)])

        # add context imports
        th.children.append(P("importing",[VerifyContexts.context_theory_name(proc.ctx)]))

        # add parameters and initconsts as variables
        for b in proc.consts + proc.valreses + proc.initconsts + proc.results:
            th.children.append(P("var",[b.get_ID(),b.type]))

        # add specs
        pre_binds = proc.consts+proc.valreses
        if proc.pre: cs = map(WRAP_IDENTITY,[c.ast for c in proc.pre.get_constraints()])
        else: cs = []
        self.trs_predicate(th,cs,ID("pre__"),pre_binds)

        post_binds = proc.consts+proc.initconsts+proc.valreses+proc.results
        for post in proc.posts:
            self.trs_predicate(th,map(WRAP_IDENTITY,[c.ast for c in post.get_constraints()]),
                               post.get_ID(),post_binds)

        # add variant
        if proc.variant:
            variant_binds = proc.consts + proc.valreses
            self.trs_constdecl(th,
                               self.variant_name(proc,False),
                               VBS(variant_binds) if variant_binds else None,
                               NAME_IDOP(ID("int")),proc.variant)

        # TCC strategy.
        tccproof = P("proof",
                     [ID("*_TCC*"),
                      self.tcc_endgame_strategy(proc,self.defstruct_procedure_spec(proc))])
        th.children.append(tccproof)

        p.children.append(th)


    def trs_constdecl( self, p, id, bnds, type, body=None ):
        if bnds:
            decl = P("constdecl_binding",[id,bnds])
        else:
            decl = P("constdecl_list",[id])
        decl.children.append(type)
        if body:
            decl.children.append(body)
        p.children.append(decl)


    def trs_predicate( self, p, cs, id, bnds ):
        if not cs: cs = []
        self.trs_constdecl(p,id,TYPED_VBS(bnds) if bnds else None,NAME_IDOP(ID("bool")),AND(cs))


    def trs_situation_decl( self, p, sit ):
        binds = sit.get_variables()

        if isinstance(sit,InitialSituation):
            # TODO: REFACTOR: the x__0=x part should not really be done here,
            # but rather in the semantic translation
            cs = [PGROUP(AND([EQUALS(sit.proc.initconsts[i].get_ID(),
                                     sit.proc.valreses[i].get_ID())
                              for i in range(len(sit.proc.valreses))]))] + \
                 [APPLY(self.pre_name(sit.proc),
                        [NAME_IDOP(x.get_ID())
                         for x in sit.proc.consts+sit.proc.valreses])]
        elif isinstance(sit,FinalSituation):
            cs = [APPLY(self.post_name(sit.post),
                        [NAME_IDOP(x.get_ID())
                         for x in sit.proc.consts+sit.proc.initconsts+sit.proc.valreses+sit.proc.results])]
        else:
            cs = map(WRAP_IDENTITY,[c.ast for c in sit.get_constraints()])
            if sit.parent:
                cs = [APPLY(NAME_IDOP(VerifyContexts.sit_id(sit.parent)),
                            [NAME_IDOP(b.get_ID()) for b in sit.parent.get_variables()])] + cs
        self.trs_predicate(p,cs,VerifyContexts.sit_id(sit),binds)


    def trs_procedure_impl( self, p, proc ):
        th = P("theory",[self.procedure_impl_theory_id(proc)])

        # add spec import 
        th.children.append(
            P("importing",
              [P("theory_name",[VerifyContexts.procedure_spec_theory_id(proc)])]))
        
        # add spec theory imports for called procedures
        for c in proc.get_called_procedures():
            th.children.append(
                P("importing",
                  [P("theory_name",[VerifyContexts.procedure_spec_theory_id(c)])]))

        #vals = remove_duplicates(mapcan(lambda x: [x.env[v] for v in x.env if v.type=="value"],
        #                                mapcan(lambda sit:[sit]+sit.trs.get_leaves() if sit.trs else [],
        #                                       proc.diagram.get_all_situations())))
        for b in proc.consts + proc.initconsts:
            th.children.append(P("constdecl_list",[b.get_ID(),b.type]))

        # add declared constants
        for c in proc.local_consts:
            th.children.append(c)

        # add transition derivations
        for d in proc.diagram.get_derivations():
            self.trs_derivation(th, d)

        # add situations
        for s in proc.diagram.get_all_situations():
            self.trs_situation_decl(th,s)

        # add VCs
        for s in proc.diagram.get_all_situations():
            self.trs_situation_vc(th,s)

        # Use the default endgame for TCCs as well
        tccproof = P("proof",
                     [ID("*_TCC*"),
                      self.tcc_endgame_strategy(proc,self.defstruct_procedure_impl(proc))])
        th.children.append(tccproof)

        p.children.append(th)


    def trs_situation_vc( self, p, sit ):
        if not sit.trs:
            return
        initvals = [sit.trs.nestedenv[sit.trs.nestedenv.find_indexed(v.id,0,"value")] for v in sit.get_variables()]
        p.children.append(self._theorem("lemma",sit,initvals,
                                        [APPLY_VBS(VerifyContexts.sit_id(sit),initvals)],
                                        self.pvs(sit.trs)))

        if self._ibpenv.should_verify(sit):
            prf_head = SEXP_LIST("uid",SEXP_STRING(sit.trs.get_uid())),
            prf_head += SEXP_LIST("skolem-2"),
            prf_head += SEXP_LIST("flatten-disjunct",1,":depth",1),

            # Only for initial transition:
            if isinstance(sit,InitialSituation):
                prf_head += (SEXP_LIST("expand","sit__ini__"),
                             SEXP_LIST("flatten-disjunct",-1,":depth",1))

            proof = prf_head+self.prf(sit.trs)
        else:
            proof = SEXP_LIST("skip",),

        p.children.append(P("proof",list((self.theorem_id(sit),)+proof)))


    def pvs( self, x ):
        if isinstance(x,Trs): return self.pvs_Trs(x)
        elif isinstance(x,Branch): return self.pvs_Branch(x)
        elif isinstance(x,Goto): return self.pvs_Goto(x)


    def prf( self, x ):
        if isinstance(x,Trs): return self.prf_Trs(x)
        elif isinstance(x,Branch): return self.prf_Branch(x)
        elif isinstance(x,Goto): return self.prf_Goto(x)


    def pvs_Predicate( self, cs, args ):
        if isinstance(cs,Situation):
            idop = NAME_IDOP(VerifyContexts.sit_id(cs))
            return APPLY(idop,args)

        elif isinstance(cs,SpecItem):
            # pre/postconditon assertion in procedure call
            idop = self.pre_name(cs.proc) if cs==cs.proc.pre else self.post_name(cs)
            return APPLY(idop,args)

        else:
            raise Exception("Can't happen")


    def pvs_Goto( self, goto ):
        env = goto.get_post_env()
        indexed = [Symbol.make_indexed(x.id, env.get_index(x.id)) for x in goto.target.get_variables()]
        args = [NAME_IDOP(ID(x.id)) for x in indexed]
        return self.pvs_Predicate(goto.target, args)


    def prf_Check( self, obl ):
        self.goal_count += 1
        return SEXP(("then",
                     ("uid",SEXP_STRING(obl.get_uid())),
                     ("check-report",obl.get_strategy()[0])))


    def prf_Goto( self, goto ):
        return SEXP( ["then"] +
                     [VerifyContexts._invoke_theorems(goto.derivations)]+

                     [SEXP_LIST("uid",SEXP_STRING(goto.get_uid()))] +
                     [SEXP_LIST("expand-defs",self.defstruct_procedure_impl(goto.get_procedure()))] +

                      # an extra assert command is required if the topmost situation of the target has
                      # no constraint, since the situation predicate TRUE appears as a conjunct.
                      ([SEXP_LIST("spread",("split",1),(("assert",),("skip",)))] if isinstance(goto.target,IntermediateSituation) and not goto.target.get_topmost().constraints else []) +
                      
                     [SEXP_LIST("spread",
                                SEXP_LIST("split-n",len(goto.obligations)),
                                SEXP([self.prf_Check(x) for x in goto.obligations]))] ),

    def pvs_Branch( self, branch ):
        # return true for empty transition list
        return AND([self.pvs(x) for x in branch.get_transitions()])

        
    def prf_Branch( self, branch ):
        if branch.get_transitions():
            return SEXP_LIST("spread",
                         SEXP_LIST("split-n",str(len(branch.get_transitions()))),
                         SEXP([SEXP(("then",
                                     SEXP_LIST("uid", SEXP_STRING(x.get_uid()))) +
                                    self.prf(x)) for x in branch.get_transitions()])),
        else:
            return SEXP_LIST("propax"),


    def pvs_Trs( self, trs ):
        q = self.pvs(trs.tail)
        for s in reversed(trs.atomic_statements):
            if isinstance(s,Assign):
                q = APPLY(PGROUP(LAMBDA(s.variables,q)),s.expressions)
            elif isinstance(s,Havoc):
                q = FORALL(P("lambdabindings",[TYPED_VBS(s.variables)]),q)
            elif isinstance(s,Assume):
                q = IMPLIES(PGROUP(s.predicate),q)
            elif isinstance(s,AssumeConstraints):
                q = IMPLIES(PGROUP(self.pvs_Predicate(s.cs,s.args)),q)
            elif isinstance(s,Assert):
                q = AND((WRAP_IDENTITY(s.predicate),q))
            elif isinstance(s,AssertConstraints):
                q = AND((PGROUP(self.pvs_Predicate(s.cs,s.args)),q))
            elif isinstance(s,AssertRecursiveBounded):
                v1 = APPLY(self.variant_name(s.get_procedure()),s.args)
                q = AND((WRAP_IDENTITY(P("LT_EQUAL",[L("NUMBER","0"),v1])),q))
            elif isinstance(s,AssertRecursiveDecreases):
                v0 = APPLY(self.variant_name(s.get_procedure()),
                       [NAME_IDOP(x.get_ID()) for x in s.get_procedure().consts + s.get_procedure().initconsts])
                v1 = APPLY(self.variant_name(s.get_procedure()),s.args)
                q = AND((WRAP_IDENTITY(P("LT",[v1,v0])),q))
            else:
                raise Exception("Can't happen: %s"%type(s))
        return q


    def _invoke_theorems( theorems ):
        if theorems:
            return SEXP( ("then",) + tuple((("lemma",SEXP_STRING(VerifyContexts.theorem_id(d).value)) for d in theorems)) )
        else:
            return SEXP(("skip",))
    _invoke_theorems = staticmethod(_invoke_theorems)


    def _check_obligations( self, obligations ):
        return SEXP(("spread",
                     ("split-n",len(obligations)),
                     [self.prf_Check(x) for x in obligations]))
    

    def prf_Trs( self, trs ):
        prf = self.prf(trs.tail)
        
        for s in reversed(trs.atomic_statements):
            if isinstance(s,Assign):
                prf = (SEXP_LIST("beta","1"),) + prf
            elif isinstance(s,Havoc):
                prf = (SEXP_LIST("skolem-2"),) + prf
            elif isinstance(s,Assume) or isinstance(s,AssumeConstraints):
                prf = (SEXP_LIST("flatten-disjunct",1,":depth",1),) + prf
            else:
                if type(s)==Assert: l = [s]
                elif type(s)==AssertConstraints: l = s.obligations[:]
                elif type(s)==AssertRecursiveBounded: l = [s]
                elif type(s)==AssertRecursiveDecreases: l = [s]
                else: raise Exception("Can't happen: %s"%type(s))
                prf = SEXP(("spread",
                            ("split",1,":depth",1),
                            (("then",
                              VerifyContexts._invoke_theorems(s.derivations),
                              ("uid",SEXP_STRING(s.get_uid())),
                              ("expand-defs",self.defstruct_procedure_impl(s.get_procedure())),
                              self._check_obligations(l)),
                             ("then",)+prf))),

        return prf

        
    def endgame_strategy( self, elem, defstruct ):
        if self._ibpenv.should_verify(elem):
            return SEXP(("then",
                         SEXP(("expand-nested-defs","-",defstruct)),
                         SEXP(("flatten-disjunct", "-" )),
                         SEXP(("check-report",elem.get_strategy()[0]),)))
        else:
            return SEXP(("skip",))


    def tcc_endgame_strategy( self, elem, defstruct ):
        if self._ibpenv.should_verify(elem):
            return SEXP(("then",
                         SEXP(("skosimp-2",)),
                         SEXP(("uid",SEXP_STRING(elem.get_uid()))),
                         SEXP(("then",
                               SEXP(("expand-nested-defs","-",defstruct)),
                               SEXP(("flatten-disjunct", "-" )),
                               SEXP(("check-report",elem.get_strategy()[1]),)))))
        else:
            return SEXP(("skip",))
        

    def _procedure_spec_decls( self, proc, qft ):
        return [SEXP((SEXP_PVS(self.pre_name(proc,qft)),
                      len(proc.pre.get_constraints()))) ] + \
               [SEXP((SEXP_PVS(self.post_name(x,qft)),
                      len(x.get_constraints()))) for x in proc.posts] +\
               ([SEXP((SEXP_PVS(self.variant_name(proc,qft)),SEXP((1,))))] if proc.variant else [])


    def defstruct_procedure_spec( self, proc ):
        return SEXP(self._procedure_spec_decls(proc,False))
    

    def defstruct_procedure_impl( self, proc ):
        # include all called procedures' pre- and postconditions:
        called_specs = mapcan(lambda p:self._procedure_spec_decls(p,True),proc.get_called_procedures())
        variant = [SEXP((SEXP_PVS(self.variant_name(proc)),SEXP((1,))))] if proc.variant else []
        return SEXP([self.defstruct_situation(sit) for sit in proc.diagram.get_toplevel_situations()] + called_specs + variant)
    

    def defstruct_situation(self, sit):
        if isinstance(sit,InitialSituation):
            sexp = SEXP((SEXP_PVS(self.pre_name(sit.proc)),len(sit.pre.get_constraints()),
                         SEXP((SEXP([SEXP_PVS(VerifyContexts.sit_id(sit)),len(sit.get_constraints())]),))))

        elif isinstance(sit,FinalSituation):
            sexp = SEXP((SEXP_PVS(self.post_name(sit.post)),len(sit.post.get_constraints()),
                         SEXP((SEXP([SEXP_PVS(VerifyContexts.sit_id(sit)),len(sit.get_constraints())]),))))

        elif isinstance(sit,IntermediateSituation):
            sexp = SEXP([SEXP_PVS(VerifyContexts.sit_id(sit)),len(sit.get_constraints())])
            if sit.nested:
                sexp.children.append(SEXP(map(self.defstruct_situation,sit.nested)))

        return sexp


    def defstruct_context( self, ctx ):
        return "nil"


    ### STRUCTURED DERIVATIONS ###


    def _proofs( self, elem, proof_tail, tcc_proof_tail ):
        proof = ["then",SEXP_LIST("uid",SEXP_STRING(elem.get_uid()))] + proof_tail
                  
        tcc_id = ID(self.theorem_id(elem).value + "_TCC*")
        tcc_proof = ["then",SEXP_LIST("uid",SEXP_STRING(elem.get_uid()))] + tcc_proof_tail
       
        return (P("proof",[self.theorem_id(elem),SEXP_LIST(*proof)]),
                P("proof",[tcc_id,SEXP_LIST(*tcc_proof)]))


    def prf_Motivation( self, motivation, strati ):
        assert strati==0 or strati==1
        prf = []
        branch = prf
        strata = motivation.get_strategy()
        for cmd in motivation.commands:
            if isinstance(cmd,Case):
                if cmd.by: cmd = ("case-by",SEXP_PVS(cmd.expression),cmd.by)
                else: cmd = ("case",SEXP_PVS(cmd.expression))
                branch.append(cmd)
                
            if isinstance(cmd,Use):
                branch.append(("use",cmd.lemma))                

        branch.append(("check-report",strata[strati]))
        return prf


    def trs_derivation( self, p, der ):
        i = 1
        for obs in der.observations:
            self.trs_observation(p, obs)
            i = i + 1

        if der.goal and der.goal.parse_ok and der.should_check_goal():
            # generate a lemma to check that the assumptions and steps imply the goal
            assumptions = [x.predicate for x in der.motivation.get_used_assumptions()]
            for step in der.steps:
                ass = step.get_predicate()
                if ass:
                    stepass = [PGROUP(x.predicate) for x in step.motivation.get_used_assumptions()]
                    if stepass:
                        ass = IMPLIES(AND(stepass), ass)
                        assumptions.append(ass)

            p.children.append(self._theorem("lemma", der.goal,der.get_logicals(), assumptions, der.goal.get_predicate()))
            if self._ibpenv.should_verify(der.goal):
                proof = [("skolem-2",),
                         ("flatten-disjunct","+",":depth",1),
                         ("flatten-disjunct","-",":depth",len(assumptions)),] + self.prf_Motivation(der.motivation,0)
                tccproof =  [("skolem-2",),("flatten","-")]+self.prf_Motivation(der.motivation,1)
                self.goal_count += 1
            else:
                proof,tccproof = [("skip",)],[("skip",)]
            p.children += self._proofs(der.goal, proof, tccproof)

        # generate a lemma for each step
        i = 1
        for step in der.steps:
            self.trs_step(p, step)

        # the main theorem
        if der.goal and der.goal.parse_ok:
            p.children.append(self._theorem(der.goal.get_theorem_type(),
                                            der,
                                            der.get_logicals(),
                                            [x.predicate for x in der.get_collected_assumptions()],
                                            der.goal.get_predicate()))

            vars = [SEXP_STRING(x.get_id_str()) for x in der.get_logicals()]

            steps = []
            for s in der.steps:
                if s.parse_ok:
                    steps.append(SEXP_STRING(self.theorem_id(s).value))

            if self._ibpenv.should_verify(der):
                proof = [("skolemize-and-instantiate",
                          vars,
                          [SEXP_STRING(self.theorem_id(der.goal).value)] 
                          + steps 
                          + [SEXP_STRING(self.theorem_id(o).value) for o in der.observations if o.goal.parse_ok])]
                #proof = proof + [("prop",),("simplify",)]
                #proof = proof + [("bddsimp",),("simplify",)]
                proof = proof + [("grind",)]
                tccproof = self.prf_Motivation(der.motivation,1)
                self.goal_count += 1
            else:
                proof,tccproof = [("skip",)],[("skip",)]

            p.children += self._proofs(der,proof,tccproof)



    def trs_observation( self, p, obs ):
        for der in obs.motivation.subderivations:
            self.trs_derivation(p,der)

        assumptions = [x.predicate for x in obs.motivation.get_used_assumptions()]
        assumptions += [der.get_lemma() for der in obs.motivation.subderivations if der.is_lemma_ok()]
        

        p.children.append(self._theorem("lemma",
                                        obs.goal,
                                        obs.derivation.get_logicals(),
                                        assumptions,
                                        obs.predicate))

        # observation follows from its subderivations
        if self._ibpenv.should_verify(obs):
            proof = [("skolem-2",),
                     ("flatten-disjunct","+",":depth",1),
                     ("flatten-disjunct","-",":depth",len(assumptions))] + self.prf_Motivation(obs.motivation,0)
            tccproof =  [("skolem-2",),("flatten","-")]+self.prf_Motivation(obs.motivation,1)
            self.goal_count += 1
        else:
            proof,tccproof = [("skip",)],[("skip",)]
        p.children += self._proofs(obs.goal, proof, tccproof)


        # proof of observation using the subderivations' lemmas
        #############################
        
        
        p.children.append(self._theorem("lemma",
                                        obs,
                                        obs.derivation.get_logicals(),
                                        [x.predicate for x in obs.derivation.get_collected_assumptions()],
                                        obs.predicate))

        if self._ibpenv.should_verify(obs):
            vars = [SEXP_STRING(x.get_id_str()) for x in obs.derivation.get_logicals()]
            lemmas = [SEXP_STRING(self.theorem_id(obs.goal).value)]
            for s in obs.motivation.subderivations:
                if s.goal:
                    lemmas.append(SEXP_STRING(self.theorem_id(s.goal).value))
                else:
                    lemmas = lemmas + [SEXP_STRING(self.theorem_id(x).value) for x in s.steps]

            proof = [("skolemize-and-instantiate",
                      vars,
                      lemmas)]
            proof = proof + [("grind",)]
            tccproof = self.prf_Motivation(obs.motivation, 1)
            self.goal_count += 1
        else:
            proof,tccproof = [("skip",)],[("skip",)]
            
        p.children += self._proofs(obs, proof, tccproof)


            
    def trs_step( self, p, step ):
        # process subderivations
        for der in step.motivation.subderivations:
            self.trs_derivation(p,der)

        if step.get_predicate():
            assumptions = [PGROUP(x.predicate) for x in step.motivation.get_used_assumptions()]
            #print "assumptions: ", [tree_to_str(x) for x in assumptions]
            
            # Add all subderivation lemmas as assumptions
            assumptions += [der.get_lemma() for der in step.motivation.subderivations if der.is_lemma_ok()]

            p.children.append(self._theorem("lemma",
                                            step,
                                            step.derivation.get_logicals(),
                                            assumptions,
                                            step.get_predicate()))

            if self._ibpenv.should_verify(step):
                proof = [("skolem-2",),
                         ("flatten-disjunct","+",":depth",1),
                         ("flatten-disjunct","-",":depth",len(assumptions))] + self.prf_Motivation(step.motivation,0)
                tccproof =  [("skolem-2",),("flatten","-")]+self.prf_Motivation(step.motivation,1)
                self.goal_count += 1
            else:
                proof,tccproof = [("skip",)],[("skip",)]

            p.children += self._proofs(step,proof,tccproof)
                                       


class TraceContexts( VerifyContexts ):

    def procedure_exec_theory_id( proc ):
        return ID("exec__%s__%s"%(proc.ctx.get_id_str(),proc.get_id_str()))
    procedure_exec_theory_id = staticmethod(procedure_exec_theory_id)

    def procedure_exec_spec_adt_id( proc ):
        return ID("execspec__%s__%s"%(proc.ctx.get_id_str(),proc.get_id_str()))
    procedure_exec_spec_adt_id = staticmethod(procedure_exec_spec_adt_id)

    def procedure_exec_impl_adt_id( proc ):
        return ID("state__%s__%s"%(proc.ctx.get_id_str(),proc.get_id_str()))
    procedure_exec_impl_adt_id = staticmethod(procedure_exec_impl_adt_id)

    def procedure_exec_main_id( proc ):
        return ID("main__")
    procedure_exec_main_id = staticmethod(procedure_exec_main_id)
    
    def procedure_exec_step_id( proc, qft=False ):
        if qft: return NAME_IDOP(TraceContexts.procedure_exec_theory_id(proc),
                                 TraceContexts.procedure_exec_step_id(proc))
        else: return ID("step__")
        #return ID("step__"+proc.get_id_str())
    procedure_exec_step_id = staticmethod(procedure_exec_step_id)

    def procedure_exec_entry_id( proc, qft=False ):
        if qft: return NAME_IDOP(TraceContexts.procedure_exec_theory_id(proc),
                                 TraceContexts.procedure_exec_entry_id(proc))
        else: return ID("enter__")
    procedure_exec_entry_id = staticmethod(procedure_exec_entry_id)

    def procedure_exec_exit_id( proc, qft=False ):
        if qft: return NAME_IDOP(TraceContexts.procedure_exec_theory_id(proc),
                                 TraceContexts.procedure_exec_exit_id(proc))
        else: return ID("exit__")
    procedure_exec_exit_id = staticmethod(procedure_exec_exit_id)


    def call_adt_id( call ):
        id = TraceContexts.pvs_id(call)
        id.value += "__call"
        return id
    call_adt_id = staticmethod(call_adt_id)

    def ret_adt_id( call ):
        id = TraceContexts.pvs_id(call)
        id.value += "__ret"
        return id
    ret_adt_id = staticmethod(ret_adt_id)

    def adt_id( elem ):
        if isinstance(elem,Call): return TraceContexts.call_adt_id(elem)
        else: return TraceContexts.pvs_id(elem)
    adt_id = staticmethod(adt_id)
    

    def _get_locations_proc( self, proc ):
        return mapcan(self._get_locations_situation,proc.diagram.get_all_situations())


    def _get_locations_situation( self, sit ):
        x = [sit]
        if sit.trs: x += self._get_locations_trs(sit.trs)
        return x


    def _get_locations_trs( self, trs ):
        return mapcan(self._get_locations_stmt,trs.statements)


    def _get_locations_stmt( self, stmt ):
        if isinstance(stmt,Branch):
            return [stmt] + mapcan(self._get_locations_trs,stmt.get_transitions())
        else:
            return [stmt]


    def trs_context( self, p, ctx ):
        self.__uid = VariableBinding(ctx.filename,ctx.nestedenv,None,Symbol("uid__","debug"),
                                     NAME_IDOP(ID("string")))
        for proc in ctx.procedures:
            self.trs_procedure_exec(p,proc)


    def trs_procedure_exec( self, p, proc ):
        self.trs_procedure_adt(p,proc)

        th = P("theory",[self.procedure_exec_theory_id(proc)])

        th.children.append(
            P("importing",
              [P("theory_name",[self.procedure_exec_impl_adt_id(proc)])]))

        binds = proc.consts + proc.valreses + proc.results + proc.locals
        for b in binds:
            th.children.append(P("var",[b.get_ID(),b.type]))

        # add declared constants
        for c in proc.local_consts:
            th.children.append(c)

        self.trs_procedure_entry(th,proc)
        self.trs_procedure_step(th,proc)
        self.trs_procedure_exit(th,proc)

        if not proc.get_params():
            self.trs_constdecl(th,self.procedure_exec_main_id(proc),[],
                               NAME_IDOP(self.procedure_exec_impl_adt_id(proc)),
                               APPLY(self.procedure_exec_entry_id(proc),
                                     [APPLY(NAME_IDOP(self.adt_id(proc.pre)),
                                            [STRING(proc.pre.get_uid())])]))
        
        p.children.append(th)


    def trs_procedure_adt( self, p, proc ):
        add = p.children.append
        
        # spec adt
        spec_adt = P("datatype",[self.procedure_exec_spec_adt_id(proc)])
        spec_adt.children.append(
            P("importing",
              [P("theory_name",[self.procedure_spec_theory_id(proc)])]))
        for x in [proc.pre] + proc.posts:
            spec_adt.children.append(self._adt_constructor(self.adt_id(x),
                                                           self._get_bindings(x)))
        add(spec_adt)

        # intermediate situations
        adt = P("datatype",[self.procedure_exec_impl_adt_id(proc)])
        imported = [P("theory_name",[self.procedure_exec_spec_adt_id(proc)])]
        for c in proc.get_called_procedures():
            imported.append(P("theory_name",[self.procedure_exec_spec_adt_id(c)]))
        adt.children.append(P("importing",imported))

        for loc in self._get_locations_proc(proc):
            if isinstance(loc,Call):
                adt.children.append(self._adt_constructor(self.call_adt_id(loc),
                                                          self._get_bindings(loc)))
                adt.children.append(self._adt_constructor(self.ret_adt_id(loc),
                                                          self._ret_bindings(loc)))
            else:
                adt.children.append(self._adt_constructor(self.adt_id(loc),
                                                          self._get_bindings(loc)))
                
        add(adt)


    def trs_procedure_entry( self, p, proc ):
        q = self.apply_updates(proc.diagram.initial_situation)
        cases = [CASE(NAME_IDOP(self.adt_id(proc.pre)),self._get_bindings(proc.pre,False),q)]
        self.trs_constdecl(p,self.procedure_exec_entry_id(proc),
                           P("bindings",[TYPED_BINDING(ID("s__"),
                                                       NAME_IDOP(self.procedure_exec_spec_adt_id(proc)))]),
                           NAME_IDOP(self.procedure_exec_impl_adt_id(proc)),
                           P("cases",[NAME_IDOP(ID("s__")),cases]))


        
    def trs_procedure_exit( self, p, proc ):
        q = self.apply_updates(proc.diagram.initial_situation)
        
        cases = []
        for fin in proc.diagram.final_situations:
            q = APPLY(NAME_IDOP(self.adt_id(fin.post)),self._get_bindings(fin.post,False))
            case = CASE(NAME_IDOP(self.adt_id(fin)),self._get_bindings(fin,False),q)
            cases.append(case)
            
        self.trs_constdecl(p,self.procedure_exec_exit_id(proc),
                           P("bindings",[TYPED_BINDING(ID("s__"),
                                                       NAME_IDOP(self.procedure_exec_impl_adt_id(proc)))]),
                           NAME_IDOP(self.procedure_exec_spec_adt_id(proc)),
                           P("cases",[NAME_IDOP(ID("s__")),cases]))
    
        
    def trs_procedure_step( self, p, proc ):
        cases = P("cases",[NAME_IDOP(ID("s__"))])

        for s in proc.diagram.get_all_situations():
            self.trs_situation_exec_case(cases,s)

        self.trs_constdecl(p,self.procedure_exec_step_id(proc),
                           P("bindings",[TYPED_BINDING(ID("s__"),
                                                       NAME_IDOP(self.procedure_exec_impl_adt_id(proc)))]),
                           self.procedure_exec_impl_adt_id(proc),
                           cases)


    def _adt_constructor( id, bindings ):
        c = P("constructor",[id])
        if bindings: c.children.append(P("bindings",bindings))
        c.children.append(ID(id.value+"?"))
        return c
    _adt_constructor = staticmethod(_adt_constructor)


    def _get_bindings( self, elem, typed=True ):
        if isinstance(elem,Precondition):
            b = elem.proc.consts + elem.proc.valreses
        elif isinstance(elem,Postcondition):
            b = elem.proc.valreses + elem.proc.results
        elif isinstance(elem,Situation):
            b = elem.get_constants() + elem.get_variables()
        elif isinstance(elem,TrsElement):
            b = elem.get_constants() + elem.get_accessibles()
        else:
            raise Exception("Can't happen: %r"%elem)
        b.append(self.__uid)
        return map(TYPED_VB if typed else VB, b)


    def _ret_bindings( self, call, typed=True ):
        assert isinstance(call,Call)
        bindings = self._get_bindings(call,typed)
        th = ID("th__")
        th_type = NAME_IDOP(ID("string"))
        cs = ID("cs__")
        cs_type = NAME_IDOP(TraceContexts.procedure_exec_spec_adt_id(call.get_callee()))
        if typed: bindings += [TYPED_BINDING(th,th_type),TYPED_BINDING(cs,cs_type)]
        else: bindings += [BINDING(th),BINDING(cs)]
        return bindings
        

    def _match( self, elem, body ):
        return CASE(NAME_IDOP(self.adt_id(elem)),self._get_bindings(elem,False),body)


    def update( self, b, updates={} ):
        if updates:
            return APPLY(PGROUP(LAMBDA(updates.keys(),b)),updates.values())
        else:
            return b

    def apply_updates( self, elem, updates={} ):
        b = APPLY(NAME_IDOP(TraceContexts.adt_id(elem)),self._get_bindings(elem,typed=False))
        upd = { self.__uid : STRING(elem.get_uid()) }
        upd.update(updates)
        return self.update(b,upd)
    

    def trs_situation_exec_case( self, p, sit ):
        next = sit.trs.statements[0] if sit.trs else sit
        p.children.append(self._match(sit,self.apply_updates(next)))
        if sit.trs:
            self.trs_trs_exec_case(p,sit.trs)


    def trs_call_exec_case( self, p, call, nexts ):
        add = p.children.append
        add(CASE(NAME_IDOP(self.call_adt_id(call)),
                 self._get_bindings(call,False),
                 APPLY(NAME_IDOP(self.ret_adt_id(call)),
                       self._get_bindings(call,False) + 
                       [STRING(self.procedure_exec_theory_id(call.get_callee()).value)] +
                       [APPLY(self.adt_id(call.get_callee().pre),
                              call.get_actual_values()+[STRING(call.get_callee().pre.get_uid())])])))

        cases = []
        for (post,next) in nexts: 
            finalsit = [x for x in call.get_callee().diagram.final_situations if x.post==post][0]
            cases.append(self._match(post,
                                     self.apply_updates(next,dict(zip([x for x in call.get_actual_result_vars()],
                                                                      [x.get_ID() for x in call.get_callee().valreses + call.get_callee().results])))))

        retbinds = self._ret_bindings(call,False)
        
        # else clause:
        cases.append(APPLY(NAME_IDOP(self.ret_adt_id(call)),retbinds))

        add(CASE(NAME_IDOP(self.ret_adt_id(call)),retbinds,P("cases",[NAME_IDOP(ID("cs__"))]+cases)))

        for trs in call.get_transitions():
            self.trs_trs_exec_case(p,trs)

        
    def trs_trs_exec_case( self, p, trs ):
        elems = trs.statements
        add = p.children.append
        for i in range(len(elems)-1):
            elem = elems[i]
            next = elems[i+1]
            if isinstance(elem,Call):
                self.trs_call_exec_case(p,elem,[(elem.get_callee().posts[0],next)])
            elif isinstance(elem,Assign):
                add(self._match(elem,self.apply_updates(next,dict(zip(elem.variables,elem.expressions)))))
            elif isinstance(elem,Assert) or isinstance(elem,Assume):
                add(self._match(elem,P("ifthen",[elem.predicate,self.apply_updates(next),self.apply_updates(elem)])))
            else:
                add(self._match(elem,self.apply_updates(next)))
        self.trs_tail_exec_case(p,trs.tail)


    def trs_tail_exec_case( self, p, tail ):
        if isinstance(tail,Goto):
            p.children.append(self._match(tail,self.apply_updates(tail.target)))

        elif isinstance(tail,Call):
            self.trs_call_exec_case(p,tail,[(post,trs.statements[0]) for (post,trs) in tail.get_labeled_transitions()])
            
        elif isinstance(tail,If) or isinstance(tail,Choice):
            cases = []
            if tail.get_transitions():
                if isinstance(tail,Choice):
                    next = tail.get_transitions()[0].statements[0]
                    cases.append(self.apply_updates(next))
                elif isinstance(tail,If):
                    c = P("ifthen",[])
                    cases.append(c)
                    for i in range(len(tail.transitions)):
                        next = tail.get_transitions()[i].statements[0]
                        x = [tail.guards[i],
                             self.apply_updates(next)]
                        if i>0: c.children.append(P("elsif",x))
                        else: c.children += x
                    # else branch
                    c.children.append(self.apply_updates(tail))
            else:
                cases.append(self.apply_updates(tail))

            for trs in tail.get_transitions():
                self.trs_trs_exec_case(p,trs)

            p.children.append(CASE(NAME_IDOP(self.adt_id(tail)),
                                   self._get_bindings(tail,False),
                                   cases))

        else:
            raise Exception("unsupported translation")

        
