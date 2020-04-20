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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.cybersource.ws.client.Utility.*;
/**
 * Class creates pooling http client connection flow. It maintains a pool of
 * http client connections and is able to service connection requests
 * from multiple execution threads.
 */
public class PoolingHttpClientConnection extends Connection {
    private HttpPost httpPost = null;
    private HttpClientContext httpContext = null;
    private CloseableHttpResponse httpResponse = null;
    private static CloseableHttpClient httpClient = null;
    private static IdleConnectionMonitorThread staleMonitorThread;
    private final static String STALE_CONNECTION_MONITOR_THREAD_NAME = "http-stale-connection-cleaner-thread";
    private static PoolingHttpClientConnectionManager connectionManager = null;

    /**
     * Constructor.
     * @param mc
     * @param builder
     * @param logger
     * @throws ClientException
     */
    PoolingHttpClientConnection(MerchantConfig mc, DocumentBuilder builder, LoggerWrapper logger) throws ClientException {
        super(mc, builder, logger);
        initializeConnectionManager(mc);
        logger.log(Logger.LT_INFO, "Using PoolingHttpClient for connections.");
    }

    /**
     * Initialize pooling connection manager with max connections based on properties
     * @param merchantConfig
     * @throws ClientException
     */
    private void initializeConnectionManager(MerchantConfig merchantConfig) throws ClientException {
        if (connectionManager == null) {
            synchronized (PoolingHttpClientConnection.class) {
                if (connectionManager == null) {
                    String url = merchantConfig.getEffectiveServerURL();
                    try {
                        URI uri = new URI(url);
                        String hostname = uri.getHost();
                        connectionManager = new PoolingHttpClientConnectionManager();
                        connectionManager.setDefaultMaxPerRoute(merchantConfig.getDefaultMaxConnectionsPerRoute());
                        connectionManager.setMaxTotal(merchantConfig.getMaxConnections());
                        connectionManager.setValidateAfterInactivity(mc.getValidateAfterInactivityMs());
                        final HttpHost httpHost = new HttpHost(hostname);
                        connectionManager.setMaxPerRoute(new HttpRoute(httpHost), merchantConfig.getMaxConnectionsPerRoute());
                        initHttpClient(merchantConfig, connectionManager);
                        startStaleConnectionMonitorThread(merchantConfig, connectionManager);
                        if(merchantConfig.isShutdownHookEnabled()) {
                            addShutdownHook();
                        }
                    } catch (Exception e) {
                        logger.log(Logger.LT_FAULT, "invalid server url");
                        throw new ClientException(e, logger);
                    }
                }
            }
        }
    }

    /**
     * Initialize Http Client
     * @param merchantConfig
     * @param poolingHttpClientConnManager
     */
    protected void initHttpClient(MerchantConfig merchantConfig, PoolingHttpClientConnectionManager poolingHttpClientConnManager) {
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
                .setSocketTimeout(merchantConfig.getSocketTimeoutMs())
                .setConnectionRequestTimeout(merchantConfig.getConnectionRequestTimeoutMs())
                .setConnectTimeout(merchantConfig.getConnectionTimeoutMs());

        HttpClientBuilder httpClientBuilder = HttpClients.custom()
                .setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE)
                .setRetryHandler(new CustomRetryHandler())
                .setConnectionManager(poolingHttpClientConnManager);

        setProxy(httpClientBuilder, requestConfigBuilder, merchantConfig);

