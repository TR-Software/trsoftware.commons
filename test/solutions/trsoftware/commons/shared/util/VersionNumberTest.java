package solutions.trsoftware.commons.shared.util;

import junit.framework.TestCase;

import static solutions.trsoftware.commons.shared.testutil.AssertUtils.*;
import static solutions.trsoftware.commons.shared.util.RichComparableTest.checkRichComparisons;
import static solutions.trsoftware.commons.shared.util.VersionNumber.parse;

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
    assertEqualsAndHashCode(new VersionNumber(), new VersionNumber());
    assertEqualsAndHashCode(new VersionNumber(), new VersionNumber(0));
    assertEqualsAndHashCode(new VersionNumber(1, 2), new VersionNumber(1, 2));
    assertEqualsAndHashCode(new VersionNumber(1, 2), new VersionNumber(1, 2, 0));
    assertEqualsAndHashCode(new VersionNumber(1, 2), new VersionNumber(1, 2, 0, 0));
    assertNotEqual(new VersionNumber(1, 2), new VersionNumber(1));
    assertNotEqual(new VersionNumber(1), new VersionNumber());
  }

  public void testToString() throws Exception {
    assertEquals("", new VersionNumber().toString());
    assertEquals("1", new VersionNumber(1).toString());
    assertEquals("1.0.0", new VersionNumber(1, 0, 0).toString());
    assertEquals("1.23.456", new VersionNumber(1, 23, 456).toString());
  }

  public void testParseIntVersion() throws Exception {
    assertThrows(NullPointerException.class, (Runnable)() -> parse(null));
    assertEquals(new VersionNumber(), parse(""));
    assertEquals(new VersionNumber(1), parse("1"));
    assertEquals(new VersionNumber(1, 23, 456), parse("1.23.456"));
    assertEquals(new VersionNumber(1, 23, 456), parse(" 1.23.456  "));
  }

  public void testRichComparisonMethods() throws Exception {
    checkRichComparisons(new VersionNumber(1, 2, 456), new VersionNumber(1, 23, 456),
        versionNumber -> VersionNumber.parse(versionNumber.toString()));
  }

}