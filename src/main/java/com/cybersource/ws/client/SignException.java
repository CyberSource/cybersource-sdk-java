/* Copyright 2003-2004 CyberSource Corporation */

package com.cybersource.ws.client;

import java.io.PrintWriter;

/**
 * Exception that is thrown by the Signature object.  Note that unless you are
 * calling Signature.initializeContext() or Signature.cacheIdentity() yourself,
 * you do not have to catch this exception as it would be the inner exception
 * of a ClientException.
 */
public class SignException
	extends Exception
{
	private Exception innerException;
	
	/**
	 * Sole constructor.
	 *
	 * @param _innerException	the actual exception that occurred.
	 */	
	public SignException( Exception _innerException )
	{
		innerException = _innerException;
	}
	
	/**
	 * Gets the actual exception that occurred.
	 *
	 * @return the actual exception that occurred.
	 */
	public Exception getInnerException()
	{
		return( innerException );
	}	

	
	/**
	 * Returns the message in the inner exception.
	 *
	 * @return the message in the inner exception
	 */
	public String getMessage()
	{
		if (innerException != null)
		{
			return( innerException.getMessage() );
		}
		
		return( "No inner exception" );
	}
		
		
	/**
	 * Prints this exception's stack trace to the standard error stream.
	 */
	public void printStackTrace()
	{
		System.err.println( "SignException:" );
		if (innerException != null)
		{
			innerException.printStackTrace();
		}
		else
		{
			System.err.println( "No inner exception" );
		}
	}
	
	/**
	 * Prints this exception's stack trace to the specified print writer. 
	 *
	 * @param s	PrintWriter object to output to.
	 *
	 * @return the actual exception that occurred.
	 */
	public void printStackTrace( PrintWriter s )
	{
		s.println( "SignException:" );
		if (innerException != null)
		{
			innerException.printStackTrace( s );
		}
		else
		{
			s.println( "No inner exception" );
		}
	}
}