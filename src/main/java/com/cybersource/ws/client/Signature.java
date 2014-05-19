/* Copyright 2003-2004 CyberSource Corporation */

package com.cybersource.ws.client;

import java.io.*;
import java.util.*;
import org.w3c.dom.Document;
import com.com.com.a;
import com.com.com.security.c;


/**
 * Singature class helps in signing the request document.
 * This class uses pkcs12 file for signing the request document. pkcs12 key has to be generated for specific client
 * and stored on the local directory. 
 * 
 * @author sunagara
 *
 */
public class Signature
{
	
   /**
    * Context used by the security library.
    */
   private static com.com.com.c context = null;

   /**
    * Exception that occurred during initialization, if any.
    */
   private static SignException initException = null;

   /**
    * Identity cache.  We use a Hashtable because it is synchronized.
    */
   private static Hashtable<String,CachedIdentity> identities = new Hashtable();

   static class CachedIdentity
   {
      private final long lastModified;
      private final com.com.com.security.c identity;
      public CachedIdentity(com.com.com.security.c identity, long lastModified)
      {
         this.identity = identity;
         this.lastModified = lastModified;
      }

      long getLastModified()
      {
         return lastModified;
      }

      com.com.com.security.c getIdentity()
      {
         return identity;
      }

      public boolean isValid(File keyFile)
      {
         return getLastModified() == keyFile.lastModified();
      }
   }


   static
   {
      internalInitializeContext();
   }


   /**
    * Signs the given document.
    *
    * @param merchantID	the merchant id.
    * @param keyFile		File object representing the merchant's key file.
    * @param doc			the document to sign.
    *
    * @throws SignException if the signing fails for any reason.  Its
    *                       inner exception will contain the actual exception.
    */
   public static Document sign(
         String merchantID, File keyFile, Document doc )
         throws SignException
   {
      if (initException != null)
      {
         throw initException;
      }

     /* try
      { *****/
    	  com.com.com.security.c id = getIdentity( merchantID, keyFile );
         return( com.com.com.com.com.f.a( context, doc, id ) );
     /*}
      catch (com.com.com.cybersource.n e)
      {
         throw new SignException( e );
      }*****/
   }

   /**
    * Initializes the context used by the security library.  If you experience
    * significant delay on the first request, you may call this method in
    * some start-up routine to avoid delaying the first request.  The start-up
    * routine must be executed before any threads that use the clients are
    * spawned as this method is not synchronized.
    *
    * @throws SignException if initialization fails.
    */
   public static void initializeContext()
         throws SignException
   {
      internalInitializeContext();
      if (initException != null)
      {
         throw initException;
      }
   }

   /**
    * Initializes the context used by the security library.  This method is
    * called once and only once in the static constructor.  If the merchant
    * has called initializeContext() themselves, then this method will not
    * do anything.
    */
   private static void internalInitializeContext()
   {
       if (context == null && initException == null)
         {
        	
        	context = (com.com.com.c) a.a(a.a );
            
         }
       
   }

   /**
    * Obtains from the cache the identity for the given merchant, or if not
    * there yet, reads it from the pkcs12 file and adds it to the cache.
    *
    * @param merchantID	the merchant id.
    * @param keyFile		File object representing the merchant's key file.
    *
    * @throws SignException	if retrieval or caching of identity fails.
    */
   static com.com.com.security.c getIdentity( String merchantID, File keyFile )
         throws SignException
   {
      CachedIdentity cachedIdentity = identities.get(merchantID);
      if (cachedIdentity != null && cachedIdentity.isValid(keyFile))
      {
    	  com.com.com.security.c id = cachedIdentity.getIdentity();
         return( id );
      }

      return( cacheIdentity( merchantID, keyFile ) );
   }

   /**
    * Reads the identity for the given merchant from the pkcs12 file and adds
    * it to the cache.  This method is called once and only once for each
    * merchant id not yet in the cache.  If you experience significant delay
    * on the first request, you may call this method in some start-up routine
    * to avoid delaying the first request.  You would want to call it for
    * each merchantID that will be used by the application.
    * Signature.initializeContext() must precede any Signature.cacheIdentity()
    * calls.
    *
    * @param merchantID	the merchant id.
    * @param keyFile		File object representing the merchant's key file.
    *
    * @throws SignException	if the retrieval or caching of identity fails.
    */
   public static com.com.com.security.c cacheIdentity( String merchantID, File keyFile )
         throws SignException
   {
      try
      {
         byte[] p12 = Utility.read( keyFile );

         c id = null;
         c[] ids = c.b(p12, merchantID);
         
         for (int i = 0; i < ids.length; ++i)
         {
            if (ids[i].d()!= null)
            {
               id = ids[i];
               break;
            }
         }

         if (id != null)
         {
            identities.put( merchantID, new CachedIdentity(id, keyFile.lastModified()) );
            return( id );
         }

         throw new SignException(
               new Exception( "Key file contains invalid content." ) );
      }
      catch (IOException e)
      {
         throw new SignException( e );
      }
   
   }

}	    
