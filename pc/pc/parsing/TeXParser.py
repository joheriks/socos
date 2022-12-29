from pc.parsing.TeXMultiParser import *
from pc.parsing.TeXSingleParser import *

TeXMultiExprParserA = GetTexMultiExprParser("a", decimal_sep = ".", tuple_sep = ",")
TeXMultiIdListParserA = GetTexMultiExprParser("a_id", decimal_sep = ".", tuple_sep = ",")
TeXMultiDeclParserA = GetTexMultiDeclParser("a", TeXMultiExprParserA)

TeXMultiExprParserB = GetTexMultiExprParser("b", decimal_sep = ",", tuple_sep = ",")
TeXMultiIdListParserB = GetTexMultiExprParser("b_id", decimal_sep = ",", tuple_sep = ",")
TeXMultiDeclParserB = GetTexMultiDeclParser("b", TeXMultiExprParserB)

TeXMultiExprParserC = GetTexMultiExprParser("c", decimal_sep = ",", tuple_sep = ";")
TeXMultiIdListParserC = GetTexMultiExprParser("c_id", decimal_sep = ",", tuple_sep = ";")
TeXMultiDeclParserC = GetTexMultiDeclParser("c", TeXMultiExprParserC)

TeXMultiExprParserD = GetTexMultiExprParser("d", decimal_sep = ",", tuple_sep = ",|;")
TeXMultiIdListParserD = GetTexMultiExprParser("d_id", decimal_sep = ",", tuple_sep = ",|;")
TeXMultiDeclParserD = GetTexMultiDeclParser("d", TeXMultiExprParserD)

TeXSingleExprParserA = GetTexSingleExprParser("a", decimal_sep = ".", tuple_sep = ",")
TeXSingleIdListParserA = GetTexSingleExprParser("a_id", decimal_sep = ".", tuple_sep = ",")
TeXSingleDeclParserA = GetTexSingleDeclParser("a", TeXSingleExprParserA)

TeXSingleExprParserB = GetTexSingleExprParser("b", decimal_sep = ",", tuple_sep = ",")
TeXSingleIdListParserB = GetTexSingleExprParser("b_id", decimal_sep = ",", tuple_sep = ",")
TeXSingleDeclParserB = GetTexSingleDeclParser("b", TeXSingleExprParserB)

TeXSingleExprParserC = GetTexSingleExprParser("c", decimal_sep = ",", tuple_sep = ";")
TeXSingleIdListParserC = GetTexSingleExprParser("c_id", decimal_sep = ",", tuple_sep = ";")
TeXSingleDeclParserC = GetTexSingleDeclParser("c", TeXSingleExprParserC)

TeXSingleExprParserD = GetTexSingleExprParser("d", decimal_sep = ",", tuple_sep = ",|;")
TeXSingleIdListParserD = GetTexSingleExprParser("d_id", decimal_sep = ",", tuple_sep = ",|;")
TeXSingleDeclParserD = GetTexSingleDeclParser("d", TeXSingleExprParserD)

default_parser = "multi"
default_decimal_sep = "."
default_tuple_sep = ","

TeXExprParserClasses = {
    "multi.," :   TeXMultiExprParserA,
    "multi,," :   TeXMultiExprParserB,
    "multi,;" :   TeXMultiExprParserC,
    "multi,,|;" : TeXMultiExprParserD,
    "multi,;|," : TeXMultiExprParserD,

    "single.," :  TeXSingleExprParserA,
    "single,," :  TeXSingleExprParserB,
    "single,;" :  TeXSingleExprParserC,
    "single,,|;" :TeXSingleExprParserD,
    "single,;|," :TeXSingleExprParserD,
}

TeXIdListParserClasses = {
    "multi.," :   TeXMultiIdListParserA,
    "multi,," :   TeXMultiIdListParserB,
    "multi,;" :   TeXMultiIdListParserC,
    "multi,,|;" : TeXMultiExprParserD,
    "multi,;|," : TeXMultiExprParserD,

    "single.," :  TeXSingleIdListParserA,
    "single,," :  TeXSingleIdListParserB,
    "single,;" :  TeXSingleIdListParserC,
    "single,,|;" :TeXSingleIdListParserD,
    "single,;|," :TeXSingleIdListParserD,
}

TeXDeclParserClasses = {
    "multi.," :   TeXMultiDeclParserA,
    "multi,," :   TeXMultiDeclParserB,
    "multi,;" :   TeXMultiDeclParserC,
    "multi,,|;" : TeXMultiDeclParserD,
    "multi,;|," : TeXMultiDeclParserD,

    "single.," :   TeXSingleDeclParserA,
    "single,," :   TeXSingleDeclParserB,
    "single,;" :   TeXSingleDeclParserC,
    "single,,|;" : TeXSingleDeclParserD,
    "single,;|," : TeXSingleDeclParserD,
}

def GetTeXExprParserClass(parser = None, decimal_sep = None, tuple_sep = None):
    parser = parser or default_parser
    decimal_sep = decimal_sep or default_decimal_sep
    tuple_sep = tuple_sep or default_tuple_sep
    key = parser + decimal_sep + tuple_sep
    if not key in TeXExprParserClasses.keys():
        raise ParseException([ParseError("The parser %s is not supported" % key)])
    TeXExprParser = TeXExprParserClasses[key]
    return TeXExprParser

def GetTeXIdListParserClass(parser = None, decimal_sep = None, tuple_sep = None):
    parser = parser or default_parser
    decimal_sep = decimal_sep or default_decimal_sep
    tuple_sep = tuple_sep or default_tuple_sep
    key = parser + decimal_sep + tuple_sep
    if not key in TeXIdListParserClasses.keys():
        raise ParseException([ParseError("The parser %s is not supported" % key)])
    TeXIdListParser = TeXIdListParserClasses[key]
    return TeXIdListParser


def GetTeXDeclParserClass(parser = None, decimal_sep = None, tuple_sep = None):
    parser = parser or default_parser
    decimal_sep = decimal_sep or default_decimal_sep
    tuple_sep = tuple_sep or default_tuple_sep
    key = parser + decimal_sep + tuple_sep
    if not key in TeXExprParserClasses.keys():
        raise ParseException([ParseError("The parser %s is not supported" % key)])
    TeXDeclParser = TeXDeclParserClasses[key]
    return TeXDeclParser
