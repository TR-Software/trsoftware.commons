package solutions.trsoftware.commons.shared.util.iterators;

import solutions.trsoftware.commons.shared.BaseTestCase;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Collections.emptyList;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertSameSequence;
import static solutions.trsoftware.commons.shared.util.ListUtils.concat;

/**
 * @author Alex
 * @since 4/24/2023
 */
public class ConcatenatedIteratorTest extends BaseTestCase {

  public void testIteration() throws Exception {
    List<Integer> list1 = IntStream.range(1, 5).boxed().collect(Collectors.toList());
    List<Integer> list2 = IntStream.range(5, 7).boxed().collect(Collectors.toList());
    
    // general case: each input iterator contains multiple elements 
    assertSameSequence(concat(list1, list2).iterator(), ConcatenatedIterator.of(list1, list2));
    
    // special case: one of the iterators is empty
    assertSameSequence(list2.iterator(), 
        ConcatenatedIterator.of(emptyList(), list2));
    assertSameSequence(list1.iterator(),
        ConcatenatedIterator.of(list1, emptyList()));

    // special case: both iterators empty
    assertSameSequence(Collections.emptyIterator(),
        ConcatenatedIterator.of(emptyList(), emptyList()));
  }

}