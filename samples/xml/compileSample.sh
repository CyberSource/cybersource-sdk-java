#!/bin/sh

LOCAL_CP=
# -----------------------------------------------------------------------------
# Replace this with cybersource-sdk-java-6.1.0.jar when using Java SDK 1.6 or later.
# -----------------------------------------------------------------------------
LOCAL_CP=$LOCAL_CP:../../lib/cybersource-sdk-java-6.1.0.jar

if test ! -d ./classes 
then
   mkdir classes
fi

if [ "$JAVA_HOME" != "" ]; then
JAVAC_CMD=$JAVA_HOME/bin/javac
else
JAVAC_CMD=javac
fi

$JAVAC_CMD -d ./classes -classpath "$LOCAL_CP" src/com/cybersource/sample/AuthSample.java
