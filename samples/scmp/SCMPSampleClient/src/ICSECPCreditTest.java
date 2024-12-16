/**
 *  Copyright 2000 CyberSource Corporation.  All rights reserved.
 *
 * @author Vishnu Shankar
 * $Id: clientTest.java,v 1.0
 * 2000/05/01 01:10:34 vshankar Exp $
 *
 */

/**
 * A short simple example program demonstrating ECP credit requests
 * to ICS.
 */

import com.cybersource.ics.base.message.*;
import com.cybersource.ics.base.exception.*;
import com.cybersource.ics.client.message.*;
import com.cybersource.ics.client.*;

import java.util.*;
import java.net.*;


public class ICSECPCreditTest {
    public static final Properties cyberProperties = Util.readPropertyFile("cybs.properties");

  public static void main(String []args) throws ICSException,
    MalformedURLException {
      Properties ecpCreditProps = Util.readPropertyFile("ecp_credit.properties");

      //ICSClient client = new ICSClient("../properties/ICSClient.props");
      //ICSClient client = new ICSClient("ICSClient.props");

      //ICSClientOffer offer = new ICSClientOffer();
      ICSClientRequest request = new ICSClientRequest();

      /*
      // Adding ICS Requests.
      request.setField("ics_applications", "ics_ecp_credit");
      request.setField("merchant_ref_number", "012");
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
      request.setField("ecp_account_no","4100");
      request.setField("ecp_account_type","s");
      request.setField("ecp_rdfi","121042882");
      request.setField("currency", "USD");

      //Add offers to requests.
      offer.setField("amount", "10.99");
      request.addOffer(offer);
*/
      request = Util.convertPropertyFileToICSClientRequest(ecpCreditProps);
      System.out.println("-- request --");

      System.out.println(request);
      ICSReply reply = Util.processRequest(request, cyberProperties);
      System.out.println("-- response --");
      //ICSReply reply =  client.send(request);
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
