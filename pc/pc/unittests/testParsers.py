#!/usr/bin/env python

from pc.parsing.AST import tree_to_str
from pc.parsing.PVSParser import *
from pc.parsing.IBPParser import *
from pc.parsing.ParseError import ParseException
from glob import glob
import re


def skip_empty_or_comment_lines(file):
    line = file.readline()
    while (len(line) == 1 or re.match(r'^\s*#', line)):
        line = file.readline()

    return line

def skip_comment_lines(file):
    line = file.readline()
    while (re.match(r'^\s*#', line)):
        line = file.readline()

    return line

def test_exp_file(filename, parser):
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

            expected_tree = []
            line = skip_empty_or_comment_lines(file)
            while (len(line) > 1):
                expected_tree.append(line)
                line = skip_comment_lines(file)

            expected_tree = ''.join(expected_tree).strip()

            if (test_name.startswith("ignore:")):
                print "WARNING: test %s ignored" % test_name[len("ignore:"):]
            else:
                try:
                    parsed_tree = tree_to_str(parser.parse(test_exp)).strip()
                except ParseException, inst:
                    print "Test %s FAILED. Expression %s" % (test_name, test_exp)
                    print "Parse error: %s" % (str(inst))
                    ret = 1
                    break

                if (parsed_tree != expected_tree):
                    print "Test %s FAILED. Expression: %s" % (test_name, test_exp)
                    print "Expected tree:"
                    print expected_tree
                    print "Parsed tree:"
                    print parsed_tree
                    ret = 1
                    break

            test_name = skip_empty_or_comment_lines(file)
    finally:
        file.close()

    return ret

if __name__=='__main__':
    import sys

    exit_val = 0

    # Instantiate one parser of each type
    parsers = ( PVSParser(), IBPParser() )
    patterns = ( '*.ipet', '*.ibpt' )

    for i, pattern in enumerate(patterns):
        test_files = glob(pattern)
        for test in test_files:
            try:
                if test_exp_file(test, parsers[i]):
                    exit_val = 1
                    break
            except (IOError, EOFError), inst:
                print "test: unable to read test file " + test + ": " + str(inst)
                print "test FAILED"
                exit_val = 1
                break

    sys.exit(exit_val)
