package solutions.trsoftware.commons.server.util;
/**
 *
 * Date: Oct 1, 2008
 * Time: 5:06:47 PM
 * @author Alex
 */

import junit.framework.TestCase;

public class ServerArrayUtilsTest extends TestCase {

  public void testToHexString() throws Exception {
    assertEquals("", ServerArrayUtils.toHexString(null, 2, " "));
    assertEquals("02 03", ServerArrayUtils.toHexString(new byte[]{2, 3}, 2, " "));
    assertEquals("020 304 05", ServerArrayUtils.toHexString(new byte[]{2, 3, 4, 5}, 3, " "));
    assertEquals("0203 0405", ServerArrayUtils.toHexString(new byte[]{2, 3, 4, 5}, 4, " "));
    assertEquals("0203_0405", ServerArrayUtils.toHexString(new byte[]{2, 3, 4, 5}, 4, "_"));
  }

}