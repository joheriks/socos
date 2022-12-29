from pc.util.Utils import *
from pc.parsing.AST import *
from pc.parsing.PVSUtils import *
from pc.parsing.ParserUtil import binops
from pc.semantic.Context import *
from pc.util.Utils import *
from pc.pp.PVS_Theory_PP import *

import itertools

import sys
import time
def debug( x ):
    sys.stderr.write("["+time.strftime("%a, %d %b %Y %H:%M:%S", time.localtime())+"] ")
    sys.stderr.write(unicode(x).encode("latin-1","replace")+"\n")
    sys.stderr.flush()

def and_nonstring_exprs( l ):
    assert len(l)>0
    return AND([x for x in l if not x.type=="STRING"])


def ast_to_command( parent, ast ):
    if ast.type=="case": c = Case
    elif ast.type=="use": c = Use
    elif ast.type=="try": c = StepTry
    elif ast.type=="add": c = Add
    elif ast.type=="del": c = Del
    else: assert False,ast
    return c(parent.derivation,ast,parent)


def motivation_to_command_list( parent, ast ):
    assert ast.type=="motivation"
    return [ast_to_command(parent,x) for x in ast.children if x.type in ("case","use","try","add","del")]



class Derivation( Element ):
    def __init__( self, parent, ast ):
        from pc.semantic.Trs import TrsElement
        assert (isinstance(parent,TrsElement) or
                isinstance(parent,Context) or
                isinstance(parent,Motivation))
        
        if isinstance(parent,Context): env = parent.nestedenv
        elif isinstance(parent,Motivation): env = parent.derivation.nestedenv
        else: env = parent.nestedenv
        Element.__init__(self,parent.filename,env)

        self.ast = ast
        self.parent = parent
        self.nestedenv = LexicalEnvironment(env)
        
        if ast["ID"]: self.set_id_from_node(ast["ID"][0])
        else: self.set_id(parent.get_next_id("derivation"),override=True)

        self.init_id_counter("step","step","step")

        # running numbers for anonymous assumptions and observation (only
        # relevant for top-level derivation)        
        if self.is_toplevel():
            self.init_id_counter("assumption","assumption","ass__")
            self.init_id_counter("observation","assumption","obs__")
        
        # declarations
        self.assumptions = []
        self.observations = []
        self.declarations = []
        constdecls = []
        self.logicals = []
        for x in self.ast["derivation_decl"]:
            for val in x.children:
                if val.type=="assumption":
                    self.assumptions.append(Assumption(self,x))
                elif val.type=="observation":
                    self.observations.append(Observation(self,x))

                elif val.type in ("constdecl_list","constdecl_binding","recdecl_list","recdecl_binding"):
                    self.declarations.append(val)
                    constdecls.append(val)

                elif val.type in ("vardecl","pvardecl"):
                    self.declarations.append(val)
                    self.logicals += make_variable_bindings(val,self.filename,self.nestedenv)

        bind_constdecls(constdecls,self.filename,self.nestedenv)

        # motivation
        self.motivation = Motivation(self,self.ast["motivation"][0] if self.ast["motivation"] else None,self) 

        # steps
        self.steps = []
        if ast["chain"]:
            x = ast["chain"][0]
            while x.type=="chain":
                st = Step(self, x)
                self.steps.append(st)
                if len(x)<3: break
                else: x = x[2]

        if ast["simplify"]:
            self.goal = SimplifyGoal(self, ast["simplify"][0]) 
        elif ast["solve"]:
            self.goal = SolveGoal(self, ast["solve"][0]) 
        elif ast["goal"]:
            self.goal = Goal(self, ast["goal"][0]) 
        else:
            self.goal = None


    def get_strategy( self ):
        return self.parent.get_strategy()

    
    def get_inherited_logicals( self ):
        from pc.semantic.Trs import TrsElement
        if isinstance(self.parent,Context): logicals = self.parent.get_logicals()
        elif isinstance(self.parent,Step): logicals = self.parent.derivation.get_logicals()
        elif isinstance(self.parent,Motivation): logicals = self.parent.derivation.get_logicals()
        else: logicals = []
        return filter(lambda x:not some(lambda y:y.id==x.id,self.logicals),logicals)

    def get_logicals( self ):
        return self.get_inherited_logicals() + self.logicals


    def get_collected_assumptions( self ):
        return ([] if self.is_toplevel() else self.parent.derivation.get_collected_assumptions()) + self.assumptions


    def get_collected_observations( self ):
        return ([] if self.is_toplevel() else self.parent.derivation.get_collected_observations()) + [o for o in self.observations if o.goal.parse_ok]

    def is_lemma_ok(self):
        return not self.goal or self.goal.parse_ok


    def get_lemma( self ):
        if not self.is_lemma_ok():
            return None
        assumptions = AND([PGROUP(x.predicate) for x in self.assumptions])
        if self.goal: conclusion = self.goal.get_predicate()
        else: conclusion = AND([PGROUP(x.get_predicate()) for x in self.steps if x.parse_ok])
        return IMPLIES(assumptions,conclusion) 


    def idref_check( self ):
        if self.motivation: self.motivation.idref_check()
        if self.declarations and not self.is_toplevel():
            raise ProgramException([ElementError("declarations not allowed in subderivations",self,self.declarations[0])])

        for o in self.observations: o.idref_check()
        for s in self.steps: s.idref_check()


    def semantic_check( self, warnings ):
        if not self.should_check_goal():
            warnings.append(ElementWarning("formula '%s' not checked"%(self.get_id_str()),self))
        if self.goal: self.goal.semantic_check(warnings)
        for o in self.observations: o.semantic_check(warnings)
        for s in self.steps: s.semantic_check(warnings)


    def get_idtype( self ):
        return "derivation"


    def get_attribute( self ):
        return "derivation"


    def is_toplevel( self ):
        from pc.semantic.Trs import TrsElement
        return isinstance(self.parent,Context) or isinstance (self.parent,TrsElement)


    def get_toplevel( self ):
        if self.is_toplevel(): return self
        else: return self.parent.derivation.get_toplevel()


    def get_context( self ):
        t = self.get_toplevel().parent
        if isinstance(t,Context): return t
        else: return t.get_procedure().ctx


    def get_prev_uid_part( self ):
        if isinstance(self.parent,Motivation):
            return self.parent.derivation
        else:
            return self.parent


    def get_next_uid_env( self ):
        # Returns the environment in which this element binds nested elements to symbols
        return self.nestedenv


    def should_check_goal( self ):
        return bool(self.ast["check"])

    def get_last_term(self):
        if self.steps:
            return self.steps[-1].t2[0]
        return None


