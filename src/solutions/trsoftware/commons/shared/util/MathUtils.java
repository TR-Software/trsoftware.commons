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

import java.math.BigDecimal;
import java.math.MathContext;

import static java.lang.Math.*;

/**
 * Date: Oct 23, 2008 Time: 3:59:50 PM
 *
 * @author Alex
 */
public class MathUtils {

  /**
   * An arbitrarily small positive quantity that can be used to ignore precision loss with floating-point operations.
   */
  public static final double EPSILON = 0.0001;

  /**
   * Converts a 128-bit integer represented by the two given {@code long} components to a {@code byte} array.
   */
  public static byte[] int128ToByteArray(long msb, long lsb) {
    byte[] bytes = new byte[16];
    for (int i = 0; i < 8; i++) {
      bytes[i] = (byte)((msb >> (64-8*(i+1))) & 0xffL);
      bytes[8+i] = (byte)((lsb >> (64-8*(i+1))) & 0xffL);
    }
    return bytes;
  }

  /**
   * Maps the given unsigned 32-bit number into the range of a signed int.
   * {@code 0} maps to {@link Integer#MIN_VALUE} and {@code 0xFFFFFFFF} maps to {@link Integer#MAX_VALUE}
   * This ensures that the values will still be comparable by their natural ordering.
   */
  public static int packUnsignedInt(long unsigned) {
    if (unsigned < 0 || unsigned > 0xffffffffL)
      throw new IllegalArgumentException("Expected value in range 0..0xffffffffL");
    return (int)(unsigned + Integer.MIN_VALUE);
  }

  /**
   * Returns the unsigned representation of the given integer as a long.
   * This is the inverse of {@link #packUnsignedInt(long)}
   */
  public static long unsignedInt(int i) {
    return (long)i - Integer.MIN_VALUE;
  }

  /**
   * Converts an 8 bit integer value (0..255) into a signed byte (-128..127).
   * The results will still be comparable by their natural ordering.
   */
  public static byte packUnsignedByte(int i) {
    if (i < 0 || i > 255)
      throw new IllegalArgumentException("Expected value in range 0..255");
    return (byte)(i + Byte.MIN_VALUE);
  }

  /**
   * Convert a signed byte (-128..127) to an "unsigned byte" (0..255) int value.
   * This is the inverse of {@link #packUnsignedByte(int)}
   */
  public static int unsignedByte(byte b) {
    return (int)b - Byte.MIN_VALUE;
  }
  
  public static long factorial(int n) {
    if (n < 0)
      throw new IllegalArgumentException("Factorial is not defined for negative numbers.");
    return nPr(n, n);
  }

  /**
   * How many permutations (order matters) of r elements from a sequence of n?
   * n!/(n-r)!
   *
   * see: http://en.wikipedia.org/wiki/Permutation
   */
  public static long nPr(int n, int r) {
    if (n < 0 || r < 0 || n < r)
      throw new IllegalArgumentException("nPr is not defined for inputs " + n + " and " + r);
    long product = 1;
    for (int i = n-r+1; i <= n; i++) {
      product *= i;
      if (product < 0)
        throw new ArithmeticException("Result is too large to be represented by the long integer type.");
    }
    return product;
  }

  /**
   * How many subsets (order irrelevant) of r elements from a set of n?
   * n!/(r!(n-r)!)
   * 
   * see: http://en.wikipedia.org/wiki/Choose_function
   */
  public static long nCr(int n, int r) {
    return nPr(n, r) / factorial(r);
  }

  /**
   * A Fibonacci implementation that runs in O(n) time and O(1) space without
   * using recursion.
   *
   * @param n must be positive
   * @return The n-th number in the Fibonacci sequence (starting with first,
   * the numbers are 0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233,
   * 377, 610, 987, 1597, etc..)
   */
  public static int fibonacci(int n) {
    if (n < 0)
      throw new IllegalArgumentException("fibonacci(n): n must be nonnegative");
    int a = 0;
    int b = 1;
    for (int i = 0; i < n; i++) {
      a = a + b;
      b = a - b;
    }
    return a;
  }

  /** @return the number of characters required to represent the given integer as a string (including the sign) */
  public static int decimalCharCount(int n) {
    if (n == 0)
      return 1;
    int signChars = 0;
    if (n < 0) {
      signChars = 1;
      if (n == Integer.MIN_VALUE) // this is a special case for which Math.abs doesn't work
        n = Integer.MAX_VALUE;  // Integer.MAX_VALUE has the same number of digits as Integer.MIN_VALUE
      n = abs(n);
    }
    return signChars + (int)floor(log10(n)) + 1;
  }

  /**
   * @return the fractional part of the given double
   * <strong>Examples</strong>:
   * {@code 3.456 &rarr; .456}; {@code 3 &rarr; 0}
   */
  public static double getFractionalPart(double val) {
    if (val <= Integer.MAX_VALUE) {
      int wholePart = (int)val;
      return val - wholePart;
    } else {
      long wholePart = (long)val;
      return val - wholePart;
    }
  }

  /**
   * Checks whether the given {@code double} values are "approximately" equal.  More specifically, compares them
   * within the given margin of error.
   * <p>
   * This is preferable to using the {@code ==} operator or {@link Double#compare(double, double)}
   * when one of the values might come from a result of a floating-point arithmetic
   * (which could have precision error). For example:
   * {@code 32450.0 / 3.75 * 3.75} &rarr; {@code 32450.000000000004} (whereas we would expect {@code 32450.0}).
   *
   * @param delta the margin of error for the comparison;
   *              the {@link #EPSILON} constant is provided for this use-case
   * @return {@code true} iff {@code expected == actual} or the difference between the two values is {@code <= delta}
   * @throws IllegalArgumentException if {@code delta} is negative
   * @see #EPSILON
   * @see junit.framework.Assert#assertEquals(double, double, double)
   */
  public static boolean equal(double a, double b, double delta) {
    // handle infinity specially since subtracting to infinite values gives NaN and the following test fails
    if (Double.isInfinite(a) || Double.isInfinite(b)) {
      return a == b;
    }
    return Math.abs(a-b) <= Math.abs(delta);
  }

