/**
 * Copyright 2003 CyberSource Corporation.  All rights reserved.
 **/
import java.util.Hashtable;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Properties;
import java.util.Iterator;
import java.io.IOException;
import com.cybersource.ics.client.ICSClient;
import com.cybersource.ics.client.message.ICSClientRequest;
import com.cybersource.ics.base.message.ICSRequest;
import com.cybersource.ics.base.exception.ICSException;
import com.cybersource.ics.base.message.ICSReply;
import java.util.List;
import java.util.Vector;

/**
 * Multi-threaded tester program.
 *
 * @author Darin Franklin
 *
 **/
public class MTTest
  implements Runnable
{
  private static int threads = 1;
  private static int iterations = 1;
  private static Properties props = null;
  private static List reqbufs = new Vector();
  private static Iterator reqbufIter = null;
  private ICSClient icsClient = null;
  private static String charenc = null;
  private static boolean verbose = false;

  /**
   * Args: <pre>
   * -i iterations
   * -t threads
   * -p propsfile
   * -enc charset
   * requestfile
   * </pre>
   **/
  public static void main(String[] args)
    throws IOException
  {
    String propsfile = null;
    String reqfile = null;

    for (int i = 0; i < args.length; i++) {
      if ("-t".equals(args[i])) {
        threads = Integer.parseInt(args[++i]);
      } else if ("-i".equals(args[i])) {
        iterations = Integer.parseInt(args[++i]);
      } else if ("-v".equals(args[i])) {
        verbose = true;
      } else if ("-p".equals(args[i])) {
        propsfile = args[++i];
      } else if ("-enc".equals(args[i])) {
        charenc = args[++i];
      } else if (reqfile == null) {
        reqbufs.add(readFile(args[i]));
      }
    }

    if (propsfile != null) {
      props = new Properties();
      props.load(new FileInputStream(propsfile));
    }

    if (reqbufs.size() == 0) {
      System.out.println("you must provide a request file");
      System.exit(1);
    }

    MTTest me = new MTTest();
    ThreadGroup tg = new ThreadGroup("one");
    for (int i = 0; i < threads; i++) {
      new Thread(tg, me).start();
    }
    while (tg.activeCount() > 0) {
      try {
        Thread.sleep(1);
      } catch (InterruptedException e) {
        break;
      }
    }
  }

  private static Request readFile(String filename)
    throws IOException
  {
    Request request = new Request();
    int b = 0;
    ByteArrayOutputStream outbuf = new ByteArrayOutputStream();
    FileInputStream in = new FileInputStream(filename);
    while ((b = in.read()) != -1) {
      outbuf.write(b);
    }
    request.filename = filename;
    request.buf = outbuf.toByteArray();
    return request;
  }

  private synchronized ICSClient getClient()
    throws ICSException
  {
    if (icsClient == null) {
      if (props == null) {
        icsClient = new ICSClient();
      } else {
        icsClient = new ICSClient(props);
      }
    }
    return icsClient;
  }

  /**
   * returns the requests on a rotating iteration
   **/
  private static Request nextReqbuf()
  {
    synchronized (reqbufs) {
      if (reqbufIter == null || ! reqbufIter.hasNext()) {
        reqbufIter = reqbufs.iterator();
      }
      return (Request)reqbufIter.next();
    }
  }

  public void run()
  {
     for (int i = 0; i < iterations; i++) {
        String prefix = Thread.currentThread().getName() + "(" + i + ")";
        try {
           ICSClient client = getClient();
           ICSClientRequest req = new ICSClientRequest();
           Request request = nextReqbuf();
           if (verbose) System.out.println(prefix + ": REQUEST=" + request.filename);
           ICSRequest reqr;
           if (charenc != null) {
              reqr = ICSRequest.parseICSRequest(request.buf, charenc);
           } else {
              reqr = ICSRequest.parseICSRequest(request.buf);
           }
           req.setAllOffers(reqr.getAllOffers());
           req.setHashtable(reqr.getHashtable());
           long start = System.currentTimeMillis();
           ICSReply rep = client.send(req);
           long len = System.currentTimeMillis() - start;
           if (threads > 1) {
              print(prefix, rep);
           } else {
              print(null, rep);
           }
           if (verbose) System.out.println(prefix + ": transaction time: " + len);
        } catch (Exception e) {
           //e.printStackTrace();
           System.err.println(prefix + ": " + e.getMessage());
        }
     }
  }

  private void print(String prefix, ICSReply rep)
  {
    Hashtable ht = rep.getHashtable();
    for (Iterator iter = ht.keySet().iterator(); iter.hasNext(); ) {
      String name = iter.next().toString();
      String value = ht.get(name).toString();
      if (prefix != null) {
        System.out.println(prefix + ": " + name + "=" + value);
      } else {
        System.out.println(name + "=" + value);
      }
    }
  }
}

class Request
{
  public String filename;
  public byte[] buf;
}
