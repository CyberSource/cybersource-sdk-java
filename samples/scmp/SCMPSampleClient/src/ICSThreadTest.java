
/**
 *  Copyright 1998, 1999 CyberSource Corporation.  All rights reserved.
 *
 * @author Hubert Chen
 * $Id: ICSThreadTest.java,v 1.3 2001/03/16 23:42:37 vshankar Exp $
 * 
 */

/**
 * A short, simple example program for sending requests to ICS.
 */

import com.cybersource.ics.base.message.*;
/*
 * This test program demonstrates a simple example of using threads
 * to send multiple ICS transactions
 */

import com.cybersource.ics.base.exception.*;
import com.cybersource.ics.client.message.*;
import com.cybersource.ics.client.*;
import java.util.*;
import java.net.*;
import java.lang.*;

public class ICSThreadTest extends Thread{

  public static ICSClient client;

  public static void usage() 
  {
    System.out.println("usage: java ICSThreadTest [loops] [threads]");
  }

  public static void main(String []args) throws Exception
  {
    /*
     * Default number of threads and loops
     */
    int numLoops = 5;
    int numThreads = 5;
    int j;

    /*
     * parse command line arguments
     */
    if ( args.length > 0 && args[0] != null ) {
        numLoops = Integer.parseInt(args[0]);
    }
    if ( args.length > 1 && args[1] != null ) {
        numThreads = Integer.parseInt(args[1]);
    }
    if ( args.length > 2 ) {
        usage();
        return;
    }

    System.out.println("Test running " + numLoops + " loops of " + 
        numThreads + " threads each");

    /*
     * Reuse the ICSClient object, because whenever a new ICSClient
     * object is created, it must load each of the keys from files.
     * The only time a new ICSClient object needs to be created is
     * when the keys, merchant id, server id, or URL changes.
     */
    client = new ICSClient("../properties/ICSClient.props");


    /*
     * Start the loop of threads
     */
    for( j = 1; j < numLoops + 1; j++ ) {
       Thread arr[] = new Thread[numThreads];
       int i = 0;
       for(i = 1; i < numThreads + 1; i++) {
          arr[i-1] = new ICSThreadTest();
          arr[i-1].start();
       }

       /*
        * Here we wait for the threads sequentially. Not
        * the most efficient, but nice and simple
        */
       for(i = 1; i < numThreads + 1; i++) {
          try{ arr[i-1].join(); } catch(InterruptedException e) {}
          System.out.println("Loop: " + j + " Thread: " + i + " OK");
       }
    }
  }

  public void run() 
  {
      try {
          ICSClientOffer offer = new ICSClientOffer();
          ICSClientRequest request = new ICSClientRequest();
    
          /*
           * Set request fields
           */
          request.setField("ics_applications", "ics_auth");
          request.setField("merchant_ref_number", "007");
          //request.setField("merchant_id", client.getMerchantID());
          request.setField("customer_firstname", "Joe");
          request.setField("customer_lastname", "Smith");
          request.setField("customer_email", "joe@nowhere.com");
          request.setField("customer_phone", "800.555.1212");
          request.setField("bill_address1", "2134 N. 7th");
          request.setField("bill_address2", "Apt. 42");
          request.setField("bill_city", "Portland");
          request.setField("bill_state", "ME");
          request.setField("bill_zip", "01275");
          request.setField("bill_country", "USA");
          request.setField("customer_cc_number", "4111111111111111");
          request.setField("customer_cc_expmo", "12");
          request.setField("customer_cc_expyr", "2020");
          request.setField("currency", "USD");

          /*
           * Set offer fields
           */
          offer.setField("amount", "10.99");
          offer.setField("quantity", "2");
          request.addOffer(offer);

          ICSReply reply = client.send(request);

          if ( reply.getReplyCode() <= 0 )
          {
              System.out.println("Transaction failed: " + reply.getErrorMessage());
              throw new ICSException("Test failed!");
          }
    } catch( Exception e ){}
  }


}
