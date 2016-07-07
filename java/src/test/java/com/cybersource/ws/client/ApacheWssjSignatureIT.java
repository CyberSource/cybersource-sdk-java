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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.InputStream;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;

/**
 * This class helps in testing Apache WebService Security Signature class
 * User: jeaton
 * Date: 6/20/14
 * Time: 1:36 PM
 */
public class ApacheWssjSignatureIT {

    private static final String textXmlDoc = "<soap:Envelope xmlns:soap=\"" +
            "http://schemas.xmlsoap.org/soap/envelope/\">\n<soap:Body id=\"body1\">\n" +
            "<nvpRequest xmlns=\"{0}\">\n{1}</nvpRequest>" +
            "\n</soap:Body>\n</soap:Envelope>";

    private static Map<String,String> requestMap;
    private SignedAndEncryptedMessageHandler handler;
    private Document wrappedDoc;
    private MerchantConfig config;
    
    @Before
    public void setup() throws Exception{
    	requestMap = new HashMap<String, String>();
        requestMap.put("ccAuthService_run", "true");
        requestMap.put("merchantReferenceCode", "your_reference_code");
        requestMap.put("billTo_firstName", "John");
        requestMap.put("billTo_lastName", "Doe");
        requestMap.put("billTo_street1", "1295 Charleston Road");
        requestMap.put("billTo_city", "Mountain View");
        requestMap.put("billTo_state", "CA");
        requestMap.put("billTo_postalCode", "94043");
        requestMap.put("billTo_country", "US");
        requestMap.put("billTo_email", "nobody@cybersource.com");
        requestMap.put("billTo_ipAddress", "10.7.7.7");
        requestMap.put("billTo_phoneNumber", "650-965-6000");
        requestMap.put("shipTo_firstName", "Jane");
        requestMap.put("shipTo_lastName", "Doe");
        requestMap.put("shipTo_street1", "100 Elm Street");
        requestMap.put("shipTo_city", "San Mateo");
        requestMap.put("shipTo_state", "CA");
        requestMap.put("shipTo_postalCode", "94401");
        requestMap.put("shipTo_country", "US");
        requestMap.put("card_accountNumber", "4111111111111111");
        requestMap.put("card_expirationMonth", "12");
        requestMap.put("card_expirationYear", "2020");
        requestMap.put("purchaseTotals_currency", "USD");
        requestMap.put("item_0_unitPrice", "12.34");
        requestMap.put("item_1_unitPrice", "56.78");
        requestMap.put("merchant_id", "your_merchant_id");
        
        //Loading the properties file from src/test/resources
        Properties merchantProperties = new Properties();
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("test_cybs.properties");
		if (in == null) {
			throw new RuntimeException("Unable to load test_cybs.properties file");
		}
		merchantProperties.load(in);
        Logger logger = new LoggerImpl(new MerchantConfig(merchantProperties, merchantProperties.getProperty("merchantID")));

        config = new MerchantConfig(merchantProperties, merchantProperties.getProperty("merchantID"));

        Object[] arguments
                = {config.getEffectiveNamespaceURI(),
                Client.mapToString(requestMap, false, PCI.REQUEST)};
        String xmlString = MessageFormat.format(textXmlDoc, arguments);
        
        DocumentBuilder builder = Utility.newDocumentBuilder();
        StringReader sr = new StringReader( xmlString );
        wrappedDoc = builder.parse( new InputSource( sr ) );
        sr.close();
        
        handler = SignedAndEncryptedMessageHandler.getInstance(config,logger);
    }
    
    @Test
    public void testSoapWrapAndSign() throws Exception {
    	Document doc = handler.createSignedDoc(wrappedDoc,config.getMerchantID(),null);
        NodeList signatureElement = doc.getElementsByTagName("wsse:Security");
        assert (signatureElement.getLength() >= 1);
    }
    
    @Test
    public void testSoapWrapSignedAndEncrypt() throws Exception {
    	Document doc = handler.handleMessageCreation(wrappedDoc, config.getMerchantID());
        NodeList signatureElement = doc.getElementsByTagName("xenc:EncryptedKey");
        assert (signatureElement.getLength() >= 1);
        assertEquals("Id", signatureElement.item(0).getAttributes().item(0).getLocalName());
    }
}
