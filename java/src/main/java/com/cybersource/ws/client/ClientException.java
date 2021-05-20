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

import java.net.HttpURLConnection;

/**
 * Used by both Basic and XML clients.
 * All exceptions other than faults are wrapped in ClientException.
 */
public class ClientException
        extends Exception {
    private Exception innerException;
    private boolean critical = false;
    private int httpStatusCode = -1;
    private String httpError;

    /**
     *
     * @param _innerException the actual exception that occurred.
     * @param logger          used to log the exception details.
     */
    public ClientException(
            Exception _innerException, Logger logger) {
        super(_innerException);
        innerException = _innerException;
        log(logger);
    }

    /**
     *
     * @param _innerException the actual exception that occurred.
     * @param _critical       flag that tells whether or not the exception
     *                        occurred at a critical point in the
     *                        transaction.
     * @param logger          used to log the exception details.
     */
    public ClientException(
            Exception _innerException, boolean _critical, Logger logger) {
        super(_innerException);
        innerException = _innerException;
        critical = _critical;
        log(logger);
    }

    /**
     *
     * @param _httpStatusCode HTTP status code
     * @param logger          used to log the exception details.
     */
    public ClientException(int _httpStatusCode, Logger logger) {
        httpStatusCode = _httpStatusCode;
        critical
                = (_httpStatusCode == HttpURLConnection.HTTP_GATEWAY_TIMEOUT);
        log(logger);
    }

    /**
     *
     * @param _httpStatusCode HTTP status code
     * @param _httpError      Additional HTTP error information; may be null.
     * @param logger          used to log the exception details.
     */
    public ClientException(
            int _httpStatusCode, String _httpError, Logger logger) {
        super(_httpError);
        httpStatusCode = _httpStatusCode;
        critical
                = (_httpStatusCode == HttpURLConnection.HTTP_GATEWAY_TIMEOUT);
        httpError = _httpError;
        log(logger);
    }

    /**
     *
     * @param _httpStatusCode HTTP status code
     * @param _httpError      Additional HTTP error information; may be null.
     * @param _critical       flag that tells whether or not the exception
     *                        occurred at a critical point in the
     *                        transaction.
     * @param logger          used to log the exception details.
     */
    public ClientException(
            int _httpStatusCode, String _httpError, boolean _critical,
            Logger logger) {
        this(_httpStatusCode, _httpError, logger);

        // if critical is already true (the other constructor invoked in the
        // first line may set it to true), don't bother setting it as we don't
        // want to inadvertently reset it to false here.
        if (!critical) {
            critical = _critical;
        }

        log(logger);
    }

    /**
     * Returns the actual exception that occurred, if any.
     *
     * @return the actual exception that occurred or <code>null</code> if this
     *         object was constructed using an HTTP status code.
     */
    public Exception getInnerException() {
        return (innerException);
    }

    /**
     * Returns whether or not this exception is critical.  It will return
     * <code>true</code> in the following cases:
     * <ul>
     * <li>An exception occurred while parsing the reply or fault.
     * <li>An HTTP status code of
     * <code>HttpURLConnection.HTTP_GATEWAY_TIMEOUT</code> was returned.
     * </ul>
     *
     * @return whether or not this exception is critical.
     */
    public boolean isCritical() {
        return (critical);
    }

    /**
     * Returns the HTTP status code, if any.
     *
     * @return the HTTP status code or -1 if this object was constructed using
     *         an actual exception that occurred.
     */
    public int getHttpStatusCode() {
        return (httpStatusCode);
    }

    /**
     * Returns additional HTTP error information, if any.
     *
     * @return additional HTTP error information or <code>null</code> if this
     *         object was constructed using an actual exception that occurred.
     */
    public String getHttpError() {
        return (httpError);
    }

    /**
     * Logs the exception details.
     *
     * @param logger used to log the exception details.
     */
    void log(Logger logger) {
        if (logger != null) {
            logger.log(Logger.LT_EXCEPTION, getLogString());
        }
    }

    /**
     * Returns a string representation of the object for logging purposes.
     *
     * @return a string representation of the object for logging purposes.
     */
    String getLogString() {
        StringBuilder sb = new StringBuilder("ClientException details:\n");

        if (critical) {
            sb.append("CRITICAL\n");
        }

        if (httpStatusCode != -1) {
            sb.append("httpStatusCode = ").append(httpStatusCode).append("\n");
        }

        if (httpError != null) {
            sb.append("httpError = ").append(httpError).append("\n");
        }

        if (innerException != null) {
            sb.append("innerException: \n").append(Utility.getStackTrace(innerException));
        } else {
            sb.append("Stack trace: \n").append(Utility.getStackTrace(this));
        }

        return (sb.toString());
    }

    /**
     * Returns a description of the exception.
     *
     * @return a description of the exception.
     */
    public String getMessage() {
        if (innerException != null) return innerException.getMessage();

        StringBuilder sb = new StringBuilder("ClientException:");
        if (httpStatusCode != -1) {
            sb.append(" (").append(httpStatusCode).append(")");
        }

        if (httpError != null) {
            sb.append(" ").append(httpError);
        }

        if (critical) {
            sb.append(" (CRITICAL)");
        }

        return sb.toString();
    }
}