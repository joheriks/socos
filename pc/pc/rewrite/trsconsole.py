#!/bin/env python2

'''
An interactive console app for testing TRS rules
'''

import os, re, readline
from glob import glob

from traceback import print_exc
from pc.util.Utils import remove_duplicates
from pc.parsing.IBP_Parser import IBP_Parser
from pc.parsing.AST import ParentNode, tree_to_str, filter_nodes
from pc.rewrite.trs import TRS, RuleParser


class RuleSet(object):
    '''
    A set of rules accessible by their name. One unique rule name is associated
    with one or zero rules.
    '''

    def __init__(self):
        self._rules = {}


    def add(self, rule):
        '''
        Adds <rule> to set. If a rule with the same name already exists, it
        will be overwritten. Names are not case sensitive.
        '''
        self._rules[rule.name.lower()] = rule


    def get(self, rule_name):
        '''
        Returns stored rule with name <rule_name>. Name is not case sensitive.
        Returns None if no such rule.
        '''
        try:
            return self._rules[rule_name.lower()]
        except KeyError:
            return None


    def find(self, f_match):
        '''
        Returns a list of all rules for which the function <f_match> is true.
        '''
        found = []
        for rule in self._rules.values():
            if f_match(rule):
                found.append(rule)
        return found


    def match(self, *patterns):
        '''
        Returns a list of all rules that match <patterns>. Patterns are strings
        where the wildcard * matches anything.
        '''
        found = []
        for pattern in patterns:
            pattern = pattern.replace('*', '.*') + '$'
            found += self.find(lambda r: re.match(pattern, r.name, re.I))
        return found


    def clear(self):
        '''
        Removes all stored rules.
        '''
        self._rules = {}


    def __len__(self):
        return len(self._rules)



