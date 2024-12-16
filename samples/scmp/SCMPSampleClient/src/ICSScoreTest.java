
/**
 *  Copyright 1998, 1999 CyberSource Corporation.  All rights reserved.
 *
 * @author Hubert Chen
 * $Id: ICSScoreTest.java,v 1.4 2001/04/03 19:00:15 darin Exp $
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

public class ICSScoreTest {
    private static final Properties cyberProperties = Util.readPropertyFile("cybs.properties");

  public static void main(String []args) throws ICSException,
    MalformedURLException {
      Properties scoreProps = Util.readPropertyFile("score.properties");

      /*
      ICSClient client = new ICSClient("../properties/ICSClient.props");

      ICSClientOffer offer = new ICSClientOffer();
      ICSClientRequest request = new ICSClientRequest();

      request.setField("ics_applications", "ics_score");
      request.setField("merchant_ref_number", "007");
      //request.setField("merchant_id", client.getMerchantID());
      request.setField("customer_firstname", "Jane");
      request.setField("customer_lastname", "User");
      request.setField("customer_email", "nobody@cybersource.com");
      request.setField("customer_phone", "408-556-9100");
      request.setField("bill_address1", "550 S. Winchester Blvd");
      request.setField("bill_address2", "Suite 1");
      request.setField("bill_city", "San Jose");
      request.setField("bill_state", "CA");
      request.setField("bill_zip", "95128");
      request.setField("bill_country", "USA");
      request.setField("customer_cc_number", "4111111111111111");
      request.setField("customer_cc_expmo", "12");
      request.setField("customer_cc_expyr", "2020");

      offer.setField("product_name", "Bookend");
      offer.setField("product_code", "electronic_software");
      offer.setField("merchant_product_sku", "no_packer_test");
      offer.setField("amount", "10.99");
      offer.setField("quantity", "2");
      offer.setField("packer_code", "portland10");
      request.addOffer(offer);
      */

      ICSClientRequest request = new ICSClientRequest();
      request = Util.convertPropertyFileToICSClientRequest(scoreProps);

      System.out.println("-- request --");

      System.out.println(request);
      System.out.println("-- response --");
     // ICSReply reply = client.send(request);
      ICSReply reply = Util.processRequest(request,cyberProperties);
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