class DerivationItem( Element ):

    # Superclass for all items (sans subderivations) nested inside a derivation.

    def __init__( self, derivation, ast ):
        assert isinstance(derivation, Derivation)
        Element.__init__(self, derivation.filename, derivation.nestedenv)
        self.derivation = derivation
        self.ast = ast

    def get_strategy( self ):
        return self.derivation.get_strategy()
    
    def get_prev_uid_part( self ):
        return self.derivation

    def get_next_uid_env( self ):
        # Returns the environment in which this element binds nested elements to symbols
        return self.env


class Goal( DerivationItem ):

    def __init__( self, derivation, ast ):
        DerivationItem.__init__(self, derivation, ast)
        self.set_id_from_str("goal", override = True)
        self.parse_ok = ast.parse_ok

    def get_theorem_type( self ):
        return self.ast[0].value.lower()

    def get_predicate( self ):
        if not self.parse_ok:
            return None
        return and_nonstring_exprs(self.ast.children[1:])
    
    def get_idtype( self ):
        return "derivation"
    
    def get_attribute( self ):
        return "goal"

    def semantic_check(self, warnings):
        pass

    def get_sage_predicate(self):
        pp = PVS_Theory_PP()
        s = pp.output_to_string(self.predicate)
        s = s.replace('\n', ' ')
        s = s.replace('=', '==')
        return s

class ObservationGoal( Goal ):

    def __init__( self, obs, ast ):
        Goal.__init__(self, obs.derivation, ast)
        self.set_id_from_str(str(obs.id) + "__goal", override=True) 


class SimplifyGoal( Goal ):

    def __init__( self, derivation, ast ):
        Goal.__init__(self,derivation,ast)

    def semantic_check(self, warnings):
        if not self.derivation.steps:
            raise ProgramException([ElementError("missing simplification",self,self.ast[0])])
    
    def get_predicate( self ):
        if not self.parse_ok:
            return None
        return EQUALS(PGROUP(self.ast[0]),
                      PGROUP(self.derivation.get_last_term()))
    
    def get_theorem_type( self ):
        return "theorem"

