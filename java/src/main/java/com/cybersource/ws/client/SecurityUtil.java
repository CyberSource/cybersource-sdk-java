package com.cybersource.ws.client;

import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.CredentialException;
import org.apache.ws.security.message.WSSecEncrypt;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecSignature;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.w3c.dom.Document;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for security related functions like loading p12 key, create signed or encrypted doc,
 */

public class SecurityUtil {
    
    private static final String KEY_FILE_TYPE = "PKCS12";
    
    private static final String FAILED_TO_LOAD_KEY_STORE = "Exception while loading KeyStore";
    private static final String FAILED_TO_OBTAIN_PRIVATE_KEY = "Exception while obtaining private key from KeyStore with alias";


    private static MessageHandlerKeyStore localKeyStoreHandler = null;
    
    //mapping between IdentityName and Identity
    private static ConcurrentHashMap<String, Identity> identities = new ConcurrentHashMap<String, Identity>();
    
    // By default signature algorithm is set to null and during WSSecSignature build() Signature algorithm will set to "http://www.w3.org/2000/09/xmldsig#rsa-sha1" .
    private static final String SIGNATURE_ALGORITHM = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";
    // By default digest algorithm is set to "http://www.w3.org/2000/09/xmldsig#sha1"
    private static final String DIGEST_ALGORITHM = "http://www.w3.org/2001/04/xmlenc#sha256";
    
    private static BouncyCastleProvider bcProvider = new BouncyCastleProvider();
    
    // This is loaded by WSS4J but since we use it lets make sure its here
    static {
        Security.addProvider(bcProvider);
        try {
            initKeystore();
        } catch (Exception e) {
            localKeyStoreHandler=null;
        }
    }
    
    private static void initKeystore() throws KeyStoreException, CredentialException, IOException, NoSuchAlgorithmException, CertificateException{
        KeyStore keyStore = KeyStore.getInstance("jks");
        keyStore.load(null, null);
        localKeyStoreHandler = new MessageHandlerKeyStore();
        localKeyStoreHandler.setKeyStore(keyStore);
    }
    
    /**
     * Method loads the Merchant P12 key.
     *  IMPORTANT :This change is made based on the assumptions that at point of time , a merchant will have only one P12 Key
     *
     *CertificateCacheEnabled : If it is true then only first time merchant p12 file will be loaded.
     *							If it is false then every time merchant p12 file will be loaded.
     *
     *isValid() method checks : If this method returns true that means existing certificate is valid and reload of merchant p12 file will not happen.                            
     *						  : If method returns false that means existing certificate is not valid and reload of new merchant p12 file will happen.
     *    
     * @param merchantConfig - Merchant Config
     * @param logger - logger instance
     * @throws SignException - Signature exception
     * @throws SignEncryptException
     * @throws ConfigException
     */
    public static void loadMerchantP12File(MerchantConfig merchantConfig, Logger logger) throws SignException, SignEncryptException, ConfigException {
               
        Identity identity=identities.get(merchantConfig.getKeyAlias());
        if(!merchantConfig.isCertificateCacheEnabled() || identity == null || !(identity.isValid(merchantConfig.getKeyFile(), logger))){
            try {
                if (localKeyStoreHandler == null)
                    initKeystore();
            } catch (Exception e) {
                logger.log(Logger.LT_EXCEPTION,
                           "SecurityUtil, cannot instantiate class with keystore error. "
                           + e.getMessage());
                throw new SignException(e.getMessage(), e);
            }
            if(merchantConfig.isJdkCertEnabled()){
                logger.log(Logger.LT_INFO," Loading the certificate from JDK Cert");
                SecurityUtil.readJdkCert(merchantConfig,logger);
            }
			else if(merchantConfig.isCacertEnabled()){
                logger.log(Logger.LT_INFO," Loading the certificate from JRE security cacert file");
                SecurityUtil.loadJavaKeystore(merchantConfig,logger);
            }
            else{
                logger.log(Logger.LT_INFO,"Loading the certificate from p12 file ");
                readAndStoreCertificateAndPrivateKey(merchantConfig, logger);
            }
        }
    }
    
