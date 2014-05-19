#!/bin/sh

LOCAL_CP=
LOCAL_CP=$LOCAL_CP:./classes
LOCAL_CP=$LOCAL_CP:lib/cybsclients-1.0.0.jar
LOCAL_CP=$LOCAL_CP:lib/cybssecurity_obs_1.0.jar
LOCAL_CP=$LOCAL_CP:lib/xercesImpl.jar
LOCAL_CP=$LOCAL_CP:lib/xml-apis.jar
LOCAL_CP=$LOCAL_CP:lib/saxon-7.3.1.jar
LOCAL_CP=$LOCAL_CP:lib/commons-httpclient-3.0.1.jar
LOCAL_CP=$LOCAL_CP:lib/commons-logging.jar
LOCAL_CP=$LOCAL_CP:lib/commons-codec-1.3.jar
LOCAL_CP=$LOCAL_CP:lib/xalan.jar

if [ "$JAVA_HOME" != "" ]; then
JAVA_CMD=$JAVA_HOME/bin/java
else
JAVA_CMD=java
fi

$JAVA_CMD -version
$JAVA_CMD -cp "$LOCAL_CP" com.cybersource.sample.AuthCaptureSample