class SolveGoal( SimplifyGoal ):

    def __init__( self, derivation, ast):
        SimplifyGoal.__init__(self, derivation, ast)

    def semantic_check(self, warnings):
        if not self.derivation.steps:
            raise ProgramException([ElementError("missing solution", self, self.ast[0])])
 
class Step( DerivationItem ):

    def __init__( self, derivation, ast ):
        DerivationItem.__init__(self, derivation, ast)
        assert ast.type=="chain"
        self.t1 = ast[0]
        if len(ast)==3:
            self.t2 = ast[2] if ast[2].type=="term" else ast[2][0]
        else:
            self.t2 = None

        self.set_id(derivation.get_next_id("step"),override=True)
        d = ast[1]
        rel =  d["relation"][0]
        self.relation = rel[0]

        self.parse_ok = self.t1.parse_ok and (not self.t2 or self.t2.parse_ok) and rel.parse_ok

        self.motivation = Motivation(derivation, d["motivation"][0] if d["motivation"] else None, self)


    def get_predicate( self ):
        # for binary relation, generate an infix application
        if not self.parse_ok:
            return None
        if self.relation.type=="idop" and self.relation.children[0].type in binops:
            return PGROUP(ParentNode(self.relation.children[0].type,[PGROUP(self.t1[0]),PGROUP(self.t2[0])]))
        else:
            return APPLY(self.relation,[self.t1[0],self.t2[0]]) if self.t2 else None

    sage_infix_operators = {"=": "=="}

    def sage_APPLY(self, rel, e1, e2 ):
        pp = PVS_Theory_PP()
        
        s1 = pp.output_to_string(e1).replace('\n', ' ')
        s2 = pp.output_to_string(e2).replace('\n', ' ')
        rel = pp.output_to_string(rel)
        if rel in self.sage_infix_operators:
            return "(" + s1 + ") " + self.sage_infix_operators[rel] + " (" + s2 + ")"
        else:
            return rel + "(" + s1 + ", " + s2 + ")"
    
    def get_sage_predicate( self ):
        return self.sage_APPLY(self.relation, self.t1[0], self.t2[0]) if self.t2 else None


    def idref_check( self ):
        self.motivation.idref_check()


    def semantic_check( self, warnings ):
        if not self.t2:
            warnings.append(ElementWarning("incomplete step '%s' not checked"%(self.get_id_str()),
                                           self))
        self.motivation.semantic_check(warnings)


    def get_idtype( self ):
        return "step"
    

    def get_attribute( self ):
        return "step"



class StepTry( Try ):

    def __init__( self, derivation, ast, parent ):
        Try.__init__(self,derivation.get_context(),ast,parent.get_next_id("command"))



class Command( DerivationItem ):

    def __init__( self, derivation, ast, parent ):
        DerivationItem.__init__(self,derivation,ast)
        self.parent = parent
        self.set_id(self.parent.get_next_id("command"),override=True)
    
    def get_attribute( self ):
        return "command"

    def get_idtype( self ):
        return "command"

    def idref_check( self ):
        pass

    def semantic_check( self, warnings ):
        pass


