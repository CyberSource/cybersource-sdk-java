@echo off

set LOCAL_CP=
rem ----------------------------------------------------------------------------
rem Replace this with cybersource-sdk-java-6.2.7.jar when using Java SDK 1.6 or later.
rem If using this scripts outside zip package then give maven clean install. 
rem This will generate all required dependencies under target/dependencies.These dependencies are used in CLASSPATH.
rem ----------------------------------------------------------------------------

if exist ../../lib set LOCAL_CP=%LOCAL_CP%;../../lib/cybersource-sdk-java-6.2.7.jar
if not exist ../../lib (
	if not exist target goto error
	set LOCAL_CP=%LOCAL_CP%;target/dependencies/cybersource-sdk-java-6.2.7.jar
)

if not exist classes mkdir classes

if "%JAVA_HOME%" == "" (
   set JAVAC_CMD=javac
) else (
   set JAVAC_CMD="%JAVA_HOME%"\bin\javac
)

%JAVAC_CMD% -d classes -classpath "%LOCAL_CP%" src/main/java/com/cybersource/sample/RunSample.java
goto eof

:error
echo "Dependencies are missing."
echo "Execute maven clean install , This will generate all required dependencies under target/dependencies!!" 

:eof

