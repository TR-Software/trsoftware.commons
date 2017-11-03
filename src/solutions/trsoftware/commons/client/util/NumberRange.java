package solutions.trsoftware.commons.client.util;

import solutions.trsoftware.commons.client.bridge.util.RandomGen;

import java.util.Iterator;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Immutable number range, capable of generating random numbers within the
 * range.  The endpoints of the range MUST BE either Integer, Long, Double, or Float.
 * and their values must fit into the double range for this class to function correctly.
 *
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

  // TODO: refactor this method using a new static randomDouble method (implemented the same way as the new static randomInt method)
  /** Returns a random double in this range */
  public double random() {
    // using Math.random instead of java.util.Random for GWT compatibility
    return Math.random() * (max.doubleValue() - min.doubleValue()) + min.doubleValue();
  }

  // TODO: refactor this method using the new static randomInt method
  /** Returns a random integer in this range */
  public int randomInt() {
    int lowerBound, upperBound;
    if (min.intValue() < min.doubleValue())
      lowerBound = (int)Math.ceil(min.doubleValue());
    else
      lowerBound = min.intValue();
    upperBound = max.intValue();
    return RandomGen.getInstance().nextIntInRange(lowerBound, upperBound + 1);
  }

  public boolean contains(T number) {
    return inRange(min, max, number);
  }

  /**
   * @return true iff value is in the given range (both endpoints inclusive) as
   * defined by the type's Comparable implementation.
   */
  public static <T extends Comparable<T>> boolean inRange(T rangeMin, T rangeMax, T value) {
    return value.compareTo(rangeMin) >= 0 && value.compareTo(rangeMax) <= 0;
  }

  /**
   * Coerces the number into the range, i.e. if the number is in the range,
   * returns it unchanged, otherwise returns the closest endpoint of the range.
   */
  public T coerce(T number) {
    if (contains(number))
      return number;
    else {
      return number.compareTo(min) < 0 ? min : max;
    }
  }

  /**
   * @returns how many whole numbers are contained within this range (which
   * is equivalent to asking how many numbers will be returned by iterator()).
   */
  public int size() {
    return (int)(Math.floor(max.doubleValue()) - Math.ceil(min.doubleValue())) + 1;
  }

  public String toString() {
    return new StringBuilder().append(min).append("..").append(max).toString();
  }

// This beatiful code, alas, would only work on the server side, not GWT-side
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
   * @param str A string in the form of "1..5"
   */
  public static NumberRange<Integer> fromStringIntRange(String str) {
    String[] parts = splitRangeString(str);
    return new NumberRange<Integer>(Integer.valueOf(parts[0]), Integer.valueOf(parts[1]));
  }

  /**
   * @param str A string in the form of "1.523..5.1324"
   */
  public static NumberRange<Double> fromStringDoubleRange(String str) {
    String[] parts = splitRangeString(str);
    return new NumberRange<Double>(Double.valueOf(parts[0]), Double.valueOf(parts[1]));
  }

  private static String[] splitRangeString(String str) {
    int idx = str.indexOf("..");
    return new String[]{str.substring(0, idx), str.substring(idx+2)};
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
    return new Iterator<T>() {
      T next = min;
      public boolean hasNext() {
        return next.compareTo(max) <= 0;
      }
      public T next() {
        T ret = next;
        next = fromDouble(ret.doubleValue() + step.doubleValue());
        return ret;
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  private T fromDouble(double v) {
    if (min instanceof Double)
      return (T)Double.valueOf(v);
    else if (min instanceof Integer)
      return (T)Integer.valueOf((int)v);
    else if (min instanceof Long)
      return (T)Long.valueOf((long)v);
    else if (min instanceof Float)
      return (T)Float.valueOf((float)v);
    throw new NumberFormatException("NumberRange " + toString() + " must be either Double/Float/Integer/Long");
  }

  private static SortedSet emptySortedSet;

  /**
   * Parses a string of the form "1, 2, 6..10, 12, 19..23", where each entry
   * is either an integer or an integer range, and returns a sorted set of all the integers
   * represented by this string (in ascending order).
   * @param str
   * @return
   */
  public static SortedSet<Integer> parseIntRangeList(String str) {
    if (StringUtils.isBlank(str))
      return emptySortedSet != null ? emptySortedSet : (emptySortedSet = new TreeSet()); 
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


  /**
   * Returns a random {@code int} in range {@code [lowerBound, upperBound]} from the
   * given RNG.
   *
   * @param lowerBound inclusive
   * @param upperBound exclusive
   */
  public static int randomInt(Random rnd, int lowerBound, int upperBound) {
    Assert.assertTrue(lowerBound < upperBound);
    return rnd.nextInt(upperBound - lowerBound) + lowerBound;
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