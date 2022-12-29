#!/usr/bin/python
# -*- coding: utf-8 -*-

import smtplib
from datetime import datetime
from datetime import timedelta
from datetime import date
import os
import zipfile

logintime = timedelta(minutes = 15)

if __name__ != "__main__":
    from mod_python import apache
    from mod_python import util

sender = 'noreply@fourferrries.com'

from Crypto.Cipher import AES

import os
from init_socos import *

from pc.database.table import *
from pc.database.session import *

def getConnect():
    return mdb.connect('localhost', 'fourferries', 'fourferries', 'fourferries', charset="utf8")

action = "/users.py"

aes = AES.new("check@derivation", AES.MODE_ECB)

def addPadding(s, n = 16):
    k = n - len(s) % n
    x = s
    return s + "a" * k

class Action:
    def __init__(self, handler = None, session = 1, values = {}, action = None):
        if handler != None:
            self.encrypt(handler, session, values)
            self.handler = handler
            self.session = session,
            self.values = values
        else:
            self.decrypt(action)
            self.action = action

    def hex2(self, c):
        n = ord(c)
        s = "%x" % n
        if n < 16:
            return "0" + s
        return s

    def encrypt(self, handler, session, values):
        values["session"] = [session]
        values["handler"] = [handler]
        s = "&".join("%s=%s" % (x, str(y)) for x in values for y in values[x])
        
        s = addPadding(s + "&pad=a")
        x = aes.encrypt(s)
        action = "".join(self.hex2(c) for c in x)
        self.action = action

    def decrypt(self, action):

        if not action:
            self.handler, self.session, self.values = (FrontPage.index, 1, {})
            return
        n = len(action) - 1
        i = 0
        s = ""
        try:
            while i < n:
                s += chr(int(action[i : i + 2], 16))
                i += 2
        except:
            self.handler, self.session, self.values = (FrontPage.index, 1, {})
            return
    
        if len(s) % 16:
            self.handler, self.session, self.values = (FrontPage.index, 1, {})
            return
        x = aes.decrypt(s)
        query = Query(x)
        self.values = query.data
        handler = query.getSingleValueSafe("handler")
        session = query.getSingleValueSafe("session")
        if not handler:
            self.handler, self.session = (FrontPage.index, 1)
            return

        self.handler = int(handler)
        self.session = int(session)
        
    def getHS(self):
        return self.handler, self.session

    def getSingleValueSafe(self, key):
        if key in self.values:
            return self.values[key][0]
        return None

def getstyle(stylelist):
    style = ""
    for x in stylelist:
        if x:
            if style:
                style += " " + x
            else:
                style = x
    return style

class Container:
    def __init__(self, style, html, title):
        self.style = style
        self.title = title
        self.content = html
        
    def html(self):
        style = getstyle(["Container",  self.style])
        if self.title:
            title = Div("ContainerTitle", self.title).html()
        else:
            title = ""
        s = title + self.content
        if s:
            return Div(style, title + self.content).html()
        return ""

class HtmlA:
    def __init__(self, style, label, action, param):
        if style:
            self.style = "class='%s' " % style
        else:
            self.style = ""
            
        self.action = action
        self.param = param
        self.label = label
    def html(self):
        return "<a %shref='%s?%s'>%s</a>\n" % (self.style, self.action, self.param, self.label)
        
class Menu:
    def __init__(self, title, style = "Padding"):
        self.title = title
        self.style = style
        self.itemstyle = "Menu"
        self.items = []

    def additem(self, session, name, action):
        if session.group in action.groupaccess:
            #self.items += [[name, "action=%s" % encryptAction(action.index, session.id)]]
            self.items += [[name, "action=%s" % Action(action.index, session.id).action]]

    def html(self):
        s = "<div class='%s'>\n" % self.style
        for x in self.items:
            s += "    " + HtmlA(self.itemstyle, x[0], action, x[1]).html()
            #s += "    <a class='%s' href='%s?%s'>%s</a>\n" % (self.itemstyle, action, x[1], x[0])
        s += "</div>\n"
        return s

