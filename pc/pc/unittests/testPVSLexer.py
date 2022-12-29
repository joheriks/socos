#!/usr/bin/env python

from pc.parsing.PVSLexer import PVSLexer
from pc.parsing.Token import Token
from pc.parsing.ParseError import ParseException


lexer = PVSLexer()

def string_to_token_list(string):
    lexer.input(string)
    l = []
    t = lexer.token()
    while t:
        l.append(t)
        t = lexer.token()
    return l

def assert_tokenization(string, t_value, t_type, lex = lexer):
    lex.input(string)
    token = lex.token()
    if not (token.value == t_value and token.type == t_type):
        print "Tokenization failed: expected T: %s, V:%r" % (t_type, t_value)
        print "Got: %r" % (token)
        assert False

def assert_fail_tokenization(string, lex = lexer):
    lex.input(string)
    try:
        lex.slurp()
        assert False

    except ParseException:
        assert True

def assert_token(token, value, type, line, pos):
    assert(token.value == value)
    assert(token.type == type)
    assert(token.lineno == line)
    assert(token.lexpos == pos)

def assert_empty(string):
    lexer.input(string)
    assert not lexer.token()

def test_special_symbols():
    assert_tokenization("#", "#", "HASH")
    assert_tokenization("##", "##", "HASH_HASH")
    assert_tokenization("#)", "#)", "HASH_RPAREN")
    assert_tokenization("#]", "#]", "HASH_RBRACKET")
    # Note: this test does not belong here: % is a comment startsymbol
    # assert_tokenization("%", "%", "PERCENTAGE")
    assert_tokenization("&", "&", "AMPERSAND")
    assert_tokenization("&&", "&&", "AMPERSAND_AMPERSAND")
    assert_tokenization("(#", "(#", "LPAREN_HASH")
    assert_tokenization("(:", "(:", "LPAREN_COLON")
    assert_tokenization("(||)", "(||)", "LPAREN_VBAR_VBAR_RPAREN")
    assert_tokenization("(|", "(|", "LPAREN_VBAR")
    assert_tokenization("(", "(", "LPAREN")
    assert_tokenization(")", ")", "RPAREN")
    assert_tokenization("**", "**", "STAR_STAR")
    assert_tokenization("*", "*", "STAR")
    assert_tokenization("++", "++", "PLUS_PLUS")
    assert_tokenization("+", "+", "PLUS")
    assert_tokenization(",", ",", "COMMA")
    assert_tokenization("->", "->", "MINUS_GT")
    assert_tokenization("-", "-", "MINUS")
    assert_tokenization(".", ".", "DOT")
    assert_tokenization("//", "//", "SLASH_SLASH")
    assert_tokenization("/=", "/=", "SLASH_EQUAL")
    assert_tokenization("/\\", "/\\", "SLASH_BSLASH")
    assert_tokenization("/", "/", "SLASH")
    assert_tokenization(":)", ":)", "COLON_RPAREN")
    assert_tokenization("::", "::", "COLON_COLON")
    assert_tokenization(":=", ":=", "COLON_EQUAL")
    assert_tokenization(":", ":", "COLON")
    assert_tokenization(";", ";", "SEMI_COLON")
    assert_tokenization("<<=", "<<=", "LT_LT_EQUAL")
    assert_tokenization("<<", "<<", "LT_LT")
    assert_tokenization("<=>", "<=>", "LT_EQUAL_GT")
    assert_tokenization("<=", "<=", "LT_EQUAL")
    assert_tokenization("<>", "<>", "LT_GT")
    assert_tokenization("<|", "<|", "LT_VBAR")
    assert_tokenization("<", "<", "LT")
    assert_tokenization("==", "==", "EQUAL_EQUAL")
    assert_tokenization("=>", "=>", "EQUAL_GT")
    assert_tokenization("=", "=", "EQUAL")
    assert_tokenization(">=", ">=", "GT_EQUAL")
    assert_tokenization(">>=", ">>=", "GT_GT_EQUAL")
    assert_tokenization(">>", ">>", "GT_GT")
    assert_tokenization(">", ">", "GT")
    assert_tokenization("@@", "@@", "AT_AT")
    assert_tokenization("@", "@", "AT")
    assert_tokenization("[#", "[#", "LBRACKET_HASH")
    assert_tokenization("[]", "[]", "LBRACKET_RBRACKET")
    assert_tokenization("[||]", "[||]", "LBRACKET_VBAR_VBAR_RBRACKET")
    assert_tokenization("[|", "[|", "LBRACKET_VBAR")
    assert_tokenization("[", "[", "LBRACKET")
    assert_tokenization("\\/", "\\/", "BSLASH_SLASH")
    assert_tokenization("\\", "\\", "BSLASH")
    assert_tokenization("]|", "]|", "RBRACKET_VBAR")
    assert_tokenization("]", "]", "RBRACKET")
    assert_tokenization("^^", "^^", "CARET_CARET")
    assert_tokenization("^", "^", "CARET")
    assert_tokenization("`", "`", "SINGLE_QUOTE")
    assert_tokenization("{||}", "{||}", "LBRACE_VBAR_VBAR_RBRACE")
    assert_tokenization("{|", "{|", "LBRACE_VBAR")
    assert_tokenization("{", "{", "LBRACE")
    assert_tokenization("|)", "|)", "VBAR_RPAREN")
    assert_tokenization("|->", "|->", "VBAR_MINUS_GT")
    assert_tokenization("|-", "|-", "VBAR_MINUS")
    assert_tokenization("|=", "|=", "VBAR_EQUAL")
    assert_tokenization("|>", "|>", "VBAR_GT")
    assert_tokenization("|[", "|[", "VBAR_LBRACKET")
    assert_tokenization("|]", "|]", "VBAR_RBRACKET")
    assert_tokenization("||", "||", "VBAR_VBAR")
    assert_tokenization("|}", "|}", "VBAR_RBRACE")
    assert_tokenization("|", "|", "VBAR")
    assert_tokenization("}", "}", "RBRACE")
    assert_tokenization("~", "~", "TILDE")
    assert_tokenization("!", "!", "EXCLAMATION_MARK")

