from init_socos import *

from pc.util.SharedData import SharedData

import os
import time

data_dir = os.environ.get("SOCOS_DATA_DIR")

server_config = {}
with SharedData(os.path.join(data_dir,"config.json"),lambda x:x,save=False) as data:
    server_config = data

host_id = server_config["id"]

defer_hosts = server_config["defer_hosts"]

validate_user = server_config["validate_user"]

max_processes = server_config["max_processes"]


def debug( x ):
    sys.stderr.write("["+time.strftime("%a, %d %b %Y %H:%M:%S", time.localtime())+"] ")
    sys.stderr.write(unicode(x).encode("latin-1","replace")+"\n")
    sys.stderr.flush()

