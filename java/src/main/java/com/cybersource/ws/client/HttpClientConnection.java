/*
* Copyright 2003-2014 CyberSource Corporation
*
* THE SOFTWARE AND THE DOCUMENTATION ARE PROVIDED ON AN "AS IS" AND "AS
* AVAILABLE" BASIS WITH NO WARRANTY.  YOU AGREE THAT YOUR USE OF THE SOFTWARE AND THE
* DOCUMENTATION IS AT YOUR SOLE RISK AND YOU ARE SOLELY RESPONSIBLE FOR ANY DAMAGE TO YOUR
* COMPUTER SYSTEM OR OTHER DEVICE OR LOSS OF DATA THAT RESULTS FROM SUCH USE. TO THE FULLEST
* EXTENT PERMISSIBLE UNDER APPLICABLE LAW, CYBERSOURCE AND ITS AFFILIATES EXPRESSLY DISCLAIM ALL
* WARRANTIES OF ANY KIND, EXPRESS OR IMPLIED, WITH RESPECT TO THE SOFTWARE AND THE
* DOCUMENTATION, INCLUDING ALL WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE,
* SATISFACTORY QUALITY, ACCURACY, TITLE AND NON-INFRINGEMENT, AND ANY WARRANTIES THAT MAY ARISE
* OUT OF COURSE OF PERFORMANCE, COURSE OF DEALING OR USAGE OF TRADE.  NEITHER CYBERSOURCE NOR
* ITS AFFILIATES WARRANT THAT THE FUNCTIONS OR INFORMATION CONTAINED IN THE SOFTWARE OR THE
* DOCUMENTATION WILL MEET ANY REQUIREMENTS OR NEEDS YOU MAY HAVE, OR THAT THE SOFTWARE OR
* DOCUMENTATION WILL OPERATE ERROR FREE, OR THAT THE SOFTWARE OR DOCUMENTATION IS COMPATIBLE
* WITH ANY PARTICULAR OPERATING SYSTEM.
*/

package com.cybersource.ws.client;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class helps in posting the Request document for the Transaction using HttpClient.
 * Converts the document to String format and also helps in setting up the Proxy connections.
 *
 * @author sunagara
 */
class HttpClientConnection extends Connection {
    private PostMethod postMethod = null;

    HttpClientConnection(
            MerchantConfig mc, DocumentBuilder builder, LoggerWrapper logger) {
        super(mc, builder, logger);
        logger.log(Logger.LT_INFO, "Using HttpClient for connections.");
    }

    /* (non-Javadoc)
     * @see com.cybersource.ws.client.Connection#postDocument(org.w3c.dom.Document)
     */
    void postDocument(Document request)
            throws IOException, TransformerConfigurationException,
            TransformerException, MalformedURLException,
            ProtocolException {
    	
    	/*
    	 * SimpleHttpConnectionManager(boolean alwaysClose) : 
    	 * alwaysClose - if set true, the connection manager will always close connections upon release.
    	 */
    	
        HttpClient httpClient = new HttpClient(new SimpleHttpConnectionManager(true));
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

    /* (non-Javadoc)
     * @see com.cybersource.ws.client.Connection#isRequestSent()
     */
    public boolean isRequestSent() {
        return postMethod != null && postMethod.isRequestSent();
    }

    /* (non-Javadoc)
     * @see com.cybersource.ws.client.Connection#release()
     */
    public void release() {
        if (postMethod != null) {
            postMethod.releaseConnection();
            postMethod = null;
        }
    }

    /* (non-Javadoc)
     * @see com.cybersource.ws.client.Connection#getHttpResponseCode()
     */
    int getHttpResponseCode()
            throws IOException {
        return postMethod != null ? postMethod.getStatusCode() : -1;
    }

    /* (non-Javadoc)
     * @see com.cybersource.ws.client.Connection#getResponseStream()
     */
    InputStream getResponseStream()
            throws IOException {
        return postMethod != null ? postMethod.getResponseBodyAsStream() : null;
    }

    /* (non-Javadoc)
     * @see com.cybersource.ws.client.Connection#getResponseErrorStream()
     */
    InputStream getResponseErrorStream()
            throws IOException {
        return getResponseStream();
    }

    /**
     * Methos helps to set the timeout for HTTP request call.
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
     *  We had to override the default retryMethod as it also
     *  retries if there is no response from the server. 
     *  We don't want to take any chances.
     *
     */
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

/* Copyright 2006 CyberSource Corporation */