class Motivation( DerivationItem ):

    def __init__( self, derivation, ast, parent ):
        DerivationItem.__init__(self,derivation,ast)
        assert (ast==None or ast.type=="motivation")
        assert isinstance(parent,Step) or isinstance(parent,Derivation) or isinstance(parent,Observation)
        self.parent = parent
        self.init_id_counter("command","command","cmd")
        self.commands = motivation_to_command_list(self,ast) if ast else []
        self.init_id_counter("derivation","derivation","sub")
        self.subderivations = [Derivation(self,x) for x in ast["derivation"]] if ast else []


    def get_strategy( self ):
        strategy = self.parent.get_strategy()
        for cmd in self.commands:
            cmd.idref_check()
            if isinstance(cmd,StepTry):
                strategy = cmd.get_updated_strategy(strategy)
        return strategy


    def idref_check( self ):
        for cmd in self.commands:
            cmd.idref_check()
        for sub in self.subderivations: sub.idref_check()
            

    def semantic_check( self, warnings ):
        for cmd in self.commands: cmd.semantic_check(warnings)
        for sub in self.subderivations: sub.semantic_check(warnings)


    def get_available_assumptions( self ):
        x = []
        m = self
        while isinstance(m, DerivationItem):
            if isinstance(m.parent, Step) or isinstance(m.parent, Derivation):
                x = m.derivation.assumptions + [o for o in m.derivation.observations if o.goal.parse_ok] + x
                m = m.derivation.parent
            elif isinstance(m.parent, Observation):
                x = m.derivation.assumptions + [o for o in m.derivation.observations[:m.derivation.observations.index(m.parent)] if o.goal.parse_ok] + x
                m = m.derivation.parent
        return x
        
    def get_used_assumptions( self ):
        available = self.get_available_assumptions()
        
        ass = dict([(a, a.always_add) for a in available])
        
        for cmd in self.commands:
            if isinstance(cmd,Add):
                for a in cmd.contents: ass[a] = True
            if isinstance(cmd,Del):
                for a in cmd.contents: ass[a] = False
                    
        return [a for a in available if ass[a]]
    

    def get_used_lemmas( self ):
        return remove_duplicates(flatten([[a for a in c.contents if isinstance(a,Derivation)] for c in self.commands if isinstance(c,Use)]))
        
    


class Add( Command ):

    def __init__( self, derivation, ast, parent ):
        Command.__init__(self,derivation,ast,parent)
        self.contents = []

    def idref_check( self ):
        for c in self.ast.children:
            if c.type=="assumption_all": self.contents += self.derivation.get_collected_assumptions()
            if c.type=="observation_all": self.contents += self.derivation.get_collected_observations()
            if c.type=="assumption_id": self.contents.append(self.idref_str(c[0].value,"assumption"))
            if c.type=="assumption_index": self.contents.append(self.idref_str("ass__%d"%int(c[1].value),"assumption"))
            if c.type=="observation_index": self.contents.append(self.idref_str("obs__%d"%int(c[1].value),"assumption"))



class Del( Add ):
    pass



class Use( Command ):

    def __init__( self,derivation,ast,parent ):
        Command.__init__(self,derivation,ast,parent)
        assert ast.type=="use"
        self.lemma = self.ast[0].value

    def idref_check( self ):
        pass



class Case( Command ):

    def __init__( self,derivation,ast,parent ):
        Command.__init__(self,derivation,ast,parent)
        assert ast.type=="case"
        self.expression = self.ast[0]
        self.by = self.ast[1].value if len(self.ast.children)>1 else None
        
    def idref_check( self ):
        pass



class Assumption( DerivationItem ):

    def __init__( self, derivation, ast ):
        DerivationItem.__init__(self,derivation,ast)

        assert ast.type=="derivation_decl"

        # Increment auto-id counter even if we have an id, to get consistent numbering
        autoid = self._auto_id()

        self.predicate = None
        self.always_add = False
        for c in ast.children:
            if c.type=="ID": self.set_id_from_node(c)
            elif c.type=="KEY_ADD": self.always_add = True
            elif c.type=="assumption": self.predicate = c[0]
            elif c.type=="observation": self.predicate = c[0]
        assert self.predicate

        if not self.id:
            self.set_id(autoid)
        
            
    def _auto_id( self ):
        return self.derivation.get_toplevel().get_next_id("assumption")

    def get_idtype( self ):
        return "assumption"

    def get_attribute( self ):
        return "assumption"



class Observation( Assumption ):   
    def __init__( self, derivation, ast ):
        Assumption.__init__(self, derivation, ast)
        #self.motivation = Motivation(derivation, ast["motivation"][0], self)
        self.motivation = Motivation(derivation, ast["motivation"][0], self)
        self.goal = ObservationGoal(self, ast["observation"][0])

    def _auto_id( self ):
        return self.derivation.get_toplevel().get_next_id("observation")


    def get_attribute( self ):
        return "observation"


    def idref_check( self ):
        self.motivation.idref_check()


    def semantic_check( self, warnings ):
        self.motivation.semantic_check(warnings)


    def get_used_assumptions( self ):
        return []


    def get_used_lemmas( self ):
        return []
