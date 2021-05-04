package com.cybersource.ws.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.Collections;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

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


public class CustomHttpConnectionClass extends Connection{

    private HttpPost httpPost = null;
    private HttpClientContext httpContext = null;
    private CloseableHttpClient httpClient = null;
    private CloseableHttpResponse httpResponse = null;


    CustomHttpConnectionClass(MerchantConfig mc, DocumentBuilder builder, LoggerWrapper logger) {
        super(mc, builder, logger);
        initHttpClient(mc, mc.getTimeout()*1000);
    }
	
	@Override
	public boolean isRequestSent() {
        return httpContext != null && httpContext.isRequestSent();
	}

	@Override
	public void release() {
        if (httpPost != null) {
            httpPost.releaseConnection();
            httpPost = null;
        }
	}
	
	@Override
	void postDocument(Document request, long startTime) throws IOException,
			TransformerConfigurationException, TransformerException,
			MalformedURLException, ProtocolException {

        String serverURL = mc.getEffectiveServerURL();
        httpPost = new HttpPost(serverURL);
        String requestString = documentToString(request);
        StringEntity stringEntity = new StringEntity(requestString, "UTF-8");
        logger.log(Logger.LT_INFO,
                "Sending " + requestString.length() + " bytes to " + serverURL);
        httpPost.setEntity(stringEntity);
        httpResponse = httpClient.execute(httpPost, httpContext);
		
	}

	@Override
	int getHttpResponseCode() throws IOException {
		 return httpResponse != null ? httpResponse.getStatusLine().getStatusCode() : -1;
	}

	@Override
	InputStream getResponseStream() throws IOException {
		return httpResponse != null ? httpResponse.getEntity().getContent() : null;
	}

	@Override
	InputStream getResponseErrorStream() throws IOException {
		return getResponseStream();
	}

    /**
     * Set proxy by using proxy credentials to create httpclient
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
     * @param mc
     * Set Socket timeout and connection timeout using RequestConfig.Builder
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
     *  Override the retryRequest as it also
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
 	public void logResponseHeaders() {	
 		
 	}
     
     
     @Override
 	public void logRequestHeaders() {

 	}
}

