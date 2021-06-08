/*
 * Copyright 2021 TR Software Inc.
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
 */

package solutions.trsoftware.commons.shared.util.stats;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import solutions.trsoftware.commons.shared.util.NumberRange;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Reports statistics for a sample of numbers.
 * Caution: since all samples must be remembered, this class uses O(N) memory.
 * For an O(1) memory algorithm, see {@link NumberSampleOnline}.
 * <p>
 * Implements serializable to support persisting instances to disk using ObjectOutputStream.
 * <p>
 * NOTE: this implementation is fully {@code synchronized}.
 * <span style="color: #0073BF; font-weight: bold;">
 *   TODO: might want to also provide an unsynchronized version, for faster performance in single-threaded contexts.
 * </span>
 *
 * @author Alex
 * @see NumberSampleOnline
 * @see NumberSampleOnlineDouble
 * @see com.google.common.math.Stats
 */
public class NumberSample<N extends Number & Comparable<N>> implements SampleStatistics<N>, CollectableStats<N, NumberSample<N>>, Serializable {

  private static final long serialVersionUID = 1L;


  /**
   * The insertion order of these inputs will not be preserved, as they
   * will be re-sorted on demand whenever the min, max, median, etc. is computed
   * <p>
   * NOTE: it's probably not worth amortizing the cost of sorting this list
   * (e.g. by keeping it sorted with a data structure like {@link com.google.common.collect.TreeMultiset}
   * or {@link solutions.trsoftware.commons.shared.util.ListUtils#insertInOrder(List, Comparable)})
   * because the typical use-case here is to collect all the data first and analyze it later (when no new
   * elements would be inserted).
   */
  private final List<N> samples;

  /**
   * True if one or more new samples have been added since the last time the
   * list was sorted (to avoid potentially costly re-sorting).
   */
  private boolean dirty;

  /**
   * Creates a new instance representing the data in the given list.
   * Equivalent to
   * <pre>
   *   NumberSample ns = new NumberSample(data.size());
   *   ns.{@link #addAll}(data);
   * </pre>
   * @param data the existing data
   */
  public NumberSample(List<N> data) {
    this(data.size());
    addAll(data);
  }

  public NumberSample(int estimatedSize) {
    samples = createBackingList(estimatedSize);
    samples.clear();
  }

  public NumberSample() {
    this(1024);
  }

  /**
   * Creates a new {@link ArrayList} that will be used to store the data.
   * Subclasses can override to use a different {@linkplain RandomAccess random access} list implementation.
   * NOTE: when overriding this method, might want to also override {@link #trimToSize()}.
   */
  protected List<N> createBackingList(int initialCapacity) {
    return new ArrayList<>(initialCapacity);
  }

  public synchronized void update(N sample) {
    samples.add(sample);
    dirty = true;
  }


  @Override
  // Overriding the default implementation simply to make it synchronized
  public synchronized void updateAll(Iterable<N> items) {
    if (items instanceof Collection)
      addAll((Collection<N>)items);  // potentially faster than iterating
    else {
      for (N item : items) {
        update(item);
      }
    }
  }

  @SafeVarargs
  @Override
  // Overriding the default implementation simply to make it synchronized
  public final synchronized void updateAll(N... items) {
    for (N item : items) {
      update(item);
    }
  }

  public synchronized void addAll(Collection<N> samplesToBeAdded) {
    dirty |= samples.addAll(samplesToBeAdded);
  }

  /**
   * Can be called after data collection is finished to free up some memory.
   * <p>
   * Delegates to {@link ArrayList#trimToSize()} if the backing list is an {@link ArrayList}
   * (which would be true unless the {@link #createBackingList(int)} method was overridden to provide a different
   * implementation).
   */
  public synchronized void trimToSize() {
    if (samples instanceof ArrayList) {
      ((ArrayList<N>)samples).trimToSize();
    }
  }

  public synchronized int size() {
    return samples.size();
  }

  public synchronized N min() {
    if (size() == 0) return null;
    return sort().get(0);
  }

  public synchronized N max() {
    if (size() == 0) return null;
    return sort().get(size() - 1);
  }

  public synchronized double mean() {
    if (size() == 0) return 0;  // avoid divide-by-zero
    return sum() / size();
  }

  @Override
  public synchronized double sum() {
    double sum = 0;
    // TODO: perhaps ignore entries whose double value is not finite (NaN, Inf, -Inf)
    for (N sample : samples) {
      sum += sample.doubleValue();
    }
    return sum;
  }

