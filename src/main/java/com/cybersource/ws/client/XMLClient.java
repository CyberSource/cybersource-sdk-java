/* Copyright 2003-2004 CyberSource Corporation */

package com.cybersource.ws.client;

import java.io.*;
import java.net.*;
import java.util.Properties;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * Class containing runTransaction() methods that accept the requests in the
 * form of a Document object.
 */
public class XMLClient
{
	private static final String SOAP_ENVELOPE = 
			"<soap:Envelope xmlns:soap=\"" + 
			"http://schemas.xmlsoap.org/soap/envelope/" +
			"\"><soap:Body></soap:Body></soap:Envelope>";
			
	private static final String ELEM_REQUEST_MESSAGE = "requestMessage";
	private static final String ELEM_REPLY_MESSAGE = "replyMessage";
	private static final String ELEM_MERCHANT_ID = "merchantID";
	private static final String ELEM_MERCHANT_REFERENCE_CODE 
													= "merchantReferenceCode";
	private static final String ELEM_CLIENT_LIBRARY = "clientLibrary";
	private static final String ELEM_CLIENT_LIBRARY_VERSION 
													= "clientLibraryVersion";
	private static final String ELEM_CLIENT_ENVIRONMENT = "clientEnvironment";
	
	private static final String[] VERSION_FIELDS
	 	= { ELEM_CLIENT_LIBRARY,
	 	  	ELEM_CLIENT_LIBRARY_VERSION,
	 	  	ELEM_CLIENT_ENVIRONMENT };
		
	private static Document soapEnvelope = null;
	private static Exception initException = null;
			
	static
	{
		try
		{
			// load the SOAP envelope document.
	   		DocumentBuilder builder = Utility.newDocumentBuilder();
	   		StringReader sr = new StringReader( SOAP_ENVELOPE );
	   		soapEnvelope = builder.parse( new InputSource( sr ) );   				
	   		sr.close();
   		}
   		catch (ParserConfigurationException e)
   		{
	   		initException = e;
   		}
   		catch (SAXException e)
   		{
	   		initException = e;
   		}
   		catch (IOException e)
   		{
	   		initException = e;
   		}
	}
			
	/**
	 * Returns the effective namespace URI for the specified merchant id.
	 * Refer to <code>MerchantConfig.getProperty()</code> for the search
	 * behavior.  This method is provided so that the sample application
	 * can dynamically plug the correct namespace URI into the sample XML
	 * inputs.  You do not need to call it if you have the namespace URI
	 * hardcoded in your XML documents.
	 *
	 * @param props			Properties object to look up the properties in.
	 * @param merchantID	merchant ID whose effective namespace URI is wanted.
	 *                      It may be null, in which case, the generic effective
	 *						namespace URI is returned.
	 *
	 * @throws ClientException if a ConfigException occurs.  Call
	 *                         <code>getInnerException()</code> to get at the
	 *                         ConfigException.
	 */
	public static String getEffectiveNamespaceURI(
		Properties props, String merchantID )
		throws ClientException
	{
		try
		{
			MerchantConfig mc = new MerchantConfig( props, merchantID );
			return( mc.getEffectiveNamespaceURI() );
		}
		catch (ConfigException ce)
		{
			throw new ClientException( ce, false, null );
		}
	} 
	
	/**
	 * Runs a transaction.
	 *
	 * @param request	request to send.
	 * @param props		properties the client needs to run the transaction.
	 *					See README for more information.
	 *
	 * @throws FaultException  if a fault occurs.
	 * @throws ClientException if any other exception occurs.
	 */
	public static Document runTransaction( Document request, Properties props )
		throws FaultException, ClientException
	{
		return( runTransaction(
					request, props, null, true, true ) );  		
    }
    
