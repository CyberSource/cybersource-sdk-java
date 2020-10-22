package com.cybersource.sample;

import com.cybersource.ws.client.Client;
import com.cybersource.ws.client.ClientException;
import com.cybersource.ws.client.FaultException;
import com.cybersource.ws.client.Utility;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.cybersource.ws.client.Utility.MERCHANT_TRANSACTION_IDENTIFIER;


/**
 * Sample class that demonstrates how to call Credit Card Authorization.
 */
public class RunSample {
    public static AtomicInteger totalSuccessfulTxn = new AtomicInteger(0);
    public static AtomicInteger totalFailedTxn = new AtomicInteger(0);
    public static AtomicInteger totalTxnSent = new AtomicInteger(0);

    // Authreversal
    public static AtomicInteger authReversalTotalSuccessfulTxn = new AtomicInteger(0);
    public static AtomicInteger authReversalTotalFailedTxn = new AtomicInteger(0);
    public static AtomicInteger authReversalTotalTxnSent = new AtomicInteger(0);

    private static final String PROPERTIES = "cybs.properties";

    public enum sample {
        auth, capture, emv_auth, credit, auth_reversal, sale;

        public static int enum_exist(String str) {
            for (sample me : sample.values()) {
                if (me.name().equalsIgnoreCase(str))
                    return me.ordinal();
            }
            return -1;
        }

    }

    /**
     * Entry point.
     *
     * @param args command-line arguments. The name of the property file may be
     *             passed as a command-line argument. If not passed, it will look
     *             for "cybs.properties" in the current directory.
     */
    public static void main(String[] args) {
        String argument = args[0];
        String requestID;

        Properties cybProperties = readCybsProperty();
        int choice = sample.enum_exist(argument);

        switch (choice) {
            case 0:
                runAuth(cybProperties);
                break;
            case 1:
                requestID = runAuth(cybProperties);
                System.out.println("Request ID is " + requestID);
                if (requestID != null) {
                    runCapture(cybProperties, requestID);
                }
                break;
            case 2:
                runAuthEMV(cybProperties, argument);
                break;
            case 3:
                requestID = runAuth(cybProperties);
                System.out.println("Request ID is " + requestID);
                if (requestID != null) {
                    runCapture(cybProperties, requestID);
                    runCredit(cybProperties, requestID, argument);
                }
                break;
            case 4:
                requestID = runAuth(cybProperties);
                if (requestID != null) {
                    runAuthReversal(cybProperties, requestID, argument);
                }
                break;
            case 5:
                runSale(cybProperties);
                break;
            case -1:
                System.out.println(" \t\t Enter the correct Service_name\n\n " +
                        "\t\t while running the Script enter the service_name \n\n" +
                        "\t\t for example to run a auth transaction enter the following command: \n" +
                        "\t\t\t  for windows : runSample <service_name> \n " +
                        "\t\t\t  for linux : ./runSample.sh <service_name>  \n" +
                        "\t\t service name argument can be auth,sale,credit,authreversal,capture,emv_auth \n" +
                        "\t\t NOTE: if no argument is entered the script will terminate the program");
                break;
            default:
                break;
        }
    }

    public static String runSale(Properties props) {
        String requestID = null;
        Properties saleProps = readProperty("sale.properties");
        HashMap<String, String> request = new HashMap<String, String>((Map) saleProps);
        try {
            displayMap("CREDIT CARD SALE REQUEST:", request);
            // run transaction now
            Map<String, String> reply = Client.runTransaction(request, props);
            displayMap("CREDIT CARD SALE REPLY:", reply);
            requestID = reply.get("requestID");
            System.out.println("Sale completed " + requestID);

        } catch (ClientException e) {
            System.out.println(e.getMessage());
            if (e.isCritical()) {
                handleCriticalException(e, request);
            }
        } catch (FaultException e) {
            System.out.println(e.getMessage());
            if (e.isCritical()) {
                handleCriticalException(e, request);
            }
        }

        return (requestID);
    }

    /**
     * Runs Credit Card Authorization.
     *
     * @param props Properties object.
     * @return the requestID.
     */

    public static String runAuth(Properties props) {
        long startTime = System.currentTimeMillis();
        String requestID = null;
        Properties authProps = readProperty("auth.properties");
        authProps.put(MERCHANT_TRANSACTION_IDENTIFIER, UniquIDGenerator.getInstance().createUniqueID());
        HashMap<String, String> request = new HashMap<String, String>(
                (Map) authProps);
        try {
            displayMap("CREDIT CARD AUTHORIZATION REQUEST:", request);
            // run transaction now
            Map<String, String> reply = Client.runTransaction(request, props);
            displayMap("CREDIT CARD AUTHORIZATION REPLY:", reply);
            // if the authorization was successful, obtain the request id
            // for the follow-on capture later.
            String decision = reply.get("decision");
            if (decision != null) {
                requestID = reply.get("requestID");
            }
            if (requestID == null) {
                System.out.println("Request id null >>" + reply);
            }
        } catch (ClientException e) {
            String stackTrace = Utility.getStackTrace(e.getInnerException() != null ? e.getInnerException() : e);
            System.out.println("client exception >>> " + stackTrace);
        } catch (FaultException e) {
            System.out.println("fault exception >>>" + e);
        } catch (Exception e) {
            System.out.println("exception >>>" + e.getMessage());
        }

        System.out.println("Time taken for request id : " + requestID + " " + (System.currentTimeMillis() - startTime));
        return requestID;
    }

