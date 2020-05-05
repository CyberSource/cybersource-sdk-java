package com.cybersource.ws.client;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ClientTest extends BaseTest {
    @Test
    public void testSetVersionInformation() throws InvocationTargetException {
        Class[] argClasses = {Map.class};
        Map<String,String> request = getSampleRequest();
        Object[] argObjects = {request};
        invokePrivateStaticMethod(Client.class, "setVersionInformation", argClasses, argObjects);
        assertEquals(Utility.NVP_LIBRARY, request.get("clientLibrary"));
    }
}
