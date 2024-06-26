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

import java.io.File;
import java.text.MessageFormat;
import java.util.Properties;

/**
 * An internal class used by the clients to hold and derive the properties
 * applicable to the current transaction.
 */
public class MerchantConfig {
    /**
     * Prefix used when looking up properties in the System properties.
     */
    private final String SYSPROP_PREFIX = "cybs.";

    private final static int DEFAULT_TIMEOUT = 130;
    private final static int DEFAULT_PROXY_PORT = 8080;

    private final Properties props;

    private final String merchantID;
    private String keysDirectory;
    private String keyAlias;
    private String keyPassword;
    private boolean sendToProduction;
    private boolean sendToAkamai;
    private String targetAPIVersion;
    private String keyFilename;
    private String serverURL;
    private String namespaceURI;
    private String password;

    private boolean useHttpClientWithConnectionPool;
    private int maxConnections;
    private int defaultMaxConnectionsPerRoute;
    private int maxConnectionsPerRoute;
    private int connectionRequestTimeoutMs;
    private int connectionTimeoutMs;
    private int socketTimeoutMs;
    private int evictThreadSleepTimeMs;
    private int maxKeepAliveTimeMs;
    private int validateAfterInactivityMs;
    private boolean staleConnectionCheckEnabled;
    private boolean shutdownHookEnabled;
    private boolean retryIfMTIFieldExist;

    private boolean useHttpClient;

    //Retry Pattern
    private boolean allowRetry;
    private int numberOfRetries = 0;
    private long retryInterval = 0;

    @Deprecated
    private int timeout;

    private String proxyHost;
    private int proxyPort;
    private String proxyUser;
    private String proxyPassword;

    private boolean enableJdkCert;
    private boolean enableCacert;
    private boolean enableLog;
    private boolean logSignedData;
    private String logDirectory;
    private String logFilename;
    private int logMaximumSize;

    private String cacertPassword;
    private String customHttpClass;
    private boolean customHttpClassEnabled;

    private boolean certificateCacheEnabled;
    private boolean useSignAndEncrypted;
    private boolean merchantConfigCacheEnabled;

    // computed values
    private String effectiveServerURL;
    private String effectiveNamespaceURI;
    private String effectivePassword;

