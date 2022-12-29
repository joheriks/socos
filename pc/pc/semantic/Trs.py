from pc.util.Utils import *
from pc.parsing.AST import tree_to_str

from pc.semantic.Element import *
from pc.semantic.Context import *
from pc.semantic.Message import *
from pc.semantic.Derivation import *


class TrsElement( Element ):

    # A nested transition element with a parent backpointer

    def __init__( self, parent, ast=None ):
        assert isinstance(parent,TrsElement) or isinstance(parent,Situation)
        self.parent = parent
        super(TrsElement,self).__init__(parent.filename,parent.nestedenv)
        self.ast = ast
        self.nestedenv = LexicalEnvironment(self.env)
        self.assumptions_added=False # used in semantic translate of goto 
        self.derivations=[]

    def get_procedure( self ):
        if isinstance(self.parent,Situation): return self.parent.proc
        else: return self.parent.get_procedure()

    def get_strategy( self ):
        return self.parent.get_strategy()

    def get_constants( self ):
        return self.get_procedure().get_constants()

    def get_prev_uid_part( self ):
        return self.parent

    def get_next_uid_env( self ):
        return self.nestedenv

    def get_idtype( self ):
        return "transition"

    def get_accessibles( self ):
        raise Exception("Not implemented")

    def get_accessibles_after( self ):
        return self.get_accessibles()

    def get_indexed_vb(self, vb, indexinc=0):
        sym = Symbol.make_indexed(vb.id,self.env.get_index(vb.id) + indexinc,"value")
        if self.env.is_defined(sym):
            vb = self.env.get_binding(sym)
        else:
            vb = VariableBinding(vb.filename, self.env, None, sym, vb.type)
        assert type(vb)==VariableBinding
        return vb

    def substitute_indexed_mutables(self, expression):
        retval = expression
        substvars = self.get_accessibles()
        if substvars:
            retval = APPLY(PGROUP(LAMBDA(substvars,retval)),
                           [self.get_indexed_vb(x).get_ID() for x in substvars])
        return retval

    def idref_check( self ):
        pass
    
    def semantic_check( self, warnings ):
        pass
    
    def semantic_translate( self ):
        return [self]

    def perform_indexing( self ):
        return [self]

    def _validate_expression( self, pred ):
        # idref-checks all underscored ids in the given predicate,
        # as well as raises an error if an underscored id is bound
        for x in filter_nodes(is_binding_expr,pred,True):
            for y in get_binding_ids(x):
                assert y.type=='ID'
                if "__" in y.value:
                    raise ProgramException([ElementError("binding identifier '%s' contains '__', which is disallowed"%y.value,self)])
                if self._has_indexed_form(y.value):
                    raise ProgramException([ElementError("binding identifier '%s' has the form of an indexed variable, which is disallowed"%y.value, self)])

        for x in filter_nodes( lambda x:x.type=="ID" and "__" in x.value, pred, True):
            self.idref(x,"variable")



class StrategyTrsElement( StrategyElement, TrsElement  ):

    def __init__( self, parent, ast ):
        super(StrategyTrsElement,self).__init__(parent,ast)
        self.init_strategy(self.get_procedure().ctx,ast)
        
    get_strategy = StrategyElement.get_strategy

    def idref_check( self ):
        super(StrategyTrsElement,self).idref_check()

    def get_default_strategy( self ):
        return self.parent.get_strategy()



class Check( TrsElement ):

    # An obligation to check a single constraint. 
    # AST is set to point to parent's AST.
    
    def __init__( self, constraint, parent ):
        super(Check,self).__init__(parent)
        self.set_id_from_str("check",override=True)
        self.ast = parent.ast
        self.constraint = constraint

    def get_attribute( self ):
        return "check"

    def get_accessibles( self ):
        return self.parent.get_accessibles_after()



