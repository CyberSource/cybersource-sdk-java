
/**
 *  Copyright 1998, 1999 CyberSource Corporation.  All rights reserved.
 *
 * @author Hubert Chen
 * $Id: ICSExportTest.java,v 1.5 2000/08/22 05:52:04 vshankar Exp $
 * 
 */

/**
 * A short, simple example program for sending requests to ICS.
 */

import com.cybersource.ics.base.message.*;
import com.cybersource.ics.base.exception.*;
import com.cybersource.ics.client.message.*;

import java.util.*;
import java.net.*;

public class ICSExportTest {
    public static final Properties cybsProperties = Util.readPropertyFile("cybs.properties");

  public static void main(String []args) throws ICSException,
    MalformedURLException {
      Properties exportProps = Util.readPropertyFile("export.properties");

      ICSClientRequest request = new ICSClientRequest();
      /*
      ICSClient client = new ICSClient("../properties/ICSClient.props");


      ICSClientOffer offer = new ICSClientOffer();
      ICSClientRequest request = new ICSClientRequest();

      request.setField("ics_applications", "ics_export");
      request.setField("merchant_ref_number", "007");
      //request.setField("merchant_id", client.getMerchantID());
      request.setField("customer_firstname", "Joe");
      request.setField("customer_lastname", "Smith");
      request.setField("customer_email", "nobody@cybersource.com");
      request.setField("customer_phone", "800.555.1212");
      request.setField("customer_ipaddress", "10.2.5.18");
      request.setField("bill_address1", "2134 N. 7th");
      request.setField("bill_address2", "Apt. 42");
      request.setField("bill_city", "San Jose");
      request.setField("bill_state", "CA");
      request.setField("bill_zip", "95128");
      request.setField("bill_country", "USA");
      request.setField("http_browser_type", "win32");
      request.setField("platform", "win32");

      offer.setField("product_name", "Virus Scan");
      offer.setField("product_code", "electronic_software");
      offer.setField("merchant_product_sku", "no_packer_test");
      offer.setField("amount", "10.99");
      offer.setField("quantity", "1");
      offer.setField("packer_code", "rover10");
      request.addOffer(offer);

*/
      request = Util.convertPropertyFileToICSClientRequest(exportProps);
      System.out.println("-- request --");

      System.out.println(Util.getLoggableICSClientRequest(request));
      System.out.println("-- response --");
      //ICSReply reply = client.send(request);
      ICSReply reply = Util.processRequest(request, cybsProperties);
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