class Div:
    def __init__(self, style, html = ""):
        self.style = style
        self.htm = html

    def html(self):
        if self.style:
            return "<div class='%s'>\n%s</div>" % (self.style, self.htm)
        else:
            return "<div>\n%s</div>" % self.htm
        
class Form:
    def __init__(self, action, session, style = "Padding"):
        self.action = Action(action.index, session).action
        self.fields = []
        self.style = style
        self.menu = None

    def addfields(self, fields):
        self.fields += fields

    def html(self):
        s = "<form action='%s' method='post'>\n" % action
        if self.menu:
            s += self.menu.html()
        s += "<input type='hidden' name='action' value='%s'/>\n" % self.action
        s += "<table>\n"
        for x in self.fields:
            s += "    <tr>\n"
            s += "        <td>%s</td>\n" % x.labelhtml()
            s += "        <td>%s</td>\n" % x.html()
            s += "    </tr>\n"
        
        s += "</table>\n"
        s += "</form>\n"
        return s

class LoginMenu(Menu):
    def __init__(self, session):
        Menu.__init__(self, "Menu")
        self.additem(session, "Login", Login)
        self.additem(session, "Register", Register)
        self.additem(session, "Reset password", ResetPassword)

        self.additem(session, "Download editor", DownloadEditor)
        self.additem(session, "Search users", SearchUsersForm)
        self.additem(session, "Logout", Logout)

current_dir = os.path.dirname(os.path.realpath(__file__))
filemainhtml = os.path.join(current_dir, "database/main.xml")
f = open(filemainhtml)
mainhtml = f.read()
f.close()

class Query:
    def __init__(self, querystring):
        self.data = util.parse_qs(querystring, True)
        
    def getSingleValueSafe(self, key):
        if key in self.data:
            return self.data[key][0]
        return None

    def getSingleIntValueSafe(self, key, default):
        s = self.getSingleValueSafe(key)
        if s:
            return int(s)
        else:
            return default

class Session:
    def __init__(self, req):
        self.req = req
        self.query = Query(req.subprocess_env['QUERY_STRING'])
        self.postquery = Query(req.read())
        action = self.readquery('action')
        self.actionobj = Action(action = action)
        self.handler, self.id  = self.actionobj.getHS()
        self.group = Group.guest
        self.user = None
        self.con = getConnect()
        self.message = None
        self.verify()

    def readaction(self, name):
        return self.actionobj.getSingleValueSafe(name)

    def readquery(self, name):
        value = self.query.getSingleValueSafe(name)
        if value == None:
            value = self.postquery.getSingleValueSafe(name)
        return value

    def verify(self):
        self.table = TableSession()
        if not self.table.readOne(self.con, "`Id` = %d" % self.id, ()):
            self.message = "The session has expired. Please login again."
            self.id = 1
            self.handler = FrontPage.index
            return False
        if self.table.ExpireTime.value != None:
            if self.table.ExpireTime.value < datetime.now():
                self.id = 1
                self.message = "The session has expired. Please login again."
                self.handler = FrontPage.index
                return False
        #if check ip adress
        self.req.add_common_vars()
        ip = self.req.subprocess_env['REMOTE_ADDR']
        if self.table.RemoteAddress.value:
            if self.table.RemoteAddress.value != ip:
                self.id = 1
                self.message = "The session has expired. Please login again."
                self.handler = FrontPage.index
                return False
        self.group = self.table.Group.value
        self.user = self.table.UserId.value
        if not self.group in Handlers.data[self.handler].groupaccess:
            self.id = 1
            self.message = "The session has expired. Please login again."
            self.handler = FrontPage.index
            return False

        # update the expiration date, only if it is defined
        if self.table.ExpireTime.value != None:
            table = TableSession()
            table.ExpireTime.value = datetime.now() + logintime
            table.updateId(self.con, self.id)

        #self.message = str(self.req.subprocess_env)

        return True

    def create(self):
        # create new session after login. query must contain the Email and password
        email = self.readquery("Email")
        password = self.readquery("Password")
        #self.message = "Login: %s, %s" % (email, password)
        user = TableUser()
        if not user.readOne(self.con, email = email, password = password):
            self.message = "The email address and password do not match, please try again."
            return False

        if user.Status.value == Status.pending_email:
            self.message = "You must verify the email address before loging in."
            return False

        if user.Status.value == Status.pending:
            self.message = "Your account was not approved yet. You will receive an emaill message when the account is approved."
            return False

        self.user = user.Id.value
        self.group = user.Group.value

        self.table = TableSession()
        
        self.req.add_common_vars()

        self.table.UserId.value =  self.user
        self.table.Group.value = self.group
        self.table.RemoteAddress.value = self.req.subprocess_env['REMOTE_ADDR']
        self.table.ExpireTime.value = datetime.now() + logintime
        self.id = self.table.insert(self.con)
        self.message = "Login successful."
        return True

