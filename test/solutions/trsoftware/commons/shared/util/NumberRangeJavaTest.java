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
import solutions.trsoftware.commons.shared.annotations.Slow;

import java.util.*;

import static solutions.trsoftware.commons.shared.util.NumberRange.*;
import static solutions.trsoftware.commons.shared.util.iterators.IteratorTestCase.assertIteratedElements;

public class NumberRangeJavaTest extends TestCase {

  private Random rnd = new Random();
  private int numValidRangesToTest = 1000;
  private int numRandomsWithinRange = 5000;  // should be enough to generate every number within a range

  public void testCornerCases() throws Exception {
    assertEquals(4.5d, new NumberRange<Double>(4.5d, 4.5d).randomDouble());
    assertEquals(4.5d, new NumberRange<Float>(4.5f, 4.5f).randomDouble());
    assertEquals(5d, new NumberRange<Integer>(5, 5).randomDouble());
    assertEquals(5d, new NumberRange<Long>(5L, 5L).randomDouble());
  }

  public void testRandomDoubleFromFloatRanges() throws Exception {
    int i = 0;
    while (i < numValidRangesToTest) {
      float min = rnd.nextFloat() * 20;
      float max = rnd.nextFloat() * 20;

      NumberRange<Float> range = null;
      try {
        range = new NumberRange<Float>(min, max);
        assertTrue(min <= max);  // otherwise exception would have been thrown
      }
      catch (IllegalArgumentException ex) {
        assertTrue(min > max);
        continue;
      }

      // so now we have a valid range
      i++;
      System.out.println("range = " + range);

      // test a bunch of randoms in this range and make sure they're indeed in range
      for (int j = 0; j < numRandomsWithinRange; j++) {
        double random = range.randomDouble();
        assertTrue(random >= min && random <= max);
      }
    }
  }

  public void testRandomDoubleFromIntegerRanges() throws Exception {
    int i = 0;
    while (i < numValidRangesToTest) {
      int min = rnd.nextInt(100);
      int max = rnd.nextInt(100);

      NumberRange<Integer> range = null;
      try {
        range = new NumberRange<Integer>(min, max);
        assertTrue(min <= max);  // otherwise exception would have been thrown
      }
      catch (IllegalArgumentException ex) {
        assertTrue(min > max);
        continue;
      }

      // so now we have a valid range
      i++;
      System.out.println("range = " + range);

      // test a bunch of randoms in this range and make sure they're indeed in range
      for (int j = 0; j < numRandomsWithinRange; j++) {
        double random = range.randomDouble();
        assertTrue(random >= min && random <= max);
      }
    }
  }

  @Slow
  public void testRandomIntegerFromFloatRanges() throws Exception {
    int i = 0;
    while (i < numValidRangesToTest) {
      float min, max;

      // make sure the range is wide enough to accommodate more than one integer
      do {
        min = rnd.nextFloat() * 20;
        max = rnd.nextFloat() * 20;
      }
      while (Math.abs(max - min) < 2);

      NumberRange<Float> range = null;
      try {
        range = new NumberRange<Float>(min, max);
        assertTrue(min <= max);  // otherwise exception would have been thrown
      }
      catch (IllegalArgumentException ex) {
        assertTrue(min > max);
        continue;
      }

      // so now we have a valid range
      i++;
      System.out.println("range = " + range);

      // test a bunch of randoms in this range and make sure they're indeed in range
      for (int j = 0; j < numRandomsWithinRange; j++) {
        int random = range.randomInt();
        assertTrue(random >= min && random <= max);
      }
    }
  }

