package solutions.trsoftware.commons.shared.util.text;

import solutions.trsoftware.commons.shared.BaseTestCase;

import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertThrows;

/**
 * @author Alex
 * @since 11/1/2023
 */
public class ArrayCharSequenceTest extends BaseTestCase {

  public void testCharAt() throws Exception {
    String s = "foobar";
    char[] chars = s.toCharArray();
    ArrayCharSequence cs = new ArrayCharSequence(chars);
    assertEquals(s, cs.toString());
    assertEquals(cs, new ArrayCharSequence(chars));
    assertCharSequenceEquals(s, cs);  // tests charAt for each valid index
    assertThrows(IndexOutOfBoundsException.class, () -> cs.charAt(-1));
    assertThrows(IndexOutOfBoundsException.class, () -> cs.charAt(chars.length));
  }

  public void testSubSequence() throws Exception {
    String s = "foobar";
    char[] chars = s.toCharArray();
    ArrayCharSequence cs = new ArrayCharSequence(chars);
    assertCharSequenceEquals("foobar", cs);
    assertCharSequenceEquals("f", cs.subSequence(0, 1));
    assertCharSequenceEquals("o", cs.subSequence(1, 2));
    assertCharSequenceEquals("o", cs.subSequence(2, 3));
    assertCharSequenceEquals("oba", cs.subSequence(2, 5));
    assertCharSequenceEquals("ar", cs.subSequence(4, 6));
    assertCharSequenceEquals("", cs.subSequence(6, 6));
    assertCharSequenceEquals("", cs.subSequence(5, 5));
    assertSame(cs, cs.subSequence(0, cs.length()));  // should return the same instance if identical

    assertThrows(IndexOutOfBoundsException.class, () -> cs.subSequence(-1, 1));
    assertThrows(IndexOutOfBoundsException.class, () -> cs.subSequence(3, 7));
    assertThrows(IndexOutOfBoundsException.class, () -> cs.subSequence(4, 1));
  }

  /**
   * Asserts that the two char sequences have the same length and contain the same chars at corresponding indices.
   */
  public static void assertCharSequenceEquals(CharSequence expected, CharSequence actual) {
    assertEquals(expected.length(), actual.length());
    for (int i = 0; i < expected.length(); i++) {
      assertEquals(expected.charAt(i), actual.charAt(i));
    }
  }
}