def handler(req):
    session = Session(req)
    return Handlers.process(session)

class Main:
    index = -1
    groupaccess = [Group.guest]
    def __init__(self, session):
        self.session = session
        self.title = ""
        self.menu = None
        self.main = ""

    def prepare(self):
        pass

    def header(self):
        self.session.req.content_type = 'text/html'
        self.session.req.encodiong = "UTF-8"
        self.session.req.headers_out['Access-Control-Allow-Origin'] = '*'
        self.session.req.headers_out['Access-Control-Allow-Methods'] = 'GET, POST, OPTIONS'

    def content(self):
        main = self.main if self.main else ""
        if self.session.message:
            main = self.session.message + "<p/>" + main
        x = ""
        if main:
            x =  Div("Padding", main).html()
        
        main = Container("MainFormPos", x, self.title).html()
        if self.menu:
            menu = Container("MenuPos", self.menu.html(), self.menu.title).html()
        else:
            menu = ""
        text = mainhtml.replace("<content/>", menu + main)
        self.session.req.write(text.encode("utf-8"))
        

    def handler(self):
        self.header()
        self.prepare()
        self.content()
        return apache.OK

class FrontPage(Main):
    def __init__(self, session):
        Main.__init__(self,session)
        
    def prepare(self):
        self.menu = LoginMenu(self.session)

class Login(Main):   
    def __init__(self, session):
        Main.__init__(self, session)
        self.title = "Login"
        self.fields = FieldList(
            FormField("text", "Email address", "Email"),
            FormField("password", "Password", "Password"),
            FormField("submit", "", "Submit", "Submit")
            )
    def prepare(self):
        form = Form(LoginSubmit, self.session.id)        
        form.addfields(self.fields.data)
        self.main = form.html()
        self.menu = LoginMenu(self.session)

class LoginSubmit(Login):
    def prepare(self):
        if(not self.session.create()):
            form = Form(LoginSubmit, self.session.id)
            self.fields.Email.read(self.session)
            form.addfields(self.fields.data)
            self.main = form.html()
        self.menu = LoginMenu(self.session)
        #self.main = "Login submit"

class FormField:
    def __init__(self, type, label, name, value = "", disabled = False):
        self.label = label
        self.name = name
        self.value = value
        self.type = type
        self.ok = True
        self.error = []
        self.style = None
        if type == "text" or type == 'password':
            self.style = "inputtext"
        self.edit = True
        self.disabled = disabled
        
    def display(self):
        return self.value
            
    def labelhtml(self):
        if self.ok:
            return self.label
        return '<span class="FieldError">%s</span>' % self.label

    def html(self):
        if not self.edit:
            return self.display()
        else:
            return self.edithtml()

    def edithtml(self):
        style = ""
        disabled = ""
        if self.style:
            style = "class='%s' " % self.style
        if self.disabled:
            disabled = "disabled='true' "

        return "<input type='%s' %s%sname='%s' value='%s'/>" % (self.type, style, disabled, self.name, self.display())

    def validate(self):
        self.ok = True
        return self.ok

    def read(self, session):
        self.value = session.readquery(self.name)

    def initfromtable(self, tableitem):
        self.value = tableitem.value

    def savetotable(self, tableitem):
        if not self.disabled:
            tableitem.value = self.value

class NonEmptyField(FormField):
    def validate(self):
        self.ok = self.value != ""
        if not self.ok:
            self.error += ["The fields '%s' must not be empty." % self.name]
        return self.ok

