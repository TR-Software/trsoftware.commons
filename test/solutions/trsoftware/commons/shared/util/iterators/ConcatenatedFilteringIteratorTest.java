package solutions.trsoftware.commons.shared.util.iterators;

import solutions.trsoftware.commons.shared.BaseTestCase;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertSameSequence;

/**
 * @author Alex
 * @since 4/24/2023
 */
public class ConcatenatedFilteringIteratorTest extends BaseTestCase {

  public void testIteration() throws Exception {
    List<Integer> list1 = asList(1, 2, 3, 4);
    List<Integer> list2 = asList(5, 6, 7, 8);
    
    // general case: each input iterator contains multiple elements 
    assertSameSequence(asList(2, 4, 6, 8).iterator(),
        ConcatenatedFilteringIterator.of(list1, list2, i -> i % 2 == 0));  // even numbers
    
    // special case: one of the iterators is empty or contains no matching elements
    assertSameSequence(asList(6, 8).iterator(),
        ConcatenatedFilteringIterator.of(emptyList(), list2, i -> i % 2 == 0));  // even numbers
    assertSameSequence(asList(6, 8).iterator(),
        ConcatenatedFilteringIterator.of(singletonList(1), list2, i -> i % 2 == 0));  // even numbers

    assertSameSequence(asList(1, 3).iterator(),
        ConcatenatedFilteringIterator.of(list1, emptyList(), i -> i % 2 == 1));  // odd numbers
    assertSameSequence(asList(1, 3).iterator(),
        ConcatenatedFilteringIterator.of(list1, asList(4, 6, 8), i -> i % 2 == 1));  // odd numbers

    // special case: both iterators empty or contain no matching elements
    assertSameSequence(Collections.emptyIterator(),
        ConcatenatedFilteringIterator.of(emptyList(), emptyList(), i -> true));  // empty lists
    assertSameSequence(Collections.emptyIterator(),
        ConcatenatedFilteringIterator.of(list1, list2, i -> false));  // none match
  }

}