
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
      ICSClientRequest request = Util.convertPropertyFileToICSClientRequest(exportProps);
      System.out.println("-- SCMP request --");
      System.out.println(Util.getLoggableICSClientRequest(request));
      ICSReply reply = Util.processRequest(request, cybsProperties);
      System.out.println("-- SCMP response --");
      System.out.println(reply);
      System.out.println("-- end --");
  }
}
