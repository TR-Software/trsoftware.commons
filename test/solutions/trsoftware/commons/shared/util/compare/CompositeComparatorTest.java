package solutions.trsoftware.commons.shared.util.compare;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Comparator;

/**
 * @author Alex
 * @since 1/16/2019
 */
public class CompositeComparatorTest extends TestCase {

  public void testCompare() throws Exception {
    // create a composite comparator that compares strings first according to their length, and then based on natural order
    Comparator<String> lengthComparator = Comparator.comparingInt(String::length);
    CompositeComparator<String> cmp = new CompositeComparator<>(Arrays.asList(
        lengthComparator,
        Comparator.naturalOrder()));
    assertTrue(cmp.compare("zx", "abc") < 0);  // the first string is shorter that the second
    assertTrue(cmp.compare("zx", "ab") > 0);  // same length, but the 2nd comes first lexicographically
    assertTrue(cmp.compare("zx", "zx") == 0);  // same length, but the 2nd comes first lexicographically
  }

}