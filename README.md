# CyberSource Simple Order API for Java

[![Build Status](https://travis-ci.org/CyberSource/cybersource-sdk-java.png?branch=master)](https://travis-ci.org/CyberSource/cybersource-sdk-java)

## Package Managers

### Maven
To install the `cybersource-sdk-java` from central repository, add dependency to your application `pom.xml` as below.
```xml
<dependency>
  <groupId>com.cybersource</groupId>
  <artifactId>cybersource-sdk-java</artifactId>
  <version>6.2.13</version>
</dependency> 
```
Run `mvn install` to install dependency

### Grails/Gradle
Add the dependency to your build.gradle
```java
dependencies {
  compile 'com.cybersource:cybersource-sdk-java:6.2.13'
}
```
## Requirements
- Java SDK 1.6 and later  
- Maven 3 and later  
- It is recommended to use Unlimited Strength Jurisdiction Policy files from Oracle® (US_export_policy.jar and local_policy.jar) for appropriate JAVA version. For JAVA 7, it is available at http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html

## Prerequisites
### CyberSource Evaluation account
Sign up here:  <http://www.cybersource.com/register/>
- Complete your Evaluation account creation by following the instructions in the Registration email

### Transaction Security Keys
Create security keys in the [Enterprise Business Center](https://ebctest.cybersource.com/) after you've created your Merchant Admin account. 
- Refer to our [Developer Guide](http://apps.cybersource.com/library/documentation/dev_guides/security_keys/html/wwhelp/wwhimpl/js/html/wwhelp.htm#href=securityKeys_SO_API.4.1.html) for details.

### JCE Unlimited Strength Jars
Replace your Java installation’s existing security policy files with the new ones you downloaded from the Oracle site:
- Locate your existing US_export_policy.jar and local_policy.jar files in the $JAVA_HOME/jre/lib/security directory.
- Rename or move your existing files to another directory.
- Copy the new US_export_policy.jar and local_policy.jar that you downloaded from Oracle to the $JAVA_HOME/jre/lib/security directory.  
## Installing the SDK 
You do not need to download and build the source to use the SDK but if you want to do that, follow these steps:
- Clone this repository.
- Go to the `cybersource-sdk-java-master` directory.
- To run the integration tests, edit the [test_cybs.properties](java/src/test/resources/test_cybs.properties) and make the following changes:
  - Set `merchantID`, `keyAlias` and `keyPassword` to your merchantID.  Please note that it is case-sensitive.
  - Set `keysDirectory` to the directory where your key resides.  Use forward-slashes for the directory separator, even on Windows. For example, "c:/keys"
  - Uncomment & set KeyFilename if you want to use JKS file or if the p12 file name is different from Merchant_ID
  - Optional Additional Changes
    - Set `targetAPIVersion` to the latest version displayed at: https://ics2ws.ic3.com/commerce/1.x/transactionProcessor/. By default, it is set to the latest version when the package was created.
    - Modify the logging properties as appropriate. Use forward-slashes for the directory separator in the `logDirectory` value, even on Windows. The directory you specify must already exist.
    - Set `useSignAndEncrypted` to true, to encrypt the signed Payload.
      - For more information about MLE, See [Message Level Encryption](README.md#message-level-encryption).
    - Set `sendToProduction` config parameter with toggle value "true/false" to send requests to Cybersource production/testing environment.
      - `sendToProduction` is initially set to false. Set it to true only when you are ready to send live transactions.
    - Set `sendToAkamai` config parameter with toggle value "true/false" to turn on/off routing requests through Akamai to Cybersource. By default, it is set to true.
    - `serverURL` config parameter will take precedence over `sendToProduction` and `sendToAkamai` config parameters. By default the `serverURL` configuration is commented out.
    - If `enableJdkcert` parameter is set to true, certificates will be read from the JKS file specified at keysDirectory location. The JKS file should be of the same name as specified in keyFilename.
        - To know how to convert p12 to JKS refer the JKS creation section of this document.
    - If 'enableCacert' property parameter is set to true, certificates will be read from the cacerts file specified at keysDirectory location.
        - If keysDirectory path is not set,certificate will be loaded from Java Installation cacerts file. The cacerts file should be of the same name as specified in keyFilename.
    - If `certificateCacheEnabled` parameter is set to false (default is true), the p12 certificate of a merchant will be reloaded from filesystem every time a transaction is made 
    - If `useHttpClient` parameter is set to true (default is false), then simple HttpClientConnection will be enabled
    - If `useHttpClientWithConnectionPool` parameter is set to true (default is false), then poolingHttpClientConnection will be enabled. In case of poolingHttpConnection, 
      we are initializing connection manager and httpclient once, If any change in value in between the application is running, it will not reflect. need to restart it. 
    - Below properties are specific to poolinghttpclient connection, If it is not added in properties file, it will throw config exception.
      
      Note : Sample values used in properties files are based on our testing application factors such as TPS, CPU, JVM, OS etc.
      Before using these values in actual real time application, please consider all real time factors.
         - `maxConnections` Specifies the maximum number of concurrent, active HTTP connections allowed by the resource instance to be opened with the target service. 
            There is no default value. For applications that create many long-lived connections, increase the value of this parameter.
         - `defaultMaxConnectionsPerRoute` the maximum number of connections per (any) route.
         - `maxConnectionsPerRoute` Specifies the maximum number of concurrent, active HTTP connections allowed by the resource instance to the same host or route. 
            In SDK, all above config does same functionality and the same value can be given to these configs as we have only one route. 
            
            Note: This number cannot be greater than Maximum Total Connections and every connection created here also counts into Maximum Total Connections.
         - `connectionRequestTimeoutMs` Time taken in milliseconds to get connection request from the pool. If it times out, it will throw error as Timeout waiting for connection from pool
         - `connectionTimeoutMs` Specifies the number of milliseconds to wait while a connection is being established. With 6.2.11 release onwards, this property can be used for basic 
            apache http client and JDK provided HttpUrlConnection implementation as well while keeping the backward compatibility with 'timeout' property.
         - `socketTimeoutMs` Specifies the time waiting for data – after establishing the connection; maximum time of inactivity between two data packets. 
            With 6.2.11 release onwards, this property can be used for basic apache http client and JDK provided HttpUrlConnection implementation as well while keeping the 
            backward compatibility with 'timeout' property.
         - `evictThreadSleepTimeMs` Specifies time duration in milliseconds between "sweeps" by the "idle connection" evictor thread. 
            This thread will check if any idle/expired/stale connections are available in pool and evict it.
         - `maxKeepAliveTimeMs` Specifies the time duration in milliseconds that a connection can be idle before it is evicted from the pool.
         - `staleConnectionCheckEnabled` It determines whether the stale connection check is to be used. Disabling the stale connection check can result in slight performance improvement 
            at the risk of getting an I/O error, when executing a request over a connection that has been closed at the server side. By default it is set to true, which means it is enabled.
         - `validateAfterInactivityMs` By default it is set to 0. This value can be set if in case you decide to disable staleConnectionCheckEnabled to get slight better performance. 
            We recommended a value of 2000ms. 
         - `enabledShutdownHook` We should close the connection manager, http client and idle connection cleaner thread when application get shutdown both abruptly and gracefully.
            If `enabledShutdownHook` is true, then JVM runtime addShutdownHook method will be initialized. Shutdown Hooks are a special construct that allows developers to plug in a piece of 
            code to be executed when the JVM is shutting down. This comes in handy in cases where we need to do special clean-up operations in case the VM is shutting down.
                `    private void addShutdownHook() {
                      Runtime.getRuntime().addShutdownHook(this.createShutdownHookThread());
                    }`
            createShutdownHookThread method will call static shutdown api to close connectionManager, httpClient and IdleCleanerThread. By default this is enabled when useHttpClientWithConnectionPool is true.
    - `allowRetry` config parameter will only work for HttpClient and PoolingHttpClient. 
       Set `allowRetry` config parameter to "true" to enable retry mechanism and set merchant specific values for the retry.
       - Set integer values and long values for config parameter `numberOfRetries` *and* `retryInterval` respectively. Retry Interval is time delay for next retry in milliSeconds.
          - Number of retry parameter should be set between 1 to 5. By default the value for numberOfRetries will be 3. Any other value will throw an Error Message.
      - Refer to the [Retry Pattern](README.md#retry-pattern) section below.
    - Please refer to the accompanying documentation for the other optional properties that you may wish to specify.
    - Set customHttpClassEnabled to true to make use of Custom Http Library. 
      - Enter the custom class name in customHttpClass field. Provide the full package name along with the class name.
        example customHttpClass= <packagename.customHttpClass>
      - The custom HTTP Class must have a three argument constructor which accepts MerchantConfig, DocumentBuilder and LoggerWrapper as argument. Then it should call the constructor of the parent class.
    - `merchantConfigCacheEnabled` If this property is set to true (default value is false) it will cache the merchantConfig object based on keyAlias/merchantID
     -If cache enabled is true, for single merchant id, if you change any properties after first initialization, it will not reflect.
- Build this project using Maven.
  - `mvn clean` - Cleans the Project
  - `mvn install` - Builds the project and creates a jar file of client SDK. Includes running all unit tests and integration tests
  - `mvn test` - Runs unit tests
  - `mvn failsafe:integration-test` - Runs unit and integration tests. Note that integration tests require proper setup of `test_cybs.properties`

## Testing the SDK 
We have two ways to test -- one is by downloading the zip and using scripts to test; other is using maven tool.
### Using Scripts
- Unzip the downloaded zip file into a directory of your choice.  It will create a directory called `cybersource-sdk-java-master`. 
  - If in the Request, a key called "_has_escapes" is present and is set to "1", we will not escape the special characters. This is a way for the merchant to specify that they have escaped the characters themselves. This might prove useful for more advanced users of the Basic client.
    - Note: The Script will take Service_name as program argument. Service Name can be auth, auth_reversal, capture, sale, emv_auth, credit. If no argument is passed the script will terminate the program.
- Choosing which sample to test:
  - If you want to test Name-Value Pair, go to the `cybersource-sdk-java-master/samples/nvp` directory.
  - If you want to test XML, go to the `cybersource-sdk-java-master/samples/xml` directory.
- Use `compileSample` scripts to create classes directory as it is not included in SDK.
- Then at a command prompt, type this line:
```
Windows:	runSample.bat <service_name>
Unix or Linux:	runSample.sh <service_name>
```
  - If JAVA_HOME is defined, the script uses <JAVA_HOME>/bin/java. Otherwise, it uses whatever java is in the path.
  - If the client is installed correctly, the requests and replies for a credit card authorization and a follow-on capture appear.
- If you make any changes to the `RunSample.java` sample, you must rebuild the sample before using it. Use the `compileSample` batch file or shell script provided in the sample directory.

### Using samples and maven tool

- Clone/Download the code from GitHub.
- Choosing which sample to test:
  - If you want to test Name-Value Pair, `cd` to the `cybersource-sdk-java-master/samples/nvp` directory.
  - If you want to test XML, `cd` to the `cybersource-sdk-java-master/samples/xml` directory.
    - Sample projects `nvp` and `xml` use `cybersource-sdk-java` and [3rd party jars](README.md#third-party-jars) as dependent jar files.
- Build this project using Maven. `mvn install`
- If build is successful then it will put all jars inside `cybersource-sdk-java/samples/nvp/target/dependencies` folder.
- Edit `cybs.properties` and make the required changes.
- Now use scripts to test.

## JKS creation

- To convert the p12 file to JKS follow the steps mentioned below.
  - These commands will take out all the certs from the p12 file. 
  1. `openssl pkcs12 -in <Merchant_ID>.p12 -nocerts -out <Merchant_ID>.key`
  2. `openssl pkcs12 -in <Merchant_ID>.p12 -clcerts -nokeys -out  <Merchant_ID>.crt`
  3. `openssl pkcs12 -in <Merchant_ID>.p12 -cacerts -nokeys -out CyberSourceCertAuth.crt`
  4. `openssl pkcs12 -in <Merchant_ID>.p12 -cacerts -nokeys -out CyberSource_SJC_US.crt`

- Create a new p12. Here Identity.p12 is the new p12 file
```
openssl pkcs12 -export -certfile CyberSourceCertAuth.crt -in <Merchant_ID>.crt -inkey <Merchant_ID>.key -out identity.p12 -name "<Merchant_ID>"
```

- Create JKS from p12 using keytool
```
keytool -importkeystore -destkeystore <Your_keystore_name>.jks -deststorepass <your_password> -srckeystore identity.p12 -srcstoretype PKCS12 -srcstorepass <Merchant_ID>
```
- Now import the CyberSource_SJC_US.crt to your keystore
```
keytool -importcert -trustcacerts -file CyberSource_SJC_US.crt -alias CyberSource_SJC_US -keystore <Your_keystore_name>.jks
```
- List the entries of your keystore
```
keytool -list -v -keystore <Your_keystore_name>`
```
- It should have two entries.
  - The first entry should contain a chain of two certificates - `CyberSourceCertAuth` and <Merchant_ID> with alias name <Merchant_ID>
  - Second entry should be for `CyberSource_SJC_US` certificate with alias name as CyberSource_SJC_US
  
## PoolingHttpClient
   PoolingHttpClient is built using the apache's PoolingHttpClientConnectionManager class. It comes with retry functionality which is very much needed in case if
   SDK receives an I/O error/exception, when executing a request over a connection that has been closed at the server side. However there might be some cases when
   transaction has reached server and similar or some other exception has occurred. We are considering `merchantTransactionIdentifier` as idempotent key, specially 
   in case of auth service(`ccAuthService`). Hence if you want to use PoolingHttpClient, for auth service(`ccAuthService`) merchantTransactionIdentifier field is 
   mandatory in the payload for both nvp and xml. The value of the merchant transaction ID must be unique for 60 days.
   
   To get more information related to connection pooling please refer wiki.
   
## Message Level Encryption
CyberSource supports Message Level Encryption (MLE) for Simple Order API. Message level encryption conforms to the SOAP Security 1.0 specification published by the OASIS standards group. 

## Meta Key support
Meta Key is a key generated by an entity that can be used to authenticate on behalf of other entities provided that the entity which holds key is a parent entity or associated as a partner.

SOAPI Java SDK supports meta key by default. Additional details regarding cybs.properties.

    merchantID=  <comment/remove this line> 
    keysDirectory=<Directory where P12 is present>
    keyAlias=<Refers to the portfolio>
    keyPassword=<Password of p12>
    targetAPIVersion=<latest API version, refer here https://ics2ws.ic3.com/commerce/1.x/transactionProcessor>
    keyFilename= <metakey downloaded from portfolio MID>
 
 Auth sample payload:
  
    merchantID=<meta_2232323> <Refers to the Child transactional MID>
    ccAuthService_run=true
    merchantReferenceCode=MRC-14344 
    billTo_firstName=John

### Authentication Details
    Message level encryption authenticates using the same mechanism as signed SOAP messages. The signature creation involves utilizing the merchants private key which combined with a hash of the message to be signed, can be validated with the merchants certificate and the message which was signed. 
    The merchant certificate is included in the SOAP message for both signature and message level encryption. Message level encryption, encrypts a temporary message key for a specific recipient. This is done by encrypting the temporary message key with the recipient’s public certificate. Therefore only the party holding the private key (CyberSource) can decrypt the temporary message key. The merchant sending the request must be a valid merchant for the environment which the message is being processed in. After validating the merchant and retrieving the CyberSource copy of the merchant certificate from our database, these additional authentication steps are performed:
    1.	The certificate sent in the message must have valid trust chain with the CyberSource certificate authority as the root signer.
    2.	A certificate belonging to the merchant which sent the message must exist within our database, having the exact serial number of the certificate provided. 
    3.	Our record of the certificate must have a valid start and end date for the transaction time sent.
    4.	Our record of the certificate must have a “active” state (ie. Not deactivated by support).
    5.	If merchant is reseller, the merchant must allow reseller to act upon their behalf and reseller must be configured as a reseller and the provided merchant must be configured as a merchant of this reseller. Additionally all above authorizations apply.

### Cryptography Algorithms
    CyberSource utilizes the following algorithms for this implementation. While others may work, the following are validated and recommended. SSL is used for transport security even with encrypted messages. CyberSource asymmetric keys are RSA 2048 keys and therefore your cryptography API should support 2048 bit RSA keys and signatures create with them. The messages are encrypted with a temporary derived key which is used per message. This derived key is AES 256 bit and utilizes CBC blocking mode for encryption. The derived key is encrypted with the recipient (CyberSource) public key. The key exchange algorithm used is RSA-OAEP.

## Retry Pattern

Retry Pattern allows to retry sending a failed request and it will only work with `useHttpClient=true` or `useHttpClientWithConnectionPool. `allowRetry` flag enables the retry mechanism. 
  - Set the value of `allowRetry` parameter to "TRUE/FALSE". Then the system will retry the failed request as many times as configured by the merchant in the config parameter 'numberOfRetries'.
  - numberOfRetries parameter value should be set between 0 to 5. By default the value for numberOfRetries will be 3. User can set a delay in between the retry attempts.
  - Config parameter for this property is 'retryInterval' in `cybs.property` file. The default value for 'retryInterval' parameter is 1000 which means a delay of 1000 milliSeconds.

## Third Party jars
    1. org.apache.wss4j:wss4j-ws-security-common:2.4.1
      The Apache WSS4J project provides a Java implementation of the common primary security standards for Web Services, namely the OASIS Web Services Security (WS-Security) specifications from the OASIS Web Services Security TC.
    2. org.apache.wss4j:wss4j-ws-security-dom:2.4.1
      WSS4J 2.0.0 introduces a streaming (StAX-based) WS-Security implementation to complement the existing DOM-based implementation. The DOM-based implementation is quite performant and flexible, but suffers from having to read the entire XML tree into memory. For large SOAP requests this can have a detrimental impact on performance. In addition, for web services stacks such as Apache CXF which are streaming-based, it carries an additional performance penalty of having to explicitly convert the request stream to a DOM Element.
    3. org.bouncycastle:bcprov-jdk15on:1.70
      This jar contains JCE provider and lightweight API for the Bouncy Castle Cryptography APIs for JDK 1.5 to JDK 1.8.
    4. org.apache.santuario:xmlsec:2.3.0
      The XML Security project is aimed at providing implementation of security standards for XML,supports XML-Signature Syntax and Processing,XML Encryption Syntax and Processing, and supports XML Digital Signature APIs.
    5. org.apache.commons:commons-lang3:3.4
      Apache Commons Lang, a package of Java utility classes for the classes that are in java.lang's hierarchy, or are considered to be so standard as to justify existence in java.lang.
    6. commons-logging:commons-logging:jar:1.1.1
      This is getting downloaded as compile time dependency of wss4j:1.6.19.Apache Commons Logging is a thin adapter allowing configurable bridging to other, well known logging systems.
    7. org.slf4j:slf4j-api:1.7.32 and org.slf4j:slf4j-jcl:1.7.32
      slf4j-api is getting used as a dependency for wss4j. Modified to latest version.
    8. junit:junit:4.13.1
      JUnit is a unit testing framework for Java.
    9. org.mockito:mockito-all:1.10.19
      Mock objects library for java  
    10. org.apache.httpcomponents:httpclient:4.5.13
       Provides reusable components for client-side authentication, HTTP state management, and HTTP connection management. It is used for poolinghttpclientconnectionmanager feature.
    11. org.apache.httpcomponents:httpcore:4.4.13
       Provides low level HTTP transport components that can be used to build custom client and server side HTTP services with a minimal footprint.

## Changes
_______________________________
Version Cybersource-sdk-java 6.2.13 (AUGUST,2022)
_______________________________
    1)Modified the CYBS P12 certificate's CN name verification to case insensitive.
_______________________________

_______________________________    
Version Cybersource-sdk-java 6.2.12 (JULY,2022)
_______________________________
    1) Mitigation of Apache WSS4j Security Vulnerability (CVE-2016-1000343, CVE-2018-1000180).
       i) Updated Apache wss4j version from 1.6.19 to 2.4.1
       ii) Updated dependent libraries version. (xmlsec from 1.5.6 to 2.3.0, bcprov-jdk15on from 1.61 to 1.70)
_______________________________
_______________________________
Version Cybersource-sdk-java 6.2.11 (MAY,2020)
_______________________________
    1)Exception handling improvement.
    2)Upgrading Apache's basic http client functionality.
    3)Upgrading org.apache.httpcomponents:httpclient:4.5.11 to org.apache.httpcomponents:httpclient:4.5.13 because of CVE-2020-13956 vulnerability.
    4)ReadMe changes for meta key support.
    5)Http request retry is added in case of HttpPoolingClient when 'javax.net.ssl.SSLException:Connection reset' exception is thrown(specific to jdk8u251 & + version refer this https://bugs.openjdk.java.net/browse/JDK-8214339)
    6)Separate out connection and socket timeout prop. Right now both are set via timeout property in case of jdk HttpUrlConnectiona and Apache basic http client.
