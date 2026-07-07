package solutions.trsoftware.commons.shared.util.iterators;

import solutions.trsoftware.commons.shared.BaseTestCase;
import solutions.trsoftware.commons.shared.testutil.AssertUtils;
import solutions.trsoftware.commons.shared.util.MathUtils;

import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.stream.IntStream;

/**
 * @author Alex
 * @since 6/1/2026
 */
public class IntRangeIteratorTest extends BaseTestCase {

  public void testNextInt() throws Exception {
    verifyExpectedSequence(new IntRangeIterator(0));  // empty range
    verifyExpectedSequence(new IntRangeIterator(1), 0);  // singleton range
    verifyExpectedSequence(new IntRangeIterator(2), 0, 1);
    verifyExpectedSequence(new IntRangeIterator(3), 0, 1, 2);
    verifyExpectedSequence(new IntRangeIterator(10, 13), 10, 11, 12);
    // with positive step:
    verifyExpectedSequence(new IntRangeIterator(10, 19, 3), 10, 13, 16);
    verifyExpectedSequence(new IntRangeIterator(10, 20, 3), 10, 13, 16, 19);
    verifyExpectedSequence(new IntRangeIterator(10, 21, 3), 10, 13, 16, 19);
    // with negative step:
    verifyExpectedSequence(new IntRangeIterator(1, -10, -2), 1, -1, -3, -5, -7, -9);
    verifyExpectedSequence(new IntRangeIterator(1, -11, -2), 1, -1, -3, -5, -7, -9);
    verifyExpectedSequence(new IntRangeIterator(1, -2, -2), 1, -1);
    verifyExpectedSequence(new IntRangeIterator(1, -1, -2), 1);  // singleton range
    verifyExpectedSequence(new IntRangeIterator(1, 1, -2));  // empty range

    // test some invalid start/limit combinations (resulting in empty ranges)
    verifyExpectedSequence(new IntRangeIterator(-1));
    verifyExpectedSequence(new IntRangeIterator(1, 0));
    verifyExpectedSequence(new IntRangeIterator(1, 0, 3));
    verifyExpectedSequence(new IntRangeIterator(-1, 0, -3));
  }

  public static void verifyExpectedSequence(IntRangeIterator it, int... expected) {
    verifyExpectedSequence((PrimitiveIterator.OfInt)it, expected);
    // TODO: temp experiment: compare to equivalent IntStream:
    int start = it.getStart();
    int limit = it.getLimit();
    int step = it.getStep();
    IntStream stream;
    if (step == 1) {
      stream = IntStream.range(start, limit);
    } else {
      stream = IntStream.iterate(start, i -> i + step).limit(MathUtils.ceilDiv(limit - start, step));
    }
    AssertUtils.assertArraysEqual(expected, stream.toArray());
  }

  public static void verifyExpectedSequence(PrimitiveIterator.OfInt it, int... expected) {
    for (int i : expected) {
      assertTrue(it.hasNext());
      assertEquals(i, it.nextInt());
    }
    assertFalse(it.hasNext());
    AssertUtils.assertThrows(NoSuchElementException.class, it::nextInt);  // TODO: make this less verbose (AssUtils always prints stack trace)
  }
}