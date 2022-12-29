import json
from mod_python import apache
from init_server import *
from pc.session import *

def handler_rewrite(req, request):
    sid = request["session"]
    session = get_session_by_id(sid)
    if not session:
        return handler_error(req,"Invalid session id %s"%sid)
    
    t0 = time.time()
    qed = None
    collision = False
    while True:
        qed = get_qed_instance(sid)
        if qed: break
        time.sleep(0.2)
        collision = True
    if collision:
        debug("COLLISION (WAITED %.5fs)"%(time.time()-t0))
            
    qed.process_id = os.getpid()
    qed.filename = data["filename"] if "filename" in data else "%d.ibp"%sid
    update_qed_process_id(sid, qed.process_id)


    #do the processing here. qed contains the pipes to pvs
    nextterm = "\\\\alpha \\\\Rightarrow \\\\beta \\\\Rightarrow \\\\gamma \\\\Leftrightarrow \\\\delta+a"
    
    release_qed_instance(sid)
    consume_session(sid)

    
    req.write('{\n    "ack": "check",\n    "session": %d,\n    "terms":\n    ["%s"]}' % (sid, nextterm))
    #req.write(json.dumps(response))
    return apache.OK
