from pc.semantic.Context import *
from pc.semantic.Trs import *
from pc.semantic.Statement import *


class Call( StrategyTrsElement, Branch ):

    def __init__( self, parent, ast ):
        super(Call,self).__init__(parent,ast)
        self.set_id(parent.get_next_id("statement"),override=True)

        assert ast.type=="call",ast.type
        #self.nestedenv = LexicalEnvironment(self,self.env)

        self.init_id_counter("statement","statement","stmt")

        self.result_vbs = []
        self._labeled_exits = [(x["ID"][0],Trs(self,x["trs"][0])) for x in ast["labeled_trs"]]


    def get_accessibles_after( self ):
        return remove_duplicates(self.get_accessibles()+self.get_actual_result_vars())


    def get_attribute( self ):
        return "call"


    def get_transitions( self ):
        return [x[1] for x in self._labeled_exits]


    def get_labeled_transitions( self ):
        return self._labeled_exits[:]

    
    def get_called_procedures( self ):
        return remove_duplicates(mapcan(lambda x:x.get_called_procedures(),
                                        self.get_transitions()) + [self.get_callee()])


    def get_callee( self ):
        return self.get_procedure().ctx.get_procedure_by_name(self.ast[0].value)


    def get_actuals( self ):
        if self.ast["arguments"]: return self.ast["arguments"][0].children
        else: return []


    def get_actuals_tuple( self ):
        act_consts = []
        act_valreses = []
        act_results = []
        for i in range(min(len(self.get_actuals()),len(self.get_callee().params))):
            act = self.get_actuals()[i]
            frm = self.get_callee().params[i]
            if frm in self.get_callee().consts: act_consts.append(act)
            if frm in self.get_callee().valreses: act_valreses.append(act)
            if frm in self.get_callee().results: act_results.append(act)
        return act_consts,act_valreses,act_results


    def get_actual_values( self ):
        act_consts,act_valreses,_ = self.get_actuals_tuple()
        return act_consts+act_valreses
    

    def get_actual_result_vars( self ):
        _,act_valreses,act_results = self.get_actuals_tuple()
        return map(lambda x:self.idref_str(x["idop"][0]["ID"][0].value,"variable"),
                   act_valreses+act_results)

    def get_arguments( self ):
        act_consts,act_valreses,_ = self.get_actuals_tuple()
        return act_consts+act_valreses


    def is_multi_call( self ):
        return bool(self._labeled_exits)
    

    def idref_check( self ):
        StrategyTrsElement.idref_check(self)
        Branch.idref_check(self)
        callee = self.get_callee()
        if not callee:
            raise ProgramException([ElementError("no such procedure: '%s'"%self.ast[0].value,
                                                   self)])
        for expr in self.get_actuals():
            self._validate_expression(expr)

        declared_posts = dict([(x.get_id_str(),x) for x in callee.posts])
        connected_exits = []
        for lbl,trs in self._labeled_exits:
            if lbl.value not in declared_posts:
                raise ProgramException([ElementError("called procedure '%s' does not declare postcondition '%s'"%(callee.get_id_str(),lbl.value),self,lbl)])
            connected_exits.append((declared_posts[lbl.value],trs))
        self._labeled_exits = connected_exits

        #super(Call,self).idref_check()


    def semantic_check( self, warnings ):
        callee = self.get_callee()

        # check for unconnected exits
        if self.is_multi_call():
            remaining_posts = callee.posts[:]
            for post,_ in self._labeled_exits:
                if post in remaining_posts:
                    remaining_posts.remove(post)
            if remaining_posts:
                warnings.append(ElementWarning("call to procedure '%s' may not be live (missing transitions for postcondition(s) %s)"%(callee.get_id_str(),", ".join(["'%s'"%x.get_id_str() for x in remaining_posts])),self))

        actuals = self.get_actuals()

        errors = []

        # procedures with named posts may not be called statement-like
        if not callee.has_unnamed_post() and self.parent.tail!=self:
            errors.append(ElementError("procedure '%s', which has named postconditions, must be called as a multi-exit statement"%callee.get_id_str(),self))

        # check number of actuals
        if len(actuals)!=len(callee.get_params()):
            errors.append(ElementError("procedure '%s' expects %d parameter(s), %d given"%(callee.get_id_str(),len(callee.get_params()),len(actuals)),
                                         self))

        act_consts,act_valreses,act_results = self.get_actuals_tuple()

        # valres and result parameters must be program variables
        i = len(act_consts)
        for a in act_valreses + act_results:
            if a.type=="name" and a["idop"]:
                self.idref_str(a["idop"][0]["ID"][0].value,"variable")
            else:
                errors.append(ElementError("program variable required for parameter %d of procedure '%s'"%(i,callee.get_id_str()),self))
            i += 1

        if errors:
            raise ProgramException(errors)

        super(Call,self).semantic_check(warnings)
        
        for result in self.get_callee().valreses+self.get_callee().results:
            env=self.parent.env
            id=self.get_callee().get_id_str()+'__'+result.id.id

            symbol=Symbol(id, "variable", result.id.file, result.id.lineno, result.id.pos)

            if symbol not in self.env:
                vb = VariableBinding(self.filename, self.env, None, symbol, result.type)
            else:
                vb = self.env[symbol]
            self.result_vbs.append(vb)


    def semantic_translate( self ):
        super(Call,self).semantic_translate()
        
        generated = []

        generated += list(self.get_assert_havoc())

        if self.is_multi_call():
            choice = Choice(self.parent,None)
            choice.ast = self.ast
            choice.env = self.env
            choice.nestedenv = self.nestedenv

            for (post,trs) in self._labeled_exits:
                trs.parent = choice
                choice.transitions.append(trs)
                assign = self.get_assign(trs)
                if assign:
                    trs.prepend_stmt(assign)
                trs.prepend_stmt(self.get_assume(post,trs))

            generated.append(choice)
            
        else:
            assert self.get_callee().has_unnamed_post()
            generated.append(self.get_assume(self.get_callee().posts[0],self.parent))
            assign = self.get_assign(self.parent)
            if assign:
                generated.append(assign)

        return generated


    def perform_indexing( self ):
        x = super(Call,self).perform_indexing()
        return x


    def get_assert_havoc( self ):
        act_consts,act_valreses,act_results = self.get_actuals_tuple()
        act_result_vars = self.get_actual_result_vars()

        args = act_consts+act_valreses

        stmts = AssertConstraints(self.get_callee().pre,args,self.parent,self.ast),

        # if this is a recursive call and we should verify termination, add decreasing check
        if (self.get_procedure().ctx.in_same_component(self.get_procedure(),self.get_callee()) and
            self.get_callee().variant):
            stmts += (AssertRecursiveBounded(self.get_callee(),args,self.parent,self.ast),
                       AssertRecursiveDecreases(self.get_callee(),args,self.parent,self.ast))

        for c in stmts:
            c.idref_check()
            c.semantic_translate()
            c.trystrategy = self.trystrategy
            
        # if we have result parameters, add havoc:
        if self.result_vbs:
            havoc = Havoc(self.parent,None)
            havoc.ast = self.ast
            havoc.variables = self.result_vbs
            stmts += (havoc,)

        return stmts
        

    def get_assign( self, parent ):
        if self.get_actual_result_vars():
            assign = Assign(parent,None)
            assign.assignables = [vb.get_ID() for vb in self.get_actual_result_vars()]
            assign.expressions = [vb.get_ID() for vb in self.result_vbs]
            assign.idref_check()
            assign.ast = self.ast
            return assign
        else:
            return None


    def get_assume( self, post, parent ):
        # returns a list of tuples (for each postcondition)
        return AssumeConstraints(post,
                                 self.get_arguments()+[vb.get_ID() for vb in self.result_vbs],
                                 parent)

        
    def __str__( self ):
        s =  "%s[%s]"%(self.get_callee().get_id_str(),
                       [expr_to_string(e) for e in self.get_actuals()])
        if self.get_transitions():
            s += "\nCALL\n" + "\n".join(["  "+str(x) for x in self.get_transitions()]) + "\nENDCALL"
        return s
