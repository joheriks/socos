'''
Module for utility classes/function with no particular
dependencies/associations to the project
'''

import types
from itertools import imap, izip, count

def invert(fun):
    """ Returns a function lambda x : not fun(x) """
    return lambda x : not fun(x)

def repeat(fun, times):
    """ Call fun (which should not take arguments) specified number of times """
    i = 0
    while i < times:
        fun()
        i += 1

def ienumerate(lst):
    ''' Lazy enumeration over lst. See module itertools '''
    return izip(count(), lst)

def mapcan(fun, lst):
    """ Like map (on one list), except result is constructed with extend
    instead of append """
    result = []
    for e in imap(fun, lst):
        result.extend(e)

    return result

def some(predicate, *lst):
    """ Returns True if there is an element in list for which
    predicate is true, otherwise False. If multiple lists are given,
    the predicate is called with arguments taken from zip of
    lists. Behaviour is undefined if the lists are not of equal
    length"""

    for e in izip(*lst):
        if predicate(*e):
            return True

    return False

def every(predicate, *lst):
    """ Returns True if predicate is true for every element in list(s)
    otherwise False. See some also """

    for e in izip(*lst):
        if not predicate(*e):
            return False

    return True

def flatten(lst):
    """ Flattens lists inside lst (non-destructive)"""
    result = []
    for e in lst:
        if type(e) is types.ListType:
            result += flatten(e)
        else:
            result.append(e)
    return result

def find(element, lst):
    """ Returns first occurance of element in lst, or None if
    lst does not contain element"""
    for e in lst:
        if element == e:
            return e

def find_when(predicate, lst):
    """ Returns first element in lst for which predicate is True, or None if
    lst does not contain such an element"""
    for e in lst:
        if predicate(e):
            return e

def position(predicate, lst):
    """ Returns position of first element in lst for which predicate
    is True, or None if lst does not contain such an element"""
    for i, e in ienumerate(lst):
        if predicate(e):
            return i

def partition(predicate, lst):
    """ Returns a tuple of two lists, the first containing elements in
    list for which predicate is true, the second containing the rest
    of the elements in lst"""

    good = []
    bad = []

    for e in lst:
        if predicate(e):
            good.append(e)
        else:
            bad.append(e)

    return (good, bad)

def remove_duplicates(lst):
    """ Removes duplicates from lst (non-destructive). Does not
    guarantee that order of lst elements is retained """

    # Stupid O(n^2) brute force implementation.
    # If list elements are hashable, a list -> dict -> list
    # conversion should be used instead. Fix if time allows

    result = []
    for e in lst:
        if not e in result:
            result.append(e)

    return result

def remove_duplicates2(lst, cmpf):
    """ Removes duplicates from lst (non-destructive). Does not
    guarantee that order of lst elements is retained """

    # Stupid O(n^2) brute force implementation.
    # If list elements are hashable, a list -> dict -> list
    # conversion should be used instead. Fix if time allows

    result = []
    for e in lst:
        found = False
        for r in result:
            if cmpf(e, r):
                found = True
                break
        if not found:
            result.append(e)

    return result


def reflexive_transitive_closure( R, x, visited=None ):
    """R is a relation (function X -> seq[X]) over elements of
    the same type as x. Returns the reflexive transitive
    closure of R from x as a set"""
    if not visited:
        visited = set()
    visited.add(x)
    for y in R(x):
        if y not in visited:
            visited.update(reflexive_transitive_closure(R,y,visited))
    return visited
    


def transitive_closure( R, x ):
    """R is a relation (function X -> seq[X]) over elements of
    the same type as x. Returns the transitive closure of R from
    x as a set"""
    c = set()
    for y in R(x):
        c.update(reflexive_transitive_closure(R,y,c))
    return c


def find_sccs( nodes, edges ):
    # Returns a list of strongly connected components.
    # 'edges' is a function Node -> [Node].

    # build SCCs, discarding singletons, and ignoring cut
    # transitions (inefficient as hell, could probably be
    # optimized using e.g Tarjan)
    reachable = dict([(x,transitive_closure(edges,x)) for x in nodes])

    components = []
    remaining = nodes[:]
    while remaining:
        x = remaining[0]
        c = [y for y in remaining if x in reachable[y] and y in reachable[x]]
        if c: components.append(c)
        remaining = filter(lambda z:z!=x and z not in c,remaining)

    return components


def prefix_lines( s, p ):
    """Returns string s with each line prefixed by string p"""
    return p + s.replace("\n","\n"+p)


def prefix_with_ida( s ):
    """Returns s prefixed with the correct indefinite article ("a" or" an")"""
    assert len(s.strip())>0
    if s.strip()[0] in ["a","e","i","o"]: return "an "+s
    else: return "a "+s


def reverse_lookup(d, v):
    """Reverse lookup of value v in dictionary d"""
    for k in d:
        if d[k] == v:
            return k
    raise ValueError


def ensure_list(l):
    """If l is a list, identity; otherwise return [l]"""
    return l if type(l)==list else [l]

