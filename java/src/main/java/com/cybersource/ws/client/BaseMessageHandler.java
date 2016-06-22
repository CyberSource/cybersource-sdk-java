package com.cybersource.ws.client;

import org.apache.ws.security.components.crypto.CredentialException;


import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * Created by jeaton on 3/10/2016.
 */
public class BaseMessageHandler {

    MessageHandlerKeyStore localKeyStoreHandler = null;
    Logger logger = null;
    BaseMessageHandler(Logger logger) throws SignEncryptException {
        if (logger != null) this.logger = logger;

        try {
            localKeyStoreHandler = new MessageHandlerKeyStore(logger);
            KeyStore keyStore = KeyStore.getInstance("jks");
            keyStore.load(null, null);
            localKeyStoreHandler.setKeyStore(keyStore);
        } catch (CredentialException e) {
            throw new SignEncryptException("BaseMessageHandler, " +
                    "cannot instantiate class with keystore error.", e);
        } catch (IOException e) {
            throw new SignEncryptException("BaseMessageHandler, " +
                    "cannot instantiate class with keystore error.", e);
        } catch (KeyStoreException e) {
            throw new SignEncryptException("BaseMessageHandler, " +
                    "cannot instantiate class with keystore error.", e);
        } catch (CertificateException e) {
            throw new SignEncryptException("BaseMessageHandler, " +
                    "cannot instantiate class with keystore error.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new SignEncryptException("BaseMessageHandler, " +
                    "cannot instantiate class with keystore error.", e);
        }
    }

    public void addIdentityToKeyStore( Identity id ) throws SignEncryptException { localKeyStoreHandler.addIdentityToKeyStore(id);}
}
