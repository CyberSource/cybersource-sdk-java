
/**
 *  Copyright 1998, 1999 CyberSource Corporation.  All rights reserved.
 *
 * @author Hubert Chen
 * $Id: ICSTaxTest.java,v 1.4 2001/04/03 19:00:15 darin Exp $
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

public class ICSTaxTest {
    private static final Properties cybsProps = Util.readPropertyFile("cybs.properties");

  public static void main(String []args) throws ICSException,
    MalformedURLException {
      Properties taxProps = Util.readPropertyFile("tax.properties");

      /*
      ICSClient client = new ICSClient("../properties/ICSClient.props");

      ICSClientOffer offer = new ICSClientOffer();
      ICSClientOffer offer2 = new ICSClientOffer();
      ICSClientRequest request = new ICSClientRequest();

      request.setField("ics_applications", "ics_tax");
      request.setField("merchant_ref_number", "007");
      //request.setField("merchant_id", client.getMerchantID());
      request.setField("customer_firstname", "Joe");
      request.setField("customer_lastname", "Smith");
      request.setField("customer_email", "nobody@cybersource.com");
      request.setField("customer_phone", "800.555.1212");
      request.setField("bill_address1", "123 Main");
      request.setField("bill_address2", "");
      request.setField("bill_city", "San Jose");
      request.setField("bill_state", "CA");
      request.setField("bill_zip", "95131");
      request.setField("bill_country", "us");
      request.setField("customer_cc_number", "4111111111111111");

      offer.setField("product_name", "Software Product1999");
      offer.setField("product_code", "electronic_software");
      offer.setField("merchant_product_sku", "no_packer_test");
      offer.setField("amount", "10.99");
      offer.setField("quantity", "2");
      offer.setField("packer_code", "portland10");
      request.addOffer(offer);

      offer2.setField("product_name", "Software Product2000");
      offer2.setField("product_code", "electronic_software");
      offer2.setField("merchant_product_sku", "no_packer_test");
      offer2.setField("amount", "109.99");
      offer2.setField("quantity", "2");
      offer2.setField("packer_code", "portland10");
      request.addOffer(offer2);

       */

      ICSClientRequest request = new ICSClientRequest();
      request = Util.convertPropertyFileToICSClientRequest(taxProps);
      System.out.println("-- request --");

      System.out.println(Util.getLoggableICSClientRequest(request));
      System.out.println("-- response --");
      //ICSReply reply = client.send(request);
      ICSReply reply = Util.processRequest(request,cybsProps);
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
