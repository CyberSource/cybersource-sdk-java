package com.cybersource.ws.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;



/** 
 * Import : This JUNIT Test case wil fail until P12 related details are not modified
 * Steps to Modify the P12
 * - Generate a P12 specific to Merchant ID and place the new P12 key under <keysDirectory>/src/test/resources/
 * - Change Merchnt ID , KeyAlias and keyPassword in all test cases
 * /

/**
 * Test case to validate the Conenction instance
 * @author sunagara
 *
 */
public class ConnectionTest {

	  private Connection con = null;
	  Properties merchantProperties = new Properties();
	  
	 /**
	  * Method returns the Connection instance of JDKHttpURLConnection 		 
	  */
	 @Test		 
	 public void testGetInstance(){
		 
		 	
	        
	        merchantProperties.setProperty("merchantID", "jasoneatoncorp");
	        merchantProperties.setProperty("keysDirectory", "src/test/resources");
	        //merchantProperties.setProperty("keyAlias", "jasoneatoncorp");
	        //merchantProperties.setProperty("keyPassword", "jasoneatoncorp");
	        merchantProperties.setProperty("targetAPIVersion", "1.97");
	        merchantProperties.setProperty("sendToProduction", "false");
	        merchantProperties.setProperty("serverURL", "https://ics2wstest.ic3.com/commerce/1.x/transactionProcessor/");
	       // merchantProperties.setProperty("serverURL", "http://mvqsstage002d.qa.intra:11080/commerce/1.x/transactionProcessor/");
	        merchantProperties.setProperty("timeout", "1000");
	        merchantProperties.setProperty("enableLog", "true");
	        merchantProperties.setProperty("logDirectory", ".");
	        merchantProperties.setProperty("logMaximumSize", "10");
	        
	        DocumentBuilder builder;
			try {
				builder = Utility.newDocumentBuilder();
			    Document request = Utility.readRequest(merchantProperties);
		        MerchantConfig mc;
				mc = new MerchantConfig(merchantProperties, null);
			    String merchantID = mc.getMerchantID();
	            String nsURI = mc.getEffectiveNamespaceURI();
	            setMerchantID(request, merchantID, nsURI);
	            LoggerWrapper logger = new LoggerWrapper(null, true, true, mc);
	            con = Connection.getInstance(mc, builder, logger);
	            Assert.assertNotNull(con);
	           
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}catch (ConfigException e) {
			
				e.printStackTrace();
			} 
		
	 }

	    /**
	     * Sets the merchantID in the request.
	     *
	     * @param request    request to add the merchantID to.
	     * @param merchantID merchantID to add to request.
	     * @param nsURI      namespace URI to use.
	     */
	    private static void setMerchantID(
	            Document request, String merchantID, String nsURI) {
	        // create merchantID node
	        Element merchantIDElem
	                = Utility.createElement(request, nsURI,  "merchantID", merchantID);

	        // add it as the first child of the requestMessage element.
	        Element requestMessage
	                = Utility.getElement(request, "requestMessage", nsURI);
	        requestMessage.insertBefore(
	                merchantIDElem, requestMessage.getFirstChild());
	    }

	    
	 
	   
	    
	    
}
