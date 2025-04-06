package solutions.trsoftware.commons.shared.util.stats;

import solutions.trsoftware.commons.shared.BaseTestCase;
import solutions.trsoftware.commons.shared.testutil.TestUtils;
import solutions.trsoftware.commons.shared.util.collections.FluentList;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;

import static com.google.common.base.Strings.lenientFormat;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertArraysEqual;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertThrows;
import static solutions.trsoftware.commons.shared.util.StringUtils.methodCallToString;

/**
 * @author Alex
 * @since 10/21/2024
 */
public class BoundedNumberSampleOfDoubleTest extends BaseTestCase {

  public void testEverything() throws Exception {
    for (int capacity = 1; capacity <= 10; capacity++) {
      TestUtils.printSectionHeader(lenientFormat("Testing %s(%s)", BoundedNumberSampleOfDouble.class.getSimpleName(), capacity));
      testEverything(capacity);
    }

    // test some invalid capacity values
    for (int capacity = -2; capacity < 1; capacity++) {
      int finalCapacity = capacity;
      assertThrows(IllegalArgumentException.class, () -> new BoundedNumberSampleOfDouble(finalCapacity));
    }
  }

  private void testEverything(int capacity) throws Exception {
    MockBoundedNumberSampleOfDouble sample = new MockBoundedNumberSampleOfDouble(capacity);
    assertEquals(0, sample.size());
//    assertThrows(IndexOutOfBoundsException.class, () -> sample.get(0));  // TODO: temp - disabled, too verbose
    assertFalse(sample.summarize().isPresent());
    assertFalse(sample.median().isPresent());

    // TODO: test toString, toArray when empty
    System.out.println("sample = " + sample);

    LinkedList<Double> expected = new LinkedList<>();
    for (int i = 0; i < capacity*2; i++) {
      int n = i + 1;
      sample.add(n);
      expected.addLast((double)n);
      assertEquals(Math.min(n, capacity), sample.size());
      assertEquals((double)n, sample.get(Math.min(i, capacity-1)));
//      assertThrows(IndexOutOfBoundsException.class, () -> sample.get(n+1));  // TODO: temp - disabled, too verbose
      Optional<SampleStatisticsDouble> stats = sample.summarize();
      OptionalDouble median = sample.median();
      System.out.println(
          lenientFormat("After add(%s):\n  sample=%s\n    stats=%s\n    median=%s",
          n, sample, stats, median));
      if (i >= capacity) {
        Double evicted = expected.removeFirst();
        //noinspection OptionalGetWithoutIsPresent
        assertEquals(evicted, sample.evicted.getLast().get());
      }
      verifySample(sample, expected);
    }
  }

  private void verifySample(BoundedNumberSampleOfDouble sample, List<Double> values) {
    FluentList<Double> expected = FluentList.from(values);
    int size = expected.size();
    assertEquals(size, sample.size());
    assertEquals(expected, sample.asList());
    for (int i = 0; i < size; i++) {
      assertEquals(expected.get(i), sample.get(i));
      assertEquals(expected.get(i), sample.get(-(size-i)));  // negative index (indicating offset from the end)
    }
    // TODO: test IOOBE for invalid indices
    verifyStats(sample, expected);

    // also test all the subrange methods (toArray, median, summarize, stream)
    assertArraysEqual(toArray(expected), sample.toArray());
    for (int fromIndex = 0; fromIndex < size; fromIndex++) {
      // with fromIndex:
      verifySampleRange(sample, expected, fromIndex);
      verifySampleRange(sample, expected, negateIndex(fromIndex, size));
      // with fromIndex, toIndex:
      for (int toIndex = fromIndex+1; toIndex < size; toIndex++) {
        verifySampleRange(sample, expected, fromIndex, toIndex);
        verifySampleRange(sample, expected, negateIndex(fromIndex, size), toIndex);
        verifySampleRange(sample, expected, fromIndex, negateIndex(toIndex, size));
        verifySampleRange(sample, expected, negateIndex(fromIndex, size), negateIndex(toIndex, size));
      }
      // TODO: also test with negative to/from indices, as well as empty ranges (e.g. summarize(0, 0) or (1, 1), etc.)
    }
  }

  private static int negateIndex(int i, int size) {
    return i - size;
  }

  private void verifySampleRange(BoundedNumberSampleOfDouble sample, FluentList<Double> expected, int fromIndex, int toIndex) {
    FluentList<Double> expectedSublist = expected.subList(fromIndex, toIndex);
    assertArraysEqual(methodCallToString("sample.toArray", fromIndex, toIndex),
        toArray(expectedSublist), sample.toArray(fromIndex, toIndex));
    assertArraysEqual(methodCallToString("sample.stream", fromIndex, toIndex),
        toArray(expectedSublist), sample.stream(fromIndex, toIndex).toArray());
    verifyStats(expectedSublist, () -> sample.summarize(fromIndex, toIndex), () -> sample.median(fromIndex, toIndex));
  }

  private void verifySampleRange(BoundedNumberSampleOfDouble sample, FluentList<Double> expected, int fromIndex) {
    FluentList<Double> expectedSublist = expected.subList(fromIndex);
    assertArraysEqual(methodCallToString("sample.toArray", fromIndex),
        toArray(expectedSublist), sample.toArray(fromIndex));
    assertArraysEqual(methodCallToString("sample.stream", fromIndex),
        toArray(expectedSublist), sample.stream(fromIndex).toArray());
    verifyStats(expectedSublist, () -> sample.summarize(fromIndex), () -> sample.median(fromIndex));
  }

