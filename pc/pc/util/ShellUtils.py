import subprocess 
import os

def find_executable( tool, env=None ):
    p = subprocess.Popen(["which",tool],
                         stdout=subprocess.PIPE,
                         stderr=subprocess.PIPE,
                         env=env)
    out,_ = p.communicate()
    toolpath = out.strip()
    if p.returncode!=0 or not (os.path.exists(toolpath)):
        return None
    else:
        return toolpath