class Decreasing( StrategyTrsElement ):

    def __init__( self, parent, ast ):
        super(Decreasing,self).__init__(parent,ast)
        self.set_id_from_str("check_decreasing",override=True)
        self.situations = []
        
    # idref decreased situations
    def idref_check( self ):
        super(Decreasing,self).idref_check()
        self.situations = [ self.idref(x,"situation") for x in self.ast["ID"] ]

    def semantic_check( self, warnings ):
        super(Decreasing,self).semantic_check(warnings)
        # check that every decreased situation has a variant
        msgs = []
        for decr in self.situations:
            if not decr.variant:
                msgs.append(ElementError("situation '%s' does not have a variant"%decr.get_id_str(),
                                         self))
        if msgs:
            raise ProgramException(msgs)



class Goto( StrategyTrsElement ):

    def __init__( self, parent, ast ):
        assert isinstance(parent,Trs)
        assert ast.type in ("goto","exit")

        super(Goto,self).__init__(parent,ast)

        self.set_id_from_str("goto",override=True)
        derid = "%s__%s__der"%(self.get_procedure().get_id_str(), self.get_id_str())
        self.init_id_counter('derivation', 'derivation', 'der')
        self.target = None

        self.decreasing = Decreasing(self,ast["decreasing"][0]) if ast["decreasing"] else None

        self.nonincreasing = []
        self.obligations = []
        self.derivations = [Derivation(self,d) for d in self.ast['derivation']]


    def get_accessibles( self ):
        idx = self.parent.statements.index(self)
        if idx==0:
            return self.parent.get_accessibles()
        else:
            return self.parent.statements[idx-1].get_accessibles_after()

   
    def is_exit( self ):
        return self.ast.type=="exit"


    def idref_check( self ):
        super(Goto,self).idref_check()
        if self.is_exit():
            # wrap exception to make the error a bit more user friendly
            try:
                self.target = self.env.get_binding(Symbol("fin__"+self.ast["ID"][0].value if self.ast['ID'] else "fin__post__",
                                                          "situation",self.filename))

            except IllegalSymbolException,e:
                sym = e.sym
                if sym.id=="fin__":
                    raise ProgramException([ElementError("procedure '%s' does not declare an "
                                                           "unnamed postcondition"%self.proc.get_id_str(),
                                                           self)])
                elif sym.id.startswith("fin__"):
                    raise ProgramException([ElementError("unknown postcondition: "
                                                           "%s"%sym.id[5:],self)])

        else:
            # goto 
            self.target = self.idref(self.ast["ID"][0],"situation")

        if self.decreasing:
            self.decreasing.idref_check()

        for der in self.derivations:
            der.idref_check()


    def semantic_check( self, warnings ):
        msgs = []
        # check that target situation variables are assigned
        acc = self.get_accessibles()
        for vb in self.target.get_variables():
            if vb not in self.get_accessibles():
                msgs.append(ElementError("variable '%s' must be assigned a value by transition"%vb.get_id_str(),
                                           self))
                
        
        if msgs:
            raise ProgramException(msgs)

        if self.decreasing:
            self.decreasing.semantic_check(warnings)

        for d in self.derivations:
            d.semantic_check(warnings)


    def semantic_translate( self ):
        # Create one obligation for each target constraint. 
        self.obligations = [Check(c,self) for c in self.target.get_combined_constraints()]
        self.create_derivation_assumptions()

        return [self]


    def create_derivation_assumptions(self):
        from pc.semantic.Statement import Assume, Assert

        statements=self.parent.get_stmtseq_chain()
        derivations=[]
        assumptions=[]

        # create assumptions for situation constraints
        for constraint in self.get_source_situation().get_combined_constraints():
            pred=constraint.ast
            id=None
            assumptions.append((pred, id))

        for stmt in statements:
            # add all assumptions encountered so far to each derivation for this statement
            # TODO: add assumptions to statement's derivation in semantic translate of Trs instead, 
            # in order to avoid using the ugly stmt.assumptions_added flag
            if not stmt.assumptions_added:
                stmt.assumptions_added=True
                for der in stmt.derivations:
                    der.env=stmt.nestedenv
                    for pred, id in assumptions:
                        ass=Assumption(der, P('derivation_decl',[P('assumption', [pred])]))
                        ass.always_add=True
                        if id: ass.set_id(id)
                        der.assumptions.append(ass)

            # create a new assumption for each assume statement
            if isinstance(stmt, Assume):
                pred=stmt.predicate
                #id = Symbol(stmt.get_id_str(),"assumption")
                id = None
                assumptions.append((pred, id))

        # finally add all assumptions to all derivations of this goto
        for der in self.derivations:
            der.env=self.parent.get_post_env()

            for pred, id in assumptions:
                ass=Assumption(der, P('derivation_decl',[P('assumption', [pred])]))
                ass.always_add=True
                if id: ass.set_id(id)
                der.assumptions.append(ass)


    def perform_indexing( self ):
        from pc.semantic.Statement import Assert

        asserts = []

        decr = self.parent.get_decreased_situations()
        nincr = self.parent.get_nonincreased_situations()
        if decr or nincr:
            v0d = [self.subst_zero_indexed_mutables(x.variant[0]) for x in decr]
            v1d = [x.variant[0] for x in decr]
            v0n = [self.subst_zero_indexed_mutables(x.variant[0]) for x in nincr]
            v1n = [x.variant[0] for x in nincr]
            conds = []
            for i in range(len(decr)):
                conds.append(P("LT_EQUAL",[L("NUMBER","0"),v1d[i]]))
                conds.append(P("LT",[v1d[i],v0d[i]]))
            for i in range(len(nincr)):
                conds.append(P("LT_EQUAL",[L("NUMBER","0"),v1n[i]]))
                conds.append(P("LT_EQUAL",[v1n[i],v0n[i]]))

            for cond in conds:
                ass=Assert(self.parent, None)
                ass.ast=self.ast["decreasing"][0] if decr else self.ast

                ass.predicate = self.substitute_indexed_mutables(cond)
                ass.env = LexicalEnvironment(self.parent.get_post_env())
                if self.decreasing:
                    ass.trystrategy = self.decreasing.trystrategy
                asserts.append(ass)

        return asserts + [self]


    def subst_zero_indexed_mutables(self, expression):
        sit = self.get_source_situation()
        if sit.get_variables():
            lambdafunc=LAMBDA(sit.get_variables(),expression)
            initvals = [sit.trs.nestedenv[sit.trs.nestedenv.find_indexed(v.id,0,"value")] for v in sit.get_variables()]
            lambdaappl=APPLY(PGROUP(lambdafunc), [vb.get_ID() for vb in initvals])
        else:
            # a lambda function cannot take zero arguments, hence the special case
            lambdaappl=expression

        return lambdaappl


    def get_leaves( self ):
        return [self]


    def get_source_situation( self ):
        return self.parent.get_source_situation()


    def set_nonincreasing( self, ni ):
        assert not [x for x in self.get_decreased_situations() if x in ni],"%s: %s"%(self.proc.get_id_str(),
                                                                     [x.get_id_str() for x in self.get_decreased_situations() if x in ni])
        self.nonincreasing = list(ni)


    def get_decreased_situations( self ):
        return self.decreasing.situations[:] if self.decreasing else []


    def get_nonincreased_situations( self ):
        return self.nonincreasing[:]


    def get_called_procedures( self ):
        return []


    def get_attribute( self ):
        return "goto"


    def get_post_env( self ):
        return self.parent.get_post_env()


    def __str__( self ):
        return "GOTO %s"%self.target.get_id_str()
    


