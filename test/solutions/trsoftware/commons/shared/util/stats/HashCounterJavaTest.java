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

package solutions.trsoftware.commons.shared.util.stats;

import junit.framework.TestCase;
import solutions.trsoftware.commons.server.testutil.MultithreadedTestHarness;
import solutions.trsoftware.commons.server.util.ServerArrayUtils;
import solutions.trsoftware.commons.shared.annotations.Slow;
import solutions.trsoftware.commons.shared.util.MapDecorator;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class HashCounterJavaTest extends TestCase {

  public void testHashCounter() throws Exception {
    HashCounter<Integer> counter = new HashCounter<Integer>();

    assertEquals(0, counter.sumOfAllEntries());
    counter.increment(0);
    counter.increment(0);
    counter.increment(0);
    counter.increment(0);
    assertEquals(4, counter.sumOfAllEntries());

    counter.increment(1);
    counter.increment(1);
    counter.increment(1);
    assertEquals(7, counter.sumOfAllEntries());

    counter.increment(2);
    counter.increment(2);
    assertEquals(9, counter.sumOfAllEntries());

    counter.increment(3);
    assertEquals(10, counter.sumOfAllEntries());

    assertEquals(4, counter.size());

    assertEquals(4, counter.get(0));
    assertEquals(3, counter.get(1));
    assertEquals(2, counter.get(2));
    assertEquals(1, counter.get(3));

    // test the sorting of entries
    assertEquals("[(0,4), (1,3), (2,2), (3,1)]", counter.entriesSortedByKeyAscending().toString());
    assertEquals("[(3,1), (2,2), (1,3), (0,4)]", counter.entriesSortedByValueAscending().toString());
    assertEquals("[(3,1), (2,2), (1,3), (0,4)]", counter.entriesSortedByKeyDescending().toString());
    assertEquals("[(0,4), (1,3), (2,2), (3,1)]", counter.entriesSortedByValueDescending().toString());

    // now test decrementing the counter
    counter.add(0, -1);
    assertEquals(3, counter.get(0));
    assertEquals(9, counter.sumOfAllEntries());

    counter.add(2, -20);
    assertEquals(-18, counter.get(2));

    assertEquals(3, counter.get(0));
    assertEquals(3, counter.get(1));
    assertEquals(-18, counter.get(2));
    assertEquals(1, counter.get(3));

    assertEquals(-11, counter.sumOfAllEntries());

    // test the sorting of entries again, now that we have some negative numbers present
    assertEquals("[(0,3), (1,3), (2,-18), (3,1)]", counter.entriesSortedByKeyAscending().toString());
    assertEquals("[(2,-18), (3,1), (0,3), (1,3)]", counter.entriesSortedByValueAscending().toString());
    assertEquals("[(3,1), (2,-18), (1,3), (0,3)]", counter.entriesSortedByKeyDescending().toString());
    assertEquals("[(1,3), (0,3), (3,1), (2,-18)]", counter.entriesSortedByValueDescending().toString());

    // test merging with another counter
    HashCounter<Integer> other = new HashCounter<Integer>();
    other.increment(0);
    other.add(3, -3);
    other.increment(4);
    other.increment(4);

    counter.merge(other);
    assertEquals(5, counter.size());

    assertEquals(4, counter.get(0));
    assertEquals(3, counter.get(1));
    assertEquals(-18, counter.get(2));
    assertEquals(-2, counter.get(3));
    assertEquals(2, counter.get(4));
  }


  public void testEquivalentEntrySorting() throws Exception {
    // since the underlying implementation uses a SortedSet, we have to make sure that equivalent values don't get lost

    HashCounter<Integer> counter = new HashCounter<Integer>();
    counter.increment(1);
    counter.increment(2);
    counter.increment(3);

    assertEquals(0, counter.get(0));
    assertEquals(1, counter.get(1));
    assertEquals(1, counter.get(2));
    assertEquals(1, counter.get(3));

    // test the sorting of entries
    assertEquals("[(1,1), (2,1), (3,1)]", counter.entriesSortedByKeyAscending().toString());
    assertEquals("[(1,1), (2,1), (3,1)]", counter.entriesSortedByValueAscending().toString());
    assertEquals("[(3,1), (2,1), (1,1)]", counter.entriesSortedByKeyDescending().toString());
    assertEquals("[(3,1), (2,1), (1,1)]", counter.entriesSortedByValueDescending().toString());
  }

  @Slow
  public void testMultithreaded() throws Exception {
    final Random rnd = new Random(1);
    final HashCounter<String> counter = new HashCounter<String>();
    // use 100 threads to increment 5 keys at random
    final String[] keys = new String[]{"a", "b", "c", "d", "e"};
    final AtomicInteger[] ourCounters = ServerArrayUtils.fill(
        new AtomicInteger[keys.length], AtomicInteger.class);

    Collection<Throwable> errors = new MultithreadedTestHarness(new Runnable() {
      public void run() {
        int index = rnd.nextInt(keys.length);
        counter.increment(keys[index]);
        ourCounters[index].incrementAndGet();
      }
    }).run(100, 100000);

    // verify that our reference counters match the HashCounter:
    for (int i = 0; i < keys.length; i++) {
      assertEquals(ourCounters[i].get(), counter.get(keys[i]));
    }

    System.out.println("counter = " + counter);
    System.out.println("errors = " + errors);
    
    assertTrue(errors.isEmpty());
  }

  /**
   * Creates an instance pre-populated with the given counts.
   */
  public static <T> HashCounter<T> createCounter(Map<T, Integer> counts) {
    HashCounter<T> counter = new HashCounter<T>();
    for (Map.Entry<T, Integer> entry : counts.entrySet()) {
      counter.add(entry.getKey(), entry.getValue());
    }
    return counter;
  }

  public void testProbabilityOf() throws Exception {
    HashCounter<Character> counter = createCounter(new MapDecorator<>(new LinkedHashMap<Character, Integer>())
        .put('a', 2)
        .put('b', 4)
        .put('c', 6)
        .getMap());
    System.out.println(counter);
    assertEquals(2./12, counter.probabilityOf('a'), .001);
    assertEquals(4./12, counter.probabilityOf('b'), .001);
    assertEquals(6./12, counter.probabilityOf('c'), .001);
    assertEquals(0., counter.probabilityOf('X'), .001);
  }
}