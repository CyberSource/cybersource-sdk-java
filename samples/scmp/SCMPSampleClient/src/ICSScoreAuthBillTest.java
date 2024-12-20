
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
import java.util.*;
import java.net.*;

public class ICSScoreAuthBillTest {
    private static final Properties cyberProperties = Util.readPropertyFile("cybs.properties");

  public static void main(String []args) throws ICSException,
    MalformedURLException {
      Properties scoreAuthBillProps = Util.readPropertyFile("score_auth_bill.properties");
      ICSClientRequest request = Util.convertPropertyFileToICSClientRequest(scoreAuthBillProps);
      System.out.println("-- SCMP request --");
      System.out.println(Util.getLoggableICSClientRequest(request));
      ICSReply reply = Util.processRequest(request, cyberProperties);
      System.out.println("-- SCMP response --");
      System.out.println(reply);
      System.out.println("-- end --");
  }
}
