package com.cybersource.ws.client;

import org.junit.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Properties;

import static com.cybersource.ws.client.Utility.*;
import static org.junit.Assert.assertEquals;

public class ClientTest extends BaseTest {

    Map<String, String> request = getSampleRequest();
    Properties orgMerchantProps = new Properties();
    @Before
    public void setup(){
        orgMerchantProps.putAll(merchantProperties);
    }

    @After
    public void tearDown(){
        merchantProperties.clear();
        merchantProperties.putAll(orgMerchantProps);
    }
    @Test
    public void testSetVersionInformation() throws InvocationTargetException {
        Class[] argClasses = {Map.class};
        Object[] argObjects = {request};
        invokePrivateStaticMethod(Client.class, "setVersionInformation", argClasses, argObjects);
        assertEquals(Utility.NVP_LIBRARY, request.get("clientLibrary"));
    }

    @Test
    public void testShouldFailedIfMTIFieldNotExist_1() throws FaultException {
        merchantProperties.put("useHttpClientWithConnectionPool", "true");
        merchantProperties.put("retryIfMTIFieldExist", "true");
        try {
            Client.runTransaction(request, merchantProperties);
            Assert.fail();
        } catch (ClientException e) {
            Assert.assertEquals(MTI_FIELD_ERR_MSG, e.getHttpError());
            Assert.assertFalse(e.isCritical());
            Assert.assertNull(e.getInnerException());
            Assert.assertEquals("ClientException: (" + HTTP_BAD_REQUEST + ") " + MTI_FIELD_ERR_MSG, e.getLocalizedMessage());
            Assert.assertEquals(400, e.getHttpStatusCode());
        }
    }

    @Test
    public void testShouldFailedIfMTIFieldNotExist_2() throws FaultException {
        merchantProperties.put("useHttpClientWithConnectionPool", "true");
        merchantProperties.put("retryIfMTIFieldExist", "false");
        try {
            Client.runTransaction(request, merchantProperties);
            Assert.fail();
        } catch (ClientException e) {
            Assert.assertEquals(MTI_FIELD_ERR_MSG, e.getHttpError());
            Assert.assertFalse(e.isCritical());
            Assert.assertNull(e.getInnerException());
            Assert.assertEquals("ClientException: (" + HTTP_BAD_REQUEST + ") " + MTI_FIELD_ERR_MSG, e.getLocalizedMessage());
            Assert.assertEquals(400, e.getHttpStatusCode());
        }
    }
}
