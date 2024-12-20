/**
 *  Copyright 2000 CyberSource Corporation.  All rights reserved.
 *
 * @author Vishnu Shankar
 * $Id: ICSECPDebitTest.java,v 1.0
 * 2000/05/16 01:20:34 vshankar Exp $
 *
 */

/**
 * A short simple example program demonstrating ECP Debit requests 
 * to ICS.
 */

import com.cybersource.ics.base.message.*;
import com.cybersource.ics.base.exception.*;
import com.cybersource.ics.client.message.*;
import java.util.*;
import java.net.*;


public class ICSECPDebitTest {
    private static final Properties cyberProperties = Util.readPropertyFile("cybs.properties");

    public static void main(String []args) throws ICSException,
    MalformedURLException {
      Properties scoreAuthBillProps = Util.readPropertyFile("ecp_debit.properties");
      ICSClientRequest request = Util.convertPropertyFileToICSClientRequest(scoreAuthBillProps);
      System.out.println("-- SCMP request --");
      System.out.println(Util.getLoggableICSClientRequest(request));
      ICSReply reply = Util.processRequest(request, cyberProperties);
      System.out.println("-- SCMP response --");
      System.out.println(reply);
      System.out.println("-- end --");
  }
}
