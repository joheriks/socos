class Digraph(object):
    """ Simple class for representing a directed graph (a set of vertices and
    edges between vertices). Vertex objects must be comparable (with
    ==) and hashable. Edges are taken to be 3-tuples (x,y,z) describing
    an edge from x to y, where z describes the edge. """

    def __init__(self, name):
        """ Creates a new empty graph with specified name"""
        object.__init__(self)
        self.name = name
        self._adj_map = { }

    def add_vertex(self, v):
        """ Adds vertex v to graph, if not already present. """
        if not self._adj_map.get(v):
            self._adj_map[v] = []

    def del_vertex(self, v):
        """ Removes a vertex v from graph """
        if self._adj_map.has_key(v):
            del self._adj_map[v]
            for x, y, z in self.get_edges():
                if y == v:
                    self.del_edge(x, y, z)


    def unreachable_vertices( self, root ):
        """ Returns vertices unreachable from root """
        unvisited = set(self.get_vertices())

        def dfs_check(v):
            if v in unvisited:
                unvisited.remove(v)
                map(dfs_check, self.get_neighbours(v))

        dfs_check(root)
        return list(unvisited)
    

    def reachable_vertices( self, root ):
        """ Returns vertices reachable from root """
        visited = set([])

        def dfs_check(v):
            if not v in visited:
                visited.add(v)
                map(dfs_check, self.get_neighbours(v))

        dfs_check(root)
        return list(visited)

    def del_unreachable_vertices(self, root):
        """ Removes vertices unreachable from root """
        map(self.del_vertex, self.unreachable_vertices(root))

    def del_reachable_vertices(self, root):
        """ Removes vertices reachable from root """
        map(self.del_vertex, self.reachable_vertices(root))

    def add_edge(self, from_v, to_v, edge_description = None):
        """ Adds an edge between two vertices in the graph, adding the
        vertices to the graph if they are not already present. """

        self.add_vertex(from_v)
        self.add_vertex(to_v)

        self._adj_map[from_v] += [ (to_v, edge_description) ]

    def del_edge(self, from_v, to_v, edge_description = None):
        """ Deletes an edge from the graph"""
        edges = self._adj_map.get(from_v)
        if edges:
            edges.remove((to_v, edge_description))

    def get_vertices(self):
        """ Returns list of vertices in graph """
        return self._adj_map.keys()

    def get_edges(self):
        """ Returns list of all edges in graph (edges are x,y,z
        triplets as explained in class documentation)"""
        lst = []
        for item in self._adj_map.items():
            lst += map(lambda x : (item[0], x[0], x[1]), item[1])

        return lst

    def get_out_edges(self, v):
        """ Returns a list of outbound edges for vertice v """
        edges = self._adj_map.get(v)
        if edges:
            return edges
        return []

    def get_neighbours(self, v):
        """ Returns a list of vertices directly reachable from v """
        return map(lambda x : x[0], self.get_out_edges(v))

    def get_out_degree(self, v):
        """ Returns the out degree of v """
        return len(self.get_out_edges(v))

    def get_in_degree(self, v):
        """ Returns the in degree of v """
        degree = 0
        for _, y, _ in self.get_edges():
            if y == v:
                degree += 1

        return degree

    def is_empty(self):
        """ Return True if graph contains no vertices, false otherwise """
        return len(self._adj_map) == 0

    def num_vertices(self):
        """ Returns number of vertices in graph """
        return len(self._adj_map)

    def transpose(self):
        """ Transposes graph (not destructive). """
        graph = Digraph(self.name)
        map(graph.add_vertex, self.get_vertices())
        for x, y, z in self.get_edges():
            graph.add_edge(y, x, z)

        return graph

    def topological_sort(self):
        """ Returns a topological sorting of the vertices in the
        graph. The graph is assumed to be a acyclic """
        if self.is_empty():
            return []

        assert not self.is_cyclic()

        vertices = self._adj_map.keys()
        g = self.subgraph(vertices)
        queue = filter(lambda x : self.get_in_degree(x) == 0, vertices)
        sorted = []

        while queue:
            w = queue.pop()
            sorted.append(w)

            for y, _ in g.get_out_edges(w)[:]:
                g.del_edge(w, y)
                if g.get_in_degree(y) == 0:
                    queue.append(y)

        return sorted

    def is_cyclic(self):
        """ Returns true if graph has cycles, false otherwise."""

        if self.is_empty():
            return False

        # Color graph. 0 = white, 1 = gray, 2 = black

        vertices = self._adj_map.keys()
        for v in vertices:
            self._adj_map[v].append(0)

        unvisited = set(vertices)

        def dfs(v):
            ve = self._adj_map[v]

            if ve[-1] == 1:
                return True
            elif ve[-1] == 2:
                return False
            else:
                unvisited.discard(v)
                ret = False

                for w in ve[:-1]:
                    ve[-1] = 1
                    ret |= dfs(w[0])

                ve[-1] = 2

                return ret

        cyclic = False

        while len(unvisited) > 0:
            v = unvisited.pop()
            cyclic |= dfs(v)

        for v in vertices:
            self._adj_map[v].pop()

        return cyclic

    def subgraph(self, vertices):
        """ Returns a subgraph of graph containing only specified vertices """
        graph = Digraph("subgraph of %s" % (self.name,) )
        for v in vertices:
            graph.add_vertex(v)
            for e in self.get_out_edges(v):
                if e[0] in vertices:
                    graph.add_edge(v, e[0], e[1])

        return graph

    def scc(self, start_v):
        """ Returns a list of strongly connected components in graph
        reachable from start_v. List elements are subgraphs of
        self"""

        if self.is_empty():
            return [ self.subgraph([]) ]

        # Add extra information to vertices needed in algorithm:
        # dfs, lowlink and in_stack. Added as a list in that order.

        vertices = self._adj_map.keys()

        for v in vertices:
            self._adj_map[v].append([0, 0, False])

        # Use Tarjans algorithm

        unvisited = set(vertices)
        stack = []
        max_dfs = 0
        result = []
        self._tarjan(unvisited, stack, max_dfs, start_v, result)

        for v in vertices:
            self._adj_map[v].pop()

        return map(self.subgraph, result)

    def _tarjan(self, unvisited, stack, max_dfs, v, result):
        """ Tarjan's algorithm for scc:s """
        props = self._adj_map[v][-1]
        props[0] = max_dfs
        props[1] = max_dfs
        props[2] = True
        stack.append(v)
        unvisited.remove(v)

        for e in self.get_out_edges(v)[:-1]:
            w = e[0]
            w_props = self._adj_map[w][-1]

            if w in unvisited:
                self._tarjan(unvisited, stack, max_dfs + 1, w, result)
                props[1] = min(props[1], w_props[1])
            elif w_props[2]:
                props[1] = min(props[1], w_props[0])

        if props[0] == props[1]:
            component = []

            while True:
                w = stack.pop()
                 # w leaves the stack, update flag accordingly
                self._adj_map[w][-1][2] = False
                component.append(w)
                if w == v:
                    break

            result.append(component)

    def __str__(self):
        lst = []
        for item in self._adj_map.items():
            lst += map(lambda x : str((item[0], x[0], x[1])), item[1])

        lst2 = map(str, self._adj_map.keys())

        return ('Graph %s\nVertices:\n%s\nTriplets:\n%s' %
                (self.name, '\n'.join(lst2), '\n'.join(lst))).strip()

