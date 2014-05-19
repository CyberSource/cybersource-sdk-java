#!/bin/sh

LOCAL_CP=
LOCAL_CP=$LOCAL_CP:lib/cybsclients-1.0.0.jar

if test ! -d ./classes 
then
   mkdir classes
fi

if [ "$JAVA_HOME" != "" ]; then
JAVAC_CMD=$JAVA_HOME/bin/javac
else
JAVAC_CMD=javac
fi

$JAVAC_CMD -d ./classes -classpath "$LOCAL_CP" src/com/cybersource/sample/AuthCaptureSample.java
