Copyright 2002-2006 CyberSource Corporation

CyberSource Java SDK for ICS


To send transactions to the CyberSource ICS application servers with
the Java SDK, you must first create your own private key and
certificate with the ecert application.  The key and certificate are
both used to authenticate and sign all transactions between you and
CyberSource.  You will use your unique keys for both test and live
transactions.

To use this SDK, you must be a registered ICS2 merchant. If you have
not already obtained a merchant ID from CyberSource, go to
http://www.cybersource.com/register and register for a test account.

If you have questions or comments, visit the Support Center web site
at http://www.cybersource.com/support/.  When prompted to log in,
enter the username and password you received when you registered with
CyberSource.  Information is frequently updated on the Support Center
web site. Remember to check back regularly for updates and
information.

NOTE: For information on how to use the support for Enterprise Java
Beans (EJB) in Java SDK, refer to the README_EJB.txt file.

See the DOCUMENTATION section at the bottom of this file for links to
the ICS implementation guides.

SYSTEM REQUIREMENTS	

This distribution was tested and developed with JDK 1.2.1 on
Solaris-2.6.  If you use a JVM other than that provided by Sun
Microsystems, you might encounter problems.

This version of the Java SDK does not support JDK 1.1.

CONFIGURING YOUR CLASSPATH

To configure your CLASSPATH to use this version of the Java SDK,
follow these steps:

1. Ensure that JDK 1.2.x or later is correctly installed on your computer.

2. Ensure that the directory where you installed the ics.jar
   file is listed in your CLASSPATH.

   For UNIX csh, use the following commands:

   setenv CLASSPATH       
   ${CLASSPATH}:<INSTALLATION_DIR>/ics_n.n.n/ics.jar
   
   where n.n.n is the client version.
   

   For UNIX sh, use the following commands:

   CLASSPATH=${CLASSPATH}:<INSTALLATION_DIR>/ics_n.n.n/ics.jar;
   export CLASSPATH
   
   where n.n.n is the client version.


   For Windows:

   set CLASSPATH=%CLASSPATH%;<INSTLLATION_DIR>\ics_n.n.n\ics.jar
   
   where n.n.n is the client version.

   On Windows, you can also set it in the system environment variables.

   a. Right-click My Computer.
   b. Select Properties.
   c. In the Properties dialog box, select the Environment tab. 
   d. Edit the CLASSPATH property to add the path to the 
      ics.jar file.  For example:

      C:\CyberSource\ics_n.n.n\ics.jar
   
      where n.n.n is the client version.


MERCHANT ID

The ICS merchant_id field uniquely identifies you to CyberSource and
is not related in any way to the merchant ID assigned to you by a
payment processor. To use your encryption key and certificate to send
transactions to the CyberSource ICS test or production servers, you
must provide your CyberSource merchant ID in each transaction.

GENERATING AND USING YOUR PRIVATE KEY AND CERTIFICATE

