#!/usr/bin/env python

import init_socos
import sys
import os

if 'clean' in []:#sys.args:
    pass
else:
    if not os.path.exists("parsetabs"):
        os.makedirs("parsetabs")
 
    from pc.parsing.TeXParser import *
    from pc.parsing.PVSParser import PVSParser

    from pc.parsing.IBPParser import *
    from pc.parsing.UidParser import UidParser
    from pc.parsing.SEXPParser import SEXPParser

    PVSParser(write_tables=True, debug=True)

    for p in TeXExprParserClasses.values():
        p(write_tables=True, debug=True)

    for p in TeXIdListParserClasses.values():
        p(write_tables=True, debug=True, start = "id_list")

    for p in TeXDeclParserClasses.values():
        p(write_tables=True, debug=True)
    
    IBPParser(write_tables=True, debug=True)
    IBPParserStart("context_part")(write_tables=True, debug=True)
    IBPParserStart("goal")(write_tables=True, debug=True)
    IBPParserStart("assumption")(write_tables=True, debug=True)
    IBPParserStart("term")(write_tables=True, debug=True)
    IBPParserStart("motivation_part_list")(write_tables=True, debug=True)
    IBPParserStart("ibp_expression")(write_tables=True, debug=True)
    IBPParserStart("relation")(write_tables=True, debug=True)
    IBPParserStart("motivation")(write_tables=True, debug=True)

    UidParser(write_tables=True, debug=True)
    SEXPParser(write_tables=True, debug=True)


 