    /**
     *
     * @param _props      Properties object to get properties from.  May be
     *                    null, in which case, all properties will be read
     *                    from the System properties.
     * @param _merchantID merchantID.  May be null.  If specified, merchant-
     *                    specific properties will take precedence over
     *                    the generic ones (i.e. those that do not start
     *                    with a merchant id prefix).
     *
     *                    See README for more information.
     *
     * @throws ConfigException if something is missing of invalid in the
     *                         configuration.
     */
    public MerchantConfig(Properties _props, String _merchantID)
            throws ConfigException {
        props = _props;

        merchantID = _merchantID != null
                ? _merchantID
                : getProperty(null, "merchantID");

        if (merchantID == null) {
            throw new ConfigException("merchantID is required.");
        }

        keysDirectory = getProperty(merchantID, "keysDirectory");
        keyAlias = getProperty(merchantID, "keyAlias");
        keyPassword = getProperty(merchantID, "keyPassword");
        sendToProduction = getBooleanProperty(merchantID, "sendToProduction", false);
        sendToAkamai = getBooleanProperty(merchantID, "sendToAkamai", false);
        targetAPIVersion = getProperty(merchantID, "targetAPIVersion");
        keyFilename = getProperty(merchantID, "keyFilename");
        serverURL = getProperty(merchantID, "serverURL");
        namespaceURI = getProperty(merchantID, "namespaceURI");
        password = getProperty(merchantID, "password");
        enableLog = getBooleanProperty(merchantID, "enableLog", false);
        logSignedData = getBooleanProperty(merchantID, "logNonPCICompliantSignedData", false);
        logDirectory = getProperty(merchantID, "logDirectory");
        logFilename = getProperty(merchantID, "logFilename");
        logMaximumSize = getIntegerProperty(merchantID, "logMaximumSize", 10);
        useHttpClient = getBooleanProperty(merchantID, "useHttpClient", ConnectionHelper.getDefaultUseHttpClient());
        useHttpClientWithConnectionPool = getBooleanProperty(merchantID, "useHttpClientWithConnectionPool", false);
        customHttpClass = getProperty(merchantID, "customHttpClass");
        timeout = getIntegerProperty(merchantID, "timeout", DEFAULT_TIMEOUT);

        proxyHost = getProperty(merchantID, "proxyHost");
        proxyPort = getIntegerProperty(merchantID, "proxyPort", DEFAULT_PROXY_PORT);
        proxyUser = getProperty(merchantID, "proxyUser");
        proxyPassword = getProperty(merchantID, "proxyPassword");
        enableJdkCert = getBooleanProperty(merchantID, "enableJdkCert", false);
        enableCacert = getBooleanProperty(merchantID, "enableCacert", false);
        cacertPassword = getProperty(merchantID, "cacertPassword", "changeit");
        customHttpClassEnabled = getBooleanProperty(merchantID, "customHttpClassEnabled", false);
        certificateCacheEnabled = getBooleanProperty(merchantID, "certificateCacheEnabled", true);
        merchantConfigCacheEnabled = getBooleanProperty(merchantID, "merchantConfigCacheEnabled", false);
        // compute and store effective namespace URI

        if (namespaceURI == null && targetAPIVersion == null) {
            throw new ConfigException("namespaceURI or targetAPIVersion must be supplied.");
        }

        effectiveNamespaceURI =
                namespaceURI != null
                        ? namespaceURI
                        : "urn:schemas-cybersource-com:transaction-data-" +
                        targetAPIVersion;

        // compute and store effective Server URL

        if (serverURL == null && targetAPIVersion == null) {
            throw new ConfigException("serverURL or targetAPIVersion must be supplied.");
        }

        if (serverURL != null) {
            effectiveServerURL = serverURL;
        } else {
            int dotPos = targetAPIVersion.indexOf('.');
            String majorVersion
                    = dotPos >= 0
                    ? targetAPIVersion.substring(0, dotPos)
                    : targetAPIVersion;

            Object[] arguments = {majorVersion};
            effectiveServerURL = MessageFormat.format(
                    sendToAkamai
                            ? sendToProduction
                            ? "https://ics2wsa.ic3.com/commerce/{0}.x/transactionProcessor"
                            : "https://ics2wstesta.ic3.com/commerce/{0}.x/transactionProcessor"
                            : sendToProduction
                            ? "https://ics2ws.ic3.com/commerce/{0}.x/transactionProcessor"
                            : "https://ics2wstest.ic3.com/commerce/{0}.x/transactionProcessor",
                    arguments);
        }

        // compute and store effective password
        effectivePassword = password != null ? password : merchantID;

        useSignAndEncrypted = getBooleanProperty(merchantID, "useSignAndEncrypted", false);

        if (useHttpClientWithConnectionPool) {
            if (StringUtils.isEmpty(getProperty(merchantID, "maxConnections"))) {
                throw new ConfigException("maxConnections property is empty");
            } else {
                maxConnections = getIntegerProperty(merchantID, "maxConnections");
                if (maxConnections <= 0) {
                    throw new ConfigException("maxConnections property can't be 0 or negative");
                }
            }

            if (StringUtils.isEmpty(getProperty(merchantID, "defaultMaxConnectionsPerRoute"))) {
                throw new ConfigException("defaultMaxConnectionsPerRoute property is empty");
            } else {
                defaultMaxConnectionsPerRoute = getIntegerProperty(merchantID, "defaultMaxConnectionsPerRoute");
                if (defaultMaxConnectionsPerRoute <= 0) {
                    throw new ConfigException("defaultMaxConnectionsPerRoute property can't be 0 or negative");
                }
            }

            if (StringUtils.isEmpty(getProperty(merchantID, "maxConnectionsPerRoute"))) {
                throw new ConfigException("maxConnectionsPerRoute property is empty");
            } else {
                maxConnectionsPerRoute = getIntegerProperty(merchantID, "maxConnectionsPerRoute");
                if (maxConnectionsPerRoute <= 0) {
                    throw new ConfigException("maxConnectionsPerRoute property can't be 0 or negative");
                }
            }

            if (StringUtils.isEmpty(getProperty(merchantID, "connectionRequestTimeoutMs"))) {
                throw new ConfigException("connectionRequestTimeoutMs property is empty");
            } else {
                connectionRequestTimeoutMs = getIntegerProperty(merchantID, "connectionRequestTimeoutMs");
                if (connectionRequestTimeoutMs <= 0) {
                    throw new ConfigException("connectionRequestTimeoutMs property can't be 0 or negative");
                }
            }

            if (StringUtils.isEmpty(getProperty(merchantID, "connectionTimeoutMs"))) {
                throw new ConfigException("connectionTimeoutMs property is empty");
            } else {
                connectionTimeoutMs = getIntegerProperty(merchantID, "connectionTimeoutMs");
                if (connectionTimeoutMs <= 0) {
                    throw new ConfigException("connectionTimeoutMs property can't be 0 or negative");
                }
            }

            if (StringUtils.isEmpty(getProperty(merchantID, "socketTimeoutMs"))) {
                throw new ConfigException("socketTimeoutMs property is empty");
            } else {
                socketTimeoutMs = getIntegerProperty(merchantID, "socketTimeoutMs");
                if (socketTimeoutMs <= 0) {
                    throw new ConfigException("socketTimeoutMs property can't be 0 or negative");
                }
            }

            if (StringUtils.isEmpty(getProperty(merchantID, "evictThreadSleepTimeMs"))) {
                throw new ConfigException("evictThreadSleepTimeMs property is empty");
            } else {
                evictThreadSleepTimeMs = getIntegerProperty(merchantID, "evictThreadSleepTimeMs");
                if (evictThreadSleepTimeMs <= 0) {
                    throw new ConfigException("evictThreadSleepTimeMs property can't be 0 or negative");
                }
            }

            if (StringUtils.isEmpty(getProperty(merchantID, "maxKeepAliveTimeMs"))) {
                throw new ConfigException("maxKeepAliveTimeMs property is empty");
            } else {
                maxKeepAliveTimeMs = getIntegerProperty(merchantID, "maxKeepAliveTimeMs");
                if (maxKeepAliveTimeMs <= 0) {
                    throw new ConfigException("maxKeepAliveTimeMs property can't be 0 or negative");
                }
            }

            validateAfterInactivityMs = getIntegerProperty(merchantID, "validateAfterInactivityMs", 0);
            staleConnectionCheckEnabled = getBooleanProperty(merchantID, "staleConnectionCheckEnabled", true);
            shutdownHookEnabled = getBooleanProperty(merchantID, "enabledShutdownHook", true);
            retryIfMTIFieldExist = getBooleanProperty(merchantID, "retryIfMTIFieldExist", true);
        }else{
            if (StringUtils.isEmpty(getProperty(merchantID, "connectionTimeoutMs"))) {
                connectionTimeoutMs = timeout * 1000;
            } else {
                connectionTimeoutMs = getIntegerProperty(merchantID, "connectionTimeoutMs");
                if (connectionTimeoutMs <= 0) {
                    throw new ConfigException("connectionTimeoutMs property can't be 0 or negative");
                }
            }
            if (StringUtils.isEmpty(getProperty(merchantID, "socketTimeoutMs"))) {
                socketTimeoutMs = timeout * 1000;
            } else {
                socketTimeoutMs = getIntegerProperty(merchantID, "socketTimeoutMs");
                if (socketTimeoutMs <= 0) {
                    throw new ConfigException("socketTimeoutMs property can't be 0 or negative");
                }
            }
        }

        if (useHttpClient || useHttpClientWithConnectionPool) {
            allowRetry = getBooleanProperty(merchantID, "allowRetry", true);
            if (allowRetry) {
                numberOfRetries = getIntegerProperty(merchantID, "numberOfRetries", 3);
                if (numberOfRetries > 0)
                    retryInterval = getIntegerProperty(merchantID, "retryInterval", 1000);
                //added <=1 as in previous release it was set in seconds. User should change retryInterval value to keep it in ms.
                if (numberOfRetries < 1 || numberOfRetries > 5 || retryInterval <= 1) {
                    throw new ConfigException("Invalid value of numberOfRetries and/or retryInterval(in ms)");
                }
            }
        }
        if (isCacertEnabled()) {
            if (StringUtils.isBlank(keysDirectory)) {
                keysDirectory = System.getProperty("java.home") + "/lib/security".replace('/', File.separatorChar);
            }
            if (StringUtils.isBlank(keyFilename)) {
                keyFilename = "cacerts";
            }
        }
    }

