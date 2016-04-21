#!/bin/sh

LOCAL_CP=
LOCAL_CP=$LOCAL_CP:./classes

# -----------------------------------------------------------------------------
# Replace cybersource-sdk-java-6.1.0.jar with cybsclients15.jar in /lib directory when using Java SDK 1.6 or
# later.
# -----------------------------------------------------------------------------
LOCAL_CP=$LOCAL_CP:../../lib/*

if [ "$JAVA_HOME" != "" ]; then
JAVA_CMD=$JAVA_HOME/bin/java
else
JAVA_CMD=java
fi

$JAVA_CMD -version
$JAVA_CMD -cp "$LOCAL_CP" com.cybersource.sample.AuthSample

