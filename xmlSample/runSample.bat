
set LOCAL_CP=
set LOCAL_CP=%LOCAL_CP%;classes
set LOCAL_CP=%LOCAL_CP%;../sample/lib/cybsclients-1.0.0.jar
set LOCAL_CP=%LOCAL_CP%;../sample/lib/cybssecurity_obs_1.0.jar
set LOCAL_CP=%LOCAL_CP%;../sample/lib/xercesImpl.jar
set LOCAL_CP=%LOCAL_CP%;../sample/lib/xml-apis.jar
set LOCAL_CP=%LOCAL_CP%;../sample/lib/saxon-7.3.1.jar
set LOCAL_CP=%LOCAL_CP%;../sample/lib/commons-httpclient-3.0.1.jar
set LOCAL_CP=%LOCAL_CP%;../sample/lib/commons-logging.jar
set LOCAL_CP=%LOCAL_CP%;../sample/lib/commons-codec-1.3.jar
set LOCAL_CP=%LOCAL_CP%;../sample/lib/xalan.jar



if "%JAVA_HOME%" == "" (
   set JAVA_CMD=java
) else (
   set JAVA_CMD=%JAVA_HOME%\bin\java
)

"%JAVA_CMD%" -version

"%JAVA_CMD%" -cp "%LOCAL_CP%"  com.cybersource.sample.AuthSample
