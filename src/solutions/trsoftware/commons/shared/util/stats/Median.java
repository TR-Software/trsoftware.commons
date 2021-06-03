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

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * A finite sample of numbers has a unique median element when the sample size is odd, but when the sample size is even,
 * there could be two different "medians": the two values closest to the center. In the latter (ambiguous) case,
 * the median could be interpolated by taking the mean of the two central values, but then the result would not be
 * an actual element from the sample.
 * <p>
 * This class is intended to be used to disambiguate whether the median is {@linkplain Median#isUnique() unique},
 * and if it isn't, to get the {@linkplain Median#getLower() lower}, {@linkplain Median#getUpper() upper},
 * or {@linkplain Median#interpolate() interpolated} median.
 * (If the distinction is unimportant to the application, the simplest usage is to just call {@link #getValue()},
 * which returns the {@linkplain Median#getUpper() upper} median).
 *
 * @see <a href="https://en.wikipedia.org/wiki/Median#Finite_set_of_numbers">Median of a finite set of numbers</a>
 * @see NumberSample#getMedian()
 *
 * @author Alex
 * @since 8/13/2019
 */
public class Median<N extends Number & Comparable<N>> {
  private N lower;
  private N upper;

  /**
   * When the sample size is even, use this constructor to provide the two discrete values closest to the center.
   *
   * @param lower the "lower" median of the sample
   * @param upper the "upper" median of the sample
   */
  public Median(N lower, N upper) {
    this.lower = Objects.requireNonNull(lower);
    this.upper = Objects.requireNonNull(upper);
  }

  /**
   * When the sample size is odd, use this constructor to provide the value in the middle.
   *
   * @param median the actual median of the sample
   */
  public Median(N median) {
    this(median, median);
  }

  /**
   * @return the "lower" median if this instance represents a number sample with an even number of elements.
   * The result could be the same as {@link #getUpper()} if the sample size was odd or if the two central
   * elements are equal.
   * @see Median
   */
  @Nonnull
  public N getLower() {
    return lower;
  }

  /**
   * @return the "upper" median if this instance represents a number sample with an even number of elements.
   * The result could be the same as {@link #getLower()} if the sample size was odd or if the two central
   * elements are equal.
   * @see Median
   */
  @Nonnull
  public N getUpper() {
    return upper;
  }

  /**
   * @return the median value if it's {@link #isUnique() unique}, otherwise the {@link #getUpper() "upper"} median.
   * @see Median
   */
  @Nonnull
  public N getValue() {
    return upper;
  }

  /**
   * Call this method to determine whether there is any ambiguity of the "median" value in the original number sample.
   * This could be true if <i>either</i> the sample has an odd number of values or the two values closest to the
   * center are equal.
   *
   * @return {@code true} iff the {@linkplain Median#getLower() lower} and {@linkplain Median#getUpper() upper} medians
   * {@linkplain Comparable#compareTo(Object) compare} as equal.
   * @see Median
   */
  public boolean isUnique() {
    return lower.compareTo(upper) == 0;
  }

  /**
   * @return the arithmetic mean of {@link #getLower()} and {@link #getUpper()}
   * @see Median
   */
  public double interpolate() {
    return (lower.doubleValue() + upper.doubleValue()) / 2;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Median{");
    sb.append("lower=").append(lower);
    sb.append(", upper=").append(upper);
    sb.append('}');
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    Median<?> median = (Median<?>)o;

    if (!lower.equals(median.lower))
      return false;
    return upper.equals(median.upper);
  }

  @Override
  public int hashCode() {
    int result = lower.hashCode();
    result = 31 * result + upper.hashCode();
    return result;
  }
}
