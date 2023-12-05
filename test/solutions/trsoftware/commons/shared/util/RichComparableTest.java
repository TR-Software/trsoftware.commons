package solutions.trsoftware.commons.shared.util;

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.util.compare.RichComparable;

import javax.annotation.Nonnull;
import java.util.function.Function;

/**
 * @author Alex
 * @since 1/11/2019
 */
public class RichComparableTest extends TestCase {

  static class RichString implements RichComparable<RichString> {
    private String value;

    RichString(String string) {
      this.value = string;
    }

    @Override
    public String toString() {
      return value;
    }

    @Override
    public int compareTo(@Nonnull RichString o) {
      return value.compareTo(o.value);
    }

  }

  public void testRichComparisonMethods() throws Exception {
    checkRichComparisons(new RichString("bar"), new RichString("foo"),
        (richString -> new RichString(richString.value)));
  }

  /**
   * Tests all the methods specified by the {@link RichComparable} on the given args.
   *
   * @param a the "lesser" item (as defined by the {@link Comparable}'s <em>total ordering</em>)
   * @param b the "greater" item (as defined by the {@link Comparable}'s <em>total ordering</em>)
   * @param cloneFcn a function that clones a given object of this type
   */
  public static <T extends RichComparable<T>> void checkRichComparisons(T a, T b, Function<T, T> cloneFcn) {
    assertTrue(b.isGreaterThan(a));
    assertFalse(a.isGreaterThan(b));
    assertFalse(b.isGreaterThan(b));
    assertFalse(a.isGreaterThan(a));

    assertTrue(b.isGreaterThanOrEqualTo(a));
    assertFalse(a.isGreaterThanOrEqualTo(b));
    assertTrue(b.isGreaterThanOrEqualTo(b));
    assertTrue(a.isGreaterThanOrEqualTo(a));

    assertFalse(b.isEqualTo(a));
    assertFalse(a.isEqualTo(b));
    assertTrue(b.isEqualTo(b));
    assertTrue(a.isEqualTo(a));
    T aCopy = cloneFcn.apply(a);
    assertNotSame(a, aCopy);
    assertTrue(a.isEqualTo(aCopy));
    T bCopy = cloneFcn.apply(b);
    assertNotSame(b, bCopy);
    assertTrue(b.isEqualTo(bCopy));

    assertTrue(b.isNotEqualTo(a));
    assertTrue(a.isNotEqualTo(b));
    assertFalse(b.isNotEqualTo(b));
    assertFalse(a.isNotEqualTo(a));

    assertTrue(a.isLessThanOrEqualTo(b));
    assertFalse(b.isLessThanOrEqualTo(a));
    assertTrue(b.isLessThanOrEqualTo(b));
    assertTrue(a.isLessThanOrEqualTo(a));

    assertTrue(a.isLessThan(b));
    assertFalse(b.isLessThan(a));
    assertFalse(b.isLessThan(b));
    assertFalse(a.isLessThan(a));
  }

}