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

import junit.framework.TestCase;
import solutions.trsoftware.commons.bridge.BridgeTypeFactory;
import solutions.trsoftware.commons.shared.annotations.Slow;
import solutions.trsoftware.commons.shared.testutil.AssertUtils;
import solutions.trsoftware.commons.shared.util.callables.Function3_;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static solutions.trsoftware.commons.shared.testutil.AssertUtils.*;
import static solutions.trsoftware.commons.shared.util.Levenshtein.*;
import static solutions.trsoftware.commons.shared.util.RandomUtils.rnd;
import static solutions.trsoftware.commons.shared.util.StringUtils.methodCallToString;

/**
 * Mar 14, 2011
 *
 * @author Alex
 */
public class LevenshteinTest extends TestCase {

  public void testStripCommonPrefix() throws Exception {
    // when there is not common prefix the method shouldn't change anything
    {
      String[] arr = {"", ""};
      assertEquals(0, stripCommonPrefix(arr));
      assertEquals("", arr[0]);
      assertEquals("", arr[1]);
    }
    {
      String[] arr = {"foo", ""};
      assertEquals(0, stripCommonPrefix(arr));
      assertEquals("foo", arr[0]);
      assertEquals("", arr[1]);
    }
    {
      String[] arr = {"", "bar"};
      assertEquals(0, stripCommonPrefix(arr));
      assertEquals("", arr[0]);
      assertEquals("bar", arr[1]);
    }
    {
      String[] arr = {"foo", "bar"};
      assertEquals(0, stripCommonPrefix(arr));
      assertEquals("foo", arr[0]);
      assertEquals("bar", arr[1]);
    }
    // when there is a prefix, it should be stripped from both args in the array, and its length returned
    {
      String[] arr = {"afoo", "abar"};
      assertEquals(1, stripCommonPrefix(arr));
      assertEquals("foo", arr[0]);
      assertEquals("bar", arr[1]);
    }
    // when there is a prefix, it should be stripped from both args in the array, and its length returned
    {
      String[] arr = {"ab foo", "ab bar"};
      assertEquals(3, stripCommonPrefix(arr));
      assertEquals("foo", arr[0]);
      assertEquals("bar", arr[1]);
    }
  }

  public void testStripCommonSuffix() throws Exception {
    // when there is not common suffix the method shouldn't change anything
    {
      String[] arr = {"", ""};
      assertEquals(0, stripCommonSuffix(arr));
      assertEquals("", arr[0]);
      assertEquals("", arr[1]);
    }
    {
      String[] arr = {"foo", ""};
      assertEquals(0, stripCommonSuffix(arr));
      assertEquals("foo", arr[0]);
      assertEquals("", arr[1]);
    }
    {
      String[] arr = {"", "bar"};
      assertEquals(0, stripCommonSuffix(arr));
      assertEquals("", arr[0]);
      assertEquals("bar", arr[1]);
    }
    {
      String[] arr = {"foo", "bar"};
      assertEquals(0, stripCommonSuffix(arr));
      assertEquals("foo", arr[0]);
      assertEquals("bar", arr[1]);
    }
    // when there is a suffix, it should be stripped from both args in the array, and its length returned
    {
      String[] arr = {"fooa", "bara"};
      assertEquals(1, stripCommonSuffix(arr));
      assertEquals("foo", arr[0]);
      assertEquals("bar", arr[1]);
    }
    // when there is a suffix, it should be stripped from both args in the array, and its length returned
    {
      String[] arr = {"fooab ", "barab "};
      assertEquals(3, stripCommonSuffix(arr));
      assertEquals("foo", arr[0]);
      assertEquals("bar", arr[1]);
    }
  }

