package com.cybersource.ws.client;



import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.conversation.ConversationException;
import org.apache.ws.security.message.WSSecDKEncrypt;
import org.apache.ws.security.message.WSSecEncryptedKey;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecSignature;
import org.apache.xml.security.signature.XMLSignature;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.w3c.dom.Document;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * Created by jeaton on 3/1/2016.
 */
public class SignedAndEncryptedMessageHandler extends BaseMessageHandler {

    private static final String KEY_FILE_TYPE = "PKCS12";
    
    public static List<Identity> identities = new ArrayList<Identity>();
    
	private static String currentMerchantId = null;
	
    private static final String SERVER_ALIAS = "CyberSource_SJC_US";
    
	// This is loaded by WSS4J but since we use it lets make sure its here
    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    
    private SignedAndEncryptedMessageHandler(MerchantConfig merchantConfig, Logger logger) throws SignEncryptException, SignException {
        super(logger);
         // load keystore from disk p12 file (not keystore)
        loadMerchantP12File(merchantConfig, logger);
        for(int pos=0;pos<identities.size();pos++) {
            localKeyStoreHandler.addIdentityToKeyStore(identities.get(pos));
        }

    }

    static SignedAndEncryptedMessageHandler getInstance(MerchantConfig merchantConfig, Logger logger)
            throws SignEncryptException, SignException {
        SignedAndEncryptedMessageHandler signedAndEncryptedMessageHandler = new SignedAndEncryptedMessageHandler(merchantConfig,logger);
        return signedAndEncryptedMessageHandler;
    }
    
    /**
    * Method loads the Merchant P12 key.
    *  IMPORTANT :This change is made based on the assumptions that at point of time , a merchant will have only one P12 Key 
    * @param merchantConfig - Merchant Config 
    * @param logger - logger instance
    * @throws SignException - Signature exception
    */
   private static void loadMerchantP12File(MerchantConfig merchantConfig, Logger logger) throws SignException {
       // Load the KeyStore and get the signing key and certificate do this once only
       // This change is made based on the assumptions that at point of time , a merchant will have only one P12 Key
       if(  !merchantConfig.getMerchantID().equals(currentMerchantId)){
       	readAndStoreCertificateAndPrivateKey( merchantConfig,  logger);
       }
   }
   
   /**
	 *Reads the Certificate or Public key  and Private from the P12 key .
    * @param merchantConfig - Merchant Config details
    * @param logger - logger object
    * @throws SignException
    */
   
