SOCOS was a tool for invariant-based programming. Active development ceased in 2010, this repository exists for historical / archival reasons.

In Socos, programs were drawn graphically in Eclipse and verified using the [PVS](https://pvs.csl.sri.com/) theorem prover. See the [tutorial](tutorial/socos_tutorial.pdf) for some concrete examples. Theory and implementation is described in [my thesis](https://www.doria.fi/handle/10024/64011).

Some ideas on what to do with this codebase:

* Upgrade to PVS 7.1; should be fairly easy (might even work out of the box).
* Convert to Python 3 and modern Java; should be fairly easy.
* Make it work on modern Eclipse; expect a fair share of crusty GEF code to port.
