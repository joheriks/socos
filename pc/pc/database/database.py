#!/usr/bin/python
# -*- coding: utf-8 -*-

import MySQLdb as mdb

import table
import session

def getConnect():
    return mdb.connect('localhost', 'fourferries', 'fourferries', 'fourferries', charset="utf8")

con = getConnect()
cur = con.cursor()
cur.execute("DROP DATABASE IF EXISTS fourferries")
cur.execute("CREATE DATABASE IF NOT EXISTS fourferries CHARACTER SET utf8")
cur.execute("USE fourferries")
cur.execute("DROP TABLE IF EXISTS Session")
cur.execute("DROP TABLE IF EXISTS User")

tableuser = table.TableUser()
tableuser.create(con)

tablesession = session.TableSession()
tablesession.create(con)

cur.execute("SELECT VERSION()")
data = cur.fetchone()

firstname = unicode("   Viorel 使用下列语言  ที่อยู่ในภาษ любимые   ", "utf-8")

tableuser = table.TableUser()
tableuser.Email.value = "viorel.preoteasa@abo.fi"
tableuser.LastName.value = "Preoteasa"
tableuser.FirstName.value = "Viorel"
tableuser.Group.value = table.Group.admin
tableuser.Status.value = table.Status.active
tableuser.Password.value = "password"
con = getConnect()
id = tableuser.insert(con)

tableuser.Email.value = "backrj@abo.fi"
tableuser.LastName.value = "Back"
tableuser.FirstName.value = "Ralph"
tableuser.Group.value = table.Group.admin
tableuser.Status.value = table.Status.active
tableuser.Password.value = "password"
con = getConnect()
id = tableuser.insert(con)

tableuser.Email.value = "joheriks@abo.fi"
tableuser.LastName.value = "Eriksson"
tableuser.FirstName.value = "Johannes"
tableuser.Group.value = table.Group.admin
tableuser.Status.value = table.Status.active
tableuser.Password.value = "password"
con = getConnect()
id = tableuser.insert(con)



tsession = session.TableSession()
tsession.Id.value = 1
tsession.Group.value = table.Group.guest

id = tsession.insert(con)
print tsession.error
    
print "Database version : %s " % data
con.close()
