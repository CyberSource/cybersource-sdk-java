/* Copyright 2003-2004 CyberSource Corporation */

package com.cybersource.ws.client;

/**
 * Interface that may be implemented to supply a different Logger object
 * to the runTransaction() method of the clients.  The default Logger object
 * created and used by the clients writes to a file.
 */
public interface Logger
{
	// pre-defined log types
	public static final String LT_FILESTART = "FILESTART";
	public static final String LT_TRANSTART = "TRANSTART";
	public static final String LT_MERCHCFG  = "MERCHCFG ";
	public static final String LT_PROXYCFG  = "PROXYCFG ";
	public static final String LT_REQUEST   = "REQUEST  ";
	public static final String LT_REPLY     = "REPLY    ";
	public static final String LT_FAULT     = "FAULT    ";
	public static final String LT_INFO      = "INFO     ";
	public static final String LT_EXCEPTION = "EXCEPTION";
	
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
	 * @throws ConfigException	if anything fails during preparation.
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
	 * @param type	the log entry type.  A few log types have been predefined.
	 * @param text	the actual text to be logged.
	 */
	void log( String type, String text );
}
