#CyberSource Simple Order API for Java



##Requirements


Java SDK 1.6 and later<br>
Maven 3 and later
	
##Prerequisites


A CyberSource Evaluation account. Sign up here:  <http://www.cybersource.com/register>

* Complete your Evaluation account creation by following the instructions in the Registration email

Transaction Security Keys

* Create security keys in the Enterprise Business Center (ebctest) after you've created your Merchant Admin account. 
Refer to our Developer's Guide for details <http://www.cybersource.com/developers/integration_methods/simple_order_and_soap_toolkit_api/soap_api/html>


##Installing the SDK 


1. Download the sdk-java-master.zip package into a directory of your choice. 

2. Extract and go to the sdk-java-master directory.

4. To run the tests, edit the test_cybs.properties and make the following changes:

    a. Set merchantID, keyAlias and keyPassword to your merchantID.  Please note that it is case-sensitive.
    
    b. Set keysDirectory to the directory where your key resides.  Use forward-slashes for the directory separator, even on Windows.
	   For example, "c:/keys"
	   
	(Optional Additional Changes)
    c. Set targetAPIVersion to the latest version displayed at: https://<cybersource-host>/commerce/1.x/transactionProcessor/
	   By default, it is set to the latest version when the package was created.
	   	
    d. Modify the logging properties as appropriate. Use forward-slashes for the directory separator in the logDirectory value, even on Windows. The directory you specify must already exist.
	   
    e. Please refer to the accompanying documentation for the other optional properties that you may wish to specify.
	   
	NOTE:  sendToProduction is initially set to false.  Set it to true only
	       when you are ready to send live transactions.
	
5. Build this project using Maven 

        a. mvn clean  // Cleans the Project
        
        b. mvn install 
           // Builds the project and creates a jar file of client SDk
           // Includes running all unit test cases 

  

##Documentation

For more information about CyberSource services, see <http://www.cybersource.com/developers/documentation>

For all other support needs, see <http://www.cybersource.com/support>



