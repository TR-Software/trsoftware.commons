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

package solutions.trsoftware.commons.shared.util;

import com.google.common.collect.Iterables;
import solutions.trsoftware.commons.shared.util.random.RandomCharGenerator;
import solutions.trsoftware.commons.shared.util.text.Alphabet;
import solutions.trsoftware.commons.shared.util.text.CharRange;

import java.util.*;

/**
 * @author Alex, 7/29/2017
 */
public class RandomUtils {

  /*
    TODO: might want to convert this class and all its static methods to a normal class that encapsulates a non-static Random instance (passed to the constructor).
    This would allow using seeded randoms to produce repeatable runs (if needed), as well as using instances of SecureRandom (if needed).
    Could provide a default static instance via RandomUtils.getDefaultInstance() if seeding isn't needed.
    Could call the new class something like FluentRandom or EnhancedRandom, and it could be implemented as a decorator:
     (extending java.util.Random and delegating all methods to the wrapped instance, in addition to adding our enhanced methods)
  */

  public static final Random rnd = new Random();

  private RandomUtils() {  // uninstantiable class
  }

  /**
   * @return a random string over all printable ASCII chars
   * @see #randString(int, String)
   * @see StringUtils#ASCII_PRINTABLE_CHARS
   */
  public static String randString(int length) {
    return randString(length, StringUtils.ASCII_PRINTABLE_CHARS);
  }

