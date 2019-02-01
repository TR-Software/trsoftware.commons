package solutions.trsoftware.commons.shared.util.compare;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Alex
 * @since 8/10/2018
 */
public class HierarchicalComparatorTest extends TestCase {

  private HierarchicalComparator<Integer> intListComparator;

  public void setUp() throws Exception {
    super.setUp();
    intListComparator = new HierarchicalComparator<Integer>(SortOrder.DESC);
    assertEquals(SortOrder.DESC, intListComparator.getSortOrder());
  }

  public void tearDown() throws Exception {
    intListComparator = null;
    super.tearDown();
  }

  public void testCompare() throws Exception {
    assertEquivalent(null, null);
    assertEquivalent(Collections.emptyList(), null);
    assertEquivalent(null, Collections.emptyList());
    assertEquivalent(new ArrayList<Integer>(), Collections.emptyList());
    assertEquivalent(Arrays.asList(1), Arrays.asList(1));
    assertEquivalent(Arrays.asList(1, 2), Arrays.asList(1, 2));

    assertOrdered(Arrays.asList(1), Arrays.asList());
    assertOrdered(Arrays.asList(1, 3), Arrays.asList(1, 2));
    assertOrdered(Arrays.asList(1, 2, 3), Arrays.asList(1, 2));
    // TODO: what about 1.1.0 vs 1.1 ?
  }

  private void assertEquivalent(List<Integer> a, List<Integer> b) {
    assertEquals(0, cmp(a, b));
    assertEquals(0, cmp(b, a));
  }

  private void assertOrdered(List<Integer> a, List<Integer> b) {
    assertEquals(-1, cmp(a, b));
    assertEquals(1, cmp(b, a));
  }

  private int cmp(List<Integer> a, List<Integer> b) {
    return intListComparator.compare(a, b);
  }
}