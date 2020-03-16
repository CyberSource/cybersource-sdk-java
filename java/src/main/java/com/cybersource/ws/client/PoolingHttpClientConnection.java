package com.cybersource.ws.client;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PoolingHttpClientConnection extends Connection {
    HttpPost httpPost = null;
    HttpResponse httpResponse = null;
    HttpClientContext httpContext = null;
    CloseableHttpClient httpClient = null;
    static PoolingHttpClientConnectionManager connectionManager = null;

    PoolingHttpClientConnection(MerchantConfig mc, DocumentBuilder builder, LoggerWrapper logger) {
        super(mc, builder, logger);
        initializeConnectionManager();
        logger.log(Logger.LT_INFO, "Using PoolingHttpClient for connections.");
    }

    private void initializeConnectionManager() {
        if(connectionManager == null) {
            synchronized (PoolingHttpClientConnection.class) {
                if (connectionManager == null) {
                    connectionManager = new PoolingHttpClientConnectionManager();
                    connectionManager.setDefaultMaxPerRoute(mc.getDefaultMaxPerRoute());
                    connectionManager.setMaxTotal(mc.getMaxTotalConnections());
                }
            }
        }
        System.out.println(connectionManager);
    }

    @Override
    void postDocument(Document request) throws IOException, TransformerConfigurationException, TransformerException, MalformedURLException, ProtocolException, URISyntaxException {
        System.out.println("Using pooling http client");
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
                .setSocketTimeout(mc.getTimeout())
                .setConnectTimeout(mc.getTimeout());

        HttpClientBuilder httpClientBuilder = HttpClients.custom()
                .setRetryHandler(new CustomRetryHandler())
                .setConnectionManager(connectionManager)
                .setConnectionManagerShared(true);

        setProxy(httpClientBuilder, requestConfigBuilder);

        httpClient = httpClientBuilder
                .setDefaultRequestConfig(requestConfigBuilder.build())
                .build();

        httpPost = new HttpPost(mc.getEffectiveServerURL());
        StringEntity stringEntity = new StringEntity(documentToString(request), "UTF-8");
        httpPost.setEntity(stringEntity);
        httpPost.setHeader(Utility.ORIGIN_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
        logRequestHeaders();
        httpContext = HttpClientContext.create();
        httpResponse = httpClient.execute(httpPost, httpContext);
    }

    @Override
    public boolean isRequestSent() {
        return httpContext != null && httpContext.isRequestSent();
    }

    @Override
    public void release() {
        try {
            EntityUtils.consume(httpResponse.getEntity());
        } catch (IOException e) {
            httpPost.releaseConnection();
        }
    }

    @Override
    int getHttpResponseCode() throws IOException {
        return httpResponse != null ? httpResponse.getStatusLine().getStatusCode() : -1;
    }

    @Override
    InputStream getResponseStream() throws IOException {
        return httpResponse.getEntity().getContent();
    }

    @Override
    InputStream getResponseErrorStream() throws IOException {
        return getResponseStream();
    }

    @Override
    public void logRequestHeaders() {
        List<Header> reqHeaders = Arrays.asList(httpPost.getAllHeaders());
        logger.log(Logger.LT_INFO, "Request Headers: " + reqHeaders);

    }

    @Override
    public void logResponseHeaders() {
        List<Header> respHeaders = Arrays.asList(httpResponse.getAllHeaders());
        logger.log(Logger.LT_INFO, "Response Headers: " + respHeaders);
    }

    private String documentToString(Document request) throws IOException, TransformerException {
        try (ByteArrayOutputStream baos = makeStream(request)) {
            return baos.toString("UTF-8");
        }
    }

    private class CustomRetryHandler implements HttpRequestRetryHandler {
        long retryWaitInterval = mc.getRetryInterval();
        int maxRetries = mc.getNumberOfRetries();

        @Override
        public boolean retryRequest(IOException e, int i, HttpContext httpContext) {
            if(i > maxRetries) {
                return false;
            }

            if(e instanceof NoHttpResponseException) {
                return false;
            }

            HttpClientContext httpClientContext = HttpClientContext.adapt(httpContext);

            if(!httpClientContext.isRequestSent()) {
                try {
                    Thread.sleep(retryWaitInterval);
                    logger.log(Logger.LT_INFO, "Retrying Request -- " + mc.getUniqueKey() + " Retry Count -- " + i);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                return true;
            }
            return false;
        }
    }

    private void setProxy(HttpClientBuilder httpClientBuilder, RequestConfig.Builder requestConfigBuilder) {
        if(mc.getProxyHost() != null) {
            HttpHost proxy =  new HttpHost(mc.getProxyHost(), mc.getProxyPort());
            DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
            httpClientBuilder.setRoutePlanner(routePlanner);

            if(mc.getProxyUser() != null) {
                httpClientBuilder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());
                requestConfigBuilder.setProxyPreferredAuthSchemes(Collections.singletonList(AuthSchemes.BASIC));

                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(mc.getProxyUser(), mc.getProxyPassword()));

                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
        }
    }
}
