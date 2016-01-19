/* Copyright 2003-2004 CyberSource Corporation */

package com.cybersource.sample;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import com.cybersource.ws.client.*;

/**
 * Sample class that demonstrates how to call Credit Card Authorization using
 * the XML client.
 */
public class AuthSample
{
	/**
	 * Entry point.
	 *
	 * @param args	command-line arguments. The name of the property file
	 *              followed by the name of the input XML file may be passed
	 *				as command-line arguments.  If not passed, it will look for
	 *				"cybs.properties" and "auth.xml", respectively in the
	 *				current directory.
	 */
    public static void main( String[] args )
   	{   	
	   	// read in properties file.
	   	Properties props = Utility.readProperties( args );   	
	   	
	   	// read in input XML file, replacing _NSURI_ (if any) with the
	   	// effective namespace URI.  See header comment for the method
	   	// readRequest() for more information.
		Document request = readRequest( props, args );		
		if (request == null) return;
   				
		// The sample auth.xml does not have the merchantID element.  We will
		// let the XMLClient get the merchantID from props and insert it into
		// the request document.
		
		try
		{
			displayDocument(
				"CREDIT CARD AUTHORIZATION REQUEST:", request );
			
			// run transaction now
			Document reply = XMLClient.runTransaction( request, props );	
			
			displayDocument(
				"CREDIT CARD AUTHORIZATION REPLY:", reply );			
		}	
		catch (ClientException e)
		{
			System.out.println( e.getMessage() );
			if (e.isCritical())
			{
				handleCriticalException( e, request );
			}
		}
		catch (FaultException e)
		{
			System.out.println( e.getMessage() );
			if (e.isCritical())
			{
				handleCriticalException( e, request );
			}
		}		
    }
    
	/**
	 * Reads the input XML file.  It replaces "_NSURI_" (if any) with the
	 * effective namespace URI derived from the Properties.  The sample file
	 * auth.xml included in the package has this placeholder.  This is so that
	 * you would only need to change the properties file in order to test this
	 * sample.  In your own application, you would likely have the correct
	 * namespace URI already set in your input XML documents and therefore
	 * would not need to do this.
	 *
	 * @param props				the Properties object to be used to derive
	 *                          the effective namespace URI.
	 * @param commandLineArgs	the command-line arguments.
	 *
	 * @return Document object.
	 */
    private static Document readRequest(
    	Properties props, String[] commandLineArgs )
    {
	    Document doc = null;
	    
	    try
	   	{
		   	// read in the XML file
		   	String filename
		   		= commandLineArgs.length > 1 ? commandLineArgs[1] : "auth.xml";
		   	byte[] xmlBytes = Utility.read( filename );
		   	
		   	// replace _NSURI_ (if any) with effective namespace URI.
		   	String xmlString = new String( xmlBytes, "UTF-8" );
		   	int pos = xmlString.indexOf( "_NSURI_" );
		   	if (pos != -1)
		   	{
		   		StringBuffer sb	= new StringBuffer( xmlString );
		   		sb.replace(
		   			pos, pos + 7,
		   			XMLClient.getEffectiveNamespaceURI( props, null ) );
		   		xmlBytes = sb.toString().getBytes( "UTF-8" );
	   		}
	   	
	   		// load the byte array into a Document object.
	   		ByteArrayInputStream bais = new ByteArrayInputStream( xmlBytes );		   	
	   		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	   		dbf.setNamespaceAware( true );
	   		DocumentBuilder builder = dbf.newDocumentBuilder();
	   		doc = builder.parse( bais );
	   		bais.close();
   		}
   		catch (ClientException e)
   		{
	   		e.printStackTrace();
   		}
   		catch (ParserConfigurationException e)
   		{
	   		e.printStackTrace();
   		}
   		catch (SAXException e)
   		{
	   		e.printStackTrace();
   		}
   		catch (IOException e)
   		{
	   		e.printStackTrace();
   		}
   		
   		return( doc );
	}

	/**
	 * Displays the content of the Document object.
	 *
	 * @param header	Header text.
	 * @param doc		Document object to display.
	 */
    private static void displayDocument( String header, Document doc )
    {
	    System.out.println( header );
	    
		// Note that Utility.nodeToString() is meant to be used for logging
		// or demo purposes only.  As it employs some formatting
		// parameters, parsing the string it returns may not result to a
		// Node object exactly similar to the one passed to it.
		System.out.println( Utility.nodeToString( doc ) );
    }		
	    
	/**
	 * An exception is considered critical if some type of disconnect occurs
	 * between the client and server and the client can't determine whether the
	 * transaction was successful. If this happens, you might have a
	 * transaction in the CyberSource system that your order system is not
	 * aware of. Because the transaction may have been processed by
	 * CyberSource, you should not resend the transaction, but instead send the
	 * exception information and the order information (customer name, order
	 * number, etc.) to the appropriate personnel at your company to resolve
	 * the problem. They should use the information as search criteria within
	 * the CyberSource Transaction Search Screens to find the transaction and
	 * determine if it was successfully processed. If it was, you should update
	 * your order system with the transaction information. Note that this is
	 * only a recommendation; it may not apply to your business model.
	 *
	 * @param e			Critical ClientException object.
	 * @param request	Request that was sent.
	 */
	private static void handleCriticalException(
		ClientException e, Document request )
	{
		// send the exception and order information to the appropriate
		// personnel at your company using any suitable method, e.g. e-mail,
		// multicast log, etc.
	}
	
	/**
	 * See header comment in the other version of handleCriticalException
	 * above.
	 *
	 * @param e			Critical ClientException object.
	 * @param request	Request that was sent.
	 */
	private static void handleCriticalException(
		FaultException e, Document request )
	{
		// send the exception and order information to the appropriate
		// personnel at your company using any suitable method, e.g. e-mail,
		// multicast log, etc.
	}    
} 