  /**
   * Selects a "median" element from the sample, without attempting to interpolate the actual, mathematically-correct
   * median when the sample size is even (in which case the actual median would be the mean of the two numbers
   * closest to the center).
   * <p>
   * When the sample size is odd, the result will be the actual median, otherwise it will be the
   * {@linkplain Median#getUpper() "upper"} median.
   *
   * @deprecated use {@link #getMedian()} instead
   *
   * @return an actual value from the sample that is closest to the median, or {@code null} if the sample is empty.
   * Equivalent to <code>{@link #getMedian()}.{@link Median#getUpper() getUpper()}</code>
   * @see #getMedian()
   * @see <a href="https://en.wikipedia.org/wiki/Median#Finite_set_of_numbers">Median of a finite set of numbers</a>
   */
  public synchronized N median() {
    if (size() == 0) return null;
    return sort().get(size() / 2);
  }

  // TODO: consider renaming the method above to "medianElement" and the method below to just "median"

  /**
   * The sample has a unique median element when the sample size is odd.  If the sample size is even, there could be
   * two different "medians": the two values closest to the center.  Strictly speaking, the median is usually computed by
   * taking the mean of the two central values, but in that case the result will not be an actual element from the sample.
   * <p>
   * This method returns a {@link Median wrapper} that leaves the decision up to the caller.
   *
   * @return a wrapper for the two middle values in the sample, or {@code null} if the sample is empty.
   * The returned object can be used to determine whether the median is {@link Median#isUnique() unique},
   * and if it isn't, to get the {@linkplain Median#getLower() lower}, {@linkplain Median#getUpper() upper},
   * or {@linkplain Median#interpolate() interpolated} median.
   *
   * @see <a href="https://en.wikipedia.org/wiki/Median#Finite_set_of_numbers">Median of a finite set of numbers</a>
   * @see #median()
   * @see Median
   * @see #percentile
   * @see #orderStatistic(int)
   * @see #orderStatistic(double)
   */
  @Nullable
  // TODO: consider replacing the original median() method with this (and pull up to SampleStatistics interface)
  public synchronized Median<N> getMedian() {
    int n = size();
    if (n == 0)
      return null;
    List<N> sortedList = sort();
    int mid = n / 2;
    if (n % 2 == 1) {
      // number of elements is odd, so the median is exactly the middle element
      // (NOTE: midpoint index was computed by integer division, which gives us the exact middle index of the list)
      return new Median<>(sortedList.get(mid));
    }
    else {
      // number of elements is even: the upper median will be at the midpoint index and the lower at the preceding index
      return new Median<>(sortedList.get(mid-1), sortedList.get(mid));
    }
  }

  /**
   * Sorts the samples in place, since we don't need to preserve the encounter order of the values,
   * and don't want to re-sort every time median, percentile, etc. is needed.
   * 
   * @return the {@link #samples} field after it's been sorted (to allow method call chaining)
   */
  @SuppressWarnings("unchecked")
  private synchronized List<N> sort() {
    // optimization: sort only if the list has been modified since the last time it was sorted
    if (dirty) {
      Collections.sort(samples);
      dirty = false;
    }
    return samples;
  }

  public synchronized double variance() {
    if (size() == 0) return 0;  // avoid divide-by-zero
    double mean = mean();
    double sumSquaredDiffs = 0;
    for (N sample : samples) {
      double diff = sample.doubleValue() - mean;
      sumSquaredDiffs += diff * diff;
    }
    return sumSquaredDiffs / size();
  }

