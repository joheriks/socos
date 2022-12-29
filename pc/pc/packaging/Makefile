# Version of software
ifeq ($(SOCOS_VERSION),)
SOCOS_VERSION:=0.$(shell date '+%Y%m%d')
endif
export SOCOS_VERSION

PLATFORM=noarch

SOURCETGZ:=socos-pc-$(SOCOS_VERSION).src.tar.gz
BINARYTGZ:=socos-pc-$(SOCOS_VERSION).tar.gz
RPM:=socos-pc-$(SOCOS_VERSION).$(PLATFORM).rpm

BUILDROOT:=buildroot
BUILDDIR:=build
DISTDIR:=dist

.PHONY: sourcetgz binarytgz rpm clean

sourcetgz: $(SOURCETGZ)

binarytgz: $(BINARYTGZ)

$(DISTDIR): $(BUILDDIR) MANIFEST
	mkdir -p $(DISTDIR)
	cd $(BUILDDIR) && xargs cp -a -r --parents -t ../$(DISTDIR) < ../MANIFEST 

$(BINARYTGZ): $(DISTDIR)
	xargs tar -c -z -f $(BINARYTGZ) $(DISTDIR)

$(BUILDDIR): $(SOURCETGZ)
	mkdir -p $@
	cd $(BUILDDIR) && rm -rf pc && tar -xzf ../$(SOURCETGZ)
	PYTHONPATH=$(shell pwd)/$(BUILDDIR) make -C $(BUILDDIR)/pc distconfig build
	mkdir -p $(BUILDDIR)

$(SOURCETGZ):
	cd .. && $(MAKE) distclean
	cd ../../ && tar -czf pc/packaging/$(SOURCETGZ) --exclude packaging --exclude .svn --exclude=.disttest --exclude "*~" --exclude "*.pyc" --exclude "*.pyo" pc 

rpm: $(RPM)

$(RPM): $(SOURCETGZ) socos-pc.spec $(BUILDDIR) $(BUILDROOT) 
	rpmbuild -ba --buildroot="$(shell pwd)/$(BUILDROOT)" --target $(PLATFORM) --define="_rpmdir $(shell pwd)" --define="_srcrpmdir $(shell pwd)" --define="_sourcedir $(shell pwd)" --define="_builddir $(shell pwd)/$(BUILDDIR)" socos-pc.spec

$(BUILDROOT):
	mkdir -p $@


socos-pc.spec: socos-pc.spec.in
	sed -re 's|@socosversion@|$(SOCOS_VERSION)|g;' $< > $@	

clean:
	rm -f socos-pc-*.tar.gz
	rm -f *.spec
	rm -rf $(BUILDDIR) $(BUILDROOT) $(DISTDIR)
	rm -rf i386 i486 i586 i686 athlon noarch
	rm -f *.rpm
