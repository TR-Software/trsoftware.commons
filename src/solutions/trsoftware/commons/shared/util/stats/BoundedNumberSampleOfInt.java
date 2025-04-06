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

import com.google.common.primitives.Ints;

import java.util.*;
import java.util.stream.IntStream;

import static com.google.common.base.Strings.lenientFormat;
import static solutions.trsoftware.commons.shared.util.ListUtils.normalizeIndex;
import static solutions.trsoftware.commons.shared.util.ListUtils.normalizePositionIndex;

/**
 * Represents a fixed-size number sample of {@code int} values, and computes {@linkplain #summarize() statistics}
 * and {@linkplain #median() median} for the contained numbers on-demand.
 * Adding a new value when the buffer is full evicts out the oldest value.
 *
 * @author Alex, 10/26/2024
 * @see BoundedNumberSampleOfDouble
 */
public class BoundedNumberSampleOfInt {

  /* Note: this class is based on the old CyclicFloatBuffer */

  /** Fixed length of the {@link #buffer} array */
  private final int capacity;
  private final int[] buffer;
  /**
   * The index within {@link #buffer} where the first (oldest) element resides:
   * {@code 0} until buffer capacity exceeded, then will be incremented by 1 (mod {@link #capacity})
   * when adding a new element.
   */
  private int cursor;
  /** The number of values currently held by this buffer (between 0 and {@link #capacity}) */
  private int size;

  /**
   * Creates a new instance backed by an {@code int[]} array of the given length.
   *
   * @param capacity the maximum number of values that can be held by this buffer;
   * {@linkplain #add(int) adding} a new value after {@link #size()} exceeds this capacity will
   * {@linkplain #evict(int) evict} the eldest value
   */
  public BoundedNumberSampleOfInt(int capacity) {
    if (capacity <= 0) {
      throw new IllegalArgumentException(lenientFormat("%s capacity must be > 0 (given %s)",
          getClass().getSimpleName(), capacity));
    }
    this.capacity = capacity;
    buffer = new int[capacity];
  }

  /**
   * Copy constructor.
   * @param other instance to be copied
   */
  public BoundedNumberSampleOfInt(BoundedNumberSampleOfInt other) {
    capacity = other.capacity;
    buffer = Arrays.copyOf(other.buffer, other.buffer.length);
    cursor = other.cursor;
    size = other.size;
  }

  /**
   * @return the maximum number of values that can be held by this buffer;
   * {@linkplain #add(int) adding} a new value after {@link #size()} exceeds this capacity will
   * {@linkplain #evict(int) evict} the eldest value
   */
  public int getCapacity() {
    return capacity;
  }

  /**
   * Adds a new value to the buffer, {@linkplain #evict(int) evicting} the eldest value if buffer capacity exceeded.
   * @param x the value to insert
   */
  public void add(int x) {
    if (size == capacity) {
      // evict the oldest value by advancing the wrap-around cursor
      evict(buffer[cursor]);
      cursor = ordinalToIndex(1);
    } else {
      size++;
    }
    // insert the new value
    buffer[ordinalToIndex(size - 1)] = x;
  }

  public void evict(int oldestValue) {
    // subclasses may override (e.g. to promote to a larger buffer)
  }

  /**
   * Returns the <i>i</i>-th element, s.t. the element at {@code index 0} is the eldest and {@link #size}{@code -1}
   * is the most-recently added. The {@code index} can also be negative, which indicates an offset from the end,
   * i.e. {@code -1} is the most recent and {@code -size} is the eldest.
   * @param index the element index, between {@code -size} (inclusive) and {@code size} (exclusive)
   * @return the value at the given index
   * @throws IndexOutOfBoundsException if {@code index} is less than {@code -size} or not less than {@code size}
   */
  public int get(int index) {
    index = normalizeIndex(index, size);
    return buffer[ordinalToIndex(index)];
  }

  private int ordinalToIndex(int ordinal) {
    /* TODO: might want to replace the slow modulo op with something faster:
        - if capacity is power of 2, could use bitwise & ((cursor+ordinal) & (capacity-1))
        - o/w *maybe* could replace the modulo with an if-else expression, similar to java.lang.ThreadLocal.ThreadLocalMap.nextIndex,
            e.g. (cursor+ordinal) < capacity ? (cursor+ordinal) : (cursor+ordinal) - capacity; // Note: this expression doesn't actually work
     */
    return (cursor+ordinal) % capacity;
  }

  /**
   * @return the number of values currently held by this buffer (between 0 and {@link #capacity})
   */
  public int size() {
    return size;
  }

  /**
   * Computes statistics (mean, min, max, variance, etc.) for the values currently held in this buffer.
   *
   * @return an {@link Optional} containing the statistics or an empty {@link Optional} if the buffer is empty
   */
  public Optional<SampleStatisticsInt> summarize() {
    return summarize(0, size);
  }