	/**
	 * Runs a transaction.
	 *
	 * @param request	    request to send.
	 * @param props		    properties the client needs to run the transaction.
	 *					    See README for more information.
	 * @param _logger		Logger object to used for logging.
	 * @param prepare   	Flag as to whether or not the logger's
	 *                      prepare() method should be called.
	 * @param logTranStart  Flag as to whether or not the logger's
	 *                      logTransactionStart() method should be called.
	 *
	 * @throws FaultException  if a fault occurs.
	 * @throws ClientException if any other exception occurs.
	 */
	public static Document runTransaction(
		Document request, Properties props,
		Logger _logger, boolean prepare, boolean logTranStart )
 		throws FaultException, ClientException
	{
		if (initException != null)
		{
			throw new ClientException( initException, false, null );
		}
		
		String nsURI;
		MerchantConfig mc;	
		LoggerWrapper logger = null;
		Connection con = null;
		
		try
		{
			// At this point, we do not know what namespace to use yet so
			// we locate the first merchantID element with any namespace
			// (actually, there should be just one.  Otherwise, there's
			// something wrong with their XML request).
			String merchantID 
				= Utility.getElementText( request, ELEM_MERCHANT_ID, "*" );
			if (merchantID == null)
			{
				// if no merchantID is present in the request, get its
				// value from the properties and add it to the request.
				mc = new MerchantConfig( props, null );
				merchantID = mc.getMerchantID();
				nsURI = mc.getEffectiveNamespaceURI();
				setMerchantID( request, merchantID, nsURI );
			}
			else
			{
				mc = new MerchantConfig( props, merchantID );
				nsURI = mc.getEffectiveNamespaceURI();
			}
			
			logger = new LoggerWrapper( _logger, prepare, logTranStart, mc );
			
			setVersionInformation( request, nsURI );
			
		   	DocumentBuilder builder = Utility.newDocumentBuilder();
		   	
	    		Document signedDoc
	    	   	   = soapWrapAndSign( request, mc, builder, logger );
			
			con = Connection.getInstance( mc, builder, logger );
			Document wrappedReply = con.post( signedDoc );

			return( soapUnwrap( wrappedReply, mc, builder, logger ) );
  		}
  		catch (ParserConfigurationException e)
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
	 * Creates an Element object in the CyberSource namespace.
	 *
	 * @param owner					Document object to own the Element object.
	 & @param nsURI					Namespace URI to use.
	 * @param elementName			local name of Element object to create.
	 * @param textValue				text value of the new Element object.
	 *
	 * @return the newly created Element object.
	 */
    private static Element createElement(
    	Document owner, String nsURI, String elementName, String textValue )
    {
	    Attr attr
		= owner.createAttributeNS(
			"http://www.w3.org/2000/xmlns/", "xmlns" );
	    attr.setValue( nsURI );
	    Element elem 
	    	= owner.createElementNS( nsURI, elementName );
	    elem.setAttributeNodeNS( attr );
	    
	    elem.appendChild( owner.createTextNode( textValue ) );
	    return( elem );
    }
       

	/**
	 * Sets the merchantID in the request.
	 *
	 * @param request	 request to add the merchantID to.
	 * @param merchantID merchantID to add to request.
	 * @param nsURI 	 namespace URI to use.
	 */
	private static void setMerchantID(
		Document request, String merchantID, String nsURI )
	{
		// create merchantID node
		Element merchantIDElem
			= createElement( request, nsURI, ELEM_MERCHANT_ID, merchantID );
			
		// add it as the first child of the requestMessage element.
		Element requestMessage 
			= Utility.getElement( request, ELEM_REQUEST_MESSAGE, nsURI );
		requestMessage.insertBefore(
			merchantIDElem, requestMessage.getFirstChild() );
	}
		
    
    /**
	 * Sets the version information in the request.
	 *
	 * @param request	request to set the version information in.
	 * @param nsURI		namespaceURI to use.
	 */
	private static void setVersionInformation( Document request, String nsURI )
	{	
		//
		// First, delete the version fields currently in the request document,
		// if any.
		//
		
		// get the requestMessage element				
		Element requestMessage 
			= Utility.getElement( request, ELEM_REQUEST_MESSAGE, nsURI );
		
		// get the node that is supposed to precede the version fields,
		// which is merchantReferenceCode
		Element previous
			= Utility.getElement(
				request, ELEM_MERCHANT_REFERENCE_CODE, nsURI );
			
		// if it does not exist, look for the node that is supposed to precede
		// merchantReferenceCode, which is merchantID
		if (previous == null)
		{
			previous = Utility.getElement( request, ELEM_MERCHANT_ID, nsURI );
		}
		
		Node currElem, save;
		
		// if either the merchantReferenceCode or merchantID exists, its
		// next sibling (which may be null) becomes the current element
		if (previous != null)
		{
			currElem = previous.getNextSibling();
		}
		// else, if neither exists, the first child of requestMessage (which
		// may be null) becomes the current element
		else
		{
			currElem = requestMessage.getFirstChild();
		}
		
		// for each of the version fields...
		for (int i = 0; i < VERSION_FIELDS.length; ++i)
		{
			// if the current element is not null, compare it with
			// the current version field in the loop
			if (currElem != null)
			{
				// if they match, save the element next to it, delete
				// the current element and make the saved element the current
				// element
				if (VERSION_FIELDS[i].equals( currElem.getNodeName() ))
				{
					save = currElem.getNextSibling();
					requestMessage.removeChild( currElem );
					currElem = save;
				}
			}
			
			// else, if the current element is null, it means we have reached
			// the end of the requestMessage (parent) element.
			else
			{
				break;
			}
		}
		
		// create DocumentFragment for the version-related fields
		DocumentFragment versionsFragment = request.createDocumentFragment();
   		versionsFragment.appendChild( createElement(
   			request, nsURI, ELEM_CLIENT_LIBRARY, "Java XML" ) );
   		versionsFragment.appendChild( createElement(
   			request, nsURI, ELEM_CLIENT_LIBRARY_VERSION, Utility.VERSION ) );
   		versionsFragment.appendChild( createElement(
   			request, nsURI, ELEM_CLIENT_ENVIRONMENT, Utility.ENVIRONMENT ) );
			
		// if the current element is not null, it will be the sibling right
		// next to the version fields.
		if (currElem != null)
		{
			requestMessage.insertBefore( versionsFragment, currElem );
		}
		// else, if the current element is null, the version fields will be
		// added at the end of the requestMessage.
		else
		{
			requestMessage.appendChild( versionsFragment );
		}
	}		

	/**
	 * Wraps the given Map object in SOAP envelope and signs it.
	 *
	 * @param doc			Document object containing the request.
	 * @param mc			MerchantConfig object.
	 * @param db			DocumentBuilder object.
	 * @param logger		LoggerWrapper object to use for logging.
	 * 
	 * @return signed document.
	 *
	 * @throws ParserConfigurationException if no suitable parser
	 *                                      implementation is found.
	 * @throws SignException				if signing fails.
	 * @throws ConfigException				if something in the configuration
	 *                                      is missing or invalid.
	 */
	private static Document soapWrapAndSign(
		Document doc, MerchantConfig mc, DocumentBuilder builder,
		LoggerWrapper logger )
		throws ParserConfigurationException, SignException, ConfigException
	{
		boolean logSignedData = mc.getLogSignedData();

		if (!logSignedData) {
			logger.log( Logger.LT_REQUEST,
			            Utility.nodeToString( doc, PCI.REQUEST ) );
		}

		// look for the requestMessage element
		Element requestMessage
			= Utility.getElement(
				doc, ELEM_REQUEST_MESSAGE, mc.getEffectiveNamespaceURI() );
				
		// wrap in SOAP envelope
								
	   	Document wrappedDoc = builder.newDocument();

		wrappedDoc.appendChild(
			wrappedDoc.importNode( soapEnvelope.getFirstChild(), true ) );

		if (requestMessage != null)
		{
			wrappedDoc.getFirstChild().getFirstChild().appendChild(
				wrappedDoc.importNode( requestMessage, true ) );
		}

	   	// sign wrapped Document object
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
	 * @param db			DocumentBuilder object.
	 * @param logger		LoggerWrapper object to use for logging.
	 * 
	 * @return content of SOAP body as a Document object.
	 *
	 * @throws ParserConfigurationException if no suitable parser
	 *                                      implementation is found.
	 */
	private static Document soapUnwrap(
		Document doc, MerchantConfig mc, DocumentBuilder builder,
		LoggerWrapper logger )
		throws ParserConfigurationException
	{
		boolean logSignedData = mc.getLogSignedData();
		if (logSignedData) {
			logger.log( Logger.LT_REPLY,
			            Utility.nodeToString( doc, PCI.REPLY ) );
		}

		// look for the replyMessage element
		Node replyMessage 
			= Utility.getElement(
				doc, ELEM_REPLY_MESSAGE, mc.getEffectiveNamespaceURI() );

		// extract it out into a new Document object
				
	   	Document unwrappedDoc = builder.newDocument();

		if (replyMessage != null)
		{
			unwrappedDoc.appendChild(
				unwrappedDoc.importNode( replyMessage, true ) );
		}
		
		if (!logSignedData)
		{
			logger.log(
				Logger.LT_REPLY,
				Utility.nodeToString( unwrappedDoc, PCI.REPLY ) );
		}
		
		return( unwrappedDoc );
	}	
} 