    /**
     * Reads the Certificate or Public key  and Private from the P12 key .
     * @param merchantConfig - Merchant Config details
     * @param logger - logger object
     * @throws SignException
     * @throws SignEncryptException
     */
    
    private static void readAndStoreCertificateAndPrivateKey(MerchantConfig merchantConfig, Logger logger) throws SignException, SignEncryptException {
        KeyStore merchantKeyStore;
        try {
            merchantKeyStore = KeyStore.getInstance(KEY_FILE_TYPE,
                                                    bcProvider);
        } catch (KeyStoreException e) {
            logger.log(Logger.LT_EXCEPTION, "Exception while instantiating KeyStore");
            throw new SignException("Exception while instantiating KeyStore",e);
        }
        
        try {
            merchantKeyStore.load(new FileInputStream(merchantConfig.getKeyFile()), merchantConfig.getKeyPassword().toCharArray());
        } catch (IOException e) {
            logger.log(Logger.LT_EXCEPTION, FAILED_TO_LOAD_KEY_STORE + ", '" + merchantConfig.getKeyFilename() + "'");
            throw new SignException(FAILED_TO_LOAD_KEY_STORE,e);
        } catch (NoSuchAlgorithmException e) {
            logger.log(Logger.LT_EXCEPTION, FAILED_TO_LOAD_KEY_STORE + ", '" + merchantConfig.getKeyFilename() + "'");
            throw new SignException(FAILED_TO_LOAD_KEY_STORE,e);
        } catch (CertificateException e) {
            logger.log(Logger.LT_EXCEPTION, FAILED_TO_LOAD_KEY_STORE + ", '" + merchantConfig.getKeyFilename() + "'");
            throw new SignException(FAILED_TO_LOAD_KEY_STORE,e);
        } catch (ConfigException e) {
            logger.log(Logger.LT_EXCEPTION, FAILED_TO_LOAD_KEY_STORE + ", '" + merchantConfig.getKeyFilename() + "'");
            throw new SignException(FAILED_TO_LOAD_KEY_STORE,e);
        }
        
        // our p12 files do not contain an alias as a normal name, its the common name and serial number
        String merchantKeyAlias;
        try {
            Enumeration enumKeyStore = merchantKeyStore.aliases();
            while (enumKeyStore.hasMoreElements()) {
                KeyStore.PrivateKeyEntry keyEntry = null;
                merchantKeyAlias = (String) enumKeyStore.nextElement();
                if (merchantKeyAlias.contains(merchantConfig.getKeyAlias())){
                    try {
                        keyEntry = (KeyStore.PrivateKeyEntry) merchantKeyStore.getEntry
                        (merchantKeyAlias, new KeyStore.PasswordProtection(merchantConfig.getKeyPassword().toCharArray()));
                    } catch (NoSuchAlgorithmException e) {
                        logger.log(Logger.LT_EXCEPTION, FAILED_TO_OBTAIN_PRIVATE_KEY + ", '" + merchantConfig.getKeyAlias() + "'");
                        throw new SignException(FAILED_TO_OBTAIN_PRIVATE_KEY, e);
                    } catch (UnrecoverableEntryException e) {
                        logger.log(Logger.LT_EXCEPTION, FAILED_TO_OBTAIN_PRIVATE_KEY + ", '" + merchantConfig.getKeyAlias() + "'");
                        throw new SignException(FAILED_TO_OBTAIN_PRIVATE_KEY, e);
                    } catch (KeyStoreException e) {
                        logger.log(Logger.LT_EXCEPTION, FAILED_TO_OBTAIN_PRIVATE_KEY + ", '" + merchantConfig.getKeyAlias() + "'");
                        throw new SignException(FAILED_TO_OBTAIN_PRIVATE_KEY, e);
                    }
                    
                    Identity identity = new Identity(merchantConfig,(X509Certificate) keyEntry.getCertificate(),keyEntry.getPrivateKey(),logger);
                    localKeyStoreHandler.addIdentityToKeyStore(identity, logger);
                    identities.put(identity.getKeyAlias(), identity);
                    continue;
                }
                Identity identity = new Identity(merchantConfig, (X509Certificate) merchantKeyStore.getCertificate(merchantKeyAlias));
                localKeyStoreHandler.addIdentityToKeyStore(identity, logger);
                identities.put(identity.getName(), identity);
            }
        } catch (KeyStoreException e) {
            logger.log(Logger.LT_EXCEPTION, FAILED_TO_OBTAIN_PRIVATE_KEY + ", '" + merchantConfig.getKeyAlias() + "'");
            throw new SignException(FAILED_TO_OBTAIN_PRIVATE_KEY, e);
        }
    }


