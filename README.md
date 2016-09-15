#CyberSource Simple Order API for Java

[![Build Status](https://travis-ci.org/CyberSource/cybersource-sdk-java.png?branch=master)]
(https://travis-ci.org/CyberSource/cybersource-sdk-java)

##Package Managers

### Maven
To install the cybersource-sdk-java from central repository,add dependency to your application pom.xml as below.
````
        <dependency>
            <groupId>com.cybersource</groupId>
            <artifactId>cybersource-sdk-java</artifactId>
            <version>6.2.2</version>
        </dependency> 
````
 Run mvn install, to install dependency

### Grails/Gradle
Add the dependency to your build.gradle
````
dependencies {
    compile 'com.cybersource:cybersource-sdk-java:6.2.2'
    }
````
##Requirements


1. Java SDK 1.6 and later  
2. Maven 3 and later  
3. It is recommended to use Unlimited Strength Jurisdiction Policy files from Oracle® (US_export_policy.jar and local_policy.jar) for appropriate JAVA version. For JAVA 7, it is available at http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html
	
##Prerequisites


######A CyberSource Evaluation account. 

Sign up here:  <http://www.cybersource.com/register>

* Complete your Evaluation account creation by following the instructions in the Registration email

######Transaction Security Keys

* Create security keys in the Enterprise Business Center (ebctest) after you've created your Merchant Admin account. 
Refer to our Developer's Guide for details <http://apps.cybersource.com/library/documentation/dev_guides/security_keys/html/wwhelp/wwhimpl/js/html/wwhelp.htm#href=securityKeys_SO_API.4.2.html>.

######JCE Unlimited Strength Jars

  Replace your Java installation’s existing security policy files with the new ones you downloaded from the Oracle site:
  
* 	Locate your existing US_export_policy.jar and local_policy.jar files in the $JAVA_HOME/jre/lib/security directory.  
* 	Rename or move your existing files to another directory.  
* 	Copy the new US_export_policy.jar and local_policy.jar that you downloaded from Oracle to the $JAVA_HOME/jre/lib/security directory.  
	


##Installing the SDK 

You do not need to download and build the source to use the SDK but if you want to do that, follow these steps:

1. Clone this repository.

2. Go to the sdk-java-master directory.

3. To run the integration tests, edit the test_cybs.properties and make the following changes:

    a. Set merchantID, keyAlias and keyPassword to your merchantID.  Please note that it is case-sensitive.
    
    b. Set keysDirectory to the directory where your key resides.  Use forward-slashes for the directory separator, even on Windows.
	   For example, "c:/keys"
	   
	(Optional Additional Changes)
    c. Set targetAPIVersion to the latest version displayed at: https://<cybersource-host>/commerce/1.x/transactionProcessor/
       By default, it is set to the latest version when the package was created.
	   	
    d. Modify the logging properties as appropriate. Use forward-slashes for the directory separator in the logDirectory value, even on Windows. 
       The directory you specify must already exist.
	
    e. Set useSignAndEncrypted to true, Mechanism to encrypt the signed Payload.For more information about MLE, See Message Level Encryption at bottom.
	
    f. Set sendToProduction config parameter with toggle value "true/false"  to send requests to Cybersource production/testing environment. 
       sendToProduction is initially set to false. Set it to true only when you are ready to send live transactions.
    
    g. Set sendToAkamai config parameter with toggle value "true/false" to turn on/off routing requests through Akamai to Cybersource. 
       By default, it is set to true.
       
    h. "serverURL" config parameter will take precedence over sendToProduction and sendToAkamai config parameters. By default the "serverURL" configuration is commented out. 
        
    i. Please refer to the accompanying documentation for the other optional properties that you may wish to specify.

	
4. Build this project using Maven.

        a. mvn clean  // Cleans the Project
        
        b. mvn install 
           // Builds the project and creates a jar file of client SDK
           // Includes running all unit tests and integration tests

        c. mvn test
           // Runs unit tests

        d. mvn failsafe:integration-test
           // Runs unit and integration tests. Note that integration tests require proper setup of test_cybs.properties

##Testing the SDK 
		
We have two ways to test, One is using maven tool and other is to download the zip and use scripts to test.

1.) Unzip the downloaded zip file into a directory of your choice.  It will create a directory called 
    cybersource-sdk-java-master. 
	
	a. TESTING THE NAME-VALUE PAIR SAMPLE
		. Go to the cybersource-sdk-java-master/sample/nvp directory.
		. Use compileSample scripts to create classes directory.As it is not included in SDK.
		. Then at a command prompt, type this line:
			Windows 	runSample.bat
			Unix or Linux 	runSample.sh
				
			If JAVA_HOME is defined, the script uses <JAVA_HOME>/bin/java. Otherwise, it uses
			whatever java is in the path.
			If the client is installed correctly, the requests and replies for a credit card authorization
			and a follow-on capture appear.

		. If you make any changes to the AuthCaptureSample.java sample, you
		  must rebuild the sample before using it. Use the compileSample batch file or
	 	  shell script provided in the sample directory.
			   
	b. TESTING THE XML SAMPLE
		. Go to the cybersource-sdk-java-master/sample/xml directory.
		. Use compileSample scripts to create classes directory.As it is not included in SDK.
		. At a command prompt, type this line:
			Windows 	runSample.bat
			Unix or Linux 	runSample.sh
				
			If JAVA_HOME is defined, the script uses <JAVA_HOME>/bin/java. Otherwise, it uses
			whatever java is in the path.
			If the client is installed correctly, the requests and replies for a credit card authorization
			and a follow-on capture appear.

		. If you make any changes to the AuthSample.java sample, you
		  must rebuild the sample before using it. Use the compileSample batch file or
	          shell script provided in the sample directory.

2.) Using samples and maven tool. samples is present at the same directory level as java and zip.
	a.) Clone/Download the code from GitHub.
	
	b.) Go to the samples directory. then cd to nvp or xml which are present inside samples dir, choose any one and do     the same thing with other.
	
	c.) Sample project nvp and xml uses cybersource-sdk-java and 3rd party jars as dependent jar files. 
	
	d.) Build this project using Maven.
		mvn install
		
	e.) If build is successful then it will put all jars inside cybersource-sdk-java/samples/nvp/target/dependencies 
	    folder.
	
	f.) Edit cybs.properties and make the required changes:
	
	g.) Now use scripts to test.
	
