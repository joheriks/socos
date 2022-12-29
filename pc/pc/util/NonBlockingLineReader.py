
from collections import deque
from threading import Thread, Lock, Semaphore


class NonBlockingLineReader(object):
    '''
    Implements a non-blocking (threaded) line reader.
    '''

    def __init__(self, f_handle):
        '''
        Constructor. Parameters:
          f_handle: The file-like object that will be read from. All available
                    lines will be stored in memory, so using this on huge files
                    is probably not a good idea.
        '''
        self._f_handle = f_handle
        self._queue = deque()

        self._thread = Thread(target=self._blocking_readline)
        self._thread.setDaemon(True)
        self._lock_q = Lock()
        self._lock_f = Lock()
        self._sem_q = Semaphore(0)

        # Go!
        self.closed = False
        self._thread.start()


    def readline(self, wait=False):
        '''
        Read a line from the file. Parameters:
          wait: Specifies whether to wait for some item to become available
                if line buffer is empty.
        Returns the first line from the buffer. The line includes the trailing
        newline char. If wait is false, an empty string is returned if buffer
        is empty.

        Raises an IOError if the file could not be read.
        '''

        if self._sem_q.acquire(wait):
            self._lock_q.acquire()
            if self._queue:
                line = self._queue.popleft()
            else:
                # This shouldn't happen, right?.
                line = ''
            self._lock_q.release()
        else:
            line = ''

        if isinstance(line, Exception):
            raise line

        return line


    def close(self):
        '''
        Closes the underlying file object. After this any calls to readline()
        will return nothing.
        '''
        self._lock_f.acquire()
        self._f_handle.close()
        self._lock_f.release()

        # Clear queue so that readline() returns nothing.
        self._lock_q.acquire()        
        self._queue.clear()
        self._lock_q.release()

        self.closed = True


    def _blocking_readline(self):
        '''
        This method runs in the thread and adds lines or exceptions to the
        line buffer.
        '''
        stop = False
        while not stop:
            try:
                # readline() throws a ValueError when f_handle is closed
                # while waiting, and sometimes an IOError (??). We throw
                # an IOError ourselves if readline() returns nothing.
                self._lock_f.acquire()
                line = self._f_handle.readline()
                self._lock_f.release()
                if not line:
                    raise IOError('File was closed')
            except IOError, error:
                line = error
                stop = True
            except ValueError:
                line = IOError('File was closed')
                stop = True

            self._lock_q.acquire()
            self._queue.append(line)
            self._lock_q.release()

            # Increase semaphore counter.
            self._sem_q.release()

