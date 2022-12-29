# A set of convenvience functions for building PVS syntax trees and
# S-expressions for proof scripts

from pc.parsing.AST import ParentNode as P, LeafNode as L
from pc.util.Utils import *

#
# PVS abstract syntax
#

def PGROUP( s ):
    return P("pgroup",[s])

def ID( s, *rest ):
    return apply(L,("ID",s)+rest)

def NUMBER( s ):
    return L("NUMBER",s)

def NAME_IDOP( *ids ):
    return P("name",[P("idop",[x]) for x in ids])


def binop_reduce( binop, seq, default ):
    if len(seq)==0:
        return default 
    elif len(seq)==1:
        return P("pgroup",[seq[0]])
    else:
        return P(binop,[P("pgroup",[seq[0]]),binop_reduce(binop,seq[1:],default)])


def TRUE():
    return L("KEY_TRUE","true")

def FALSE():
    return L("KEY_FALSE","false")

def EQUALS( a, b ):
    return P("EQUAL",[a,b])

def IMPLIES( a, b ):
    return binop_reduce("EQUAL_GT", [a,b], None)
    return P("EQUAL_GT",[a,b])

def SLASH_BSLASH( conjuncts ):
    return binop_reduce("SLASH_BSLASH",conjuncts,P("name",[P("idop",[TRUE()])]))

def AND( conjuncts ):
    return binop_reduce("KEY_AND",conjuncts,P("name",[P("idop",[TRUE()])]))

def OR( disjuncts ):
    return binop_reduce("KEY_OR",disjuncts,P("name",[P("idop",[FALSE()])]))

def APPLY( expression, arguments ):
    if arguments:
        return P("expression_arguments",[expression,P("arguments",arguments)])
    else:
        return expression

def APPLY_VBS( expression, vbs ):
    if vbs:
        return APPLY(expression,[NAME_IDOP(x.get_ID()) for x in vbs])
    else:
        return expression

def BINDING( id ):
    return P("binding",
             [P("typedids",
                [P("typedids_pre",
                   [P("idop",[id])])])])

def TYPED_BINDING( id, typ ):
    x = BINDING(id)
    x[0][0].children.append(typ)
    return x

def VB( vb ):
    return BINDING(vb.get_ID())

def TYPED_VB( vb ):
    return TYPED_BINDING(vb.get_ID(),vb.type)

def VBS( vbs ):
    return P("bindings",[VB(x) for x in vbs]) if vbs else None

def TYPED_VBS( vbs ):
    return P("bindings",[TYPED_VB(x) for x in vbs]) if vbs else None

def EXISTS( bindings, body ):
    return P("bindingexpr",[L("KEY_EXISTS","exists"),bindings,body])

def FORALL( bindings, body ):
    return P("bindingexpr",[L("KEY_FORALL","forall"),bindings,body])

def LAMBDA( bindings, body ):
    return P("bindingexpr",[L("KEY_LAMBDA","lambda"),P("lambdabindings",[TYPED_VBS(bindings)]),body])

def WRAP_IDENTITY( x ):
    return APPLY(ID("id"),[x])

def SUBTYPE( pred ):
    return P("subtype",[P("expression_list_1",[NAME_IDOP(pred)])])

def FUNCTIONTYPE( dom, ran ):
    return P("functiontype",[dom,ran])


# CASE

def CASE( cons, bindings, value ):
    return P("selection",[cons]+bindings+[value])

#
# Strings
#

def STRING( s ):
    return L("STRING",s)


#
# Building S-expressions
#

def SEXP( elem ):
    if type(elem) in (list,tuple):
        return P("sexp_list",map(SEXP,elem))
    elif type(elem) in (int,float):
        return L("sexp_number",str(elem))
    elif type(elem)==str:
        return L("sexp_symbol",str(elem))
    else:
        return elem

def SEXP_LIST ( *elems ):
    return SEXP(elems)

def SEXP_STRING( elem ):
    return L("sexp_string",str(elem))

def SEXP_PVS( elem ):
    return P("sexp_pvs",[elem])



#
# Traversing/querying ASTs
#

def is_binding_expr( ast ):
    return ast.type in ("let","where","bindingexpr","setexpr")


def get_binding_ids( ast ):
    assert is_binding_expr(ast)
    if ast.type in ("let","where"):
        idops = ast["letbinding","letbind","idop"]
    elif ast.type=="bindingexpr":
        idops = ast["lambdabindings","idop"] + ast["lambdabindings","bindings","binding","typedids","typedids_pre","idop"]
    elif ast.type=="setexpr":
        idops = ast["setbindings","setbinding","idop"]
    return [x[0] for x in idops]

