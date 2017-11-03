package solutions.trsoftware.commons.server.util;

import org.apache.commons.codec.binary.Hex;

/**
 * Date: Sep 22, 2008 Time: 1:50:50 PM
 *
 * @author Alex
 */
public class ServerArrayUtils {

  /**
   * Fills the given array with array.length new instances of the given class.
   * @return the array
   */
  public static <T> T[] fill(T[] array, Class<T> c) throws IllegalAccessException, InstantiationException {
    for (int i = 0; i < array.length; i++) {
      array[i] = c.newInstance();
    }
    return array;
  }

  public static String toHexString(byte[] bytes, int digitGrouping, String groupDelimiter) {
    if (bytes == null || bytes.length == 0)
      return "";
    char[] hexChars = Hex.encodeHex(bytes);
    StringBuilder buf = new StringBuilder(bytes.length*3);
    for (int i = 0; i < hexChars.length; i++) {
      buf.append(hexChars[i]);
      if (i < hexChars.length-1 && i % digitGrouping == (digitGrouping-1))
        buf.append(groupDelimiter);
    }
    return buf.toString();
  }
}
