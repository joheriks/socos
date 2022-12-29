class Token:
    '''class defining tokens used in parsers/lexers in pc'''

    def __init__(self, type = None, value = None, lineno = None, lexpos = None):
        self.type = type
        self.value = value
        self.lineno = lineno
        self.lexpos = lexpos

    def make(p, index):
        return Token(p.slice[index].type, p[index], p.lineno(index), p.lexpos(index))

    make = staticmethod(make)

    def __str__(self):
        return "Token: T: %s, V: %r, L: %d, P: %d" % (self.type, self.value, self.lineno, self.lexpos)

    def __repr__(self):
        return str(self)





