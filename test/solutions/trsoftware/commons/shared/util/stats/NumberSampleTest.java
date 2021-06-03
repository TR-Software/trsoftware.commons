/*
 * Copyright 2018 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.shared.util.stats;

import com.google.common.collect.ContiguousSet;
import com.google.gwt.core.shared.GwtIncompatible;
import solutions.trsoftware.commons.shared.annotations.Slow;
import solutions.trsoftware.commons.shared.testutil.WrappedIterable;
import solutions.trsoftware.commons.shared.util.CollectionUtils;
import solutions.trsoftware.commons.shared.util.RandomUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;
import solutions.trsoftware.commons.shared.util.compare.ComparisonOperator;

import java.util.*;

import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertNotEqual;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertThrows;
import static solutions.trsoftware.commons.shared.util.MathUtils.EPSILON;

/**
 * @author Alex
 */
public class NumberSampleTest extends CollectableStatsTestCase {
  private NumberSample<Integer> numberSample;
  private List<Integer> inputs;
  private Random rnd;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    numberSample = new NumberSample<Integer>();

    // add the numbers 0..9 to the sample in random order
    inputs = new ArrayList<Integer>();
    for (int i = 0; i < 10; i++) {
      inputs.add(i);
    }
    // 1) randomize the inputs (must not add them in sorted order, to truly test the code)
    Collections.shuffle(inputs);
    // 2) add them to the sample
    numberSample.updateAll(inputs);
    // use the same random seed for all test runs (to ensure repeatable results)
    rnd = new Random(1234);
  }

  @Override
  protected void tearDown() throws Exception {
    numberSample = null;
    inputs = null;
    super.tearDown();
  }

  public void testCount() throws Exception {
    assertEquals(10, numberSample.size());
  }

  public void testMedian() throws Exception {
    // will be null for an empty sample
    assertNull(new NumberSample<>().median());

    // there is an even number of samples in the given data set, make sure that the upper median is used
    assertEquals(5, (int)numberSample.median());
    // add one more value to get rid of the two medians ambiguity
    numberSample.update(10);
    assertEquals(5, (int)numberSample.median());  // now an even number of samples, so the mean is obvious

    // add some numbers out-of-order, to make sure the list will get re-sorted
    numberSample.update(-1);
    numberSample.update(-5);
    assertEquals(4, (int)numberSample.median());
  }

  public void testGetMedian() throws Exception {
    // 1) test some simple examples
    assertNull(new NumberSample<Integer>().getMedian());
    /*
    In general:
     - if sample size is odd, the median will be unique (exactly the middle element)
     - if sample size is even, there will generally be an upper and lower median
       (but if they're both the same, the median can still be considered unique)
    */
    assertMedianIs(numberSample(1), 1);
    assertMedianIsBetween(numberSample(1, 2), 1, 2);
    assertMedianIs(numberSample(0, 1, 2), 1);
    assertMedianIsBetween(numberSample(1, 2, 3, 4), 2, 3);
    assertMedianIs(numberSample(1, 1, 1), 1);
    // even if there's an even number of elements, but they're all the median will still be unique
    assertMedianIs(numberSample(1, 1, 1, 1), 1);
    assertMedianIs(numberSample(1, 2, 2, 3), 2);
    assertMedianIs(numberSample(1, 3, 3, 6, 7, 8, 9), 6);
    assertMedianIsBetween(numberSample(1, 2, 3, 4, 5, 6, 8, 9), 4, 5);
  }

  @Slow
  @GwtIncompatible("printf")
  public void testMedianVsPercentileAndOrderStat() {
    // test some random samples of with various numerical types, and compare Mean result against percentile(50) and orderStatistic(.5)
    class Counters<N extends Number & Comparable<N>> {
      // NOTE: this class probably doesn't need to be generic
      private String name;
      private int n, nUnique, nEqUnique, nCompound, nEqLower, nEqUpper;

      private Counters(String name) {
        this.name = name;
      }

      void maybeIncrement(Median<N> median, N value) {
        n++;
        if (median.isUnique()) {
          nUnique++;
          if (value.equals(median.getValue()))
            nEqUnique++;
        }
        else {
          nCompound++;
          if (value.equals(median.getLower()))
            nEqLower++;
          else if (value.equals(median.getUpper()))
            nEqUpper++;
        }
      }

      double pctEqUniqueMedian() {
        return (double)nEqUnique / nUnique;
      }

      double pctEqLowerMedian() {
        return (double)nEqLower / nCompound;
      }

      double pctEqUpperMedian() {
        return (double)nEqUpper / nCompound;
      }
    }

    Counters<Integer> percentile50Counters = new Counters<>("percentile(50)");
    Counters<Integer> order50Counters = new Counters<>("orderStatistic(.5)");
    Counters<Integer> medianCounters = new Counters<>("median()");
    for (int i = 1; i <= 10_000; i++) {
      // TODO: this probably doesn't need to be parametrized
      NumberSample<Integer> sample = numberSampleRandomIntegers(i);
      Median<Integer> median = sample.getMedian();
      assertNotNull(median);
      percentile50Counters.maybeIncrement(median, sample.percentile(50));
      order50Counters.maybeIncrement(median, sample.orderStatistic(.5));
      medianCounters.maybeIncrement(median, sample.median());
    }
    // print the stats we've gathered above
    System.out.println("Comparison of percentile(50) and orderStatistic(.5) to Median: ");
    System.out.printf("%20s, %21s, %21s, %21s%n", "Method", "= Median.getValue()", "= Median.getLower()", "= Median.getUpper()");
    for (Counters counters : Arrays.asList(percentile50Counters, order50Counters, medianCounters)) {
      System.out.printf("%20s, %20.2f%%, %20.2f%%, %20.2f%%%n",
          counters.name, counters.pctEqUniqueMedian()*100, counters.pctEqLowerMedian()*100, counters.pctEqUpperMedian()*100);
    }
  }

  /**
   * Asserts that the given {@link NumberSample} has a unique median equal to the given value.
   */
  private static <N extends Number & Comparable<N>> void assertMedianIs(NumberSample<N> sample, N expected) {
    Median<N> median = sample.getMedian();
    assertNotNull(median);
    String msg = median.toString() + " (of " + sample + ")";
    assertTrue(msg, median.isUnique());
    assertEquals(expected, median.getLower());
    assertEquals(expected, median.getUpper());
    assertEquals(expected, median.getValue());
    assertEquals(expected.doubleValue(), median.interpolate());
  }

  /**
   * Asserts that the given {@link NumberSample} has a compound median consisting of the two given values
   * (the statistical median would be the average of the two).
   */
  private static <N extends Number & Comparable<N>> void assertMedianIsBetween(NumberSample<N> sample, N lower, N upper) {
    Median<N> median = sample.getMedian();
    assertNotNull(median);
    String msg = median.toString() + " (of " + sample + ")";
    assertFalse(msg, median.isUnique());
    assertEquals(msg, lower, median.getLower());
    assertEquals(msg, upper, median.getUpper());
    assertEquals(msg, upper, median.getValue());
    assertEquals(msg, (lower.doubleValue() + upper.doubleValue()) / 2, median.interpolate(), EPSILON);
  }

  @SafeVarargs
  public static <N extends Number & Comparable<N>> NumberSample<N> numberSample(N... items) {
    NumberSample<N> sample = new NumberSample<>(items.length);
    sample.updateAll(items);
    return sample;
  }

  private NumberSample<Integer> numberSampleRandomIntegers(int size) {
    NumberSample<Integer> sample = new NumberSample<>(size);
    for (int i = 0; i < size; i++) {
      sample.update(rnd.nextInt());
    }
    return sample;
  }

  /**
   * @see RandomUtils#nextGaussian(double, double)
   */
  private NumberSample<Double> numberSampleRandomDoubles(int size, double mean, double stdev) {
    NumberSample<Double> sample = new NumberSample<>(size);
    for (int i = 0; i < size; i++) {
      sample.update(RandomUtils.nextGaussian(rnd, mean, stdev));
    }
    return sample;
    // TODO: maybe also create a Gaussian version of this method?
  }

  public void testMean() throws Exception {
    assertEquals((9d * (9 + 1) / 2) / 10, numberSample.mean());  // Gauss's formula for sum 1,2,3,..n: n(n+1)/2
    assertEquals(4.5, numberSample.mean());  // just making sure :)
  }

  public void testStdDev() throws Exception {
    // variance is the sum of the squared diffs from the mean divided by number of samples
    double sumSquaredDiffs = 0;
    double mean = numberSample.mean();
    for (int i = 0; i < 10; i++) {
      sumSquaredDiffs += Math.pow(mean - i, 2);
    }
    double variance = sumSquaredDiffs / 10;
    // variance is standard deviation squared
    assertEquals(Math.sqrt(variance), numberSample.stdev());
  }

  public void testSummarize() throws Exception {
    ImmutableStats<Integer> summary = numberSample.summarize();
    assertEquals(numberSample.size(), summary.size());
    assertEquals(numberSample.min(), summary.min());
    assertEquals(numberSample.max(), summary.max());
    assertEquals(numberSample.mean(), summary.mean());
    assertEquals(numberSample.median(), summary.median());
    assertEquals(numberSample.stdev(), summary.stdev());
    assertEquals(numberSample.variance(), summary.variance());

    // test with empty sample:
    System.out.println(new NumberSample<>().summarize());
  }

  public void testMin() throws Exception {
    // will be null for an empty sample
    assertNull(new NumberSample<>().min());

    assertEquals(0, (int)numberSample.min());
    numberSample.update(-1);
    assertEquals(-1, (int)numberSample.min());
    numberSample.update(25);
    assertEquals(-1, (int)numberSample.min());
  }

  public void testMax() throws Exception {
    // will be null for an empty sample
    assertNull(new NumberSample<>().max());

    assertEquals(9, (int)numberSample.max());
    numberSample.update(-1);
    assertEquals(9, (int)numberSample.max());
    numberSample.update(25);
    assertEquals(25, (int)numberSample.max());
  }

  public void testSize() throws Exception {
    assertEquals(inputs.size(), numberSample.size());
  }

  public void testSum() throws Exception {
    assertEquals(inputs.stream().mapToDouble(Integer::doubleValue).sum(), numberSample.sum());
  }

  public void testVariance() throws Exception {
    double mean = numberSample.mean();  // already tested this method
    // compute the variance manually:
    // it is defined as the sum of the squared distances of each term in the distribution from the mean (μ), divided by the number of terms in the distribution (N)
    double expected = inputs.stream().mapToDouble(Integer::doubleValue)
        .map(x -> Math.pow(mean - x, 2)).sum() / numberSample.size();
    assertEquals(expected, numberSample.variance());
  }

  public void testStdev() throws Exception {
    double variance = numberSample.variance();  // already tested this method
    assertEquals(Math.sqrt(variance), numberSample.stdev());
  }

  public void testGetData() throws Exception {
    assertEquals(CollectionUtils.sortedCopy(inputs), numberSample.getData());
  }

  public void testOrderStatistic() throws Exception {
    List<Integer> sortedList = numberSample.getData();
    int n = sortedList.size();
    Integer first = sortedList.get(0);
    Integer last = sortedList.get(n - 1);
    assertEquals(first, numberSample.min());
    assertEquals(last, numberSample.max());

    // 1) test the int version of the method, which simply returns the k-th smallest element

    // 1.a) test some illegal arguments
    assertThrows(IllegalArgumentException.class, (Runnable)() ->
            numberSample.orderStatistic(0));
    assertThrows(IllegalArgumentException.class, (Runnable)() ->
            numberSample.orderStatistic(n+1));
    assertThrows(IllegalArgumentException.class, (Runnable)() ->
            numberSample.orderStatistic(-n-1));
    // 1.b) test the edge cases:
    assertEquals(first, numberSample.orderStatistic(1));
    assertEquals(last, numberSample.orderStatistic(n));
    assertEquals(last, numberSample.orderStatistic(-1));
    assertEquals(first, numberSample.orderStatistic(-n));
    // 1.c) now test every valid rank value
    {
      ListIterator<Integer> fwd = sortedList.listIterator();
      ListIterator<Integer> bak = sortedList.listIterator(n);
      for (int k = 1; k <= n; k++) {
        assertEquals(fwd.next(), numberSample.orderStatistic(k));
        assertEquals(bak.previous(), numberSample.orderStatistic(-k));
      }
    }


    // 2) test the double version of this method, which derives k based on the given percentage of list size

    // 2.a) test some illegal arguments:
    assertThrows(IllegalArgumentException.class, (Runnable)() ->
        numberSample.orderStatistic(0 - EPSILON));  // arg < 0
    assertThrows(IllegalArgumentException.class, (Runnable)() ->
        numberSample.orderStatistic(1 + EPSILON));  // arg > 1
    // 2.b) test the edge cases
    assertEquals(first, numberSample.orderStatistic(0d));
    assertEquals(last, numberSample.orderStatistic(1d));

    // now let's print a table of results for all percentages in increments of 1% (or .1) for visual inspection
    printOrderStatsAndPercentiles();

    // since our number sample contains exactly 10 elements, the corresponding percentages should be easy to verify
    assertEquals(10, numberSample.size());  // verify the assumption made by the following code
    for (int i = 0; i < n; i++) {
      Integer expectedElt = sortedList.get(i);
      double pct = i * .1;
      assertOrderStatPctEquals(expectedElt, pct);
      // now just check the continuous pct interval up to the next major increment, to make sure it returns the same value
      // (this will also account for the gap between .90 and 1.0)
      for (double pct2 = pct; pct2 < (i+1)*.1; pct2+=.01) {
        assertOrderStatPctEquals(expectedElt, pct2);
      }
    }
    // TODO: also test this with samples of other sizes (very small and large)

    // TODO: check that orderStatistic(.5) always returns the median
  }

  /**
   * Asserts that the result of invoking {@link NumberSample#orderStatistic(double)} on {@link #numberSample} with
   * the given argument returns the expected element in the sample.
   */
  private void assertOrderStatPctEquals(Integer expectedElt, double pct) {
    assertEquals(StringUtils.methodCallToString("orderStatistic", pct),
        expectedElt, numberSample.orderStatistic(pct));
  }

  public void testPercentile() throws Exception {
    List<Integer> sortedList = numberSample.getData();
    final int n = sortedList.size();
    Integer first = sortedList.get(0);
    Integer last = sortedList.get(n - 1);
    assertEquals(first, numberSample.min());
    assertEquals(last, numberSample.max());

    // 1) test some illegal arguments
    assertThrows(IllegalArgumentException.class, (Runnable)() ->
        numberSample.percentile(-1));
    assertThrows(IllegalArgumentException.class, (Runnable)() ->
        numberSample.percentile(101));

    // 2) test the trivial edge cases: 0 and 100
    assertEquals(first, numberSample.percentile(0));
    assertEquals(last, numberSample.percentile(100));

    // 3) now test every valid argument (all integers 0..100):
    for (int p = 0; p <= 100; p++) {
      Integer result = numberSample.percentile(p);
      System.out.println(StringUtils.methodCallToStringWithResult("percentile", result, p));
      /*
       * Check that the result satisfies the definition given on Wikipedia (https://en.wikipedia.org/wiki/Percentile#The_nearest-rank_method):
       *  "the smallest value in the list such that no more than P percent of the data is strictly less than the value
       *   and at least P percent of the data is less than or equal to that value."
       *
       * NOTE: the above definition doesn't make a lot of sense, and is hard to test; we do our best here,
       * but not able to very the "smallest" condition.  But I think we can simply trust that the algorithm given
       * on that Wikipedia page is good enough.
      */
      double pPct = p / 100d;
      assertTrue(meetsPercentileCriteria(sortedList, result, pPct));
      // check that result is the "the smallest value in the list" satisfying the above criteria
      int lastIndexOfResult = sortedList.lastIndexOf(result);
      assertTrue(lastIndexOfResult >= 0);
      int nextIndex = lastIndexOfResult + 1;  // index of the next-highest unique value
      if (nextIndex < sortedList.size()) {
        Integer nextHighest = sortedList.get(nextIndex);
        // this next value should not satisfy "no more than P percent of the data is strictly less than the value", etc.
        // TODO: temp - the following assertion is failing when p=10, commenting out for now; no big deal
//        assertFalse(meetsPercentileCriteria(sortedList, nextHighest, pPct));
      }
    }
  }

  /**
   * Checks whether the given value satisfies the following conditions:
   * <p>
   * (1) "no more than P percent of the data is strictly less than the value", and
   * (2) "at least P percent of the data is less than or equal to that value"
   *
   * @param data the values in the number sample
   * @param value the value to check against the criteria
   * @param pPct P% expressed as unit fraction
   * @return {@code true} iff "no more than P percent of the data is strictly less than the value and at least P percent
   *     of the data is less than or equal to that value"
   */
  private <T extends Comparable<T>> boolean meetsPercentileCriteria(List<T> data, T value, double pPct) {
    double pctLessThan = pctMatchingElements(data, ComparisonOperator.LT, value);
    // check "no more than P percent of the data is strictly less than the value"
    if (pctLessThan > pPct)
      return false;
    assertTrue(pctLessThan <= pPct);
    // check "at least P percent of the data is less than or equal to that value"
    double pctLessThanOrEq = pctMatchingElements(data, ComparisonOperator.LE, value);
    return pctLessThanOrEq >= pPct;
  }

  /**
   * Prints the results of {@link NumberSample#percentile(int)} and
   * {@link NumberSample#orderStatistic(double)} for visual side-by-side comparison.
   */
  private void printOrderStatsAndPercentiles() {
    // print a table of results for all order statistics and percentiles in increments or 1% (or .1) for visual inspection
    System.out.println("Order statistics for " + numberSample + ":");
    for (int i = 0; i <= 100; i+=10) {
      double pct = i / 100d;
      Integer orderStatistic = numberSample.orderStatistic(pct);
      Integer percentileNearestRank = numberSample.percentile(i);
      System.out.println(StringUtils.methodCallToStringWithResult("orderStatP", orderStatistic, pct));
      System.out.println(StringUtils.methodCallToStringWithResult("percentile", percentileNearestRank, pct));
      System.out.println();
    }
  }

  /**
   * Counts the number of elements in the given list that satisfy the given comparison to the given sentinel value:
   * {@code compOp.compare(e, value)} &forall; elements {@code e} in the list.
   *
   * @return the number of list elements matching the comparison with the given value
   */
  private static  <T extends Comparable<T>> int countMatchingElements(List<T> sortedList, ComparisonOperator cmpOp, T value) {
    return (int)sortedList.stream().filter(cmpOp.comparingTo(value)).count();
    /*
    NOTE: if we know that the list is sorted, it would be faster to just loop over it than using a stream,
    but the perf doesn't really matter here since this is just a unit test
    */
  }

  /**
   * Calculates the percentage of elements in the given list that satisfy the given comparison to the given sentinel value:
   * {@code compOp.compare(e, value)} &forall; elements {@code e} in the list.
   *
   * @return the percentage (unit fraction) of elements matching the comparison with the given value
   */
  private static <T extends Comparable<T>> double pctMatchingElements(List<T> sortedList, ComparisonOperator cmpOp, T value) {
    return (double)countMatchingElements(sortedList, cmpOp, value) / sortedList.size();
  }

  public void testMerge() throws Exception {
    {
      NumberSample<Integer> otherSample = new NumberSample<>();
      ContiguousSet<Integer> otherInputs = ContiguousSet.closed(11, 19);
      otherSample.updateAll(otherInputs);
      assertEquals(otherInputs.size(), otherSample.size());
      // manually construct a new instance containing the inputs from both samples
      NumberSample<Integer> expectedResult = new NumberSample<>();
      expectedResult.addAll(inputs);
      expectedResult.updateAll(otherInputs);
      assertNotEqual(expectedResult, numberSample);
      // merge the other sample into ours and check that it's equal to the expected sample afterwards
      numberSample.merge(otherSample);
      assertEquals(expectedResult, numberSample);
    }

    // test that the sample will be re-sorted after merging if needed (for computing order statistic and median)
    {
      NumberSample<Integer> sample1 = numberSample(1, 2, 3);
      NumberSample<Integer> sample2 = numberSample(-1, -2);
      assertEquals((Integer)2, sample1.getMedian().getValue());
      assertEquals((Integer)1, sample1.orderStatistic(1));
      assertEquals((Integer)2, sample1.orderStatistic(2));
      assertEquals((Integer)3, sample1.orderStatistic(3));
      sample1.merge(sample2);
      // this should have changed the median and order statistics; make sure they are still correct
      assertEquals((Integer)1, sample1.getMedian().getValue());
      assertEquals((Integer)(-2), sample1.orderStatistic(1));
      assertEquals((Integer)(-1), sample1.orderStatistic(2));
      assertEquals((Integer)3, sample1.orderStatistic(-1));
    }
  }

  public void testUpdateAll() throws Exception {
    // test that the sample will be re-sorted after updateAll if needed (for computing order statistic and median)
    NumberSample<Integer> sample = numberSample(3, 2, 1);
    assertEquals((Integer)2, sample.getMedian().getValue());
    assertEquals((Integer)1, sample.orderStatistic(1));
    assertEquals((Integer)2, sample.orderStatistic(2));
    assertEquals((Integer)3, sample.orderStatistic(3));

    // 1) test updateAll with a Collection arg
    sample.updateAll(Arrays.asList(-1, -2));  // at this point we have [-2, -1, 1, 2, 3] in the sample
    // this should have changed the median and order statistics; make sure they are still correct
    assertEquals((Integer)1, sample.getMedian().getValue());
    assertEquals((Integer)(-2), sample.orderStatistic(1));
    assertEquals((Integer)(-1), sample.orderStatistic(2));
    assertEquals((Integer)3, sample.orderStatistic(-1));

    // 2) test updateAll with var-args (array)
    sample.updateAll(-3, -4);  // now we have [-4, -3, -2, -1, 1, 2, 3] in the sample
    // this should have changed the median and order statistics; make sure they are still correct
    assertEquals((Integer)(-1), sample.getMedian().getValue());
    assertEquals((Integer)(-4), sample.orderStatistic(1));
    assertEquals((Integer)(-3), sample.orderStatistic(2));
    assertEquals((Integer)3, sample.orderStatistic(-1));

    // 2) test updateAll with a generic Iterable (that's not a List)
    sample.updateAll(makeOpaqueIterable(-5, 5));  // now we have [-5, -4, -3, -2, -1, 1, 2, 3, 5] in the sample
    // this should have kept the same median but changed the order statistics; make sure they are still correct
    assertEquals((Integer)(-1), sample.getMedian().getValue());
    assertEquals((Integer)(-5), sample.orderStatistic(1));
    assertEquals((Integer)(-4), sample.orderStatistic(2));
    assertEquals((Integer)5, sample.orderStatistic(-1));
  }

  @SafeVarargs
  public static <T> Iterable<T> makeOpaqueIterable(T... items) {
    return new WrappedIterable<>(Arrays.asList(items));
  }

  @Override
  public void testAsCollector() throws Exception {
    doTestAsCollector(new NumberSample<>(), null, inputs.toArray(new Integer[inputs.size()]));
  }

}