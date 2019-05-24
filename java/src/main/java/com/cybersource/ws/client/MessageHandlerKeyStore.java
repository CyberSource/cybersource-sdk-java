package com.cybersource.ws.client;

import org.apache.ws.security.components.crypto.CredentialException;
import org.apache.ws.security.components.crypto.Merlin;

import java.io.IOException;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Properties;

/**
 * Created by jeaton on 3/11/2016.
 */
public class MessageHandlerKeyStore extends Merlin {

	public MessageHandlerKeyStore() throws CredentialException, IOException {
        super(null);
        properties = new Properties();
    }

    public void addIdentityToKeyStore(Identity id, Logger logger) throws SignEncryptException {
        if (id == null)
            return;
        X509Certificate certificate = id.getX509Cert();
        PrivateKey privateKey = id.getPrivateKey();
        try {
            if (privateKey != null) {
                X509Certificate[] certChain = {certificate};
                getKeyStore().setKeyEntry(id.getKeyAlias(), privateKey, id.getPswd(), certChain);
            } else {
                getKeyStore().setCertificateEntry(id.getKeyAlias(), certificate);
            }
        } catch (KeyStoreException e) {
        	logger.log(Logger.LT_EXCEPTION, "MessageHandlerKeyStore cannot parse identity, " + id + "'");
            throw new SignEncryptException("MessageHandlerKeyStore, " +
                    "cannot parse identity, " + id + "'", e);
        }
    }

}