  /**
   * Selects the <i>k</i>th smallest value in the sample (a.k.a. the "<i>k</i>th order statistic"),
   * such that k<sub>1</sub> denotes the sample minimum and k<sub>N</sub> the maximum for a sample of size N.
   *
   * Can select the <i>k</i>th biggest value by passing a negative argument, such that k<sub>-1</sub> denotes
   * the sample maximum, k<sub>-2</sub> denotes the "2nd biggest" value, and k<sub>-N</sub> denotes the minimum.
   *
   * @param k the rank ordinal of the desired element (1-indexed).  Positive values in the range 1..N will return
   * the <i>k</i>th smallest value in the sample and negative integer in the range -N..-1 will return the <i>-k</i>th biggest
   * value (similar to using negative list indices in Python).
   * @return the element at the desired index from the sorted list of samples
   * @throws IndexOutOfBoundsException if the index is negative or is not less than {@link #size()}
   * @see #orderStatistic(double)
   * @see #percentile(int)
   * @see #getMedian()
   * @see <a href="https://en.wikipedia.org/wiki/Order_statistic">Order statistics</a>
   * @see <a href="https://en.wikipedia.org/wiki/Selection_algorithm">Selection algorithms</a>
   */
  public synchronized N orderStatistic(int k) {
    int n = size();
    // map k to an index in the sorted list:
    int i;
    if (NumberRange.inRange(1, n, k))
      i = k - 1;
    else if (NumberRange.inRange(-n, -1, k))
      i = n + k;
    else
      throw new IllegalArgumentException(
          Strings.lenientFormat("k (%s) should be a positive or negative ordinal between 1 and %s", k, n));
    return sort().get(i);
  }

  /**
   * Returns the <i>k</i>th smallest value in the sample (a.k.a. the "<i>k</i>th order statistic"), where
   * <i>k</i> is derived based on the given fraction of the total number of values.
   * <p>
   * For example, if the sample contains 10 consecutive integers 0..9, the following results will be returned
   * <pre>
   *   0.0 -> 0
   *   0.1 -> 1
   *   0.2 -> 2
   *   0.3 -> 3
   *   0.4 -> 4
   *   0.5 -> 5
   *   0.6 -> 6
   *   0.7 -> 7
   *   0.8 -> 8
   *   0.9 -> 9
   *   1.0 -> 9
   * </pre>
   * <h3>Relationship with {@link #getMedian() median} and {@link #percentile}:</h3>
   * When {@code kPct = .50}, this method will return the {@linkplain Median#getUpper() upper} median of the sample
   * (unlike {@link #percentile(int) percentile(50)}, which returns the {@linkplain Median#getLower() lower} median).
   *
   * @param kPct a fraction of the total number of elements that are &le; the desired element
   *   (expressed as a unit fraction in the range [0,1])
   * @return the element at index {@code floor(pct * N)} of the sorted list of values.
   *
   *
   * @throws IllegalArgumentException if the argument is not a finite value in the range 0..1
   *
   * @see #orderStatistic(int)
   * @see #percentile(int)
   * @see <a href="https://en.wikipedia.org/wiki/Order_statistic">Order statistics</a>
   * @see <a href="https://en.wikipedia.org/wiki/Selection_algorithm">Selection algorithms</a>
   */
  public synchronized N orderStatistic(double kPct) {
    Preconditions.checkArgument(Double.isFinite(kPct) && NumberRange.inRange(0d, 1d, kPct),
        "Percentage must be a finite value in the range 0..1, given: %s", kPct
    );
    List<N> sortedList = sort();
    int size = sortedList.size();
    int i = (int)Math.floor(size * kPct);
    // special case: if pct = 1, we get i = size, which is out of bounds
    if (i >= size)
      i = size-1;
    return sortedList.get(i);
  }