  public void testEditDistance() throws Exception {
    // These examples are from org.apache.commons.lang3.StringUtilsTest.java
    checkDistance(0, "", "");
    checkDistance(1, "", "a");
    checkDistance(7, "aaapppp", "");
    checkDistance(1, "frog", "fog");
    checkDistance(3, "fly", "ant");
    checkDistance(7, "elephant", "hippo");
    checkDistance(7, "hippo", "elephant");
    checkDistance(8, "hippo", "zzzzzzzz");
    checkDistance(8, "zzzzzzzz", "hippo");
    checkDistance(1, "hello", "hallo");
    assertThrows(NullPointerException.class, (Runnable)() ->
        editDistance("a", null));
    assertThrows(NullPointerException.class, (Runnable)() ->
        editDistance(null, "a"));
  }


  public void testEditDistanceIncremental() throws Exception {
    // first, try the same examples as for computing the normal edit distance
    checkEditDistanceIncremental(0, "", "");
    checkEditDistanceIncremental(1, "", "a");
    checkEditDistanceIncremental(7, "aaapppp", "");
    checkEditDistanceIncremental(1, "frog", "fog");
    checkEditDistanceIncremental(3, "fly", "ant");
    checkEditDistanceIncremental(7, "elephant", "hippo");
    checkEditDistanceIncremental(7, "hippo", "elephant");
    checkEditDistanceIncremental(8, "hippo", "zzzzzzzz");
    checkEditDistanceIncremental(8, "zzzzzzzz", "hippo");
    checkEditDistanceIncremental(1, "hello", "hallo");
    assertThrows(NullPointerException.class, (Runnable)() ->
        editDistanceIncremental("a", null, null));
    assertThrows(NullPointerException.class, (Runnable)() ->
        editDistanceIncremental(null, "a", null));
    // now try continuing the incremental computations
    {
      IncrementalEditDistanceResult result0 = checkEditDistanceIncremental(1, "hello", "hallo");
      IncrementalEditDistanceResult r1 = editDistanceIncremental("hello", "hallor", result0);
      assertEquals(2, r1.editDistance);
      IncrementalEditDistanceResult r2 = editDistanceIncremental("hello", "hallora", r1);
      assertEquals(3, r2.editDistance);
      IncrementalEditDistanceResult r3 = editDistanceIncremental("hellor", "hallora", r2);
      assertEquals(2, r3.editDistance);
      IncrementalEditDistanceResult r4 = editDistanceIncremental("hellora", "hallora", r3);
      assertEquals(1, r4.editDistance);
    }
    // try an example where the second string is longer than the first (the algorithm will swap them to use less memory for the row arrays)
    {
      IncrementalEditDistanceResult result0 = checkEditDistanceIncremental(2, "hell", "hallo");
      // check that s and t got swapped in the result
      assertEquals("hallo", result0.s);
      assertEquals("hell", result0.t);
      IncrementalEditDistanceResult r1 = editDistanceIncremental("hallo", "hello", result0);
      assertEquals(1, r1.editDistance);
      IncrementalEditDistanceResult r2 = editDistanceIncremental("hallora", "hello", r1);
      assertEquals(3, r2.editDistance);
      IncrementalEditDistanceResult r3 = editDistanceIncremental("hallorathon", "hello world", r2);
      assertEquals(7, editDistance("hallorathon", "hello world"));  // make sure the non-incremental algorithm gets the same result
      assertEquals(7, r3.editDistance);
    }
  }

  @Slow
  public void testEditDistanceIncrementalPerformance() throws Exception {
    // generate strings that are long-enough to make an impact on performance in JVM but not take too long to compute
    String s = StringUtils.randString(14000);
    String t =  StringUtils.randString(10000);
    checkEditDistanceIncrementalPerformance(s, t);
  }

