
/*
 * This code is to test the Secure Random number generation which is sometimes
 * very slow on certain JVM's and architectures. 
 * Sun's JDK 1.1.6 on NT is a known problem
 *
 */
import java.security.SecureRandom;
public class SecureRandomTest {
  public static void main(String args[]) {
    System.out.println("Beginning secure random:  " + new java.util.Date());
    SecureRandom sr = new SecureRandom();
    System.out.println("Second secure random:     " + new java.util.Date());
    SecureRandom sr2 = new SecureRandom();
    System.out.println("Completed secure random:  " + new java.util.Date());
  }
}

