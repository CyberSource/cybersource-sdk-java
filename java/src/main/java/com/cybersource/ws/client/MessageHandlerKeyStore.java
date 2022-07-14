package com.cybersource.ws.client;

import org.apache.wss4j.common.crypto.Merlin;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Properties;

/**
 * This is to add identity to keystore.
 */
public class MessageHandlerKeyStore extends Merlin {

    /**
     * @throws IOException
     */
	public MessageHandlerKeyStore() {
        super();
        properties = new Properties();
    }

    /**
     * Add Identity to KeyStore
     * @param id Identity
     * @param logger Logger
     * @throws SignEncryptException
     */
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
