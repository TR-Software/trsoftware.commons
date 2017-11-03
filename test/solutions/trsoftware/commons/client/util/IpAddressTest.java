package solutions.trsoftware.commons.client.util;

import junit.framework.TestCase;

import static solutions.trsoftware.commons.client.util.TestUtils.randomInts;

/**
 * @author Alex
 * @since Jun 29, 2013
 */
public class IpAddressTest extends TestCase {

  public void testAllConversions() throws Exception {
    for (int i : randomInts(1000)) {
      IpAddress ip = new IpAddress(i);
      assertEquals(ip, new IpAddress(ip.toString()));
      assertEquals(ip, new IpAddress(ip.toInt()));
      assertEquals(ip, new IpAddress(ip.toLong()));
    }
  }

}