    public boolean getUseSignAndEncrypted() {
        return useSignAndEncrypted;
    }

    public String getMerchantID() {
        return merchantID;
    }

    /**
     *
     * @return String keysDirectory path of p12 files
     */
    public String getKeysDirectory() {
        return keysDirectory;
    }

    /**
     * If keyAlias not null, return keyAlias, else return merchantId
     *
     * @return String
     */
    public String getKeyAlias() {
        if (keyAlias != null)
            return keyAlias;
        else
            return getMerchantID();
    }

    /**
     * If keyPassword not null, return keyPassword, else return merchantId
     *
     * @return String
     */
    public String getKeyPassword() {
        if (keyPassword != null)
            return keyPassword;
        else
            return getMerchantID();
    }

    public boolean getSendToProduction() {
        return sendToProduction;
    }

    public boolean getSendToAkamai() {
        return sendToAkamai;
    }

    public String getTargetAPIVersion() {
        return targetAPIVersion;
    }

    public String getKeyFilename() {
        return keyFilename;
    }

    public String getServerURL() {
        return serverURL;
    }

    public String getNamespaceURI() {
        return namespaceURI;
    }

    public String getPassword() {
        return password;
    }

    public boolean getEnableLog() {
        return enableLog;
    }

    public boolean getLogSignedData() {
        return logSignedData;
    }

