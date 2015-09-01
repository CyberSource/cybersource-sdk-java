package com.cybersource.sample;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Properties;

import com.cybersource.ws.client.*;

/**
 * Sample class that demonstrates how to call Credit Card Authorization.
 */
public class AuthCaptureSample
{
	/**
	 * Entry point.
	 *
	 * @param args	command-line arguments. The name of the property file
	 *              may be passed as a command-line argument.  If not passed, 
	 *				it will look for "cybs.properties" in the current
	 *				directory.
	 */
    public static void main( String[] args )
   	{   	
	   	// read in properties file.
	   	Properties props = Utility.readProperties( args );

		System.out.println( "Key file : "+props.getProperty("keyFilename") );

	   	// run auth
   		String requestID = runAuth( props );
   		if (requestID != null)
   		{
	   		// if auth was successful, run capture
   			runCapture( props, requestID );
   		}
	}
	
	/**
	 * Runs Credit Card Authorization.
	 * 
	 * @param props	Properties object.
	 *
	 * @return the requestID.
	 */
    public static String runAuth( Properties props )
    {  	
	    String requestID = null;
	    
	   	HashMap<String, String> request = new HashMap<String, String>();
	   	
		request.put( "ccAuthService_run", "true" );
		
		// We will let the Client get the merchantID from props and insert it
		// into the request Map.
		
		// this is your own tracking number.  CyberSource recommends that you
		// use a unique one for each order.
		request.put( "merchantReferenceCode", "your_merchant_reference_code" );
		
		request.put( "billTo_firstName", "John" );
		request.put( "billTo_lastName", "Doe" );
		request.put( "billTo_street1", "1295 Charleston Road" );
		request.put( "billTo_city", "Mountain View" );
		request.put( "billTo_state", "CA" );
		request.put( "billTo_postalCode", "94043" );
		request.put( "billTo_country", "US" );
		request.put( "billTo_email", "nobody@cybersource.com" );
		request.put( "billTo_ipAddress", "10.7.7.7" );
		request.put( "billTo_phoneNumber", "650-965-6000" );
		request.put( "shipTo_firstName", "Jane" );
		request.put( "shipTo_lastName", "Doe" );
		request.put( "shipTo_street1", "100 Elm Street" );
		request.put( "shipTo_city", "San Mateo" );
		request.put( "shipTo_state", "CA" );
		request.put( "shipTo_postalCode", "94401" );
		request.put( "shipTo_country", "US" );
		request.put( "card_accountNumber", "4111111111111111" );
		request.put( "card_expirationMonth", "12" );
		request.put( "card_expirationYear", "2020" );
		request.put( "purchaseTotals_currency", "USD" );

		// there are two items in this sample
		request.put( "item_0_unitPrice", "12.34" );
		request.put( "item_1_unitPrice", "56.78" );

		// add more fields here per your business needs
		
		try
		{
			displayMap( "CREDIT CARD AUTHORIZATION REQUEST:", request );
			
			// run transaction now
			Map<String, String>  reply = Client.runTransaction( request, props );	
			
			displayMap( "CREDIT CARD AUTHORIZATION REPLY:", reply );
			
			// if the authorization was successful, obtain the request id
			// for the follow-on capture later.
			String decision = (String) reply.get( "decision" );
			if ("ACCEPT".equalsIgnoreCase( decision ))
			{
				requestID = (String) reply.get( "requestID" );
			}
			
		}	
		catch (ClientException e)
		{
			System.out.println( e.getMessage() );
			if (e.isCritical())
			{
				handleCriticalException( e, request );
			}
		}
		catch (FaultException e)
		{
			System.out.println( e.getMessage() );
			if (e.isCritical())
			{
				handleCriticalException( e, request );
			}
		}
		
		return( requestID );
    }
    
	/**
	 * Runs Credit Card Capture.
	 * 
	 * @param props			Properties object.
	 * @param authRequestID	requestID returned by a previous authorization.
	 */
    public static void runCapture( Properties props, String authRequestID )
    {  	
	    String requestID = null;
	    
	   	HashMap<String, String> request = new HashMap<String, String>();
	   	
		request.put( "ccCaptureService_run", "true" );
		
		// We will let the Client get the merchantID from props and insert it
		// into the request Map.
		
		// so that you can efficiently track the order in the CyberSource
		// reports and transaction search screens, you should use the same
		// merchantReferenceCode for the auth and subsequent captures and
		// credits.
		request.put( "merchantReferenceCode", "MRC-14344" );
		
		// reference the requestID returned by the previous auth.
		request.put( "ccCaptureService_authRequestID", authRequestID );
		
		// this sample assumes only the first item has been shipped.
		request.put( "purchaseTotals_currency", "USD" );
		request.put( "item_0_unitPrice", "12.34" );

		// add more fields here per your business needs
		
		try
		{
			displayMap( "FOLLOW-ON CAPTURE REQUEST:", request );
			
			// run transaction now
			Map<String, String> reply = Client.runTransaction( request, props );	
			
			displayMap( "FOLLOW-ON CAPTURE REPLY:", reply );			
		}	
		catch (ClientException e)
		{
			System.out.println( e.getMessage() );
			if (e.isCritical())
			{
				handleCriticalException( e, request );
			}
		}
		catch (FaultException e)
		{
			System.out.println( e.getMessage() );
			if (e.isCritical())
			{
				handleCriticalException( e, request );
			}
		}		
    }
    
	/**
	 * Displays the content of the Map object.
	 *
	 * @param header	Header text.
	 * @param map		Map object to display.
	 */
    private static void displayMap( String header, Map map )
    {
	    System.out.println( header );
	    
		StringBuffer dest = new StringBuffer();
		
		if (map != null && !map.isEmpty())
		{
			Iterator iter = map.keySet().iterator();
			String key, val;
			while (iter.hasNext())
			{
				key = (String) iter.next();
				val = (String) map.get( key );
				dest.append( key + "=" + val + "\n" );
			}
		}
		
		System.out.println( dest.toString() );		
    }
    
       
	/**
	 * An exception is considered critical if some type of disconnect occurs
	 * between the client and server and the client can't determine whether the
	 * transaction was successful. If this happens, you might have a
	 * transaction in the CyberSource system that your order system is not
	 * aware of. Because the transaction may have been processed by
	 * CyberSource, you should not resend the transaction, but instead send the
	 * exception information and the order information (customer name, order
	 * number, etc.) to the appropriate personnel at your company to resolve
	 * the problem. They should use the information as search criteria within
	 * the CyberSource Transaction Search Screens to find the transaction and
	 * determine if it was successfully processed. If it was, you should update
	 * your order system with the transaction information. Note that this is
	 * only a recommendation; it may not apply to your business model.
	 *
	 * @param e			Critical ClientException object.
	 * @param request	Request that was sent.
	 */
	private static void handleCriticalException(
		ClientException e, Map request )
	{
		// send the exception and order information to the appropriate
		// personnel at your company using any suitable method, e.g. e-mail,
		// multicast log, etc.
	}
	
	/**
	 * See header comment in the other version of handleCriticalException
	 * above.
	 *
	 * @param e			Critical ClientException object.
	 * @param request	Request that was sent.
	 */
	private static void handleCriticalException(
		FaultException e, Map request )
	{
		// send the exception and order information to the appropriate
		// personnel at your company using any suitable method, e.g. e-mail,
		// multicast log, etc.
	}    
} 


