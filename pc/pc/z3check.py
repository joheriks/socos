#!/usr/bin/env python
from init_socos import *
from pc.semantic.IBPEnvironment import IBPEnvironment
from pc.semantic.Translation import *
from pc.pp.PVS_Theory_PP import *
from pc.util.Watchdog import *
from pc.parsing.PVSUtils import *

import sys,os
from threading import Timer
import time
import z3

count = 0
proved = 0


class TranslationException( Exception ):
    pass


class Z3Environment( IBPEnvironment ):

    def check( self ):
        self.start()

        for c in self.idref_and_semantic_check(): 
            yield c

        for ctx in self._context_topo_order:
            if ctx in self._prelude_contexts:
                continue
            ctx.semantic_translate()

        for filename in self._given_files:
            #print "=== %s ========================="%filename
            ctx_ids = self._file_contexts[filename]
            ctxs = [self.get_context(cid) for cid in ctx_ids]
            c2z3 = Contexts2Z3(self, ctxs)
            c2z3.check_z3()
            for x in c2z3.unproved:
                if type(x[1])==tuple:
                    yield VerificationResult(x[1],x[0])
                else:
                    yield ElementError(x[1],x[0]) 


class Contexts2Z3( VerifyContexts ):

    def __init__( self, ibpenv, ctxs ):
        VerifyContexts.__init__(self,ibpenv,ctxs)
        #self.constants = { "pi": NAME_IDOP(ID("nnreal")),
        #                   "e": NAME_IDOP(ID("nnreal")) }
        #self.vars = {}
        
        self.env = None
        
        self.unproved = []

    def check_z3( self ):
        p = P("root",[])
        for ctx in self._ctxs:
            self.trs_context(p, ctx)

    def __assert( self, test, msg=None ):
        if not test:
            raise TranslationException(msg or "unable to translate to Z3")


    def __make_z3_const( self, s, t ):
        namemap = {"int": (z3.IntSort(),[]),
                   "real": (z3.RealSort(),[]),
                   "bool": (z3.BoolSort(),[]),
                   "posreal": (z3.RealSort(), [lambda x: x>z3.RealVal(0)] ),
                   "nat": ( z3.IntSort(), [lambda x: x>=z3.IntVal(0)] ),
                   "nnreal": ( z3.RealSort(), [lambda x: x>=z3.RealVal(0)] )}

        var,pred = None,[]
        #print s.id
        if t.type=="functiontype":
            var = apply(z3.Function,[s.id] + [namemap[y.value][0] for y in t["idoptypeexpr.name.idop.ID"] + t["name","idop","ID"]])
            pred = []
        if t.type=="idoptypeexpr": t = t.children[0]
        if t.type=="name":
            basetype = t["idop","ID"][0].value
            if basetype not in namemap:
                raise TranslationException("not supported: %s"%basetype)
            #print "constant -- ", s.id,namemap[basetype][0]
            var = z3.Const(s.id,namemap[basetype][0])
            pred = namemap[basetype][1]
        if not var: raise TranslationException("not supported: %s"%t)
        p = map(lambda x:x(var),pred)
        return var,p
        

    def __var( self, n ):
        s = Symbol(n,"variable")
        if not self.env.is_defined(s):
            raise TranslationException("not known: %s"%n)

        typ,z3var,z3pred = self.env[s]
        if not z3var:
            z3var,z3pred = self.__make_z3_const(s,typ) 
            self.env[s] = (typ,z3var,z3pred)
            
        return z3var


    def __pred( self, n ):
        self.__var(n)
        return self.env[Symbol(n,"variable")][2]


    def __freshvar( self, prefix, sort ):
        i = 0
        while True:
            varname = "%s_%d"%(prefix,i)
            if varname not in self.vars:
                break
            i += 1
        v = Const(varname,sort)
        self.vars[varname] = v
        return v
       

    def trs_derivation( self, p, der ):
        self.derivation = der
        VerifyContexts.trs_derivation(self,p,der)
        
        
    def _theorem( self, theorem_type, elem, vars, assumptions, goals ):
        global count
        global proved
        count += 1

        pvsth = VerifyContexts._theorem(self,theorem_type,elem,vars,assumptions,goals)
        #print PVS_Theory_PP().output_to_string(pvsth),
        self.axioms = []

        try:
            goal = self.pvs2z3(pvsth[1])
            solver = z3.Solver()
            for ax in self.axioms:
                solver.add(ax)
                #print "-  %s" % ax
            solver.add(z3.Not(goal))
            r = z3.unknown
            r = solver.check()
            if r!=z3.unsat:
                self.unproved.append((elem,([str(x) for x in self.axioms],[str(goal)])))
            else:
                proved += 1
        except (TranslationException,z3.Z3Exception),e:
            self.unproved.append((elem,str(e)))
            
        return pvsth

    
    def pvs2z3( self, pvs ):
        if pvs.type=="bindingexpr":
            self.__assert(pvs.children[0].type in ("KEY_FORALL","KEY_EXISTS"),
                          "only universal and existential quantification supported")
            q = pvs[0]
            expr = pvs[-1]
            binds = []
            toplevel = not self.env
            self.env = LexicalEnvironment(self.env)
            binds = []

            if toplevel:
                for vb in self.derivation.get_logicals():
                    self.env.bind_symbol(vb.id,(vb.type,None,None))
            else:
                for lambdabindings in pvs["lambdabindings"]:
                    for bindings in lambdabindings.children:
                        if bindings.type=="bindings":
                            for binding in bindings.children:
                                for typedids in binding["typedids"]:
                                    self.__assert(len(typedids.children)==1,"predicate subtypes not supported")
                                    if typedids["typedids_pre"][0].children[-1].type=="idop":
                                        # no type declaration, use previous type
                                        for x in typedids["typedids_pre.idop.ID"]:
                                            s = Symbol.from_node(x,"variable")
                                            typ = self.env.parent[s][0]
                                            self.env.bind_symbol(s,(typ,None,None),True)
                                            #self.env[Symbol.from_node(x,"variable")]
                                    else:
                                        typ = typedids["typedids_pre"][0].children[-1]
                                        for x in typedids["typedids_pre.idop.ID"]:
                                            s = Symbol.from_node(x,"variable")
                                            self.env.bind_symbol(s,(typ,None,None),True)
                        elif bindings.type=="idop":
                            s = Symbol.from_node(bindings.children[0],"variable")
                            typ = self.env.parent[s][0]
                            self.env.bind_symbol(s,(typ,None,None),True)
                        else:
                            raise TranslationException("unable to translate binding")

            z3expr = self.pvs2z3(expr)

            z3binds = []
            z3preds = []
            for s in self.env.get_locally_defined_symbols():
                t,b,p = self.env[s]
                if b: z3binds.append(b)
                if p: z3preds.extend(p)

            assert not z3preds or z3binds
            
            self.env = self.env.parent

            if toplevel and q.type=="KEY_FORALL":
                self.axioms += z3preds
                return z3expr
            else:
                if q.type=="KEY_FORALL":
                    x = z3.Implies(apply(z3.And,z3preds),z3expr) if z3preds else z3expr
                    return z3.ForAll(z3binds,x) if z3binds else x
                else:
                    x = z3.And(apply(z3.And,z3preds),z3expr) if z3preds else z3expr
                    return z3.Exists(z3binds,x) if z3binds else x
            
        elif pvs.type=="pgroup":
            return self.pvs2z3(pvs.children[0])
        elif pvs.type=="LT_EQUAL_GT" or pvs.type=="EQUAL" or pvs.type=="KEY_IFF":
            return self.pvs2z3(pvs.children[0]) == self.pvs2z3(pvs.children[1])
        elif pvs.type=="SLASH_EQUAL":
            return self.pvs2z3(pvs.children[0]) != self.pvs2z3(pvs.children[1])
        elif pvs.type=="GT_EQUAL":
            return self.pvs2z3(pvs.children[0]) >= self.pvs2z3(pvs.children[1])
        elif pvs.type=="GT":
            return self.pvs2z3(pvs.children[0]) > self.pvs2z3(pvs.children[1])
        elif pvs.type=="LT_EQUAL":
            return self.pvs2z3(pvs.children[0]) <= self.pvs2z3(pvs.children[1])
        elif pvs.type=="LT":
            return self.pvs2z3(pvs.children[0]) < self.pvs2z3(pvs.children[1])
        elif pvs.type=="STAR":
            return self.pvs2z3(pvs.children[0]) * self.pvs2z3(pvs.children[1])
        elif pvs.type=="SLASH":
            return self.pvs2z3(pvs.children[0]) / self.pvs2z3(pvs.children[1])
        elif pvs.type=="MINUS":
            if len(pvs.children)==1: return - self.pvs2z3(pvs.children[0])
            else: return self.pvs2z3(pvs.children[0]) - self.pvs2z3(pvs.children[1])
        elif pvs.type=="PLUS":
            return self.pvs2z3(pvs.children[0]) + self.pvs2z3(pvs.children[1])
        elif pvs.type=="NUMBER":
            # work around bug in z3py
            m = re.match("(^[0-9]*)\.0*$",pvs.value)
            return z3.RealVal(m.group(1)) if m else z3.RealVal(pvs.value)
        if pvs.type=="KEY_TRUE":
            return z3.BoolVal(True)
        elif pvs.type=="KEY_FALSE":
            return z3.BoolVal(False)
        elif pvs.type=="EQUAL_GT":
            return z3.Implies(self.pvs2z3(pvs.children[0]),self.pvs2z3(pvs.children[1]))
        elif pvs.type=="KEY_WHEN":
            return z3.Implies(self.pvs2z3(pvs.children[1]),self.pvs2z3(pvs.children[0]))
        elif pvs.type=="KEY_OR":
            return z3.Or(self.pvs2z3(pvs.children[0]),self.pvs2z3(pvs.children[1]))
        elif pvs.type=="KEY_AND":
            return z3.And(self.pvs2z3(pvs.children[0]),self.pvs2z3(pvs.children[1]))
        elif pvs.type=="KEY_NOT":
            return z3.Not(self.pvs2z3(pvs.children[0]))
        elif pvs.type=="ID":
            return self.__var(pvs.value)
        elif pvs.type=="idop":
            return self.pvs2z3(pvs.children[0])
        elif pvs.type=="name":
            return self.pvs2z3(pvs["idop"][0])
        elif pvs.type=="expression_list_1":
            self.__assert(len(pvs.children)==1)
            return self.pvs2z3(pvs.children[0])
        elif pvs.type=="expression_arguments":
            if pvs["idop","ID"]: name = pvs["idop","ID"][0].value
            elif pvs["name","idop","ID"]: name = pvs["name","idop","ID"][0].value
            else: raise TranslationException("unsupported function application")
            pvsargs = pvs["arguments"][0].children[:]
            args = map(self.pvs2z3,pvsargs)
            if name=="power": return args[0] ** args[1]
            elif name=="square_root":
                return args[0]**(z3.RealVal(1)/z3.RealVal(2))
            elif name=="nth_root":
                self.__assert(len(args)==2)
                self.__assert(pvsargs[1].type=="NUMBER" and pvsargs[1].value.isdigit())
                x = args[0]
                n = args[1]
                rootn = int(pvsargs[1].value)
                self.__assert(rootn>0)
                return x ** (z3.RealVal(1)/n)
            elif name=="abs":
                self.__assert(len(args)==1)
                x = args[0]
                return z3.If(x>=z3.RealVal(0),x,-x)
                #y = self.__freshvar("abs",z3.RealSort())
                #self.axioms.append(z3.And(y>=0,Or(y==x,y==-x)))
                #return y
            else:
                f = self.__var(name)
                if isinstance(f,z3.FuncDeclRef) and len(args)==f.arity(): 
                    return apply(f,args)
                else:
                    raise TranslationException("not a function or wrong number of arguments: %s"%name)
        else:
            raise TranslationException("not supported: %s"%pvs.type)



if __name__=='__main__':
    if len(sys.argv)<2:
        print "no files given"
        sys.exit(1)
    dirs_files = [os.path.split(os.path.abspath(x)) for x in sys.argv[1:]]
    filedir = dirs_files[0][0]
    basenames = [f for (d,f) in dirs_files]
    try:
        e = Z3Environment(socos_dir,filedir,filedir,basenames)
        t0 = time.time()
        for m in e.check():
            print m.get_message()
        print time.time()-t0,"seconds"

    except ProgramException,e:
        # This is the exit point for any kind of failure
        
        print >> sys.stderr, e.get_message()
        sys.exit(1)

    print "%d/%d lemmas proved"%(proved,count)

    