  /**
   * Creates a string of the given length, with chars chosen at random from the given alphabet
   * <p>
   *   <strong>WARNING:</strong> Because this method relies on {@link String#charAt(int)} to generate chars from the
   *   given alphabet, it will only work if the given alphabet string contains only ASCII chars or unicode
   *   code points that can be represented a standard 16-bit Java {@code char}.  Unicode code points requiring more
   *   than 16 bits are represented in a Java {@link String} as a <a href="http://stn.audible.com/abcs-of-unicode/#java-and-unicode">
   *   surrogate pair</a> (i.e. 2 {@code char}s), and therefore {@link String#charAt(int)} could return invalid chars
   * </p>
   * <p style="color: #6495ed; font-weight: bold;">
   *   TODO: rewrite this method using {@link java.text.BreakIterator#getCharacterInstance()} instead of
   *   {@link String#charAt(int)} as suggested in <a href="http://stn.audible.com/abcs-of-unicode/#java-and-unicode">Java and Unicode (article)</a>
   *   (NOTE: GWT doesn't emulate {@link java.text.BreakIterator}, so instead can use our
   *   {@link StringUtils#codePoints(String)} or {@link StringUtils#codePointsStream(String)} methods, in conjunction
   *   with {@link StringBuilder#appendCodePoint(int)} to build the result string
   * </p>
   *
   * @param length the result will contain this number of chars
   * @param alphabet the chars to choose from; some useful presets are defined in {@link Alphabet} and {@link StringUtils},
   * and custom alphabets can be easily constructed using {@link CharRange}
   * @return a random string of the given length over the given alphabet
   * @see RandomCharGenerator
   * @see StringUtils#ASCII_LETTERS
   * @see StringUtils#ASCII_PRINTABLE_CHARS
   * @see Alphabet
   * @see CharRange
   * @see <a href="http://stn.audible.com/abcs-of-unicode/#java-and-unicode">Java and Unicode (article)</a>
   */
  public static String randString(int length, String alphabet) {
    if (length < 0)
      throw new IllegalArgumentException("Negative string length (" + length + ")");
    else if (length == 0)
      return "";
    StringBuilder buf = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      buf.append(alphabet.charAt(rnd.nextInt(alphabet.length())));
    }
    return buf.toString();
  }

  /**
   * Creates a string containing {@code n} &isin; {@code [minLen, maxLen]} chars chosen at random from the given alphabet.
   * @param alphabet the chars to choose from; some useful presets are defined in {@link Alphabet} and {@link StringUtils},
   * and custom alphabets can be easily constructed using {@link CharRange}
   * @param minLen the result will contain at least this number of chars
   * @param maxLen the result will contain at most this number of chars (inclusive)
   * @return a random string of {@code n} &isin; {@code [minLen, maxLen]} chars over the given alphabet
   * @see #randString(int, String)
   * @see #randString(int)
   */
  public static String randString(String alphabet, int minLen, int maxLen) {
    if (minLen < 0 || minLen >= maxLen)
      throw new IllegalArgumentException("Expected 0 <= minLen < maxLen");
    return randString(nextIntInRange(minLen, maxLen+1), alphabet);
  }

  /**
   * Generates a random integer between 2 endpoints.
   *
   * @param lowerBound inclusive
   * @param upperBound exclusive
   * @return a random {@code int} between {@code lowerBound} (inclusive) and {@code upperBound} (exclusive),
   * with roughly equal probability of any particular {@code int} in this range
   */
  public static int nextIntInRange(int lowerBound, int upperBound) {
    return nextIntInRange(rnd, lowerBound, upperBound);
  }

  /**
   * Generates a random {@code int} in the range {@code [lowerBound, upperBound)} from the given RNG.
   *
   * @param lowerBound inclusive
   * @param upperBound exclusive
   * @return a random {@code int} between {@code lowerBound} (inclusive) and {@code upperBound} (exclusive),
   * with roughly equal probability of any particular {@code int} in this range
   */
  public static int nextIntInRange(Random rnd, int lowerBound, int upperBound) {
    if (!(lowerBound < upperBound))
      throw new IllegalArgumentException("Expected lowerBound < upperBound");
    return rnd.nextInt(upperBound - lowerBound) + lowerBound;
  }

  /**
   * Generates a random {@code double} in range {@code [lowerBound, upperBound]}.
   * @param lowerBound inclusive
   * @param upperBound inclusive
   * @return a random {@code double} between {@code lowerBound} and {@code upperBound}
   */
  public static double nextDoubleInRange(double lowerBound, double upperBound) {
    return nextDoubleInRange(rnd, lowerBound, upperBound);
  }

  /**
   * Returns a random {@code double} in range {@code [lowerBound, upperBound]} from the given RNG.
   * @param lowerBound inclusive
   * @param upperBound inclusive
   * @return a random {@code double} between {@code lowerBound} and {@code upperBound}
   */
  public static double nextDoubleInRange(Random rnd, double lowerBound, double upperBound) {
    return rnd.nextDouble() * (upperBound - lowerBound) + lowerBound;
  }

  /**
   * @return the next pseudo-random number in the given generator's sequence,
   * drawn from a Gaussian distribution with the given {@code mean} and {@code stdev}.
   * @see Random#nextGaussian()
   */
  public static double nextGaussian(Random rnd, double mean, double stdev) {
    return rnd.nextGaussian() * stdev + mean;
  }

  /**
   * @return the next pseudo-random number in the sequence,
   * drawn from a Gaussian distribution with the given {@code mean} and {@code stdev}.
   * @see #nextGaussian(Random, double, double)
   */
  public static double nextGaussian(double mean, double stdev) {
    return nextGaussian(rnd, mean, stdev);
  }

  /**
   * @return the next pseudo-random integer in the sequence,
   * drawn from a Gaussian distribution with the given {@code mean} and {@code stdev}.
   * @see Random#nextGaussian()
   */
  public static int nextGaussian(int mean, int stdev) {
    return (int)(rnd.nextGaussian() * stdev + mean);
  }

  /**
   * Intended as a GWT-compatible replacement for {@link Collections#shuffle(List)}.
   *
   * @deprecated GWT now supports both {@link Collections#shuffle(List)} and {@link Collections#shuffle(List, Random)}
   * (at least as of GWT 2.8.x)
   */
  public static <T> void shuffle(List<T> list) {
    int size = list.size();
    if (CollectionUtils.isEmpty(list) || size == 1)
      return;
    // Alg:
    // dump all the elements into an array list (to give it random access)
    // then repeatedly choose random elements as we fill the original list back up
    ArrayList<T> tempList = new ArrayList<T>(list);
    // some List implementations (e.g. Arrays.asList) don't support removing elements,
    // only swapping them, so if a list supports random access we don't clear the original list
    if (list instanceof RandomAccess) {
      for (int i = 0; i < size; i++) {
        list.set(i, tempList.remove(rnd.nextInt(tempList.size())));
      }
    }
    else {
      // this code will be faster than the above branch for linked lists
      list.clear();
      while (!tempList.isEmpty()) {
        list.add(tempList.remove(rnd.nextInt(tempList.size())));
      }
    }
  }

  public static <T> List<T> randomSampleWithoutReplacement(Collection<T> collection, int sampleSize) {
    return randomSampleWithoutReplacement(collection, sampleSize, rnd);
  }

  public static <T> List<T> randomSampleWithoutReplacement(Collection<T> collection, int sampleSize, Random rnd) {
    if (sampleSize > collection.size())
      throw new IllegalArgumentException("sampleSize > collection.size()");

    ArrayList<T> copy = new ArrayList<T>(collection);
    Collections.shuffle(copy, rnd);
    return ListUtils.subList(copy, 0, sampleSize);
  }

  public static <T> List<T> randomSampleWithReplacement(Collection<T> collection, int sampleSize) {
    List<T> list;
    if (collection instanceof List && collection instanceof RandomAccess)
      list = (List<T>)collection;
    else
      list = new ArrayList<T>(collection);  // we need a random access list
    List<T> sample = new ArrayList<T>(sampleSize);
    for (int i = 0; i < sampleSize; i++) {
      sample.add(list.get(nextIntInRange(0, list.size())));
    }
    return sample;
  }

  @SafeVarargs
  public static <T> T randomElement(T... arr) {
    return randomElement(rnd, arr);
  }

  @SafeVarargs
  public static <T> T randomElement(Random rnd, T... arr) {
    return arr[rnd.nextInt(arr.length)];
  }

  public static <T> T randomElement(List<T> list) {
    return randomElement(list, rnd);
  }

  public static <T> T randomElement(List<T> list, Random rnd) {
    if (!(list instanceof RandomAccess))
      System.err.println("WARNING: RandomUtils.randomElement received a non-random-access list (" + list.getClass().getName() + ")");
    return list.get(rnd.nextInt(list.size()));
  }

  /** @return a random element from the collection, using the {@linkplain #rnd default RNG} */
  public static <T> T randomElement(Collection<T> collection) {
    return randomElement(collection, rnd);
  }

  /** @return a random element from the collection, using the given RNG */
  public static <T> T randomElement(Collection<T> collection, Random rnd) {
    int index = rnd.nextInt(collection.size());
    return Iterables.get(collection, index);
  }

  /**
   * @param n the number of bytes to generate
   * @return {@code n} random bytes
   */
  public static byte[] randBytes(int n) {
    byte[] bytes = new byte[n];
    rnd.nextBytes(bytes);
    return bytes;
  }

  /**
   * @return a random positive integer.
   */
  public static int randPositiveInt() {
    return randInt(Integer.MAX_VALUE);
  }

  /**
   * @see Random#nextInt()
   */
  public static int randInt() {
    return rnd.nextInt();
  }

  public static int[] randIntArray(int length) {
    int[] ret = new int[length];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = randInt();
    }
    return ret;
  }

  /**
   * @see Random#nextInt(int)
   */
  public static int randInt(int n) {
    return rnd.nextInt(n);
  }

  /**
   * @see Random#nextDouble()
   */
  public static double randDouble() {
    return rnd.nextDouble();
  }
}
