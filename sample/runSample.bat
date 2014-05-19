@echo off

set LOCAL_CP=
set LOCAL_CP=%LOCAL_CP%;classes
set LOCAL_CP=%LOCAL_CP%;lib/cybsclients-1.0.0.jar
set LOCAL_CP=%LOCAL_CP%;lib/cybssecurity_obs_1.0.jar
set LOCAL_CP=%LOCAL_CP%;lib/xercesImpl.jar
set LOCAL_CP=%LOCAL_CP%;lib/xml-apis.jar
set LOCAL_CP=%LOCAL_CP%;lib/saxon-7.3.1.jar
set LOCAL_CP=%LOCAL_CP%;lib/commons-httpclient-3.0.1.jar
set LOCAL_CP=%LOCAL_CP%;lib/commons-logging.jar
set LOCAL_CP=%LOCAL_CP%;lib/commons-codec-1.3.jar
set LOCAL_CP=%LOCAL_CP%;lib/xalan.jar



if "%JAVA_HOME%" == "" (
   set JAVA_CMD=java
) else (
   set JAVA_CMD=%JAVA_HOME%\bin\java
)

"%JAVA_CMD%" -version
"%JAVA_CMD%" -cp "%LOCAL_CP%"  com.cybersource.sample.AuthCaptureSample