class SelectField(FormField):
    def __init__(self, label, name, display_values, value = None, disabled = False):
        FormField.__init__(self, "", label, name, value, disabled)
        if type(display_values) == list:
            self.display_values = {}
            i = 0
            for x in display_values:
                self.display_values[i] = x
                i = i + 1
        else:
            self.display_values = display_values
        
    def display(self):
        if self.value in self.display_values:
            return self.display_values[self.value]
        else:
            return ""

    def edithtml(self):
        style = ""
        disabled = ""
        if self.style:
            style = "class='%s' " % self.style
        if self.disabled:
            disabled = "disabled='true' "

        html = "<select %s%sname=%s>\n" % (style, disabled, self.name)

        def selected(x):
            if x == self.value:
                return "selected='selected' "
            return ""
        

        html += "".join("<option %svalue='%s'>%s</option>\n" % (selected(x), str(x),  self.display_values[x]) for x in self.display_values)

        html += "</select>"

        return html


class DateField(FormField):
    def __init__(self, label, name, value = None, disabled = False):
        FormField.__init__(self, "", label, name, value, disabled)

    def validate(self):
        try:
            x = datetime.strptime(self.value, "%Y-%m-%d")
            self.value = date(x.year, x.month, x.day)
        except ValueError:
            self.ok = False
            self.error += ["The date must be in the format 'yyyy-mm-dd'."]
        return self.ok
      

class FieldList:
    def __init__(self, *fields):
        self.data = []
        self.dic = {}
        self.ok = True
        for x in fields:
            self.addField(x)

    def addField(self, x):
        self.data += [x]
        self.dic[x.name] = x
        setattr(self, x.name, x)
        

    def validate(self):
        self.ok = True
        for x in self.data:
            self.ok = self.ok & x.validate()
        return self.ok

    def read(self, session):
        for x in self.data:
            x.read(session)

    def initfromtable(self, table):
        for x in table.fields:
            if x.name in self.dic:
                self.dic[x.name].initfromtable(x)

    def savetotable(self, table):
        for x in table.fields:
            if x.name in self.dic:
                self.dic[x.name].savetotable(x)
            
    def getError(self):
        error = []
        for x in self.data:
            error += x.error
        return error

    def setedit(self, edit = True):
        for x in self.data:
            x.edit = edit
    
class Register(Main):
    def __init__(self, session):
        Main.__init__(self, session)
        self.title = "Register"
        self.fields = FieldList(
            NonEmptyField("text", "First name", "FirstName"),
            NonEmptyField("text", "Last name", "LastName"),
            NonEmptyField("text", "Email address", "Email"),
            NonEmptyField("password", "Password", "Password"),
            NonEmptyField("password", "Verify password", "VerifyPassword"),
            FormField("submit", "", "Submit", "Submit")
            )
        
    def prepare(self):
        form = Form(RegisterSubmit, self.session.id)
        form.addfields(self.fields.data)
        self.main = form.html()
        self.menu = LoginMenu(self.session)

def getHtmlError(error):
    s = ""
    for x in error:
        s += '<span class="FieldError">%s</span><br/>\n' % x
    return s