    /**
     * Runs Credit Card Capture.
     *
     * @param props         Properties object.
     * @param authRequestID requestID returned by a previous authorization.
     */
    public static String runCapture(Properties props, String authRequestID) {
        Properties captureProps = readProperty("capture.properties");
        HashMap<String, String> request = new HashMap<String, String>((Map) captureProps);

        request.put("ccCaptureService_authRequestID", authRequestID);
        try {
            displayMap("FOLLOW-ON CAPTURE REQUEST:", request);
            // run transaction now
            Map<String, String> reply = Client.runTransaction(request, props);
            displayMap("FOLLOW-ON CAPTURE REPLY:", reply);
        } catch (ClientException e) {
            System.out.println(e.getMessage());
            if (e.isCritical()) {
                handleCriticalException(e, request);
            }
        } catch (FaultException e) {
            System.out.println(e.getMessage());
            if (e.isCritical()) {
                handleCriticalException(e, request);
            }
        }
        return authRequestID;
    }

    /**
     * @param props
     * @param authRequestID
     * @param propFileName
     */
    public static void runAuthReversal(Properties props, String authRequestID, String propFileName) {
        long startTime = System.currentTimeMillis();
        String requestID = null;
        propFileName = "auth_reversal";
        Properties authReversalProps = readProperty(propFileName+".properties");
        authReversalProps = readProperty("auth_reversal.properties");
        authReversalProps.put("merchantTransactionIdentifier",UniquIDGenerator.getInstance().createUniqueID());
        HashMap<String, String> request = new HashMap<String, String>(
                (Map) authReversalProps);
        request.put("ccAuthReversalService_authRequestID", authRequestID);

        try {
            displayMap("REVERSAL REQUEST:", request);
            System.out.println("auth reversal");
            // run transaction now
            Map<String, String> reply = Client.runTransaction(request, props);
            displayMap("REVERSAL REPLY:", reply);

            String decision = reply.get("decision");
            if ("ACCEPT".equalsIgnoreCase(decision) || "REJECT".equalsIgnoreCase(decision)) {
                requestID = reply.get("requestID");
                authReversalTotalSuccessfulTxn.incrementAndGet();
            }
            if (requestID == null) {
                authReversalTotalFailedTxn.incrementAndGet();
                System.out.println(new Date()  + " authreversal request id null >> " + " auth id: " + authRequestID +  " " + reply);
            }

        } catch (ClientException e) {
            String stackTrace = Utility.getStackTrace(e.getInnerException() != null? e.getInnerException(): e);
            System.out.println(new Date() + " authreversal client exception >>> auth id: " + authRequestID + " " + stackTrace);
        } catch (FaultException e) {
            System.out.println(new Date()  + " authreversal fault exception >>>auth id: " + authRequestID + " " + e);
        } catch (Exception e) {
            System.out.println(new Date()  + " authreversal exception >>>auth id: " + authRequestID + " " + e.getMessage());
        }
        System.out.println(new Date() + " Time taken for authreversal request id : " + requestID + " auth id: " + authRequestID + " " +(System.currentTimeMillis()-startTime));

    }

    public static String runAuthEMV(Properties props, String propFileName) {
        Properties authEMVProps = readProperty(propFileName + ".properties");
        String emvRequestID = null;
        HashMap<String, String> request = new HashMap<String, String>((Map) authEMVProps);
        try {
            displayMap("CREDIT CARD EMVAUTHORIZATION REQUEST:", request);
            // run transaction now
            Map<String, String> reply = Client.runTransaction(request, props);
            displayMap("CREDIT CARD EMVAUTHORIZATION REPLY:", reply);
            // if the authorization was successful, obtain the request id
            // for the follow-on capture later.
            String decision = reply.get("decision");
            if ("ACCEPT".equalsIgnoreCase(decision)) {
                emvRequestID = reply.get("requestID");
            }

        } catch (ClientException e) {
            System.out.println(e.getMessage());
            if (e.isCritical()) {
                handleCriticalException(e, request);
            }
        } catch (FaultException e) {
            System.out.println(e.getMessage());
            if (e.isCritical()) {
                handleCriticalException(e, request);
            }
        }

        return emvRequestID;
    }

