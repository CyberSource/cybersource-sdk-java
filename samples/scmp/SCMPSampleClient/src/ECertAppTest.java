import com.cybersource.ics.client.security.ECertApp;

/**
 * This demo shows how to call the high-level ECertApp API. This does
 * the same thing that ECertApp does as a command line application,
 * but without using command line arguments.
 **/
public class ECertAppTest
{
  public static void main(String[] args)
  {
    ECertApp app = new ECertApp();
    app.setMerchantId("Test123");
    app.setOutputDir("/var/tmp");
    app.setPropertiesDir(".");
    //app.setProxyHost("hostname");
    // ...set other properties as necessary
    try {
      app.runMain();
    } catch (com.cybersource.ics.client.security.ECertException e) {
      e.printStackTrace();
    }
  }
}
