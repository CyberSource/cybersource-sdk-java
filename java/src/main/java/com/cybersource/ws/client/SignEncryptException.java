/* Copyright 2003-2016 CyberSource Corporation */

package com.cybersource.ws.client;

/**
 * Exception is thrown if operation is failed while encrypting hte document.
 */
public class SignEncryptException extends Exception {
    SignEncryptException(String msg, Throwable throwable) { super(msg,throwable);}
    SignEncryptException(String msg) { super(msg);}
}