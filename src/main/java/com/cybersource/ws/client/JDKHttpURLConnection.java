/* Copyright 2006 CyberSource Corporation */

package com.cybersource.ws.client;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import com.com.com.cybersource.b;
import com.com.com.cybersource.c;

/**
 * Class helps in posting the Request document for the Transaction using URLConnection.
 * Converts the document to String format and also helps in setting up the Proxy connections.
 * 
 * @author sunagara
 *
 */
class JDKHttpURLConnection extends Connection
{
    private boolean _isRequestSent = false;
    private HttpURLConnection con = null;
    
    JDKHttpURLConnection(
	MerchantConfig mc, DocumentBuilder builder, LoggerWrapper logger )
    {
        super( mc, builder, logger );
        logger.log( Logger.LT_INFO, "Using HttpURLConnection for connections." );
    }
    
    void postDocument( Document request )
        throws IOException, TransformerConfigurationException,
               TransformerException, MalformedURLException,
               ProtocolException, c,
	       b
    {
        String serverURL = mc.getEffectiveServerURL();
        URL url = new URL( serverURL );

        con = JDKVersion.openConnection( url, mc );
        con.setRequestMethod( "POST" );
        con.setDoOutput( true );
        JDKVersion.setTimeout( con, mc.getTimeout() );
        
        OutputStream out = con.getOutputStream();
        byte[] requestBytes = documentToByteArray( request );
        logger.log( Logger.LT_INFO,
	   "Sending " + requestBytes.length + " bytes to " + serverURL );
        out.write( requestBytes );
        out.close();
        
        _isRequestSent = true;
    }
    
    public boolean isRequestSent()
    {
        return _isRequestSent;
    }
    
    public void release()
    {
        con = null;
    }
    
    int getHttpResponseCode()
	throws IOException
    {
        return con != null ? con.getResponseCode() : -1;
    }
    
    InputStream getResponseStream()
	throws IOException
    {
        return con != null ? con.getInputStream() : null;
    }
    
    InputStream getResponseErrorStream()
	throws IOException
    {
        return con != null ? con.getErrorStream() : null;
    }
    
    private static byte[] documentToByteArray( Document doc )
        throws TransformerConfigurationException, TransformerException,
               IOException
    {
        ByteArrayOutputStream baos = null;
        try
        {
            baos = makeStream( doc );
            return baos.toByteArray();
        }
        finally {
            if (baos != null) {
                baos.close();
            }
        }
    }
}

/* Copyright 2006 CyberSource Corporation */

