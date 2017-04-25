package com.cybersource.sample;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Properties;

import com.cybersource.ws.client.*;


/**
 * Sample class that demonstrates how to call Credit Card Authorization.
 */
public class RunSample {
	public enum sample{ auth, capture, emv_auth, credit, auth_reversal, sale;
	 public static int enum_exist(String str) {
		    for (sample me : sample.values()) {
		        if (me.name().equalsIgnoreCase(str))
		            return me.ordinal();
		    }
		    return -1;
		}

	 }
	Properties cybsProps = new Properties();
	private static final String PROPERTIES="cybs.properties";
	static String authDecision;
	static String captureDecision;

	/**
	 * Entry point.
	 * 
	 * @param args
	 *            command-line arguments. The name of the property file may be
	 *            passed as a command-line argument. If not passed, it will look
	 *            for "cybs.properties" in the current directory.
	 */
	public static void main(String[] args) {

		String argument=args[0];
		Properties props = readProperty(argument + ".properties");
		@SuppressWarnings("unchecked")
		
		String requestID;
		String emvrequestID;

		Properties cybProperties = new Properties();
		cybProperties = readCybsProperty(PROPERTIES);
		int choice =sample.enum_exist(argument);
		switch (choice) {
		
		case 0:
			requestID = runAuth(cybProperties);
			break;
		
		case 1:
			requestID = runAuth(cybProperties);
			System.out.println("Request ID is " + requestID);
			if (!("null".equals(requestID))) {
				requestID = runCapture(cybProperties, requestID, argument);
			}
			break;
		
		case 2:
			emvrequestID = runAuthEMV(cybProperties,argument);
			break;
		
		case 3:
			requestID = runAuth(cybProperties );
			System.out.println("Request ID is " + requestID);
			if (!("null".equals(requestID))) {
				requestID = runCapture(cybProperties, requestID,  argument);
				if (!("null".equals(requestID)))
					runCredit(cybProperties, requestID, argument);
			}
			break;

		case 4:
			requestID = runAuth(cybProperties);
			if (!("null".equals(requestID))) {
				runAuthReversal(cybProperties, requestID, argument);
			}
			break;
		case 5:
			runSale(cybProperties);
			break;
		case -1:
			System.out.println(" \t\t Enter the correct Service_name\n\n "+
					"\t\t while running the Script enter the service_name \n\n"+
					"\t\t for example to run a auth transaction enter the following command: \n"+
					"\t\t\t  for windows : runSample <service_name> \n "+
					"\t\t\t  for linux : ./runSample.sh <service_name>  \n"+
					"\t\t service name argument can be auth,sale,credit,authreversal,capture,emv_auth \n"+
					"\t\t NOTE: if no argument is entered the script will terminate the program");
			break;
		default:
			break;

		}
	}
	public static String runSale(Properties props) {
		String requestID = null;
		Properties saleProps = new Properties();
		saleProps = readProperty("sale.properties");
		HashMap<String, String> request = new HashMap<String, String>(
				(Map) saleProps);
		try {
			displayMap("CREDIT CARD SALE REQUEST:", request);
			// run transaction now
			Map<String, String> reply = Client.runTransaction(request, props);
			displayMap("CREDIT CARD SALE REPLY:", reply);
				requestID = (String) reply.get("requestID");
				System.out.println("Sale completed " +requestID);

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
	 * @param props
	 *            Properties object.
	 * @return the requestID.
	 */
	
	public static String runAuth(Properties props) {
		String requestID = null;
		Properties authProps = new Properties();
		authProps = readProperty("auth.properties");
		HashMap<String, String> request = new HashMap<String, String>(
				(Map) authProps);
		try {
			displayMap("CREDIT CARD AUTHORIZATION REQUEST:", request);
			// run transaction now
			Map<String, String> reply = Client.runTransaction(request, props);
			displayMap("CREDIT CARD AUTHORIZATION REPLY:", reply);
			// if the authorization was successful, obtain the request id
			// for the follow-on capture later.
			String decision = (String) reply.get("decision");
			if ("ACCEPT".equalsIgnoreCase(decision)) {
				requestID = (String) reply.get("requestID");
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

		return (requestID);
	}

	/**
	 * Runs Credit Card Capture.
	 * 
	 * @param props
	 *            Properties object.
	 * @param authRequestID
	 *            requestID returned by a previous authorization.
	 */
	public static String runCapture(Properties props, String authRequestID,String propFileName ) {
		Properties captureProps = new Properties();
		captureProps = readProperty("capture.properties");
		HashMap<String, String> request = new HashMap<String, String>(
				(Map) captureProps);
		
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
	 * @param request
	 */
	public static void runAuthReversal(Properties props, String authRequestID, String propFileName) {
		Properties authReversalProps = new Properties();
		authReversalProps = readProperty(propFileName+".properties");
		HashMap<String, String> request = new HashMap<String, String>(
				(Map) authReversalProps);
		request.put("ccAuthReversalService_authRequestID", authRequestID);

		try {
			displayMap("REVERSAL REQUEST:", request);
			System.out.println("auth reversal");
			// run transaction now
			Map<String, String> reply = Client.runTransaction(request, props);
			displayMap("REVERSAL REPLY:", reply);
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

	public static String runAuthEMV(Properties props, String propFileName) {
		Properties authEMVProps = new Properties();
		String emvRequestID = null;
		authEMVProps = readProperty(propFileName+".properties");
		HashMap<String, String> request = new HashMap<String, String>(
				(Map) authEMVProps);
		
		
		try {
			displayMap("CREDIT CARD EMVAUTHORIZATION REQUEST:", request);
			// run transaction now
			Map<String, String> reply = Client.runTransaction(request, props);
			displayMap("CREDIT CARD EMVAUTHORIZATION REPLY:", reply);
			// if the authorization was successful, obtain the request id
			// for the follow-on capture later.
			String decision = (String) reply.get("decision");
			if ("ACCEPT".equalsIgnoreCase(decision)) {
				emvRequestID = (String) reply.get("requestID");
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

		return (emvRequestID);

	}

	public static void runCredit(Properties props, String RequestID, String propFileName) {
		Properties creditProps = new Properties();
		creditProps = readProperty(propFileName+".properties");
		HashMap<String, String> request = new HashMap<String, String>(
				(Map) creditProps);
		request.put("ccCreditService_captureRequestID",RequestID);
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
	 * @param header
	 *            Header text.
	 * @param map
	 *            Map object to display.
	 */
	private static void displayMap(String header, Map map) {
		System.out.println(header);

		StringBuffer dest = new StringBuffer();

		if (map != null && !map.isEmpty()) {
			Iterator iter = map.keySet().iterator();
			String key, val;
			while (iter.hasNext()) {
				key = (String) iter.next();
				val = (String) map.get(key);
				dest.append(key + "=" + val + "\n");
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
	 * @param e
	 *            Critical ClientException object.
	 * @param request
	 *            Request that was sent.
	 */
	private static void handleCriticalException(ClientException e, Map request) {
		// send the exception and order information to the appropriate
		// personnel at your company using any suitable method, e.g. e-mail,
		// multicast log, etc.
	}

	/**
	 * See header comment in the other version of handleCriticalException above.
	 * 
	 * @param e
	 *            Critical ClientException object.
	 * @param request
	 *            Request that was sent.
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
		}

		return (props);
	}

	public static Properties readCybsProperty(String file) {
		Properties cybsProps = new Properties();
		try {
			String filename = file;
			FileInputStream fis = new FileInputStream(filename);
			cybsProps.load(fis);
			fis.close();
		} catch (IOException ioe) {
		}
		return (cybsProps);
	}

}
