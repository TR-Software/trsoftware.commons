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

package solutions.trsoftware.commons.client.bridge;

import junit.framework.TestCase;
import solutions.trsoftware.commons.client.bridge.util.RandomGen;
import solutions.trsoftware.commons.client.testutil.AssertUtils;
import solutions.trsoftware.commons.client.util.callables.Function0;
import solutions.trsoftware.commons.client.util.stats.HashCounter;

import java.util.Set;
import java.util.TreeSet;

/**
 * This class is to be used as a delegate by both RandomGenJavaTest and RandomGenGwtTest,
 * but is not to be run directly by JUnit.
 * 
 * @author Alex
 */
public abstract class RandomGenTestCase extends TestCase {
  RandomGen rnd;

  static final int ITERATIONS_TO_RUN = 10000;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    rnd = RandomGen.getInstance();
  }

  public Function0<Integer> nextInt = new Function0<Integer>() {
    public Integer call() {
      return rnd.nextInt();
    }
  };
  public Function0<Double> nextDouble = new Function0<Double>() {
    public Double call() {
      return rnd.nextDouble();
    }
  };

  public class CollisionsTester {
    private int iterations = 0;
    private int collisions = 0;
    private Set generatedNumbers = new TreeSet();  // NOTE: HashSet is way too slow for storing Double types in web mode (at least on IE) for some reason

    public void addNext(Number nextRandomNumber) {
      if (!generatedNumbers.add(nextRandomNumber))
        collisions++;
      iterations++;
    }
    
    public void assertNotTooManyCollisions() {
      // make sure that there are no more than 10 collisions out of the 10K iterations
      String msg = "Got " + collisions + " collisions with " + iterations + " iterations";
      System.out.println(msg);
      assertEquals(iterations, ITERATIONS_TO_RUN);
      assertTrue(msg, collisions < 10);
      assertEquals(iterations - collisions, generatedNumbers.size());
    }
  }

  /** The GWT test should override this method to run the loop incrementally */
  protected void checkForCollisions(final Function0<? extends Number> generator) {
    CollisionsTester ct = new CollisionsTester();
    for (int i = 0; i < ITERATIONS_TO_RUN; i++) {
      ct.addNext(generator.call());
    }
    ct.assertNotTooManyCollisions();
  }

  /** Checks that the generated numbers follow the expected random distribution */
  public final void testNextInt() throws Exception {
    checkForCollisions(nextInt);
  }

  /** Checks that the generated numbers follow the expected random distribution */
  public final void testNextDouble() throws Exception {
    checkForCollisions(nextDouble);
  }

  public void testNextIntWithUpperBound() throws Exception {
    checkRangeGenerator(0, 1);
    checkRangeGenerator(0, 20);

    // these invokations should throw an exception
    AssertUtils.assertThrows(IllegalArgumentException.class, new Runnable() {
      public void run() {
        rnd.nextInt(0);
      }
    });
    AssertUtils.assertThrows(IllegalArgumentException.class, new Runnable() {
      public void run() {
        rnd.nextInt(-1);
      }
    });
  }


  public void testNextIntInRange() throws Exception {
    checkRangeGenerator(-34, -12);
    checkRangeGenerator(-2, 0);
    checkRangeGenerator(-2, 2);
    checkRangeGenerator(-1, 1);
    checkRangeGenerator(0, 2);
    checkRangeGenerator(12, 34);

    // these invokations should throw an exception
    AssertUtils.assertThrows(IllegalArgumentException.class, new Runnable() {
      public void run() {
        rnd.nextIntInRange(0, 0);
      }
    });
    AssertUtils.assertThrows(IllegalArgumentException.class, new Runnable() {
      public void run() {
        rnd.nextIntInRange(5, 5);
      }
    });
    AssertUtils.assertThrows(IllegalArgumentException.class, new Runnable() {
      public void run() {
        rnd.nextIntInRange(-1, -1);
      }
    });
  }

  private void checkRangeGenerator(int lowerBound, int upperBound) {
    int iterations = 10000;
    int range = upperBound - lowerBound;
    HashCounter<Integer> counter = new HashCounter<Integer>(range);
    for (int i = 0; i < iterations; i++) {
      int nextInt = (lowerBound == 0) ? rnd.nextInt(upperBound) : rnd.nextIntInRange(lowerBound, upperBound);
      counter.increment(nextInt);
    }
    assertEquals(range, counter.size());
    // make sure that the counts are statistically similar
    for (int i = lowerBound; i < upperBound; i++) {
      int count = counter.get(i);
      assertTrue(count > 0);
      double pct = (double)count / iterations;  // percentage of samples that went into this "bucket"
      // the percentage should be approximately (iterations/(upperBound=20))/iterations
      double expectedPct = (double)iterations / range / iterations;
      assertEquals(expectedPct, pct, .1);
    }
  }

  // TODO:
  // 1) test the bucket statistics for nextInt(upper)
  // 2) test the new custom nextIntInRange method
  

}