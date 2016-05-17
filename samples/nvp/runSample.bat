@echo off

set LOCAL_CP=
set LOCAL_CP=%LOCAL_CP%;classes

rem ----------------------------------------------------------------------------
rem Replace cybersource-sdk-java-6.1.1.jar with cybsclients15.jar in /lib directory when using Java SDK 1.6 or
rem later.
rem ----------------------------------------------------------------------------
set LOCAL_CP=%LOCAL_CP%;../../lib/*

if "%JAVA_HOME%" == "" (
   set JAVA_CMD=java
) else (
   set JAVA_CMD=%JAVA_HOME%\bin\java
)

%JAVA_CMD% -version
%JAVA_CMD% -cp "%LOCAL_CP%" com.cybersource.sample.AuthCaptureSample
