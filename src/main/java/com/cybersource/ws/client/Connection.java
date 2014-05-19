/* Copyright 2006 CyberSource Corporation */

package com.cybersource.ws.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import com.com.com.cybersource.b;
import com.com.com.cybersource.c;

/**
 * Connection class is a helper class for connecting to HttpClientConnection and
 * JDKHttpURLConnection.
 * @author sunagara
 *
 */
abstract class Connection
{
    protected MerchantConfig mc;
    protected DocumentBuilder builder;
    protected LoggerWrapper logger;

    protected Connection(
	MerchantConfig mc, DocumentBuilder builder, LoggerWrapper logger )
    {
	this.mc      = mc;
	this.builder = builder;
	this.logger  = logger;
    }
    
    public static Connection getInstance(
            MerchantConfig mc, DocumentBuilder builder, LoggerWrapper logger )
    {
        if (mc.getUseHttpClient()) {
		return new HttpClientConnection( mc, builder, logger );
	}

        return new JDKHttpURLConnection( mc, builder, logger );
    }
    
    abstract public boolean isRequestSent();
    
    abstract public void release();
    
    abstract void postDocument( Document request )
        throws IOException, TransformerConfigurationException,
               TransformerException, MalformedURLException,
               ProtocolException, c,
	       b;
                   
    abstract int getHttpResponseCode()
	throws IOException;
    
    abstract InputStream getResponseStream()
	throws IOException;
    
    abstract InputStream getResponseErrorStream()
	throws IOException;
    
    public Document post( Document request )
        throws ClientException, FaultException
    {
	try
	{
		postDocument( request );
		checkForFault();
		return( parseReceivedDocument() );
	}
	catch (IOException e) {
 		throw new ClientException( e, isRequestSent(), logger );
	} 
        catch (TransformerConfigurationException e) {
                throw new ClientException( e, isRequestSent(), logger );
        }
        catch (TransformerException e) {
                throw new ClientException( e, isRequestSent(), logger );
        }
        catch (SAXException e) {
                throw new ClientException( e, isRequestSent(), logger );
        }
        catch (c e) {
                throw new ClientException( e, isRequestSent(), logger );
        }
        catch (b e) {
                throw new ClientException( e, isRequestSent(), logger );
        }
    }

    private void checkForFault()
        throws FaultException, ClientException
    {
        try
        {
            logger.log( Logger.LT_INFO, "Reading response..." );
            
            int responseCode = getHttpResponseCode();
            
            // if successful, there's nothing left to do here.
            // we'll process the response in a later method.
            if (responseCode == HttpURLConnection.HTTP_OK) return;

            InputStream errorStream = getResponseErrorStream();

            // if there is no error stream, then it is not a fault
            if (errorStream == null) {
                throw new ClientException( responseCode, logger );
            }

            // read error stream into a byte array
            byte[] errorBytes = null;
            try {
                errorBytes = Utility.read( errorStream );
                errorStream.close();
            }
            catch (IOException ioe) {
                throw new ClientException(
                    responseCode, "Failed to read additional HTTP error",
                    true, logger );
            }

            // server will return HTTP 500 on a fault
            if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                try {
                    ByteArrayInputStream bais
                        = new ByteArrayInputStream( errorBytes );

                    Document faultDoc = builder.parse( bais );
                    bais.close();

                    throw new FaultException(
			faultDoc, mc.getEffectiveNamespaceURI(), logger );

                }
                catch (IOException ioe)
                {
                    // If an IO exception occurs, we're not sure whether
                    // or not it was a fault.  So we mark it as critical.
                    String text = new String( errorBytes );
                    throw new ClientException(
                        responseCode, text, true, logger );
                }
                catch (SAXException ioe)
                {
                    // If parsing fails, it means it's not a fault after all.
                    String text = new String( errorBytes );
                    throw new ClientException( responseCode, text, logger );
                }
            }
            else
            {
                // non-500 return codes are definitely not faults
                String text = new String( errorBytes );
                throw new ClientException( responseCode, text, logger );
            }
        }
        // catch other IOException's that we have not already handled.
        catch (IOException e)
        {
            throw new ClientException( e, true, logger );
        }
    }
    
    private Document parseReceivedDocument()
            throws IOException, SAXException
    {
	logger.log( Logger.LT_INFO, "Parsing response..." );
        return builder.parse( getResponseStream() );
    }           
            
    protected static ByteArrayOutputStream makeStream( Document doc )
        throws TransformerConfigurationException, TransformerException,
               IOException
    {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(
            new DOMSource( doc ), new StreamResult( baos ) );
        
        return baos;
    }    
}

/* Copyright 2006 CyberSource Corporation */