    /**
     * Create signed encrypted document
     * @param signedDoc
     * @param merchantId
     * @param logger
     * @return Document
     * @throws SignEncryptException
     * @throws SignException
     */
    public static Document handleMessageCreation(Document signedDoc, String merchantId, Logger logger) throws SignEncryptException, SignException{
        
        logger.log(Logger.LT_INFO, "Encrypting Signed doc ...");
        
        WSSecHeader secHeader = new WSSecHeader();
        try {
            secHeader.insertSecurityHeader(signedDoc);
        } catch (WSSecurityException e) {
            logger.log(Logger.LT_EXCEPTION, "Exception while adding document in soap securiy header for MLE");
            throw new SignException(e);
        }
        
        WSSecEncrypt encrBuilder = new WSSecEncrypt();
        //Set the user name to get the encryption certificate.
        //The public key of this certificate is used, thus no password necessary. The user name is a keystore alias usually.
        String serverAlias = getServerAlias(identities);
        encrBuilder.setUserInfo(identities.get(serverAlias).getKeyAlias());
        /*This is to reference a public key or certificate when signing or encrypting a SOAP message.
         *The following valid values for these configuration items are:
         *IssuerSerial (default),DirectReference[BST],X509KeyIdentifier,Thumbprint,SKIKeyIdentifier,KeyValue (signature only),EncryptedKeySHA1 (encryption only)
         */
        encrBuilder.setKeyIdentifierType(WSConstants.X509_KEY_IDENTIFIER);
        
        //This encryption algorithm is used to encrypt the data.
        encrBuilder.setSymmetricEncAlgorithm(WSConstants.AES_256);
        
        //Sets the algorithm to encode the symmetric key. Default is the WSConstants.KEYTRANSPORT_RSAOEP algorithm.
        //encrBuilder.setKeyEnc(WSConstants.KEYTRANSPORT_RSAOEP);
        
        
        //Create signed document
        //Document signedDoc = createSignedDoc(workingDocument,senderAlias,password,secHeader);
        
        Document signedEncryptedDoc;
        try {
            //Builds the SOAP envelope with encrypted Body and adds encrypted key.
            // If no external key (symmetricalKey) was set ,generate an encryption
            // key (session key) for this Encrypt element. This key will be
            // encrypted using the public key of the receiver
            signedEncryptedDoc = encrBuilder.build(signedDoc, localKeyStoreHandler, secHeader);
        } catch (WSSecurityException e) {
            logger.log(Logger.LT_EXCEPTION, "Failed while encrypting signed request for , '" + merchantId + "'" + " with " + serverAlias);
            throw new SignEncryptException("Failed while encrypting signed request for , '" + merchantId + "'" + " with " + serverAlias, e);
        }
        encrBuilder.prependToHeader(secHeader);
        return signedEncryptedDoc;
    }

