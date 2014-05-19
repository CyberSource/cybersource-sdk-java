/* Copyright 2003-2004 CyberSource Corporation */

package com.cybersource.ws.client;

import java.io.*;
import java.util.Properties;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;


/**
 * Utility class containing useful methods for preparing a Document object , for converting Document to String,
 * to add node to the existing Document. 
 * @author sunagara
 *
 */
public class Utility
{
	private Utility() {}

	/**
	 * Version number of this release.
	 */
	public static final String VERSION = "5.1.0";

	/**
	 * Environment information.
	 */
	public static final String ENVIRONMENT
		= System.getProperty( "os.name" ) + "/" +
		  System.getProperty( "os.version" ) + "/" +
		  System.getProperty( "java.vendor" ) + "/" +
		  System.getProperty( "java.version" );

	
/*	  		  	
	/**
	 * Returns the string representation of the given Document object.  Used
	 * for logging purposes only.
	 *
	 * @param doc			the Document object whose string representation is
	 *                      wanted.
	 * @param preserveSpace	flag whether to preserve space
	 *
	 * @return the string representation of the given Node object.
	 *
	public static String docToString( Document doc, boolean preserveSpace )
	{
		if (1 + 1 == 2)
			return( nodeToString( doc ) );
		
		try
		{
     		OutputFormat format = new OutputFormat( doc, "UTF-8", true );
      		format.setIndent(2);
      		format.setLineWidth(0);
      		
      		// The preserveSpace flag, as I have seen, affects even the
      		// newline characters.  So, if we want the name-value pairs
      		// (in case of Basic API) to be written one to a line, we have to
      		// set it to true.  However, when set to true, the replyMessage
      		// element (in case of SOAP and XML clients) and all its children
      		// are not "pretty-printed".  preserveSpace would have to be false
      		// in order for them to be.  Hence, the need for the preserveSpace
      		// parameter to this method.  The Basic client passes true and
      		// the SOAP and XML clients pass false.
      		format.setPreserveSpace( preserveSpace );
      		
    		ByteArrayOutputStream stream = new ByteArrayOutputStream();
      		XMLSerializer serializer = new XMLSerializer( stream, format );
      		serializer.serialize( doc );
    		return( stream.toString( "UTF-8" ) );
    		
    	}
    	catch (IOException e)
    	{
	    	return(
	    		"Error getting string representation of XML: " +
	    		e.getMessage() );
    	}
	}
*/
	
	/**
	 * Returns the string representation of the given Node object.  Used for
	 * logging or demo purposes only.  As it employs some formatting
	 * parameters, parsing the string it returns may not result to a Node
	 * object exactly similar to the one passed to it.
	 *
	 * @param node	the Node object whose string representation is wanted.
	 * @param type  either PCI.REQUEST or PCI.REPLY.  Used for masking.
	 * 
	 * @return the string representation of the given Node object.
	 */
	public static String nodeToString( Node node, int type )
	{
		node = node.cloneNode( true );
		maskXml( type, node, null );
		return nodeToString( node );
    }
	
	/**
	 * Returns the string representation of the given Node object.  Used for
	 * logging or demo purposes only.  As it employs some formatting
	 * parameters, parsing the string it returns may not result to a Node
	 * object exactly similar to the one passed to it.
	 *
	 * @param node	the Node object whose string representation is wanted.
	 * 
	 * @return the string representation of the given Node object.
	 */
	public static String nodeToString( Node node )
	{
		try
		{
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
    		ByteArrayOutputStream stream = new ByteArrayOutputStream();
    		transformer.transform(
    			new DOMSource( node ), new StreamResult( stream ) );
    			
    		String str = stream.toString( "UTF-8" );
    		stream.close();
    		
    		return( str );
    	}
    	catch (TransformerConfigurationException e )
    	{
	    	return( e.getMessage() );
    	}
    	catch (TransformerException e)
    	{
	    	return( e.getMessage() );
    	}
    	catch (UnsupportedEncodingException e)
    	{
	    	return( e.getMessage() );
    	}
    	catch (IOException e)
    	{
	    	return( e.getMessage() );
    	}
	}
	
	/**
	 * Reads the properties from a file.  If no filename was specified in the
	 * command-line, it will look for "cybs.properties" in the current
	 * directory.
	 *
	 * @param commandLineArgs	the command-line arguments.
	 * 
	 * @return Properties object containing the run-time properties required by
	 *                    the clients.
	 */
	public static Properties readProperties( String[] commandLineArgs )
	{
   		Properties props = new Properties();
   		
		try
		{	
			String filename = (commandLineArgs.length > 0)
		   						? commandLineArgs[0] : "cybs.properties";
			
		   	FileInputStream fis = new FileInputStream( filename );
	   		props.load( fis );
	   		fis.close();
	   		
			return( props );
		}
		catch (IOException ioe)
		{
			// do nothing.  An empty Properties object will be returned.	
		}
		
		return( props );
	}
	
	/**
	 * Reads the content of the given file into a byte array.
	 *
	 * @param filename	name of the file to read.
	 * 
	 * @return content of the file.
	 *
	 * @throws IOException	if there was an error reading the file.
	 */
	public static byte[] read( String filename )
		throws IOException
	{
		BufferedInputStream stream 
			= new BufferedInputStream(
					new FileInputStream( filename ) );
		
		byte[] content = read( stream );
		stream.close();
		
		return( content );
	}
	
