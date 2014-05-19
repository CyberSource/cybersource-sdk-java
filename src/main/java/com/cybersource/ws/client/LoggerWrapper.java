/* Copyright 2003-2004 CyberSource Corporation */

package com.cybersource.ws.client;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * An internal class used by the clients to encapsulate the logger, primarily
 * to avoid having to check if the Logger object is null before logging.  It
 * implements Logger just so the clients can pass it to the exceptions'
 * constructors.
 */
public class LoggerWrapper implements Logger
{
	private Logger logger = null;
	private MerchantConfig mc;
	
	/**
	 * Constructor.
	 *
	 * @param _logger	Logger object to encapsulate.  May be null, in which
	 *                  case, a LoggerImpl object will be created (unless
	 *                  logging is not enabled).
	 * @param _prepare  	Flag as to whether or not the logger's prepare() method
	 *                  should be called.
	 * @param _logTranStart  Flag as to whether or not the logger's
	 *                      logTransactionStart() method should be called.
	 * @param _mc   	MerchantConfig object used to create and configure the
	 *                  LoggerImpl object.
	 *
	 * @throws ConfigException if the preparation fails.
	 */
	public LoggerWrapper(
		Logger _logger, boolean _prepare, boolean _logTranStart,
		MerchantConfig _mc )
		throws ConfigException
	{
		mc = _mc;
		boolean prepare = false;
		boolean logTranStart = false;
		
		if (_logger != null)
		{
			logger = _logger;
			prepare = _prepare;
			logTranStart = _logTranStart;
		}
		else if (mc.getEnableLog())
		{
			logger = new LoggerImpl( mc );
			prepare = true;
			logTranStart = true;
		}
		
		if (prepare)
		{
			prepare();
		}
		
		if (logTranStart)
		{
			logTransactionStart();
		}
	}
	
	/**
	 * Calls the encapsulated Logger object's prepare() method.
	 *
	 * @throws ConfigException	if preparation fails.
	 */
	public void prepare()
		throws ConfigException
	{
		if (logger != null)
		{
			logger.prepare();
		}
	}
	
	/**
	 * Calls the encapsulated Logger object's logTransactionStart() method.
	 */
	public void logTransactionStart()
	{
		if (logger != null)
		{
			logger.logTransactionStart();
		}
	}
	
	/**
	 * Calls the encapsulated Logger object's log() method.
	 *
	 * @param type	the log entry type.
	 * @param text	the actual text to be logged.
	 */
	public void log( String type, String text )
	{
		if (logger != null)
		{
			logger.log( type, text );
		}
	}		
	
	/**
	 * Returns the encapsulated Logger object.
	 *
	 * @return the encapsulated Logger object.
	 */
	public Logger getLogger() { return logger; }		
}
