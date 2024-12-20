
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
import java.util.*;
import java.net.*;

public class ICSTaxTest {
    private static final Properties cybsProps = Util.readPropertyFile("cybs.properties");

  public static void main(String []args) throws ICSException,
    MalformedURLException {
      Properties taxProps = Util.readPropertyFile("tax.properties");
      ICSClientRequest request = Util.convertPropertyFileToICSClientRequest(taxProps);
      System.out.println("-- SCMP request --");
      System.out.println(Util.getLoggableICSClientRequest(request));
      ICSReply reply = Util.processRequest(request,cybsProps);
      System.out.println("-- SCMP response --");
      System.out.println(reply);
      System.out.println("-- end --");
  }
}
