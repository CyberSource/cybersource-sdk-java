@echo off

set LOCAL_CP=
set LOCAL_CP=%LOCAL_CP%;classes

rem ----------------------------------------------------------------------------
rem Replace cybersource-sdk-java-6.2.7.jar when using Java SDK 1.6 or later.
rem If using this scripts outside zip package then give maven clean install. 
rem This will generate all required dependencies under target/dependencies.These dependencies are used in CLASSPATH.
rem ----------------------------------------------------------------------------

if exist ../../lib (
	if not exist classes goto compile_error
	set LOCAL_CP=%LOCAL_CP%;../../lib/*
)
if not exist ../../lib (
	if not exist classes goto compile_error
	if not exist target goto error
	set LOCAL_CP=%LOCAL_CP%;target/dependencies/*
)

if "%JAVA_HOME%" == "" (
   set JAVA_CMD=java
) else (
   set JAVA_CMD="%JAVA_HOME%"\bin\java
)

if /I "%~1"=="" (
echo No Service Name entered ... Program terminating
goto eof
)


%JAVA_CMD% -version
%JAVA_CMD% -cp "%LOCAL_CP%" com.cybersource.sample.RunSample "%~1"
goto eof

:compile_error
echo "Classes are missing . execute compileSample script."
goto eof

:error
echo "Dependencies are missing."
echo "Execute maven clean install , This will generate all required dependencies under target/dependencies!!" 

:eof