  @Slow
  public void testRandomIntegerFromIntegerRanges() throws Exception {
    int i = 0;
    while (i < numValidRangesToTest) {
      int min, max;
      // make sure the range is wide enough to accommodate more than one integer
      do {
        min = rnd.nextInt(100);
        max = rnd.nextInt(100);
      }
      while (Math.abs(max - min) < 2);

      NumberRange<Integer> range = null;
      try {
        range = new NumberRange<Integer>(min, max);
        assertTrue(min <= max);  // otherwise exception would have been thrown
      }
      catch (IllegalArgumentException ex) {
        assertTrue(min > max);
        continue;
      }

      // so now we have a valid range
      i++;
      System.out.println("range = " + range);

      Set<Integer> uniques = new HashSet<Integer>();
      // test a bunch of randoms in this range and make sure they're indeed in range
      for (int j = 0; j < numRandomsWithinRange; j++) {
        int random = range.randomInt();
        assertTrue(random >= min && random <= max);
        uniques.add(random);
      }

      // make sure that all possible ints within this range were generated
      assertEquals(max - min + 1, uniques.size());
    }
  }


  public void testContains() throws Exception {
    NumberRange<Integer> range = new NumberRange<Integer>(5, 8);
    assertTrue(range.contains(5));
    assertTrue(range.contains(6));
    assertTrue(range.contains(7));
    assertTrue(range.contains(8));
    assertFalse(range.contains(4));
    assertFalse(range.contains(9));

    range = new NumberRange<Integer>(-2, 2);
    assertTrue(range.contains(-2));
    assertTrue(range.contains(-1));
    assertTrue(range.contains(0));
    assertTrue(range.contains(1));
    assertTrue(range.contains(2));
    assertFalse(range.contains(-3));
    assertFalse(range.contains(3));

    NumberRange<Double> doubleRange = new NumberRange<Double>(-1.0, 1.0);
    assertTrue(doubleRange.contains(-1.0));
    assertTrue(doubleRange.contains(-.5));
    assertTrue(doubleRange.contains(0d));
    assertTrue(doubleRange.contains(.5));
    assertTrue(doubleRange.contains(1.0));
    assertFalse(doubleRange.contains(-1.00000001));
    assertFalse(doubleRange.contains(1.00000001));
  }

  public void testCoerce() throws Exception {
    NumberRange<Integer> range = new NumberRange<Integer>(5, 8);
    assertEquals(5, (int)range.coerce(4));
    assertEquals(5, (int)range.coerce(5));
    assertEquals(6, (int)range.coerce(6));
    assertEquals(8, (int)range.coerce(8));
    assertEquals(8, (int)range.coerce(9));

    NumberRange<Double> doubleRange = new NumberRange<Double>(-1.0, 1.0);
    assertEquals(-1.0, (double)doubleRange.coerce(-1.1));
    assertEquals(-1.0, (double)doubleRange.coerce(-1.0));
    assertEquals(-.9, (double)doubleRange.coerce(-.9));
    assertEquals(0d, (double)doubleRange.coerce(0d));
    assertEquals(.9, (double)doubleRange.coerce(.9));
    assertEquals(1.0, (double)doubleRange.coerce(1.0));
    assertEquals(1.0, (double)doubleRange.coerce(1.1));
  }

  public void testFromString() throws Exception {
    // int range
    {
      NumberRange<Integer> range = fromStringIntRange("15..79");
      assertEquals(new Integer(15), range.min());
      assertEquals(new Integer(79), range.max());
    }
    // double range
    {
      NumberRange<Double> range = fromStringDoubleRange("15.1423..79.5");
      assertEquals(new Double(15.1423), range.min());
      assertEquals(new Double(79.5), range.max());
    }
  }

  public void testIteration() throws Exception {
    // int range
    {
      NumberRange<Integer> range = new NumberRange<Integer>(2, 5);
      // test iteration by ones
      assertIteratedElements(Arrays.asList(2, 3, 4, 5), range.iterator());
      // test iteration by twos
      assertIteratedElements(Arrays.asList(2, 4), range.iterator(2));
    }
    // double range
    {
      NumberRange<Double> range = new NumberRange<Double>(2.23, 5.89);
      // test iteration by ones
      assertIteratedElements(Arrays.asList(2.23, 3.23, 4.23, 5.23), range.iterator());
      // test iteration by twos
      assertIteratedElements(Arrays.asList(2.23, 4.23), range.iterator(2.0));
    }
  }


