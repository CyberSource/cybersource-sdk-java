
/**
 *  Copyright 1998, 1999 CyberSource Corporation.  All rights reserved.
 *
 * @author Hubert Chen
 * $Id: ICSAuthTest.java,v 1.4 2001/04/03 19:00:15 darin Exp $
 * 
 */

/**
 * A short, simple example program for sending requests to ICS.
 */

import com.cybersource.ics.base.message.*;
import com.cybersource.ics.base.exception.*;
import com.cybersource.ics.client.ICSClient;
import com.cybersource.ics.client.message.*;

import java.util.*;
import java.net.*;

public class ICSAuthTest {
    private static final Properties cyberProperties = Util.readPropertyFile("cybs.properties");


  public static void main(String []args) throws ICSException,
    MalformedURLException {
      Properties authProps = Util.readPropertyFile("auth.properties");
      ICSClientRequest request = new ICSClientRequest();
      ICSOffer offer = new ICSOffer();

      //ICSClient client = new ICSClient("ICSClient.props");


      // This is the original code. Now,we will read from the auth.properties file
/*
      request.setField("ics_applications", "ics_auth");
      request.setField("merchant_ref_number", "019");
      //request.setField("merchant_id", client.getMerchantID());
      request.setField("customer_firstname", "Joe");
      request.setField("customer_lastname", "Smith");
      request.setField("customer_email", "nobody@cybersource.com");
      request.setField("customer_phone", "800.555.1212");
      request.setField("bill_address1", "2134 N. 7th");
      request.setField("bill_address2", "Apt. 42");
      request.setField("bill_city", "Portland");
      request.setField("bill_state", "ME");
      request.setField("bill_zip", "01275");
      request.setField("bill_country", "USA");
      request.setField("customer_cc_number", "4111111111111111");
      request.setField("customer_cc_expmo", "12");
      request.setField("customer_cc_expyr", "2025");
      request.setField("currency", "USD");


      offer.setField("product_name", "Bookend");
      offer.setField("product_code", "electronic_software");
      offer.setField("merchant_product_sku", "no_packer_test");
      offer.setField("amount", "10.99");
      offer.setField("quantity", "2");
      offer.setField("packer_code", "portland10");
      request.addOffer(offer);
      */

      request = Util.convertPropertyFileToICSClientRequest(authProps);
      System.out.println("-- request --");
      System.out.println(request);

      //ICSReply reply = client.send(request);
      ICSReply reply = Util.processRequest(request, cyberProperties);
      System.out.println("-- response --");
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
