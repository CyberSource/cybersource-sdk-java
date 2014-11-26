package com.cybersource.ws.client;

import org.junit.Before;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.fail;

/**
 * Created by memirand on 11/24/2014.
 */
public class BaseTest {
    protected static Properties merchantProperties;
    protected static MerchantConfig merchantConfig;
    protected static final String testTextXmlDoc = "<soap:Envelope xmlns:soap=\"" +
            "http://schemas.xmlsoap.org/soap/envelope/\">\n<soap:Body id=\"body1\">\n" +
            "<nvpRequest xmlns=\"{0}\">\n{1}</nvpRequest>" +
            "\n</soap:Body>\n</soap:Envelope>";
    protected static Map<String,String> basicRequestMap;

    @Before
    public void setupBaseInitialization() {
        //Loading the properties file from src/test/resources
        merchantProperties = new Properties();

        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("test_cybs.properties");
        if (in == null) {
            throw new RuntimeException("Unable to load test_cybs.properties file");
        }
        try {
            merchantProperties.load(in);
            merchantConfig = new MerchantConfig(merchantProperties, merchantProperties.getProperty("merchantID"));
        } catch (Exception e) {
            e.printStackTrace();
            merchantProperties = new Properties();
            merchantConfig = null;
        }

        // Sample Transadction Data is fed as HashMap input
        basicRequestMap = new HashMap<String,String>();
        basicRequestMap.put("ccAuthService_run", "true");
        basicRequestMap.put("merchantReferenceCode", "jasoneatoncorp");
        basicRequestMap.put("billTo_firstName", "John");
        basicRequestMap.put("billTo_lastName", "Doe");
        basicRequestMap.put("billTo_street1", "1295 Charleston Road");
        basicRequestMap.put("billTo_city", "Mountain View");
        basicRequestMap.put("billTo_state", "CA");
        basicRequestMap.put("billTo_postalCode", "94043");
        basicRequestMap.put("billTo_country", "US");
        basicRequestMap.put("billTo_email", "nobody@cybersource.com");
        basicRequestMap.put("billTo_ipAddress", "10.7.7.7");
        basicRequestMap.put("billTo_phoneNumber", "650-965-6000");
        basicRequestMap.put("shipTo_firstName", "Jane");
        basicRequestMap.put("shipTo_lastName", "Doe");
        basicRequestMap.put("shipTo_street1", "100 Elm Street");
        basicRequestMap.put("shipTo_city", "San Mateo");
        basicRequestMap.put("shipTo_state", "CA");
        basicRequestMap.put("shipTo_postalCode", "94401");
        basicRequestMap.put("shipTo_country", "US");
        basicRequestMap.put("card_accountNumber", "4111111111111111");
        basicRequestMap.put("card_expirationMonth", "12");
        basicRequestMap.put("card_expirationYear", "2020");
        basicRequestMap.put("purchaseTotals_currency", "USD");
        basicRequestMap.put("item_0_unitPrice", "12.34");
        basicRequestMap.put("item_1_unitPrice", "56.78");
        basicRequestMap.put("merchant_id", "jasoneatoncorp");
    }

    protected static Map<String,String> getSampleRequest() {
        Map<String,String> sampleRequestMap = new HashMap<String,String>();
        sampleRequestMap.put("ccAuthService_run", "true");
        sampleRequestMap.put("merchantReferenceCode", "jasoneatoncorp");
        sampleRequestMap.put("billTo_firstName", "John");
        sampleRequestMap.put("billTo_lastName", "Doe");
        sampleRequestMap.put("billTo_street1", "1295 Charleston Road");
        sampleRequestMap.put("billTo_city", "Mountain View");
        sampleRequestMap.put("billTo_state", "CA");
        sampleRequestMap.put("billTo_postalCode", "94043");
        sampleRequestMap.put("billTo_country", "US");
        sampleRequestMap.put("billTo_email", "nobody@cybersource.com");
        sampleRequestMap.put("billTo_ipAddress", "10.7.7.7");
        sampleRequestMap.put("billTo_phoneNumber", "650-965-6000");
        sampleRequestMap.put("shipTo_firstName", "Jane");
        sampleRequestMap.put("shipTo_lastName", "Doe");
        sampleRequestMap.put("shipTo_street1", "100 Elm Street");
        sampleRequestMap.put("shipTo_city", "San Mateo");
        sampleRequestMap.put("shipTo_state", "CA");
        sampleRequestMap.put("shipTo_postalCode", "94401");
        sampleRequestMap.put("shipTo_country", "US");
        sampleRequestMap.put("card_accountNumber", "4111111111111111");
        sampleRequestMap.put("card_expirationMonth", "12");
        sampleRequestMap.put("card_expirationYear", "2020");
        sampleRequestMap.put("purchaseTotals_currency", "USD");
        sampleRequestMap.put("item_0_unitPrice", "12.34");
        sampleRequestMap.put("item_1_unitPrice", "56.78");
        sampleRequestMap.put("merchant_id", "jasoneatoncorp");
        return sampleRequestMap;
    }

    protected static Object invokePrivateStaticMethod(Class targetClass, String methodName, Class[] argClasses, Object[] argObjects)
     throws InvocationTargetException {

        try {
            Method method = targetClass.getDeclaredMethod(methodName, argClasses);
            method.setAccessible(true);
            return method.invoke(null, argObjects);
        }
        catch (NoSuchMethodException e) {
            fail();
        }
        catch (SecurityException e) {
            fail();
        }
        catch (IllegalAccessException e) {
            fail();
        }
        catch (IllegalArgumentException e) {
            fail();
        }
        return null;
    }

    protected static String testSimpleXML =  "<employees>\n" +
            "<person>\n" +
            "<name>Fred</name>\n" +
            "<position>UI</position>\n" +
            "</person>\n" +
            "<person>\n" +
            "<name>Steve</name>\n" +
            "<position>Dev</position>\n" +
            "</person>\n" +
            "</employees>\n";

    protected static Document getSampleXMLDocument() {
        Document document = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder builder = dbf.newDocumentBuilder();
            StringReader sr = new StringReader(testSimpleXML);
            document = builder.parse(new InputSource(sr));
            sr.close();

        } catch (Exception e) {
            document = null;
        }
        return document;
    }
}
