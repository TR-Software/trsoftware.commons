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

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.testutil.AssertUtils;
import solutions.trsoftware.commons.shared.util.mutable.MutableInteger;
import solutions.trsoftware.commons.shared.util.stats.HashCounter;
import solutions.trsoftware.commons.shared.util.stats.NumberSample;
import solutions.trsoftware.commons.shared.util.text.CharRange;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static solutions.trsoftware.commons.shared.util.RandomUtils.*;

/**
 * @author Alex, 11/1/2017
 */
public class RandomUtilsTest extends TestCase {

  /** Tests {@link RandomUtils#randString(int)} */
  public void testRandString() {
    for (int i = -5; i < 0; i++) {
      final int len = i;
      AssertUtils.assertThrows(IllegalArgumentException.class, (Runnable)() -> randString(len));
    }
    assertEquals("", randString(0));
    HashCounter<Character> counts = new HashCounter<>();
    for (int len = 1; len < 1000; len++) {
      String str = randString(len);
      assertEquals(len, str.length());
      for (int i = 0; i < str.length(); i++) {
        counts.increment(str.charAt(i));
      }
    }
    // check that all possible chars have been generated at this point (with roughly equal probability)
    assertEquals(StringUtils.ASCII_PRINTABLE_CHARS.length(), counts.size());
    System.out.println("randString(int) char frequencies: " + counts);
    assertEqualProbability(counts);
  }

  /** Tests {@link RandomUtils#randString(int, String)} */
  public void testRandString2() {
    final String alphabet = StringUtils.ASCII_LETTERS;
    for (int i = -5; i < 0; i++) {
      final int len = i;
      AssertUtils.assertThrows(IllegalArgumentException.class, (Runnable)() -> randString(len, alphabet));
    }
    assertEquals("", randString(0, alphabet));
    HashCounter<Character> counts = new HashCounter<>();
    for (int len = 1; len < 1000; len++) {
      String str = randString(len, alphabet);
      assertEquals(len, str.length());
      for (int i = 0; i < str.length(); i++) {
        counts.increment(str.charAt(i));
      }
    }
    // check that all possible chars have been generated at this point (with roughly equal probability)
    assertEquals(alphabet.length(), counts.size());
    System.out.println("randString(int, String) char frequencies: " + counts);
    assertEqualProbability(counts);
  }

  /**
   * Tests {@link RandomUtils#randString(String, int, int)}, which is a hybrid of {@link RandomUtils#randString(int, String)}
   * and {@link RandomUtils#nextIntInRange(int, int)}.
   */
  public void testRandString3() {
    final String alphabet = new CharRange('a', 'z').toString();
    for (int i = -5; i <= 0; i++) {
      for (int j = -5; j <= 0; j++) {
        final int minLen = i;
        final int maxLen = j;
        AssertUtils.assertThrows(IllegalArgumentException.class, (Runnable)() -> randString(alphabet, minLen, maxLen));
      }
    }
    assertEquals("", randString(0, alphabet));
    for (int minLen = 0; minLen < 10; minLen++) {
      int maxLen = minLen+6;
      HashCounter<Character> charCounts = new HashCounter<>();
      HashCounter<Integer> lenCounts = new HashCounter<>();
      int n = 10_000;
      for (int i = 0; i < n; i++) {
        String str = randString(alphabet, minLen, maxLen);
        lenCounts.increment(str.length());
        for (int c = 0; c < str.length(); c++) {
          charCounts.increment(str.charAt(c));
        }
      }
      String methodInfo = StringUtils.methodCallToString("randString", alphabet, minLen, maxLen);
      System.out.println(methodInfo + " char frequencies: " + charCounts);
      System.out.println(methodInfo + " length frequencies: " + lenCounts);
      // check that all possible chars have been generated at this point (with roughly equal probability)
      assertEquals(alphabet.length(), charCounts.size());
      assertEqualProbability(charCounts);
      assertEquals(maxLen - minLen + 1, lenCounts.size());
      assertEquals(n, lenCounts.sumOfAllEntries());
      assertEqualProbability(lenCounts);
    }
  }