  /**
   * Computes statistics (mean, min, max, variance, etc.) for the subrange of values currently held in this buffer
   * starting with the given {@linkplain #get(int) index}.
   *
   * @param fromIndex index of the first element in the desired subrange: either
   *   an int between 0 and {@link #size} (inclusive),
   *   or a negative offset from the end (between {@code -1} and {@code -size}, inclusive)
   * @return an {@link Optional} containing the statistics or an empty {@link Optional} if the buffer is empty
   *   or the given index is equal to {@link #size()}
   * @throws IndexOutOfBoundsException if the index is less than {@code -size} or greater than {@code size}
   */
  public Optional<SampleStatisticsInt> summarize(int fromIndex) {
    return summarize(fromIndex, size);
  }

  /**
   * Computes statistics (mean, min, max, variance, etc.) for the subrange of values currently held in this buffer
   * between the given {@linkplain #get(int) index} positions.
   *
   * @param fromIndex index of the first element in the desired subrange: either
   *   an int between 0 and {@link #size} (inclusive),
   *   or a negative offset from the end (between {@code -1} and {@code -size}, inclusive)
   * @param toIndex the limit position of the desired subrange, i.e. the last element in the subrange will be the one
   *   before {@code toIndex}.
   *   Just like {@code fromIndex}, a negative index indicates an offset from the end of the buffer
   * @return an {@link Optional} containing the statistics or an empty {@link Optional} if the buffer is empty
   *   or the specified subrange is empty
   * @throws IndexOutOfBoundsException if either index is less than {@code -size} or greater than {@code size}
   */
  public Optional<SampleStatisticsInt> summarize(int fromIndex, int toIndex) {
    if (size > 0) {
      fromIndex = normalizePositionIndex(fromIndex, size, "fromIndex");
      toIndex = normalizePositionIndex(toIndex, size, "toIndex");
      if (toIndex - fromIndex > 0) {
        NumberSampleOnlineInt stats = new NumberSampleOnlineInt();
        for (int i = fromIndex; i < toIndex; i++) {
          stats.update(get(i));
        }
        return Optional.of(stats);
      }
    }
    return Optional.empty();
  }

  /**
   * Computes the {@linkplain Median#getUpper() upper median} of the values currently held in this buffer.
   *
   * @return an {@link Optional} containing the median or an empty {@link Optional} if the buffer is empty
   */
  public OptionalInt median() {
    return median(0, size);
  }

  /**
   * Computes the {@linkplain Median#getUpper() upper median} for the subrange of values currently held in this buffer
   * starting with the given {@linkplain #get(int) index}.
   *
   * @param fromIndex index of the first element in the desired subrange: either
   *   an int between 0 and {@link #size} (inclusive),
   *   or a negative offset from the end (between {@code -1} and {@code -size}, inclusive)
   * @return an {@link Optional} containing the median or an empty {@link Optional} if the buffer is empty
   *   or the given index is equal to {@link #size()}
   * @throws IndexOutOfBoundsException if the index is less than {@code -size} or greater than {@code size}
   */
  public OptionalInt median(int fromIndex) {
    return median(fromIndex, size);
  }

  /**
   * Computes the {@linkplain Median#getUpper() upper median} for the subrange of values currently held in this buffer
   * between the given {@linkplain #get(int) index} positions.
   *
   * @param fromIndex index of the first element in the desired subrange: either
   *   an int between 0 and {@link #size} (inclusive),
   *   or a negative offset from the end (between {@code -1} and {@code -size}, inclusive)
   * @param toIndex the limit position of the desired subrange, i.e. the last element in the subrange will be the one
   *   before {@code toIndex}.
   *   Just like {@code fromIndex}, a negative index indicates an offset from the end of the buffer
   * @return an {@link Optional} containing the median or an empty {@link Optional} if the buffer is empty
   *   or the specified subrange is empty
   * @throws IndexOutOfBoundsException if either index is less than {@code -size} or greater than {@code size}
   */
  public OptionalInt median(int fromIndex, int toIndex) {
    if (size > 0) {
      int[] arr = toArray(fromIndex, toIndex);
      if (arr.length > 0) {  // (fromIndex, toIndex) slice could be empty
        Arrays.sort(arr);
        return OptionalInt.of(arr[arr.length / 2]);
      }
    }
    return OptionalInt.empty();
  }

  /**
   * Returns an array containing the values currently held in this buffer, ordered from eldest to most-recently added.
   *
   * @return an array of length equal to {@link #size()} or an empty array if the buffer is empty
   */
  public int[] toArray() {
    return toArray(0, size);
  }

  /**
   * Returns an array containing a subrange of the values currently held in this buffer
   * starting with the given {@linkplain #get(int) index}.
   *
   * @param fromIndex index of the first element in the desired subrange: either
   *   an int between 0 and {@link #size} (inclusive),
   *   or a negative offset from the end (between {@code -1} and {@code -size}, inclusive)
   * @return an array containing the elements between {@code fromIndex} and {@code size-1}, 
   *   or an empty array if the buffer is empty or the given index is equal to {@link #size()} 
   * @throws IndexOutOfBoundsException if the index is less than {@code -size} or greater than {@code size}
   */
  public int[] toArray(int fromIndex) {
    return toArray(fromIndex, size);
  }