    public String getLogDirectory() {
        return logDirectory;
    }

    public String getLogFilename() {
        return logFilename;
    }

    public int getLogMaximumSize() {
        return logMaximumSize;
    }

    /**
     * retryIfMTIFieldExist If enabled then SDK will retry the transaction in case when connection pooling http client is used
     * and if SDK receives an I/O error/exception, when executing a request over a connection that has been closed at the server side.
     *
     * If not enabled, a transaction may fail(retry wont occur in some cases) if while sending transaction SDK receives an I/O error/exception, when executing
     * a request over a connection that has been closed at the server side.
     * @return boolean
     */
    public boolean retryIfMTIFieldExistEnabled() {
        return retryIfMTIFieldExist;
    }

    public boolean getUseHttpClient() {
        return useHttpClient;
    }

    public boolean getUseHttpClientWithConnectionPool() {
        return useHttpClientWithConnectionPool;
    }

    /**
     * Timeout is set in seconds default is 130seconds
     * And it used as connectionTimeout and SocketTimeout in case of Jdk HttpUrlConnection and Apache's HttpClient.
     *
     * @return int
     */
    @Deprecated
    public int getTimeout() {
        return timeout;
    }

    /**
     * Specifies the maximum number of concurrent, active HTTP connections allowed by the resource instance to be opened with the target service.
     * There is no default value. For applications that create many long-lived connections, increase the value of this parameter
     *
     * @return int
     */
    public int getMaxConnections() {
        return maxConnections;
    }

