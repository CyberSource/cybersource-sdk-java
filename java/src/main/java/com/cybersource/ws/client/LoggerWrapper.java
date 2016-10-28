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

import org.apache.commons.lang3.time.StopWatch;

/**
 * An internal class used by the clients to encapsulate the logger, primarily
 * to avoid having to check if the Logger object is null before logging.  It
 * implements Logger just so the clients can pass it to the exceptions'
 * constructors.
 */
public class LoggerWrapper implements Logger {
    private Logger logger = null;
    private MyStopWatch stopWatch = new MyStopWatch();
  
    /**
     * Constructor.
     *
     * @param _logger       Logger object to encapsulate.  May be null, in which
     *                      case, a LoggerImpl object will be created (unless
     *                      logging is not enabled).
     * @param _prepare      Flag as to whether or not the logger's prepare() method
     *                      should be called.
     * @param _logTranStart Flag as to whether or not the logger's
     *                      logTransactionStart() method should be called.
     * @param _mc           MerchantConfig object used to create and configure the
     *                      LoggerImpl object.
     * @throws ConfigException if the preparation fails.
     */
    public LoggerWrapper(
            Logger _logger, boolean _prepare, boolean _logTranStart,
            MerchantConfig _mc)
            throws ConfigException {
        MerchantConfig mc = _mc;
        boolean prepare = false;
        boolean logTranStart = false;

        if (_logger != null) {
            logger = _logger;
            prepare = _prepare;
            logTranStart = _logTranStart;
            stopWatch.start();
        } else if (mc.getEnableLog()) {
            logger = new LoggerImpl(mc);
            prepare = true;
            logTranStart = true;
            stopWatch.start();
        }

        if (prepare) {
            prepare();
        }

        if (logTranStart) {
            logTransactionStart();
        }
        
    }

    /**
     * Calls the encapsulated Logger object's prepare() method.
     *
     * @throws ConfigException if preparation fails.
     */
    public void prepare()
            throws ConfigException {
        if (logger != null) {
            logger.prepare();
        }
    }

    /**
     * Calls the encapsulated Logger object's logTransactionStart() method.
     */
    public void logTransactionStart() {
        if (logger != null) {
            logger.logTransactionStart();
        }
    }

    /**
     * Calls the encapsulated Logger object's log() method.
     *
     * @param type the log entry type.
     * @param text the actual text to be logged.
     */
    public void log(String type, String text) {
        if (logger != null) {
            logger.log(type, text);
        }
    }
    
    /**
     * Calls the encapsulated Logger object's log() method.
     *
     * @param type the log entry type.
     * @param text the actual text to be logged.
     * @param splitTimer to log split time.
     */
	public void log(String type, String text, boolean splitTimer) {
		if (logger != null) {
			if (splitTimer) {
				logger.log(type, text + stopWatch.getMethodElapsedTime() + "ms");
			} else {
				logger.log(type, text + stopWatch.getElapsedTimeForTransaction() + "ms");
			}
		}
	}

    /**
     * Returns the encapsulated Logger object.
     *
     * @return the encapsulated Logger object.
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Returns the encapsulated StopWatch object.
     *
     * @return the encapsulated StopWatch object.
     */
    public MyStopWatch getStopWatch() {
        return stopWatch;
    }
}