    /**
     * Create signed document
     * @param workingDocument
     * @param keyAlias
     * @param password
     * @param logger
     * @return Document
     * @throws SignException
     */
    public static Document createSignedDoc(Document workingDocument,String keyAlias, String password,Logger logger) throws SignException {
        
        logger.log(Logger.LT_INFO, "Signing request...");
        //long startTime = System.nanoTime();
        WSSecHeader secHeader = new WSSecHeader();
        try {
            secHeader.insertSecurityHeader(workingDocument);
        } catch (WSSecurityException e) {
            logger.log(Logger.LT_EXCEPTION,
                       "Exception while signing XML document");
            throw new SignException(e);
        }
        
        WSSecSignature sign = new WSSecSignature();
        
        sign.setUserInfo(keyAlias, password);
        
        //sign.setUserInfo(mc.getKeyAlias(), mc.getPassword());
        sign.setDigestAlgo(DIGEST_ALGORITHM);
        sign.setSignatureAlgorithm(SIGNATURE_ALGORITHM);
        sign.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
        sign.setUseSingleCertificate(true);
        
        //Set which parts of the message to encrypt/sign.
        WSEncryptionPart msgBodyPart = new WSEncryptionPart(WSConstants.ELEM_BODY, WSConstants.URI_SOAP11_ENV, "");
        sign.setParts(Collections.singletonList(msgBodyPart));
        try {
            Document document = sign.build(workingDocument, localKeyStoreHandler, secHeader);
            //System.out.println("SecurityUtil.createSignedDoc time taken to sign the request is " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime) + " ms");
            return document;
        } catch (WSSecurityException e) {
            logger.log(Logger.LT_EXCEPTION, "Failed while signing request for , '" + keyAlias + "'");
            throw new SignException(e.getMessage(),e);
        }
    }


    /**
     * read jdk certificates from cacerts file
     * @param merchantConfig
     * @param logger
     * @throws SignEncryptException
     * @throws SignException
     */
	public static void readJdkCert(MerchantConfig merchantConfig, Logger logger)
			throws SignEncryptException, SignException {
		KeyStore keystore;
		try {
			FileInputStream is = new FileInputStream(merchantConfig.getKeyFile());
			keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			keystore.load(is, merchantConfig.getKeyPassword().toCharArray());
		} catch (Exception e) {
			logger.log(Logger.LT_EXCEPTION, "Failed to load the key , '" + merchantConfig.getKeyAlias() + "'");
			throw new SignException(e);
		}

		String merchantKeyAlias;
		try {
			Enumeration enumKeyStore = keystore.aliases();
			if (!enumKeyStore.hasMoreElements()) {
				throw new SignException("Empty Keystore or Missing Certificate ");
			}
			while (enumKeyStore.hasMoreElements()) {
				KeyStore.PrivateKeyEntry keyEntry;
				merchantKeyAlias = (String) enumKeyStore.nextElement();
				if (merchantKeyAlias.contains(merchantConfig.getKeyAlias())) {
					try {
						keyEntry = (KeyStore.PrivateKeyEntry) keystore.getEntry(merchantKeyAlias,
								new KeyStore.PasswordProtection(merchantConfig.getKeyPassword().toCharArray()));
                    } catch (NoSuchAlgorithmException e) {
                        logger.log(Logger.LT_EXCEPTION,
                                FAILED_TO_OBTAIN_PRIVATE_KEY + ", '"
                                        + merchantConfig.getKeyAlias() + "'");
                        throw new SignException(e);
                    } catch (UnrecoverableEntryException e) {
                        logger.log(Logger.LT_EXCEPTION,
                                FAILED_TO_OBTAIN_PRIVATE_KEY + ", '"
                                        + merchantConfig.getKeyAlias() + "'");
                        throw new SignException(e);
                    } catch (KeyStoreException e) {
                        logger.log(Logger.LT_EXCEPTION,
                                FAILED_TO_OBTAIN_PRIVATE_KEY + ", '"
                                        + merchantConfig.getKeyAlias() + "'");
                        throw new SignException(e);
                    }

					Identity identity = new Identity(merchantConfig, (X509Certificate) keyEntry.getCertificate(),
							keyEntry.getPrivateKey(), logger);
					localKeyStoreHandler.addIdentityToKeyStore(identity, logger);
					identities.put(identity.getKeyAlias(), identity);
					continue;
				}
				Identity identity = new Identity(merchantConfig,
						(X509Certificate) keystore.getCertificate(merchantKeyAlias));
				localKeyStoreHandler.addIdentityToKeyStore(identity, logger);
				identities.put(identity.getName(), identity);
			}
        } catch (KeyStoreException e) {
            logger.log(Logger.LT_EXCEPTION, FAILED_TO_OBTAIN_PRIVATE_KEY + ", '"
                    + merchantConfig.getKeyAlias() + "'");
            throw new SignException(e);
        }

	}
    
