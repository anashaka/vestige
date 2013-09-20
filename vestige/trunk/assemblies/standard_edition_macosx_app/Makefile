
VESTIGE_VERSION=1.0.0

Vestige.pkg:
	mkdir target
	cp -r src/Vestige.app target/
	unzip -d target/Vestige.app/Contents/Resources/vestige_home ../standard_edition_standalone/target/vestige.assemblies.standard_edition_standalone-$(VESTIGE_VERSION)-unix.zip
	sed -i~ -e 's/VESTIGE_BASE=.*/VESTIGE_BASE="$$HOME\/Library\/Application Support\/Vestige"/' target/Vestige.app/Contents/Resources/vestige_home/vestige
	rm target/Vestige.app/Contents/Resources/vestige_home/vestige~
	-find target -name .svn -o -name .DS_Store -exec rm -rf {} \;

clean:
	$(RM) -r target
