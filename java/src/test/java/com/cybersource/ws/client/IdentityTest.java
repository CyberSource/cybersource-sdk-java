package com.cybersource.ws.client;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;

import java.io.File;
import java.io.InputStream;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.*;

public class IdentityTest{
    Properties merchantProperties;
    private MerchantConfig config;
    
    @Before
    public void setUp() throws Exception {
    	//Loading the properties file from src/test/resources
        Properties merchantProperties = new Properties();
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("test_cybs.properties");
		if (in == null) {
			throw new RuntimeException("Unable to load test_cybs.properties file");
		}
		merchantProperties.load(in);
	    config = new MerchantConfig(merchantProperties, merchantProperties.getProperty("merchantID"));
    }

    @Test
    public void testSetUpMerchant() throws SignException, ConfigException{
    	File p12file = Mockito.mock(File.class);
    	MerchantConfig mc = Mockito.mock(MerchantConfig.class);
    	
    	String keyAlias = "CN="+mc.getMerchantID()+",SERIALNUMBER=400000009910179089277";
    	X509Certificate x509Cert = Mockito.mock(X509Certificate.class);
    	Principal principal =  Mockito.mock(Principal.class);
    	PrivateKey pkey = Mockito.mock(PrivateKey.class);
    	Logger logger = Mockito.mock(Logger.class);
    	Mockito.when(x509Cert.getSubjectDN()).thenReturn(principal);
    	Mockito.when(principal.getName()).thenReturn(keyAlias);
    	
    	Mockito.when(mc.getKeyFile()).thenReturn(p12file);
		Mockito.when(mc.getKeyPassword()).thenReturn("testPwd");
    	Identity identity = new Identity(mc,x509Cert,pkey,logger);
    	assertEquals(identity.getName(), mc.getMerchantID());
    	assertEquals(identity.getSerialNumber(), "400000009910179089277");
		assertEquals(String.valueOf(identity.getPswd()), "testPwd");
    	assertNotNull(identity.getPrivateKey());
    }
    
    @Test
    public void testsetUpServer() throws InstantiationException, IllegalAccessException, SignException{
    	String keyAlias = "CN=CyberSource_SJC_US,SERIALNUMBER=400000009910179089277";
    	X509Certificate x509Cert = Mockito.mock(X509Certificate.class);
    	Principal principal =  Mockito.mock(Principal.class);
    	Logger logger = Mockito.mock(Logger.class);
    	Mockito.when(x509Cert.getSubjectDN()).thenReturn(principal);
    	Mockito.when(principal.getName()).thenReturn(keyAlias);
    	Identity identity = new Identity(config,x509Cert,logger);
    	assertEquals(identity.getName(), "CyberSource_SJC_US");
    	assertEquals(identity.getSerialNumber(), "400000009910179089277");
    	assertNull(identity.getPrivateKey());
    }

}
