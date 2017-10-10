/* Copyright 2003-2004 CyberSource Corporation */

package com.cybersource.sample;


import java.util.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.FileInputStream;
import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import com.cybersource.ws.client.*;

/**
 * Sample class that demonstrates how to call Credit Card Authorization using
 * the XML client.
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
	/**
	 * Entry point.
	 *
	 * @param args
	 *            it takes the kind of transaction that has to 
	 *            be run. For ex- auth, sale
	 */
	public static void main(String[] args) {
		String argument = args[0];
		Properties props = readCybsProperty();
		String requestID;
		String decision;
		Document authReply;
		Document captureReply;
		int choice =sample.enum_exist(argument);

		switch (choice) {
		
		case 0:
			authReply = runAuth(props, "auth");
			if(authReply==null)
				break;
			requestID = getRequestID(authReply);
			decision = getDecisonCode(authReply);
			break;
			
		case 1:
			authReply = runAuth(props, "auth");
			if(authReply==null)
				break;
			requestID = getRequestID(authReply);
			decision = getDecisonCode(authReply);
			if (!(("null".equals(requestID)) && (decision
					.equalsIgnoreCase("Decline")))) {
				runCapture(props, requestID, "capture");
			}
			break;
			
		case 2:
			authReply = runAuthEMV(props, "emvauth");
				if(authReply==null)
					break;
			requestID = getRequestID(authReply);
			decision = getDecisonCode(authReply);
			break;
			
		case 3:
			authReply = runAuth(props, "auth");
				if(authReply==null)
				break;
			requestID = getRequestID(authReply);
			decision = getDecisonCode(authReply);
			captureReply = runCapture(props, requestID, "capture");
			requestID = getRequestID(captureReply);
			decision = getDecisonCode(captureReply);
			if (!(("null".equals(requestID)) && ("decline".equalsIgnoreCase(decision))))
				runCredit(props, requestID, "credit");
			break;

		case 4:
			authReply = runAuth(props, "auth");
				if(authReply==null)
				break;
			requestID = getRequestID(authReply);
			decision = getDecisonCode(authReply);
			if (!(("null".equals(requestID)) && "decline".equalsIgnoreCase(decision))) 
				runAuthReversal(props, requestID, "authReversal");
			break;
			
		case 5:
			runSale(props, "sale");
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

	private static void runSale(Properties props, String argument) {
		Document reply = null;
		Document request = readRequest(props, argument);
		try {

			displayDocument("CREDIT CARD SALE REQUEST:", request);
			// run transaction now
			reply = XMLClient.runTransaction(request, props);

			displayDocument("CREDIT CARD SALE REPLY:", reply);
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

	private static void runAuthReversal(Properties props, String requestID,
			String argument) {
		Document reply = null;
		Document request = readRequest(props, argument);
		try {
			displayDocument("CREDIT CARD AUTHREVERSAL REQUEST:", request);

			request = appendRequest(request, requestID, "authRequestID");
			displayDocument("CREDIT CARD AUTHREVERSAL REQUEST:", request);
			// run transaction now
			reply = XMLClient.runTransaction(request, props);

			displayDocument("CREDIT CARD AUTHREVERSAL REPLY:", reply);
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

	public static void runCredit(Properties props, String requestID,
			String argument) {
		Document reply = null;
		Document request = readRequest(props, argument);
		try {
			displayDocument("CREDIT CARD CREDIT REQUEST:", request);

			request = appendRequest(request, requestID, "captureRequestID");
			displayDocument("CREDIT CARD AUTHORIZATION REQUEST:", request);
			// run transaction now
			reply = XMLClient.runTransaction(request, props);

			displayDocument("CREDIT CARD CREDIT REPLY:", reply);
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

	public static Document runAuthEMV(Properties props, String argument) {
		Document reply = null;
		Document request = readRequest(props, argument);
		try {

			displayDocument("CREDIT CARD EMVAUTHORIZATION REQUEST:", request);
			// run transaction now
			reply = XMLClient.runTransaction(request, props);

			displayDocument("CREDIT CARD EMVAUTHORIZATION Reply:", reply);
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

		return reply;
	}

	public static Document runAuth(Properties props, String argument) {
		Document reply = null;
		Document request = readRequest(props, argument);
		try {
			displayDocument("CREDIT CARD AUTHORIZATION REQUEST:", request);

			// run transaction now
			reply = XMLClient.runTransaction(request, props);
			displayDocument("CREDIT CARD AUTHORIZATION authReply:", reply);
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

		return reply;
	}

	public static Document runCapture(Properties props, String requestID,
			String argument) {
		Document reply = null;
		Document request = readRequest(props, argument);
		try {
			displayDocument("CREDIT CARD CAPTURE REQUEST:", request);

			request = appendRequest(request, requestID, "authRequestID");
			displayDocument("CREDIT CARD CAPTURE REQUEST:", request);
			// run transaction now
			reply = XMLClient.runTransaction(request, props);

			displayDocument("CREDIT CARD CAPTURE Reply:", reply);
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
		return reply;

	}

	/**
	 * This method appends the requestID field and returns the request object
	 * @param request
	 * @param requestID
	 * @param fieldName
	 * @return
	 */
	public static Document appendRequest(Document request, String requestID,
			String fieldName) {

		request.getDocumentElement().normalize();
		System.out.println("----------------------------");
		NodeList nl = request.getElementsByTagName(fieldName);
		Element e = (Element) nl.item(0);
		// requestID=e.getTextContent();
		System.out.println("REQUEST ID  -- " + e.getTextContent());
		e.setTextContent(requestID);
		System.out.println("REQUEST ID  -- " + e.getTextContent());
		return request;
	}

	/**
	 * @param message
	 * 		Accepts the document object
	 * @return
	 * 		returns a String representing the RequestID
	 */
	public static String getRequestID(Document message) {
		// get the Request ID
		String requestID = null;
		message.getDocumentElement().normalize();
		System.out.println("----------------------------");
		NodeList nl = message.getElementsByTagName("c:requestID");
		Element e = (Element) nl.item(0);
		requestID = e.getTextContent();
		System.out.println("REQUEST ID  -- " + e.getTextContent());
		return requestID;
	}
	/**
	 * @param message
	 * 		Accepts the document object
	 * @return
	 * 		returns a String representing the Decision
	 */
	public static String getDecisonCode(Document message) {
		// get the Decision Code
		String decision = null;
		message.getDocumentElement().normalize();
		System.out.println("----------------------------");
		NodeList nl = message.getElementsByTagName("c:decision");
		Element e = (Element) nl.item(0);
		decision = e.getTextContent();
		System.out.println("DECISION -- " + e.getTextContent());
		return decision;
	}

	/**
	 * Reads the input XML file. It replaces "_NSURI_" (if any) with the
	 * effective namespace URI derived from the Properties. The sample files
	 * included in the package has this placeholder. This is so that
	 * you would only need to change the properties file in order to test this
	 * sample. In your own application, you would likely have the correct
	 * namespace URI already set in your input XML documents.
	 *
	 * @param props
	 *            the Properties object to be used to derive the effective
	 *            namespace URI.
	 * @param commandLineArgs
	 *            the command-line arguments. The input xml file has the same 
	 *            name as the service name which we have passed in the main method.
	 *
	 * @return Document object.
	 */
	private static Document readRequest(Properties props, String commandLineArgs) {
		Document doc = null;

		try {
			// read in the XML file
			String filename = commandLineArgs + ".xml";
			byte[] xmlBytes = Utility.read(filename);

			// replace _NSURI_ (if any) with effective namespace URI.
			String xmlString = new String(xmlBytes, "UTF-8");
			int pos = xmlString.indexOf("_NSURI_");
			if (pos != -1) {
				StringBuffer sb = new StringBuffer(xmlString);
				sb.replace(pos, pos + 7,
						XMLClient.getEffectiveNamespaceURI(props, null));
				xmlBytes = sb.toString().getBytes("UTF-8");
			}

			// load the byte array into a Document object.
			ByteArrayInputStream bais = new ByteArrayInputStream(xmlBytes);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder builder = dbf.newDocumentBuilder();
			doc = builder.parse(bais);
			bais.close();
		} catch (ClientException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return (doc);
	}

	/**
	 * Displays the content of the Document object.
	 *
	 * @param header
	 *            Header text.
	 * @param doc
	 *            Document object to display.
	 */
	private static void displayDocument(String header, Document doc) {
		System.out.println(header);

		// Note that Utility.nodeToString() is meant to be used for logging
		// or demo purposes only. As it employs some formatting
		// parameters, parsing the string it returns may not result to a
		// Node object exactly similar to the one passed to it.
		System.out.println(Utility.nodeToString(doc));
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
	private static void handleCriticalException(ClientException e,
			Document request) {
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
	private static void handleCriticalException(FaultException e,
			Document request) {
		// send the exception and order information to the appropriate
		// personnel at your company using any suitable method, e.g. e-mail,
		// multicast log, etc.
	}
	
	/**
	 * This method will read the Cybs property file
	 * @return
	 * 		returns the property object
	 */
	public static Properties readCybsProperty() {
		Properties cybsProps = new Properties();
		try {
			String filename = "cybs.properties";
			FileInputStream fis = new FileInputStream(filename);
			cybsProps.load(fis);
			fis.close();
		} catch (IOException ioe) {
		}
		return (cybsProps);
	}
}

