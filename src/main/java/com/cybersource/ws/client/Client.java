/* Copyright 2003-2004 CyberSource Corporation */

package com.cybersource.ws.client;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * Class containing runTransaction() methods that accept the requests in the
 * form of a Map object.
 */
public class Client
{
	private static final String SOAP_ENVELOPE
		= "<soap:Envelope xmlns:soap=\"" + 
		  "http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body>\n" +
		  "<nvpRequest xmlns=\"{0}\">\n{1}</nvpRequest>" +
		  "\n</soap:Body></soap:Envelope>";
			
	private static final String ELEM_NVP_REPLY = "nvpReply";
	
	private static final String MERCHANT_ID = "merchantID";
	
	/**
	 * If in the Request map, a key called "_has_escapes" is present and is set
	 * to "1", we will not escape the XML special characters.  Basically, the
	 * merchant is saying that they have escaped the characters themselves.
	 * This might prove useful for more advanced users of the Basic client.
	 */
    private static final String HAS_ESCAPES = "_has_escapes";
     
	/**
	 * Runs a transaction.
	 *
	 * @param request	request to send.
	 * @param props		properties the client needs to run the transaction.
	 *					See README for more information.
	 *
	 * @throws FaultException	if a fault occurs.
	 * @throws ClientException  if any other exception occurs.
	 */
	public static HashMap runTransaction( Map request, Properties props )
		throws FaultException, ClientException
	{
		return( runTransaction(
					request, props, null, true, true ) );
    }
    
	/**
	 * Runs a transaction.
	 *
	 * @param request		request to send.
	 * @param props			properties the client needs to run the transaction.
	 *						See README for more information.
	 * @param _logger		Logger object to used for logging.
	 * @param prepare   	Flag as to whether or not the logger's
	 *                      prepare() method should be called.
	 * @param logTranStart  Flag as to whether or not the logger's
	 *                      logTransactionStart() method should be called.
	 *
	 * @throws FaultException	if a fault occurs.
	 * @throws ClientException if any other exception occurs.
	 */
	public static HashMap runTransaction(
		Map request, Properties props,
		Logger _logger, boolean prepare, boolean logTranStart )
		throws FaultException, ClientException
	{
		MerchantConfig mc;
		LoggerWrapper logger = null;
		Connection con = null;
		
		try
		{
			setVersionInformation( request );
			
			String merchantID = (String) request.get( MERCHANT_ID );
			if (merchantID == null)
			{
				// if no merchantID is present in the request, get its
				// value from the properties and add it to the request.
				mc = new MerchantConfig( props, null );
				merchantID = mc.getMerchantID();
				request.put( MERCHANT_ID, merchantID );
			}
			else
			{
				mc = new MerchantConfig( props, merchantID );
			}
			
			logger = new LoggerWrapper( _logger, prepare, logTranStart, mc );
			
		   	DocumentBuilder builder = Utility.newDocumentBuilder();
		   	
	    		Document signedDoc 
	    		   = soapWrapAndSign( request, mc, builder, logger );
	    		
			con = Connection.getInstance( mc, builder, logger );
			Document wrappedReply = con.post( signedDoc );
		
	  		return( soapUnwrap( wrappedReply, mc, logger ) );
  		}
  		catch (IOException e)
  		{
	  		throw new ClientException(
				e, con != null && con.isRequestSent(), logger );
  		}
  		catch (ParserConfigurationException e)
  		{
	  		throw new ClientException(
				e, con != null && con.isRequestSent(), logger );
  		}
  		catch (SAXException e)
  		{
	  		throw new ClientException(
				e, con != null && con.isRequestSent(), logger );
  		}
		catch (SignException e)
		{
	  		throw new ClientException(
				e, con != null && con.isRequestSent(), logger );
		}
		catch (ConfigException e)
		{
	  		throw new ClientException(
				e, con != null && con.isRequestSent(), logger );
		}
		finally {
			if (con != null) {
				con.release();
			}
		}
	}
	    
	/**
	 * Sets the version information in the request.
	 *
	 * @param request	request to set the version information in.
	 */
	private static void setVersionInformation( Map request )
	{
		request.put( "clientLibrary", "Java Basic" );
		request.put( "clientLibraryVersion", Utility.VERSION );
		request.put( "clientEnvironment", Utility.ENVIRONMENT );
	}


	/**
	 * Wraps the given Map object in SOAP envelope and signs it.
	 *
	 * @param request		Map object containing the request.
	 * @param mc			MerchantConfig object.
	 * @param db			DocumentBuilder object.
	 * @param logger		LoggerWrapper object to use for logging.
	 * 
	 * @return signed document.
	 *
	 * @throws ParserConfigurationException if no suitable parser
	 *                                      implementation is found.
	 * @throws SAXException					if parsing fails.
	 * @throws IOException					if reading from string fails.
	 * @throws SignException				if signing fails.
	 * @throws ConfigException				if something in the configuration
	 *                                      is missing or invalid.
	 */
	private static Document soapWrapAndSign(
		Map request, MerchantConfig mc, DocumentBuilder builder,
		LoggerWrapper logger )
		throws ParserConfigurationException, SAXException,
			   IOException, SignException, ConfigException
	{
		boolean logSignedData = mc.getLogSignedData();
		if (!logSignedData) {
			logger.log(
				Logger.LT_REQUEST,
				mapToString( request, true, PCI.REQUEST ) );
		}
		
		// wrap in SOAP envelope
		Object[] arguments
			= { mc.getEffectiveNamespaceURI(),
				mapToString( request, false, PCI.REQUEST ) };	
		String xmlString = MessageFormat.format( SOAP_ENVELOPE, arguments );
		
		// load XML string into a Document object
		StringReader sr = new StringReader( xmlString );
	   	Document wrappedDoc = builder.parse( new InputSource( sr ) );
	   	sr.close();
	   				
	   	// sign Document object
		logger.log( Logger.LT_INFO, "Signing request..." );	   	
	   	Document signedDoc
	   		= Signature.sign(
	   			mc.getEffectivePassword(), mc.getKeyFile(), wrappedDoc );
	   	
		if (logSignedData) {
			logger.log( Logger.LT_REQUEST,
			            Utility.nodeToString( signedDoc, PCI.REQUEST ) );
		}
		
		return( signedDoc );		
	}
			