  public void testFromPercentOffset() throws Exception {
    {
      NumberRange<Double> twentyPercentAround1 = fromPercentOffset(1d, 20);
      assertEquals(.8d, twentyPercentAround1.min(), .001);
      assertEquals(1.2d, twentyPercentAround1.max(), .001);
    }
    {
      NumberRange<Double> twentyPercentAround100 = fromPercentOffset(100d, 20);
      assertEquals(80d, twentyPercentAround100.min(), .001);
      assertEquals(120d, twentyPercentAround100.max(), .001);
    }
  }

  public void testFromOffset() throws Exception {
    NumberRange<Double> plusMinus5from7 = fromOffset(7d, 5d);
    assertEquals(2d, plusMinus5from7.min(), .001);
    assertEquals(12d, plusMinus5from7.max(), .001);
  }

  public void testParseIntRangeList() throws Exception {
    // start with some simple inputs
    assertTrue(parseIntRangeList("").isEmpty());

    assertEquals(new TreeSet<Integer>(Arrays.asList((Integer)5)), parseIntRangeList("5"));
    assertEquals(new TreeSet<Integer>(Arrays.asList(5, 6, 7)), parseIntRangeList("5..7"));

    // now try a list with multiple ranges
    {
      Set<Integer> ints = parseIntRangeList("1, 2, 6..10, 12, 19..20");
      assertTrue(ints.contains(1));
      assertTrue(ints.contains(2));
      assertTrue(ints.contains(6));
      assertTrue(ints.contains(7));
      assertTrue(ints.contains(8));
      assertTrue(ints.contains(9));
      assertTrue(ints.contains(10));
      assertTrue(ints.contains(12));
      assertTrue(ints.contains(19));
      assertTrue(ints.contains(20));
      assertEquals(10, ints.size());
    }
    // now try a the same but with arbitrary whitespacing (and lack thereof)
    {
      Set<Integer> ints = parseIntRangeList("1,2,  6..10, 12,    19..20");
      assertTrue(ints.contains(1));
      assertTrue(ints.contains(2));
      assertTrue(ints.contains(6));
      assertTrue(ints.contains(7));
      assertTrue(ints.contains(8));
      assertTrue(ints.contains(9));
      assertTrue(ints.contains(10));
      assertTrue(ints.contains(12));
      assertTrue(ints.contains(19));
      assertTrue(ints.contains(20));
      assertEquals(10, ints.size());
    }
  }

  public void testSize() throws Exception {
    assertEquals(1, new NumberRange<Integer>(0, 0).size());
    assertEquals(2, new NumberRange<Integer>(0, 1).size());
    assertEquals(3, new NumberRange<Integer>(0, 2).size());
    assertEquals(101, new NumberRange<Integer>(100, 200).size());
    assertEquals(5, new NumberRange<Double>(.5, 5.5).size());
    assertEquals(5, new NumberRange<Double>(.5, 5.0).size());
    assertEquals(5, new NumberRange<Double>(.5, 5.9).size());
    assertEquals(4, new NumberRange<Double>(.5, 4.9).size());
    assertEquals(4, new NumberRange<Double>(.5, 4.9).size());
    assertEquals(6, new NumberRange<Double>(.5, 6.0).size());
  }

  public void testInRange() throws Exception {
    // test some Comparable objects (strings)
    assertTrue(inRange("a", "b", "a"));
    assertTrue(inRange("a", "b", "aa"));
    assertTrue(inRange("a", "b", "b"));
    assertFalse(inRange("a", "b", "bb"));
    // test some ints
    assertTrue(inRange(1, 3, 1));
    assertTrue(inRange(1, 3, 2));
    assertTrue(inRange(1, 3, 3));
    assertFalse(inRange(1, 3, 0));
    assertFalse(inRange(1, 3, 4));
  }
}