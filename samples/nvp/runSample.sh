#!/bin/sh

LOCAL_CP=
LOCAL_CP=$LOCAL_CP:./classes

# -----------------------------------------------------------------------------
# Replace this with cybersource-sdk-java-6.2.7.jar when using Java SDK 1.6 or later.
# If using this scripts outside zip package then give maven clean install.
# This will generate all required dependencies under target/dependencies.These dependencies are used in CLASSPATH.
# -----------------------------------------------------------------------------

if test -d ../../lib
then
	if test ! -d ./classes
	then
		echo "Classes are missing . execute compileSample script."
		exit 1
	fi
LOCAL_CP=$LOCAL_CP:../../lib/*
fi

if test ! -d ../../lib
then
	if test ! -d ./classes
	then
		echo "Classes are missing . execute compileSample script."
		exit 1
	fi
	if test ! -d ./target
	then
		echo "Dependencies are missing."
		echo "Execute maven clean install , This will generate all required dependencies under target/dependencies!!"
		exit 1
	fi
LOCAL_CP=$LOCAL_CP:target/dependencies/*
fi	

if [ "$JAVA_HOME" != "" ]; then
JAVA_CMD=$JAVA_HOME/bin/java
else
JAVA_CMD=java
fi


if [ -z "$1" ];
then
echo "No service_name was mentioned ... terminating program"
exit 1
fi

$JAVA_CMD -version
$JAVA_CMD -cp "$LOCAL_CP" com.cybersource.sample.RunSample $1