  /**
   * Uses the <a href="https://en.wikipedia.org/wiki/Percentile#The_nearest-rank_method">nearest-rank method</a> to
   * compute percentile based on the following definition:
   *
   *
   * <blockquote cite="https://en.wikipedia.org/wiki/Percentile#The_nearest-rank_method">
   * One definition of percentile, often given in texts, is that the P-th percentile (0 &lt; P &le; 100) of a list of N
   * ordered values (sorted from least to greatest) is the smallest value in the list such that no more than P percent
   * of the data is strictly less than the value and at least P percent of the data is less than or equal to that value.
   * This is obtained by first calculating the ordinal rank and then taking the value from the ordered list that
   * corresponds to that rank. The ordinal rank n is calculated using this formula:
   * <pre>
   *   n = ceil(P/100 &times; N)
   * </pre>
   * Note the following:
   * <ul>
   *   <li>Using the nearest-rank method on lists with fewer than 100 distinct values can result in the same value being used for more than one percentile.</li>
   *   <li>A percentile calculated using the nearest-rank method will always be a member of the original ordered list.</li>
   *   <li>The 100th percentile is defined to be the largest value in the ordered list.</li>
   * </ul>
   * </blockquote>
   *
   * <h3>Relationship with {@link #getMedian() median} and {@link #orderStatistic}:</h3>
   * When {@code p = 50}, this method will return the {@linkplain Median#getLower() lower} median of the sample
   * (unlike {@link #orderStatistic(double) orderStatistic(.50)}, which returns the {@linkplain Median#getUpper() upper} median).
   *
   * @param p the desired percentile (0..100)
   * @return the smallest element in the sample that is greater than {@code p} percent of the elements.
   * When {@code p = 50} (50th percentile), will return the "lower" median when the number of samples is even
   * (see {@link #getMedian()}).
   *
   * @see #orderStatistic(double)
   * @see <a href="https://en.wikipedia.org/wiki/Percentile">Wikipedia article about percentiles</a>
   * @see <a href="http://onlinestatbook.com/2/introduction/percentiles.html">Other definitions of "percentile"</a>
   */
  /*
      TODO: create a version of this method that performs linear interpolation (like the "Mean" class):
      @see https://en.wikipedia.org/wiki/Percentile#The_linear_interpolation_between_closest_ranks_method
      @see http://onlinestatbook.com/2/introduction/percentiles.html
   */
  public synchronized N percentile(int p) {
    Preconditions.checkArgument(NumberRange.inRange(0, 100, p),
        "Percentile must be an integer in the range 0..100, given: %s", p
    );
    List<N> sortedList = sort();
    int resultIndex;
    if (p > 0) {
      int n = sortedList.size();
      int ordinalRank = (int)Math.ceil(p / 100d * n);
      // NOTE: since the ordinal rank is 1-indexed, we have to subtract 1 to map it to a list index
      resultIndex = ordinalRank - 1;
    } else {
      // 0 is a special case that doesn't work with the above formula:
      // the 0-th percentile is always the first element
      resultIndex = 0;
    }
    return sortedList.get(resultIndex);
  }

  /**
   * @return all the data points in this {@link NumberSample}, sorted in their natural order.
   * <em>CAUTION</em>: no defensive copy is made prior to returning the data.
   */
  public List<N> getData() {
    return sort();
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    NumberSample that = (NumberSample)o;
    // NOTE: we sort the samples prior to performing the comparison because the order is not important
    synchronized (this) {
      return sort().equals(that.sort());
    }
  }

  public int hashCode() {
    // NOTE: we sort the samples prior to getting their hashCode because the order is not important
    return sort().hashCode();
  }

  @Override
  public synchronized String toString() {
    final StringBuilder sb = new StringBuilder("NumberSample{");
    sb.append("samples=").append(samples);
    sb.append('}');
    return sb.toString();
  }

  /**
   * Returns a summary of the sample, which contains all the statistics without
   * actually storing all the numbers in the sample.  This is convenient
   * when you're done collecting data and wish to release the memory used
   * up by all the numbers.
   */
  @Override
  public synchronized ImmutableStats<N> summarize() {
    return new ImmutableStats<N>(size(), min(), max(), median(), sum(), mean(), variance());
  }
  
  @Override
  public java.util.stream.Collector<N, ?, NumberSample<N>> getCollector() {
    return Collector.getInstance();
  }

  @Override
  public synchronized void merge(NumberSample<N> other) {
    addAll(other.samples);
  }

  /**
   * Provides a cached collector descriptor that can be passed to {@link Stream#collect}
   * to collect the stream elements into an instance of {@link NumberSample}.
   *
   * @param <N> the input element type
   * @see #getInstance()
   */
  public static class Collector<N extends Number & Comparable<N>> extends CollectableStats.Collector<N, NumberSample<N>> {

    /**
     * NOTE: static fields are automatically lazy-init for singletons and safer to use than double-checked locking.
     * @see <a href="https://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html">Why double-checked locking is broken</a>
     * @see <a href="https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom">Initialization-on-demand holder idiom</a>
     */
    private static final Collector INSTANCE = new Collector();

    /**
     * @param <N> the input element type
     * @return the cached instance of this {@link Collector}
     */
    @SuppressWarnings("unchecked")
    public static <N extends Number & Comparable<N>> Collector<N> getInstance() {
      return INSTANCE;
    }

    @Override
    public Supplier<NumberSample<N>> supplier() {
      return NumberSample::new;
    }

    /**
     * Since all the methods in {@link NumberSample} are synchronized, we can include the
     * {@link java.util.stream.Collector.Characteristics#CONCURRENT CONCURRENT} characteristic.
     * @see #CH_CONCURRENT_ID
     */
    @Override
    public Set<Characteristics> characteristics() {
      return CH_CONCURRENT_ID;
    }
  }


}