  public void testNextIntInRange() {
    for (int lowerBound = -5; lowerBound < 5; lowerBound++) {
      for (int upperBound = lowerBound+1; upperBound < lowerBound+5; upperBound++) {
        HashCounter<Integer> counts = new HashCounter<>();
        int n = 100000;
        for (int i = 0; i < n; i++) {
          int x = nextIntInRange(lowerBound, upperBound);
          assertTrue(x >= lowerBound && x < upperBound);
          counts.increment(x);
        }
        assertEquals(upperBound - lowerBound, counts.size());
        assertEquals(n, counts.sumOfAllEntries());
        System.out.println("Freq[" + StringUtils.methodCallToString("nextIntInRange", lowerBound, upperBound) + "] = " + counts);
        assertEqualProbability(counts);
      }
    }
  }
  
  public void testNextDoubleInRange() {
    for (double lowerBound = -5; lowerBound < 5; lowerBound++) {
      for (double upperBound = lowerBound+1; upperBound < lowerBound+5; upperBound++) {
        Set<Double> uniques = new HashSet<>();
        int n = 1000;
        for (int i = 0; i < n; i++) {
          double x = nextDoubleInRange(lowerBound, upperBound);
          assertTrue(x >= lowerBound && x <= upperBound);
          uniques.add(x);
        }
        assertTrue(uniques.size() > n/2);
      }
    }
  }

  /** Asserts that all the keys in the given counter have roughly the same probability */
  public static <K> void assertEqualProbability(HashCounter<K> counter) {
    int n = counter.size();
    for (K k : counter.keySet()) {
      assertEquals(1d / n, counter.probabilityOf(k), 0.05);
    }
  }

  public void testRandomElement() {
    String[] arr = new String[]{"a", "b", "c", "d", "e", "f"};
    List<String> list = Arrays.asList(arr);
    RandomElementTester.forArray(arr).doTest();
    RandomElementTester.forList(list).doTest();
    RandomElementTester.forCollection(list).doTest();
    // now repeat the Collection version with some non-RandomAccess collections
    RandomElementTester.forCollection(new LinkedList<>(list)).doTest();
    RandomElementTester.forCollection(new HashSet<>(list)).doTest();
  }

  static abstract class RandomElementTester<T> {
    protected final int size;

    protected RandomElementTester(int size) {
      this.size = size;
    }

    /**
     * Invokes the appropriate overload of {@link RandomUtils#randomElement}
     */
    protected abstract T get();
    
    public void doTest() {
      HashCounter<T> counts = new HashCounter<T>();
      int n = 100000;
      for (int i = 0; i < n; i++) {
        counts.increment(get());
      }
      assertEquals(size, counts.size());
      assertEquals(n, counts.sumOfAllEntries());
      assertEqualProbability(counts);
    }
    
    public static <T> RandomElementTester<T> forArray(T[] input) {
      return new RandomElementTester<T>(input.length) {
        @Override
        protected T get() {
          return randomElement(input);
        }
      };
    }

    public static <T> RandomElementTester<T> forList(List<T> input) {
      return new RandomElementTester<T>(input.size()) {
        @Override
        protected T get() {
          return randomElement(input);
        }
      };
    }

    public static <T> RandomElementTester<T> forCollection(Collection<T> input) {
      return new RandomElementTester<T>(input.size()) {
        @Override
        protected T get() {
          return randomElement(input);
        }
      };
    }
  }


  public void testShuffle() {
    List<Integer> list = Arrays.asList(1, 2, 3, 4);
    // try several different list implementations
    checkListShuffling(list);
    checkListShuffling(new ArrayList<>(list));
    checkListShuffling(new LinkedList<>(list));
    checkListShuffling(new Vector<>(list));

    // try an empty list and a list with only 1 element
    checkListShuffling(Collections.emptyList());
    checkListShuffling(Collections.singletonList(1));
  }

