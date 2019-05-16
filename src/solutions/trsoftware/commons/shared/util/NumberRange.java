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

package solutions.trsoftware.commons.shared.util;

import com.google.common.collect.AbstractSequentialIterator;
import com.google.common.collect.ImmutableSortedSet;

import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Immutable numeric range, capable of generating random numbers within the
 * range.  The endpoints of the range MUST BE either {@link Integer}, {@link Long}, {@link Double}, or {@link Float}.
 * and their values must fit into the {@code double} range for this class to function correctly.
 * <p>
 * NOTE: this class is similar to the {@code xrange} type in Python.
 *
 * @see com.google.common.collect.Range
 * @author Alex
 */
public class NumberRange<T extends Number & Comparable> implements Iterable<T> {

  // NOTE: can't extend Comparable<T> because the fromDouble wouldn't compile under those circumstances.

  private final T min;
  private final T max;

  public NumberRange(T min, T max) {
    // make sure the range is valid
    if (min.doubleValue() > max.doubleValue())
      throw new IllegalArgumentException("min > max");
    this.min = min;
    this.max = max;
  }

  /** The lowest number in this range */
  public T min() {
    return min;
  }

  /** The highest number in this range */
  public T max() {
    return max;
  }

  /** @return a random {@code double} in this range */
  public double randomDouble() {
    return RandomUtils.nextDoubleInRange(min.doubleValue(), max.doubleValue());
  }

  /** @return a random integer in this range */
  public int randomInt() {
    int lowerBound, upperBound;
    if (min.intValue() < min.doubleValue())
      lowerBound = (int)Math.ceil(min.doubleValue());
    else
      lowerBound = min.intValue();
    upperBound = max.intValue();
    return RandomUtils.nextIntInRange(lowerBound, upperBound+1);
  }

  public boolean contains(T number) {
    return inRange(min, max, number);
  }

  /**
   * @return {@code true} iff value is in the given range (both endpoints inclusive) as
   * defined by the type's {@link Comparable#compareTo} implementation.
   */
  public static <T extends Comparable<T>> boolean inRange(T rangeMin, T rangeMax, T value) {
    return value.compareTo(rangeMin) >= 0 && value.compareTo(rangeMax) <= 0;
  }

  /**
   * @return {@code true} iff value is in the given range (both endpoints inclusive)
   */
  public static boolean inRange(int rangeMin, int rangeMax, int value) {
    return value >= rangeMin && value <= rangeMax;
  }

  /**
   * Coerces the number into the range, i.e. if the number is in the range,
   * returns it unchanged, otherwise returns the closest endpoint of the range.
   * 
   * For primitive numbers, the same thing can be accomplished with {@link MathUtils}
   * 
   * @see MathUtils#restrict(int, int, int) 
   * @see MathUtils#restrict(long, long, long) 
   * @see MathUtils#restrict(double, double, double) 
   * @see MathUtils#restrict(float, float, float) 
   */
  public T coerce(T number) {
    if (contains(number))
      return number;
    else {
      return number.compareTo(min) < 0 ? min : max;
    }
  }

  /**
   * @return how many whole numbers are contained within this range (which
   * is equivalent to asking how many numbers will be returned by iterator()).
   */
  public int size() {
    return (int)(Math.floor(max.doubleValue()) - Math.ceil(min.doubleValue())) + 1;
  }

  public String toString() {
    return String.valueOf(min) + ".." + max;
  }

// This beautiful code, alas, would only work on the server side, not GWT-side
//
//  /**
//   * @param str A string in the form of "1..5"
//   */
//  public static <T extends Number> NumberRange<T> fromString(String str, Class<T> c) {
//    int idx = str.indexOf("..");
//    String minStr = str.substring(0, idx);
//    String maxStr = str.substring(idx+2);
//    try {
//      Method parser = c.getMethod("valueOf", String.class);
//      return new NumberRange<T>((T)parser.invoke(null, minStr), (T)parser.invoke(null, maxStr));
//    }
//    catch (Exception e) {
//      e.printStackTrace();
//      throw new IllegalArgumentException(e);
//    }
//  }

  /**
   * @param str a string specifying the {@code int} endpoints of a range separated by {@code ".."} (e.g. {@code "1..5"})
   */
  public static NumberRange<Integer> fromStringIntRange(String str) {
    List<String> endpoints = splitRangeString(str);
    return new NumberRange<Integer>(Integer.valueOf(endpoints.get(0)), Integer.valueOf(endpoints.get(1)));
  }

  /**
   * @param str a string specifying the {@code double} endpoints of a range separated by {@code ".."} (e.g. "1.523..5.1324")
   */
  public static NumberRange<Double> fromStringDoubleRange(String str) {
    List<String> endpoints = splitRangeString(str);
    return new NumberRange<Double>(Double.valueOf(endpoints.get(0)), Double.valueOf(endpoints.get(1)));
  }

  private static List<String> splitRangeString(String str) {
    return StringUtils.split(str, "..");
  }

  /**
   * Creates a number range whose endpoints are within percentOffset % of the midpoint.
   * @param percentOffset Given as an integer percentage value to avoid confusion
   * whether .5d or 50d means 50% percent.
   */
  public static NumberRange<Double> fromPercentOffset(double midpoint, int percentOffset) {
    double delta = midpoint * ((double)percentOffset / 100);
    return fromOffset(midpoint, delta);
  }

  /**
   * Creates a number range whose endpoints are +/- offset from the midpoint.
   */
  public static NumberRange<Double> fromOffset(double midpoint, double offset) {
    return new NumberRange<Double>(midpoint - offset, midpoint + offset);
  }
  

  /** Iterates over the possible values in the range in steps of 1 */
  public Iterator<T> iterator() {
    return iterator(fromDouble(1));
  }

  public Iterator<T> iterator(final T step) {
    return new AbstractSequentialIterator<T>(min) {
      @Override
      protected T computeNext(T previous) {
        T next = fromDouble(previous.doubleValue() + step.doubleValue());
        if (next.compareTo(max) <= 0)
          return next;
        return null;
      }
    };
  }

  private T fromDouble(double v) {
    return (T)NumberUtils.fromDouble(min.getClass(), v);
  }

  /**
   * Parses a string of the form "1, 2, 6..10, 12, 19..23", where each entry
   * is either an integer or an integer range, and returns a sorted set of all the integers
   * represented by this string (in ascending order).
   * @param str
   * @return
   */
  public static SortedSet<Integer> parseIntRangeList(String str) {
    if (StringUtils.isBlank(str))
      return ImmutableSortedSet.of();  // using the Guava version because Collections.emptySortedSet() is not part of JRE emulation in GWT
    SortedSet<Integer> set = new TreeSet<Integer>();
    String[] parts = str.split(",");
    for (String part : parts) {
      part = part.trim();
      if (part.contains("..")) {
        NumberRange<Integer> range = fromStringIntRange(part);
        for (Integer n : range)
          set.add(n);
      }
      else {
        set.add(Integer.valueOf(part));
      }
    }
    return set;
  }


  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    NumberRange that = (NumberRange)o;

    if (max != null ? !max.equals(that.max) : that.max != null) return false;
    if (min != null ? !min.equals(that.min) : that.min != null) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = (min != null ? min.hashCode() : 0);
    result = 31 * result + (max != null ? max.hashCode() : 0);
    return result;
  }
}