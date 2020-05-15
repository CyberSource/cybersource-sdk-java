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

/**
 * this may be implemented to supply a different Logger object
 * to the runTransaction() method of the clients.  The default Logger object
 * created and used by the clients writes to a file.
 */
public interface Logger {
    // pre-defined log types
    String LT_FILESTART = "FILESTART";
    String LT_TRANSTART = "TRANSTART";
    String LT_MERCHCFG = "MERCHCFG ";
    String LT_PROXYCFG = "PROXYCFG ";
    String LT_REQUEST = "REQUEST  ";
    String LT_REPLY = "REPLY    ";
    String LT_FAULT = "FAULT    ";
    String LT_INFO = "INFO     ";
    String LT_EXCEPTION = "EXCEPTION";

    /**
     * This is where any PER-TRANSACTION preparation should be done.  Any one-
     * time initializations must be done in another method as this method is
     * called at the start of every transaction (unless you pass false to the
     * prepareFile parameter of the runTransaction() method). In the case of
     * the default file-based LoggerImpl, it checks whether the file has
     * reached the maximum size and if so, archives it.  It then creates a new
     * file and logs an LT_FILESTART entry.  This is also a good place to read
     * and check any configuration parameters that your Logger object may
     * require.
     *
     * @throws ConfigException if anything fails during preparation.
     */
    void prepare() throws ConfigException;

    /**
     * This is where the entry that marks the start of the transaction should
     * be logged. The default LoggerImpl logs an LT_TRANSTART entry here.  No
     * exceptions are thrown so as not to fail transactions due to logging
     * failures.
     */
    void logTransactionStart();

    /**
     * This is where the actual logging takes place.  No exceptions are thrown
     * so as not to fail transactions due to logging failures.
     *
     * @param type the log entry type.  A few log types have been predefined.
     * @param text the actual text to be logged.
     */
    void log(String type, String text);
}