    /**
     * the maximum number of connections per (any) route
     *
     * @return int
     */
    public int getDefaultMaxConnectionsPerRoute() {
        return defaultMaxConnectionsPerRoute;
    }

    /**
     * Specifies the maximum number of concurrent, active HTTP connections allowed by the resource instance to the same host or route.
     * In SDK, all above config does same functionality and the same value can be given to these configs as we have only one route.
     *
     * Note: This number cannot be greater than Maximum Total Connections and every connection created here also counts into Maximum Total Connections.
     *
     * @return int
     */
    public int getMaxConnectionsPerRoute() {
        return maxConnectionsPerRoute;
    }

    /**
     * Time taken in milliseconds to get connection request from the pool.
     * If it times out, it will throw error as Timeout waiting for connection from pool.
     *
     * @return int
     */
    public int getConnectionRequestTimeoutMs() {
        return connectionRequestTimeoutMs;
    }

    /**
     * Specifies the number of milliseconds to wait while a connection is being established.
     *
     * @return int
     */
    public int getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }

    /**
     * Specifies the time waiting for data – after establishing the connection; maximum time of inactivity between two data packets.
     *
     * @return int
     */
    public int getSocketTimeoutMs() {
        return socketTimeoutMs;
    }

    /**
     *  Specifies time duration in milliseconds between "sweeps" by the "idle connection" evictor thread.
     *
     * @return int
     */
    public int getEvictThreadSleepTimeMs() {
        return evictThreadSleepTimeMs;
    }

    /**
     * Specifies the time duration in milliseconds that a connection can be idle before it is evicted from the pool.
     *
     * @return int
     */
    public int getMaxKeepAliveTimeMs() {
        return maxKeepAliveTimeMs;
    }

    /**
     * Defines period of inactivity in milliseconds after which persistent connections must be re-validated prior to being
     * leased to the consumer. Non-positive value passed to this method disables connection validation.
     * This check helps detect connections that have become stale (half-closed) while kept inactive in the pool.
     *
     * By default it is set to 0. This value can be set a positive value, if in case you decide to disable staleConnectionCheckEnabled
     * to get slight better performance. We recommended a value of 2000ms.
     * @return int
     */
    public int getValidateAfterInactivityMs() {
        return validateAfterInactivityMs;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public String getProxyPassword() {
        return proxyPassword != null ? proxyPassword : "";
    }

    /**
     * If this property is set to false then the p12 certificate of a merchant will be reloaded
     * every time a transaction is made
     *
     * @return boolean
     */
    public boolean isCertificateCacheEnabled() {
        return certificateCacheEnabled;
    }

    /**
     * If this property is set to true (default value is false) it will cache the merchantConfig object based on keyAlias/merchantID
     * If cache enabled is true, for single merchant id, if you change any properties after first initialization, it will not reflect
     *
     * @return boolean
     */
    public boolean isMerchantConfigCacheEnabled() {
        return merchantConfigCacheEnabled;
    }

    /**
     * It determines whether the stale connection check is to be used. Disabling the stale connection check can result in slight performance improvement
     * at the risk of getting an I/O error, when executing a request over a connection that has been closed at the server side.
     * By default it is set to true, which means it is enabled.
     *
     * @return boolean
     */
    public boolean isStaleConnectionCheckEnabled() {
        return staleConnectionCheckEnabled;
    }

    /**
     * This is to enable shutdown hook in case of httpclient with connection Polling.
     *
     * ShutdownHook method will call static shutdown api to close connectionManager, httpClient and IdleCleanerThread. By default this is enabled.
     * We should close the connection manager, http client and idle connection cleaner thread when application get shutdown both abruptly and gracefully.
     * If `enabledShutdownHook` is true, then JVM runtime addShutdownHook method will be initialized. Shutdown Hooks are a special construct that allows
     * developers to plug in a piece of code to be executed when the JVM is shutting down. This comes in handy in cases where we need to do special clean-up
     * operations in case the VM is shutting down.
     *
     *
     * @return boolean
     */
    public boolean isShutdownHookEnabled() {
        return shutdownHookEnabled;
    }



    /**
     * Returns the effective server URL to which the request will be sent.
     * If a serverURL is specified, then that is what is returned.
     * Otherwise, the effective server URL is derived from the values of
     * sendToAkamai, sendToProduction and targetAPIVersion.
     *
     * @return the effective server URL.
     */
    public String getEffectiveServerURL() {
        return effectiveServerURL;
    }


    /**
     * Returns the effective namespace URI to be used to parse the request and
     * reply documents.  If a namespaceURI is specified, then that is
     * what is returned.  Otherwise, the effective namespace URI is derived
     * from the value of targetAPIVersion.
     *
     * @return the effective namespace URI.
     */
    public String getEffectiveNamespaceURI() {
        return effectiveNamespaceURI;
    }

    /**
     * Returns the effective key password.  If a password is specified, then
     * that is what is returned.  Otherwise, the effective password is
     * the same as the merchantID.
     *
     * @return the effective key password.
     */
    public String getEffectivePassword() {
        return effectivePassword;
    }

    /**
     * Returns a File object representing the key file.  If a
     * keyFilename is specified, that will be the one used.
     * Otherwise, the filename will be derived from the merchantID.
     *
     * @throws ConfigException if the file is missing, is not a file, or is
     *                         not readable.
     */
    public File getKeyFile()
            throws ConfigException {
        File file;
        if (StringUtils.isBlank(keyFilename)) {
            file = new File(keysDirectory, merchantID + ".p12");
        } else {
            file = new File(keysDirectory, keyFilename);
        }
        String fullPath = file.getAbsolutePath();
        if (!file.isFile()) {
            throw new ConfigException(
                    "The file \"" + fullPath + "\" is missing or is not a file.");
        }
        if (!file.canRead()) {
            throw new ConfigException(
                    "This application does not have permission to read the file \""
                            + fullPath + "\".");
        }

        return (file);
    }


    /**
     * Returns a File object representing the log file.
     *
     * @throws ConfigException if the directory specified for the log file is
     *                         missing or is not a directory.
     */
    public File getLogFile()
            throws ConfigException {
        File dir = new File(logDirectory);
        String fullPath = dir.getAbsolutePath();
        if (!dir.isDirectory()) {
            throw new ConfigException(
                    "The log directory \"" + fullPath +
                            "\" is missing or is not a directory.");
        }

        return (new File(logDirectory,
                logFilename != null ? logFilename : "cybs.log"));

    }


    /**
     * Returns the value of the specified property.  See the other version
     * of this method for the complete behavior.
     *
     * @param merchantID merchant id.
     * @param prop       property to search for.
     * @return the value of the specified property or <code>null</code> if none
     * is found.
     */
    public String getProperty(String merchantID, String prop) {
        return (getProperty(merchantID, prop, null));
    }

    /**
     * Returns the value of the specified property.  The search behavior is
     * as follows:
     * <ol>
     * <li> In the Properties object supplied in the constructor, it looks for
     * the property whose format is "merchantID.prop", e.g.
     * "myMerchant.keysDirectory", unless the merchantID parameter is null,
     * in which case, this step is skipped.  If the Properties object is
     * null, this and the second step are skipped.
     * <li> If it doesn't find one, it looks in the same Properties object for
     * the property without the merchantID prefix, e.g. "keysDirectory".
     * <li> If it doesn't find one, it repeats steps 1 and 2, but looking in
     * the System properties this time.
     * <li> If none is found, it returns the default value specified.
     * </ol>
     *
     * @param merchantID merchant id.
     * @param prop       property to search for.
     * @param defaultVal default value to return if property is not found
     *                   (may be null).
     * @return the value of the specified property or the default value
     * specified if none is found.
     */
    public String getProperty(
            String merchantID, String prop, String defaultVal) {
        String val = null;

        String merchantSpecificProp =
                (merchantID != null) ? merchantID + "." + prop : null;

        // look-up the merchant-specific property in the supplied
        // Properties object.
        if (props != null && merchantSpecificProp != null) {
            val = props.getProperty(merchantSpecificProp);
        }

        // if none, look up the generic property.
        if (props != null && val == null) {
            val = props.getProperty(prop);
        }

        // if none, look up the merchant-specific property in the System
        // properties.
        if (val == null && merchantSpecificProp != null) {
            val = System.getProperty(SYSPROP_PREFIX + merchantSpecificProp);
        }

        // if none, look up the generic property in the System properties.
        if (val == null) {
            val = System.getProperty(SYSPROP_PREFIX + prop);
        }

        // if none, return default value
        if (val == null) {
            val = defaultVal;
        }

        return (val);
    }

    /**
     * Returns a string representation of the properties for logging purposes.
     *
     * @return String
     */
    public String getLogString() {

        StringBuffer sb = new StringBuffer();
        appendPair(sb, "merchantID", merchantID);
        appendPair(sb, "keysDirectory", keysDirectory);
        appendPair(sb, "keyAlias", keyAlias);
        appendPair(sb, "keyPassword", keyPassword != null ? "(masked)" : null);
        appendPair(sb, "sendToProduction", sendToProduction);
        appendPair(sb, "sendToAkamai", sendToAkamai);
        appendPair(sb, "targetAPIVersion", targetAPIVersion);
        appendPair(sb, "keyFilename", keyFilename);
        appendPair(sb, "serverURL", serverURL);
        appendPair(sb, "namespaceURI", namespaceURI);
        appendPair(sb, "enableLog", enableLog);
        appendPair(sb, "logDirectory", logDirectory);
        appendPair(sb, "logFilename", logFilename);
        appendPair(sb, "logMaximumSize", logMaximumSize);
        appendPair(sb, "customHttpClass", customHttpClass);
        appendPair(sb, "customHttpClassEnabled", customHttpClassEnabled);
        appendPair(sb, "useHttpClient", useHttpClient);
        appendPair(sb, "useHttpClientWithConnectionPool", useHttpClientWithConnectionPool);
        appendPair(sb, "enableJdkCert", enableJdkCert);
        appendPair(sb, "enableCacert", enableCacert);
        if (useHttpClient || useHttpClientWithConnectionPool) {
            appendPair(sb, "allowRetry", allowRetry);
            appendPair(sb, "retryCount", numberOfRetries);
            appendPair(sb, "retryInterval", retryInterval);
        }
        if (useHttpClientWithConnectionPool) {
            appendPair(sb, "maxConnections", maxConnections);
            appendPair(sb, "defaultMaxConnectionsPerRoute", defaultMaxConnectionsPerRoute);
            appendPair(sb, "maxConnectionsPerRoute", maxConnectionsPerRoute);
            appendPair(sb, "connectionRequestTimeoutMs", connectionRequestTimeoutMs);
            appendPair(sb, "connectionTimeoutMs", connectionTimeoutMs);
            appendPair(sb, "maxKeepAliveTimeMs", maxKeepAliveTimeMs);
            appendPair(sb, "validateAfterInactivityMs", validateAfterInactivityMs);
            appendPair(sb, "staleConnectionCheckEnabled", staleConnectionCheckEnabled);
            appendPair(sb, "enabledShutdownHook", shutdownHookEnabled);
            appendPair(sb, "retryIfMTIFieldExist", retryIfMTIFieldExist);
        }
        appendPair(sb, "timeout", timeout);
        appendPair(sb, "socketTimeoutMs", socketTimeoutMs);
        appendPair(sb, "evictThreadSleepTimeMs", evictThreadSleepTimeMs);
        if (proxyHost != null) {
            appendPair(sb, "proxyHost", proxyHost);
            appendPair(sb, "proxyPort", proxyPort);
            if (proxyUser != null) {
                appendPair(sb, "proxyUser", proxyUser);
                appendPair(sb, "proxyPassword",
                        proxyPassword != null
                                ? "(masked)" : null);
            }
        }
        appendPair(sb, "useSignAndEncrypted", useSignAndEncrypted);
        appendPair(sb, "certificateCacheEnabled", certificateCacheEnabled);
        appendPair(sb, "merchantConfigCacheEnabled", merchantConfigCacheEnabled);
        return (sb.toString());
    }

    private void appendPair(StringBuffer sb, String key, long retryInterval2) {
        appendPair(sb, key, String.valueOf(retryInterval2));

    }

    private static void appendPair(StringBuffer sb, String key, boolean value) {
        appendPair(sb, key, String.valueOf(value));
    }

    private static void appendPair(StringBuffer sb, String key, int value) {
        appendPair(sb, key, String.valueOf(value));
    }

    private static void appendPair(StringBuffer sb, String key, String value) {
        if (sb.length() > 0) {
            sb.append(", ");
        }

        sb.append(key + "=");
        sb.append(value != null ? value : "(null)");
    }

    private boolean getBooleanProperty(
            String merchantID, String prop, boolean defaultVal)
            throws ConfigException {
        String strValue = getProperty(merchantID, prop);
        if (strValue == null) return defaultVal;

        if ("1".equals(strValue) || "true".equalsIgnoreCase(strValue)) {
            return (true);
        }

        if ("0".equals(strValue) || "false".equalsIgnoreCase(strValue)) {
            return (false);
        }

        throw new ConfigException(prop + " has an invalid value.");
    }

    private int getIntegerProperty(
            String merchantID, String prop, int defaultVal)
            throws ConfigException {
        String strValue = getProperty(merchantID, prop);
        if (strValue == null) return defaultVal;

        try {
            return (Integer.parseInt(strValue));
        } catch (NumberFormatException nfe) {
            throw new ConfigException(prop + " has an invalid value.", nfe);
        }
    }

    private int getIntegerProperty(
            String merchantID, String prop)
            throws ConfigException {
        String strValue = getProperty(merchantID, prop);
        try {
            return (Integer.parseInt(strValue));
        } catch (NumberFormatException nfe) {
            throw new ConfigException(prop + " has an invalid value.", nfe);
        }
    }

    /**
     * Getter method for numberOfRetries
     *
     * @return int
     */
    public int getNumberOfRetries() {
        return numberOfRetries;
    }

    /**
     * Getter method for retryInterval
     * added <=1 check as in previous release(6.2.9 or lower) it was set in seconds. User should change retryInterval value to keep it in ms
     *
     * @return long
     */
    public long getRetryInterval() {
        return retryInterval;
    }

    /**
     * Getter method for allowRetry
     *
     * @return boolean
     */
    public boolean isAllowRetry() {
        return allowRetry;
    }

    /**
     * Setter method for allowRetry
     *
     * @param allowRetry
     */
    public void setAllowRetry(boolean allowRetry) {
        this.allowRetry = allowRetry;
    }

    /**
     * Getter method for enableCacert
     *
     * @return boolean
     */
    public boolean isCacertEnabled() {
        return enableCacert;
    }

    /**
     * Getter method for enableJdkCert
     *
     * @return boolean
     */
    public boolean isJdkCertEnabled() {
        return enableJdkCert;
    }

    /**
     * Getter method for cacertPassword
     *
     * @return String
     */
    public String getCacertPassword() {
        return cacertPassword;
    }

    public String getCustomHttpClass() {
        return customHttpClass;
    }

    public boolean isCustomHttpClassEnabled() {
        return customHttpClassEnabled;
    }

}
