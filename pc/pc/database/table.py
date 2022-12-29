# -*- coding: utf-8 -*-
import MySQLdb as mdb

from datetime import date
from datetime import timedelta

import sys
EncryptKey = 'socos!check@deriv*pvs'

class Status:
    pending_email, pending, active = range(3)
    names = ["pending email", "pending", "active"]
    public = names

class Group:
    guest, admin, user = range(3)
    names = ["quest", "admin", "user"]
    public = {admin:names[admin], user:names[user]}

class ForeignKey:
    restrict, cascade, setnull, noaction = ("RESTRICT", "CASCADE", "SET NULL", "NO ACTION")
    
    def __init__(self, colnames, reftable, refcolnames, ondelete = None, onupdate = None):
        if type(colnames) == str:
            self.colnames = [colnames]
        else:
            self.colnames = colnames
        
        if type(refcolnames) == str:
            self.refcolnames = [refcolnames]
        else:
            self.refcolnames = refcolnames

        self.reftable = reftable
        self.ondelete = ondelete
        self.onupdate = onupdate

    def getSql(self):
        colnames = ", ".join('`%s`' % x for x in self.colnames)
        refcolnames = ", ".join('`%s`' % x for x in self.refcolnames)
        sql = "FOREIGN KEY (%s) REFERENCES `%s` (%s)" % (colnames, self.reftable, refcolnames)
        if self.ondelete:
            sql += " ON DELETE %s" % self.ondelete
        if self.onupdate:
            sql += " ON UPDATE %s" % self.onupdate
        return sql


class Field:
    def __init__(self, name, type, label = "", notnull = None, default = None, unique = None):
        self.name = name
        self.type = type
        self.default = default
        self.notnull = notnull
        self.unique = unique
        self.label = label
        self.ok = None
        self.value = None
        self.error = []
        self.foreignkey = None

    def display(self):
        return self.value

    def getCreateSql(self):
        sql = "`%s` %s" % (self.name, self.type)
        if self.notnull:
            sql += " NOT NULL"
        if self.unique:
            sql += " UNIQUE"
        if self.default != None:
            sql += " DEFAULT %s"
        return sql, self.default

    def getInsertUpdateSql(self):
        if self.value != None:
            sql = "`%s` = %s" % (self.name, "%s")
            return sql, (self.value,)
        else:
            return None, (None,)

    def validate(self):
        self.ok = True
        return self.ok

class FieldStr(Field):
    def __init__(self, name, size, label = "", notnull = None, default = None, unique = None):
        Field.__init__(self, name, "VARCHAR(%d)" % size, label, notnull, default, unique)
        self.size = size

    def validate(self):
        if len(self.value) > self.size:
            self.error += ["The size of '%s' must be smaller or equal to %d." % (self.name, self.size)]
            self.ok = False
        else:
            self.ok = True
        return self.ok

class FieldStrNonEmpty(FieldStr):
    def __init__(self, name, size, label = "", notnull = None, default = None, unique = None):
        FieldStr.__init__(self, name, size, label, notnull, default, unique)
        
    def validate(self):
        FieldStr.validate(self)
        if not self.value:
            self.error += ["The fields '%s' must not be empty." % self.name]
            self.ok = False
        return self.ok
            
class FieldBinary(Field):
    def __init__(self, name, size, label = "", notnull = None, default = None, unique = None):
        Field.__init__(self, name, "VARBINARY(%d)" % size, label, notnull, default, unique)
        self.size = size

class FieldInt(Field):
    def __init__(self, name, label = "", notnull = None, default = None, unique = None):
        Field.__init__(self, name, "INT", label, notnull, default, unique)

class FieldRadio(FieldInt):
    def __init__(self, name, display_values, label = "", notnull = None, default = None, unique = None):
        FieldInt.__init__(self, name, label, notnull, default, unique)
        self.display_values = display_values
        
    def display(self):
        return self.display_values.names[self.value]

class FieldDate(Field):
    def __init__(self, name, label = "", notnull = None, default = None, unique = None):
        Field.__init__(self, name, "DATE", label, notnull, default, unique)

    def display(self):
        return self.value.isoformat()

class FieldDateTime(Field):
    def __init__(self, name, label = "", notnull = None, default = None, unique = None):
        Field.__init__(self, name, "DATETIME", label, notnull, default, unique)

    def display(self):
        return self.value.isoformat()

class FieldId(Field):
    def __init__(self, name = "Id", type = "INT PRIMARY KEY AUTO_INCREMENT"):
        Field.__init__(self, name, type)

class FieldPassword(FieldBinary):
    def __init__(self, name = "Password", size = 1200, label = "Password", notnull = True, default = None, unique = None):
        FieldBinary.__init__(self, name, size, label, notnull, default, unique)

    def getInsertUpdateSql(self):
        if self.value != None:
            sql = "%s = HEX(AES_ENCRYPT(%s, %s))" % (self.name, "%s", "%s")
            return sql, (self.value, EncryptKey)
        else:
            return None, (None,)

