
/**
 *  Copyright 1998, 1999 CyberSource Corporation.  All rights reserved.
 *
 * @author Hubert Chen
 * $Id: ICSAuthBillTest.java,v 1.4 2001/04/03 19:00:15 darin Exp $
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
import java.math.*;

public class ICSAuthBillTest {
    private static final Properties cyberProperties = Util.readPropertyFile("cybs.properties");

  public static void main(String []args) throws ICSException,
    MalformedURLException  {
     // ICSClient client = new ICSClient("../properties/ICSClient.props");
     // ICSClient client = new ICSClient("ICSClient.props");
      Properties saleProps = Util.readPropertyFile("sale.properties");

      //ICSClientOffer offer = new ICSClientOffer();
      ICSClientRequest request = new ICSClientRequest();
/*
      request.setField("ics_applications", "ics_auth,ics_bill");
      request.setField("merchant_ref_number", "007");
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
      request.setField("customer_cc_expyr", "2026");
      request.setField("currency", "USD");

      offer.setField("product_name", "Bookend");
      offer.setField("product_code", "electronic_software");
      offer.setField("merchant_product_sku", "no_packer_test");
      offer.setField("amount", "10.99");
      offer.setField("quantity", "2");
      offer.setField("packer_code", "portland10");
      request.addOffer(offer);
*/

      System.out.println("-- request --");

      request = Util.convertPropertyFileToICSClientRequest(saleProps);

      System.out.println(Util.getLoggableICSClientRequest(request));
      System.out.println("-- response --");
      //ICSReply reply = client.send(request);
      ICSReply reply = Util.processRequest(request, cyberProperties);
      System.out.println(reply);
      System.out.println("-- end --");

      if ( reply.getReplyCode() <= 0 )
      {
        System.out.println("Transaction failed: " + reply.getErrorMessage());
      } else {
        System.out.println("Transaction succeeded");
      }
  }

}
