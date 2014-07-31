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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Default file-based implementation of the Logger interface.
 */
public class LoggerImpl implements Logger {
    private final static int MB = 1048576;
    private final static String NEWLINE
            = System.getProperty("line.separator", "\n");

    private final MerchantConfig mc;

    /**
     * Constructor.
     *
     * @param _mc MerchantConfig object containing the logging parameters.
     */
    public LoggerImpl(MerchantConfig _mc) {
        mc = _mc;
    }

    /**
     * Prepares the file by checking if it has reached the maximum size and if
     * so, archives it.  It then creates a new file and logs an LT_FILESTART
     * entry.
     *
     * @throws ConfigException if anything is missing or invalid in the
     *                         configuration.
     */
    public synchronized void prepare()
            throws ConfigException {
        File file = mc.getLogFile();
        long size = file.length();

        // if it's an existing file and its size has exceeded the maximum,
        // archive it now
        if (size > mc.getLogMaximumSize() * MB) {
            SimpleDateFormat sdf
                    = new SimpleDateFormat("yyyyMMdd'T'HHmmssSSS");
            String newName
                    = file.getAbsolutePath() + "." + sdf.format(new Date());
            File newFile = new File(newName);
            file.renameTo(newFile);
        }
        // if it's an existing file and its size has not exceeded the maximum,
        // then we're fine.
        else if (size > 0) {
            return;
        }

        // at this point, either the file is new or its size had exceeded
        // the maximum and it therefore had to be archived.
        log(LT_FILESTART, "CYBERSOURCE LOG FILE");
    }

    /**
     * Logs an LT_TRANSTART entry.
     */
    public synchronized void logTransactionStart() {
        log(LT_TRANSTART, "=======================================");

        log(LT_MERCHCFG, mc.getLogString());

        String proxyCfg = getProxyConfig();
        if (proxyCfg != null) {
            log(LT_PROXYCFG, proxyCfg);
        }
    }

    /**
     * Logs the text specified.
     *
     * @param type the log entry type.
     * @param text the actual text to be logged.
     */
    public synchronized void log(String type, String text) {
        // take care of null values for the text to be logged
        if (text == null) {
            text = "(null)";
        }

        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(
                    mc.getLogFile().getAbsolutePath(), true);

            SimpleDateFormat sdf
                    = new SimpleDateFormat("yyyy'-'MM'-'dd' 'HH':'mm':'ss'.'SSS");

            Object[] arguments
                    = {LT_TRANSTART.equalsIgnoreCase(type) ? NEWLINE : "",
                    sdf.format(new Date()),
                    Thread.currentThread().getName(),
                    type};

            String intro
                    = MessageFormat.format("{0}{1} {2} {3}> ", arguments);

            // write intro
            byte[] introBytes = getBytes(intro);
            fos.write(introBytes);

            byte[] newlineBytes = getBytes(NEWLINE);

            // if the text to log has more than one line, write the first line
            // on a separate line (instead of on the same line with the intro)
            // so that it will be aligned with the subsequent lines in the text.
            if (text.indexOf(NEWLINE) >= 0) {
                fos.write(newlineBytes);
            }

            // write text
            byte[] textBytes = getBytes(text);
            fos.write(textBytes);
            fos.write(newlineBytes);

            fos.close();
        } catch (IOException ioe) {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ioe2) {
                    // do nothing.  If close() fails, there's nothing more we
                    // can do about it.
                }
            }

            // Apart from that, we don't do anything else.  We won't let log
            // exceptions fail the transactions.
        } catch (ConfigException ce) {
            // do nothing.  We won't let log exceptions fail the transactions.
        }
    }

    private static byte[] getBytes(String str)
            throws UnsupportedEncodingException {
        return (str.getBytes("UTF-8"));
    }

    private static String getProxyConfig() {
        if (System.getProperty("https.proxyHost") != null) {
            StringBuffer sb = new StringBuffer();
            appendPair(sb, "https.proxyHost");
            appendPair(sb, "https.proxyPort");
            appendPair(sb, "https.proxyUser");
            appendPair(sb, "https.proxyPassword");
            return (sb.toString());
        }
        return (null);
    }

    private static void appendPair(StringBuffer sb, String sysProp) {
        String propValue = System.getProperty(sysProp);

        if ("https.proxyPassword".equalsIgnoreCase(sysProp) &&
                propValue != null && propValue.length() > 0) {
            propValue = "(hidden)";
        }

        if (sb.length() > 0) {
            sb.append(", ");
        }

        sb.append(sysProp + "=");
        sb.append(propValue != null ? propValue : "(null)");
    }
}