class Branch( TrsElement ):

    def __init__( self, parent, ast ):
        super(Branch,self).__init__(parent,ast)
        self.init_id_counter("transition","transition","trs")


    def get_accessibles( self ):
        idx = self.parent.statements.index(self)
        if idx==0:
            return self.parent.get_accessibles()
        else:
            return self.parent.statements[idx-1].get_accessibles_after()
        

    def get_accessibles_after( self ):
        return self.get_accessibles()

    
    def get_transitions( self ):
        raise "Not implemented"
    

    def get_called_procedures( self ):
        return remove_duplicates(mapcan(lambda x:x.get_called_procedures(),
                                        self.get_transitions()))


    def get_leaves( self ):
        return mapcan(lambda x:x.get_leaves(),self.get_transitions())
    

    def get_source_situation( self ):
        return self.parent.get_source_situation()


    def idref_check( self ):
        super(Branch,self).idref_check()
        for trs in self.get_transitions():
            trs.idref_check()


    def semantic_check( self, warnings ):
        super(Branch,self).semantic_check(warnings)
        for trs in self.get_transitions():
            trs.semantic_check(warnings)


    def semantic_translate( self ):
        self.transitions = [trs.semantic_translate() for trs in self.get_transitions()]
        return [self]


    def perform_indexing( self ):
        self.transitions = [trs.perform_indexing() for trs in self.get_transitions()]
        return [self]

    def get_stmtseq_chain(self):
        return self.parent.get_stmtseq_chain()