class RegisterSubmit(Register):     
    def prepare(self): 
        form = Form(RegisterSubmit, self.session.id)
        self.fields.read(self.session)

        if(not self.validate()):
            self.fields.Password.value = ""
            self.fields.VerifyPassword.value = ""
            form.addfields(self.fields.data)
            err = getHtmlError(self.fields.getError()) + "<p/>"
            self.main = err + form.html()
            self.menu = LoginMenu(self.session)
        else:
            email = self.fields.Email.value
            tableuser = TableUser()
            tableuser.Email.value = email
            tableuser.LastName.value = self.fields.LastName.value
            tableuser.FirstName.value = self.fields.FirstName.value
            tableuser.Password.value = self.fields.Password.value
            id = tableuser.insert(self.session.con)
            self.menu = LoginMenu(self.session)
            if id == None:
                self.fields.Password.value = ""
                self.fields.VerifyPassword.value = ""
                form.addfields(self.fields.data)
                err = getHtmlError(tableuser.getError())
                self.main = err + form.html()
            else:
                #accesskey = tableuser.getAccessKey(con, id)
                #self.main = "User created: %d" % i
                self.session.req.add_common_vars()
                port = self.session.req.subprocess_env["SERVER_PORT"]
                host = self.session.req.subprocess_env["SERVER_NAME"]

                try:
                    message = "From: noreply@fourferrries.com\n"\
                        "To: %s\n"\
                        "Subject: verify your email address\n"\
                        "\n"\
                        "Click on the following link to verify your email address:\n\n"\
                        "http://%s:%s/users.py?action=%s\n"
                    
                    message = message % (email, host, port, Action(VerifyEmail.index, 1, {"userid": [id]}).action)
                    smtpObj = smtplib.SMTP('smtp.nebula.fi')
                    smtpObj.sendmail(sender, [self.fields.Email.value], message)
                    self.main += "<p/>Please validate your email address by clicking on the link which was sent by email to %s." % email
                except Exception:
                    self.main += "<p/>Cannot send email."       
            
    def validate(self):
        self.fields.validate()
        if self.fields.Password.value != self.fields.VerifyPassword.value:
            self.fields.Password.ok = False
            self.fields.VerifyPassword.ok = False
            self.fields.Password.error += ["The 'Password' and 'Verify password' fields do not match"]
            self.fields.ok = False
        return self.fields.ok
        
class VerifyEmail(Main):
    def prepare(self):
        tableuser = TableUser()
        userid = self.session.readaction("userid")
        #self.session.message = "user id: %s" % str(userid)

        self.menu = LoginMenu(self.session)
        if not tableuser.readOne(self.session.con, id = userid):
            self.main = "The user does not exists anymore. Please register again."
            return

        if tableuser.Status.value == Status.pending_email:
            user = TableUser()
            user.Status.value = Status.pending
            s = user.updateId(self.session.con, tableuser.Id.value)
            self.main = "Email address verified successfully."
        else:
            self.main = "The Email was already verified."

class ResetPassword(Main):
    groupaccess = []
    def __init__(self, session):
        Main.__init__(self, session)
        self.title = "Reset password"
        self.fields = FieldList(
            FormField("text", "Email address", "EmailAddress"),
            FormField("submit", "", "Submit", "Submit")
            )
        
    def prepare(self):
        form = Form(ResetPasswordSubmit, self.session.id)
        self.menu = LoginMenu(self.session)
        form.addfields(self.fields.data)
        self.main = form.html()

class ResetPasswordSubmit(ResetPassword):
    def prepare(self):
        self.menu = LoginMenu(self.session)

import shutil

import os
import os.path
from zipfile import ZipFile

from init_server import *

class DownloadEditor(Main):
    groupaccess = [Group.user, Group.admin]
    def header(self):
        self.session.req.content_type = 'application/octet-stream'
        self.session.req.headers_out['Content-Disposition'] = 'attachment; filename="ebook.zip"'
        self.session.req.headers_out['Access-Control-Allow-Origin'] = '*'
        self.session.req.headers_out['Access-Control-Allow-Methods'] = 'GET, POST, OPTIONS'

    def prepare(self):
        base_dir = os.path.join(socos_dir, "editor")
        editor_dir = os.path.join(base_dir,"ebook")
        download_dir = os.path.join(data_dir, "download", "user%d" % self.session.user)
        if not os.path.exists(download_dir):
            os.makedirs(download_dir)
            
        self.session.req.add_common_vars()
        port = self.session.req.subprocess_env["SERVER_PORT"]
        host = self.session.req.subprocess_env["SERVER_NAME"]
        pluginname = "fourferries"

        filename = os.path.join(editor_dir, "ebook.html")
        f = open(filename)
        text = f.read().replace("FOURFERRIES_PLUGIN_NAME", pluginname)
        f.close()
        action = Action(0, 1, {"userid":[self.session.user]}).action
        text = text.replace("FOURFERRIES_PLUGIN_SERVER", "http://%s:%s/plugin.js?pluginname=%s&action=%s" % (host, port, pluginname, action))

        ebook_html_file = "ebook/ebook.html"

        z = ZipFile(os.path.join(download_dir, "ebook.zip"),"w")
        for dirpath,dirnames,filenames in os.walk(editor_dir):
            if ".svn" in dirnames:
                dirnames.remove(".svn")
            for f in filenames:
                abspath = os.path.join(dirpath,f)
                relpath = os.path.relpath(abspath,base_dir)
                if not relpath==ebook_html_file:
                    z.write(abspath,relpath)
        z.writestr(ebook_html_file,text)
        z.close() 

        self.filename = os.path.join(download_dir, "ebook.zip")
        self.rmdir = download_dir

    def content(self):
        f = open(self.filename, "rb")
        data = f.read()
        f.close()
        #shutil.rmtree(self.rmdir, True)

        self.session.req.write(data)


