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
import static org.junit.Assert.assertFalse;  
import static org.junit.Assert.assertTrue; 
import static org.mockito.Mockito.times;  
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.InputStream;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;

public class SecurityUtilIT {

    private static final String textXmlDoc = "<soap:Envelope xmlns:soap=\"" +
            "http://schemas.xmlsoap.org/soap/envelope/\">\n<soap:Body id=\"body1\">\n" +
            "<nvpRequest xmlns=\"{0}\">\n{1}</nvpRequest>" +
            "\n</soap:Body>\n</soap:Envelope>";

    private static Map<String,String> requestMap;
    private Document wrappedDoc;
    private MerchantConfig config;
    private Properties merchantProperties;
    private Logger logger;
    
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
        requestMap.put("merchant_id", "cybs_test_ashish");
        
        //Loading the properties file from src/test/resources
        merchantProperties = new Properties();
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("test_cybs.properties");
		if (in == null) {
			throw new RuntimeException("Unable to load test_cybs.properties file");
		}
		merchantProperties.load(in);
        logger = new LoggerImpl(new MerchantConfig(merchantProperties, merchantProperties.getProperty("merchantID")));

        config = new MerchantConfig(merchantProperties, merchantProperties.getProperty("merchantID"));

        Object[] arguments
                = {config.getEffectiveNamespaceURI(),
                Client.mapToString(requestMap, false, PCI.REQUEST)};
        String xmlString = MessageFormat.format(textXmlDoc, arguments);
        
