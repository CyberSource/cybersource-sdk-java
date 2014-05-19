/* Copyright 2003-2004 CyberSource Corporation */

package com.cybersource.ws.client;

/**
 * Exception thrown when something is missing or invalid in the configuration
 * or when setting up of the log file fails.  Note that you do not have to
 * catch this exception in your code as it would be the inner exception of a
 * ClientException.
 */
public class ConfigException
	extends Exception
{
	/**
	 * Constructor.
	 *
	 * @param message 			exception message.
	 */
	public ConfigException( String message )
	{
		super( message );
	}	
}