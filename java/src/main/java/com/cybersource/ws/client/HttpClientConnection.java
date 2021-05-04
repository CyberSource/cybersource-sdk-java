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


import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.NoHttpResponseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.protocol.HttpContext;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.cybersource.ws.client.Utility.*;

/**
 * Helps in posting the Request document for the Transaction using HttpClient.
 * Converts the document to String format and also helps in setting up the Proxy connections.
 *
 */
public class HttpClientConnection extends Connection {
    private HttpPost httpPost = null;
    private HttpClientContext httpContext = null;
    private CloseableHttpClient httpClient = null;
    private CloseableHttpResponse httpResponse = null;

    /**
     * @param mc
     * @param builder
     * @param logger
     */
    HttpClientConnection(
            MerchantConfig mc, DocumentBuilder builder, LoggerWrapper logger) {
        super(mc, builder, logger);
        initHttpClient(mc, mc.getTimeout()*1000);
        logger.log(Logger.LT_INFO, "Using HttpClient for connections.");
    }

    /**
     * Post request by httpclient connection
     * @param request
     * @param startTime
     * @throws IOException
     * @throws TransformerException
     */
    /* (non-Javadoc)
     * @see com.cybersource.ws.client.Connection#postDocument(org.w3c.dom.Document)
     */
    void postDocument(Document request, long startTime)
            throws IOException, TransformerException {

        String serverURL = mc.getEffectiveServerURL();
        httpPost = new HttpPost(serverURL);
        String requestString = documentToString(request);
        StringEntity stringEntity = new StringEntity(requestString, "UTF-8");
        httpPost.setEntity(stringEntity);
        httpPost.setHeader(Utility.SDK_ELAPSED_TIMESTAMP, String.valueOf(System.currentTimeMillis() - startTime));
        httpPost.setHeader(Utility.ORIGIN_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
        logRequestHeaders();
        httpContext = HttpClientContext.create();
        logger.log(Logger.LT_INFO,
                "Sending " + requestString.length() + " bytes to " + serverURL);
        httpResponse = httpClient.execute(httpPost, httpContext);
    }

    /**
     * To check is request sent or not
     * @return boolean
     */
    /* (non-Javadoc)
     * @see com.cybersource.ws.client.Connection#isRequestSent()
     */
    public boolean isRequestSent() {
        return httpContext != null && httpContext.isRequestSent();
    }

    /**
     * To release the http connections
     */
    /* (non-Javadoc)
     * @see com.cybersource.ws.client.Connection#release()
     */
    public void release() {
        if (httpPost != null) {
            httpPost.releaseConnection();
            httpPost = null;
        }
    }

    /**
     * To get http response code
     * @return int
     */
    /* (non-Javadoc)
     * @see com.cybersource.ws.client.Connection#getHttpResponseCode()
     */
    int getHttpResponseCode() {
        return httpResponse != null ? httpResponse.getStatusLine().getStatusCode() : -1;
    }

    /**
     * To get response stream
     * @return InputStream
     * @throws IOException
     */
    /* (non-Javadoc)
     * @see com.cybersource.ws.client.Connection#getResponseStream()
     */
    InputStream getResponseStream()
            throws IOException {
        return httpResponse != null ? httpResponse.getEntity().getContent() : null;
    }

    /**
     * To get response error stream
     * @return InputStream
     * @throws IOException
     */
    /* (non-Javadoc)
     * @see com.cybersource.ws.client.Connection#getResponseErrorStream()
     */
    InputStream getResponseErrorStream()
            throws IOException {
        return getResponseStream();
    }

    /**
     * @param mc
     */
    protected void initHttpClient(MerchantConfig mc, int timeoutInMs) {
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
                .setSocketTimeout(timeoutInMs)
                .setConnectTimeout(timeoutInMs);

        HttpClientBuilder httpClientBuilder = HttpClients.custom();

        if(mc.isAllowRetry()){
            httpClientBuilder.setRetryHandler(new MyRetryHandler());
        }

        setProxy(httpClientBuilder, requestConfigBuilder, mc);

        httpClient = httpClientBuilder
                .setDefaultRequestConfig(requestConfigBuilder.build())
                .build();
    }

    /**
     * Set proxy by using proxy credentials to create httpclient
     *
     * @param httpClientBuilder
     * @param requestConfigBuilder
     * @param merchantConfig
     */
    private void setProxy(HttpClientBuilder httpClientBuilder, RequestConfig.Builder requestConfigBuilder, MerchantConfig merchantConfig) {
        if (merchantConfig.getProxyHost() != null) {
            HttpHost proxy = new HttpHost(merchantConfig.getProxyHost(), merchantConfig.getProxyPort());
            DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
            httpClientBuilder.setRoutePlanner(routePlanner);

            if (merchantConfig.getProxyUser() != null) {
                httpClientBuilder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());
                requestConfigBuilder.setProxyPreferredAuthSchemes(Collections.singletonList(AuthSchemes.BASIC));

                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(merchantConfig.getProxyUser(), merchantConfig.getProxyPassword()));

                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
        }
    }

    /**
     * Converts Document to String using java.xml package library.
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
    private class MyRetryHandler implements HttpRequestRetryHandler {
       
    	long retryWaitInterval=mc.getRetryInterval();
 	   	int maxRetries= mc.getNumberOfRetries();
 	   	
        // I copied this code from
        // http://jakarta.apache.org/commons/httpclient/exception-handling.html#HTTP%20transport%20safety
        // and changed the NoHttpResponseException case to
        // return false.
        @Override
        public boolean retryRequest(IOException exception, int executionCount, HttpContext httpContext) {
            if (executionCount > maxRetries) {
                // Do not retry if over max retry count
                return false;
            }if (exception instanceof NoHttpResponseException) {
                // Retry if the server dropped connection on us
                // return true; <-- this was the original behavior.
                return false;
            }
            HttpClientContext httpClientContext = HttpClientContext.adapt(httpContext);
            if (!httpClientContext.isRequestSent()) {
                // Retry if the request has not been sent fully or
                // if it's OK to retry methods that have been sent
                try {
                    Thread.sleep(retryWaitInterval);
                    logger.log( Logger.LT_INFO, " Retrying Request -- "+logger.getUniqueKey()+ " Retry Count -- "+executionCount);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;
            }
            // otherwise do not retry
            return false;
        }
    }

    @Override
	public void logRequestHeaders() {
        if(mc.getEnableLog() && httpPost != null) {
            List<Header> reqheaders = Arrays.asList(httpPost.getAllHeaders());
            logger.log(Logger.LT_INFO, "Request Headers: " + reqheaders);
        }
	}

	@Override
	public void logResponseHeaders() {
        if(mc.getEnableLog() && httpResponse != null) {
            Header responseTimeHeader = httpResponse.getFirstHeader(RESPONSE_TIME_REPLY);
            if (responseTimeHeader != null && StringUtils.isNotBlank(responseTimeHeader.getValue())) {
                long resIAT = getResponseIssuedAtTime(responseTimeHeader.getValue());
                if (resIAT > 0) {
                    logger.log(Logger.LT_INFO, "responseTransitTimeSec : " + getResponseTransitTime(resIAT));
                }
            }
            List<Header> respheaders = Arrays.asList(httpResponse.getAllHeaders());
            logger.log(Logger.LT_INFO, "Response Headers" + respheaders);
        }
	}
	
}

/* Copyright 2006 CyberSource Corporation */


