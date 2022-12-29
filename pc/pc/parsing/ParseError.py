
'''
Module defining classes related to parser errors/exceptions.
'''


class ParseError(object):
    """ Class representing a parser error """

    def __init__(self, msg, lineno=-1, pos=-1, type='generic'):
        """
        Constructs a new error. Msg is the cause of the error, lineno
        and pos specify where the error appears in input. Type is an
        optional error type specifier. Could be used to distinguish
        between errors that occur in different parsing phases (lexing,
        parsing, semantic analysis).
        """
        object.__init__(self)
        self.msg = msg
        self.lineno = lineno
        self.pos = pos
        self.type = type


    def make_from_node(msg, node, type='generic'):
        return ParseError(msg, node.start_line(), node.start_pos(), type)

    make_from_node = staticmethod(make_from_node)


    def make_from_p(msg, p, index, type='generic'):
        return ParseError(
            msg,
            p.lineno(index),
            p.lexpos(index)
            )

    make_from_p = staticmethod(make_from_p)


    def __str__(self):
        if self.lineno is not None and self.pos is not None:
            return ("Msg: %s, type %s, line %d, pos %d"
                    % (self.msg, self.type, self.lineno, self.pos))
        else:
            return "Msg: %s, type %s" % (self.msg, self.type)


    def __repr__(self):
        return str(self)



class ParseException(Exception):
    """
    Class representing a parser error exception. Allows simple
    iteration through error elements.
    """

    def __init__(self, errors):
        """
        Constructs a new exception. Errors is a list of
        ParseErrors. This allows including more than one message
        in the exception (since we are rarely interested in the
        traceback of the exception, only in the error information).
        """
        Exception.__init__(self)
        assert(isinstance(errors, list))
        self.errors = errors
        self.index = 0


    def next(self):
        """ Returns next ParseError in exception """
        if self.index >= len(self.errors):
            raise StopIteration

        ret = self.errors[self.index]
        self.index += 1
        return ret


    def __iter__(self):
        return self


    def __str__(self):
        return '\n'.join(map(str, self.errors))


    def __repr__(self):
        return str(self)
