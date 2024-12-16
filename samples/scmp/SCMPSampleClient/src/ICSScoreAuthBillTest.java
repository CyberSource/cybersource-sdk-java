
/**
 *  Copyright 1998, 1999 CyberSource Corporation.  All rights reserved.
 *
 * @author Hubert Chen
 * $Id: ICSScoreAuthBillTest.java,v 1.4 2000/08/30 18:14:32 vshankar Exp $
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

public class ICSScoreAuthBillTest {
    private static final Properties cyberProperties = Util.readPropertyFile("cybs.properties");

  public static void main(String []args) throws ICSException,
    MalformedURLException {

      //ICSClient client = new ICSClient("../properties/ICSClient.props");
      Properties scoreAuthBillProps = Util.readPropertyFile("score_auth_bill.properties");

      //ICSClientOffer offer = new ICSClientOffer();
      ICSClientRequest request = new ICSClientRequest();

      /*
      request.setField("ics_applications", "ics_score,ics_auth,ics_bill");
      request.setField("merchant_ref_number", "007");
      //request.setField("merchant_id", client.getMerchantID());
      request.setField("customer_firstname", "Jane");
      request.setField("customer_lastname", "Smith");
      request.setField("customer_email", "nobody@cybersource.com");
      request.setField("customer_phone", "408-556-9100");
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

      offer.setField("amount", "10.99");
      offer.setField("quantity", "2");
      request.addOffer(offer);
      */

      request = Util.convertPropertyFileToICSClientRequest(scoreAuthBillProps);


      System.out.println("-- request --");

      System.out.println(Util.getLoggableICSClientRequest(request));
      System.out.println("-- response --");
      //ICSReply reply = client.send(request);
      ICSReply reply = Util.processRequest(request, cyberProperties);
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
