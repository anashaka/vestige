
VESTIGE_VERSION=1.0.0
VESTIGE_RPM_VERSION=0.0.1

Vestige.pkg:
	mkdir target
	unzip -d target/vestige-$(VESTIGE_RPM_VERSION) ../standard_edition_standalone/target/vestige.assemblies.standard_edition_standalone-$(VESTIGE_VERSION)-unix.zip
	sed -i~ -e 's/VESTIGE_BASE=.*/VESTIGE_BASE="$$HOME\/.vestige"/' -e 's/LIBDIR=.*/LIBDIR=\/usr\/share\/vestige/' -e 's/CONFDIR=.*/CONFDIR=\/etc\/vestige\//' target/vestige-$(VESTIGE_RPM_VERSION)/vestige
	rm target/vestige-$(VESTIGE_RPM_VERSION)/vestige~
	sed -e 's/Version:        .*/Version:        $(VESTIGE_RPM_VERSION)/' -e 's/standard_edition_bootstrap.*-jar-with-dependencies.jar/standard_edition_bootstrap-$(VESTIGE_RPM_VERSION)-jar-with-dependencies.jar/' src/vestige.spec > target/vestige.spec
	cp src/Makefile target/vestige-$(VESTIGE_RPM_VERSION)/
	cp -r src/hicolor target/vestige-$(VESTIGE_RPM_VERSION)/
	cp -r src/vestige_gtk_launcher.py target/vestige-$(VESTIGE_RPM_VERSION)/
	chmod +x target/vestige-$(VESTIGE_RPM_VERSION)/vestige_gtk_launcher.py
	cp -r src/vestige.desktop target/vestige-$(VESTIGE_RPM_VERSION)/
	-find target -name .svn -o -name .DS_Store -exec rm -rf {} \;
	pushd target && tar -czf vestige.tar.gz vestige-$(VESTIGE_RPM_VERSION) && popd
	scp -P 2224 target/vestige.tar.gz builder@localhost:rpmbuild/SOURCES/vestige.tar.gz
	scp -P 2224 target/vestige.spec builder@localhost:rpmbuild/SPECS/vestige.spec
	ssh -p 2224 builder@localhost rpmbuild -bb '~/rpmbuild/SPECS/vestige.spec'
	scp -P 2224 builder@localhost:rpmbuild/RPMS/noarch/vestige-$(VESTIGE_RPM_VERSION)-1.noarch.rpm target/vestige-$(VESTIGE_RPM_VERSION)-1.noarch.rpm 

clean:
	$(RM) -r target
