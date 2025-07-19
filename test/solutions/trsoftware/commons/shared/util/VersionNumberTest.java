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

  /**
   * Tests {@link VersionNumber#getMajor()}, {@link VersionNumber#getMinor()}, and {@link VersionNumber#getPatch()}
   */
  public void testComponentGetters() {
    assertComponents(new VersionNumber(), 0, 0, 0);
    assertComponents(new VersionNumber(0), 0, 0, 0);
    assertComponents(new VersionNumber(0, 1), 0, 1, 0);
    assertComponents(new VersionNumber(0, 1, 2), 0, 1, 2);
    assertComponents(new VersionNumber(0, 1, 2, 3), 0, 1, 2);
  }

  private void assertComponents(VersionNumber v, int major, int minor, int patch) {
    assertEquals(major, v.getMajor());
    assertEquals(minor, v.getMinor());
    assertEquals(patch, v.getPatch());
  }

  public void testGetComponent() throws Exception {
    for (int i = 0; i < 10; i++) {
      assertEquals(0, new VersionNumber().getComponent(i));
    }
    assertEquals(1, new VersionNumber(1).getComponent(0));
    assertEquals(2, new VersionNumber(1, 2).getComponent(1));
    assertEquals(3, new VersionNumber(1, 2, 3).getComponent(2));
    // TODO: write a loop to test all possible arg
  }

  public void testIncrement() throws Exception {
    assertEquals(new VersionNumber(1), new VersionNumber().increment());
    assertEquals(new VersionNumber(1), new VersionNumber(0).increment());
    assertEquals(new VersionNumber(0, 2), new VersionNumber(0, 1).increment());
    assertEquals(new VersionNumber(0, 2, 3), new VersionNumber(0, 2, 2).increment());
  }
}