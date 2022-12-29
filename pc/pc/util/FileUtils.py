import select
import time
import os


def write_with_timeout( f, x, timeout ):
    _,w,_ = select.select([],[f],[],timeout)
    if w:
        f.write(x)
        return True
    else:
        return False


def read_with_timeout( f, timeout ):
    r,b,c = select.select([f],[],[],timeout)
    if r:
        return os.read(f.fileno(),1)
    else:
        return None
