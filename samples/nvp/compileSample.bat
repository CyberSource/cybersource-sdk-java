@echo off

set LOCAL_CP=
rem ----------------------------------------------------------------------------
rem Replace this with cybersource-sdk-java-6.1.0.jar when using Java SDK 1.6 or later.
rem ----------------------------------------------------------------------------
set LOCAL_CP=%LOCAL_CP%;../../lib/cybersource-sdk-java-6.1.0.jar

if not exist classes mkdir classes

if "%JAVA_HOME%" == "" (
   set JAVAC_CMD=javac
) else (
   set JAVAC_CMD=%JAVA_HOME%\bin\javac
)

%JAVAC_CMD% -d classes -classpath "%LOCAL_CP%" src/com/cybersource/sample/AuthCaptureSample.java


