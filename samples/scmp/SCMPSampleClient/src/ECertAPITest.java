import com.cybersource.ics.client.security.ECert;
import com.cybersource.ics.client.security.ECertRequest;
import com.cybersource.ics.client.security.ECertReply;
import com.cybersource.ics.client.security.ECertException;
import com.cybersource.ics.client.security.ECertConnectionParameters;
import java.util.Properties;

/**
 * This demo shows how to call the low-level ECert API. This does not
 * write any files. You are responsible for storing the certificate
 * data after successfully completing a cert request.
 **/
public class ECertAPITest
{
  private static boolean update = true;

  public static void main(String[] args)
    throws ECertException
  {
    ECertRequest request = new ECertRequest("dfrankd00");
    System.out.println("Generated request");

    System.out.println("Sending request...");
    //ECertReply reply = send(request);
    ECertReply reply = send2(request);

    System.out.println("Success");

    // You are responsible for saving these byte arrays to files:
    byte[] myPvt = request.getPrivateKey();
    byte[] myCrt = reply.getCertificate();
    byte[] serverCrt = reply.getServerCertificate();
    String requestId = reply.getRequestId();

    // For this demo, we'll print to stdout, just to prove that
    // we got them.
    print("Private Key", myPvt);
    print("Certificate", myCrt);
    print("Server Cert", serverCrt);
  }

  /** send using default connection params **/
  private static ECertReply send(ECertRequest request)
    throws ECertException
  {
    ECert ecert = new ECert();
    ECertConnectionParameters params = new ECertConnectionParameters();
    // If this is not an 'update' request, then you can accept the
    // default connection params values if you don't need http proxy.
    // The icsClientProperties are required if this is an update.  See
    // send2();
    ECertReply reply = ecert.sendCertRequest(request, false, params);
    return reply;
  }

  /** send using custom connection params **/
  private static ECertReply send2(ECertRequest request)
    throws ECertException
  {
    ECert ecert = new ECert();
    ECertConnectionParameters params = new ECertConnectionParameters();
    params.setHost("cbrdeveng5");
    params.setPort("10012");
    //params.setProxyHost("vger");
    //params.setProxyPort("3128");

    // for update, you must provide the ICSClient properties
    Properties props = new Properties();
    // where are the current crt and pvt files stored?
    props.setProperty("ics.keysPath", "./keys"); 
    // you can set any valid ICSClient properties in here
    //props.setProperty("debugLevel", "5");
    //props.setProperty("debugFile", "mydebug.log");
    params.setICSClientProperties(props); // required if doing ecert update

    ECertReply reply = ecert.sendCertRequest(request, update, params);
    return reply;
  }

  private static void print(String name, byte[] data)
  {
    System.out.println(name + ": length=" + data.length + " bytes");
  }

}