    private static void readAndStoreCertificateAndPrivateKey(
			MerchantConfig merchantConfig, Logger logger) throws SignException {
    	KeyStore merchantKeyStore;
        try {
            merchantKeyStore = KeyStore.getInstance(KEY_FILE_TYPE,
                    new BouncyCastleProvider());
        } catch (KeyStoreException e) {
            logger.log(Logger.LT_EXCEPTION, "Exception while instantiating KeyStore");
            throw new SignException(e);
        }
		
        try {
            merchantKeyStore.load(new FileInputStream(merchantConfig.getKeyFile()), merchantConfig.getKeyPassword().toCharArray());
        } catch (IOException e) {
            logger.log(Logger.LT_EXCEPTION, "Exception while loading KeyStore, '" + merchantConfig.getKeyFilename() + "'");
            throw new SignException(e);
        } catch (NoSuchAlgorithmException e) {
            logger.log(Logger.LT_EXCEPTION, "Exception while loading KeyStore, '" + merchantConfig.getKeyFilename() + "'");
            throw new SignException(e);
        } catch (CertificateException e) {
            logger.log(Logger.LT_EXCEPTION, "Exception while loading KeyStore, '" + merchantConfig.getKeyFilename() + "'");
            throw new SignException(e);
        } catch (ConfigException e) {
            logger.log(Logger.LT_EXCEPTION, "Exception while loading KeyStore, '" + merchantConfig.getKeyFilename() + "'");
            throw new SignException(e);
        }
        
     // our p12 files do not contain an alias as a normal name, its the common name and serial number
        String merchantKeyAlias = null;
        int certIndex = 0;
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
                        logger.log(Logger.LT_EXCEPTION, "Exception while obtaining private key from KeyStore with alias, '" + merchantConfig.getKeyAlias() + "'");
                        throw new SignException(e);
                    } catch (UnrecoverableEntryException e) {
                        logger.log(Logger.LT_EXCEPTION, "Exception while obtaining private key from KeyStore with alias, '" + merchantConfig.getKeyAlias() + "'");
                        throw new SignException(e);
                    } catch (KeyStoreException e) {
                        logger.log(Logger.LT_EXCEPTION, "Exception while obtaining private key from KeyStore with alias, '" + merchantConfig.getKeyAlias() + "'");
                        throw new SignException(e);
                    }
            		identities.add(new Identity(merchantConfig,(X509Certificate) keyEntry.getCertificate(),keyEntry.getPrivateKey()));
            		continue;
            	}
				identities.add(new Identity(merchantConfig, (X509Certificate) merchantKeyStore.getCertificate(merchantKeyAlias)));
            }
            
            if (identities == null || identities.isEmpty()) {
                logger.log(Logger.LT_EXCEPTION, "No valid entries found in the KeyStore, check alias, '" + merchantConfig.getKeyAlias() + "'");
                throw new SignException("No valid entries found in the KeyStore, check alias, '" + merchantConfig.getKeyAlias() + "'");
            }
            currentMerchantId = merchantConfig.getMerchantID();
        } catch (KeyStoreException e) {
            logger.log(Logger.LT_EXCEPTION, "Exception while obtaining private key from KeyStore with alias, '" + merchantConfig.getKeyAlias() + "'");
            throw new SignException(e);
        }
	}

	public Document handleMessageCreation(Document workingDocument, String senderAlias) throws SignException,SignEncryptException{
        if (senderAlias == null)
            throw new SignEncryptException("SignedAndEncryptedMessageHandler - handleMessageCreation," +
                    " specified identity is null");

        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(workingDocument);

        //EncryptedKey
        WSSecEncryptedKey encrKeyBuilder = new WSSecEncryptedKey();
        encrKeyBuilder.setUserInfo(SERVER_ALIAS);
        encrKeyBuilder.setKeyIdentifierType(WSConstants.X509_KEY_IDENTIFIER);
        
        try {
            encrKeyBuilder.prepare(workingDocument, localKeyStoreHandler);
        } catch (WSSecurityException e) {
        	logger.log(Logger.LT_EXCEPTION, "Key builder failed to create keys for , '" + senderAlias + "'" + " with " + SERVER_ALIAS);
            throw new SignEncryptException(e.getMessage(), e);
        }

        byte[] ek = encrKeyBuilder.getEphemeralKey();
        String tokenIdentifier = encrKeyBuilder.getId();

        //Create signed document
        Document signedDoc = createSignedDoc(workingDocument,senderAlias,secHeader);
        
        WSSecDKEncrypt encrBuilder = new WSSecDKEncrypt();
        encrBuilder.setSymmetricEncAlgorithm(WSConstants.AES_256);
        encrBuilder.setExternalKey(ek, tokenIdentifier);
        Document signedEncryptedDoc = null;

        try {
            signedEncryptedDoc = encrBuilder.build(signedDoc,localKeyStoreHandler, secHeader);
        } catch (WSSecurityException e) {
        	logger.log(Logger.LT_EXCEPTION, "Failed while encrypting signed requeest for , '" + senderAlias + "'" + " with " + SERVER_ALIAS);
            throw new SignEncryptException(e.getMessage(), e);
        }

        encrKeyBuilder.prependToHeader(secHeader);
        encrKeyBuilder.prependBSTElementToHeader(secHeader);
       return signedEncryptedDoc;
    }

	public Document createSignedDoc(Document workingDocument,String senderAlias, WSSecHeader secHeader) throws SignException {
		
		if(secHeader==null){
        	secHeader = new WSSecHeader();
        	secHeader.insertSecurityHeader(workingDocument);
    	}
		
		WSSecSignature sign = new WSSecSignature();
	    sign.setUserInfo(senderAlias, senderAlias);
	    sign.setSignatureAlgorithm(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256);
	    sign.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
	    sign.setUseSingleCertificate(true);
	    
	    //Set which parts of the message to encrypt/sign.
	    WSEncryptionPart msgBodyPart = new WSEncryptionPart(WSConstants.ELEM_BODY, WSConstants.URI_SOAP11_ENV, "");
        sign.setParts(new Vector(Collections.singletonList(msgBodyPart)));
		try {
	        return sign.build(workingDocument, localKeyStoreHandler, secHeader);
		} catch (WSSecurityException e) {
	        logger.log(Logger.LT_EXCEPTION, "Failed while signing requeest for , '" + senderAlias + "'");
	        throw new SignException(e.getMessage());
	   }
	}


}
