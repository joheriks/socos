# -*- coding: utf-8 -*-
import MySQLdb as mdb

from datetime import date
from datetime import timedelta
from table import *

class TableSession(Table):
    def __init__(self):
        Table.__init__(self, "Session")
        self.addField(FieldId())
        self.addField(FieldInt("UserId", label = "User Id"))
        self.UserId.foreignkey = ForeignKey("UserId", "User", "Id", ForeignKey.cascade, ForeignKey.cascade)     
        self.addField(FieldInt("Group", label = "Group", notnull = True))
        self.addField(FieldStrNonEmpty("RemoteAddress", 30, label = "Remote address"))
        self.addField(FieldDateTime("ExpireTime", label = "ExpireTime"))