_______________________________
Version Cybersource-sdk-java 6.2.10 (MAY,2020)
_______________________________
    1)Added PoolingHttpClientConnection implementation
    2)MerchantConfig Object Caching based on KeyAlias/Merchant Id
    3)Changed retry interval from second to millisecond
    4)Added one more request header "v-c-client-computetime" to calculate time taken to send request to Cybersource
    5)Added troubleshooting section in README.
_______________________________
Version Cybersource-sdk-java 6.2.9 (APR,2020)
_______________________________
    1)Corrected request header name
_______________________________  
Version Cybersource-sdk-java 6.2.8 (FEB,2020)
_______________________________
    1)Added request header and logged request and response headers
    
    2)Caching of certificate is done using keyAlias earlier it was done using merchant_id
_______________________________
Version Cybersource-sdk-java 6.2.7 (MAR,2019)
_______________________________
    1) Fixed security vulnerabilities found in the jar dependencies.
        xmlsec jar :-upgraded from version 1.4.3 to version 1.5.6
        opensaml jar :- Removed this jar as its not impacting our code base
        bcprov jar :- upgraded from version 1.54 to version 1.61
    2) Fixed keyfile password issue. Now using keyfile password to store/load p12 certs. 
_______________________________
Version Cybersource-sdk-java 6.2.6 (MAY,2018)
_______________________________
    1) Added certificateCacheEnabled optional feature. certificateCacheEnabled parameter is set to false (default is true), the p12 certificate of a merchant will be reloaded from filesystem every time a transaction is made.If the certificateCacheEnabled is true then only at the first time certificate of a merchant will loaded from filesystem.
    2) Introduced a new feature to check merchant .p12 certificate file validity at run time. If it is replaced at runtime then SDK will reload the new certificate into the cache.
    3) Changed clientLibrary version to 6.2.6;
