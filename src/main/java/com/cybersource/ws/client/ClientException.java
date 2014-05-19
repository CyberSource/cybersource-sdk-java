/* Copyright 2003-2004 CyberSource Corporation */

package com.cybersource.ws.client;

import java.net.HttpURLConnection;

/**
 * Exception class used by both Basic and XML clients.
 */
public class ClientException
	extends Exception
{
	private Exception innerException = null;
	private boolean critical = false;
	private int httpStatusCode = -1;
	private String httpError = null;
	
	/**
	 * Constructor.
	 *
	 * @param _innerException	the actual exception that occurred.
	 * @param logger			used to log the exception details.	 
	 */
	public ClientException(
		Exception _innerException, Logger logger )
	{
		innerException = _innerException;
		log( logger );		
	}
	
	/**
	 * Constructor.
	 *
	 * @param _innerException	the actual exception that occurred.
	 * @param _critical			flag that tells whether or not the exception
	 *							occurred at a critical point in the
	 *							transaction.
	 * @param logger			used to log the exception details.	 
	 */
	public ClientException(
		Exception _innerException, boolean _critical, Logger logger )
	{
		innerException = _innerException;
		critical = _critical;
		log( logger );		
	}
	
	/**
	 * Constructor.
	 *
	 * @param _httpStatusCode	HTTP status code
	 * @param logger			used to log the exception details.	 
	 */
	public ClientException( int _httpStatusCode, Logger logger )
	{
		httpStatusCode = _httpStatusCode;
		critical
			= (_httpStatusCode == HttpURLConnection.HTTP_GATEWAY_TIMEOUT);
		log( logger );		
	}
	
	/**
	 * Constructor.
	 *
	 * @param _httpStatusCode	HTTP status code
	 * @param _httpError		Additional HTTP error information; may be null.
	 * @param logger			used to log the exception details.	 
	 */
	public ClientException(
		int _httpStatusCode, String _httpError, Logger logger )
	{
		this( _httpStatusCode, logger );
		httpError = _httpError;
		log( logger );		
	}
	
	/**
	 * Constructor.
	 *
	 * @param _httpStatusCode	HTTP status code
	 * @param _httpError		Additional HTTP error information; may be null.
	 * @param _critical			flag that tells whether or not the exception
	 *							occurred at a critical point in the
	 *							transaction.
	 * @param logger			used to log the exception details.	 
	 */
	public ClientException(
		int _httpStatusCode, String _httpError, boolean _critical,
		Logger logger )
	{
		this( _httpStatusCode, logger );
		httpError = _httpError;
		
		// if critical is already true (the other constructor invoked in the
		// first line may set it to true), don't bother setting it as we don't
		// want to inadvertently reset it to false here.
		if (!critical)
		{
			critical = _critical;
		}
		
		log( logger );		
	}
	
	/**
	 * Returns the actual exception that occurred, if any.
	 *
	 * @return the actual exception that occurred or <code>null</code> if this
	 *         object was constructed using an HTTP status code.
	 */
	public Exception getInnerException()
	{
		return( innerException );
	}	
	
	/**
	 * Returns whether or not this exception is critical.  It will return
	 * <code>true</code> in the following cases:
	 * <ul>
 	 * <li>An exception occurred while parsing the reply or fault.
 	 * <li>An HTTP status code of
 	 *     <code>HttpURLConnection.HTTP_GATEWAY_TIMEOUT</code> was returned.
 	 * </ul>
	 *
	 * @return whether or not this exception is critical.
	 */
	public boolean isCritical()
	{
		return( critical );
	}	
	
	/**
	 * Returns the HTTP status code, if any.
	 *
	 * @return the HTTP status code or -1 if this object was constructed using
	 *		   an actual exception that occurred.
	 */
	public int getHttpStatusCode()
	{
		return( httpStatusCode );
	}
	
	/**
	 * Returns additional HTTP error information, if any.
	 *
	 * @return additional HTTP error information or <code>null</code> if this
	 *         object was constructed using an actual exception that occurred.
	 */
	public String getHttpError()
	{
		return( httpError );
	}	
	
	/**
	 * Logs the exception details.
	 *
	 * @param logger	used to log the exception details.	 
	 */
	public void log( Logger logger )
	{
		if (logger != null)
		{
			logger.log( Logger.LT_EXCEPTION, getLogString() );
		}		
	}
	
	/**
	 * Returns a string representation of the object for logging purposes.
	 *
	 * @return a string representation of the object for logging purposes.
	 */
	public String getLogString()
	{
		StringBuffer sb = new StringBuffer( "ClientException details:\n" );
				
		if (critical)
		{
			sb.append( "CRITICAL\n" );
		}
		
		if (httpStatusCode != -1)
		{
			sb.append( "httpStatusCode = " + httpStatusCode + "\n" );
		}
		
		if (httpError != null)
		{
			sb.append( "httpError = " + httpError + "\n" );
		}
		
		if (innerException != null)
		{
			sb.append(
				"innerException: \n" +
				Utility.getStackTrace( innerException ) );
		}
		else
		{
			sb.append(
				"Stack trace: \n" +
				Utility.getStackTrace( this ) );
		}
		
		return( sb.toString() );
	}	
	
	/**
	 * Returns a description of the exception.
	 *
	 * @return a description of the exception.
	 */
	public String getMessage()
	{
		if (innerException != null) return innerException.getMessage();
		
		StringBuffer sb = new StringBuffer( "ClientException:" );
		if (httpStatusCode != -1) {
			sb.append( " (" + httpStatusCode + ")" );
		}
		
		if (httpError != null) {
			sb.append( " " + httpError );
		}
		
		if (critical) {
			sb.append( " (CRITICAL)" );
		}
		
		return sb.toString();
	}	
}