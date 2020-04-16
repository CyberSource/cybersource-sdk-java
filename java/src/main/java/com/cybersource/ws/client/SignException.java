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

import java.io.PrintWriter;

/**
 * Exception that is thrown by the Signature object.Note that unless you are
 * calling Signature.initializeContext() or Signature.cacheIdentity() yourself,
 * you do not have to catch this exception as it would be the inner exception
 * of a ClientException.
 */
public class SignException extends Exception {
    private Exception innerException;

    /**
     * Sole constructor.
     *
     * @param _innerException the actual exception that occurred.
     */
    public SignException(Exception _innerException) {
        innerException = _innerException;
    }

    public SignException(String msg) {
        super(msg);
    }

    /**
     * Gets the actual exception that occurred.
     *
     * @return the actual exception that occurred.
     */
    public Exception getInnerException() {
        return (innerException);
    }


    /**
     * Returns the message in the inner exception.
     *
     * @return the message in the inner exception
     */
    public String getMessage() {
        if (innerException != null) {
            return (innerException.getMessage());
        }

        return super.getMessage();
    }


    /**
     * Prints this exception's stack trace to the standard error stream.
     */
    public void printStackTrace() {
        if (innerException != null) {
            innerException.printStackTrace();
        } else {
            super.getMessage();
        }
    }

    /**
     * Prints this exception's stack trace to the specified print writer.
     *
     * @param s PrintWriter object to output to.
     */
    public void printStackTrace(PrintWriter s) {
        if (innerException != null) {
            innerException.printStackTrace(s);
        } else {
            super.getMessage();
        }
    }
}