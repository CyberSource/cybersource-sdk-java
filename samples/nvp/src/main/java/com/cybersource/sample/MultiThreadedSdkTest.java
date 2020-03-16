package com.cybersource.sample;


import com.cybersource.ws.client.Client;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

class MultithreadingDemo extends Thread {
    HashMap<String, String> requestMap;
    Properties props;
//    Client client;

    MultithreadingDemo(HashMap<String, String> x, Properties y) {
        requestMap = x;
        props = y;
    }

    public void run() {
        try {
            Map<String, String> map = Client.runTransaction(requestMap, props);
//            System.out.println(map.get("ccAuthReply_processorResponse"));
        }
        catch (Exception e) {
            // Throwing an exception
            System.out.println("Exception is caught");
        }
    }
}

// Main Class
public class MultiThreadedSdkTest {
    private static final String PROPERTIES="cybs.properties";

    public static void main(String[] args) {
        Properties cybProperties;
        cybProperties = readCybsProperty(PROPERTIES);

        HashMap<String, String> requestMap = getRequestMap();
        Client c = new Client();
        for (int i=0; i<100; i++) {
//            try {
//                Thread.sleep(10000000);
//            } catch (InterruptedException ie) {
//                System.out.println("Caught ie");
//            }

            MultithreadingDemo object = new MultithreadingDemo(requestMap, cybProperties);
            object.start();
        }
    }

    public static Properties readProperty(String filename) {
        Properties props = new Properties();

        try {
            FileInputStream fis = new FileInputStream(filename);
            props.load(fis);
            fis.close();
            return (props);
        } catch (IOException ioe) {
            System.out.println("File not found");
            // do nothing. An empty Properties object will be returned.
        }

        return (props);
    }

    public static Properties readCybsProperty(String file) {
        Properties cybsProps = new Properties();
        try {
            String filename = file;
            FileInputStream fis = new FileInputStream("/Users/asnagpal/projects/cybersource-sdk-java/samples/nvp/cybs.properties");
            cybsProps.load(fis);
            fis.close();
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
        return (cybsProps);
    }

    public static HashMap<String, String> getRequestMap() {
        HashMap<String, String> requestMap = new HashMap<String, String>();
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
        return requestMap;
    }
}
