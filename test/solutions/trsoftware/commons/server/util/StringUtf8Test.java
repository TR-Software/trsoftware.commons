package solutions.trsoftware.commons.server.util;

import solutions.trsoftware.commons.Slow;
import solutions.trsoftware.commons.client.util.StringUtils;
import solutions.trsoftware.commons.client.util.callables.Function0;
import solutions.trsoftware.commons.server.testutil.TestUtils;
import junit.framework.TestCase;

/**
 * Oct 21, 2009
 *
 * @author Alex
 */
public class StringUtf8Test extends TestCase {

  public void testConversions() throws Exception {
    checkConversion("Foo bar baz");
    checkConversion("?? ???????!");
  }

  private void checkConversion(String str) {
    assertEquals(str, new StringUtf8(str).toString());
  }

  /** Prints out how much memory and CPU the various parts of the system use in order to tune them */
  @Slow
  public void testPerformance() throws Exception {
    final int nStrings = 1000;
    final int strlen = 100;
    System.out.printf("Testing %d random strings of length %d:%n", nStrings, strlen);
    // check if storing strings as Utf8 byte arrays offers any advantages over Java strings
    TestUtils.printMemoryAndTimeUsage("  String",
        new Function0() {
          public Object call() {
            String[] result = new String[nStrings];
            for (int i = 0; i < result.length; i++)
              result[i] = StringUtils.randString(strlen);
            return result;
          }
        });

    TestUtils.printMemoryAndTimeUsage("  StringUtf8",
        new Function0() {
          public Object call() {
            StringUtf8[] result = new StringUtf8[nStrings];
            for (int i = 0; i < result.length; i++)
              result[i] = new StringUtf8(StringUtils.randString(strlen));
            return result;
          }
        });
  }
}