        httpClient = httpClientBuilder
                .setDefaultRequestConfig(requestConfigBuilder.build())
                .build();
    }

    /**
     * Initialize thread to clean Idle/Stale/Expired connections
     * @param merchantConfig
     * @param poolingHttpClientConnManager
     */
    private void startStaleConnectionMonitorThread(MerchantConfig merchantConfig, PoolingHttpClientConnectionManager poolingHttpClientConnManager) {
        staleMonitorThread = new IdleConnectionMonitorThread(poolingHttpClientConnManager, merchantConfig.getEvictThreadSleepTimeMs(), merchantConfig.getMaxKeepAliveTimeMs());
        staleMonitorThread.setName(STALE_CONNECTION_MONITOR_THREAD_NAME);
        staleMonitorThread.setDaemon(true);
        staleMonitorThread.start();
    }

    /**
     * Method to post the request using http pool connection
     * @param request
     * @param requestSentTime
     * @throws IOException
     * @throws TransformerException
     */
    @Override
    void postDocument(Document request, long requestSentTime) throws IOException, TransformerException {
        String serverURL = mc.getEffectiveServerURL();
        httpPost = new HttpPost(serverURL);
        String requestString = documentToString(request);
        StringEntity stringEntity = new StringEntity(requestString, "UTF-8");
        httpPost.setEntity(stringEntity);
        httpPost.setHeader(Utility.SDK_ELAPSED_TIMESTAMP, String.valueOf(System.currentTimeMillis() - requestSentTime));
        httpPost.setHeader(Utility.ORIGIN_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
        logRequestHeaders();
        httpContext = HttpClientContext.create();
        logger.log(Logger.LT_INFO,
                "Sending " + requestString.length() + " bytes to " + serverURL);
        httpResponse = httpClient.execute(httpPost, httpContext);
    }

    /**
     * Method to check whether request sent or not
     * @return boolean
     */
    @Override
    public boolean isRequestSent() {
        return httpContext != null && httpContext.isRequestSent();
    }

    /**
     * Enable JVM runtime shutdown hook
     */
    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(this.createShutdownHookThread());
    }

    /**
     * Thread which calls shutdown method
     * @return
     */
    private Thread createShutdownHookThread() {
        return new Thread() {
            public void run() {
                try {
                    PoolingHttpClientConnection.this.onShutdown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    /**
     * Method to close the httpClient, connectionManager, staleMonitorThread
     * when application got shutdown
     * @throws IOException
     */
    public static void onShutdown() throws IOException {
        System.out.println("Triggered sdk shut down");
        if (httpClient != null) {
            httpClient.close();
        }
        if (connectionManager != null) {
            connectionManager.close();
        }
        if (staleMonitorThread != null && staleMonitorThread.isAlive()) {
            staleMonitorThread.shutdown();
        }
    }

    /**
     * Method to close httpResponse
     * @throws ClientException
     */
    @Override
    public void release() throws ClientException {
        try {
            if(httpResponse != null) {
                EntityUtils.consume(httpResponse.getEntity());
                httpResponse.close();
            }
        } catch (IOException e) {
            throw new ClientException(e, logger);
        }
    }

    /**
     * Method to get http response code
     * @return int
     */
    @Override
    int getHttpResponseCode() {
        return httpResponse != null ? httpResponse.getStatusLine().getStatusCode() : -1;
    }

    /**
     * Method to get response stream
     * @return InputStream
     * @throws IOException
     */
    @Override
    InputStream getResponseStream() throws IOException {
        return httpResponse != null ? httpResponse.getEntity().getContent() : null;
    }

    /**
     * Method to get response error stream
     * @return InputStream
     * @throws IOException
     */
    @Override
    InputStream getResponseErrorStream() throws IOException {
        return getResponseStream();
    }

    /**
     * Log Request Headers
     */
    @Override
    public void logRequestHeaders() {
        if(mc.getEnableLog() && httpPost != null) {
            List<Header> reqHeaders = Arrays.asList(httpPost.getAllHeaders());
            logger.log(Logger.LT_INFO, "Request Headers: " + reqHeaders);
        }

    }

    /**
     * Log Response Headers
     */
    @Override
    public void logResponseHeaders() {
        if(mc.getEnableLog() && httpResponse != null) {
            Header responseTimeHeader = httpResponse.getFirstHeader(RESPONSE_TIME_REPLY);
            if (responseTimeHeader != null && StringUtils.isNotBlank(responseTimeHeader.getValue())) {
                long resIAT = getResponseIssuedAtTimeInSecs(responseTimeHeader.getValue());
                if (resIAT > 0) {
                    logger.log(Logger.LT_INFO, "responseTransitTimeSec : " + getResponseTransitTimeSeconds(resIAT));
                }
            }
            List<Header> respHeaders = Arrays.asList(httpResponse.getAllHeaders());
            logger.log(Logger.LT_INFO, "Response Headers: " + respHeaders);
        }
    }

    /**
     * Conver Document to String
     * @param request
     * @return
     * @throws IOException
     * @throws TransformerException
     */
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

    /**
     * Inner class for custom retry handling
     */
    private class CustomRetryHandler implements HttpRequestRetryHandler {
        long retryWaitInterval = mc.getRetryInterval();
        int maxRetries = mc.getNumberOfRetries();

        @Override
        public boolean retryRequest(IOException exception, int executionCount, HttpContext httpContext) {
            if (executionCount > maxRetries) {
                return false;
            }

            if (exception instanceof NoHttpResponseException) {
                return false;
            }

            HttpClientContext httpClientContext = HttpClientContext.adapt(httpContext);

            if (!httpClientContext.isRequestSent()) {
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
}
