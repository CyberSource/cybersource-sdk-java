
/**
 *  Copyright 1998, 1999 CyberSource Corporation.  All rights reserved.
 *
 * @author Hubert Chen
 * $Id: ICSClientProxyTest.java,v 1.4 2001/03/15 23:45:34 vshankar Exp $
 * 
 */

/**
 * A short, simple example program for sending requests to ICS.
 */

import com.cybersource.ics.base.message.*;
import com.cybersource.ics.base.exception.*;
import com.cybersource.ics.client.message.*;
import com.cybersource.ics.client.*;
import java.util.*;
import java.net.*;

public class ICSClientProxyTest {
  public static void main(String []args) throws ICSException,
    MalformedURLException  {
      System.getProperties().put("proxySet","true");
  //    System.getProperties().put("proxyHost","proxy.cybersource.com");
      System.getProperties().put("proxyHost","10.2.7.88");
      System.getProperties().put("proxyPort","6588");

      ICSClient client = new ICSClient("../properties/ICSClient.props");

      ICSClientOffer offer = new ICSClientOffer();
      ICSClientRequest request = new ICSClientRequest();

      request.setField("ics_applications", "ics_tax");
      request.setField("merchant_ref_number", "007");
      //request.setField("merchant_id", client.getMerchantID());
      request.setField("customer_firstname", "Joe");
      request.setField("customer_lastname", "Smith");

      request.setField("customer_email", "nobody@cybersource.com");
      request.setField("customer_phone", "800.555.1212");
      request.setField("bill_address1", "2134 N. 7th");
      request.setField("bill_address2", "Apt. 42");
      request.setField("bill_city", "San Jose");
      request.setField("bill_state", "CA");
      request.setField("bill_zip", "95128");
      request.setField("bill_country", "USA");

      offer.setField("amount", "10.99");
      offer.setField("quantity", "1");
      request.addOffer(offer);


      System.out.println("-- request --");

      System.out.println(request);
      System.out.println("-- response --");
      ICSReply reply = client.send(request);
      System.out.println(reply);
      System.out.println("-- end --");

      if ( reply.getReplyCode() <= 0 )
      {
        System.out.println("Transaction failed: " + reply.getErrorMessage());
        throw new ICSException("Test failed!");
      } else {
        System.out.println("Transaction succeeded");
      }
    }
}