To generate your private key and certificate to use for sending test
and live transactions to the ICS2 server, Run the ecert application 
to generate your private key and certificate (see "RUNNING THE 
ECERT APPLICATION" below). You must provide the merchant ID 
supplied to you when you registered as a merchant with CyberSource.

RUNNING THE ECERT APPLICATION

This distribution contains a script named "ecert" ("ecert.bat", for
Windows), located in the base directory of the package. The ecert
script generates a certificate request and submits it to the ICS
server.  The server fulfills the request and replies with a signed
certificate.

To run ecert, follow these steps.  (In this example, we will assume
that your merchantId is "YourMerchantId".)

1. Change to the ics_n.n.n directory (where n.n.n is the client
   version).

2. Run the ecert script and specify your merchantId.

   ecert YourMerchantId

   (This assumes that the current directory, ".", is in your PATH. If
   not, run it like this: ./ecert YourMerchantId.)

   Run 'ecert -help' to see a list of additional options that you can
   set, including options for an HTTP proxy server.

3. If the request is successful, ecert will write these files to disk.

   keys/YourMerchantId.crt     (certificate)
   keys/YourMerchantId.pvt     (private key)
   keys/YourMerchantId.pwd     (certificate verification password)
   keys/CyberSource_SJC_US.crt (server certificate)
   properties/ICSClient.props  (client properties)

4. If you have more than one client installation, you must copy these
   output files (at least the .crt and .pvt files) to the other
   locations.

The ecert application, by default, inserts a new certificate into the
CyberSource server's test database. When you complete testing and your
site is ready to go live, contact the CyberSource Implementation 
Support Engineering (ISE) group to request activation of your 
generated certificate in the production database.

The CyberSource ISE group verbally verifies the password that is
produced by ecert. After the generated certificate is activated in the
production database, you can use your certificate and private key to
send live requests to the ICS production server.

TESTING A TRANSACTION

To test a transaction, follow these steps:

1. Change to the test directory. 

2. Enter the following command to run the test application:

   java ICSClientTest

Note that you can modify the values for some of the fields (for
example, the setBillCity("")field) to deliberately fail a service.

There are other test files in the test directory. For example, to test
auth, run java ICSAuthTest. To deliberately fail an auth transaction,
change the request.setCustomerCreditCardExpirationYear("1999")in 
ICSAuthText.java, recompile, and run it again.

If ICSClientTest fails unexpectedly, confirm that you completed the
steps as outlined above before you send e-mail to the support address:

   ics_support@cybersource.com

For an example of how to create an application that uses CyberSource
applications, refer to the ICSClientTest.java file. This is a very
simple application that calls the ICS client libraries and requests an
application from the ICS servers.

For testing purposes, the ICSFileTest.java file is also included.
This is a simple application. Its standard input is an ICS message
that consists of name=value pairs which ICSFileTest sends to the ICS
server. ICSFileTest is provided as a mechanism to quickly test various
ICS messages. The command

  java ICSFileTest myinfile

takes the file "myinfile" as input and sends it to the ICS application
servers.  You can set all of the parameters for ICSFileTest as
name=value pairs. This mechanism enables developers who are familiar
with the ICS2 field names and values to quickly test their
applications. ICSFileTest is not required by any ICS application.

The various services CyberSource offers have their own fields, as well
as special requirements you must adhere to when sending ICS requestd
to be processed. Refer to the ICS implementation guides for details on
which information to pass for each service.

For more detailed information on the different sample tests included
with the Java SDK distribution file, see the "README_TEST" file.

PREPARING TO SEND LIVE TRANSACTIONS 

To go live, verify that you have done the following:

- Changed the ICSClient.props file in the properties directory to
  match your merchant ID, certificate, and private key. Keep in mind
  that merchant ID is your merchant ID with CyberSource, and it is not
  related to any payment processor merchant ID. This is the ID
  assigned to you by CyberSource. You use it to generate your key
  pairs.

- Successfully tested your application against the ICS2 test server at
  the following URL:

  http://ics2test.ic3.com/

- Changed the ICSClient.props file to match the URL of the production
  server:

  http://ics2.ic3.com/

If you have questions or require assistance, send e-mail to

  ics_support@cybersource.com

JAVA SDK FAQ

QUESTION:
My Java class cannot load the key or certificate files. Why?

ANSWER:
The answer depends on which of the ICSClient constructors you are using. 

- The default constructor (which is deprecated) for the ICSClient()
   object takes no parameters.  The constructor searches the contents
   of the ../properties/ICSClient.props file (where ../ is the root
   directory from which you run the JVM). In order for the ICSClient()
   constructor to function correctly, your
   ../properties/ICSClient.props file must contain the following
   properties:

   serverName=CyberSource_SJC_US
   merchantID=your-assigned-merchant-id
   myPrivateKey=C:/<install-path>/keys/my-assigned-merchant-id.pvt
   myCert=C:/<install-path>/keys/my-assigned-merchant-id.crt
   serverURL=http://ics2test.ic3.com:80/ (or for live 
   transactions, http://ics2.ic3.com:80/)

- The required entries in the ../properites/ICSClient.props file when
   using the ICSClient (String)contructor are as follows:

   ICSClient client = new ICSClient ("../properties/ICSClient.props");

   The filename shown in the example above can be absolute or
   relative to the directory from which your JVM is run.


- The required entries for the 
  ICSClient(String,String,String,String,String,URL) constructor are 
  listed below. The filenames in can be either absolute or relative 
  to the directory from which your JVM is run.

	String "myPrivateKey",
	Indicates your merchant ID.

	String "../keys/my-assigned-merchant-id.pvt",
	The file that contains your private key (the .pvt file generated by ecert).

	String "../keys/my-assigned-merchant-id.crt",
	The file that contains your certificate (the .crt file generated by ecert).

	String "CyberSource_SJC_US",
	A required CyberSource ID, entered exactly as shown.

	String "../keys/CyberSource_SJC_US.crt",
	The CyberSource certificate.

	URL("http://ics2test.ic3.com:80"));
	This is the URL of the CyberSource server. 
	Enter "http://ics2test.ic3.com" to send test transactions or 
	"http://ics2.ic3.com" to send live transactions.

