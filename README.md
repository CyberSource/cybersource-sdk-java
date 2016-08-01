#CyberSource Simple Order API for Java

[![Build Status](https://travis-ci.org/CyberSource/cybersource-sdk-java.png?branch=future)]
(https://travis-ci.org/CyberSource/cybersource-sdk-java)

##Package Managers

### Maven
To install the cybersource-sdk-java from central repository,add dependency to your application pom.xml as below.
````
        <dependency>
            <groupId>com.cybersource</groupId>
            <artifactId>cybersource-sdk-java</artifactId>
            <version>6.2.1</version>
        </dependency> 
````
 Run mvn install, to install dependency

### Grails/Gradle
Add the dependency to your build.gradle
````
dependencies {
    compile 'com.cybersource:cybersource-sdk-java:6.2.1'
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
	   	
    d. Modify the logging properties as appropriate. Use forward-slashes for the directory separator in the logDirectory value, even on Windows. The directory you specify must already exist.
	   
    e. Set useSignAndEncrypted to true,Mechanism to encrypt the signed Payload.For more information about MLE, see Message Level Encryption at bottom.
	
    f. Set "sendToAkamai" boolean flag with toggle value "true/false" so that the merchant can turn on/off routing transactions to Akamai.By default, it is set to false. 
       "serverURL will be used, if if is mentioned.
	
    g. Please refer to the accompanying documentation for the other optional properties that you may wish to specify.
	   
	NOTE:  sendToProduction is initially set to false.  Set it to true only
	       when you are ready to send live transactions.
	
4. Build this project using Maven.

        a. mvn clean  // Cleans the Project
        
        b. mvn install 
           // Builds the project and creates a jar file of client SDK
           // Includes running all unit tests and integration tests

        c. mvn test
           // Runs unit tests

        d. mvn failsafe:integration-test
           // Runs unit and integration tests. Note that integration tests require proper setup of test_cybs.properties

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
		CyberSource utilizes the following algorithms for this implementation. While others may work, the following are validated and recommended. SSL is used for transport security even with encrypted messages. CyberSource asymmetric keys are RSA 2048 keys and therefore your cryptography API should support 2048 bit RSA keys and signatures create with them. The messages are encrypted with a temporary derived key which is used per message. This derived key is AES 128 bit and utilizes CBC blocking mode for encryption. The derived key is encrypted with the recipient ( CyberSource ) public key. The key exchange algorithm used is RSA-OAEP.
  

##Documentation

For more information about CyberSource services, see <http://www.cybersource.com/developers/documentation>

For all other support needs, see <http://www.cybersource.com/support>





