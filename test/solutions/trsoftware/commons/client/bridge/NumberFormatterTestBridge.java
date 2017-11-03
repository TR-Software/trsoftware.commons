package solutions.trsoftware.commons.client.bridge;

import solutions.trsoftware.commons.client.bridge.text.AbstractNumberFormatter;
import solutions.trsoftware.commons.client.testutil.AssertUtils;
import junit.framework.TestCase;

public abstract class NumberFormatterTestBridge extends TestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  public <T extends AbstractNumberFormatter> void testCorrectInstanceUsed(Class<T> expectedClass) throws Exception {
    assertEquals(expectedClass, AbstractNumberFormatter.getInstance(0, 0, 1, false, false).getClass());
  }
  
  private void assertFormatterConstructionThrowsException(final int minIntegerDigits, final int minFractionalDigits, final int maxFractionalDigits) {
    boolean[] digitGroupingArgs = {true, false};
    for (final boolean useDigitGrouping : digitGroupingArgs) {
      // make sure that a formatter with invalid args cannot be created
      AssertUtils.assertThrows(IllegalArgumentException.class, new Runnable() {
        public void run() {
          AbstractNumberFormatter.getInstance(minIntegerDigits, minFractionalDigits, maxFractionalDigits, useDigitGrouping, false);
        }
      });
    }
  }

  /**
   * This method should be called when testing both the serverside implementation
   * and the GWT implementation.
   */
  public void testFormattingDeterministically() {
    // 1) make sure that invalid formatting args will throw an exception
    assertFormatterConstructionThrowsException(0, -1, 1);
    assertFormatterConstructionThrowsException(0, 1, -1);
    assertFormatterConstructionThrowsException(0, 1, 0);
    assertFormatterConstructionThrowsException(0, 2, 1);
    assertFormatterConstructionThrowsException(-1, 0, 1);

    // 2) check some numbers manually (to get most of the corner cases covered)

    for (boolean pct : new boolean[]{true, false}) {
      String p = pct ? "%" : "";
      double div = pct ? 100 : 1;  // percentage inputs must be divided by 100 to match the non-percentage outputs
      
      // these two checks should be independent of the digit grouping flag
      for (boolean dg : new boolean[]{true, false}) {
        // digit count checks
        assertEquals(".5" + p, AbstractNumberFormatter.getInstance(0, 1, 1, dg, pct).format(.5/div));  // make sure a leading 0s are printed only if minIntegerDigits > 0
        assertEquals("0.5" + p, AbstractNumberFormatter.getInstance(1, 1, 1, dg, pct).format(.5/div));
        assertEquals("00.5" + p, AbstractNumberFormatter.getInstance(2, 1, 1, dg, pct).format(.5/div));
        assertEquals("000.5" + p, AbstractNumberFormatter.getInstance(3, 1, 1, dg, pct).format(.5/div));

        assertEquals("5" + p, AbstractNumberFormatter.getInstance(1, 0, 0, dg, pct).format(5.0/div));
        assertEquals("5" + p, AbstractNumberFormatter.getInstance(1, 0, 1, dg, pct).format(5.0/div));
        assertEquals("5.1" + p, AbstractNumberFormatter.getInstance(1, 0, 1, dg, pct).format(5.1/div));
        assertEquals("5.0" + p, AbstractNumberFormatter.getInstance(1, 1, 1, dg, pct).format(5.0/div));
        assertEquals("5.10" + p, AbstractNumberFormatter.getInstance(1, 2, 3, dg, pct).format(5.1/div));

        // rounding check
        assertEquals("5" + p, AbstractNumberFormatter.getInstance(1, 0, 0, dg, pct).format(5.4/div));
        assertEquals("5.4" + p, AbstractNumberFormatter.getInstance(1, 0, 1, dg, pct).format(5.4/div));
        assertEquals("5.5" + p, AbstractNumberFormatter.getInstance(1, 0, 1, dg, pct).format(5.46/div));
        assertEquals("5.5" + p, AbstractNumberFormatter.getInstance(1, 0, 1, dg, pct).format(5.45/div));
        assertEquals("6" + p, AbstractNumberFormatter.getInstance(1, 0, 0, dg, pct).format(5.5/div));
        assertEquals("5.50" + p, AbstractNumberFormatter.getInstance(1, 2, 2, dg, pct).format(5.495/div));
      }

      // digit grouping check
      assertEquals("6001" + p, AbstractNumberFormatter.getInstance(1, 0, 0, false, pct).format(6000.9/div));
      assertEquals("6,001" + p, AbstractNumberFormatter.getInstance(1, 0, 0, true, pct).format(6000.9/div));
      assertEquals("123,456,789.5" + p, AbstractNumberFormatter.getInstance(1, 0, 1, true, pct).format(123456789.489123/div));

      // all of the following tests were prompted by actual failure cases
      assertEquals("0,123,456,789.5" + p, AbstractNumberFormatter.getInstance(10, 0, 1, true, pct).format(123456789.489123/div));
      assertEquals("0000" + p, AbstractNumberFormatter.getInstance(4, 0, 1, false, pct).format(0.018058360370651838/div));
      assertEquals("0,000" + p, AbstractNumberFormatter.getInstance(4, 0, 1, true, pct).format(0.018058360370651838/div));
      assertEquals("1" + p, AbstractNumberFormatter.getInstance(0, 0, 1, false, pct).format(0.968475310297362/div));
    }
  }

}