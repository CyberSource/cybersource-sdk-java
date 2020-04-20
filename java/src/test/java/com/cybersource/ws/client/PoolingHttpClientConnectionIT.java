package com.cybersource.ws.client;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Properties;

/**
 * Test case to validate the PoolingHttpClientConnection instance
 * @author Mayuri K.
 *
 */
public class PoolingHttpClientConnectionIT {
    private String requestFilename = "src/test/resources/auth.xml";
    PoolingHttpClientConnection con = null;
    Document signedDoc;
    DocumentBuilder builder;
    MerchantConfig mc;
    LoggerWrapper logger;
    Document request;
    private static Document soapEnvelope;
    private static final String SOAP_ENVELOPE =
            "<soap:Envelope xmlns:soap=\"" +
                    "http://schemas.xmlsoap.org/soap/envelope/" +
                    "\"><soap:Body></soap:Body></soap:Envelope>";
    private long requestSentTime = System.currentTimeMillis();

    static {
        try {
            // load the SOAP envelope document.
            DocumentBuilder builder = Utility.newDocumentBuilder();
            StringReader sr = new StringReader(SOAP_ENVELOPE);
            soapEnvelope = builder.parse(new InputSource(sr));
            sr.close();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void start(){
        Properties merchantProperties = new Properties();
        //Loading the properties file from src/test/resources
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("test_cybs.properties");
        if (in == null) {
            throw new RuntimeException("Unable to load test_cybs.properties file");
        }
        try {
            merchantProperties.load(in);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        try {
            builder = Utility.newDocumentBuilder();
            request = Utility.readRequest(merchantProperties, requestFilename);
            mc = new MerchantConfig(merchantProperties, null);
            String merchantID = mc.getMerchantID();
            String nsURI = mc.getEffectiveNamespaceURI();
            setMerchantID(request, merchantID, nsURI);
            logger = new LoggerWrapper(null, true, true, mc);

            con = new PoolingHttpClientConnection(mc, builder, logger);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (ConfigException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method returns the Connection instance of PoolingHttpURLConnection
     */
    @Test
    public void testGetInstance(){
        Assert.assertNotNull(con);
    }

    /**
     * Method test the post request
     */
    @Test
    public void testPostRequest(){
        try {
            Element requestMessage
                    = Utility.getElement(
                    request, "requestMessage", mc.getEffectiveNamespaceURI());
            Document wrappedDoc = builder.newDocument();

            wrappedDoc.appendChild(
                    wrappedDoc.importNode(soapEnvelope.getFirstChild(), true));

            if (requestMessage != null) {
                wrappedDoc.getFirstChild().getFirstChild().appendChild(
                        wrappedDoc.importNode(requestMessage, true));
            }
            SecurityUtil.loadMerchantP12File(mc,logger);
            signedDoc = SecurityUtil.createSignedDoc(wrappedDoc, mc.getKeyAlias(), mc.getKeyPassword(), logger);
            Document wrappedReply = con.post(signedDoc, requestSentTime);
            Assert.assertNotNull(wrappedReply);
            //test the http response code
            Assert.assertEquals(200, con.getHttpResponseCode());
            //test the http request sent or not
            Assert.assertEquals(true, con.isRequestSent());

        } catch (ClientException e) {
            e.printStackTrace();
        } catch (FaultException e) {
            e.printStackTrace();
        } catch (SignException e) {
            e.printStackTrace();
        } catch (SignEncryptException e) {
            e.printStackTrace();
        } catch (ConfigException e) {
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
    private static void setMerchantID(Document request, String merchantID, String nsURI) {
        // create merchantID node
        Element merchantIDElem = Utility.createElement(request, nsURI,  "merchantID", merchantID);

        // add it as the first child of the requestMessage element.
        Element requestMessage = Utility.getElement(request, "requestMessage", nsURI);
        requestMessage.insertBefore(merchantIDElem, requestMessage.getFirstChild());
    }

}