  void checkEditDistanceIncrementalPerformance(String s, String t) throws Exception {
    // make sure that working incrementally from a prior result is actually faster than computing edit distance from scratch
    String s0 = s.substring(0, s.length() / 2);
    String t0 = t.substring(0, t.length() / 2);
    IncrementalEditDistanceResult partialResult = editDistanceIncremental(s0, t0, null);
    // validate the partial result just in case
    assertEquals(editDistance(s0, t0), partialResult.editDistance);
    // now time both the incremental and the full computation
    int fullDistance;
    long fullElapsed;
    {
      Duration fullDuration = BridgeTypeFactory.newDuration(StringUtils.methodCallToString("editDistance", s.length(), t.length()));
      fullDistance = editDistance(s, t);
      fullElapsed = (long)fullDuration.elapsedMillis();
      System.out.println(fullDuration);
    }
    Duration incDuration = BridgeTypeFactory.newDuration(StringUtils.methodCallToString("editDistanceIncremental", s.length(), t.length()));
    IncrementalEditDistanceResult finalResult = editDistanceIncremental(s, t, partialResult);
    long incElapsed = (long)incDuration.elapsedMillis();
    System.out.println(incDuration);
    assertEquals(fullDistance, finalResult.editDistance);
    assertTrue(incElapsed < fullElapsed);
  }


  private IncrementalEditDistanceResult checkEditDistanceIncremental(int expectedDistance, String s, String t) {
    IncrementalEditDistanceResult result = checkEditDistanceIncrementalHelper(expectedDistance, s, t);
    // try swapping the arguments to just to check for symmetry
    checkEditDistanceIncrementalHelper(expectedDistance, t, s);
    return result;
  }

  private IncrementalEditDistanceResult checkEditDistanceIncrementalHelper(final int expectedDistance, final String s, final String t) {
    System.out.println("Calling " + methodCallToString("editDistanceIncremental", s, t));
    IncrementalEditDistanceResult result = editDistanceIncremental(s, t, null);
    System.out.println("Got editDistance=" + result.editDistance + ", data=" + Arrays.toString(result.d));
    assertEquals(expectedDistance, result.editDistance);
    return result;
  }


  /** Compares performance of editDistance vs. editSequence vs. diff */
  public void testEditDistanceVsSequenceVsDiffs() throws Exception {
    String s = randString(100, 200);
    String t = randString(300, 400);
    String m1 = StringUtils.methodCallToString("editDistance", s.length(), t.length());
    String m2 = StringUtils.methodCallToString("editSequence", t.length(), s.length());
    String m3 = StringUtils.methodCallToString("diff", s.length(), t.length());
    {
      String msg = m1;
      Duration d = BridgeTypeFactory.newDuration(msg);
      System.out.println(msg + " = " + editDistance(t, s) + " " + d);
    }
    {
      String msg = m2;
      Duration d = BridgeTypeFactory.newDuration(msg);
      System.out.println(msg + " = " + editSequence(s, t).length() + " " + d);
    }

    System.out.println("");
    {
      String msg = m3;
      Duration d = BridgeTypeFactory.newDuration(msg);
      System.out.println(msg + " = " + diff(t, s).editDistance() + " " + d);
    }
  }


  public void testEditSequence() throws Exception {
    // 1) test editSequence(String, String)
    // These examples are from org.apache.commons.lang3.StringUtilsTest.java
    checkSequence(0, "", "");
    checkSequence(1, "", "a");
    checkSequence(3, "bar", "f");
    checkSequence(7, "aaapppp", "");
    checkSequence(1, "frog", "fog");
    checkSequence(3, "fly", "ant");
    checkSequence(7, "elephant", "hippo");
    checkSequence(7, "hippo", "elephant");
    checkSequence(8, "hippo", "zzzzzzzz");
    checkSequence(8, "zzzzzzzz", "hippo");
    checkSequence(1, "hello", "hallo");
    assertThrows(NullPointerException.class, (Runnable)() -> editSequence("a", null));
    assertThrows(NullPointerException.class, (Runnable)() -> editSequence(null, "a"));
    // TODO: figure out why some of the editSequence calls are returning +(1,'o') instead of +(2,'o') for ("Fo", "Foo")
    checkSequence(1, "Fo", "Foo");
  }

  private void checkSequence(int expectedDistance, String s, String t) {
    checkSequenceHelper(expectedDistance, s, t);
    // try swapping the arguments to check for symmetry
    checkSequenceHelper(expectedDistance, t, s);
  }

