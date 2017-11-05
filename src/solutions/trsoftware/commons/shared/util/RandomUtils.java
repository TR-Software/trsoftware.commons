/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.shared.util;

import solutions.trsoftware.commons.shared.util.random.RandomCharGenerator;
import solutions.trsoftware.commons.shared.util.text.Alphabet;

import java.util.*;

/**
 * @author Alex, 7/29/2017
 */
public class RandomUtils {

  /*
    TODO: might want to convert this class and all its static methods to a normal class that encapsulates a non-static Random instance (passed to the constructor).
    This would allow using seeded randoms to produce repeatable runs (if needed), as well as using instances of SecureRandom (if needed).
    Could provide a default static instance via RandomUtils.getDefaultInstance() if seeding isn't needed.
  */

  public static final Random rnd = new Random();

  private RandomUtils() {  // uninstantiable class
  }

  /**
   * @return a random string over all printable ASCII chars
   * @see #randString(int, String)
   * @see Alphabet#getAllPrintableAsciiChars()
   */
  public static String randString(int length) {
    return randString(length, Alphabet.getAllPrintableAsciiChars());
  }

  /**
   * @return a random string over the given alphabet
   * @see RandomCharGenerator
   */
  public static String randString(int length, String alphabet) {
    // can't use org.apache.commons.lang.RandomStringUtils here because this class
    // will be used client-side in GWT unit tests
    StringBuilder buf = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      buf.append(alphabet.charAt(rnd.nextInt(alphabet.length())));
    }
    return buf.toString();
  }

  /**
   * Returns a random <code>int</code> between lowerBound (inclusive) and
   * <code>upperBound</code> (exclusive) with roughly equal probability of
   * returning any particular <code>int</code> in this range.
   *
   * This is not a standard method provided by a typical RNG, we just provide it
   * for convenience here.
   *
   * @param lowerBound inclusive
   * @param upperBound exclusive
   */
  public static int nextIntInRange(int lowerBound, int upperBound) {
    if (lowerBound >= upperBound)
      throw new IllegalArgumentException("lowerBound < upperBound must be true");
    return rnd.nextInt(upperBound - lowerBound) + lowerBound;
  }

  /**
   * Returns a random {@code int} in range {@code [lowerBound, upperBound)} from the
   * given RNG.
   *
   * @param lowerBound inclusive
   * @param upperBound exclusive
   */
  public static int nextIntInRange(Random rnd, int lowerBound, int upperBound) {
    Assert.assertTrue(lowerBound < upperBound);
    return rnd.nextInt(upperBound - lowerBound) + lowerBound;
  }

  /**
   * Intended as a GWT-compatible replacement for {@link Collections#shuffle(List)}.
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
    if (sampleSize > collection.size())
      throw new IllegalArgumentException("Can't have a sample without replacement that's larger than the original list.");

    ArrayList<T> copy = new ArrayList<T>(collection);
    shuffle(copy);
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

  public static <T> T randomElement(T[] arr) {
    return arr[rnd.nextInt(arr.length)];
  }
}