  /**
   * Returns an array containing a subrange of the values currently held in this buffer
   * between the given {@linkplain #get(int) index} positions.
   *
   * @param fromIndex index of the first element in the desired subrange: either
   *   an int between 0 and {@link #size} (inclusive),
   *   or a negative offset from the end (between {@code -1} and {@code -size}, inclusive)
   * @param toIndex the limit position of the desired subrange, i.e. the last element in the subrange will be the one
   *   before {@code toIndex}.
   *   Just like {@code fromIndex}, a negative index indicates an offset from the end of the buffer
   * @return an array containing the elements between {@code fromIndex} (inclusive) and {@code toIndex} (exclusive),
   *   or an empty array if the buffer is empty or the specified subrange is empty
   * @throws IndexOutOfBoundsException if the index is less than {@code -size} or greater than {@code size}
   */
  public int[] toArray(int fromIndex, int toIndex) {
//    Slice slice = new Slice(fromIndex, toIndex);
    int start = normalizePositionIndex(fromIndex, size, "fromIndex");
    int end = normalizePositionIndex(toIndex, size, "toIndex");
    int length = end - start;
    if (length < 0)
      throw new NegativeArraySizeException(lenientFormat("For range [%s, %s)", start, end));
    int[] ret = new int[length];
    // NOTE: we can't easily use System.arraycopy here because the array needs to wrap around
    for (int i = start, j = 0; i < end; i++, j++) {
      ret[j] = get(i);
    }
    // TODO(6/20/2024): might be able to replace the slow loop with 2 System.arraycopy calls (using subranges before/after cursor)
    return ret;
  }

  /**
   * @return a stream of the values currently held in this buffer, ordered from eldest to most-recently added.
   */
  public IntStream stream() {
    return stream(0, size);
  }

  /**
   * Returns a stream containing a subrange of the values currently held in this buffer
   * starting with the given {@linkplain #get(int) index}.
   *
   * @param fromIndex index of the first element in the desired subrange: either
   *   an int between 0 and {@link #size} (inclusive),
   *   or a negative offset from the end (between {@code -1} and {@code -size}, inclusive)
   * @return a stream containing the elements between {@code fromIndex} and {@code size-1}, 
   *   or an empty stream if the buffer is empty or the given index is equal to {@link #size()}
   * @throws IndexOutOfBoundsException if the index is less than {@code -size} or greater than {@code size}
   */
  public IntStream stream(int fromIndex) {
    return stream(fromIndex, size);
  }

  /**
   * Returns a stream containing a subrange of the values currently held in this buffer
   * between the given {@linkplain #get(int) index} positions.
   *
   * @param fromIndex index of the first element in the desired subrange: either
   *   an int between 0 and {@link #size} (inclusive),
   *   or a negative offset from the end (between {@code -1} and {@code -size}, inclusive)
   * @param toIndex the limit position of the desired subrange, i.e. the last element in the subrange will be the one
   *   before {@code toIndex}.
   *   Just like {@code fromIndex}, a negative index indicates an offset from the end of the buffer
   * @return a stream containing the elements between {@code fromIndex} (inclusive) and {@code toIndex} (exclusive),
   *   or an empty stream if the buffer is empty or the specified subrange is empty
   * @throws IndexOutOfBoundsException if the index is less than {@code -size} or greater than {@code size}
   */
  public IntStream stream(int fromIndex, int toIndex) {
    fromIndex = normalizePositionIndex(fromIndex, size, "fromIndex");
    toIndex = normalizePositionIndex(toIndex, size, "toIndex");
    return IntStream.range(fromIndex, toIndex).map(this::get);
  }

  /**
   * @return an iterator of the values currently held in this buffer, ordered from eldest to most-recently added
   */
  public PrimitiveIterator.OfInt iterator() {
    return stream().iterator();
  }

  /**
   * @return a <code>int[]</code> {@linkplain Ints#asList(int...) array wrapper}
   * containing the values currently held in this buffer, ordered from eldest to most-recently added.
   */
  public List<Integer> asList() {
    return Ints.asList(toArray());
  }

  @Override
  public String toString() {
    return Arrays.toString(toArray());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    BoundedNumberSampleOfInt that = (BoundedNumberSampleOfInt)o;
    return capacity == that.capacity &&
        cursor == that.cursor &&
        size == that.size &&
        Arrays.equals(buffer, that.buffer);
  }

  @Override
  public int hashCode() {
    int result = capacity;
    result = 31 * result + Arrays.hashCode(buffer);
    result = 31 * result + cursor;
    result = 31 * result + size;
    return result;
  }
}
