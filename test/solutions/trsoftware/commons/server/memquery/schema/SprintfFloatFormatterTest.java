package solutions.trsoftware.commons.server.memquery.schema;

import junit.framework.TestCase;

import static solutions.trsoftware.commons.client.testutil.AssertUtils.assertThrows;
import static solutions.trsoftware.commons.server.memquery.schema.SprintfFloatFormatter.*;


public class SprintfFloatFormatterTest extends TestCase {

  SprintfFloatFormatter formatter;

  public void setUp() throws Exception {
    super.setUp();
    formatter = new SprintfFloatFormatter(false, null, 2, true);
  }

  public void testFormat() throws Exception {
    assertEquals("3.46", formatter.format(3.456d));
    assertEquals("3.46", formatter.format(3.456f));
    assertEquals("3", formatter.format(3.0d));
    assertEquals("3", formatter.format(3.0f));
    assertEquals(Integer.toString(Integer.MAX_VALUE), formatter.format((double)Integer.MAX_VALUE));
    assertEquals(Long.toString(340282L), formatter.format(340282f));
    assertEquals(Integer.toString(Integer.MAX_VALUE) + ".12", formatter.format((double)Integer.MAX_VALUE + .12345));
    assertEquals(Long.toString(340282L) + ".13", formatter.format(340282f + .135f));
    assertEquals("477982", formatter.format(477981.99999999825d));

    System.err.println("Testing arguments that should throw an exception (because they're neither of type Float nor Double):");
    assertThrows(IllegalArgumentException.class, new Runnable() {
      @Override
      public void run() {
        formatter.format(5);
      }
    });

    assertThrows(IllegalArgumentException.class, new Runnable() {
      @Override
      public void run() {
        formatter.format(5L);
      }
    });
  }

  public void testGetFormatSpec() throws Exception {
    assertEquals("%f", getFormatSpec(false, null, null));
    assertEquals("%.2f", getFormatSpec(false, null, 2));
    assertEquals("%,.2f", getFormatSpec(true, null, 2));
    assertEquals("%,5.2f", getFormatSpec(true, 5, 2));
    assertEquals("%,5f", getFormatSpec(true, 5, null));

  }
}