class Table:
    def __init__(self, name):
        self.name = name
        self.fields = []
        self.error = []

    def addField(self, x):
        self.fields += [x]
        setattr(self, x.name, x)

    def getCreateSql(self):
        fields = None
        defaults = ()
        foreignkeys = None
        for x in self.fields:
            sql, default = x.getCreateSql()
            if fields:
                fields += ",\n%s" % sql
            else:
                fields =  sql
            if default != None:
                defaults += default,
        
            if x.foreignkey:
                if not foreignkeys:
                    foreignkeys = x.foreignkey.getSql()
                else:
                    foreignkeys += ", %s" % x.foreignkey.getSql()
        if foreignkeys:
            fields += ", %s" % foreignkeys
        
        sql = "CREATE TABLE IF NOT EXISTS `%s`(%s) ENGINE = InnoDB" % (self.name, fields)

        return sql, defaults

    def getInserUpdateSql(self):
        fields = None
        values = ()
        for x in self.fields:
            sql, value = x.getInsertUpdateSql()
            if sql:
                if fields:
                    fields += ",\n%s" % sql
                else:
                    fields =  sql
                values += value
        return fields, values

    def getInsertSql(self):
        fields, values = self.getInserUpdateSql()
        sql = "INSERT INTO `%s` SET %s" % (self.name, fields)
        return sql, values

    def getUpdateSql(self, where, wherevalues):
        fields, values = self.getInserUpdateSql()
        sql = "UPDATE `%s` SET %s" % (self.name, fields)
        if where:
            sql += " WHERE %s" % where
            values += wherevalues
        return sql, values
        

    def getError(self):
        error = self.error
        for x in self.fields:
            error += x.error
        return error

    def create(self, con):
        sql, defaults = self.getCreateSql()
        cur = con.cursor()
        print "Create Table: ", sql, defaults
        cur.execute(sql, defaults)
        con.commit()
        cur.close()

    def insert(self, con):
        try:
            sql, values = self.getInsertSql()
            cur = con.cursor()
            print "Insert into table: ", sql, values
            cur.execute(sql, values)
            con.commit()
            cur.execute("SELECT LAST_INSERT_ID()")
            id = cur.fetchone()
            cur.close()
            return id[0]
        except Exception as err:
            self.error = [err[1]]
            #print err[0], err[1]
            return None
        
    def getSelectAll(self):
        return ", ".join(["`%s`" % x.name for x in self.fields])

    def update(self, con, where, wherevalues):
        sql, values = self.getUpdateSql(where, wherevalues)
        cur = con.cursor()
        n = cur.execute(sql, values)
        con.commit()
        cur.close()
        return n
        
    def updateId(self, con, id):
        where, wherevalues = "`Id` = %s", (id,)
        return self.update(con, where, wherevalues)

    def readOne(self, con, where, wherevalues):
        cur = con.cursor()
        sql = "SELECT %s FROM `%s`" % (self.getSelectAll(), self.name)
        if where:
            sql += "WHERE %s" % where
        cur.execute(sql, wherevalues)
        data = cur.fetchone()
        if not data:
            return False
        i = 0
        for x in self.fields:
            x.value = data[i]
            i = i + 1
        return True

    def readAll(self, con, where, wherevalues):
        cur = con.cursor()
        sql = "SELECT %s FROM `%s`" % (self.getSelectAll(), self.name)
        if where:
            sql += "WHERE %s" % where
        cur.execute(sql, wherevalues)
        self.allrecords = cur.fetchall()
        self.current = 0
        self.total = len(self.allrecords)
        cur.close()

    def updateNext(self):
        if self.current < self.total:
            data =  self.allrecords[self.current]
            i = 0
            for x in self.fields:
                x.value = data[i]
                i = i + 1
            self.current += 1
            return True
        return False

class TableUser(Table):
    def __init__(self):
        Table.__init__(self, "User")
        self.addField(FieldId())
        self.addField(FieldStrNonEmpty("Email", 200, label = "Email", notnull = True, unique = True))
        self.addField(FieldStrNonEmpty("FirstName", 100, label = "First name", notnull = True))
        self.addField(FieldStrNonEmpty("LastName", 100, label = "Last name", notnull = True))
        self.addField(FieldRadio("Group", Group, label = "Group", notnull = True, default = Group.user))
        self.addField(FieldRadio("Status", Status, label = "Status", notnull = True, default = Status.pending_email))
        self.addField(FieldDate("EndDate", label = "End date", notnull = True, default = "2000-01-01"))
        self.addField(FieldInt("CheckNumber", label = "Check number", notnull = True, default = 0))
        self.addField(FieldPassword())

    #def getAccessKey(self, con, id):
        #cur = con.cursor()
        #cur.execute("SELECT HEX(AES_ENCRYPT(%s, %s))", (id, EncryptKey))
        #accesskey = cur.fetchone()
        #cur.close()
        #return accesskey[0]

    def readOne(self, con, where = "", wherevalues = (), id = None, email = None, password = None):
        x = [where] if where else []
            
        if id != None:
            x += ["`Id`  = %s"]
            wherevalues += (id,)
        if password != None:
            x += ["`Password` = HEX(AES_ENCRYPT(%s, %s))"]
            wherevalues += (password, EncryptKey)
        if email != None:
            x += ["`Email` = %s"]
            wherevalues += (email,)

        where = " AND ".join(x)

        #print >>sys.stderr, str((where, wherevalues))
        #sys.stderr.flush()
        #return 0
        return Table.readOne(self, con, where, wherevalues)
    


    def getemailpassword(self, email, password):
        return email.strip() + '@' + password.strip()


