/**
 * Copyright 1998-1999 CyberSource Corporation. All rights reserved.
 *
 * Identity.java
 *
 * @author Jason Eaton
 * @version 1.0, 09/23/98
 * @since 3.0
 */
package com.cybersource.ws.client;

import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * This class is used to store an identity of a unknown entity.
 */
public class Identity { 
   
   //Our p12 files do not contain an alias as a normal name, its the common name and serial number
   private String name;
   
   // we need to create alias for our keystores, it looks like "serialNumber=4032987129910179089277,CN=jasoneatoncorp"
   private String keyAlias;
   
   // for an unknown reason the serial number of the certificate is set incorrectly, we must parse it from DN
   private String serialNumber;
   
   private X509Certificate x509Cert;
   
   private PrivateKey privateKey;
   
   private MerchantConfig merchantConfig;
   
   private static final String SERVER_ALIAS = "CyberSource_SJC_US";
   
   /**
    * Creates an Identity instance.this type of the instance can 
    * only be used to store server certificate identity.
    * 
    * @param merchantConfig identity will be used for merchant specified in merchantConfig object.
    * @param x509Certificate 
    * @throws SignException 
    */
   public Identity(MerchantConfig merchantConfig,X509Certificate x509Certificate) throws SignException {
	   this.merchantConfig = merchantConfig;
       this.x509Cert=x509Certificate;
       setUpServer();
   }

   /**
    * Creates an Identity instance.this type of the instance can 
    * only be used to store merchant identity.
    * 
    * @param merchantConfig identity will be used for merchant specified in merchantConfig object.
    * @param x509Certificate 
    * @param privateKey 
    * @throws SignException 
    */
   public Identity(MerchantConfig merchantConfig,X509Certificate x509Certificate, PrivateKey privateKey) throws SignException {
	   this.merchantConfig = merchantConfig;
	   this.x509Cert = x509Certificate;
	   this.privateKey = privateKey;
	   setUpMerchant();
   }

	private void setUpMerchant() throws SignException {
		 if (serialNumber == null && x509Cert != null) {
		       	 String subjectDN = x509Cert.getSubjectDN().getName();
	             if (subjectDN != null) {
	                 String subjectDNrray[] = subjectDN.split("SERIALNUMBER=");
	                 if (subjectDNrray.length != 2) {
	                     throw new SignException("Exception while obtaining private key from KeyStore with alias, '" + merchantConfig.getKeyAlias() + "'");
	                 }
	                 name = merchantConfig.getMerchantID();
	                 serialNumber = subjectDNrray[1]; 
	                 keyAlias = "serialNumber=" + serialNumber + ",CN=" + name;
	             } else {
	                 throw new SignException("Exception while obtaining private key from KeyStore with alias, '" + merchantConfig.getKeyAlias() + "'");
	             }
	             
	    }
	}

	private void setUpServer() throws SignException {
		 if (serialNumber == null && x509Cert != null) {
	       	 String subjectDN = x509Cert.getSubjectDN().getName();
             if (subjectDN != null) {
                 String subjectDNrray[] = subjectDN.split("SERIALNUMBER=");
                 if (subjectDNrray.length == 1 && subjectDNrray[0].contains("CyberSourceCertAuth")){
                	 name = keyAlias = "CyberSourceCertAuth";
                 }
                 else if (subjectDNrray.length == 2 && subjectDNrray[0].contains(SERVER_ALIAS)) {
                     name = SERVER_ALIAS;
                     serialNumber = subjectDNrray[1]; 
                     keyAlias = "serialNumber=" + serialNumber + ",CN=" + name;
                 }else{
                     throw new SignException("Exception while obtaining private key from KeyStore with alias, '" + merchantConfig.getKeyAlias() + "'");
                 }

             } else {
                 throw new SignException("Exception while obtaining private key from KeyStore with alias, '" + merchantConfig.getKeyAlias() + "'");
             }
      }
		
	}
	public String getName() {
		return name;
	}
	
	
	public void setName(String name) {
		this.name = name;
	}
	
	
	public String getKeyAlias() {
		return keyAlias;
	}
	
	
	public void setKeyAlias(String keyAlias) {
		this.keyAlias = keyAlias;
	}
	
	
	public String getSerialNumber() {
	     
		return serialNumber;
	}
	
	
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
	
	
	public X509Certificate getX509Cert() {
		return x509Cert;
	}
	
	
	public void setX509Cert(X509Certificate x509Cert) {
		this.x509Cert = x509Cert;
	}
	
	
	public PrivateKey getPrivateKey() {
		return privateKey;
	}
	
	
	public void setPrivateKey(PrivateKey privateKey) {
		this.privateKey = privateKey;
	}
	
	/**
	* Prints out a nice string that describes the Identity name and size of
	* public key, private key and cert if they exist
	*
	* @return String representing the identity name and sizes of keys and cert.
	*/
	@Override
	public String toString() {
		int privateKeyLen, certLen = 0;
		Date date = null;
		String expireStr = null;
		
		if (x509Cert != null) {
	       try {
			certLen = x509Cert.getEncoded().length;
		} catch (CertificateEncodingException e) {
			certLen =0;
		}
	       date = x509Cert.getNotAfter();
	    }
	    
		serialNumber = getSerialNumber();
	    
	    if (privateKey != null) {
	    	privateKeyLen = privateKey.getEncoded().length;
	    }
	    else {
	        privateKeyLen = 0;
	    }
	    
	    if (date != null) {
	    	TimeZone usPacificTimeZone = TimeZone.getTimeZone("America/Los_Angeles");
	        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	        formatter.setTimeZone(usPacificTimeZone);
	        expireStr = formatter.format(date);
	    }
	    
	    if (privateKey != null) {
	        privateKeyLen = privateKey.getEncoded().length;
	    }
	    else {
	        privateKeyLen = 0;
	    }

	    return "{" + getName() + ",privatekey="+ privateKeyLen + ",cert=" + certLen + ",serial="
	            + serialNumber + ",expiration=" + expireStr+ " }";
	   }
	
	   
}
