package com.cybersource.ws.client;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;

import org.junit.function.ThrowingRunnable;
import org.junit.rules.ExpectedException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.util.*;

public class UtilityTest extends BaseTest {
    String propertiesFilename;
    Properties properties;
    private static final String ELEM_NVP_REPLY = "nvpReply";

    @Before
    public void setUp() {
     URL fileUrl = Thread.currentThread().getContextClassLoader().getResource("test_cybs.properties");
        String filepath = "";
        if(fileUrl != null) {
            propertiesFilename = fileUrl.getFile();
            try {
                properties = new Properties();
                properties.load(new FileReader(propertiesFilename));
            } catch (IOException e) {
                fail("Unable to load properties file");
            }
        } else {
            fail("Unable to find properties file");
        }
    }

    /*
    @Test
    public void testNodeToString() {
        Document document = getSampleXMLDocument(testSimpleXML);
        Node n = document.getChildNodes().item(0);
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + testSimpleXML;
        String result = Utility.nodeToString(n);
        result = result.replaceAll("\\n", "");
        expected = expected.replaceAll("\\n","");
        assertEquals(expected, result);
    } */

    //nodeToString(Node, int)

    @Test
    public void testReadProperties() throws Exception {
        String[] commandLineArgs = {propertiesFilename};
        Properties result = Utility.readProperties(commandLineArgs);
        Set<String> propertyNames = properties.stringPropertyNames();
        for(String expectedKey : propertyNames) {
            assertTrue(result.containsKey(expectedKey));
        }
    }

    @Test
    public void testRead() throws Exception {
        byte[] data = "abcdefhijklmnopqrstuvwxyz".getBytes();
        InputStream in = new ByteArrayInputStream(data);
        byte[] result = Utility.read(in);
        assertArrayEquals(data, result);
    }

    @Test
    public void testRead_empty() throws Exception {
        byte[] data = {};
        InputStream in = new ByteArrayInputStream(data);
        byte[] result = Utility.read(in);
        assertArrayEquals(data, result);
    }

    @Test
    public void testStringToMap() {
        String nvp = "ui=Fred\n" + "eng=Steve";
        HashMap result = Utility.stringToMap(nvp);
        assertEquals(result.get("ui"), "Fred");
        assertEquals(result.get("eng"), "Steve");
    }

    @Test
    public void testMapToString() {
        Map map = new LinkedHashMap();
        map.put("ui", "Annette");
        map.put("eng", "Risa");
        String expected = "ui=Annette\neng=Risa\n";
        String result = Utility.mapToString(map,false,0);
        assertEquals(expected,result);

        //test escaped chars - expecting Utility to escape chars
        map = new LinkedHashMap();
        map.put("_has_escapes", "0");
        map.put("ui", "Annette & Risa");
        result = Utility.mapToString(map,false,0);
        expected = "_has_escapes=0\nui=Annette &amp; Risa\n";
        assertEquals(expected,result);

        //test escaped chars - expecting Utility to NOT escape chars
        map = new LinkedHashMap();
        map.put("_has_escapes", "1");
        map.put("ui", "Annette &amp; Risa");
        result = Utility.mapToString(map,false,0);
        expected = "_has_escapes=1\nui=Annette &amp; Risa\n";
        assertEquals(expected,result);

        //test escaped chars with has_escapes=true - expecting Utility to NOT escape chars
        map = new LinkedHashMap();
        map.put("_has_escapes", "true");
        map.put("ui", "Annette &amp; Risa");
        result = Utility.mapToString(map,false,0);
        expected = "_has_escapes=true\nui=Annette &amp; Risa\n";
        assertEquals(expected,result);

        //test masking
        map = new LinkedHashMap();
        map.put("requestMessage_randomField", "12345");
        result = Utility.mapToString(map,true,0);
        expected = "requestMessage_randomField=xxxxx\n";
        assertEquals(expected,result);

    }

    @Test
    public void testMapToString_error() {
        Map map = null;
        String result = Utility.mapToString(map,false,0);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testNewDocumentBuilder_validate() {
        String testData1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE example [\n" +
                "  <!ENTITY file SYSTEM \"file:///secrets.txt\" >\n" +
                "]>\n" +
                "<example>&file;</example>";

        String testData2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE example [\n" +
                "  <!ENTITY file SYSTEM \"file:////etc/shadow\" >\n" +
                "]>\n" +
                "<example>&file;</example>";

        String testData3 = "<?xml version=\"1.0\"?>" +
                "<w3resource>" +
                "<design>html xhtml css svg xml</design>" +
                "<programming>php mysql</programming>" +
                "</w3resource>";

        try {
            DocumentBuilder docBuilder = Utility.newDocumentBuilder();

            InputStream testStream = new ByteArrayInputStream(testData1.getBytes());
            Document testDoc = docBuilder.parse(testStream);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            assertSame(e.getClass(), SAXParseException.class);
        }

        try {
            DocumentBuilder docBuilder = Utility.newDocumentBuilder();

            InputStream testStream = new ByteArrayInputStream(testData2.getBytes());
            Document testDoc = docBuilder.parse(testStream);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            assertSame(e.getClass(), SAXParseException.class);
        }

        try {
            DocumentBuilder docBuilder = Utility.newDocumentBuilder();

            InputStream testStream = new ByteArrayInputStream(testData3.getBytes());
            Document testDoc = docBuilder.parse(testStream);
            assertNotNull(testDoc);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }
}
