/*
* Copyright 2003-2014 CyberSource Corporation
*
* THE SOFTWARE AND THE DOCUMENTATION ARE PROVIDED ON AN "AS IS" AND "AS
* AVAILABLE" BASIS WITH NO WARRANTY.  YOU AGREE THAT YOUR USE OF THE SOFTWARE AND THE
* DOCUMENTATION IS AT YOUR SOLE RISK AND YOU ARE SOLELY RESPONSIBLE FOR ANY DAMAGE TO YOUR
* COMPUTER SYSTEM OR OTHER DEVICE OR LOSS OF DATA THAT RESULTS FROM SUCH USE. TO THE FULLEST
* EXTENT PERMISSIBLE UNDER APPLICABLE LAW, CYBERSOURCE AND ITS AFFILIATES EXPRESSLY DISCLAIM ALL
* WARRANTIES OF ANY KIND, EXPRESS OR IMPLIED, WITH RESPECT TO THE SOFTWARE AND THE
* DOCUMENTATION, INCLUDING ALL WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE,
* SATISFACTORY QUALITY, ACCURACY, TITLE AND NON-INFRINGEMENT, AND ANY WARRANTIES THAT MAY ARISE
* OUT OF COURSE OF PERFORMANCE, COURSE OF DEALING OR USAGE OF TRADE.  NEITHER CYBERSOURCE NOR
* ITS AFFILIATES WARRANT THAT THE FUNCTIONS OR INFORMATION CONTAINED IN THE SOFTWARE OR THE
* DOCUMENTATION WILL MEET ANY REQUIREMENTS OR NEEDS YOU MAY HAVE, OR THAT THE SOFTWARE OR
* DOCUMENTATION WILL OPERATE ERROR FREE, OR THAT THE SOFTWARE OR DOCUMENTATION IS COMPATIBLE
* WITH ANY PARTICULAR OPERATING SYSTEM.
*/

package com.cybersource.ws.client;

import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecSignature;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;

/**
 * This class helps in loading the P12 Key and Signing the XML Request Document using Apache WS Security packages.
 * User: jeaton
 * Date: 6/24/14
 * Time: 12:19 PM
 */
class ApacheSignatureWrapper {
    private static final String KEY_FILE_TYPE = "PKCS12";

    private static X509Certificate merchantCertificate = null;
    private static PrivateKey merchantPrivateKey = null;
    private static String currentMerchantId = null;
    // This is loaded by WSS4J but since we use it lets make sure its here
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
	 * This helps to Sign the XML request document using Apache WS Security jars and Wraps the SOAP XML request with Signature tags.
     * @param trxnDataXml  - Input transaction details.
     * @param merchantConfig - Merchant Config
     * @param logger - Logget isntance
     * @return  - Signed SOAP XML Document request.
     * @throws SignException
     */
    public static Document soapWrapAndSign(String trxnDataXml, MerchantConfig merchantConfig, Logger logger)
            throws SignException {

        Document doc = getDocumentFromString(trxnDataXml, logger);

        loadMerchantP12File(merchantConfig, logger);

        WSSecSignature wsSecSignature = new WSSecSignature();
        wsSecSignature.setX509Certificate(merchantCertificate);
        wsSecSignature.setUseSingleCertificate(true);
        wsSecSignature.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);

        WSEncryptionPart msgBodyPart = new WSEncryptionPart(WSConstants.ELEM_BODY, WSConstants.URI_SOAP11_ENV, "");
        wsSecSignature.setParts(Collections.singletonList(msgBodyPart));

        WSSecHeader secHeader = new WSSecHeader();
        try {
            secHeader.insertSecurityHeader(doc);
        } catch (WSSecurityException e) {
            logger.log(Logger.LT_EXCEPTION, "Exception while signing XML document");
            throw new SignException(e);
        }

        String keyFileName = "";
        if (merchantConfig.getKeyFilename() != null) {
            if (merchantConfig.getKeysDirectory() != null) {
                keyFileName = merchantConfig.getKeysDirectory() + File.separatorChar + merchantConfig.getKeyFilename();
            }
        }else{
        	 keyFileName = merchantConfig.getKeysDirectory() + File.separatorChar + merchantConfig.getMerchantID() + ".p12";
        }

        Properties properties = new Properties();
        properties.setProperty("org.apache.ws.security.crypto.merlin.keystore.provider", "BC");
        properties.setProperty("org.apache.ws.security.crypto.merlin.cert.provider", "BC");
        properties.setProperty("org.apache.ws.security.crypto.merlin.keystore.file", keyFileName);
        properties.setProperty("org.apache.ws.security.crypto.merlin.keystore.password", merchantConfig.getKeyPassword());
        properties.setProperty("org.apache.ws.security.crypto.merlin.keystore.type", KEY_FILE_TYPE);

