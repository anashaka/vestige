${at}echo off

setlocal

set DIRNAME=%~dp0
if "%DIRNAME:~-1%" == "\" (set DIRNAME=%DIRNAME:~0,-1%)

set LIBDIR=%DIRNAME%\lib
set CONFDIR=%DIRNAME%

if defined JAVA goto :java_found

for %%I in (java.exe) do set JAVA=%%~$PATH:I
if defined JAVA goto :java_found

if defined JAVA_HOME goto :java_home_found

echo Unable to start a JVM : %%JAVA%% is not set and java.exe is not in PATH and %%JAVA_HOME%% is not set
exit /B 1

:java_home_found
set JAVA=%JAVA_HOME%\bin\java.exe

:java_found

if not defined JAVA_OPTS set JAVA_OPTS=-Djava.net.useSystemProxies=true

if not defined VESTIGE_BASE set VESTIGE_BASE=%DIRNAME%

if not defined VESTIGE_OPTS set VESTIGE_OPTS=%JAVA_OPTS%

if defined VESTIGE_DEBUG set VESTIGE_OPTS=%VESTIGE_OPTS% -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000

set LOGBACK_CONFIGURATION_FILE=%VESTIGE_BASE%\logback.xml
if not exist "%LOGBACK_CONFIGURATION_FILE%" set LOGBACK_CONFIGURATION_FILE=%CONFDIR%\logback.xml

set VESTIGE_OPTS=%VESTIGE_OPTS% -Djava.util.logging.manager=com.googlecode.vestige.core.logger.JULLogManager -Dlogback.logsDirectory="%VESTIGE_BASE%\logs" -Dlogback.configurationFile="%LOGBACK_CONFIGURATION_FILE%"

"%JAVA%" %VESTIGE_OPTS% -jar "%LIBDIR%\vestige.core-${vestige.core.version}.jar" "%LIBDIR%\vestige.assemblies.standard_edition_bootstrap-${project.version}-jar-with-dependencies.jar" com.googlecode.vestige.jvm_enhancer.JVMEnhancer com.googlecode.vestige.resolver.maven.VestigeMavenResolver "%CONFDIR%\m2\vestige-se.xml" "%CONFDIR%\m2\settings.xml" "%VESTIGE_BASE%\m2\resolver-cache.ser" "%CONFDIR%" "%VESTIGE_BASE%" || exit /B 2

endlocal
