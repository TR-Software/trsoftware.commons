package solutions.trsoftware.commons.client.util.iterators;

import solutions.trsoftware.commons.client.util.Predicate;
import solutions.trsoftware.commons.client.util.mutable.MutableInteger;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class PredicatedIteratorTest extends IteratorTestCase {

  public void testPredicatedIterator() throws Exception {
    List<Integer> inputList = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    assertIteratedElements(Arrays.asList(1,3,5,7,9), new PredicatedIterator<Integer>(inputList.iterator(), new Predicate<Integer>() {
      @Override
      public boolean apply(@Nullable Integer x) {
        assert x != null;
        return x % 2 == 1;  // match only odd numbers
      }
    }));
  }


  public void testFilteringWithNullElements() throws Exception {
    // the 1-arg constructor excludes null input elements by default
    final List<Integer> inputList = Arrays.asList(null, 2, null, 4, 5, 6, 7, 8, 9, null);
    assertIteratedElements(Arrays.asList(5, 7, 9),
        new PredicatedIterator<Integer>(inputList.iterator(), new Predicate<Integer>() {
          @Override
          public boolean apply(@Nullable Integer x) {
            assert x != null;
            return x % 2 == 1;  // match only odd numbers
          }
        })
    );
    // using the the 2-arg constructor we can configure it to include null input elements
    final MutableInteger nullsSeen = new MutableInteger();
    assertIteratedElements(Arrays.asList(5, 7, 9),
        new PredicatedIterator<Integer>(true, inputList.iterator(), new Predicate<Integer>() {
          @Override
          public boolean apply(@Nullable Integer x) {
            if (x == null) {
              nullsSeen.increment();
              return false;
            }
            return x % 2 == 1;  // match only odd numbers
          }
        })
    );
    assertEquals(3, nullsSeen.get());
  }
}