        // we need to create alias for our keystores, it looks like "serialNumber=4032987129910179089277,CN=jasoneatoncorp"
        // for an unknown reason the serial number of the certificate is set incorrectly, we must parse it from DN
        String keySerial = merchantCertificate.getSubjectDN().getName();
        if (keySerial != null) {
            String keySerialrray[] = keySerial.split("SERIALNUMBER=");
            if (keySerialrray.length != 2) {
                logger.log(Logger.LT_EXCEPTION, "Exception while obtaining private key from KeyStore with alias, '" + merchantConfig.getKeyAlias() + "'");
                throw new SignException("Exception while obtaining private key from KeyStore with alias, '" + merchantConfig.getKeyAlias() + "'");
            }
            keySerial = keySerialrray[1];
        } else {
            logger.log(Logger.LT_EXCEPTION, "Exception while obtaining private key from KeyStore with alias, '" + merchantConfig.getKeyAlias() + "'");
            throw new SignException("Exception while obtaining private key from KeyStore with alias, '" + merchantConfig.getKeyAlias() + "'");
        }

        String keyAlias = "serialNumber=" + keySerial + ",CN=" + merchantConfig.getMerchantID();
        properties.setProperty("org.apache.ws.security.crypto.merlin.keystore.alias", keyAlias);

        wsSecSignature.setUserInfo(keyAlias, merchantConfig.getKeyPassword());

        Document soapDocument;
        try {
            soapDocument = wsSecSignature.build(doc, CryptoFactory.getInstance(properties), secHeader);
        } catch (WSSecurityException e) {
            logger.log(Logger.LT_EXCEPTION, "Exception while signing XML document");
            throw new SignException(e);
        }

        return soapDocument;
    }

    /** Reads the Transaction XML String and returns Document object
     * @param trxnDataXml - Input XML string
     * @param logger - logger 
     * @return - Document object
     * @throws SignException
     */
    private static Document getDocumentFromString(String trxnDataXml, Logger logger) throws SignException {
        Document doc;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder builder = Utility.newDocumentBuilder();
            StringReader sr = new StringReader(trxnDataXml);
            doc = builder.parse(new InputSource(sr));
            sr.close();
        } catch (ParserConfigurationException e) {
            logger.log(Logger.LT_EXCEPTION, "Exception while signing XML document");
            throw new SignException(e);
        } catch (SAXException e) {
            logger.log(Logger.LT_EXCEPTION, "Exception while signing XML document");
            throw new SignException(e);
        } catch (IOException e) {
            logger.log(Logger.LT_EXCEPTION, "Exception while signing XML document");
            throw new SignException(e);
        }
        return doc;
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
        if (merchantCertificate == null || merchantPrivateKey == null) {
        	readAndStoreCertificateAndPrivateKey( merchantConfig,  logger);
        }else if(  !currentMerchantId.equals(merchantConfig.getMerchantID() )){
        	readAndStoreCertificateAndPrivateKey( merchantConfig,  logger);
        }
    }
    
    /**
	 *Reads the Certificate or Public key  and Private from the P12 key .
     * @param merchantConfig - Merchant Config details
     * @param logger - logger object
     * @throws SignException
     */
    private static void readAndStoreCertificateAndPrivateKey(MerchantConfig merchantConfig,Logger logger) throws SignException{
    	
    	
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
        try {
            Enumeration enumKeyStore = merchantKeyStore.aliases();
            while (enumKeyStore.hasMoreElements()) {
                merchantKeyAlias = (String) enumKeyStore.nextElement();
                if (merchantKeyAlias.contains(merchantConfig.getKeyAlias()))
                    break;
            }
        } catch (KeyStoreException e) {
            logger.log(Logger.LT_EXCEPTION, "Exception while obtaining private key from KeyStore with alias, '" + merchantConfig.getKeyAlias() + "'");
            throw new SignException(e);
        }

        KeyStore.PrivateKeyEntry keyEntry;
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

        merchantCertificate = (X509Certificate) keyEntry.getCertificate();
        merchantPrivateKey = keyEntry.getPrivateKey();
        currentMerchantId = merchantConfig.getMerchantID();
        if (merchantCertificate == null || merchantPrivateKey == null) {
            logger.log(Logger.LT_EXCEPTION, "No valid entries found in the KeyStore, check alias, '" + merchantConfig.getKeyAlias() + "'");
            throw new SignException("No valid entries found in the KeyStore, check alias, '" + merchantConfig.getKeyAlias() + "'");
        }
    }
}