class AdminHandler(Main):
    groupaccess = [Group.admin]
    def prepare(self):
        self.menu = LoginMenu(self.session)

class SearchUsersForm(AdminHandler):
    def __init__(self, session):
        Main.__init__(self, session)
        self.title = "Search Users"
        self.fields = FieldList(
            NonEmptyField("text", "First name", "FirstName"),
            NonEmptyField("text", "Last name", "LastName"),
            NonEmptyField("text", "Email address", "Email"),
            FormField("submit", "", "Submit", "Search")
            )
    def prepare(self):
        form = Form(SearchUsersList, self.session.id)
        form.addfields(self.fields.data)
        self.main = form.html()
        self.menu = LoginMenu(self.session)

class HtmlTable:
    def __init__(self):
        self.rows = []
    def addRow(self, row):
        self.rows += [row]

    def html(self):
        html = "<table>\n"
        for row in self.rows:
            html += "<tr>"
            for elem in row:
                html += "<td>%s</td>" % elem
            html += "</tr>\n"
        html += "</table>"
        return html

class SearchUsersList(SearchUsersForm):
    def prepare(self):
        self.title = "User List"
        self.fields.read(self.session)

        search = [self.fields.FirstName, self.fields.LastName, self.fields.Email]
        
        where = " AND ".join("`%s` LIKE %s" % (x.name, "%s") for x in search if x.value)
        wherevalues = tuple(["%s%s%s" % ("%", x.value, "%") for x in search if x.value])

        self.main = where % wherevalues
        
        user = TableUser()
        user.readAll(self.session.con, where, wherevalues)

        table = HtmlTable()

        tablefields =  [user.LastName, user.Email, user.Group, user.Status, user.EndDate, user.CheckNumber]
        
        table.addRow(["", user.FirstName.label] + [x.label for x in tablefields])
        
        i = 1
        while user.updateNext():
            param = "action=%s" % Action(ViewUser.index, self.session.id,  {"userid": [user.Id.value]}).action
            table.addRow([str(i), HtmlA("", user.FirstName.value, action, param).html()] + [x.display() for x in tablefields])
            i = i + 1
            
        self.main = table.html()
        
        self.menu = LoginMenu(self.session)

class EditUserForm(AdminHandler):
    def __init__(self, session):
        Main.__init__(self, session)
        self.title = "Edit User"
        self.fields = FieldList(
            NonEmptyField("text", "Email address", "Email"),
            NonEmptyField("text", "First name", "FirstName"),
            NonEmptyField("text", "Last name", "LastName"),
            SelectField("Group", "Group", Group.public),
            SelectField("Status", "Status", Status.public),
            DateField("End date", "EndDate")
            )

            #class SelectField(FormField):
            #def __init__(self, label, name, display_values, value = None, disabled = False):

    def prepare(self):
        self.menu = LoginMenu(self.session)
        form = self.createform()
        if form:
            form.menu = self.formmenu()
            self.main = form.html()

    def formmenu(self):
        formmenu = FormMenu()
        formmenu.additem(self.session, "submit", "Save", EditUserSave, {"userid": [self.userid]})
        formmenu.additem(self.session, "button", "Cancel", ViewUser, {"userid": [self.userid]})
        return formmenu

    def readuserdata(self):
        self.tableuser = TableUser()
        self.userid = self.session.readaction("userid")
        #self.session.message = "user id: %s" % str(self.userid)
        self.menu = LoginMenu(self.session)
        if not self.tableuser.readOne(self.session.con, id = self.userid):
            self.main = "The user does not exists anymore. Please register again."
            return False
        return True

    def createform(self, edit = True):
        if not self.readuserdata():
            return False
        self.fields.initfromtable(self.tableuser)
        self.fields.setedit(edit)
        form = Form(SearchUsersList, self.session.id)
        form.addfields(self.fields.data)
        return form

