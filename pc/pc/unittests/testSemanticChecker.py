#!/usr/bin/env python

from pc.parsing.AST import tree_to_str
from pc.parsing.IBPParser import IBPParser
from pc.parsing.ParseError import ParseException
from pc.semantic.IBPEnvironment import IBPEnvironment
from pc.parsing.AST import ParentNode as P, LeafNode as L
from pc.parsing.UidParser import UidParser
from pc.semantic.Message import *

from glob import glob
from StringIO import StringIO
import re


def skip_empty_or_comment_lines(file):
    line = file.readline()
    while (line == '\n' or re.match(r'^\s*#', line)):
        line = file.readline()

    return line


def start_fail_report( test_name, test_exp ):
    print "Test %s FAILED, expression:\n  %s"%(test_name,test_exp)
    print "Details:"  
    
def end_fail_report():
    print "-------------------------------------"

def test_semantic_file(filename, parser):
    file = open(filename)
    ret = 0

    try:
        line = skip_empty_or_comment_lines(file)
        test_name = line

        while test_name:
            test_name = test_name.strip()
            # If test name starts with 'file:', next non-empty line is a file
            # name instead of an expression. The file contains the expression.
            if test_name.startswith('file:'):
                path = skip_empty_or_comment_lines(file).strip()
                try:
                    f_handle = open(path, 'rt')
                    test_exp = f_handle.read()
                    f_handle.close()
                except IOError, error:
                    print "Test %s FAILED for expression file '%s': %s." % \
                        (test_name[5:], path, error.strerror)
                    ret = 1
                    break
            else:
                test_exp = skip_empty_or_comment_lines(file).strip()

            pass_test = test_name.endswith(":pass")
            err_test = test_name.endswith(":error")
            warn_test= test_name.endswith(":warn")

            if test_name.startswith("ignore:"):
                print "WARNING: test %s ignored" % test_name[len("ignore:"):]
            else:

                #try:
                #    parsed_tree = tree_to_str(parser.parse(test_exp)).strip()
                #    parsed_tree = parser.parse(test_exp)
                #except ParseException, inst:
                #    start_fail_report(test_name,test_exp)
                #    print "Parse error: %s" % (str(inst))
                #    end_fail_report()
                #    ret = 1
                stream = StringIO(test_exp)
                
                env = IBPEnvironment("dummy", # directories are not used
                                     "dummy", 
                                     "dummy", 
                                     [(test_name,stream)],
                                     ["noprelude"])
                warnings = []
                error = None
                try:
                    env.start()
                    warnings = env.idref_and_semantic_check()
                except ProgramException,e:
                    error = e

                if ((pass_test and (error or warnings)) or
                    (warn_test and error)):
                    start_fail_report(test_name,test_exp)
                    if error:
                        print error.get_message()
                    else:
                        print ProgramException(warnings).get_message()
                    end_fail_report()
                    ret = 1

                if ((err_test and not error) or
                    (warn_test and not warnings)):
                    print error,warnings
                    start_fail_report(test_name,test_exp)
                    end_fail_report()
                    ret = 1


            test_name = skip_empty_or_comment_lines(file)
    finally:
        file.close()

    return ret


if __name__ == '__main__':

    import sys
    import os

    exit_val = 0
    os.putenv("IBPPATH",".")

    parser =  IBPParser()
    pattern = ( '*.sct' )

    test_files = glob(pattern)
    for test in test_files:
        try:
            if test_semantic_file(test, parser):
                exit_val = 1
                break
        except (IOError, EOFError), inst:
            print "test: unable to read test file " + test + ": " + str(inst)
            print "test FAILED"
            exit_val = 1
            break

    sys.exit(exit_val)
