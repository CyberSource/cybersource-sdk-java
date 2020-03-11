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

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
/**
 * Class helps in posting the Request document for the Transaction using HttpClient.
 * Converts the document to String format and also helps in setting up the Proxy connections.
 *
 * @author sunagara
 */
class HttpClientConnection extends Connection {
    HttpPost httpPost = null;
    HttpResponse httpResponse = null;
    HttpClientContext httpContext = null;

    HttpClientConnection(
            MerchantConfig mc, DocumentBuilder builder, LoggerWrapper logger) {
        super(mc, builder, logger);
        logger.log(Logger.LT_INFO, "Using HttpClient for connections.");
    }

    /* (non-Javadoc)
     * @see com.cybersource.ws.client.Connection#postDocument(org.w3c.dom.Document)
     */
    void postDocument(Document request) throws TransformerException, IOException {
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
                .setSocketTimeout(mc.getTimeout())
                .setConnectTimeout(mc.getTimeout());

        HttpClientBuilder httpClientBuilder = HttpClients.custom()
                .setRetryHandler(new newRetryHandler());

        setProxyNew(httpClientBuilder, requestConfigBuilder);

        HttpClient httpClient = httpClientBuilder.setDefaultRequestConfig(requestConfigBuilder.build()).build();

        httpPost = new HttpPost(mc.getEffectiveServerURL());
        StringEntity stringEntity = new StringEntity(documentToString(request), "UTF-8");
        httpPost.setEntity(stringEntity);
        httpPost.setHeader(Utility.ORIGIN_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
        logRequestHeaders();
        httpContext = HttpClientContext.create();
        httpResponse = httpClient.execute(httpPost, httpContext);
    }

    /* (non-Javadoc)
     * @see com.cybersource.ws.client.Connection#isRequestSent()
     */
    public boolean isRequestSent() {
        return httpContext != null && httpContext.isRequestSent();
    }

    /* (non-Javadoc)
     * @see com.cybersource.ws.client.Connection#release()
     */
    public void release() {
        if(httpPost != null) {
            httpPost.releaseConnection();
            httpPost = null;
        }
    }

    /* (non-Javadoc)
     * @see com.cybersource.ws.client.Connection#getHttpResponseCode()
     */
    int getHttpResponseCode(){
        return httpResponse != null ? httpResponse.getStatusLine().getStatusCode() : -1;
    }

    /* (non-Javadoc)
     * @see com.cybersource.ws.client.Connection#getResponseStream()
     */
    InputStream getResponseStream()
            throws IOException {
        return httpResponse != null ? httpResponse.getEntity().getContent() : null;
    }

    /* (non-Javadoc)
     * @see com.cybersource.ws.client.Connection#getResponseErrorStream()
     */
    InputStream getResponseErrorStream()
            throws IOException {
        return getResponseStream();
    }


    /**
    * This method is useful in Client environment where Firewall is set to prevent accessing the external services.
    * Proxy settings are required in such scenarios.
    * @param httpClientBuilder
    * @param requestConfigBuilder
    */
    private void setProxyNew(HttpClientBuilder httpClientBuilder, RequestConfig.Builder requestConfigBuilder) {
        if(mc.getProxyHost() != null) {
            requestConfigBuilder.setProxy(new HttpHost(mc.getProxyHost(), mc.getProxyPort()));

            if(mc.getProxyUser() != null) {
                requestConfigBuilder.setProxyPreferredAuthSchemes(new ArrayList<String>(Collections.singleton(AuthSchemes.BASIC)));

                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(
                        org.apache.http.auth.AuthScope.ANY,
                        new org.apache.http.auth.UsernamePasswordCredentials(mc.getProxyUser(), mc.getProxyPassword())
                );
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
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
    */
    class newRetryHandler implements HttpRequestRetryHandler {
        long retryWaitInterval = mc.getRetryInterval();
        int maxRetries = mc.getNumberOfRetries();

        @Override
        public boolean retryRequest(IOException exception, int executionCount, HttpContext httpContext) {
            if(executionCount > maxRetries) {
                return false;
            }

            if(exception instanceof NoHttpResponseException) {
                return false;
            }

            Boolean b = (Boolean) httpContext.getAttribute("http.request_sent");
            boolean sent = b != null && b;

            if(!sent) {
                try {
                    Thread.sleep(retryWaitInterval);
                    logger.log( Logger.LT_INFO + " Retrying Request -- ", mc.getUniqueKey().toString() + " Retry Count -- " + executionCount);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return false;
        }
    }

    @Override
    public void logRequestHeaders() {
        List<org.apache.http.Header> reqHeaders = Arrays.asList(httpPost.getAllHeaders());
        logger.log(Logger.LT_INFO, "Request Headers: " + reqHeaders);
    }

    @Override
    public void logResponseHeaders() {
        List<org.apache.http.Header> respheaders = Arrays.asList(httpResponse.getAllHeaders());
        logger.log(Logger.LT_INFO, "Response Headers"+ respheaders);
    }

}

/* Copyright 2006 CyberSource Corporation */