	private static void loadJavaKeystore(MerchantConfig merchantConfig, Logger logger)
			throws SignException, SignEncryptException, ConfigException {
		FileInputStream is = null;
		try {
			is = new FileInputStream(merchantConfig.getKeyFile());
			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			keystore.load(is, merchantConfig.getCacertPassword().toCharArray());

			java.security.cert.Certificate[] cert = keystore.getCertificateChain(merchantConfig.getKeyAlias());
			if (cert == null) {
				throw new SignException("Empty Keystore or Missing Certificate ");
			}
			PrivateKey key;
			try {
				key = (PrivateKey) keystore.getKey(merchantConfig.getKeyAlias(),
						merchantConfig.getKeyAlias().toCharArray());
			} catch (UnrecoverableKeyException e) {
				logger.log(Logger.LT_EXCEPTION, "Exception while obtaining private key from KeyStore with alias, '"
						+ merchantConfig.getKeyAlias() + "'");
				throw new SignException(e);
			}
            Identity identity;
            for (java.security.cert.Certificate certificate : cert) {
                if (merchantConfig.getKeyAlias().equals(keystore.getCertificateAlias(certificate))) {
                    identity = new Identity(merchantConfig, (X509Certificate) certificate, key, logger);
                    localKeyStoreHandler.addIdentityToKeyStore(identity, logger);
                    identities.put(identity.getKeyAlias(), identity);
                } else {
                    identity = new Identity(merchantConfig, (X509Certificate) certificate);
                    localKeyStoreHandler.addIdentityToKeyStore(identity, logger);
                    identities.put(identity.getName(), identity);
                }
            }
			java.security.cert.Certificate serverCert = keystore.getCertificate(getServerAlias(identities));
			if (serverCert == null) {
				throw new SignException("Missing Server Certificate ");
			}
			identity = new Identity(merchantConfig, (X509Certificate) serverCert);
			localKeyStoreHandler.addIdentityToKeyStore(identity, logger);
			identities.put(identity.getName(), identity);

		}
        catch (java.security.cert.CertificateException e) {
            logger.log(Logger.LT_EXCEPTION, "Unable to load the certificate," + merchantConfig.getKeyFilename() + "'");
            throw new SignException(e);
        } catch (NoSuchAlgorithmException e) {
            logger.log(Logger.LT_EXCEPTION, "Unable to find the certificate with the specified algorithm");
            throw new SignException(e);
        } catch (FileNotFoundException e) {
            logger.log(Logger.LT_EXCEPTION, "File Not found ");
            throw new SignException(e);
        } catch (KeyStoreException e) {
            logger.log(Logger.LT_EXCEPTION,
                    "Exception while obtaining private key from KeyStore" + merchantConfig.getKeyFilename() + "'");
            throw new SignException(e);
        } catch (IOException e) {
            logger.log(Logger.LT_EXCEPTION,
                    FAILED_TO_LOAD_KEY_STORE + ", '" + merchantConfig.getKeyFilename() + "'");
            throw new SignException(e);
        } finally {
			if (null != is)
				try {
					is.close();
				} catch (IOException e) {
					logger.log(Logger.LT_EXCEPTION,
							"Exception while closing FileStream, '" + merchantConfig.getKeyFilename() + "'");
				}
		}

	}

    protected static String getServerAlias(Map<String, Identity> identitiesMapper) {
        String serverAlias = Utility.SERVER_ALIAS;
        if(!identitiesMapper.containsKey(serverAlias)) {
            if(identitiesMapper.containsKey(serverAlias.toLowerCase())) {
                serverAlias = serverAlias.toLowerCase();
            } else if(identitiesMapper.containsKey(serverAlias.toUpperCase())) {
                serverAlias = serverAlias.toUpperCase();
            } else {
                for(String identityKey :identitiesMapper.keySet()) {
                    if(identityKey.equalsIgnoreCase(serverAlias)) {
                        serverAlias = identityKey;
                        break;
                    }
                }
            }
        }
        return serverAlias;
    }
}