  private void checkSequenceHelper(final int expectedDistance, final String s, final String t) {
    callAllPermutations(new Function3_<Boolean, Boolean, Boolean>() {
      public void call(Boolean useOptimizations, Boolean a1, Boolean a2) {
        EditSequence editSequence;
        if (useOptimizations) {
          // use the optimized version of the algorithm
          System.out.println(methodCallToString("editSequence", s, t, a1, a2) + ":");
          editSequence = editSequence(s, t, a1, a2);
        }
        else {
          // use the default version of the algorithm
          System.out.println(methodCallToString("editSequence", s, t) + ":");
          editSequence = editSequence(s, t);
        }
        System.out.println("  " + editSequence);
        assertEquals(expectedDistance, editSequence.length());
        assertEquals(t, editSequence.apply(s));
      }
    });
  }

  public void testLongStrings() throws Exception {
    // generate strings that are long-engough to make an impact on performance in JVM but not take too long to compute
    String s = StringUtils.randString(1000);
    String t =  StringUtils.randString(700);
    checkLongStrings(s, t);
  }

  void checkLongStrings(String s, String t) throws Exception {
    assertDistanceMatchesSequence(s, t);
    // try swapping the arguments to check for symmetry
    assertDistanceMatchesSequence(t, s);
    System.out.println();  // empty line
  }

  private void assertDistanceMatchesSequence(final String s, final String t) {
    callAllPermutations(new Function3_<Boolean, Boolean, Boolean>() {
      public void call(Boolean useOptimizations, Boolean a1, Boolean a2) {
        EditSequence editSequence;
        int editDistance;
        if (useOptimizations) {
          // use the optimized version of the algorithm
          {
            String debugInfo = methodCallToString("editDistance", s, t, a1, a2);
            System.out.println("Calling " + debugInfo);
            Duration dt = BridgeTypeFactory.newDuration(debugInfo);
            editDistance = editDistance(s, t, a1, a2);
            System.out.println(dt);
          }
          {
            String debugInfo = methodCallToString("editSequence", s, t, a1, a2);
            System.out.println("Calling " + debugInfo);
            Duration dt = BridgeTypeFactory.newDuration(debugInfo);
            editSequence = editSequence(s, t, a1, a2);
            System.out.println(dt);
          }
        }
        else {
          // use the default version of the algorithm
          {
            String debugInfo = methodCallToString("editDistance", s, t);
            System.out.println("Calling " + debugInfo);
            Duration dt = BridgeTypeFactory.newDuration(debugInfo);
            editDistance = editDistance(s, t);
            System.out.println(dt);
          }
          {
            String debugInfo = methodCallToString("editSequence", s, t);
            System.out.println("Calling " + debugInfo);
            Duration dt = BridgeTypeFactory.newDuration(debugInfo);
            editSequence = editSequence(s, t);
            System.out.println(dt);
          }
        }
        System.out.println("distance(s, t) = " + editDistance);
        System.out.println("sequence(s, t) = " + editSequence);
        assertEquals(editDistance, editSequence.length());
        assertEquals(t, editSequence.apply(s));
      }
    });
  }

  private String randString(int minLength, int maxLength) {
    if (maxLength == minLength)
      return "";
    return StringUtils.randString(RandomUtils.nextIntInRange(minLength, maxLength));
  }

  private void checkDistance(int expectedDistance, String s, String t) {
    checkDistanceHelper(expectedDistance, s, t);
    // try swapping the arguments to check for symmetry
    checkDistanceHelper(expectedDistance, t, s);
  }

  private void checkDistanceHelper(final int expectedDistance, final String s, final String t) {
    callAllPermutations(new Function3_<Boolean, Boolean, Boolean>() {
      public void call(Boolean useOptimizations, Boolean a1, Boolean a2) {
        int editDistance;
        if (useOptimizations) {
          // use the optimized version of the algorithm
          System.out.println("Calling " + methodCallToString("editDistance", s, t, a1, a2));
          editDistance = editDistance(s, t, a1, a2);
        }
        else {
          // use the default version of the algorithm
          System.out.println("Calling " + methodCallToString("editDistance", s, t));
          editDistance = editDistance(s, t);
        }
        System.out.println("Got the following edit distance:\n" + editDistance);
        assertEquals(expectedDistance, editDistance);
      }
    });
  }