	/**
	 * Reads the content of the given file into a byte array.
	 *
	 * @param file	File object to read.
	 * 
	 * @return content of the file
	 *
	 * @throws IOException	if there was an error reading the file.
	 */
	public static byte[] read( File file )
		throws IOException
	{
		BufferedInputStream stream 
			= new BufferedInputStream(
					new FileInputStream( file ) );
		
		byte[] content = read( stream );
		stream.close();
		
		return( content );
	}
	
	/**
	 * Reads the content of the given file into a byte array.
	 *
	 * @param in	InputStream object to read.
	 * 
	 * @return content of the file
	 *
	 * @throws IOException	if there was an error reading the file.
	 */
	public static byte[] read( InputStream in )
		throws IOException
	{	
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		boolean more = true;
		int totalBytes = 0;
		while (more)
		{
	  		int avail = in.available();
	  		if (avail <= 0)
	  		{
	  			avail = 1;
  			}
	  		
  			byte[] buf = new byte[avail];
	  		int numRead = in.read( buf, 0, avail );
	  		if (numRead != -1)
	  		{
	    		baos.write( buf, 0, numRead );
	    		totalBytes += numRead;
	  		}
	  		else
	  		{
	    		more = false;
	  		}
		}
		
		if (totalBytes == 0)
		{
			return new byte[0];
		}
		
		byte[] ba = baos.toByteArray();
		baos.close();
		
		return( ba );
	}

	/**
	 * Returns a DocumentBuilder object.
	 *
	 * @return a DocumentBuilder object.
	 *
	 * @throws ParserConfigurationException	 if no suitable parser
	 *										 implementation is found.
	 */
	public static DocumentBuilder newDocumentBuilder()
		throws ParserConfigurationException
	{
   		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	   	dbf.setNamespaceAware( true );
	   	return( dbf.newDocumentBuilder() );
  	}
  	
	/**
	 * Returns the Element object corresponding to the given element name.
	 *
	 * @param owner			Document object to search.
	 * @param elementName	local name to search for.
	 * @param nsURI			namespaceURI to used (may be null).
	 *
	 * @return the Element object corresponding to the given element name or
	 *		   <code>null</code> if none is found.
	 */
	public static Element getElement(
		Document owner, String elementName, String nsURI )
	{
		NodeList nodes 
			= nsURI != null
				? owner.getElementsByTagNameNS(	nsURI, elementName )
				: owner.getElementsByTagName( elementName );
				
		if (nodes != null && nodes.getLength() > 0)
		{
			return( (Element) nodes.item( 0 ) );
		}
		
		return( null );
	}
	
	/**
	 * Returns the text value of the given element name in the CyberSource
	 * namespace.
	 *
	 * @param owner			Document object to search.
	 * @param elementName	local name to search for.
	 * @param nsURI			namespaceURI to used (may be null).
	 *
	 * @return the text value of the given element name in the CyberSource
	 *         namespace or <code>null</code> if none is found.
	 */
	public static String getElementText(
		Document owner, String elementName, String nsURI )
	{
		Element elem = getElement( owner, elementName, nsURI );
		if (elem != null)
		{
			return( elem.getFirstChild().getNodeValue() );
		}
		
		return( null );
	}		
	
	
	/**
	 * Returns the stack trace of the supplied Exception object.
	 *
	 * @param e	Exception object.
	 *
	 * @return the stack trace of the Exception object.
	 */
	public static String getStackTrace( Exception e )
	{
  		StringWriter sw = new StringWriter();
   		PrintWriter pw = new PrintWriter( sw );
   		e.printStackTrace( pw );
   		String stackTrace = sw.toString();
   		pw.close();
   		return( stackTrace );
   	}
   	
   	private static final String CYBS_ROOT_FIELDS = "requestMessage replyMessage nvpRequest nvpReply";
   	
    // parentName must be null.  This is a recursive method.
    // The recursive calls will pass non-NULL strings to said
    // parameter.
    private static void maskXml( int type, Node node, String parentName )
    {
        if (node == null) return;

        short nodeType = node.getNodeType();
        if (nodeType == Node.TEXT_NODE)
        {
            if (!PCI.isSafe( type, parentName ))
            {
                String origVal = node.getNodeValue();
                if (origVal != null) origVal = origVal.trim();
                if (origVal != null && origVal.length() > 0)
                {
                    node.setNodeValue( PCI.mask( parentName, origVal ) );
                }
            }
        }
        else if (nodeType == Node.ELEMENT_NODE ||
                 nodeType == Node.DOCUMENT_NODE)
        {
            if (!node.hasChildNodes()) return;
            
            String localName = node.getLocalName();
            if (localName == null) localName = "";

            String fieldFullName = null;
            if (parentName == null) {
	            // we have not encountered any of the fields in
	            // CYBS_ROOT_FIELDS, in which case, we check if
	            // the current node's local name is one of them.
	            // If so, then we set fieldFullName to "".
	            // Otherwise, fieldFullName remains null.
	            if (localName.length() > 0 &&
                        CYBS_ROOT_FIELDS.indexOf( localName ) != -1) {
		            fieldFullName = "";
	            }
            }
            else if (parentName.length() == 0) {
	            // the immediate parent of this node is one of
	            // those in CYBS_ROOT_FIELDS, in which case, we
	            // use its local name as the field name so far.
	            fieldFullName = localName;
            }
            else {
	            // this is a node that is at least two levels
	            // down from one of the CYBS_ROOT_FIELDS, in which
	            // case, we append its local name to the parent's name.
	            fieldFullName = parentName + "_" + localName;
            }
            
            // call this method recursively on each of the child nodes
            NodeList children = node.getChildNodes();
            int numChildren = children.getLength();
            for (int i = 0; i < numChildren; ++i) {
                maskXml( type, children.item( i ), fieldFullName );
            }
        }
    }
}	
