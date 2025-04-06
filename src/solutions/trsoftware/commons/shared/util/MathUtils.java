/*
 * Copyright 2022 TR Software Inc.
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

package solutions.trsoftware.commons.shared.util;

import com.google.common.primitives.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.*;

/**
 * @since Oct 23, 2008
 * @author Alex
 *
 * @see com.google.common.math
 */
public class MathUtils {

  /**
   * An arbitrarily small positive quantity that can be used to ignore precision loss from floating-point operations.
   * @see #equal(double, double, double)
   */
  public static final double EPSILON = 0.0001;

  // TODO: move all the bitwise operations (e.g. packUnsignedInt) to BitwiseUtils (and make sure they don't duplicate the functionality in com.google.common.primitives.UnsignedInteger

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
   * Maps a non-negative 32-bit number onto the range of a signed {@code int}, such that
   * the results retain their natural order comparison semantics (but lose their identity).
   * Specifically, this conversion maps:
   * <br>{@code 0}                                             &rarr; {@link Integer#MIN_VALUE}
   * <br>{@link Integer#MAX_VALUE} (<tt>2<sup>31</sup>-1</tt>) &rarr; {@code -1}
   * <br><tt>2<sup>31</sup></tt>                               &rarr; {@code 0}, and
   * <br><tt>2<sup>32</sup></tt>                               &rarr; {@link Integer#MAX_VALUE}
   * <br>The conversion can be reversed by invoking {@link #unsignedInt(int)} on the result.
   * <p>
   * <b>Note:</b> This scheme differs from the more-standard conversions produced by Java's {@code (int)} cast
   * and Guava's {@link UnsignedInts#saturatedCast(long)}, which map values
   * {@code 0}..{@value Integer#MAX_VALUE} to themselves, <tt>2<sup>31</sup></tt> to {@value Integer#MIN_VALUE},
   * and <tt>2<sup>32</sup></tt> to {@code -1}.
   * <p>
   * The advantage of our approach is the retention of comparison semantics, e.g.
   * <nobr><code>{@link #packUnsignedInt}({@value Integer#MAX_VALUE}) < 2147483648</code></nobr>,
   * (whereas a plain cast results in <nobr><code>(int){@value Integer#MAX_VALUE} > (int)2147483648</code></nobr>).
   * However, the downside is the loss of identity for values that already fit into the {@code int} range.
   * <p>
   * Therefore, unless the comparison semantics are really needed, we recommend just using a standard {@code (int)} cast
   * along with {@link Integer#toUnsignedLong(int)} instead of {@link #packUnsignedInt(long)} / {@link #unsignedInt(int)}.
   *
   * @param unsigned value between {@code 0} and <tt>2<sup>32</sup></tt>
   * @return {@code int} representation of the argument that can be converted back to the original {@code long}
   *   by invoking {@link #unsignedInt(int)}
   * @throws IllegalArgumentException if argument not in range <tt>[0, 2<sup>32</sup>]</tt>
   * @see #unsignedInt(int)
   * @see com.google.common.primitives.UnsignedInteger
   * @see UnsignedInts#saturatedCast(long)
   */
  public static int packUnsignedInt(long unsigned) {
    checkArgument(unsigned >= 0 && unsigned <= 0xffffffffL,
        "Out of range [0, 0xffffffffL]: %s", unsigned);
    return (int)(unsigned + Integer.MIN_VALUE);
  }

  /**
   * Converts a signed {@code int} value produced by {@link #packUnsignedInt(long)}
   * back to a {@code long} representing an "unsigned int" [0, 2<sup>32</sup>]
   *
   * @see com.google.common.primitives.UnsignedInteger
   * @see Integer#toUnsignedLong(int)
   */
  public static long unsignedInt(int i) {
    return (long)i - Integer.MIN_VALUE;
  }

  /**
   * Maps a non-negative 8-bit number [0, 255] onto the range of a signed {@code byte} [-128, 127], such that
   * the results retain their natural order comparison semantics (but lose their identity).
   * <p>
   * Specifically, this conversion maps
   * {@code 0} to {@value Byte#MIN_VALUE} ({@code 0x80}),
   * {@code 128} to {@code 0}, and
   * {@code 255} to {@value Byte#MAX_VALUE} ({@code 0x7F}).
   * The conversion can be reversed by invoking {@link #unsignedByte(byte)} on the result.
   * <p>
   * <b>Note:</b> This scheme differs from the more-standard values produced by Java's {@code (byte)} cast
   * and {@link UnsignedBytes#saturatedCast(long)}, which map values {@code 0}..{@code 127} to themselves,
   * {@code 128} to {@code -1} ({@code 0x80}), and
   * {@code 255} to {@code -128} ({@code 0xFF}).
   * <p>
   * The advantage of our approach is the retention of comparison semantics, e.g.
   * <nobr><code>({@link #packUnsignedByte}(127) < {@link #packUnsignedByte}(128))</code></nobr>, whereas a plain cast
   * results in <nobr><code>((byte)127 > (byte)128)</code></nobr>.  However, the downside is the loss of identity
   * for values {@code 0}..{@code 127}.
   * <p>
   * Therefore, unless the comparison semantics are really needed, we recommend just using a standard {@code (byte)} cast
   * along with {@link Byte#toUnsignedInt(byte)} instead of {@link #packUnsignedByte(int)} / {@link #unsignedByte(byte)}.
   * @param unsigned integer between {@code 0} and {@code 255}
   * @return a {@code byte} representation of the argument that can be converted back to the original {@code int}
   *   by invoking {@link #unsignedByte(byte)}
   * @throws IllegalArgumentException if argument not in range {@code [0, 255]}
   * @see UnsignedBytes#saturatedCast(long)
   */
  public static byte packUnsignedByte(int unsigned) {
    checkArgument(unsigned >= 0 && unsigned <= 255,
        "Out of range [0, 255]: %s", unsigned);
    return (byte)(unsigned + Byte.MIN_VALUE);
  }

  /**
   * Converts a signed {@code byte} value produced by {@link #packUnsignedByte(int)}
   * back to an {@code int} representing an "unsigned byte" [0, 255]
   * @see UnsignedBytes
   * @see Byte#toUnsignedInt(byte)
   */
  public static int unsignedByte(byte b) {
    return (int)b - Byte.MIN_VALUE;
  }

  /**
   * Returns {@link BigInteger#toByteArray()} without the extra sign byte if the sign byte is {@code 0}
   *
   * @return the value of {@link BigInteger#toByteArray()} if the sign byte is not {@code 0}, or a copy of that array without
   * the leading byte if it's {@code 0}
   */
  public static byte[] bigIntToUnsignedByteArray(BigInteger bigInt) {
    // TODO: maybe add a corresponding bigIntFromUnsignedByteArray method (returning new BigInteger(1, byteArr))
    // see https://stackoverflow.com/a/4408124/1965404
    byte[] bytes = bigInt.toByteArray();
    // remove the extra sign byte if it's 0 (i.e. positive)
    if (bytes.length > 1 && bytes[0] == (byte)0)
      bytes = Arrays.copyOfRange(bytes, 1, bytes.length);
    return bytes;
  }

  /**
   * Computes {@code n!}
   *
   * @throws IllegalArgumentException is {@code n} is negative
   * @throws ArithmeticException if the result doesn't fit in a {@code long} (i.e. if {@code n > 20})
   *
   * @see com.google.common.math.LongMath#factorial(int)
   * @see com.google.common.math.IntMath#factorial(int)
   */
  public static long factorial(int n) {
    if (n < 0)
      throw new IllegalArgumentException("Factorial is not defined for negative numbers.");
    return nPr(n, n);
  }

  /**
   * Computes the number of permutations (order matters) of {@code r} elements from a sequence of {@code n}.
   * <p>
   * Equal to {@code n!/(n-r)!}
   *
   * @see <a href="http://en.wikipedia.org/wiki/Permutation">"Permutation" on Wikipedia</a>
   */
  public static long nPr(int n, int r) {
    if (n < 0 || r < 0 || n < r)
      throw new IllegalArgumentException("nPr is not defined for inputs " + n + " and " + r);
    long product = 1;
    for (int i = n-r+1; i <= n; i++) {
      product = Math.multiplyExact(product, i);
      if (product < 0)
        throw new ArithmeticException("Result is too large to be represented by the long integer type.");
    }
    return product;
  }

  /**
   * Computes <i>n choose r</i>, which is the number of subsets (order irrelevant) of {@code r} elements from a set of {@code n}.
   * <p>
   * Equal to {@code n!/(r!(n-r)!)}
   *
   * @see <a href="http://en.wikipedia.org/wiki/Choose_function">"Choose function" on Wikipedia</a>
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
    // handle infinity specially since subtracting two infinite values gives NaN and the following test fails
    if (Double.isInfinite(a) || Double.isInfinite(b)) {
      return a == b;
    }
    return Math.abs(a-b) <= Math.abs(delta);
  }

  /**
   * Returns {@code x} rounded to {@code n} digits after the decimal point
   * (just like the <a href="https://docs.python.org/2.7/library/functions.html#round">{@code round}</a> function in Python).
   * <p>
   * Values are rounded to the closest multiple of <code>10<sup>-n</sup></code>;
   * if two multiples are equally close, rounding is done away from 0
   * (so, for example, {@code round(0.5, 0)} is 1.0 and {@code round(-0.5, 0)} is -1.0).
   * <p>
   * <b>Warning</b>: The rounding behavior of this method can be surprising due to the fact that most decimal fractions can't be
   * represented exactly with floating point values.
   * For example, {@code round(2.675, 2)} gives {@code 2.67} instead of the expected {@code 2.68}.
   * <p>
   * <em>Note:</em>
   * Since this method has to instantiates a {@link BigDecimal} to do the rounding
   * (unless the given {@code double} is zero, NaN, or Infinity), you might as well use {@link #round(BigDecimal, int)}
   * whenever possible, in order to avoid the aforementioned floating-point representation errors.
   * However, in most cases, it's probably even better to just avoid this method entirely and use string formatting
   * (e.g. {@link java.text.DecimalFormat}, {@link String#format}, etc.) if that's ultimately the desired use-case.
   *
   * @param x the number to round
   * @param n decimal places
   * @return {@code x} rounded to {@code n} decimal places
   *
   * @see <a href="https://docs.python.org/2.7/tutorial/floatingpoint.html">Floating Point Arithmetic: Issues and Limitations</a>
   * @see #round(BigDecimal, int)
   */
  public static double round(double x, int n) {
    /*
     * This code was borrowed from the Jython implementation of Python's round function
     * (see https://github.com/jython/jython/blob/3f01873cdf41c0a536425152e1357892f681ea49/src/org/python/core/util/ExtraMath.java#L39-L86)
     *
     * Except that in order to make it GWT-compatible, we had to remove certain fast-path optimizations performed in
     * the Jython code source code. However, some basic testing (with 1000 random doubles in range [0,300])
     * showed that their fast path doesn't apply 99.9% of the time anyway.
     */
    // see https://github.com/jython/jython/blob/3f01873cdf41c0a536425152e1357892f681ea49/src/org/python/core/util/ExtraMath.java#L39-L86
    if (Double.isNaN(x) || Double.isInfinite(x) || x == 0.0) {
      // NaNs, infinities, and zeros round to themselves
      return x;
    }
    else {
      // go straight to BigDecimal, since GWT doesn't support Math.getExponent(x), which is used in Jython's fast-path
      // (NOTE: the BigDecimal constructor throws a NumberFormatException if the double is infinite or NaN, but we've already checked for that)
      return round(new BigDecimal(x), n).doubleValue();
    }
  }

  /**
   * Rounds {@code x} to {@code n} digits after the decimal point by invoking
   * {@link BigDecimal#setScale(int, RoundingMode) x.setScale(n, RoundingMode.HALF_UP)}.
   * <p>
   * Working with {@link BigDecimal} values allows avoiding the various floating point representation pitfalls,
   * so this method should be used instead of {@link #round(double, int)} whenever possible.
   * Just make sure you instantiate the value with {@link BigDecimal#valueOf(double)} and
   * <em>not</em> {@link BigDecimal#BigDecimal(double)}.
   *
   * @param x the number to round
   * @param n decimal places
   * @return {@code x} rounded to {@code n} decimal places
   *
   * @see <a href="https://docs.python.org/2.7/library/functions.html#round">The <code>round</code> function in Python</a>
   */
  public static BigDecimal round(BigDecimal x, int n) {
    return x.setScale(n, RoundingMode.HALF_UP);
  }

  /**
   * The equivalent of Python's {@code %} operator, which always returns a number with the same sign as the divisor {@code b}.
   * This is useful for things like wrapping array indices.
   * <p>
   * Note: this method is equivalent to {@link Math#floorMod(int, int)} (introduced in Java 1.8), so the only reason
   * to use it instead of the one in {@link Math} is compatibility with older GWT versions.
   *
   * @return The mathematical {@code a mod b}, which unlike the native {@code %} operator,
   * is guaranteed to have the same sign as the divisor {@code b}
   * @see Math#floorMod(int, int)
   */
  public static int floorMod(int a, int b) {
    return (a % b + b) % b;
  }

  /**
   * @return the number closest to {@code value} in the range {@code [a, b]}
   * @see NumberRange#coerce(Number)
   * @see Doubles#constrainToRange(double, double, double)
   */
  public static double restrict(double value, double a, double b) {
    return min(max(value, a), b);
  }

  /**
   * @return the number closest to {@code value} in the range {@code [a, b]}
   * @see NumberRange#coerce(Number)
   * @see Floats#constrainToRange
   */
  public static float restrict(float value, float a, float b) {
    return min(max(value, a), b);
  }

  /**
   * @return the number closest to {@code value} in the range {@code [a, b]}
   * @see NumberRange#coerce(Number)
   * @see Ints#constrainToRange
   */
  public static int restrict(int value, int a, int b) {
    return min(max(value, a), b);
  }

  /**
   * @return the number closest to {@code value} in the range {@code [a, b]}
   * @see NumberRange#coerce(Number)
   * @see Longs#constrainToRange
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
    return Integer.compare(x, 0);
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

  /**
   * Determines whether the arg is a (positive) power of 2.
   *
   * @param n the integer to test
   * @return {@code true} iff {@code n} is a power of 2 (e.g. 1, 2, 4, 8, etc..)
   */
  public static boolean isPowerOf2(int n) {
    // see https://stackoverflow.com/a/600306/1965404
    return n > 0 && ((n & (n-1)) == 0);
  }
}
