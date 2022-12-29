#!/usr/bin/env python
# -*- coding: latin-1 -*-
# Experimental interface to remote Socos checker

import sys
import httplib
import urllib
import urlparse

# if available, use simplejson for efficienct
try:
    import simplejson as json
except ImportError:
    import json
    
import signal
import time
import traceback
import cStringIO

headers = { "Content-Type": "application/x-www-form-urlencoded",
            "Accept": "text/plain" }

def encode_cmd( cmd ):
    return urllib.urlencode({'request':json.dumps(cmd,encoding="latin-1")})
                             

class CheckerSession( object ):

    def __init__( self, url="localhost:8081", sid=None ):
        if url.startswith("http://"):
            parts = urlparse.urlparse(url)
            self.host = parts.netloc
            self.path = parts.path + "/check"
        else:
            self.host = url
            self.path = "/check"
        self.connection = None
        self.sid = sid
        self.messages = []


    def send_request( self, cmd, timeout=None ):
        assert type(cmd) in (dict,str), "string or JSON required"
        if type(cmd)==str:
            cmd = json.loads(cmd)
        if timeout:
            self.connection = httplib.HTTPConnection(self.host, timeout=timeout)
        else:
            self.connection = httplib.HTTPConnection(self.host)
	self.connection.request("POST",self.path,encode_cmd(cmd),headers)
	return self.connection.getresponse()


    def call_server( self, cmd, timeout=None ):
	rsp = self.send_request(cmd, timeout)
        retval = []
        sb = cStringIO.StringIO()
        while 1:
            s = rsp.read(1)
            if s=="" or s=="\n":
                d = sb.getvalue()
                if d.strip():
                    try:
                        j = json.loads(d)
                        self.messages.append(j)
                        yield j
                    except ValueError,e:
                        raise CheckerException("Unable to decode message (%s)"%e.message,d)
                sb = cStringIO.StringIO()
            else:
                sb.write(s)
            if s=="":
                break


    def get_session_id( self, arguments={}, attempts=60, timeout=20 ):
        c = {"cmd": "get_session_id"}
        c.update(arguments)
        msg = None
        self.sid = None
        for attempt in range(attempts):
            try:
                for msg in self.call_server(c, timeout):
                    if "session" in msg:
                        self.sid = int(msg["session"])
            except Exception,e:
                raise CheckerException(repr(e))
            if self.sid: break
            time.sleep(1)
            #if msg:
            #    print "==================================================",repr(msg)
            
        if self.sid is None:
            raise CheckerException("unable to acquire session",msg)


    def kill( self, arguments={} ):
        c = {"cmd": "kill",
             "session": self.sid}
        c.update(arguments)
        return self.call_server(c)


    def check( self, data, strategy ):
        if self.sid is None:
            raise CheckerException("no session id acquired")
        
        cmd = { "cmd":"check",
                "session":self.sid,
                "data":data,
                "strategy":strategy }
        return self.call_server(cmd)

    


class CheckerException( Exception ):

    def __init__( self, msg, server_response=None ):
        Exception.__init__(self,msg)
        self.server_response = server_response


    def __str__( self ):
        s = self.message
        if self.server_response:
            s += "(%s)"%self.server_response
        return s
