/*
* Copyright 2003-2014 CyberSource Corporation
*
* THE SOFTWARE AND THE DOCUMENTATION ARE PROVIDED ON AN "AS IS" AND "AS
* AVAILABLE" BASIS WITH NO WARRANTY.  YOU AGREE THAT YOUR USE OF THE SOFTWARE AND THE
* DOCUMENTATION IS AT YOUR SOLE RISK AND YOU ARE SOLELY RESPONSIBLE FOR ANY DAMAGE TO YOUR
* COMPUTER SYSTEM OR OTHER DEVICE OR LOSS OF DATA THAT RESULTS FROM SUCH USE. TO THE FULLEST
* EXTENT PERMISSIBLE UNDER APPLICABLE LAW, CYBERSOURCE AND ITS AFFILIATES EXPRESSLY DISCLAIM ALL
* WARRANTIES OF ANY KIND, EXPRESS OR IMPLIED, WITH RESPECT TO THE SOFTWARE AND THE
* DOCUMENTATION, INCLUDING ALL WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE,
* SATISFACTORY QUALITY, ACCURACY, TITLE AND NON-INFRINGEMENT, AND ANY WARRANTIES THAT MAY ARISE
* OUT OF COURSE OF PERFORMANCE, COURSE OF DEALING OR USAGE OF TRADE.  NEITHER CYBERSOURCE NOR
* ITS AFFILIATES WARRANT THAT THE FUNCTIONS OR INFORMATION CONTAINED IN THE SOFTWARE OR THE
* DOCUMENTATION WILL MEET ANY REQUIREMENTS OR NEEDS YOU MAY HAVE, OR THAT THE SOFTWARE OR
* DOCUMENTATION WILL OPERATE ERROR FREE, OR THAT THE SOFTWARE OR DOCUMENTATION IS COMPATIBLE
* WITH ANY PARTICULAR OPERATING SYSTEM.
*/

package com.cybersource.ws.client;

import com.cybersource.ws.client.Client;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Junit Test case for validating XMLClient.java class.
 * This aimed at validating the Transaction where XML document is used  as the input for XMLClient.java
 * User: sunagara
 * 
 */
public class XmlClientTest {
    /**
     * validating RunTransaction method of XMLClient.java
     */
    @Test
    public void testRunTransaction() throws Exception {
      
        Properties merchantProperties = new Properties();
        merchantProperties.setProperty("merchantID", "jasoneatoncorp");
        merchantProperties.setProperty("keysDirectory", "src/test/resources");
        //merchantProperties.setProperty("keyAlias", "jasoneatoncorp");
        //merchantProperties.setProperty("keyPassword", "jasoneatoncorp");
        merchantProperties.setProperty("targetAPIVersion", "1.97");
        merchantProperties.setProperty("sendToProduction", "false");
        merchantProperties.setProperty("serverURL", "https://ics2wstest.ic3.com/commerce/1.x/transactionProcessor/");
       // merchantProperties.setProperty("serverURL", "http://mvqsstage002d.qa.intra:11080/commerce/1.x/transactionProcessor/");
        merchantProperties.setProperty("timeout", "10000");
        merchantProperties.setProperty("enableLog", "true");
        merchantProperties.setProperty("logDirectory", ".");
        merchantProperties.setProperty("logMaximumSize", "10");
        Document request = Utility.readRequest(merchantProperties);
        
        // run transaction now
         Document replyDoc = XMLClient.runTransaction(request, merchantProperties);
         String responseStr =  Utility.nodeToString(replyDoc);
         Assert.assertTrue(responseStr.contains("<c:reasonCode>100</c:reasonCode>"));
    }
    
    
    
   
}
