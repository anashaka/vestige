
VESTIGE_VERSION=1.0.0

vestige-setup:
	mkdir -p target/vestige
	unzip -d target/vestige ../standard_edition_standalone/target/vestige.assemblies.standard_edition_standalone-$(VESTIGE_VERSION)-windows.zip
	sed -i~ -e 's/VESTIGE_BASE=%DIRNAME%/VESTIGE_BASE=%appdata%\\Vestige/' target/vestige/vestige.bat
	rm target/vestige/vestige.bat~
	cp src/vestige.iss target
	cp src/CheckJava.class target
	cp src/checkJava.bat target
	-find target -name .svn -o -name .DS_Store -exec rm -rf {} \;
	-ssh -p 2225 root@localhost 'rmdir /S /Q C:\target'
	scp -r -P 2225 target/ root@localhost:C:/
	ssh -p 2225 root@localhost '"C:\Program Files\Inno Setup 5\iscc.exe" C:\target\vestige.iss'
	scp -P 2225 root@localhost:C:/target/Output/vestige-setup.exe target/vestige-setup.exe
clean:
	$(RM) -r target