class FormMenu:
    def __init__(self):
        self.items = []

    def additem(self, session, type, name, action, param):
        if session.group in action.groupaccess:
            self.items += [[type, name, Action(action.index, session.id, param).action]]

    def html(self):
        s = "<div>\n"
        for x in self.items:
            s += "<input type='%s' name='Submit' value='%s' onclick='this.form.action.value=\"%s\"; this.form.submit();'/> " % (x[0], x[1], x[2])
        s += "</div>\n"
        return s

class ViewUser(EditUserForm):
    def prepare(self):
        self.menu = LoginMenu(self.session)
        self.title = "View User"
        form = self.createform(False)
        if form:
            form.menu = self.formmenu()
            self.main = form.html()

    def formmenu(self):
        formmenu = FormMenu()
        formmenu.additem(self.session, "submit", "Edit", EditUserForm, {"userid": [self.userid]})
        formmenu.additem(self.session, "button", "Delete", DeleteUser, {"userid": [self.userid]})
        return formmenu

class EditUserSave(ViewUser):
    def prepare(self):
        self.menu = LoginMenu(self.session)

        if not self.readuserdata():
            return
        
        self.fields.read(self.session)

        if not self.fields.validate():
            form = Form(SearchUsersList, self.session.id)
            form.addfields(self.fields.data)
            form.menu = EditUserForm.formmenu(self)
            err = getHtmlError(self.fields.getError()) + "<p/>"
            self.main = err + form.html()
            return 

        user = TableUser()
        self.fields.savetotable(user)
        s = user.updateId(self.session.con, self.userid)
        self.main = "The user was saved successfully.<p/>"

        self.title = "View User"
        form = self.createform(False)
        if form:
            form.menu = self.formmenu()
            self.main += form.html()

class DeleteUser(ViewUser):
    def formmenu(self):
        self.title = "Confirm Delete User"
        formmenu = FormMenu()
        formmenu.additem(self.session, "button", "Delete", DeleteUserDo, {"userid": [self.userid]})
        formmenu.additem(self.session, "submit", "Cancel", ViewUser, {"userid": [self.userid]})
        return formmenu

    def prepare(self):
        ViewUser.prepare(self)
        self.main = "Do you want to delete the user? All data associated with the user will be permanently deleted.<p/>\n" + self.main
        

class DeleteUserDo(ViewUser):
    def prepare(self):
        self.userid = self.session.readaction("userid")
        cur = self.session.con.cursor()
        cur.execute("DELETE FROM `User` WHERE `Id` = %s", self.userid)
        self.session.con.commit()
        cur.close()
        self.menu = LoginMenu(self.session)
        self.main = "The user was deleted.<p/>\n" + self.main

class Logout(Login):
    groupaccess = [Group.user, Group.admin]
    def prepare(self):
        cur = self.session.con.cursor()
        cur.execute("DELETE FROM `Session` WHERE `Id` = %s", self.session.id)
        self.session.con.commit()
        cur.close()
        self.session.id = 1
        self.session.group = Group.guest
        self.menu = LoginMenu(self.session)
        self.main = "Logout successfully"

class Handlers:
    data =  [FrontPage, Login, LoginSubmit,
             Register, RegisterSubmit, VerifyEmail,
             ResetPassword, ResetPasswordSubmit,
             DownloadEditor,
             SearchUsersForm, SearchUsersList,
             ViewUser, EditUserForm, EditUserSave,
             DeleteUser, DeleteUserDo,
             Logout
             ]
    length = 0
    for x in data:
        x.index = length
        length += 1
        
    def process(session):
        h = (Handlers.data[session.handler])(session)
        return h.handler()
    
    process = staticmethod(process)
    
