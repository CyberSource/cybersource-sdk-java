Copyright 2002-2006 CyberSource Corporation

CyberSource Java SDK for ICS

Description and brief information on different test examples in this directory:
===============================================================================

For an example of how to create an application that uses CyberSource
applications, refer to the ICSClientTest.java file. This is a very
simple application that calls the ICS client libraries and requests an
application from the ICS servers.

For testing purposes, the ICSFileTest.java file is also included.
This is a simple application. Its standard input is an ICS message
that consists of name=value pairs which it sends to the ICS server.
This file is provided as a mechanism to quickly test various ICS
messages. The command

  java ICSFileTest in

takes the "in" file as input, then sends it to the ICS application
servers.  All the parameters can be set as name=value pairs. This
mechanism enables developers who are familiar with the ICS field names 
and values to quickly test their applications. ICSFileTest is not required 
by any ICS application.

To gain an increased understanding of the SDK libraries, follow the test
examples to learn more about the different CyberSource services. To run
the following examples, set up your CLASSPATH as described in the README 
file in the root directory of this package.

Use the following syntax to run the test examples:

  java filename
  

NOTE:
  Do not use the ".java" or the ".class" file extensions when you use 
  the test files.


1) ICSAuthBillTest.*
      Demonstrates how to use the CyberSource Authorization and Billing 
      services.

2) ICSAuthTest.*
      Demonstrates how to use the CyberSource Authorization service.

3) ICSDownloadTest.*
      Demonstrates the use of the CyberSource "ics_download" service.

4) ICSELCTest.*
      Demonstrates the use of the CyberSource "ics_elc" service.

5) ICSExportTest.*
      Demonstrates the use of the CyberSource "ics_export" service.

6) ICSScoreAuthBillTest.*
      Demonstrates how to use multiple CyberSource services within the
      same request. It uses "ics_score,ics_auth,ics_bill" services
      within the same request.

7) ICSScoreTest.*
      Demonstrates the use of the CyberSource "ics_score" service,
      which is itself part of the complete suite of CyberSource
      Risk Management services.

8) ICSThreadTest
    	Demonstrates the use of Threads in calling ICSClient libraries. This
	class can control the no. of loops and threads by passing it as
    	parameters.

9) ICSTaxTest.*
      Demonstrates the use of the CyberSource "ics_tax" service.

10) TestAll.*
      Runs all of the previously mentioned tests in sequential order.

11) SecureRandomTest
    	This class file tests the secure random number generation which is
	sometimes very slow on certain JVM's.

12) ICSClientProxyTest
    	Demonstrates the use of proxy host and proxy port when calling
	ICSClient libraries.

13) ICSServletTest.*
      Demonstrates the use of CyberSource Services using servlets.
      Modify the path to the properties directory(properties file)
      when moving the class file to the server directory and recompile
      accordingly.

14) ICSECPCreditTest.*
      Demonstrates the use of the CyberSource "ics_ecp_credit" service.

15) ICSECPDebitTest.*
      Demonsrates the use of the CyberSource "ics_ecp_debit" service.

The various applications that CyberSource offers have their own fields, 
as well as special requirements you must adhere to when you send an 
ICS request to be processed. Refer to the ICS2 Developer's Guide for 
more information on what must be passed for the services to function 
properly.
