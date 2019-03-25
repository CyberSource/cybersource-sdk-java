#!/bin/sh

LOCAL_CP=
# -----------------------------------------------------------------------------
# Replace this with cybersource-sdk-java-6.2.7.jar when using Java SDK 1.6 or later.
# If using this scripts outside zip package then give maven clean install.
# This will generate all required dependencies under target/dependencies.These dependencies are used in CLASSPATH.
# -----------------------------------------------------------------------------

if test -d ../../lib
then LOCAL_CP=$LOCAL_CP:../../lib/cybersource-sdk-java-6.2.7.jar
fi

if test ! -d ../../lib
then
	if test ! -d ./target
	then
		echo "Dependencies are missing."
		echo "Execute maven clean install , This will generate all required dependencies under target/dependencies!!"
		exit 1
	fi
LOCAL_CP=$LOCAL_CP:target/dependencies/cybersource-sdk-java-6.2.7.jar
fi

if test ! -d ./classes
then
   mkdir classes
fi

if [ "$JAVA_HOME" != "" ]; then
JAVAC_CMD=$JAVA_HOME/bin/javac
else
JAVAC_CMD=javac
fi

$JAVAC_CMD  -d ./classes -classpath "$LOCAL_CP" src/main/java/com/cybersource/sample/RunSample.java

