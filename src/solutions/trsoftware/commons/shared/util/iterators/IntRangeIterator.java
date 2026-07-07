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

package solutions.trsoftware.commons.shared.util.iterators;

import com.google.common.base.MoreObjects;
import solutions.trsoftware.commons.shared.util.MathUtils;

import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Iterates over a range of {@code int} values, similar to Python's
 * <a href="https://docs.python.org/3/library/stdtypes.html#range">{@code range}</a> function.
 *
 * @author Alex, 6/1/2026
 */
public class IntRangeIterator implements PrimitiveIterator.OfInt {

  /** The starting value (iteration will start with <code>{@link #i} = {@link #start}</code>) */
  protected final int start;
  /**
   * The limiting bound: iteration will stop when
   * {@link #i} &ge; {@link #limit} if {@link #step} is positive or
   * {@link #i} &le; {@link #limit} if {@link #step} is negative
   */
  protected final int limit;
  /** Increment for the next value to be returned */
  protected final int step;
  /** The next value to be returned */
  protected volatile int i;
  // NOTE: volatile i doesn't make this thread-safe, since the i+=step operation is not atomic

  /**
   * Iterate consecutive ints in range {@code [0, limit[}.
   * <p>
   * Stream equivalent:
   * <pre>
   *   {@link IntStream#range}(0, limit)
   * </pre>
   *
   * @param limit upper bound for the range (exclusive)
   */
  public IntRangeIterator(int limit) {
   this(0, limit);
  }

  /**
   * Iterate consecutive ints in range {@code [start, limit[}
   * <p>
   * Stream equivalent:
   * <pre>
   *   {@link IntStream#range}(start, limit)
   * </pre>
   *
   * @param start the first value to be {@linkplain #next() produced}
   * @param limit upper bound for the range (exclusive)
   */
  public IntRangeIterator(int start, int limit) {
    this(start, limit, 1);
  }

  /**
   * Iterate ints between {@code start} (inclusive) and {@code limit} (exclusive)
   * using the {@code step} parameter as the increment.
   * <p>
   * Stream equivalent for Java 8:
   * <pre>
   *   {@link IntStream#iterate}(start, i -> i + step).limit({@link MathUtils#ceilDiv}(limit - start, step))
   * </pre>
   * <p>
   * Stream equivalent for Java 9+ (which allows passing an {@link IntPredicate} as the 2nd arg to {@link IntStream#iterate}):
   * <pre>
   *   // if step &gt; 0:
   *   IntStream.iterate(start, i -> i < limit,  i -> i + step)
   *   // if step &lt; 0:
   *   IntStream.iterate(start, i -> i > limit, i -> i + step)
   * </pre>
   *
   *
   * @param start the first value to be {@linkplain #next() produced}
   * @param limit the limiting bound: iteration will stop when
   *   {@code i} &ge; {@code limit} if {@code step} is positive or
   *   {@code i} &le; {@code limit} if {@code step} is negative
   * @param step increment for consecutive values
   * @throws IllegalArgumentException if {@code step} is {@code 0}
   */
  public IntRangeIterator(int start, int limit, int step) {
    checkArgument(step != 0, "step must not be zero");
    // TODO: maybe check the start, limit bounds? Note: Python's range doesn't enforce these, just returns an empty range
    this.start = start;
    this.limit = limit;
    this.step = step;
    i = start;
  }

  @Override
  public boolean hasNext() {
    return step > 0 ? i < limit : i > limit;
  }

  @Override
  public int nextInt() {
    if (!hasNext())
      throw new NoSuchElementException();  // to comply with the Iterator interface
    int ret = i;
    i += step;
    return ret;
  }

  public int getStart() {
    return start;
  }

  public int getLimit() {
    return limit;
  }

  public int getStep() {
    return step;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("start", start)
        .add("limit", limit)
        .add("step", step)
        .add("next", i)
        .toString();
  }
}
