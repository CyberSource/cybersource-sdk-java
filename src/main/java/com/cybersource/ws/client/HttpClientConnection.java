/* Copyright 2006 CyberSource Corporation */

package com.cybersource.ws.client;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpMethodRetryHandler;
import org.apache.commons.httpclient.NoHttpResponseException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Class helps in posting the Request document for the Transaction using HttpClient.
 * Converts the document to String format and also helps in setting up the Proxy connections.
 * @author sunagara
 *
 */
class HttpClientConnection extends Connection
{
    private PostMethod postMethod = null;
    
    HttpClientConnection(
	MerchantConfig mc, DocumentBuilder builder, LoggerWrapper logger )
    {
        super( mc, builder, logger );
        logger.log( Logger.LT_INFO, "Using HttpClient for connections." );
    }
    
    void postDocument( Document request )
        throws IOException, TransformerConfigurationException,
               TransformerException, MalformedURLException,
               ProtocolException
    {
        HttpClient httpClient = new HttpClient();
        setTimeout( httpClient, mc.getTimeout() * 1000 );
	setProxy( httpClient );
        
        String serverURL = mc.getEffectiveServerURL();
        postMethod = new PostMethod( serverURL );
        postMethod.getParams().setParameter(
            HttpMethodParams.RETRY_HANDLER, new MyRetryHandler() );
        
        String requestString = documentToString( request );
        logger.log( Logger.LT_INFO,
	   "Sending " + requestString.length() + " bytes to " + serverURL );
            
        postMethod.setRequestEntity(
            new StringRequestEntity( requestString ) );
            
        httpClient.executeMethod( postMethod );
    }
    
    public boolean isRequestSent()
    {
        return postMethod != null && postMethod.isRequestSent();
    }
    
    public void release()
    {
        if (postMethod != null) {
            postMethod.releaseConnection();
            postMethod = null;
        }
    }

    int getHttpResponseCode()
	throws IOException
    {
        return postMethod != null ? postMethod.getStatusCode() : -1;
    }
    
    InputStream getResponseStream()
	throws IOException
    {
        return postMethod != null ? postMethod.getResponseBodyAsStream() : null;
    }
    
    InputStream getResponseErrorStream()
	throws IOException
    {
        return getResponseStream();
    }
    
    private void setTimeout( HttpClient httpClient, int timeoutInMs )
    {    
        HttpConnectionManagerParams params
            = httpClient.getHttpConnectionManager().getParams();        
        params.setConnectionTimeout( timeoutInMs );
        params.setSoTimeout( timeoutInMs );
    }

    private void setProxy( HttpClient httpClient )
    {
	if (mc.getProxyHost() != null) {
		httpClient.getHostConfiguration().setProxy(
			mc.getProxyHost(), mc.getProxyPort() ); 
		
		if (mc.getProxyUser() != null) {
			List authPrefs = new ArrayList();
			authPrefs.add(AuthPolicy.BASIC);
			httpClient.getParams().setParameter(
				AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs);

			HttpState state = new HttpState();
			state.setProxyCredentials(
			//httpClient.getState().setProxyCredentials(
				AuthScope.ANY, 
				new UsernamePasswordCredentials(
			  	  mc.getProxyUser(), mc.getProxyPassword() ) );
			httpClient.setState( state );
		}		
	}
    }
    
    private static String documentToString( Document doc )
        throws TransformerConfigurationException, TransformerException,
               IOException
    {
        ByteArrayOutputStream baos = null;
        try
        {
            baos = makeStream( doc );
            return baos.toString( "utf-8" );
        }
        finally {
            if (baos != null) {
                baos.close();
            }
        }
    }
            
    private class MyRetryHandler implements HttpMethodRetryHandler
    {
        // I had to override the default retryMethod as it also
        // retries if there is no response from the server.  We
        // don't want to take any chances.
        // 
        // I copied this code from
        // http://jakarta.apache.org/commons/httpclient/exception-handling.html#HTTP%20transport%20safety
        // and changed the NoHttpResponseException case to
        // return false.
        public boolean retryMethod(
            final HttpMethod method, 
            final IOException exception, 
            int executionCount) {
            if (executionCount > 5) {
                // Do not retry if over max retry count
                return false;
            }
            if (exception instanceof NoHttpResponseException) {
                // Retry if the server dropped connection on us
                // return true; <-- this was the original behavior.
                return false;
            }
            if (!method.isRequestSent()) {
                // Retry if the request has not been sent fully or
                // if it's OK to retry methods that have been sent
                return true;
            }
            // otherwise do not retry
            return false;
        }
   }    
}

/* Copyright 2006 CyberSource Corporation */