class TRSConsole(object):
    '''
    Interactive TRS console.
    '''

    COMMANDS = [ 'load', 'clear_rules', 'rule', 'display', 'parse',
                 'show_rule', 'find', 'rule_repeat', 'help' ]
    COMMANDS.sort()
    MSG_PRE = '>> '

    HISTFILE = '~/.trsconsole_history'

    cmd_re = re.compile(r'([a-zA-Z_]+)\s')
    cmdline_re = re.compile(r'(?<!\\)\s+')


    def __init__(self, prompt='trsc> '):
        self.prompt = prompt
        self.trs = TRS()
        self.root = None
        self.rule_parser = RuleParser()
        self.rule_set = RuleSet()


    def main_loop(self):
        '''
        Main loop for console. Queries user for input and displays results
        until EOF.
        '''
        histfile = os.path.expanduser(TRSConsole.HISTFILE)
        try:
            readline.read_history_file(histfile)
        except IOError:
            pass

        readline.set_completer_delims(' \t')
        readline.set_completer(self.completer)
        readline.parse_and_bind('tab: complete')
        try:
            while True:
                cmdline = raw_input(self.prompt).strip()
                cmd_parts = self.cmdline_re.split(cmdline)

                if cmd_parts:
                    cmd = cmd_parts[0].lower()
                    if cmd:
                        try:
                            cmd_func = getattr(self, 'command_%s' % cmd)
                        except AttributeError:
                            self.err('unknown command "%s".' % cmd)
                            continue

                        # Run command and catch all errors so that any uncaught
                        # ones don't bring whole system down.
                        try:
                            cmd_func(*cmd_parts[1:])
                        except Exception, error:
                            self.err('unexpected exception: %s.' % error)
                            print_exc()
                else:
                    self.err('invalid command line.')

        except (EOFError, KeyboardInterrupt):
            print '\nBye!'

        try:
            readline.write_history_file(histfile)
        except IOError:
            pass


    def completer(self, text, state):
        '''
        Performs command-line completion. Should be added as a completer to the
        readline module.
        '''
        match = self.cmd_re.match(readline.get_line_buffer())
        if match:
            cmd = match.group(1).lower()
            try:
                cmd_completer = getattr(self, 'complete_%s' % cmd)
                return cmd_completer(text, state)
            except AttributeError:
                pass

        else:
            text = text.lower()
            cmds = [ cmd for cmd in self.COMMANDS if cmd.startswith(text) ]
            if state < len(cmds):
                if len(cmds) == 1:
                    return cmds[state] + ' '
                else:
                    return cmds[state]


    def _complete_filenames(self, text, state, suffix):
        '''
        Cmd line completion helper. Completes file names.
        '''
        path = os.path.expanduser(text) + '*'
        suffix = suffix.lower()
        files = filter(
            lambda f: os.path.isdir(f) or f.lower().endswith(suffix),
            glob(path))

        if state < len(files):
            item = files[state]
            if os.path.isdir(item):
                return item + os.path.sep
            else:
                return item


    def complete_load(self, text, state):
        '''
        Cmd line completion for the load command.
        Completes file names.
        '''
        return self._complete_filenames(text, state, 'trs')


    def complete_parse(self, text, state):
        '''
        Cmd line completion for the parse command.
        Completes file names.
        '''
        return self._complete_filenames(text, state, 'ibp')


    def complete_rule(self, text, state):
        '''
        Cmd line completion for the rule, rule_repeat and show_rule commands.
        Completes rule names.
        '''
        rules = self.rule_set.match(text + '*')

        if state < len(rules):
            return rules[state].name


    complete_rule_repeat = complete_rule
    complete_show_rule = complete_rule


    def complete_help(self, text, state):
        '''
        Cmd line completion for the help command. Completes command names.
        '''
        text = text.lower()
        cmds = [ cmd for cmd in self.COMMANDS if cmd.startswith(text) ]
        if state < len(cmds):
            if len(cmds) == 1:
                return cmds[state] + ' '
            else:
                return cmds[state]


    def command_load(self, *args):
        '''
        Executes the load command which loads rules. Syntax:
        load <filename> ...
        '''
        new_rules = []

        for arg in args:
            try:
                new_rules += self.rule_parser.parse(open(arg, 'rt').read())
            except IOError:
                self.err('failed to load rules from "%s".' % arg)

        for rule in new_rules:
            self.rule_set.add(rule)

        self.info('Parsed %i new rules, %i rules available.' \
              % (len(new_rules), len(self.rule_set)))


    def command_parse(self, *args):
        '''
        Executes the parse command which parses ibp modules. Syntax:
        parse <filename> ...
        '''
        if len(args) == 1:
            try:
                ibp = open(args[0], 'rt').read()
            except IOError:
                self.err('failed to load IBP tree from file "%s".' % args[0])
                self.root = None
                return

            self.root = IBP_Parser().parse(ibp)
            count = add_node_numbers(self.root)
            self.info('loaded tree with %i nodes.' % count)

        else:
            self.err('expected one IBP module.')


    def command_clear_rules(self, *args):
        '''
        Executes the clear_rules command which clears the list of loaded rules.
        Syntax: clear_rules
        '''
        if len(args) == 0:
            self.rule_set.clear()
            self.info('All rules removed.')
        else:
            self.err('expected no arguments.')


    def command_display(self, *args):
        '''
        Executes the display command which displays a tree. Syntax:
        display <max depth> <node number> ...
        Parameter <max depth> determines how many levels below the root node is
        shown.
        '''
        if self.root:
            lines = tree_to_str(self.root).split('\n')

            if len(args) <= 2:
                num = 0
                max_depth = 100000

                if len(args)==1:
                    try:
                        max_depth = max(0, int(args[0]))
                    except ValueError:
                        self.err('invalid max depth.')
                        return

                if len(args) == 2:
                    try:
                        num = int(args[1])
                        if num > len(lines):
                            raise ValueError
                    except ValueError:
                        self.err('invalid node number.')
                        return
            else:
                self.err('too many arguments, expected optional <max depth> and <node number>.')
                return

            nodes = filter_nodes(lambda n: n.number == num, self.root, True)
            assert len(nodes) <= 1

            if nodes:
                self._show_tree(max_depth, nodes[0])
            else:
                self.err('node not found.')
        else:
            self.err('no tree to display.')


    def command_rule(self, *args):
        '''
        Executes the rule command which applies one or more rules to the current
        tree. Syntax:
        rule <rule name or pattern> ...
        '''
        self._apply_rules(args)


    def command_rule_repeat(self, *args):
        '''
        Executes the rule_repeat command which applies one or more rules to the
        current tree. Each rule is applied as many times as possible before the
        next one is applied. Syntax:
        rule <rule name or pattern> ...
        '''
        self._apply_rules(args, repeat=True)


    def command_show_rule(self, *args):
        '''
        Executes the show_rule command which displays one or more rules. Syntax:
        show_rule <rule name or pattern>, ...
        '''
        if len(args) > 0:
            rules = self.rule_set.match(*args)
            if rules:
                for rule in rules:
                    print rule
            else:
                self.err('no rules found')
        else:
            self.err('expected name of rule(s) to show.')


    def command_find(self, *args):
        """
        Executes the find command which finds and displays all nodes with
        certain attributes. Syntax:
        find <attribute>=<regexp> ...
        """
        if len(args) > 0:
            if self.root:
                nodes = []
                for arg in args:
                    match = re.match(r"(\w+)=(.*)", arg)
                    if match:
                        attr = match.group(1)
                        value = match.group(2).replace('*', '.*') + '$'
                        nodes += filter_nodes(
                            lambda n: hasattr(n, attr) and \
                                      re.match(value,
                                               str(getattr(n, attr)),
                                               re.I),
                            self.root,
                            recursive=True
                            )
                    else:
                        self.err('invalid attribute match %s' % arg)
                for i, node in enumerate(nodes):
                    self.info('match %i:' % (i + 1))
                    self._show_tree(1, node)
            else:
                self.err('no tree to apply rule to.')
        else:
            self.err('expected rule type(s) to find')


    def command_help(self, *args):
        '''
        Executes the help command, which displays helpful information about one
        or more commands. Syntax:
        help <command> ...
        '''

        cmds = remove_duplicates(args)
        if not cmds:
            cmds.append('help')

        for cmd in cmds:
            try:
                func = getattr(self, 'command_' + cmd)
                self.info('Help for command "%s":\n' % cmd + func.__doc__)
            except AttributeError:
                self.err('No command "%s"' % cmd)


    def err(self, msg):
        '''Displays an error message.'''
        print TRSConsole.MSG_PRE + 'Error:', msg


    def warn(self, msg):
        '''Displays a warning message.'''
        print TRSConsole.MSG_PRE + 'Warning:', msg


    def info(self, msg):
        '''Displays a general info message.'''
        print TRSConsole.MSG_PRE + msg


    def _apply_rules(self, rules, repeat=False):
        """
        Applies any rules with names that match the patterns in <rules>. If
        repeat, the rule is applied until it won't.
        """
        if len(rules) >= 1:
            if self.root:
                rules = self.rule_set.match(*rules)
                if not rules:
                    self.err('no valid rules given')
                    return

                root = ParentNode('!fake root!', children=[ self.root ])
                num_repl = 0
                for rule in rules:
                    i = 1
                    while True:
                        i = rule.apply(root)
                        if i > 0:
                            num_repl += i
                            self.info('Rule %s applied; %i replacements.'\
                                  % (rule.name, i))
                            if len(root.children) == 0:
                                # This should not happen.
                                self.warn('tree was removed by rule.')
                                self.root = None
                            elif len(root.children) == 1:
                                self.root = root.children[0]
                                # Redo numbering since there are changes
                                add_node_numbers(root.children[0])
                            else:
                                self.warn('rule produced multiple new roots.')
                                self.info('Unifying under fake root.')
                                self.root = root
                                # Redo numbering since there are changes
                                add_node_numbers(root)

                        if not repeat or i == 0:
                            break

                if not num_repl:
                    self.info('No rules apply')
                elif repeat:
                    self.info('A total of %i replacements performed.'\
                              % num_repl)
            else:
                self.err('no tree to apply rule to.')
        else:
            self.err('expected rule name(s) as argument.')


    def _show_tree(self, max_depth, node, level=0):
        '''Displays tree starting at <node>.'''
        if hasattr(node, 'value'):
            print '%5i:%s T: %s, V: %s' % \
                  (node.number, level*'    ', node.type, node.value)
        else:
            print '%5i:%s T: %s' % \
                  (node.number, level*'    ', node.type)
            if level < max_depth:
                for child in node.children:
                    self._show_tree(max_depth, child, level+1)



def add_node_numbers(node, current=0):
    '''
    Adds an attribute number to every node in the tree starting at <node>.
    This attribute specifies how many nodes precede the node in the tree.
    '''
    node.number = current
    if hasattr(node, 'children') and node.children:
        for child in node.children:
            current = add_node_numbers(child, current + 1)
    return current



if __name__ == '__main__':
    trsc = TRSConsole()
    trsc.main_loop()

