package solutions.trsoftware.commons.client.useragent;

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.testutil.AssertUtils;

import static solutions.trsoftware.commons.client.useragent.VersionNumber.parse;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.*;

/**
 * @author Alex
 * @since 8/10/2018
 */
public class VersionNumberTest extends TestCase {

  public void testCompareTo() throws Exception {
    assertComparablesOrdering(
        new VersionNumber(0),
        new VersionNumber(0, 1),
        new VersionNumber(1),
        new VersionNumber(1, 0, 1),
        new VersionNumber(1, 1, 0),
        new VersionNumber(1, 2, 0),
        new VersionNumber(1, 2, 0, 3, 4)
    );
  }

  public void testEqualsAndHashCode() throws Exception {
    assertEqual(new VersionNumber(), new VersionNumber());
    assertEqual(new VersionNumber(), new VersionNumber(0));
    assertEqual(new VersionNumber(1, 2), new VersionNumber(1, 2));
    assertEqual(new VersionNumber(1, 2), new VersionNumber(1, 2, 0));
    assertEqual(new VersionNumber(1, 2), new VersionNumber(1, 2, 0, 0));
    assertNotEqual(new VersionNumber(1, 2), new VersionNumber(1));
    assertNotEqual(new VersionNumber(1), new VersionNumber());
  }

  @SuppressWarnings("unchecked")
  private static void assertEqual(VersionNumber a, VersionNumber b) {
    assertEqualsAndHashCode(a, b);
    assertComparablesEqual(a, b);
  }

  @SuppressWarnings("unchecked")
  private static void assertNotEqual(VersionNumber a, VersionNumber b) {
    AssertUtils.assertNotEqual(a, b);
    assertComparablesNotEqual(a, b);
  }

  public void testToString() throws Exception {
    assertEquals("", new VersionNumber().toString());
    assertEquals("1", new VersionNumber(1).toString());
    assertEquals("1.23.456", new VersionNumber(1, 23, 456).toString());
  }

  public void testParseIntVersion() throws Exception {
    assertThrows(NullPointerException.class, new Runnable() {
      @Override
      public void run() {
        parse(null);
      }
    });
    assertEquals(new VersionNumber(), parse(""));
    assertEquals(new VersionNumber(1), parse("1"));
    assertEquals(new VersionNumber(1, 23, 456), parse("1.23.456"));
    assertEquals(new VersionNumber(1, 23, 456), parse(" 1.23.456  "));
  }
}