import signal
import fcntl
import json
import os

class SharedData:
    # This class ensures mutually exclusive access to the given data file
    # using file locking. Use as follows:
    #
    # with access_data_file("file.data",data_class) as data:
    #     # critical section manipulating data goes here
    #

    def __init__( self, fname, cls=None, save=True ):
        self.fname = fname
        self.cls = cls
        self.save = save
        self.fd = None
        self.data = None

    def __enter__( self ):
        self.__open_data_file()
        self.__read_data_file()
        return self.data

    def __exit__( self, type, value, traceback ):
        if self.save:
            self.__close_save_data_file()
        else:
            self.__close_data_file()
	
        
    def __open_data_file( self ):
        # ignore SIGINT (sent by kill request) while accessing the data
        signal.signal(signal.SIGINT,signal.SIG_IGN)

        if os.path.exists(self.fname):
            self.fd = open(self.fname,"rb+")
        else:
            self.fd = open(self.fname,"wb+")
        fcntl.lockf(self.fd.fileno(),
                    fcntl.LOCK_EX if self.save else fcntl.LOCK_SH,
                    0)

    def __read_data_file( self ):
        if self.cls:
            try:
                self.data = self.cls(json.load(self.fd))
            except (IOError,EOFError,ValueError), e:
                self.data = self.cls(None)

    def __close_data_file( self ):
        # restore SIGINT
        signal.signal(signal.SIGINT,signal.SIG_DFL)
        fcntl.lockf(self.fd.fileno(),fcntl.LOCK_UN,0)
        self.fd.close()

    def __close_save_data_file( self ):
        self.fd.seek(0)
        self.fd.truncate()
        json.dump(self.data.serial(),self.fd)
        self.fd.flush()
        self.__close_data_file()
        