	/**
	 * Extracts the content of the SOAP body from the given Document object
	 * inside a SOAP envelope.
	 *
	 * @param doc	Document object to extract content from.
	 * @param mc			MerchantConfig object.
	 * @param logger		LoggerWrapper object to use for logging.
	 * 
	 * @return content of SOAP body as a Map object.
	 */
	private static HashMap soapUnwrap(
		Document doc, MerchantConfig mc, LoggerWrapper logger )
	{
		boolean logSignedData = mc.getLogSignedData();
		if (logSignedData) {
			logger.log( Logger.LT_REPLY,
			            Utility.nodeToString( doc, PCI.REPLY ) );
		}
			
		// look for the nvpReply element
		Node nvpReply
			= Utility.getElement(
				doc, ELEM_NVP_REPLY, mc.getEffectiveNamespaceURI() );
				
		Text nvpString = (Text) nvpReply.getFirstChild();
		
		String replyString = nvpString.getNodeValue();
		
		HashMap reply = stringToMap( replyString );
		
		if (!logSignedData)
		{
			logger.log(
				Logger.LT_REPLY, 
				mapToString( reply, true, PCI.REPLY ) );
		}
		
		return reply;
	}
	
	/**
	 * Converts the contents of a Map object into a string, one name-value pair
	 * to a line and the name and value are separated by an equal sign.
	 *
	 * @param src	Map object whose contents are being converted.
	 * @param mask	Flag whether or not to mask "unsafe" fields.
	 * @param type  Relevant only when mask is true, indicates whether this
	 *              is the request or the reply map.  Pass either
	 *				PCI.REQUEST or PCI.REPLY.
	 * 
	 * @return resulting string; will be empty if the Map object was null or
	 *         empty.
	 */
	private static String mapToString( Map src, boolean mask, int type )
	{
		StringBuffer dest = new StringBuffer();
		
		if (src != null && !src.isEmpty())
		{
			Iterator iter = src.keySet().iterator();
			String key, val;
			while (iter.hasNext())
			{
				key = (String) iter.next();
				val = mask ? PCI.maskIfNotSafe(type, key, (String) src.get(key))
				           : (String) src.get( key );
				dest.append( key + "=" + val + "\n" );
			}
		}
		
		String hasEscapes = (String) src.get( HAS_ESCAPES );
		
		// no need to escape the string if merchant had already escaped it
		// themselves.
		return( ("1".equals( hasEscapes ) ||
		         "true".equalsIgnoreCase( hasEscapes ))
				? dest.toString() : escapeXML( dest ) );
	}
	
	/**
	 * Escapes the XML special characters.
	 *
	 * @param unescaped	String to be escaped.
	 * 
	 * @return escaped string.
	 */
	private static String escapeXML( StringBuffer unescaped )
	{
		int length = unescaped.length();
		
		// pre-allocate extra 128 characters (I arbitrarily chose the number)
		// to avoid frequent reallocation.
	    StringBuffer escaped = new StringBuffer( length + 128 );
	    
	    char ch;	    
	    for (int i = 0; i < length; ++i)
	    {
	       ch = unescaped.charAt( i );
	       switch (ch)
	       {
		       case '<' : escaped.append( "&lt;" ); break;
		       case '>' : escaped.append( "&gt;" ); break;
		       case '&' : escaped.append( "&amp;" ); break;
		       case '"' : escaped.append( "&quot;" ); break;
		       case '\'': escaped.append( "&apos;" ); break;
		       default  : escaped.append( ch );
	     	}
	     }
	     
	     return( escaped.toString() );
    }
	

	/**
	 * Converts a name-value pair string into a Map object.
	 *
	 * @param src	String containing name-value pairs.
	 * 
	 * @return resulting Map object; will be empty if the string was null or
	 *         empty.
	 */
	private static HashMap stringToMap( String src )
	{
		HashMap dest = new HashMap();
		
		if (src == null)
		{
			return( dest );
		}
		
		String line, key, val;
		int equalsPos, newLinePos, startPos = 0, len = src.length();
		while (startPos < len)
		{
			newLinePos = src.indexOf( '\n', startPos );
			
			// if the last line does not end with a newline character,
			// assume an imaginary newline character at the end of the string
			if (newLinePos == -1)
			{
				newLinePos = len;
			}
			
			line = src.substring( startPos, newLinePos );
					
			equalsPos = line.indexOf( '=' );
			if (equalsPos != -1)
			{
				key = line.substring( 0, equalsPos );
				val = line.substring( equalsPos + 1 );
			}
			else
			{
				key = line;
				val = null;
			}
			
			dest.put( key, val );
			
			startPos = newLinePos + 1;
		}
		
		return( dest );
	}
} 