class Choice( Branch ):
    
    def __init__( self, parent, ast ):
        super(Choice,self).__init__(parent,ast)
        self.set_id_from_str("choice",override=True)
        self.transitions = [Trs(self,x) for x in ast.children] if ast else []


    def get_transitions( self ):
        return self.transitions


    def semantic_check( self, warnings ):
        super(Choice,self).semantic_check(warnings)
        if not self.get_transitions():
            warnings.append(ElementWarning("choice has no branches; procedure may not be live",self))


    def __str__( self ):
        return "CHOICE\n" + "\n".join(["  "+str(x) for x in self.transitions]) + "\nENDCHOICE"



class If( Branch, StrategyTrsElement ):

    def __init__( self, parent, ast ):
        super(If,self).__init__(parent,ast)
        self.set_id_from_str("if",override=True)
        self.transitions = map(lambda x:Trs(self,x),ast['trs'])
        self.guards = []
        for trs in self.transitions:
            from Statement import Assume
            if trs.statements and isinstance(trs.statements[0],Assume):
                self.guards.append(trs.statements[0].predicate)
            else:
                self.guards.append(NAME_IDOP(TRUE()))
        self.init_id_counter('derivation', 'derivation', 'der')
        self.derivations = [Derivation(self,d) for d in ast['derivation']]


    def get_transitions( self ):
        return self.transitions[:]


    def idref_check( self ):
        super(If,self).idref_check()
        for der in self.derivations:
            der.idref_check()


    def semantic_translate( self ):
        super(If,self).semantic_translate()

        from pc.semantic.Statement import Assert

        env = LexicalEnvironment(self.parent.get_post_env())

        ass = Assert(self.parent,None)
        ass.ast = self.ast
        ass.predicate = OR(self.guards)
        ass.env = env
        ass.derivations = self.derivations[:]
        ass.trystrategy = self.trystrategy
        ass.idref_check()

        choice = Choice(self.parent,None)
        choice.ast = self.ast
        choice.transitions = self.transitions
        for t in self.transitions:
            t.parent = choice
        choice.env = env
        choice.nestedenv = self.nestedenv
        
        return [ass,choice]
        

    def __str__( self ):
        return "IF\n" + "\n".join(["  "+str(x) for x in self.transitions]) + "\nENDIF"



