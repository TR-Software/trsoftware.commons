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

import junit.framework.TestCase;
import solutions.trsoftware.commons.client.testutil.AssertUtils;
import solutions.trsoftware.commons.shared.util.callables.Function1;
import solutions.trsoftware.commons.shared.util.mutable.MutableInteger;
import solutions.trsoftware.commons.shared.util.stats.HashCounter;

import java.util.*;

import static solutions.trsoftware.commons.shared.util.RandomUtils.*;

/**
 * @author Alex, 11/1/2017
 */
public class RandomUtilsTest extends TestCase {

  /** Tests {@link RandomUtils#randString(int)} */
  public void testRandString() throws Exception {
    fail("TODO"); // TODO
  }

  /** Tests {@link RandomUtils#randString(int, String)} */
  public void testRandStringWithAlphabet() throws Exception {
    fail("TODO"); // TODO
  }

  public void testNextIntInRange() throws Exception {
    fail("TODO"); // TODO
  }

  public void testRandomElement() throws Exception {
    HashCounter<String> elementCount = new HashCounter<String>();
    String[] arr = new String[]{"a", "b", "c", "d", "e", "f"};
    int n = 100000;
    for (int i = 0; i < n; i++) {
      elementCount.increment(randomElement(arr));
    }
    assertEquals(6, elementCount.size());
    assertEquals(n, elementCount.sumOfAllEntries());
    for (String elt : arr) {
      assertEquals(1d / 6, (double)elementCount.get(elt) / n, 0.01);
    }
  }


  public void testShuffle() throws Exception {
    List<Integer> list = Arrays.asList(1, 2, 3, 4);
    // try several different list implementations
    checkListShuffling(list);
    checkListShuffling(new ArrayList<Integer>(list));
    checkListShuffling(new LinkedList<Integer>(list));
    checkListShuffling(new Vector<Integer>(list));

    // try an empty list and a list with only 1 element
    checkListShuffling(Arrays.<Integer>asList());
    checkListShuffling(Arrays.asList(1));
  }

  public void testRandomSampleWithoutReplacement() throws Exception {
    // corner cases:
    assertEquals(Collections.<Integer>emptyList(), randomSampleWithoutReplacement(Arrays.<Integer>asList(), 0));
    // Can't have a sample without replacement that's larger than the original list:
    AssertUtils.assertThrows(IllegalArgumentException.class, new Runnable() {
      public void run() {
        randomSampleWithoutReplacement(Arrays.<Integer>asList(), 1);
      }
    });
    AssertUtils.assertThrows(IllegalArgumentException.class, new Runnable() {
      public void run() {
        randomSampleWithoutReplacement(Arrays.<Integer>asList(1, 2), 3);
      }
    });

    final List<Integer> list = Arrays.asList(1, 2, 3, 4);
    for (int i = 0; i <= list.size(); i++) {
      final int r = i;
      checkPermutations(list, (int)MathUtils.nPr(list.size(), r), new Function1<List<Integer>, List<Integer>>() {
        public List<Integer> call(List<Integer> arg) {
          return randomSampleWithoutReplacement(list, r);
        }
      }, r);
    }
  }

  public void testRandomSampleWithReplacement() throws Exception {
    // corner cases:
    assertEquals(Collections.<Integer>emptyList(), randomSampleWithReplacement(Arrays.<Integer>asList(), 0));
    // Can't have a negative sample size
    AssertUtils.assertThrows(IllegalArgumentException.class, new Runnable() {
      public void run() {
        randomSampleWithReplacement(Arrays.<Integer>asList(), -1);
      }
    });
    AssertUtils.assertThrows(IllegalArgumentException.class, new Runnable() {
      public void run() {
        randomSampleWithReplacement(Arrays.<Integer>asList(1, 2), -1);
      }
    });

    final MutableInteger c = new MutableInteger(0);
    final List<Integer> list = Arrays.asList(1, 2, 3, 4);
    for (int i = 0; i <= list.size() + 1; i++) {  // going up to size+1 because samples without replacement can be larger than the original list
      final int r = i;
      checkPermutations(list, (int)Math.pow(list.size(), i), new Function1<List<Integer>, List<Integer>>() {
        public List<Integer> call(List<Integer> arg) {
          c.incrementAndGet();
          return randomSampleWithReplacement(list, r);
        }
      }, r);
    }
    System.out.println(c.get());
  }

