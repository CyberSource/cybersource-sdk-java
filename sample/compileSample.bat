@echo off

set LOCAL_CP=
set LOCAL_CP=%LOCAL_CP%;lib/cybsclients-1.0.0.jar

if not exist classes mkdir classes

if "%JAVA_HOME%" == "" (
   set JAVAC_CMD=javac
) else (
   set JAVAC_CMD=%JAVA_HOME%\bin\javac
)

"%JAVAC_CMD%" -d classes -classpath "%LOCAL_CP%" src/com/cybersource/sample/AuthCaptureSample.java


