#! /usr/bin/env python


from mod_python import apache
from mod_python import util
import os
import socket

from init_server import *

#from pc.users import Query

class Query:
    def __init__(self, querystring):
        self.data = util.parse_qs(querystring, True)
        
    def getSingleValueSafe(self, key):
        if key in self.data:
            return self.data[key][0]
        return None

    def getSingleIntValueSafe(self, key, default):
        s = self.getSingleValueSafe(key)
        if s:
            return int(s)
        else:
            return default

 
def handler(req):
    req.content_type = 'text/javascript'
    req.headers_out['Access-Control-Allow-Origin'] = '*'
    req.headers_out['Access-Control-Allow-Methods'] = 'GET, POST, OPTIONS'

    req.add_common_vars()
    port = req.subprocess_env["SERVER_PORT"]
    host = req.subprocess_env["SERVER_NAME"]

    protocol = "http"
    if "HTTPS" in req.subprocess_env.keys() and req.subprocess_env["HTTPS"] == "on":  protocol = "https" 
   

    q = Query(req.subprocess_env['QUERY_STRING'])
    action = q.getSingleValueSafe('action')
    if not action:
        action = ""
    
    course = q.getSingleValueSafe('course')
    if not course:
        course = ""

    pluginname = q.getSingleValueSafe('pluginname')
    if not pluginname: pluginname = host
    
    server = "%s://%s:%s" % (protocol, host, str(port))
    
    current_dir = os.path.dirname(os.path.realpath(__file__))
    jsfile = os.path.join(current_dir, "plugin/plugin.js")
    f = open(jsfile)
    text = f.read().replace("CHECK_SERVER",'"%s"'%server)
    f.close()
    text = text.replace("ACTION", '"%s"' % action)
    text = text.replace("PLUGIN_NAME", '"%s"' % pluginname)
    text = text.replace("COURSE", '"%s"' % course)
    req.write(text)
    return apache.OK

