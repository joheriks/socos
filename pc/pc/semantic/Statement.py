from pc.semantic.Context import *
from pc.semantic.Trs import *

from pc.pp.PVS_Expression_Printer import expr_to_string


class AtomicStatement( TrsElement ):

    """Base class for atomic statements"""

    def __init__( self, parent, ast ):
        super(AtomicStatement,self).__init__(parent,ast)
        self.set_id(parent.get_next_id("statement"),override=True)


    def idref_check( self ):
        super(AtomicStatement,self).idref_check()


    def semantic_check( self, warnings ):
        super(AtomicStatement,self).semantic_check(warnings)


    def semantic_translate( self ):
        return [self]


    def get_accessibles( self ):
        idx = self.parent.statements.index(self)
        if idx==0:
            return self.parent.get_accessibles()
        else:
            return self.parent.statements[idx-1].get_accessibles_after()
        

    def get_accessibles_after( self ):
        return self.get_accessibles()

    
    def _has_indexed_form(cls, id):
        for i in range(len(id)-1):
            if id[-i:].isdigit():
                if id[-i-1]=='_':
                    return True
        return False
    _has_indexed_form = classmethod(_has_indexed_form)



class AtomicStrategyStatement( StrategyTrsElement, AtomicStatement  ):

    def idref_check( self ):
        super(AtomicStrategyStatement,self).idref_check()
        
##     def __init__( self, parent, ast ):
##         super(AtomicStrategyStatement,self).__init__(parent,ast)
##         self.init_strategy(self.get_procedure().ctx,ast)

##     get_strategy = StrategyElement.get_strategy

##     def idref_check( self ):
##         super(AtomicStrategyStatement,self).idref_check()

##     def get_default_strategy( self ):
##         return self.parent.get_strategy()
        


class Assert( AtomicStrategyStatement ):

    def __init__( self, parent, ast=None ):
        super(Assert,self).__init__(parent,ast)
        self.predicate = None
        if ast:
            if ast.children:
                self.predicate = self.ast[0]
            else:
                self.predicate = FALSE()

        if ast:
            self.init_id_counter('derivation', 'derivation', 'der')
            self.derivations = [Derivation(self,d) for d in self.ast['derivation']]

        self.assumption_id=None


    def idref_check( self ):
        super(Assert,self).idref_check()
        self._validate_expression(self.predicate)
        for der in self.derivations:
            der.idref_check()


    def semantic_translate(self):
        # append assume statement to user defined assertion
        assume = Assume(self.parent,None)
        assume.predicate=self.predicate
        assume.ast=self.ast
        assume.assumption_id=Symbol("assert", None, None)
        return [self, assume]


    def perform_indexing(self):
        self.predicate=self.substitute_indexed_mutables(self.predicate)
        return [self]


    def get_attribute( self ):
        return "assert"


    def __str__( self ):
        return "{%s}"%(expr_to_string(self.predicate))



class Assume( AtomicStatement ):

    def __init__( self, parent, ast ):
        AtomicStatement.__init__(self,parent,ast)
        self.predicate = self.ast[0] if ast else None


    def idref_check( self ):
        self._validate_expression(self.predicate)


    def semantic_check( self, warnings ):
        if not (self.parent.statements.index(self)==0 and isinstance(self.parent.parent,If)):
            warnings.append(ElementWarning("assumption used, procedure '%s' may not be live"%(self.get_procedure().get_id_str()),
                                           self))


    def semantic_translate(self):
        return [self]


    def perform_indexing(self):
        self.predicate=self.substitute_indexed_mutables(self.predicate)
        return [self]


    def get_attribute( self ):
        return "assume"


    def __str__( self ):
        return "[%s]"%(expr_to_string(self.predicate))



