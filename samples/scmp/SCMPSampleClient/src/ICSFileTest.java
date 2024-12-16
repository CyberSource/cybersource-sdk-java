

/**
 *  Copyright 1998, 1999 CyberSource Corporation.  All rights reserved.
 *
 * @author Hubert Chen
 * $Id: ICSFileTest.java,v 1.5 2001/04/10 19:43:29 darin Exp $
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
import java.io.*;
import java.net.*;

public class ICSFileTest {

  public static void main(String []args){
    try
    {
      InputStream is;
      if ( args.length > 1 ) {
        System.err.println("Usage: java ICSFileTest [file]");
        System.exit(1);
      }

      if ( args.length == 0 ) {
        is = System.in;
      } else {
        is = new BufferedInputStream(new FileInputStream(new File(args[0])));
      }

      ByteArrayOutputStream buf = new ByteArrayOutputStream();
      int b = 0;
      while ((b = is.read()) != -1) {
	buf.write(b);
      }

      ICSClient client = new ICSClient("../properties/ICSClient.props");

      ICSClientRequest request = new ICSClientRequest();
      ICSRequest req = ICSRequest.parseICSRequest(buf.toByteArray());
      request.setAllOffers(req.getAllOffers());
      request.setHashtable(req.getHashtable());
      //request.setField("merchant_id", client.getMerchantID());

      //String server =  client.getDefaultServerURL().toString();
      System.out.println("-- request --");

      System.out.println(request);
      System.out.println("-- response --");
      ICSReply reply = client.send(request);
      System.out.println(reply);
      System.out.println("-- end --");
      if ( reply.getReplyCode() <= 0 )
      {
        System.out.println("Transaction failed: " + reply.getErrorMessage());
        System.exit(-1);
      } else {
        System.out.println("Transaction succeeded");
        System.exit(0);
      }
    } catch ( Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }

}
