
VESTIGE_VERSION=1.0.0

vestige-setup:
	mkdir -p target/vestige
	unzip -d target/vestige ../standard_edition_standalone/target/vestige.assemblies.standard_edition_standalone-$(VESTIGE_VERSION)-windows.zip
	sed -i~ -e 's/VESTIGE_BASE=%DIRNAME%/VESTIGE_BASE=%appdata%\\Vestige/' target/vestige/vestige.bat
	rm target/vestige/vestige.bat~
	cp src/vestige.iss target
	cp src/CheckJava.class target
	cp src/checkJava.bat target

clean:
	$(RM) -r target