_______________________________  
Version Cybersource-sdk-java 6.2.5 (OCT,2017)
_______________________________
    1) Merchant cert to be read from JAVA key store. Flag is added to enable reading cert from Java keystore.
    2) Added Custom HttpClient feature. Merchants can use there own http client instead of defaults which comes with SDK.
    3) Http Client connection reuse issue.
    4) Changed clientLibrary version to 6.2.5; in 6.2.4 release it was missed. So, in 6.2.4 release, clientLibrary version was      pointing to 6.2.3.
_______________________________  
Version Cybersource-sdk-java 6.2.4 (Dec 15, 2016)
_______________________________
    1) RetryPattern config for http client.
    2) Code review comments.
    3) Added timers to log the method execution time.
    4) Sample added to support other services.
_______________________________
Version Cybersource-sdk-java 6.2.3 (Oct 17, 2016)
_______________________________
    1) Fixed performance issue; in case of multiple merchantIDs, p12 was getting loaded for every request.
    2) p12 will be loaded once per merchantId.
_______________________________
Version Cybersource-sdk-java 6.2.2 (Sep 15, 2016)
_______________________________
    1)Upgraded 3rd party dependencies jars including wss4j.
_______________________________
Version Cybersource-sdk-java 6.2.1 (Aug 4, 2016)
_______________________________
    1) AkamaiSureroute config parameter introduced
    2) i18n fix for NVP sample.            
    3) In `Sample/cybs.properties` file, `targetAPIVersion` changed to latest 1.129.
