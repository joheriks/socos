from pc.parsing.AST import ParentNode
from pc.parsing.AST import tree_to_str

from pc.pp.PVS_Expression_Printer import PVS_Expression_Printer


class LineBreak:
    '''Class that represents line breaks.'''
    def __init__(self, lb_type):
        self.lb_type = lb_type


# Voluntary line break
LBV      = LineBreak('voluntary')

# Forced line break
LBF      = LineBreak('forced')

# Line breaks before BEGIN keywords
LB_begin = LineBreak('begin')

# Line breaks after prog statements
LB_prog  = LineBreak('prog')

# Line breaks between separate extern define:s
LB_extdef  = LineBreak('extdef')



class PP( object ):

    def __init__( self, max_line_length=320000, uppercase_keywords=False,
                  indent_amount=4, break_at_begin=True ):
        """
        Constructor. Parameters:
            max_line_length:    Maximum desired line length; line length cannot be
                                guaranteed to stay under this limit.
            uppercase_keywords: Should keywords be upper case or not.
            indent_amount:      Number of spaces per indentation level.
        """
        if uppercase_keywords:
            self._format = lambda s : s
        else:
            self._format = lambda s : s.lower()

        self.max_line_length = max_line_length
        self.indent_amount = indent_amount

        self.break_at_begin = break_at_begin
        self.break_at_prog = True
        self.break_at_extdef = True
        self.break_at_forced = True

        self.indent = 0
        self.line_length = 0
        self.buffer = None
        self._outf = None
        self.last_break = None
        self.block = None
        
        self.pvs_pp = PVS_Expression_Printer(max_line_length=max_line_length)
        self.pvs_pp.option_keywords_upcase = uppercase_keywords

        # for printing PVS expression inside strings inside s-expressions, where
        # newlines are not allowed
        self.sexp_pvs_string_pp = PVS_Expression_Printer(max_line_length=32000000)


    def output(self, root, outf):
        """
        Performs pretty printing on the tree starting at root. The function outf
        will be called once for each line of output.
        """
        self.indent = 0
        self.line_length = 0
        self.buffer = [ '' ]
        self._outf = outf
        self.last_break = None
        self.block = None

        self.prefix = None

        # Process tree
        self._process(root)
        self._next_line()

        # ...done!
        self._outf = None


    def begin_prefixed_region( self, prefix ):
        self._next_line()
        self.prefix = prefix
        

    def end_prefixed_region( self ):
        self._next_line()
        self.prefix = None
        

    def output_to_string(self, root):
        """
        Performs pretty printing on the tree starting at root. Returns a string
        containing the pretty-printed code.
        """
        lines = []
        self.output(root, lines.append)
        return '\n'.join(lines)


    def _add_to_buffer(self, *output_data):
        for item in output_data:
            self.buffer.append(item)

            if isinstance(item, LineBreak):
                self.last_break = len(self.buffer) - 1
                self.line_length += 1
                if item.lb_type != 'voluntary':
                    # Mode-dependent line break: type is a mode name
                    if getattr(self, 'break_at_%s' % item.lb_type):
                        del(self.buffer[-1])
                        self._finish_line(self.last_break)
                    else:
                        # Mode says no break needed - convert to voluntary
                        self.buffer[-1] = LBV
            else:
                # Must be string; update line length
                self.line_length += len(item)
                # Line too long?
                if self.line_length > self.max_line_length:
                    if not self.last_break is None:
                        del(self.buffer[self.last_break])
                        self._finish_line(self.last_break)


    def indent_more( self, amount=None ):
        '''Increases the indentation amount for the current and future lines.'''
        if amount==None: amount = self.indent_amount
        self.indent      += amount
        self.line_length += amount
        if self.buffer:
            # First element in buffer is indentation, adjust it.
            self.buffer[0] = ' '*self.indent


    def indent_less( self, amount=None ):
        '''Decreases the indentation amount for the current and future lines.'''
        if amount==None: amount = self.indent_amount
        self.indent      -= amount 
        self.line_length -= amount 
        if self.buffer:
            # First element in buffer is indentation, adjust it.
            self.buffer[0] = self.buffer[0][:-self.indent_amount]


    def node_list_of_items(self, node, start=0, end=None, separator=(',', LBV)):
        '''
        Outputs children of node from start index to end index, separated by
        the given separator.
        '''
        if end==None:
            end = len(node.children)
        nodes = node.children[start:end]
        for n in nodes[:-1]:
            self._process(n)
            self._add_to_buffer(*separator)
        if nodes:
            self._process(nodes[-1])


    def _process(self, node):
        '''Processes the given node. Adds pretty-print string to buffer and/or
        outputs lines.'''
        f_name = 'node_%s' % node.type

        if hasattr(self, f_name):
            getattr(self, f_name)(node)
        else:
            lines = []

            # Hack! Modify pvs_pp line length after first line. If we don't,
            # pvs_pp thinks it needs to make every line of the expression the
            # same width, but lines after the first one only need to fit in
            # what's left after we indent.
            self.pvs_pp.option_line_length = self.max_line_length - self.line_length

            def append_and_set_len(line):
                lines.append(line)
                self.pvs_pp.option_line_length = (
                    self.max_line_length - (self.indent + self.indent_amount)
                    )

            self.pvs_pp.output(node, append_and_set_len)
            first = True

            if len(lines) > 1:
                for line in lines[:-1]:
                    if line:
                        self._add_to_buffer(line)
                        self._next_line()
                        if first and not self.block in ( 'definition', ):
                            self.indent_more()
                            first = False

            if not first:
                self.indent -= self.indent_amount
            if lines and lines[-1]:
                self._add_to_buffer(lines[-1])


    def _finish_line(self, index):
        '''
        Outputs current line up to, but not including, index. Any line breaks
        are replaced with spaces and items up to index removed from the buffer.
        '''

        # Replace line breaks with spaces
        for i, item in enumerate(self.buffer[ : index]):
            if isinstance(item,LineBreak):
                self.buffer[i] = ' '
            else:
                self.buffer[i] = item

        # Output lines
        outs = ''.join(self.buffer[:index])
        outs = self.prefix+outs if self.prefix else outs
        self._outf(outs)

        # Re-add any remaining items
        remaining = self.buffer[index : ]
        self.last_break = None
        self.line_length = self.indent
        self.buffer = [ ' '*self.indent ]
        self._add_to_buffer(*remaining)



    def _next_line( self, if_empty=True ):
        '''
        Concatenates strings in buffer and sends them to output as one line.
        Buffer is emptied and a new line started (with proper indentation).
        Parameter if_empty decides if this is done if the buffer only contains
        an indentation.
        '''
        if if_empty or len(self.buffer) > 1:
            self._finish_line(len(self.buffer))


