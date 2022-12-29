Below follows simple build instructions for Socos. A standard Unix
environment with make, sed, grep, etc. and also Python version 2.6 or
later (not Python 3!) must be present.

Cd to 'pc', do 'make develconfig' followed by 'make'. You will see
some warnings when the parser are built, but they can be ignored. If
everything compiled correctly, you should now be able to run the
script called 'socos' in the 'pc' folder. E.g.:

./socos file.ibp

Note that using Socos requires PVS, ProofLite and Yices to also be
installed.  Socos looks for these dependencies when it is invoked to
verify a program and will inform you if they are not found. For PVS,
the executable 'pvs' should be on the PATH, and ProofLite should be
installed properly in the PVS library directory. Instructions for how
the latter is done can be found in the ProofLite documentation.

Yices should also be on the PATH. If you try to verify programs
without Yices being installed, the PVS (yices) command will fail and
Socos will not be able to prove anything.

To test if everything is working properly, run the
'run_acceptance_tests.sh' script. This script tries to verify a
collection of small programs that test various aspects of the tool.