def test_keyword(keyword_map , lexer):
    pvs_string = ''
    keyMap=[]
    for key in keyword_map.keys():
        pvs_string= pvs_string + key+ ' '
        keyMap.append(key)

    count = 0
    lexer.input(pvs_string)
    t = lexer.token()
    while t:
        key = keyMap[count]
        assert(t.type == keyword_map[key])
        assert(t.value == key)
        count= count +1
        t = lexer.token()

def test_strings():
    assert_tokenization('"abc"', 'abc', "STRING")
    assert_tokenization('"abc\\\\"', 'abc\\', "STRING")
    assert_tokenization('"\\"abc\\""', '"abc"', "STRING")
    assert_fail_tokenization('"\\"')

def test_ids():
    assert_tokenization('abc', 'abc', "ID")
    assert_tokenization('q', 'q', "ID")
    assert_tokenization('q123?', 'q123?', "ID")
    assert_tokenization('a_b_c', 'a_b_c', "ID")

def test_whitespace():
    assert_empty('%asdasdasd')
    assert_empty('   \n%')
    assert_empty('%')
    assert_empty('\n')
    assert_empty(' ')
    assert_empty('')

def test_lineNumber():
    s = '"abc" "def"'
    l = string_to_token_list(s)
    assert_token(l[0],'abc','STRING',1,0)
    assert_token(l[1],'def','STRING',1,6)
    
    s = """abc
123
 and x"""
    l = string_to_token_list(s)
    assert_token(l[0], 'abc','ID',1,0)
    assert_token(l[1], '123','NUMBER',2,0)
    assert_token(l[2], 'and','KEY_AND',3,1)
    assert_token(l[3], 'x','ID',3,5)

    s='''"abc
123
" and x'''
    l = string_to_token_list(s)
    assert_token(l[0], 'abc\n123\n','STRING',1,0)
    assert_token(l[1], 'and','KEY_AND',3,2)
    assert_token(l[2], 'x','ID',3,6)


def test_misc():
    s='q'
    l = string_to_token_list(s)
    assert len(l) == 1

def test_all():
    test_special_symbols()
    test_strings()
    test_ids()
    test_whitespace()
    test_keyword(PVSLexer.keyword_map, lexer)
    test_lineNumber()
    test_misc()


if __name__== '__main__':
    test_all()
