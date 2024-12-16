@echo off
set CP=.\ics.jar
java -classpath %CP% com.cybersource.ics.client.security.ECertApp %*
