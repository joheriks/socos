from mod_python import apache
from mod_python import util

from init_server import *

from pc.parsing.TeXParser import *
from pc.pp.PVS_Theory_PP import *

def handler(req):
    req.content_type = 'text/plain'
    req.headers_out['Access-Control-Allow-Origin'] = '*'
    req.headers_out['Access-Control-Allow-Methods'] = 'GET, POST, OPTIONS'

    x = util.FieldStorage(req)
    tex = x.get("translate", None)
    parser = x.get("parser", "")
    decimalsep = x.get("decimalsep", "")
    tuplesep = x.get("tuplesep", "")

    p = GetTeXExprParserClass(parser,decimalsep, tuplesep)()
    if tex:
        pp = PVS_Theory_PP()
        try:
            t = p.parse(tex)
            s = ""
            s = pp.output_to_string(t)
            s += "\n"
            s += tree_to_str(t)
        except ParseException,e:
            s = str(e)
        req.write(s)
    else:
        req.write("Parser: \"" + parser + "\"\n")
        req.write("Decimal separator: \"" + decimalsep + "\"\n")
        req.write("Tuple separator: \"" + tuplesep + "\"\n\n\n")
        req.write(p.get_grammar_string())
    
    return apache.OK