  /**
   * Attempts to use {@link BigDecimal} to round a {@code double} to the given number of decimal places, as suggested
   * by <a href="http://www.baeldung.com/java-round-decimal-number">this article</a>.
   *
   * <p style="color: red; font-weight: bold;">
   *   DO NOT USE this method because it often doesn't return the desired result.  Use string formatting instead.
   * </p>
   *
   * @return {@code value} rounded to {@code nPlaces} decimal places.
   *
   * @deprecated this method still exists simply as a warning to never attempt to solve this problem again; it's pointless.
   * It doesn't even make sense to attempt to round a {@code double} to a given number of decimal places because
   * floating-point numbers are so imprecise.  Most of the solutions described on the web don't actually work well,
   * and it seems that the only way to do it properly is to use the string formatting classes (e.g. DecimalFormat, printf, etc).
   * It actually makes a lot more sense to use string formatting to achieve this kind of rounding,
   * because the only real need for this operation is to pretty-print a number as text.
   */
  public static double round(double value, int nPlaces) {
    if (nPlaces < 0)
      throw new IllegalArgumentException(String.valueOf(nPlaces));
    double rint = Math.rint(value);
    if (nPlaces == 0 || rint == value)
      return rint;  // can't pass precision=0 to BigDecimal.round (this would cancel rounding)
    else {
      /*
      We can only use BigDecimal to round the fractional part of the number, because
      new BigDecimal(245.245678).round(new MathContext(2, RoundingMode.HALF_UP)) returns 250.0, whereas we want 245.25
      Furthermore, it won't even work for the fractional part some of the time, for example:
      - round(0.04584203269100395, 1) = 0.05 // instead of 0.0
      - round(0.013107220012424037, 3) = 0.0131  // instead of 0.013
      */
      double floor = Math.floor(value);
      double fraction = value - floor;
      return floor + new BigDecimal(fraction).round(new MathContext(nPlaces)).doubleValue();
    }
  }

  /**
   * Casts {@code x} to {@code float} and invokes {@link Math#round(float)} on it. This is preferable to using
   * {@link Math#round(double)} when we want to avoid {@code long} emulation in compiled GWT code.
   */
  public static int round(double x) {
    return Math.round((float)x);
  }

  /**
   * The equivalent of Python's {@code %} operator, which always returns a number with the same sign as the divisor {@code b}.
   * This is useful for things like wrapping array indices.
   *
   * NOTE: Java 8 actually introduced the {@link Math#floorMod(int, int)} method, but we can't use that from older GWT versions.
   *
   * @return The mathematical {@code a mod b}. Unlike Java's native {@code %} operator, our result
   * is guaranteed to have the same sign as the divisor {@code b} (same as Python's {@code %} operator).
   */
  public static int floorMod(int a, int b) {
    return (a % b + b) % b;
  }

  /**
   * @return the number closest to {@code value} in the range {@code [a, b]}
   * @see NumberRange#coerce(Number)
   */
  public static double restrict(double value, double a, double b) {
    return min(max(value, a), b);
  }

  /**
   * @return the number closest to {@code value} in the range {@code [a, b]}
   * @see NumberRange#coerce(Number)
   */
  public static float restrict(float value, float a, float b) {
    return min(max(value, a), b);
  }

  /**
   * @return the number closest to {@code value} in the range {@code [a, b]}
   * @see NumberRange#coerce(Number)
   */
  public static int restrict(int value, int a, int b) {
    return min(max(value, a), b);
  }

  /**
   * @return the number closest to {@code value} in the range {@code [a, b]}
   * @see NumberRange#coerce(Number)
   */
  public static long restrict(long value, long a, long b) {
    return min(max(value, a), b);
  }

  /**
   * Computes the mathematical signum function, which is defined to return one of {@code -1},
   * {@code 0}, or {@code 1} according to whether the argument is negative, zero or positive.
   * <p>
   * Note: this is the integer version of {@link Math#signum(float)} / {@link Math#signum(double)}, useful for
   * testing results of {@link java.util.Comparator#compare(Object, Object)}.
   *
   * @return the signum function of the argument: zero if the argument is zero, {@code 1} if the argument is greater
   *     than zero, {@code -1} if the argument is less than zero.
   * @see Math#signum(float)
   * @see Math#signum(double)
   */
  public static int signum(int x) {
    if (x == 0)
      return 0;
    else if (x > 0)
      return 1;
    else
      return -1;
  }

  /**
   * Computes the mathematical signum function, which is defined to return one of {@code -1},
   * {@code 0}, or {@code 1} according to whether the argument is negative, zero or positive.
   * <p>
   * Note: this is the integer version of {@link Math#signum(float)} / {@link Math#signum(double)}, useful for
   * testing results of {@link java.util.Comparator#compare(Object, Object)}.
   *
   * @return the signum function of the argument: zero if the argument is zero, {@code 1} if the argument is greater
   *     than zero, {@code -1} if the argument is less than zero.
   * @see Math#signum(float)
   * @see Math#signum(double)
   */
  public static int signum(long x) {
    if (x == 0)
      return 0;
    else if (x > 0)
      return 1;
    else
      return -1;
  }
}