        DocumentBuilder builder = Utility.newDocumentBuilder();
        StringReader sr = new StringReader( xmlString );
        wrappedDoc = builder.parse( new InputSource( sr ) );
        sr.close();
        SecurityUtil.loadMerchantP12File(config,logger);
    }
    
    @Test
    public void testSoapWrapAndSign() throws Exception {
    	Document doc = SecurityUtil.createSignedDoc(wrappedDoc,config.getMerchantID(),config.getKeyPassword(),logger);
        NodeList signatureElement = doc.getElementsByTagName("wsse:Security");
        assert (signatureElement.getLength() >= 1);
    }
    
    @Test
    public void testSoapWrapSignedAndEncrypt() throws Exception {
    	Document signedDoc = SecurityUtil.createSignedDoc(wrappedDoc,config.getMerchantID(),config.getKeyPassword(),logger);
        NodeList signatureElement = signedDoc.getElementsByTagName("wsse:Security");
        assert (signatureElement.getLength() >= 1);
    	Document doc = SecurityUtil.handleMessageCreation(wrappedDoc, config.getMerchantID(),logger);
        NodeList signatureElementEnc = doc.getElementsByTagName("xenc:EncryptedKey");
        assert (signatureElementEnc.getLength() >= 1);
        assertEquals("Id", signatureElementEnc.item(0).getAttributes().item(0).getLocalName());
    }

	@Test
    public void testServerIdentityToKeyStore() throws Exception{
    	Identity identity = Mockito.mock(Identity.class);
    	X509Certificate x509Cert = Mockito.mock(X509Certificate.class);
    	Logger logger = Mockito.mock(Logger.class);
        KeyStore myKeystore = KeyStore.getInstance("jks");
        myKeystore.load(null, null);
        
        Mockito.when(identity.getPrivateKey()).thenReturn(null);
    	Mockito.when(identity.getX509Cert()).thenReturn(x509Cert);
    	Mockito.when(identity.getName()).thenReturn("MahenCertTest");
    	Mockito.when(identity.getKeyAlias()).thenReturn("MahenCertTest");
    	
    	MessageHandlerKeyStore mhKeyStore= new MessageHandlerKeyStore();  
    	MessageHandlerKeyStore spyMhKeyStore = Mockito.spy(mhKeyStore);
    	Mockito.when(spyMhKeyStore.getKeyStore()).thenReturn(myKeystore);
    	spyMhKeyStore.addIdentityToKeyStore(identity,logger);
    	Mockito.verify(spyMhKeyStore).getKeyStore();
    }
	
	@Test
    public void testMerchantIdentityToKeyStore() throws Exception{
    	Identity identity = Mockito.mock(Identity.class);
    	X509Certificate x509Cert = Mockito.mock(X509Certificate.class);
    	PrivateKey pkey = Mockito.mock(PrivateKey.class);
    	Logger logger = Mockito.mock(Logger.class);
        KeyStore myKeystore = KeyStore.getInstance("jks");
        myKeystore.load(null, null);
        
        PrivateKey newPkay= instPrivateKey(pkey);
        
    	Mockito.when(identity.getPrivateKey()).thenReturn(newPkay);
    	Mockito.when(identity.getX509Cert()).thenReturn(x509Cert);
    	Mockito.when(identity.getKeyAlias()).thenReturn("MahenCertTest");
        Mockito.when(identity.getPswd()).thenReturn("testPwd".toCharArray());
    	
    	MessageHandlerKeyStore mhKeyStore= new MessageHandlerKeyStore();    	

    	MessageHandlerKeyStore spyMhKeyStore = Mockito.spy(mhKeyStore);
    	Mockito.when(spyMhKeyStore.getKeyStore()).thenReturn(myKeystore);
    	spyMhKeyStore.addIdentityToKeyStore(identity,logger);

    	Mockito.verify(identity,times(1)).getKeyAlias();
        Mockito.verify(identity,times(1)).getPrivateKey();
        Mockito.verify(identity,times(1)).getPswd();
        Mockito.verify(identity,times(1)).getX509Cert();

    }
    
	@Test  
    public void testcertificateCacheEnabled() throws Exception{  
        merchantProperties.setProperty("keyFilename", merchantProperties.getProperty("keyAlias") + ".p12");  
  
        // caching enabled (default)  
        final MerchantConfig configCertificateCachingEnabled =  
                new MerchantConfig(merchantProperties, "merchant_id_optional_caching_test");  
        final MerchantConfig configCertificateCachingEnabledSpy = Mockito.spy(configCertificateCachingEnabled);  
  
        SecurityUtil.loadMerchantP12File(configCertificateCachingEnabledSpy, logger);  
        SecurityUtil.loadMerchantP12File(configCertificateCachingEnabledSpy, logger);  
  
        verify(configCertificateCachingEnabledSpy, times(3)).getKeyFile();  
  
        // caching disabled  
        merchantProperties.setProperty("certificateCacheEnabled", "false");  
        final MerchantConfig certificateCacheDisabled =  
                new MerchantConfig(merchantProperties, "merchant_id_optional_caching_test");  
        final MerchantConfig certificateCacheDisabledSpy = Mockito.spy(certificateCacheDisabled);  
  
        SecurityUtil.loadMerchantP12File(certificateCacheDisabledSpy, logger);  
        SecurityUtil.loadMerchantP12File(certificateCacheDisabledSpy, logger);  
  
        verify(certificateCacheDisabledSpy, times(4)).getKeyFile();  
    } 
	
    private static PrivateKey instPrivateKey(PrivateKey pkey) throws Exception{
    	byte[] pkByts = {(byte) 0x30, (byte) 0x82, (byte) 0x02, (byte) 0x75, (byte) 0x02, (byte) 0x01, (byte) 0x00, (byte) 0x30, (byte) 0x0d, (byte) 0x06, (byte) 0x09, (byte) 0x2a, (byte) 0x86, (byte) 0x48, (byte) 0x86, (byte) 0xf7, (byte) 0x0d, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x05, (byte) 0x00, (byte) 0x04, (byte) 0x82, (byte) 0x02, (byte) 0x5f, (byte) 0x30, (byte) 0x82, (byte) 0x02, (byte) 0x5b, (byte) 0x02, (byte) 0x01, (byte) 0x00, (byte) 0x02, (byte) 0x81, (byte) 0x81, (byte) 0x00, (byte) 0xb9, (byte) 0x53, (byte) 0x1a, (byte) 0x98, (byte) 0xaa, (byte) 0x44, (byte) 0xcf, (byte) 0xe2, (byte) 0x9a, (byte) 0x42, (byte) 0x4d, (byte) 0x59, (byte) 0xba, (byte) 0x35, (byte) 0x18, (byte) 0x9a, (byte) 0xce, (byte) 0xb1, (byte) 0x3b, (byte) 0xcf, (byte) 0x60, (byte) 0x22, (byte) 0xc9, (byte) 0x7f, (byte) 0x86, (byte) 0x6e, (byte) 0x61, (byte) 0x31, (byte) 0xfe, (byte) 0xf9, (byte) 0x19, (byte) 0xce, (byte) 0x46, (byte) 0xb0, (byte) 0xe4, (byte) 0x86, (byte) 0x60, (byte) 0x3d, (byte) 0x54, (byte) 0x23, (byte) 0x7e, (byte) 0x0d, (byte) 0x65, (byte) 0xfa, (byte) 0xc1, (byte) 0x5b, (byte) 0xd9, (byte) 0x92, (byte) 0xa3, (byte) 0x24, (byte) 0x8f, (byte) 0x81, (byte) 0x76, (byte) 0x01, (byte) 0x1f, (byte) 0x4a, (byte) 0x6a, (byte) 0xf3, (byte) 0x65, (byte) 0xfa, (byte) 0xa5, (byte) 0xae, (byte) 0xd6, (byte) 0x84, (byte) 0xa5, (byte) 0x59, (byte) 0x1f, (byte) 0x32, (byte) 0xbf, (byte) 0xd0, (byte) 0x00, (byte) 0x3b, (byte) 0x86, (byte) 0x76, (byte) 0xca, (byte) 0x6e, (byte) 0xbb, (byte) 0x21, (byte) 0xca, (byte) 0xf1, (byte) 0xdf, (byte) 0xbf, (byte) 0x69, (byte) 0x4d, (byte) 0x8e, (byte) 0x91, (byte) 0xea, (byte) 0x44, (byte) 0x09, (byte) 0xb3, (byte) 0xc1, (byte) 0xf0, (byte) 0x3d, (byte) 0x81, (byte) 0xcf, (byte) 0x49, (byte) 0xdd, (byte) 0xca, (byte) 0xd3, (byte) 0x0d, (byte) 0x73, (byte) 0xea, (byte) 0xfa, (byte) 0xdb, (byte) 0x10, (byte) 0x26, (byte) 0x34, (byte) 0x69, (byte) 0x3e, (byte) 0x12, (byte) 0xdd, (byte) 0x33, (byte) 0x7f, (byte) 0x8f, (byte) 0x6b, (byte) 0x0e, (byte) 0x80, (byte) 0xc4, (byte) 0x5b, (byte) 0x42, (byte) 0x79, (byte) 0x7e, (byte) 0x28, (byte) 0x5f, (byte) 0xde, (byte) 0x7d, (byte) 0x99, (byte) 0x4f, (byte) 0x02, (byte) 0x03, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x81, (byte) 0x80, (byte) 0x69, (byte) 0xc8, (byte) 0xdf, (byte) 0x18, (byte) 0x9f, (byte) 0xb0, (byte) 0x91, (byte) 0xbd, (byte) 0x76, (byte) 0x72, (byte) 0x3c, (byte) 0x36, (byte) 0xe8, (byte) 0x8c, (byte) 0x60, (byte) 0x54, (byte) 0x15, (byte) 0x81, (byte) 0xa3, (byte) 0x73, (byte) 0x57, (byte) 0x1b, (byte) 0xe4, (byte) 0x4a, (byte) 0xcf, (byte) 0xd0, (byte) 0x77, (byte) 0xd8, (byte) 0x93, (byte) 0x03, (byte) 0x5b, (byte) 0xd0, (byte) 0x9c, (byte) 0x17, (byte) 0x63, (byte) 0x0a, (byte) 0xb5, (byte) 0x2a, (byte) 0xac, (byte) 0xb9, (byte) 0x69, (byte) 0xbd, (byte) 0x7a, (byte) 0x25, (byte) 0xad, (byte) 0x73, (byte) 0xa1, (byte) 0x79, (byte) 0x0b, (byte) 0x78, (byte) 0xd6, (byte) 0x15, (byte) 0x7e, (byte) 0xe7, (byte) 0x5b, (byte) 0x16, (byte) 0x1e, (byte) 0x80, (byte) 0x7b, (byte) 0x08, (byte) 0x9c, (byte) 0xc4, (byte) 0x75, (byte) 0x1b, (byte) 0xdd, (byte) 0xad, (byte) 0x70, (byte) 0x00, (byte) 0x38, (byte) 0x0d, (byte) 0xe0, (byte) 0x72, (byte) 0x69, (byte) 0x07, (byte) 0xe4, (byte) 0x31, (byte) 0x7a, (byte) 0x06, (byte) 0x58, (byte) 0x94, (byte) 0x9c, (byte) 0x41, (byte) 0xc6, (byte) 0x21, (byte) 0x73, (byte) 0x0b, (byte) 0x7e, (byte) 0x9a, (byte) 0x6b, (byte) 0x21, (byte) 0xc0, (byte) 0x8d, (byte) 0xd7, (byte) 0x57, (byte) 0x98, (byte) 0x70, (byte) 0x24, (byte) 0x69, (byte) 0x85, (byte) 0x9f, (byte) 0x77, (byte) 0x6e, (byte) 0x10, (byte) 0xf4, (byte) 0xdf, (byte) 0x4f, (byte) 0xbf, (byte) 0x38, (byte) 0xe7, (byte) 0xc3, (byte) 0x35, (byte) 0x55, (byte) 0xbe, (byte) 0x18, (byte) 0xbf, (byte) 0x33, (byte) 0x87, (byte) 0xba, (byte) 0x08, (byte) 0xe8, (byte) 0x22, (byte) 0xbe, (byte) 0x7f, (byte) 0x8e, (byte) 0xdc, (byte) 0x1a, (byte) 0x04, (byte) 0x21, (byte) 0x02, (byte) 0x41, (byte) 0x00, (byte) 0xf6, (byte) 0xd8, (byte) 0x18, (byte) 0x84, (byte) 0x52, (byte) 0x5f, (byte) 0xdf, (byte) 0x8f, (byte) 0xc9, (byte) 0xd4, (byte) 0x7d, (byte) 0x94, (byte) 0x70, (byte) 0x1d, (byte) 0x18, (byte) 0xab, (byte) 0xd3, (byte) 0x18, (byte) 0x7e, (byte) 0xfd, (byte) 0x32, (byte) 0xb5, (byte) 0xca, (byte) 0x5e, (byte) 0x97, (byte) 0x36, (byte) 0xc5, (byte) 0x66, (byte) 0xdc, (byte) 0x09, (byte) 0x5f, (byte) 0x5a, (byte) 0x9e, (byte) 0x82, (byte) 0x22, (byte) 0x05, (byte) 0x5c, (byte) 0x91, (byte) 0x8a, (byte) 0x66, (byte) 0xb1, (byte) 0x1d, (byte) 0xf1, (byte) 0x8c, (byte) 0x60, (byte) 0xd6, (byte) 0xe2, (byte) 0x98, (byte) 0xdc, (byte) 0x9c, (byte) 0x08, (byte) 0x02, (byte) 0x33, (byte) 0xa4, (byte) 0x79, (byte) 0x73, (byte) 0xef, (byte) 0x22, (byte) 0x8e, (byte) 0xe9, (byte) 0xc4, (byte) 0xe4, (byte) 0x13, (byte) 0x3f, (byte) 0x02, (byte) 0x41, (byte) 0x00, (byte) 0xc0, (byte) 0x32, (byte) 0xd9, (byte) 0xca, (byte) 0xb5, (byte) 0xda, (byte) 0x54, (byte) 0xf0, (byte) 0x13, (byte) 0xcd, (byte) 0x31, (byte) 0x3b, (byte) 0x11, (byte) 0xb1, (byte) 0x86, (byte) 0xa4, (byte) 0x27, (byte) 0x02, (byte) 0x79, (byte) 0xc2, (byte) 0xd4, (byte) 0xf7, (byte) 0x34, (byte) 0xb1, (byte) 0x8e, (byte) 0x76, (byte) 0x5d, (byte) 0x44, (byte) 0xe3, (byte) 0x1b, (byte) 0x3f, (byte) 0x3f, (byte) 0x18, (byte) 0x75, (byte) 0x1f, (byte) 0xe9, (byte) 0xc9, (byte) 0xf1, (byte) 0x03, (byte) 0xdf, (byte) 0x6e, (byte) 0xb1, (byte) 0x39, (byte) 0x8b, (byte) 0x6a, (byte) 0x69, (byte) 0x05, (byte) 0x4b, (byte) 0x27, (byte) 0xe2, (byte) 0x82, (byte) 0x65, (byte) 0x2b, (byte) 0x23, (byte) 0x11, (byte) 0x12, (byte) 0x76, (byte) 0x5b, (byte) 0x80, (byte) 0x67, (byte) 0xd9, (byte) 0x08, (byte) 0xc5, (byte) 0xf1, (byte) 0x02, (byte) 0x40, (byte) 0x70, (byte) 0xf1, (byte) 0x1a, (byte) 0xf6, (byte) 0xa0, (byte) 0x42, (byte) 0x21, (byte) 0xa6, (byte) 0x46, (byte) 0xb0, (byte) 0xa4, (byte) 0xec, (byte) 0xe0, (byte) 0x07, (byte) 0x50, (byte) 0x1c, (byte) 0x7e, (byte) 0x2f, (byte) 0xbd, (byte) 0x1a, (byte) 0xd8, (byte) 0xb2, (byte) 0xf8, (byte) 0xef, (byte) 0x22, (byte) 0xbc, (byte) 0xfa, (byte) 0xc1, (byte) 0x3f, (byte) 0x78, (byte) 0x42, (byte) 0x5a, (byte) 0xd2, (byte) 0x1f, (byte) 0xb4, (byte) 0xb5, (byte) 0x43, (byte) 0x4f, (byte) 0x8c, (byte) 0x45, (byte) 0xc4, (byte) 0x50, (byte) 0x71, (byte) 0x0e, (byte) 0xcb, (byte) 0xd8, (byte) 0x46, (byte) 0x41, (byte) 0xae, (byte) 0xde, (byte) 0xed, (byte) 0x83, (byte) 0x24, (byte) 0x61, (byte) 0xe2, (byte) 0xf8, (byte) 0x3a, (byte) 0xb8, (byte) 0x53, (byte) 0x2f, (byte) 0x7e, (byte) 0xd8, (byte) 0xe4, (byte) 0x3d, (byte) 0x02, (byte) 0x40, (byte) 0x1b, (byte) 0x03, (byte) 0x4a, (byte) 0x9e, (byte) 0xf5, (byte) 0xfe, (byte) 0x30, (byte) 0xaf, (byte) 0xe9, (byte) 0x68, (byte) 0x8e, (byte) 0x81, (byte) 0xc9, (byte) 0xd3, (byte) 0xd4, (byte) 0xa3, (byte) 0x9f, (byte) 0xa3, (byte) 0xf6, (byte) 0x6f, (byte) 0x0e, (byte) 0xb5, (byte) 0x8b, (byte) 0xdf, (byte) 0x64, (byte) 0xb1, (byte) 0x78, (byte) 0x1c, (byte) 0x65, (byte) 0x7a, (byte) 0xff, (byte) 0xe1, (byte) 0xa3, (byte) 0x53, (byte) 0x5a, (byte) 0xdf, (byte) 0xe5, (byte) 0xf5, (byte) 0x0c, (byte) 0xe1, (byte) 0x4b, (byte) 0x52, (byte) 0x77, (byte) 0x4f, (byte) 0x03, (byte) 0xee, (byte) 0xac, (byte) 0xc2, (byte) 0xca, (byte) 0x61, (byte) 0x48, (byte) 0x88, (byte) 0x65, (byte) 0x8e, (byte) 0xb1, (byte) 0x28, (byte) 0x92, (byte) 0x1f, (byte) 0xfc, (byte) 0x25, (byte) 0x1c, (byte) 0x58, (byte) 0xe2, (byte) 0x51, (byte) 0x02, (byte) 0x40, (byte) 0x48, (byte) 0x2e, (byte) 0x48, (byte) 0xa5, (byte) 0x92, (byte) 0x9e, (byte) 0xc8, (byte) 0x08, (byte) 0xca, (byte) 0x0e, (byte) 0x89, (byte) 0x80, (byte) 0x16, (byte) 0xff, (byte) 0x4c, (byte) 0x56, (byte) 0xa7, (byte) 0x4c, (byte) 0xc2, (byte) 0xc1, (byte) 0x2c, (byte) 0xb5, (byte) 0x80, (byte) 0x7d, (byte) 0xe4, (byte) 0xdd, (byte) 0xef, (byte) 0x25, (byte) 0x7c, (byte) 0xaa, (byte) 0xd1, (byte) 0x6d, (byte) 0x8d, (byte) 0xf1, (byte) 0x94, (byte) 0x3d, (byte) 0xf9, (byte) 0xb8, (byte) 0x13, (byte) 0x24, (byte) 0xad, (byte) 0x6e, (byte) 0x84, (byte) 0x6f, (byte) 0xfe, (byte) 0xa5, (byte) 0x2a, (byte) 0xa8, (byte) 0xfb, (byte) 0x87, (byte) 0xcd, (byte) 0x9e, (byte) 0x80, (byte) 0xc8, (byte) 0xc4, (byte) 0x53, (byte) 0x0d, (byte) 0xe2, (byte) 0x63, (byte) 0x67, (byte) 0x04, (byte) 0x5f, (byte) 0xa3, (byte) 0x09};
    	KeyFactory pvtcf = KeyFactory.getInstance("RSA");
        KeySpec keySpec = new PKCS8EncodedKeySpec(pkByts);
        PrivateKey privateKey = pvtcf.generatePrivate(keySpec);
        return privateKey;
    }
    
 }
    


