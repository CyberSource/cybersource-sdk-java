package com.cybersource.ws.client;


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
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PoolingHttpClientConnection extends Connection {
    private HttpPost httpPost = null;
    private CloseableHttpResponse httpResponse = null;
    private static CloseableHttpClient httpClient = null;
    private static IdleConnectionMonitorThread staleMonitorThread;
    private final static String STALE_CONNECTION_MONITOR_THREAD_NAME = "http-stale-connection-cleaner-thread";
    private static PoolingHttpClientConnectionManager connectionManager = null;

    PoolingHttpClientConnection(MerchantConfig mc, DocumentBuilder builder, LoggerWrapper logger) throws ClientException {
        super(mc, builder, logger);
        initializeConnectionManager();
        logger.log(Logger.LT_INFO, "Using PoolingHttpClient for connections.");
    }

    private void initializeConnectionManager() throws ClientException {
        if (connectionManager == null) {
            synchronized (PoolingHttpClientConnection.class) {
                if (connectionManager == null) {
                    String url = mc.getEffectiveServerURL();
                    try {
                        URI uri = new URI(url);
                        String hostname = uri.getHost();
                        connectionManager = new PoolingHttpClientConnectionManager();
                        connectionManager.setDefaultMaxPerRoute(mc.getDefaultMaxConnectionsPerRoute());
                        connectionManager.setMaxTotal(mc.getMaxConnections());
                        final HttpHost httpHost = new HttpHost(hostname);
                        connectionManager.setMaxPerRoute(new HttpRoute(httpHost), mc.getMaxConnectionsPerRoute());
                        initHttpClient();
                    } catch (URISyntaxException e) {
                        logger.log(Logger.LT_FAULT, "invalid server url");
                        throw new ClientException(e, logger);

                    }
                }
            }
        }
    }

    protected void initHttpClient() {
        staleMonitorThread = new IdleConnectionMonitorThread(connectionManager, mc.getEvictThreadSleepTimeMs(), mc.getMaxKeepAliveTimeMs());
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
                .setSocketTimeout(mc.getSocketTimeoutMs())
                .setConnectionRequestTimeout(mc.getConnectionRequestTimeoutMs())
                .setConnectTimeout(mc.getConnectionTimeoutMs());

        HttpClientBuilder httpClientBuilder = HttpClients.custom()
                .setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE)
                 .setRetryHandler(new CustomRetryHandler())
                .setConnectionManager(connectionManager);

        setProxy(httpClientBuilder, requestConfigBuilder);

        httpClient = httpClientBuilder
                .disableConnectionState()
                .setDefaultRequestConfig(requestConfigBuilder.build())
                .build();
        staleMonitorThread.setName(STALE_CONNECTION_MONITOR_THREAD_NAME);
        staleMonitorThread.setDaemon(true);
        staleMonitorThread.start();
    }

    @Override
    void postDocument(Document request) throws IOException, TransformerException {

        String serverURL = mc.getEffectiveServerURL();
        httpPost = new HttpPost(serverURL);
        String requestString = documentToString(request);
        logger.log(Logger.LT_INFO,
                "Sending " + requestString.length() + " bytes to " + serverURL);
        StringEntity stringEntity = new StringEntity(requestString, "UTF-8");
        httpPost.setEntity(stringEntity);
        httpPost.setHeader(Utility.ORIGIN_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
        logRequestHeaders();
        httpResponse = httpClient.execute(httpPost);
    }

    @Override
    public boolean isRequestSent() {
        return true;
    }

    @Override
    public void release() throws ClientException {
        try {
            EntityUtils.consume(httpResponse.getEntity());
            httpResponse.close();
        } catch (IOException e) {
            //need to check this part
            //httpPost.releaseConnection();
            throw new ClientException(e, logger);
        }
    }

    @Override
    int getHttpResponseCode() {
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
        ByteArrayOutputStream baos = null;
        try {
            baos = makeStream(request);
            return baos.toString("utf-8");
        } finally {
            if (baos != null) {
                baos.close();
            }
        }
    }

    private class CustomRetryHandler implements HttpRequestRetryHandler {
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

            HttpClientContext httpClientContext = HttpClientContext.adapt(httpContext);

            if(!httpClientContext.isRequestSent()) {
                try {
                    Thread.sleep(retryWaitInterval);
                    logger.log(Logger.LT_INFO, "Retrying Request -- " + mc.getUniqueKey() + " Retry Count -- " + executionCount);
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
