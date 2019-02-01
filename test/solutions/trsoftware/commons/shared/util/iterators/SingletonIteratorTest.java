package solutions.trsoftware.commons.shared.util.iterators;

import java.util.Collections;

/**
 * @author Alex
 * @since 1/7/2019
 */
public class SingletonIteratorTest extends IteratorTestCase {

  public void testHasNext() throws Exception {
    assertTrue(new SingletonIterator<>(1).hasNext());
    // test null element handling
    assertTrue(new SingletonIterator<>(null).hasNext());
  }

  public void testNext() throws Exception {
    assertIteratedElements(Collections.singletonList(1), new SingletonIterator<>(1));
    // test null element handling
    assertIteratedElements(Collections.singletonList(null), new SingletonIterator<>(null));
  }
}