  public void testRandomSampleWithoutReplacement() {
    // corner cases:
    assertEquals(Collections.<Integer>emptyList(), randomSampleWithoutReplacement(Collections.<Integer>emptyList(), 0));
    // Can't have a sample without replacement that's larger than the original list:
    AssertUtils.assertThrows(IllegalArgumentException.class,
        (Runnable)() -> randomSampleWithoutReplacement(Collections.<Integer>emptyList(), 1));
    AssertUtils.assertThrows(IllegalArgumentException.class,
        (Runnable)() -> randomSampleWithoutReplacement(Arrays.asList(1, 2), 3));

    final List<Integer> list = Arrays.asList(1, 2, 3, 4);
    for (int i = 0; i <= list.size(); i++) {
      final int r = i;
      checkPermutations(list, (int)MathUtils.nPr(list.size(), r), arg -> randomSampleWithoutReplacement(list, r), r);
    }
  }

  public void testRandomSampleWithReplacement() {
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
        randomSampleWithReplacement(Arrays.asList(1, 2), -1);
      }
    });

    final MutableInteger c = new MutableInteger(0);
    final List<Integer> list = Arrays.asList(1, 2, 3, 4);
    for (int i = 0; i <= list.size() + 1; i++) {  // going up to size+1 because samples without replacement can be larger than the original list
      final int r = i;
      checkPermutations(list, (int)Math.pow(list.size(), i), arg -> {
        c.incrementAndGet();
        return randomSampleWithReplacement(list, r);
      }, r);
    }
    System.out.println(c.get());
  }

  /**
   * @param list contains integers 1..n
   * @param nPermutations expected number of permutations
   */
  private static void checkPermutations(List<Integer> list, int nPermutations, Function<List<Integer>, List<Integer>> permuter, int sampleSize) {
    System.out.println("Permuting " + list.getClass().getName() + "(" + list + ")");

    // check that permuting generates all possible lists with roughly equal probability
    int iterations = nPermutations * 100; // do enough iterations to generate all possible permutations and smaller % differences
    HashCounter<List<Integer>> permutationCounts = new HashCounter<>(nPermutations);
    for (int i = 0; i < iterations; i++) {
      List<Integer> result = permuter.apply(list);
      // should still have the desired size
      assertEquals(sampleSize, result.size());
      // make a copy of the list to preserve its originality and increment the count for this permutation
      permutationCounts.increment(new ArrayList<>(result));
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
    HashCounter<List<Integer>> permutationCounts = new HashCounter<>(nPermutations);
    for (int i = 0; i < iterations; i++) {
      shuffle(list);
      // 1) should still have the same size and all the same elements
      assertEquals(n, list.size());
      // check that the list still contains integers 1..n
      for (int j = 1; j <= n; j++) {
        assertTrue(list.contains(j));
      }
      permutationCounts.increment(new ArrayList<>(list));  // make a copy of the list to preserve its originality
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

  public void testNextGaussian() {
    // make sure the distribution of the results matches our expected mean
    // 1) for the nextGaussian(double, double) -> double of the method:
    verifyGaussianDistribution(55d, 30d, RandomUtils::nextGaussian);
    // 2) for the nextGaussian(int, int) -> int version of the method:
    verifyGaussianDistribution(55, 30, RandomUtils::nextGaussian);
  }

  private <N extends Number & Comparable<N>> void verifyGaussianDistribution(N mean, N stdev, BiFunction<N, N, N> nextGaussianFcn) {
    int nSamples = 100000;
    NumberSample<N> sample = new NumberSample<>();
    System.out.println("Random gaussian numbers with mean=" + mean + " and stdev=" + stdev + ":");
    for (int i = 0; i < nSamples; i++) {
      N generatedNumber = nextGaussianFcn.apply(mean, stdev);
      // print every 1000th number
      if (i % 1000 == 0)
        System.out.println(generatedNumber);
      sample.update(generatedNumber);
    }
    // make sure that the generated numbers match the expected distribution +/- 1 unit
    assertEquals(mean.doubleValue(), sample.mean(), 1);
    assertEquals(stdev.doubleValue(), sample.stdev(), 1);
  }


}