_______________________________
Version Cybersource-sdk-java 6.2.0 (Jul 28, 2016)
_______________________________
    1) MLE[Message Level Encryption] is enabled.
    2) published zip file with samples and packaged compiled cybersoruce-sdk-java jar file.
    3) `Bouncycastle` jar issue; changed scope from provided to default"scope"
_______________________________
Version Cybersource-sdk-java 6.1.0 (Feb 24,2016)
_______________________________
    1) SHA256 changes which are required to signed the request with SHA256.

## Troubleshooting
- If you get an exception **`java.lang.SecurityException: JCE cannot authenticate the provider BC`**. This could be because of
  many reasons. bcprov*.jar is a signed jar if java fails to validate the signature, it throws this exception. Make sure
  you run below java command to verify this signature.
   
    `jarsigner -verify bcprov-jdk15on-1.61.jar` 
    
  when above command fails it says "jar is unsigned. (signatures missing or not parsable)", this could be because of many
  reasons. e.g 
  
        1) When we unpack it and include in our own jar file. Including bcprov*.jar separately in the CLASSPATH should solve this issue.
        2) May be changes in Oracle jar signer. If using Java SDK 1.6 or 1.7 with cybersource-sdk-java:6.2.7 and higher 
           (ships with org.bouncycastle:bcprov-jdk15on:1.61). Upgrading version to bcprov-jdk15to18-1.63.jar should solve this issue.
        3) If you are using some old version of JBOSS and have copied bcprov*.jar under $JBOSS_HOME/server/default/lib/. 
            copying bcprov*.jar in $JBOSS_HOME/server/default/lib/ instead of $JBOSS_HOME/server/servername/lib/ should solve this issue.
            
- If you get an exception  **`exception decrypting data - java.security.InvalidKeyException: Illegal key size`**. 
  It is recommended to download Unlimited Strength Jurisdiction Policy files from Oracle (US_export_policy.jar and local_policy.jar) 
  for appropriate JAVA version. I meant if merchant are using java 6 then download these policy file only for java6. 
  You need to copy security jars (US_export_policy.jar, local_policy.jar) in the $JAVA_HOME/jre/lib/security directory not in $JAVA_HOME/jre/lib/ext/).

- Put below block of code to handle the ClientException to print the complete stacktrace.

        try{
            Client.runTransaction(requestMap, merchantProperties);
        }catch (ClientException e){
            e.getInnerException().printStackTrace();
            // or 
            String stackTrace = Utility.getStackTrace(e.getInnerException() != null? e.getInnerException(): e);      
        }
      
## Documentation
- For more information about CyberSource services, see <https://www.cybersource.com/en-us/support/technical-documentation.html>.
- For all other support needs, see <https://support.cybersource.com/>.


