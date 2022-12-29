#import atexit
#import codecs
#import errno
#import fcntl
#import getopt
#import json
#import os
#import random
#import re
#import shutil
#import signal
#import sys
#import time

from init_server import *


#import pc.semantic.Message
#from pc.client import *
#from pc.util.SharedData import SharedData


class session_data:
    def __init__(self, serial = None):
        if serial:
            self.next_sid = serial["next_sid"]
            self.sessions = serial["sessions"]
        else:
            self.next_sid = 0
            self.sessions = []
            
    def serial(self):
        return {"sessions": self.sessions,
                "next_sid": self.next_sid}

    def get_new_session(self, source, target, target_sid ):
        sid = self.next_sid
        self.next_sid += 1
        session = {"id":sid,"source":source,"target":target,"target_sid":target_sid}
        self.sessions.append(session)
        return session

    def delete_session( self, sid ):
        self.sessions = filter(lambda x:x["id"]!=sid,self.sessions)

    def get_session_by_id( self, sid ):
        for session in self.sessions:
            if session["id"]==sid:
                return session
        return None


def get_new_session( source, target, target_sid ):
    with SharedData(os.path.join(data_dir,"session.json"),session_data) as data:
        session = data.get_new_session(source,target,target_sid)
        return session
    
def get_session_by_id( sid ):
    with SharedData(os.path.join(data_dir,"session.json"),session_data,save=False) as data:
        session = data.get_session_by_id(sid)
        return session

def consume_session( sid ):
    with SharedData(os.path.join(data_dir,"session.json"),session_data) as data:
        data.delete_session(sid)
        
class qed_instance:
    def __init__(self, session = -1, id = -1, serial = None):
        # when self.session > 0 then the pvs process is busy 
        self.session = session
        # self.id is used for creating the pvs context directory 
        # as well as to create the pvs pipes
        self.id = id
        
        # this must be set when creating the pvs process and
        # it will be used to kill the pvs process.
        self.process_id = -1
        self.filename = None
        
        if serial:
            self.session = serial["session"]
            self.id = serial["id"]
            self.process_id = serial["process_id"]
            self.filename = serial["filename"]
        
    def serial(self):
        return {"session": self.session,
                "id": self.id,
                "process_id": self.process_id,
                "filename": self.filename}

    def get_filename( self ):
        return self.filename

    def get_context_dir(self):
        return os.path.join(data_dir, "%d"%self.id)

    def get_pipe_name(self):
        return os.path.join(self.get_context_dir(), "pipe")
    
    def kill( self ):
        if self.process_id == -1:
            return False
        # kill the process with id process_id
        debug("killing pid: %d" % self.process_id)
        os.kill(self.process_id,signal.SIGINT)
        self.process_id = -1
        return True


class qed_data:
  def __init__(self, serial = None):
      # used to generate unique pvs ids
      self.id = 0
      # stores all checker instances
      self.qed_instances = []
      if serial:
          self.id = serial["last_id"]
          for x in serial["qed_instances"]:
               self.qed_instances.append(qed_instance(serial = x))

  def serial(self):
      qed_instances = []
      for x in self.qed_instances: 
          qed_instances.append(x.serial()) 
      return {"last_id": self.id, "qed_instances": qed_instances}
      
  def get_qed_instance(self, session):
      # if the maximum number of processes have already been spawned,
      # return None
      if (max_processes is not None and
          len(self.get_active_processes())>=max_processes):
          return None
      
      for x in self.qed_instances:
          if x.session < 0:
              x.session = session
              return x

      self.id = self.id + 1
      x = qed_instance(session, self.id)
      self.qed_instances.append(x)
      return x

  def update_qed_process_id(self, session, pid ):
      for x in self.qed_instances:
          if x.session == session:
              x.process_id = pid
              return True
      return False

  def release_qed_instance(self, session):
      for x in self.qed_instances:
          if x.session == session:
              x.session = -1
              return True
      return False

  def kill_qed_instance(self, session):
      retval = False
      qed_instances = []
      for x in self.qed_instances:
          if x.session == session:
              retval = x.kill()
          else:
              qed_instances.append(x)
      self.qed_instances = qed_instances
      return retval

  def get_active_processes(self):
      return filter(lambda x:x.session >= 0, self.qed_instances)

  def get_available_processes(self):
      return filter(lambda x:x.session < 0, self.qed_instances)


def get_qed_instance(session):
    with SharedData(os.path.join(data_dir,"qed.json"),qed_data) as data:
        return data.get_qed_instance(session)
    
def update_qed_process_id(session,pid):
    with SharedData(os.path.join(data_dir,"qed.json"),qed_data) as data:
        data.update_qed_process_id(session,pid)

def release_qed_instance(session):
    with SharedData(os.path.join(data_dir,"qed.json"),qed_data) as data:
        return data.release_qed_instance(session)

def kill_qed_instance(session):
    with SharedData(os.path.join(data_dir,"qed.json"),qed_data) as data:
        return data.kill_qed_instance(session)

def get_active_process_count():
    with SharedData(os.path.join(data_dir,"qed.json"),qed_data,save=False) as data:
        return len(data.get_active_processes())