QUESTION:
My system behaves strangely after I call the ICSClient
constructor. Why?

ANSWER: 
ICSClient constructors in previous versions of the Java SDK (Java CDK)
overwrite your System.properties environment. This problem is fixed in
version 3.3.0.0 of the Java CDK.

QUESTION:
I am behind a firewall. How do I use the ICS Java SDK with proxies?

ANSWER:
The Java SDK uses built-in functions within the JVM client to make
HTTP connections.  Therefore, the Java SDK uses the standard JVM
methods for handling proxies.

To set a regular HTTP proxy, include code that is similar to the
following:

	System.getProperties().put("proxySet","true");	
	System.getProperties().put("proxyHost","proxy.cybersource.com");
	System.getProperties().put("proxyPort","80");

To set a SOCKS proxy, include code that is similar to the following:

	System.getProperties().put("proxySet","true");
	System.getProperties().put("socksProxyHost","socks.cybersource.com");
	System.getProperites().put("socksProxyPort","1080");

If you use the Microsoft JVM, include code that is similar to the
following:

	propSystem.put("firewallSet", "true");
	propSystem.put("firewallHost", "yourProxyServer.com");
	propSystem.put("firewallPort", "80");
	propSystem.put("http.proxyHost", "yourProxyServer.com");
	propSystem.put("http.proxyPort", "80");

The Java SDK uses the URL class to make a socket connection. If you
need to specify an HTTP proxy that requires username and password
authorization, you must use the ICSClientRequest class. Include code
before an ICSClient.send() call that is similar to the following:

	request.setHTTPProxyUsername("yourProxyUserName");
	request.setHTTPProxyPassword("yourProxyPassword");

The ICSClientRequest method only provides support for basic proxy
authorization.  Passwords sent by this method are sent in unencrypted
format to the proxy server.  For a more secure method for
authorization, consider using a SOCKS proxy instead.

QUESTION:
When I attempt to run multiple threads, I encounter problems. What can
I do to ensure that the Java SDK runs well under thread methods?

ANSWER:
The Java SDK supports many threads. It was tested extensively with
more than ten threads on Solaris with JDK 1.2.

In previous versions of the SDK, you had to destroy the ICSClient
object and instantiate a new one to use a different merchant ID.
Starting with version 3.7.1, you need only one ICSClient instance,
and you specify the merchant ID in the request itself.

QUESTION:
I received an error similar to the following:

java.util.MissingResourceException: Can't find resource for bundle
java.text.resources.LocaleElements_en_US, key Eras
	at java.util.ResourceBundle.getObject(ResourceBundle.java:322)
	at java.text.DateFormatSymbols.initializeData(DateFormatSymbols.java:458)
	at java.text.DateFormatSymbols.(DateFormatSymbols.java:109)
	at java.text.SimpleDateFormat.(SimpleDateFormat.java:286)
	at java.util.Date.toString(Date.java:969)
	at Worker.run(Worker.java:22)

What does this error message mean?

ANSWER:

For a description of the problem associated with this message, refer
to the following URL:

	http://developer.java.sun.com/developer/bugParade/bugs/4261469.html

The problem is resolved with JVM versions after 1.2.2.


-------------------------------------------------------------------------------
DOCUMENTATION
-------------------------------------------------------------------------------

For more information about installing and using this software package, see the
accompanying documentation.

Business Center users:

For information about how to use the various payment services, see the
Business Center Simple Order API User's Guide, available in the documentation
area of the Business Center.

Enteprise Business Center users:

For information about how to use a specific ICS service, see the Implementation
Guide for that service. The Implementation Guides are available on the Support
Center at:

http://www.cybersource.com/support_center/support_documentation/services_documentation/