class Assign( AtomicStatement ):

    def __init__( self, parent, ast ):
        AtomicStatement.__init__(self,parent,ast)

        self.assignables = self.ast["left"][0].children[:] if ast else []
        self.expressions = self.ast["right"][0].children[:] if ast else []

        # list of VariableBindings, set at idref_check!
        self.variables = []
        

    def idref_check( self ):
        assert self.assignables and len(self.expressions)==len(self.assignables)
        self.variables = [self.idref(x,"variable") for x in self.assignables]
        for expr in self.expressions:
            self._validate_expression(expr)


    def semantic_check( self, warnings ):
        assert self.assignables
        
        # check const correctness (this should be handled in a nicer
        # way, however at the present the only way to introduce
        # constants into the namespace is through (const) parameters
        for x in self.variables:
            if x in self.get_procedure().consts:
                raise ProgramException([ElementError("assignment to constant parameter '%s' is not allowed"%x.get_id_str(),self)])
            elif x in self.get_procedure().initconsts: 
                raise ProgramException([ElementError("assignment to initial-value constant '%s' is not allowed"%x.get_id_str(),self)])

        # check for multiple occurence of a variable on LHS
        processed = set()
        warned = set()
        for x in self.assignables:
            if x in processed and x not in warned:
                warned.add(x)
                warnings.append(ElementWarning("variable '%s' occurs multiple times on left hand side of assignment"%x.get_id_str(),
                                               self))
            processed.add(x)

        # only target situation variables may occur on LHS
        for x in self.variables:
            if x not in remove_duplicates(self.get_procedure().locals + self.parent.get_source_situation().get_variables() +
                                          mapcan(lambda l:l.target.get_variables(),self.parent.get_leaves())):
                raise ProgramException([ElementError("assignment to variable '%s' is out of scope"%x.get_id_str(),self)])


    def semantic_translate( self ):
        # if a variable occurs multiple times on the left hand side, use the last
        # assignment and discard the intermediate values
        i = len(self.assignables)-1
        while i>=0:
            if self.assignables[i] in self.assignables[i+1:]:
                del self.assignables[i]
                del self.expressions[i]
                del self.variables[i]
            i = i-1
        return [self]


    def perform_indexing( self ):
        expressions = map(self.substitute_indexed_mutables,self.expressions)

        havoc = Havoc(self.parent,None)
        havoc.ast = self.ast
        havoc.variables = [self.get_indexed_vb(vb) for vb in self.variables]
        havoc.variables = self.variables[:]
        havoc = havoc.perform_indexing()[0]

        assume = Assume(self.parent,None)
        assume.ast = self.ast
        assume.predicate = AND([EQUALS(x.get_ID(),
                                       P("COLON_COLON",[PGROUP(e),x.type])) for (x,e) in zip(havoc.variables,expressions)])

        return [havoc,assume]


    def get_accessibles_after( self ):
        acc = self.get_accessibles()
        return acc + [x for x in self.variables if x not in acc]
    

    def get_attribute( self ):
        return "assign"


    def __str__( self ):
        if self.variables: vars = [x.get_id_str() for x in self.variables]
        else: vars = [expr_to_string(x) for x in self.assignables]
        vals = [expr_to_string(e) for e in self.expressions]
        return "%s:=%s"%(",".join(vars),",".join(vals))



class Havoc( AtomicStatement ):

    def __init__( self, parent, ast ):
        AtomicStatement.__init__(self,parent,ast)
        self.parameters = self.ast.children if ast else []
        self.variables = []


    def idref_check( self ):
        assert self.parameters
        self.variables = [self.idref(x,"variable") for x in self.parameters]
        

    def get_attribute( self ):
        return "havoc"


    def perform_indexing(self):
        for v in self.variables:
            # ensure that an instance of the base variable is generated
            self.get_indexed_vb(v)
        
            # increase index for havoced variable
            self.env.inc_index(v.id)

        havoc2 = Havoc(self.parent,None)
        havoc2.ast = self.ast
        havoc2.variables = [self.get_indexed_vb(vb) for vb in self.variables]
        return [havoc2]


    def get_accessibles_after( self ):
        acc = self.get_accessibles()
        return acc + [x for x in self.variables if x not in acc]


    def __str__( self ):
        if self.variables: vars = [x.get_id_str() for x in self.variables]
        else: vars = [expr_to_string(x) for x in self.parameters]
        return "%s:=?"%(",".join(vars))


#
# The following are similar to Assert/Assume, but on the level of ConstraintSets instead
# of literal formulas. Generate precondition checks for calls.
#

class AssertConstraints( AtomicStrategyStatement ):

    def __init__( self, cs, args, parent, call_ast ):
        super(AssertConstraints,self).__init__(parent,call_ast)
        assert isinstance(cs,ConstraintSet)
        self.cs = cs
        self.args = args
        self.obligations = []

        if call_ast:
            self.init_id_counter('derivation', 'derivation', 'der')
            self.derivations = [Derivation(self,d) for d in call_ast['derivation']]
            
    def idref_check(self):
        super(AssertConstraints,self).idref_check()
        for der in self.derivations:
            der.idref_check()
            

    def semantic_translate( self ):
        super(AssertConstraints,self).semantic_translate()
        for c in self.cs.get_constraints():
            self.obligations.append(Check(c,self))
        return [self]


    def perform_indexing(self):
        self.args = [self.substitute_indexed_mutables(x) for x in self.args]
        return [self]


    def get_attribute( self ):
        return "assert_precondition"


    def __str__( self ):
        return "{{%s(%s)}}"%(self.cs.get_id_str(),",".join(map(expr_to_string,self.args)))



class AssumeConstraints( AtomicStatement ):

    def __init__( self, cs, args, parent ):
        AtomicStatement.__init__(self,parent,None)
        assert isinstance(cs,ConstraintSet)
        self.cs = cs
        self.args = args


    def perform_indexing(self):
        self.args = [self.substitute_indexed_mutables(x) for x in self.args]
        return [self]


    def get_attribute( self ):
        return "assume_postcondition"


    def __str__( self ):
        return "[[%s(%s)]]"%(self.cs.get_id_str(),",".join(map(expr_to_string,self.args)))



class AssertRecursiveDecreases( AtomicStrategyStatement ):

    def __init__( self, callee, args, parent, call_ast ):
        super(AssertRecursiveDecreases,self).__init__(parent,call_ast)
        assert isinstance(callee,Procedure)
        self.callee = callee
        self.args = args


    def perform_indexing(self):
        self.args = [self.substitute_indexed_mutables(x) for x in self.args]
        return [self]


    def get_attribute( self ):
        return "assert_recursive_decreases"


    def __str__( self ):
        return "{{variant(%s) decreases}}"%(self.callee.get_id_str())



class AssertRecursiveBounded( AssertRecursiveDecreases ):

    def __str__( self ):
        return "{{variant(%s) bounded}}"%(self.callee.get_id_str())
