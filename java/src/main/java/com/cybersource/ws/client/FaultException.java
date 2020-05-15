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

import org.w3c.dom.Document;

/**
 * Exception that encapsulates a fault returned by CyberSource.Used by both
 * the Basic and XML clients.
 */
public class FaultException
        extends Exception {
    private final Document faultDocument;
    private String faultCode = null;
    private String faultString = null;
    private String requestID = null;
    private boolean critical = true;

    /**
     * Constructor.
     *
     * @param _faultDocument Document object representing the fault.
     * @param logger         used to log the fault details.
     */
    public FaultException(
            Document _faultDocument, String nsURI, Logger logger) {
        faultDocument = _faultDocument;
        extractFields(nsURI);
        log(logger);
    }

    /**
     * Returns the fault document passed in the constructor.
     *
     * @return the fault document passed in the constructor.
     */
    public Document getFaultDocument() {
        return (faultDocument);
    }

    /**
     * Returns the fault code.
     *
     * @return the fault code.
     */
    public String getFaultCode() {
        return (faultCode);
    }

    /**
     * Returns the fault string.
     *
     * @return the fault string.
     */
    public String getFaultString() {
        return (faultString);
    }

    /**
     * Returns the request id returned by CyberSource.
     *
     * @return the request id returned by Cybersource or <code>null</code> if
     *         none was returned, which would mean that the fault occurred
     *         before CyberSource was able to create one.
     */
    public String getRequestID() {
        return (requestID);
    }

    /**
     * Returns whether or not this exception is critical.  It will return
     * <code>true</code> if the local part of the fault code is
     * "CriticalServerError".
     *
     * @return whether or not this exception is critical.
     */
    public boolean isCritical() {
        return (critical);
    }

    /**
     * Extracts the different fields from the fault document.
     */
    private void extractFields(String nsURI) {
        if (faultDocument != null) {
            faultString
                    = Utility.getElementText(faultDocument, "faultstring", null);
            requestID
                    = Utility.getElementText(faultDocument, "requestID", nsURI);
            faultCode
                    = Utility.getElementText(faultDocument, "faultcode", null);

            int colonPos = faultCode.indexOf(":");
            String localPart
                    = (colonPos != -1)
                    ? faultCode.substring(colonPos + 1)
                    : faultCode;

            critical = "CriticalServerError".equalsIgnoreCase(localPart);
        }
    }

    /**
     * Logs the fault details.
     *
     * @param logger used to log the fault details.
     */
    void log(Logger logger) {
        if (logger != null) {
            logger.log(Logger.LT_FAULT, getLogString());
        }
    }

    /**
     * Returns a string representation of the object for logging purposes.
     *
     * @return a string representation of the object for logging purposes.
     */
    String getLogString() {
        StringBuffer sb = new StringBuffer("FaultException details:\n");

        if (critical) {
            sb.append("CRITICAL\n");
        }

        if (requestID != null) {
            sb.append("requestID = " + requestID + "\n");
        }

        sb.append(Utility.nodeToString(faultDocument));

        return (sb.toString());
    }

    /**
     * Returns a description of the exception.
     *
     * @return a description of the exception.
     */
    public String getMessage() {
        StringBuffer sb = new StringBuffer("Fault:");
        if (faultString != null) {
            sb.append(" " + faultString);
        }

        if (requestID != null) {
            sb.append(" (requestID=" + requestID + ")");
        }

        if (critical) {
            sb.append(" (CRITICAL)");
        }

        return sb.toString();
    }
}