class Trs( TrsElement ):

    def __init__( self, parent, trs_ast ):
        super(Trs,self).__init__(parent,trs_ast)
        assert trs_ast.type=="trs",trs_ast.type
        self.set_id(parent.get_next_id("transition"),override=True)

        self.init_id_counter("statement","transition","stmt")

        from pc.semantic.Statement import Assert,Assign,Assume,Havoc
        from pc.semantic.Call import Call

        self.statements = []
        for ast in trs_ast.children:
            if ast.type=="assert": S = Assert
            elif ast.type=="assign": S = Assign
            elif ast.type=="assume": S = Assume
            elif ast.type=="call": S = Call
            elif ast.type=="havoc": S = Havoc
            elif ast.type in ("goto","exit","KEY_EXIT"): S = Goto
            elif ast.type=="if": S = If
            elif ast.type=="choice": S = Choice
            else: raise Exception("Unknown statement: %s"%ast.type)
            self.statements.append(S(self,ast))


    tail = property(lambda self : self.statements[-1])

    atomic_statements = property(lambda self : self.statements[:-1])


    def prepend_stmt( self, stmt ):
        self.statements.insert(0,stmt)
        stmt.parent = self


    def get_accessibles( self ):
        if isinstance(self.parent,Situation):
            return self.parent.get_variables()
        else:
            return self.parent.get_accessibles_after()


    def get_accessibles_after( self ):
        if len(self.statements)>1:
            return self.statements[-1].get_accessibles_after()
        else:
            return self.get_accessibles()


    def idref_check( self ):
        super(Trs,self).idref_check()
        for stmt in self.statements:
            stmt.idref_check()

        from pc.semantic.Call import Call
        self.callees = remove_duplicates([x.get_callee() for x in self.statements if isinstance(x,Call)])


    def semantic_check( self, warnings ):
        super(Trs,self).semantic_check(warnings)
        for s in self.statements:
            s.semantic_check(warnings)

        leaves = self.get_leaves()
        for x in leaves:
            for y in leaves:
                if x!=y and x.target==y.target and set(x.get_decreased_situations())!=set(y.get_decreased_situations()):
                    raise ProgramException([ElementError("mismatched decreasing declarations for transitions '%s' -> '%s'"%(self.get_source_situation().get_id_str(),x.target.get_id_str()),self)])
    

    def semantic_translate( self ):
        self.statements = mapcan(lambda x:x.semantic_translate(),self.statements)
        return self


    def perform_indexing( self ):
        if isinstance(self.parent,Situation):
            for v in self.parent.get_variables():
                sym = Symbol.make_indexed(v.id,0,"value")
                VariableBinding(self.filename,self.nestedenv,None,sym,v.type)

        self.statements = mapcan(lambda x:x.perform_indexing(),self.statements)
        return self
            

    def get_called_procedures( self ):
        return remove_duplicates(self.callees+self.tail.get_called_procedures())


    def get_leaves( self ):
        return self.tail.get_leaves()


    def get_source_situation( self ):
        if isinstance(self.parent,Situation):
            return self.parent
        else:
            return self.parent.get_source_situation()


    def get_target_situations( self ):
        return remove_duplicates(map(lambda x:x.target,self.tail.get_leaves()))

    
    def get_decreased_situations( self ):
        return remove_duplicates(mapcan(lambda x:x.get_decreased_situations(),self.tail.get_leaves()))


    def get_nonincreased_situations( self ):
        return remove_duplicates(mapcan(lambda x:x.get_nonincreased_situations(),self.tail.get_leaves()))


    def get_attribute( self ):
        return "transition"


    def get_stmtseq_chain(self):
        return self.parent.get_stmtseq_chain()+self.atomic_statements


    def get_parent_stmtseq( self ):
        if isinstance(self.parent,Branch): return self.parent.parent
        else: return None


    def get_post_env( self ):
        if self.statements:
            return self.statements[-1].env
        else:
            if self.get_parent_stmtseq():
                return self.get_parent_stmtseq().get_post_env()
            else:
                return self.parent.env


    def __str__( self ):
        return " ; ".join([str(s) for s in self.statements])

