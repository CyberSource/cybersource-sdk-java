/* Copyright 2003-2016 CyberSource Corporation */

package com.cybersource.ws.client;

/**
 * Exception that is thrown by the SignEncryptException object.  Note that unless you are
 * calling SignAndEncryptedMessageHandler.initializeContext() or SignAndEncryptedMessageHandler.cacheIdentity() yourself,
 * you do not have to catch this exception as it would be the inner exception
 * of a ClientException.
 */
public class SignEncryptException extends Exception {
    SignEncryptException(String msg, Throwable throwable) { super(msg,throwable);}
    SignEncryptException(String msg) { super(msg);}
}