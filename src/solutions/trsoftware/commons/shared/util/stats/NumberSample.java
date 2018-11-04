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

import solutions.trsoftware.commons.shared.util.NumberRange;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Reports statistics for a sample of numbers.  Caution: since all samples
 * must be remembered, this class uses O(N) memory.
 *
 * Implements serializable to support persisting instances to disk
 * using ObjectOutputStream.
 *
 * @author Alex
 */
public class NumberSample<N extends Number & Comparable> implements SampleStatistics<N>, Serializable {

  private static final long serialVersionUID = 1L;


  /**
   * The insertion order of these inputs will not be preserved, as they
   * will be re-sorted on demand whenever the min, max, median, etc. is computed
   */
  private final List<N> samples;

  /**
   * True if one or more new samples have been added since the last time the
   * list was sorted (to avoid potentially costly re-sorting).
   */
  private boolean dirty;

  /**
   * @param backingArray The data structure that will be used to store
   * the data.
   */
  public NumberSample(List<N> backingArray) {
    this.samples = backingArray;
  }

  public NumberSample(int estimatedNumberOfSamples) {
    this(new ArrayList<N>(estimatedNumberOfSamples));
  }

  public NumberSample() {
    this(8192);
  }

  public synchronized void update(N sample) {
    samples.add(sample);
    dirty = true;
  }

  public synchronized void addAll(Collection<N> samplesToBeAdded) {
    samples.addAll(samplesToBeAdded);
    dirty = true;
  }

  public synchronized int size() {
    return samples.size();
  }

  public synchronized N min() {
    return sort().get(0);
  }

  public synchronized N max() {
    return sort().get(size() - 1);
  }

  public synchronized double mean() {
    return sum() / size();
  }

  @Override
  public double sum() {
    double sum = 0;
    for (N sample : samples) {
      sum += sample.doubleValue();
    }
    return sum;
  }

  /** The upper median of the dataset (if there are 2 medians) */
  public synchronized N median() {
    return sort().get(size() / 2);
  }

  /**
   * Sorts the samples in place, since we don't need to preserve the ordering,
   * and don't want to re-sort every time median, mean, etc. is needed.
   * 
   * @return the samples field after it's been sorted (to allow method call chaining)
   */
  private synchronized List<N> sort() {
    if (dirty) {
      Collections.sort(samples);
      dirty = false;
    }
    return samples;
  }

  public synchronized double stdev() {
    return Math.sqrt(variance());
  }

  public synchronized double variance() {
    double mean = mean();
    double sumSquaredDiffs = 0;
    for (N sample : samples) {
      double diff = sample.doubleValue() - mean;
      sumSquaredDiffs += diff * diff;
    }
    return sumSquaredDiffs / size();
  }

  public synchronized N percentile(double pct) {
    if (!new NumberRange<Double>(0d, 1d).contains(pct))
      throw new IllegalArgumentException("Percentage must be in the range 0..1, given: " + pct);
    return sort().get((int)(size() * pct));
  }

  /**
   * Returns all the data points of the given NumberSample, sorted.
   * WARNING: no defensive copy is made prior to returning the data.
   */
  public List<N> getData() {
    return sort();
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    NumberSample that = (NumberSample)o;
    return samples.equals(that.samples);
  }

  public int hashCode() {
    return samples.hashCode();
  }

  /**
   * Returns a summary of the sample, which contains all the statistics without
   * actually storing all the numbers in the sample.  This is convenient
   * when you're done collecting data and wish to release the memory used
   * up by all the numbers.
   */
  @Override
  public ImmutableStats<N> summarize() {
    return new ImmutableStats<N>(size(), min(), max(), median(), sum(), mean(), stdev(), variance());
  }
}