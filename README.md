===============================================================================
CyberSource Simple Order API for Java
Copyright 2014 CyberSource Corporation
===============================================================================

CyberSource APIs provide access to every service CyberSource offers. With Simple Order API, 
CyberSource provides the client software. It's the right choice for merchants who want scalability, 
a full range of services, and greater control of the buying experience.

Tip: Software Development Kits contain API Client software, code samples, and release notes
Simple Order API SDK Client uses Bouncy Castle and Apache WS Security Open source jars 
for sending the transactions over SOAP based Web services.

- Bouncy Castle jars are used for loading the P12 and Apache WS Security for singing the Request XML Document 

- P12 keys generated to specific merchant will be used for encrypting the data and for signing the SOAP XML Request

-------------------------------------------------------------------------------
RELEASE NOTES
-------------------------------------------------------------------------------

Please refer to the CHANGES file for the release notes.


-------------------------------------------------------------------------------
REQUIREMENTS
-------------------------------------------------------------------------------

P12 Key for specific to Merchant ID needs to generated.

IMPORTANT: ** JUNIT Test Cases will fail until you configure or add specific P12 key details in each Test Case.

--------------------
Minimum Requirements
--------------------
Java SDK 1.5 or later 
	


-------------------------------------------------------------------------------
CONFIGURATION AND TESTING THE SDK CLIENT ( This is not Junit Test cases )
-------------------------------------------------------------------------------

1. You need a security key (<your merchant id>.p12) in order to send requests
   to the server.  If you do not have your key yet, refer to the CyberSource
   Developer's Guide for the procedure to create one.
   
2. Download the sdk-java-master.zip package into a directory of your choice. 

3. Extract and Go to the sdk-java-master directory.

4. Edit cybs.properties and make the following changes:

	a. Set merchantId to your merchantID.  Please note that it is
           case-sensitive.
	b. Set keysDirectory to the directory where your key resides.  Use
	   forward-slashes for the directory separator, even on Windows.
	   For example, "c:/keys"
	   
	(Optional Additional Changes)
	c. Set targetAPIVersion to the latest version displayed at:
	   https://<cybersource-host>/commerce/1.x/transactionProcessor/
	   By default, it is set to the latest version when the package was
	   created.
	d. Modify the logging properties as appropriate. Use forward-slashes for
	   the directory separator in the logDirectory value, even on Windows.
	   The directory you specify must already exist.
	e. Please refer to the accompanying documentation for the other
           optional properties that you may wish to specify.
	   
	NOTE:  sendToProduction is initially set to false.  Set it to true only
	       when you are ready to send live transactions.
	
5. Build this project using Maven 

        a. mvn clean  // Cleans the Project
        
        b. mvn install 
           // Builds the project and creates a jar file of client SDk
           // Includes running all JUnit Test cases 
        
        c. mvn install -Dmaven.test.skip=true  
           // This is for building the project by skipping the Junit Test cases
        

NOTES:

1. If you encounter the following exception message when you run the test case:

  		java.net.MalformedURLException: unknown protocol: https

   you will need to follow the instructions in the "If Unable to Connect to
   External Sites with HTTPS" section appropriate to the Java SDK you are
   using below.
   
2. If you encounter the following exception message when you run the test case:

		javax.net.ssl.SSLException: untrusted server cert chain

   you will need to follow the instructions in the "Importing the Root CA
   Certificate" section below.

   

---------------------------------
Importing the Root CA Certificate
---------------------------------

Perform this procedure only if the following exception message appeared when
you ran the test case:  javax.net.ssl.SSLException: untrusted server cert chain

1. Go to the SimpleOrderAPI directory where the file entrust_ssl_ca.cer
   is located.

2. Type the following (without the line breaks):

	  	keytool -import -alias entrust_ssl_ca 
	  			-keystore <JAVA_HOME>/jre/lib/security/cacerts 
	  			-file entrust_ssl_ca.cer
	  			
	  	where <JAVA_HOME> is the path to your Java installation.
	  	
   (keytool is a utility included in the Java SDK.)
	  	
3. Enter the keystore password when prompted for it.  The default password
   is usually "changeit".
   
 
-------------------------------------------------------------------------------
JAR FILES INCLUDED
-------------------------------------------------------------------------------

The following are the jar files included in the lib directory:

bcprov-jdk16-1.45.jar  	- includes the classes required for Digital Signature and Encrytion techniques. 
		          These mainly used for signing the outgoing messages/Transaction data.	
xmlsec-2.0.0.jar        - Part of Apache Santuario , this contains XML parsing packages.			  
wss4j-1.6.5.jar         - part of Apache Web Security  jar used for building and signing the XML document
slf4j-api-1.7.7.jar   	- Logging helper jars
slf4j-jcl-1.7.7.jar     - Helps in logging.
commons-logging-2.6.jar - commons-logging jar file needed by HttpClient.
	  
----------------------------------------------------------------------------------
JUNIT TEST CASES	  
----------------------------------------------------------------------------------

** JUnit test cases will fail until you provide the generated P12 key and add P12 key details in all Test cases.

TESTING THE NAME-VALUE PAIR SAMPLE

   Junit Test Case  : ClientTest.java is made available under Test package which
   has sample Name-Value pair Merchant and Transaction details.
   Command Line : mvn clean install or mvn test 
  This will run all the Test cases under this project.
  Target URL and Merchant Details are hard coded in each Test Case. 
  User can change the hard coded details and re-run the test case.
  
  
  
TESTING THE XML SAMPLE

   Junit Test Case  : XMLClientTest.java is made available under Test package which contains sample
   auth.xml file which is being used for fetching the Merchant and Transaction details.
   Command Line : mvn clean install or mvn test 
   This will run all the Test cases under this project.
   Target URL and Merchant Details are hard coded in each Test Case. 
   User can change the hard coded details and re-run the test case.

-------------------------------------------------------------------------------
THIRD-PARTY LICENSES
-------------------------------------------------------------------------------

This product includes software developed by the Apache Software Foundation
(<http://www.apache.org>).

See the accompanying NOTICE and LICENSE files.


-------------------------------------------------------------------------------
DOCUMENTATION
-------------------------------------------------------------------------------

For the Javadoc for the client's classes, bring up docs/api/index.html in a Web
browser.
 
For more information about installing and using this software package, see the
accompanying documentation.
 
Business Center users:

For information about how to use the various payment services, see the Business
Center Simple Order API User's Guide, available in the documentation area of the
Business Center.
 
Enteprise Business Center users:

For information about how to use a specific ICS service, see the Implementation
Guide for that service.  The Implementation Guides are available on the Support
Center at:

http://www.cybersource.com/support_center/support_documentation/services_documentation/