    public static void runCredit(Properties props, String RequestID, String propFileName) {
        Properties creditProps = readProperty(propFileName + ".properties");
        HashMap<String, String> request = new HashMap<String, String>((Map) creditProps);
        request.put("ccCreditService_captureRequestID", RequestID);
        try {
            displayMap("CREDIT REQUEST:", request);
            // run transaction now
            Map<String, String> reply = Client.runTransaction(request, props);
            displayMap("CREDIT REPLY:", reply);
        } catch (ClientException e) {
            System.out.println(e.getMessage());
            if (e.isCritical()) {
                handleCriticalException(e, request);
            }
        } catch (FaultException e) {
            System.out.println(e.getMessage());
            if (e.isCritical()) {
                handleCriticalException(e, request);
            }
        }
    }

    /**
     * Displays the content of the Map object.
     *
     * @param header Header text.
     * @param map    Map object to display.
     */
    private static void displayMap(String header, Map map) {
        System.out.println(header);

        StringBuilder dest = new StringBuilder();

        if (map != null && !map.isEmpty()) {
            Iterator iter = map.keySet().iterator();
            String key, val;
            while (iter.hasNext()) {
                key = (String) iter.next();
                val = (String) map.get(key);
                dest.append(key).append("=").append(val).append("\n");
            }
        }

        System.out.println(dest.toString());
    }

    /**
     * An exception is considered critical if some type of disconnect occurs
     * between the client and server and the client can't determine whether the
     * transaction was successful. If this happens, you might have a transaction
     * in the CyberSource system that your order system is not aware of. Because
     * the transaction may have been processed by CyberSource, you should not
     * resend the transaction, but instead send the exception information and
     * the order information (customer name, order number, etc.) to the
     * appropriate personnel at your company to resolve the problem. They should
     * use the information as search criteria within the CyberSource Transaction
     * Search Screens to find the transaction and determine if it was
     * successfully processed. If it was, you should update your order system
     * with the transaction information. Note that this is only a
     * recommendation; it may not apply to your business model.
     *
     * @param e       Critical ClientException object.
     * @param request Request that was sent.
     */
    private static void handleCriticalException(ClientException e, Map request) {
        // send the exception and order information to the appropriate
        // personnel at your company using any suitable method, e.g. e-mail,
        // multicast log, etc.
    }

    /**
     * See header comment in the other version of handleCriticalException above.
     *
     * @param e       FaultException object.
     * @param request Request that was sent.
     */
    private static void handleCriticalException(FaultException e, Map request) {
        // send the exception and order information to the appropriate
        // personnel at your company using any suitable method, e.g. e-mail,
        // multicast log, etc.
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
            ioe.printStackTrace();
        }

        return (props);
    }

    private static Properties readCybsProperty() {
        Properties cybsProps = new Properties();
        try {
            FileInputStream fis = new FileInputStream(PROPERTIES);
            cybsProps.load(fis);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (cybsProps);
    }

    public static class UniquIDGenerator {
        private AtomicLong serialNumber = new AtomicLong(1);
        private String ipAddress = generateIPAddress();
        private static UniquIDGenerator SELF = new UniquIDGenerator();

        static UniquIDGenerator getInstance() {
            return SELF;
        }

        private String generateIPAddress() {
            InetAddress addr = null;
            for (int tries = 1; ; tries++) {
                try {
                    addr = InetAddress.getLocalHost();
                    String hostID = getHostID();
                    if (hostID == null) {
                        addr = InetAddress.getLocalHost();
                    } else {
                        addr = InetAddress.getByName(hostID);
                    }
                    validateIPAddress(addr);
                    break;
                } catch (UnknownHostException e) {
                    if (tries >= 3) {
                        break;
                    } else {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ignored) {
                            // ignored
                        }
                    }
                }
            }
            if (addr == null) {
                return null;
            }
            BigInteger ip = new BigInteger(1, addr.getAddress());
            // pad the ip address string to 10 characters
            String ipString = ip.toString();
            if (ipString.length() < 10) {
                ipString = "0000000000".substring(ipString.length()) + ipString;
            }
            // trim leading characters in case it's > 10 digits long
            if (ipString.length() > 10) {
                ipString = ipString.substring(ipString.length() - 10);
            }
            return ipString;
        }

        private String getHostID() {
            return System.getProperty("host_id");
        }

        String createUniqueID() {
            long time = System.nanoTime();
            long serial = serialNumber.getAndIncrement();
            if (ipAddress == null) {
                return null;
            }
            return createUniqueID(ipAddress, time, serial);
        }

        private String createUniqueID(String ipAddress, long timeInNano, long serial) {
            return String.format("%015d%05d%10s",
                    timeInNano,
                    serial % 100000,
                    ipAddress);
        }

        /**
         * Validates the IP address.  The only validation currenly
         * being made is the check against 127.0.0.1.
         **/
        private static void validateIPAddress(InetAddress addr)
                throws UnknownHostException {
            if (addr.equals(InetAddress.getByName("127.0.0.1"))) {
                throw new UnknownHostException(
                        "127.0.0.1 is not allowed.  Use a different IP address.");
            }
        }
    }
}