package com.cybersource.ws.client;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.*;

public class RetryIT{

	private static final String SERVER_URL = "https://ics2wstest.ic3.com/commerce/1.x/transactionProcessor";
	private static final String WRONG_SERVER_URL = "https://ics2wstest123.ic3.com/commerce/1.x/transactionProcessor";
	private MerchantConfig mc;
	private Properties props;
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();


	@Before
	public void setUp() throws Exception
	{
		props = new Properties();
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("test_cybs.properties");
		if (in == null) {
			throw new RuntimeException("Unable to load test_cybs.properties file");
		}
		props.load(in);
		mc = new MerchantConfig(props, null);

	}
	
	@Test 
	public void checkRetryConfigValuesForHttpClient() throws Exception
	{
		String errMsg="Invalid value of numberOfRetries and/or retryInterval";
		// request  fails as number of retry value is incorrect
		props.setProperty("useHttpClient", "true");
		props.setProperty("allowRetry", "true");
		props.setProperty("numberOfRetries", "10");
		props.setProperty("retryInterval", "10");
		props.setProperty("serverURL",SERVER_URL);
		// request 1 Should fail as the number of retry attempt is exceeding 3
		try{
			Client.runTransaction(new HashMap(), props);
		}
		catch(ClientException e){
			assertEquals(errMsg, e.getMessage());
		}

	}  

	@Test 
	public void checkRetryConfigValuesForNonHttpClient() throws Exception
	{
		// In this case retry should not be active as it is non http client. transaction should be successful.
		String errMsg="Invalid value of numberOfRetries and/or retryInterval";
		props.setProperty("useHttpClient", "false");
		props.setProperty("allowRetry", "true");
		props.setProperty("numberOfRetries", "10");
		props.setProperty("retryInterval", "10");
		props.setProperty("serverURL",WRONG_SERVER_URL);

		try{
			Map<String, String> replyMap = Client.runTransaction(new HashMap(), props);
		}
		catch(ClientException e){
			assertNotEquals(errMsg, e.getMessage());
		}

	}
	
	@Test 
	public void checkNegativeForHttp() throws Exception
	{
		String errMsg="Invalid value of numberOfRetries and/or retryInterval";
		// request  fails as number of retry and retry interval values are in negative
		props.setProperty("useHttpClient", "true");
		props.setProperty("allowRetry", "true");
		props.setProperty("numberOfRetries", "-10");
		props.setProperty("retryInterval", "-10");
		try
		{
			Client.runTransaction(new HashMap(), props);
		}
		catch (ClientException e)
		{
			assertEquals(errMsg, e.getMessage());
		}
	}
	
	@Test 
	public void retryDisabled() throws Exception{
		// request should work as the Allow Retry is set to false other values will be ignored
		props.setProperty("useHttpClient", "true");
		props.setProperty("allowRetry", "false");
		props.setProperty("numberOfRetries", "-10");
		props.setProperty("retryInterval", "-10");
		Map reply = Client.runTransaction(new HashMap(), props);
		assertNotNull(reply.get("requestID"));

	}
}
