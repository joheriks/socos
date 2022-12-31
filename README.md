SOCOS was a tool for invariant-based programming. Active development ceased in 2010, this repository exists for historical / archival reasons.

In SOCOS, programs were drawn graphically in Eclipse and verified using the [PVS](https://pvs.csl.sri.com/) theorem prover. See the [tutorial](tutorial/socos_tutorial.pdf) for some concrete examples. Theory and implementation is described in [my thesis](https://www.doria.fi/handle/10024/64011).

Some comments in retrospect:

* A lot of effort went into the frontend-backend interface file format (`.ibp`); in hindsight, should probably have used JSON for this (it would have saved A LOT of time tweaking parsers).
* The [declarative rewrite system](pc/pc/rules) was a bad idea; the semantical complexity in the program to VC translation did not justify a meta-language layer, better do it directly in Python.
* The translation pipeline is embarrasingly sequential; with today's Python, you could optimize a lot with asyncio.

Some ideas on what to do with this codebase:

* Upgrade to PVS 7.1; should be fairly easy (might even work out of the box).
* Convert to Python 3 and modern Java; should be easy.
* Make it work on modern Eclipse; expect a fair share of crusty GEF code to port.
* An HTML5-based diagram editor, e.g. based on [GoJS](https://gojs.net/).

SOCOS is Copyright Johannes Eriksson and Ralph-Johan Back. See [LICENSE](LICENSE) for terms of reuse.
