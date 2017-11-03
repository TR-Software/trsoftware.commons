package solutions.trsoftware.commons.client.util.iterators;

import solutions.trsoftware.commons.client.testutil.AssertUtils;
import solutions.trsoftware.commons.client.util.CollectionUtils;
import junit.framework.TestCase;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author Alex, 4/9/2017
 */
public abstract class IteratorTestCase extends TestCase {

  /**
   * Asserts that the given iterator returns all of the elements in the given list.
   */
  public <T> void assertIteratedElements(List<T> elements, final Iterator<T> testIter) {
    assertEquals(elements, CollectionUtils.asList(testIter));
    assertFalse(testIter.hasNext());
    AssertUtils.assertThrows(NoSuchElementException.class, new Runnable() {
      @Override
      public void run() {
        testIter.next();
      }
    });
  }
}
