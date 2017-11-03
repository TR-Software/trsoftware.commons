package solutions.trsoftware.commons.client.util.iterators;

import java.util.Arrays;
import java.util.List;

public class ResettableCachingIteratorTest extends IteratorTestCase {

  public void testReset() throws Exception {
    // 1) test without null values in the sequence
    doTestReset(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    // 2) test with null values in the sequence
    doTestReset(Arrays.asList(1, 2, 3, null, 5, 6, null, 8, 9, 10));
  }

  /**
   * Asserts that an instance of {@link solutions.trsoftware.commons.client.util.iterators.ResettableIterator} initialized
   * with an iterator over the given list works as expected.
   */
  public <T> void doTestReset(List<T> elements) {

    final ResettableCachingIterator<T> testIter = new ResettableCachingIterator<T>(elements.iterator());
    assertTrue(testIter.hasNext());

    // 1) check that the iterator returns all of the expected elements if it's never reset
    assertIteratedElements(elements, testIter);

    // 2) repeat the above, but this time, checking the reset functionality on each iteration
    for (int i = 0; i < elements.size(); i++) {
      testIter.reset();
      assertIteratedElements(elements, testIter);
    }
  }

}