  /**
   * Calls a function which takes 3 boolean arguments using all possible
   * permutations of those arguments with the only exception being that a2 and
   * a3 are used only if a1 is true
   */
  private void callAllPermutations(Function3_<Boolean, Boolean, Boolean> callable) {
    boolean[] bools = new boolean[]{false, true};
    for (boolean a1 : bools) {
      if (a1) {
        for (boolean a2 : bools) {
          for (boolean a3 : bools) {
            callable.call(a1, a2, a3);
          }
        }
      }
      else {
        callable.call(a1, false, false);
      }
    }
  }

  public void testDiffHelperAddDiffsNoCS() throws Exception {
    checkAddDiffsNoCS("", "", "[]");
    checkAddDiffsNoCS("", "a", "[+a]");
    checkAddDiffsNoCS("", "ab", "[+ab]");
    checkAddDiffsNoCS("a", "bc", "[$b, +c]");
    checkAddDiffsNoCS("ad", "bc", "[$bc]");
    checkAddDiffsNoCS("ad", "bce", "[$bc, +e]");
    checkAddDiffsNoCS("ade", "b", "[$b, -de]");
    checkAddDiffsNoCS("ade", "", "[-ade]");
    checkAddDiffsNoCS("a", "", "[-a]");
  }

  public void checkAddDiffsNoCS(String s, String t, String expectedSequence) throws Exception {
    String result = diffHelperAddDiffsNoCS(new ArrayList<TextRun>(), s, t).toString();
    System.out.println(StringUtils.methodCallToString("diffHelperAddDiffsNoCS", s, t) + " = " + result);
    assertEquals(expectedSequence, result);
  }

  public void testDiffHelperUnmergedDiffsGivenLCS() throws Exception {
    checkUnmergedDiffsGivenLCS("frabcodo", "0a12b345", "[$0, -r, =a, +12, =b, $345, -o]");
    checkUnmergedDiffsGivenLCS("abc", "a12b345", "[=a, +12, =b, $3, +45]");
    checkUnmergedDiffsGivenLCS("abcd", "a1b", "[=a, +1, =b, -cd]");
    checkUnmergedDiffsGivenLCS("abcd", "ab", "[=a, =b, -cd]");
  }

  public void checkUnmergedDiffsGivenLCS(String s, String t, String expectedSequence) throws Exception {
    String result = diffHelperUnmergedDiffsGivenLCS(s, t, "ab").toString();
    System.out.println(StringUtils.methodCallToString("diffHelperUnmergedDiffsGivenLCS", s, t) + " = " + result);
    assertEquals(expectedSequence, result);
  }


  public void testDiff() throws Exception {
    // check the same inputs as above, but make sure the resulting sequences are merged
    checkDiff("frabcodo", "0a12b345", "[$0, -r, =a, +12, =b, $345, -o]");
    checkDiff("abc", "a12b345", "[=a, +12, =b, $3, +45]");
    checkDiff("abcd", "a1b", "[=a, +1, =b, -cd]");
    checkDiff("abcd", "ab", "[=ab, -cd]");
  }

  public void checkDiff(String s, String t, String expectedSequence) throws Exception {
    Diffs diffs = diff(s, t);
    String resultStr = diffs.toString();
    System.out.println(StringUtils.methodCallToString("diff", s, t) + " = " + resultStr);
    assertEquals(expectedSequence, resultStr);
    // count the number of non-matching chars in the resulting diff sequence,
    // and compare it with the edit distance (they should be equal)
    int diffDistance = diffs.editDistance();
    System.out.println("edit distance derived from the resulting diff = " + diffDistance);
    int levenshteinDistance = editDistance(s, t);
    System.out.println(StringUtils.methodCallToString("editDistance", s, t) + " = " + levenshteinDistance);
    assertEquals(levenshteinDistance, diffDistance);
    System.out.println();
  }

