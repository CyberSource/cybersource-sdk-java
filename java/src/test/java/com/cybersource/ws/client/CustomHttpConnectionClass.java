package com.cybersource.ws.client;

import com.cybersource.ws.client.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.NoHttpResponseException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.w3c.dom.Document;


public class CustomHttpConnectionClass extends Connection{
	private PostMethod postMethod = null;
	
	CustomHttpConnectionClass(MerchantConfig mc, DocumentBuilder builder, LoggerWrapper logger) {
        super(mc, builder, logger);
    }
	
	@Override
	public boolean isRequestSent() {
		 return postMethod != null && postMethod.isRequestSent();
	}
	@Override
	public void release() {
		 if (postMethod != null) {
	            postMethod.releaseConnection();
	            postMethod = null;
	        }	
	}
	
	@Override
	void postDocument(Document request) throws IOException,
			TransformerConfigurationException, TransformerException,
			MalformedURLException, ProtocolException {
		HttpClient httpClient = new HttpClient();
        setTimeout(httpClient, mc.getTimeout() * 1000);
        setProxy(httpClient);

        String serverURL = mc.getEffectiveServerURL();
        postMethod = new PostMethod(serverURL);
        postMethod.getParams().setParameter(
                HttpMethodParams.RETRY_HANDLER, new MyRetryHandler());

        String requestString = documentToString(request);
        logger.log(Logger.LT_INFO,
                "Sending " + requestString.length() + " bytes to " + serverURL);

        postMethod.setRequestEntity(
                new StringRequestEntity(requestString, null, "UTF-8"));

        httpClient.executeMethod(postMethod);
		
	}
	@Override
	int getHttpResponseCode() throws IOException {
		 return postMethod != null ? postMethod.getStatusCode() : -1;
	}
	@Override
	InputStream getResponseStream() throws IOException {
		return postMethod != null ? postMethod.getResponseBodyAsStream() : null;
	}
	@Override
	InputStream getResponseErrorStream() throws IOException {
		return getResponseStream();
	}
	 /**
     * This method is useful in Client environment where Firewall is set to prevent accessing the external services. 
     * Proxy settings are required in such scenarios. 
     * @param httpClient
     */
    private void setProxy(HttpClient httpClient) {
        if (mc.getProxyHost() != null) {
            httpClient.getHostConfiguration().setProxy(
                    mc.getProxyHost(), mc.getProxyPort());

            if (mc.getProxyUser() != null) {
                List<String> authPrefs = new ArrayList<String>();
                authPrefs.add(AuthPolicy.BASIC);
                httpClient.getParams().setParameter(
                        AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs);

                HttpState state = new HttpState();
                state.setProxyCredentials(
                        AuthScope.ANY,
                        new UsernamePasswordCredentials(
                                mc.getProxyUser(), mc.getProxyPassword()));
                httpClient.setState(state);
            }
        }
    }
    
    /**
     * Method converts Document to String using java.xml package library.
     * @param doc - Document object
     * @return - String object
     * @throws TransformerConfigurationException
     * @throws TransformerException
     * @throws IOException
     */
    private static String documentToString(Document doc)
            throws TransformerConfigurationException, TransformerException,
            IOException {
        ByteArrayOutputStream baos = null;
        try {
            baos = makeStream(doc);
            return baos.toString("utf-8");
        } finally {
            if (baos != null) {
                baos.close();
            }
        }
    }
    
    /**
     * Methods helps to set the timeout for HTTP request call.
     * cybs.properties can be used to configure the timeout details.
     * @param httpClient
     * @param timeoutInMs
     */
    private void setTimeout(HttpClient httpClient, int timeoutInMs) {
        HttpConnectionManagerParams params
                = httpClient.getHttpConnectionManager().getParams();
        params.setConnectionTimeout(timeoutInMs);
        params.setSoTimeout(timeoutInMs);
    }
    
    private class MyRetryHandler implements HttpMethodRetryHandler {
        
    	long retryWaitInterval=mc.getRetryInterval();
 	   	int maxRetries= mc.getNumberOfRetries();
 	   	
        // I copied this code from
        // http://jakarta.apache.org/commons/httpclient/exception-handling.html#HTTP%20transport%20safety
        // and changed the NoHttpResponseException case to
        // return false.
        public boolean retryMethod(
                final HttpMethod method,
                final IOException exception,
                int executionCount) {
            if (executionCount > maxRetries) {
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
            	try {
         	        Thread.sleep(retryWaitInterval);
         	        logger.log( Logger.LT_INFO+" Retrying Request -- ",mc.getUniqueKey().toString()+ " Retry Count -- "+executionCount);
                 } catch (InterruptedException e) {
         	        // TODO Auto-generated catch block
         	        e.printStackTrace();
                 }
                return true;
            }
            // otherwise do not retry
            return false;
        }
    }

}

