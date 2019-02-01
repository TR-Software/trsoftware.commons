package solutions.trsoftware.commons.shared.util;

import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;

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
    public int compareTo(@NotNull RichString o) {
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
    assertTrue(b.greaterThan(a));
    assertFalse(a.greaterThan(b));
    assertFalse(b.greaterThan(b));
    assertFalse(a.greaterThan(a));

    assertTrue(b.greaterThanOrEqualTo(a));
    assertFalse(a.greaterThanOrEqualTo(b));
    assertTrue(b.greaterThanOrEqualTo(b));
    assertTrue(a.greaterThanOrEqualTo(a));

    assertFalse(b.equalTo(a));
    assertFalse(a.equalTo(b));
    assertTrue(b.equalTo(b));
    assertTrue(a.equalTo(a));
    T aCopy = cloneFcn.apply(a);
    assertNotSame(a, aCopy);
    assertTrue(a.equalTo(aCopy));
    T bCopy = cloneFcn.apply(b);
    assertNotSame(b, bCopy);
    assertTrue(b.equalTo(bCopy));

    assertTrue(b.notEqualTo(a));
    assertTrue(a.notEqualTo(b));
    assertFalse(b.notEqualTo(b));
    assertFalse(a.notEqualTo(a));

    assertTrue(a.lessThanOrEqualTo(b));
    assertFalse(b.lessThanOrEqualTo(a));
    assertTrue(b.lessThanOrEqualTo(b));
    assertTrue(a.lessThanOrEqualTo(a));

    assertTrue(a.lessThan(b));
    assertFalse(b.lessThan(a));
    assertFalse(b.lessThan(b));
    assertFalse(a.lessThan(a));
  }

}