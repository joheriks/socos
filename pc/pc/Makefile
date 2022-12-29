SHELL=/bin/bash

.PHONY: all build clean test disttest unittest accepttest sloc check elisp parsetabs distconfig develconfig

all: build

build: config.py parsetabs 
	@echo "Compiling:"
	python -c "import sys,compileall; sys.exit(not compileall.compile_dir('.',quiet=1))"
	@echo

# Force parsetab rebuild, PLY keeps track of dependencies
parsetabs:
	@echo "Building parsers:"
	python make_parsers.py
	@echo

config.py: develconfig

distconfig: Makefile
	echo "OPTIMIZE_PARSER = True" >> config.py

develconfig: Makefile
	echo "OPTIMIZE_PARSER = False" >> config.py

test: unittest accepttest

check:
	@ echo "Checking..."
	@ find . -name '*.py' -and -not -name '*_parsetab.py'  | xargs -n 1 pychecker -Q -6 -R 8 -c -J 8 -w -s

sloc:
	@ find . -name '*.py' -and -not -name '*_parsetab.py'  | xargs grep -vE '^[ \t]*(#.*)*$$' | wc -l


# Acceptance testing
accepttest: 
	./run_acceptance_tests.sh

unittest: 
	./run_unit_tests.sh


TAGS: *.py
	etags *.py

clean:
	rm -f *.pyc
	rm -f pp/*.pyc
	rm -f semantic/*.pyc
	rm -f parsing/*.pyc
	rm -f rewrite/*.pyc
	rm -f util/*.pyc
	rm -rf parsetabs
	rm -f *.tar.gz
	rm -f *.rpm
	rm -f examples/*.log*
	rm -rf examples/pvsbin

distclean: clean
	rm -f config.py