  private static double[] toArray(List<Double> expected) {
    // TODO: maybe extract to a util class
    return expected.stream().mapToDouble(Double::doubleValue).toArray();
  }

  private void verifyStats(BoundedNumberSampleOfDouble sample, FluentList<Double> expected) {
    verifyStats(expected, sample::summarize, sample::median);
  }

  private void verifyStats(FluentList<Double> expected,
                           Supplier<Optional<SampleStatisticsDouble>> statsSupplier,
                           Supplier<OptionalDouble> medianSupplier) {
    Optional<SampleStatisticsDouble> optionalStats = statsSupplier.get();
    OptionalDouble optionalMedian = medianSupplier.get();
    if (expected.size() == 0) {
      assertFalse(optionalStats.isPresent());
      assertFalse(optionalMedian.isPresent());
    }
    else {
      assertTrue(optionalStats.isPresent());
      assertTrue(optionalMedian.isPresent());
      SampleStatisticsDouble stats = optionalStats.get();
      NumberSample<Double> expectedStats = new NumberSample<>(expected);
      assertEquals(expectedStats.size(), stats.size());
      assertEquals(expectedStats.min(), stats.min());
      assertEquals(expectedStats.max(), stats.max());
      assertEquals(expectedStats.sum(), stats.sum());
      assertEquals(expectedStats.mean(), stats.mean());
      assertEquals(expectedStats.stdev(), stats.stdev());
      assertEquals(expectedStats.variance(), stats.variance());
      assertEquals(expectedStats.median(), optionalMedian.getAsDouble());
    }
  }

  public void testTestToArray() throws Exception {
    BoundedNumberSampleOfDouble sample = new BoundedNumberSampleOfDouble(3);
    assertArraysEqual(new double[0], sample.toArray());
    sample.add(1);
    assertArraysEqual(new double[]{ 1d }, sample.toArray());
    // toArray slices:
    assertArraysEqual(new double[0], sample.toArray(0, 0));
    assertArraysEqual(new double[0], sample.toArray(1, 1));
    assertThrows(NegativeArraySizeException.class, () -> sample.toArray(1, 0));
    sample.add(2);
    sample.add(3);
    assertArraysEqual(new double[]{ 1d, 2d, 3d }, sample.toArray());
    // toArray slices:
    assertArraysEqual(new double[0], sample.toArray(0, 0));
    assertArraysEqual(new double[0], sample.toArray(1, 1));
    assertArraysEqual(new double[0], sample.toArray(2, 2));

    assertArraysEqual(new double[]{ 1d, 2d }, sample.toArray(0, 2));
    assertArraysEqual(new double[]{ 1d, 2d }, sample.toArray(-3, -1));
    assertArraysEqual(new double[]{ 3d }, sample.toArray(-1));
    assertArraysEqual(new double[]{ 3d }, sample.toArray(-1, 3));
    assertArraysEqual(new double[]{ 3d }, sample.toArray(2, 3));
    assertArraysEqual(new double[]{ 1d, 2d, 3d }, sample.toArray(-3, 3));
  }

  public void testStream() throws Exception {
    BoundedNumberSampleOfDouble sample = new BoundedNumberSampleOfDouble(3);
    assertArraysEqual(new double[0], sample.stream().toArray());
    sample.add(1);
    assertArraysEqual(new double[]{ 1d }, sample.stream().toArray());
    // slices:
    assertArraysEqual(new double[0], sample.stream(0, 0).toArray());
    assertArraysEqual(new double[0], sample.stream(1, 1).toArray());
    assertArraysEqual(new double[0], sample.stream(1, 0).toArray());  // Note: unlike toArray, stream doesn't throw exception if range size is negative
    sample.add(2);
    sample.add(3);
    assertArraysEqual(new double[]{ 1d, 2d, 3d }, sample.stream().toArray());
    // slices:
    assertArraysEqual(new double[0], sample.stream(0, 0).toArray());
    assertArraysEqual(new double[0], sample.stream(1, 1).toArray());
    assertArraysEqual(new double[0], sample.stream(2, 2).toArray());

    assertArraysEqual(new double[]{ 1d, 2d }, sample.stream(0, 2).toArray());
    assertArraysEqual(new double[]{ 1d, 2d }, sample.stream(-3, -1).toArray());
    assertArraysEqual(new double[]{ 3d }, sample.stream(-1).toArray());
    assertArraysEqual(new double[]{ 3d }, sample.stream(-1, 3).toArray());
    assertArraysEqual(new double[]{ 3d }, sample.stream(2, 3).toArray());
    assertArraysEqual(new double[]{ 1d, 2d, 3d }, sample.stream(-3, 3).toArray());
  }

  private static class MockBoundedNumberSampleOfDouble extends BoundedNumberSampleOfDouble {
    private final FluentList<Double> evicted = new FluentList<>();

    public MockBoundedNumberSampleOfDouble(int capacity) {
      super(capacity);
    }

    @Override
    public void evict(double oldestValue) {
      super.evict(oldestValue);
      evicted.add(oldestValue);
    }
  }
}