  /**
   * @param list contains integers 1..n
   * @param nPermutations expected number of permutations
   */
  private static void checkPermutations(List<Integer> list, int nPermutations, Function1<List<Integer>, List<Integer>> permuter, int sampleSize) {
    System.out.println("Permuting " + list.getClass().getName() + "(" + list + ")");

    // check that permuting generates all possible lists with roughly equal probability
    int iterations = nPermutations * 100; // do enough iterations to generate all possible permutations and smaller % differences
    HashCounter<List<Integer>> permutationCounts = new HashCounter<List<Integer>>(nPermutations);
    for (int i = 0; i < iterations; i++) {
      List<Integer> result = permuter.call(list);
      // should still have the desired size
      assertEquals(sampleSize, result.size());
      // make a copy of the list to preserve its originality and increment the count for this permutation
      permutationCounts.increment(new ArrayList<Integer>(result));
    }

    // make sure that every permutation was generated
    assertEquals(nPermutations, permutationCounts.size());
    // make sure each perm was generated with approx. equal probability
    double expectedPct = ((double)iterations / (double)nPermutations) / (double)iterations;
    for (Map.Entry<List<Integer>, Integer> entry : permutationCounts.entriesSortedByKeyAscending()) {
      int count = entry.getValue();
      double pct = (double)count / iterations;
      System.out.println(entry.getKey() + ": " + entry.getValue() + " (" + (pct * 100) + "%); expected " + (expectedPct * 100) + "%");
      assertEquals(expectedPct, pct, expectedPct / 2); // assert the pct accuracy with 50% leeway
    }
    System.out.println("");
  }


  /** @param list contains integers 1..n */
  private void checkListShuffling(List<Integer> list) {
    System.out.println("Shuffling " + list.getClass().getName() + "(" + list + ")");

    final int n = list.size();
    // check that shuffling generates all possible lists with roughly equal probability
    int nPermutations = (int)MathUtils.nPr(n, n);
    int iterations = nPermutations * 1000; // do enough iterations to generate all possible permutations and smaller % differences
    HashCounter<List<Integer>> permutationCounts = new HashCounter<List<Integer>>(nPermutations);
    for (int i = 0; i < iterations; i++) {
      shuffle(list);
      // 1) should still have the same size and all the same elements
      assertEquals(n, list.size());
      // check that the list still contains integers 1..n
      for (int j = 1; j <= n; j++) {
        assertTrue(list.contains(j));
      }
      permutationCounts.increment(new ArrayList<Integer>(list));  // make a copy of the list to preserve its originality
    }

    // make sure that every permutation was generated
    assertEquals(nPermutations, permutationCounts.size());
    // make sure each perm was generated with approx. equal probability
    double expectedPct = ((double)iterations / (double)nPermutations) / (double)iterations;
    for (Map.Entry<List<Integer>, Integer> entry : permutationCounts.entriesSortedByKeyAscending()) {
      int count = entry.getValue();
      double pct = (double)count / iterations;
      System.out.println(entry.getKey() + ": " + entry.getValue() + " (" + (pct * 100) + "%)");
      assertEquals(expectedPct, pct, .02);
    }
    System.out.println("");
  }

  public void testNextGaussian() throws Exception {
    System.out.println("Random Gaussian:");
    for (int i = 0; i < 20; i++) {
      System.out.println(rnd.nextGaussian());
    }
    System.out.println();
    printGaussians(50, 25);
    System.out.println();
    printGaussians(5000, 5000/2);
    fail("TODO"); // TODO verify some assertions
  }

  private void printGaussians(double mean, double stdev) {
    System.out.println(StringUtils.methodCallToString("nextGaussian", mean, stdev));
    for (int i = 0; i < 20; i++) {
      System.out.println(nextGaussian(mean, stdev));
    }
  }

  private void printGaussians(int mean, int stdev) {
    System.out.println(StringUtils.methodCallToString("nextGaussian", mean, stdev));
    for (int i = 0; i < 20; i++) {
      System.out.println(MathUtils.restrict((int)nextGaussian(mean, stdev), 0, Integer.MAX_VALUE));
    }
  }

}