##Message Level Encryption

	CyberSource supports Message Level Encryption (MLE) for Simple Order API. Message level encryption conforms to the SOAP Security 1.0 specification published by the OASIS standards group. 

	Authentication Details
		Message level encryption authenticates using the same mechanism as signed SOAP messages. The signature creation involves utilizing the merchants private key which combined with a hash of the message to be signed, can be validated with the merchants certificate and the message which was signed. 
		The merchant certificate is included in the SOAP message for both signature and message level encryption. Message level encryption, encrypts a temporary message key for a specific recipient. This is done by encrypting the temporary message key with the recipient’s public certificate. Therefore only the party holding the private key (CyberSource) can decrypt the temporary message key. The merchant sending the request must be a valid merchant for the environment which the message is being processed in. After validating the merchant and retrieving the CyberSource copy of the merchant certificate from our database, these additional authentication steps are performed;
		1.	The certificate sent in the message must have valid trust chain with the CyberSource certificate authority as the root signer.
		2.	A certificate belonging to the merchant which sent the message must exist within our database, having the exact serial number of the certificate provided. 
		3.	Our record of the certificate must have a valid start and end date for the transaction time sent.
		4.	Our record of the certificate must have a “active” state (ie. Not deactivated by support).
		5.	If merchant is reseller, the merchant must allow reseller to act upon their behalf and reseller must be configured as a reseller and the provided merchant must be configured as a merchant of this reseller. Additionally all above authorizations apply.

	Cryptography Algorithms
		CyberSource utilizes the following algorithms for this implementation. While others may work, the following are validated and recommended. SSL is used for transport security even with encrypted messages. CyberSource asymmetric keys are RSA 2048 keys and therefore your cryptography API should support 2048 bit RSA keys and signatures create with them. The messages are encrypted with a temporary derived key which is used per message. This derived key is AES 256 bit and utilizes CBC blocking mode for encryption. The derived key is encrypted with the recipient ( CyberSource ) public key. The key exchange algorithm used is RSA-OAEP.
  
##Third Party jars
	1.) org.apache.ws.security.wss4j:1.6.19
	    The Apache WSS4J project provides a Java implementation of the primary security standards for Web Services, namely the OASIS Web Services Security (WS-Security) specifications    
	    from the OASIS Web Services Security TC.
	    
	2.) org.bouncycastle:bcprov-jdk15on:1.54
		This jar contains JCE provider and lightweight API for the Bouncy Castle Cryptography APIs for JDK 1.5 to JDK 1.8.
	
	3.) org.apache.santuario:xmlsec:1.5.8
		The XML Security project is aimed at providing implementation of security standards for XML,supports XML-Signature Syntax and Processing,XML Encryption Syntax and Processing, 
		and supports XML Digital Signature APIs.
	
	4.) org.apache.commons:commons-lang3:3.4
		Apache Commons Lang, a package of Java utility classes for the classes that are in java.lang's hierarchy, or are considered to be so standard as to justify existence in  
		java.lang.
	
	5.) commons-httpclient:commons-httpclient:3.1
		provides a framework by which new request types (methods) or HTTP extensions can be created easily.
	
	6.) commons-logging:commons-logging:jar:1.1.1
		This is getting downloaded as compile time dependency of wss4j:1.6.19.Apache Commons Logging is a thin adapter allowing configurable bridging to other, well known logging 
		systems.
		
	7.) org.slf4j:slf4j-api:1.7.21 and org.slf4j:slf4j-jcl:1.7.21 . 
	    slf4j-api is getting used as a dependency for wss4j. Modified to latest version.
	
	8.) junit:junit:4.12
		JUnit is a unit testing framework for Java.
	
	9.) org.mockito:mockito-all:1.10.19
		Mock objects library for java  

##Documentation

For more information about CyberSource services, see <http://www.cybersource.com/developers/documentation>

For all other support needs, see <http://www.cybersource.com/support>