  public void testLongestCommonSubsequence() throws Exception {
    assertEquals("ab", longestCommonSubsequence("frabcodo", "0a12b345"));
    assertEquals("ab", longestCommonSubsequence("abc", "a12b345"));
    assertEquals("ab", longestCommonSubsequence("abcd", "a1b"));
    assertEquals("ab", longestCommonSubsequence("abcd", "ab"));
    assertEquals("abcdfgjz", longestCommonSubsequence("abcdfghjqz", "abcdefgijkrxyz"));
    // now do a real-word example:
    assertEquals("The ", longestCommonSubsequence("The ", "The recipe for turning fruit into wine goes something like this: 1. Pick a large quantity of ripe grapes from grapevines (you could substitute raspberries or any other fruit, but 99.9 percent of all the wine in the world is made from grapes, because they make the best wines). 2. Put the grapes into a clean container that doesn't leak. 3. Crush the grapes somehow to release their juice. (Once upon a time, feet performed this step). 4. Wait."));

  }

  public void testIsSubsequence() throws Exception {
    assertTrue(isSubsequence("", ""));
    assertTrue(isSubsequence("", "a"));
    assertFalse(isSubsequence("a", ""));
    assertTrue(isSubsequence("a", "a"));
    assertTrue(isSubsequence("a", "ab"));
    assertTrue(isSubsequence("ab", "ab"));
    assertTrue(isSubsequence("a", "abc"));
    assertTrue(isSubsequence("ab", "abc"));
    assertTrue(isSubsequence("abc", "abc"));
  }

  @Slow
  public void testRandomStrings() throws Exception {
    // test the distance and sequence methods random strings with all lengths up to 20
    for (int i = 0; i < 20; i++) {
      for (int j = 0; j < 20; j++) {
        checkRandomStrings(i, j);
      }
    }
  }

  private void checkRandomStrings(int l1, int l2) {
    String s = randString(l1 / 2, l1);
    String t = randString(l2 / 2, l2);
    assertDistanceMatchesSequence(s, t);
    // try swapping the arguments to check for symmetry
    assertDistanceMatchesSequence(t, s);
    System.out.println();  // empty line
  }


  // test the internal data structures

  /**
   * Tests {@link LinkedEditSequence}
   */
  public void testLinkedEditSequence() throws Exception {
    // create a list of n random edit ops, which will be used to construct a recursive LinkedEditSequence instances
    int n = 100;
    ArrayList<EditOperation> ops = Stream.generate(LevenshteinTest::randomEditOp).limit(n).collect(Collectors.toCollection(ArrayList::new));
    // at the base of the chain, we'll start with an ImmutableEditSequence of the first 10 ops
    int i = 10;
    ImmutableEditSequence base = new ImmutableEditSequence(ops.subList(0, i));
    assertEquals(ops.subList(0, i), base.getOperations());
    LinkedEditSequence seq = new LinkedEditSequence(base, ops.get(i++));
    assertEquals(i, seq.length());
    while (i < n) {
      seq = new LinkedEditSequence(seq, ops.get(i++));
      assertEquals(i, seq.length());
    }
    // verify that LinkedEditSequence.getOperations() returns the full sequence of edit ops
    assertEquals(ops, seq.getOperations());
  }


  /**
   * @return an arbitrary {@link EditOperation} with a random position value between 0 and 10, and a random alphanumeric char.
   */
  private static EditOperation randomEditOp() {
    String chars = StringUtils.ASCII_LETTERS_AND_NUMBERS;
    char c = chars.charAt(rnd.nextInt(chars.length()));
    int pos = rnd.nextInt(10);
    switch (rnd.nextInt(3)) {
      case 0:
        return new Insertion(pos, c);
      case 1:
        return new Deletion(pos, c);
      case 2:
        return new Substitution(pos, c);
      default:
        throw new IllegalArgumentException();
    }
  }


}