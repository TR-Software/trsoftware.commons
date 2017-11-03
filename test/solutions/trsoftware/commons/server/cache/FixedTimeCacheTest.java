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

package solutions.trsoftware.commons.server.cache;

import solutions.trsoftware.commons.client.testutil.AssertUtils;
import solutions.trsoftware.commons.client.util.stats.NumberSample;
import solutions.trsoftware.commons.server.TestCaseCanStopClock;
import solutions.trsoftware.commons.server.util.Clock;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Jul 1, 2009
 *
 * @author Alex
 */
public class FixedTimeCacheTest extends TestCaseCanStopClock {

  private final Random rnd = new Random(1);

  public void testFixedSizeBehavior() throws Exception {
    long maxAge = TimeUnit.MINUTES.toMillis(1);
    int maxSize = 2;
    FixedTimeCache<Integer, String> cache = new FixedTimeCache<Integer, String>(maxAge, maxSize);
    Clock.stop();
    // put a few entries
    cache.put(11, "1_1");
    assertEquals(1, cache.size());
    cache.put(12, "1_2");
    assertEquals(2, cache.size());
    // the cache is now at full capacity, the next put should evict the oldest entry
    cache.put(13, "1_3");
    assertEquals(2, cache.size());
    assertFalse(cache.containsKey(11)); // this one should have been evicted
    assertEquals("1_2", cache.get(12)); // this one is still there
    assertEquals("1_3", cache.get(13)); // this one is still there too
    Clock.advance(maxAge+1);
    // now everything should have been evicted
    assertEquals(0, cache.size());
  }

  public void testFixedTimeBehavior() throws Exception {
    // create a cache with a one minute expiration time
    final long maxAge = TimeUnit.MINUTES.toMillis(1);
    FixedTimeCache<Integer, String> cache = new FixedTimeCache<Integer, String>(maxAge);
    Clock.stop();
    // put a few entries
    cache.put(11, "1_1");
    cache.put(12, "1_2");
    cache.put(13, "1_3");
    assertTrue(cache.containsKey(11));
    assertEquals("1_2", cache.get(12));
    assertEquals(3, cache.size());
    // test replacement and removal
    assertEquals("1_3", cache.put(13, "1_3b"));
    assertEquals("1_3b", cache.get(13));
    assertEquals(3, cache.size());
    assertEquals("1_3b", cache.remove(13));
    assertNull(cache.get(13));
    assertFalse(cache.containsKey(13));
    assertEquals(2, cache.size());
    // now advance the clock far enough to evict both of the remaining entries
    Clock.advance(maxAge);
    // be sure no evictions yet (age = maxAge is acceptable)
    assertEquals(2, cache.size());
    Clock.advance(1);  // now this should put it over the top
    // make sure that get performs amortized eviction
    assertNull(cache.get(12));  // check the entries in reverse order of insertion to be sure all expired entries have been evicted, not just the oldest one
    assertNull(cache.get(11));
    assertEquals(0, cache.size());
    // now check that containsKey also performs amortized eviction
    cache.put(21, "2_1");
    cache.put(22, "2_2");
    cache.put(23, "2_3");
    assertEquals(3, cache.size());
    assertTrue(cache.containsKey(21));
    assertTrue(cache.containsKey(22));
    assertTrue(cache.containsKey(23));
    Clock.advance(maxAge + 1);
    assertFalse(cache.containsKey(23));
    assertFalse(cache.containsKey(22));
    assertFalse(cache.containsKey(21));
    // now check that put also performs amortized eviction (albeit for just the oldest entry)
    cache.put(31, "3_1");
    Clock.advance(1);
    cache.put(32, "3_2");
    Clock.advance(1);
    cache.put(33, "3_3");
    Clock.advance(1);
    assertEquals(3, cache.size());
    assertTrue(cache.containsKey(31));
    assertTrue(cache.containsKey(32));
    assertTrue(cache.containsKey(33));
    Clock.advance(maxAge-2);
    cache.put(34, "3_4");
    assertEquals(3, cache.size());  // 34 should have evicted 31
    assertFalse(cache.containsKey(31));
    assertTrue(cache.containsKey(32));
    assertTrue(cache.containsKey(33));
    assertTrue(cache.containsKey(34));
    // even size() shoud perform amortized eviction
    assertEquals(3, cache.size());
    Clock.advance(maxAge);
    assertEquals(1, cache.size());
    assertTrue(cache.containsKey(34));

    // now just add entries at will and keep checking the invariant
    NumberSample<Integer> evictionsPerIteration = new NumberSample<Integer>();
    int lastSize = cache.size();
    for (int i = 0; i < 100000; i++) {
      cache.put(i, "4_" + i);
      Clock.advance(rnd.nextInt((int)(maxAge >> 1)));
      int size = cache.size();  // call a method that performs amortized cleanup
      evictionsPerIteration.update((lastSize + 1) - size);  // we expect there to be lastSize + 1 without any evictions
      lastSize = size;
      assertFalse("" + i, cache.anyExpiredEntries());
    }
    System.out.println("evictionsPerIteration = " + evictionsPerIteration.summarize().toString());
    assertTrue(cache.size() > 0);
    Clock.advance(maxAge);  // one more maxAge interval should clear the cache
    assertTrue(cache.size() == 0);
  }

  /** Test how iteration over the key set deals with expired values */
  public void testKeySet() throws Exception {
    Clock.stop();
    final long startTime = Clock.currentTimeMillis();
    final long maxAge = TimeUnit.MINUTES.toMillis(1);
    FixedTimeCache<Integer, String> cache = new FixedTimeCache<Integer, String>(maxAge);
    // check key set operations when empty
    {
      Set<Integer> keySet = cache.keySet();
      assertEquals(0, keySet.size());
      assertTrue(keySet.isEmpty());
      assertFalse(keySet.contains(11));
    }
    // put a few entries
    cache.put(11, "1_1");
    Clock.advance(10);
    cache.put(12, "1_2");
    Clock.advance(10);
    cache.put(13, "1_3");
    // check key set operations when not empty
    {
      Set<Integer> keySet = cache.keySet();
      assertEquals(3, keySet.size());
      assertFalse(keySet.isEmpty());
      assertTrue(keySet.contains(11));
      // check normal iteration over the entry set (without any concurrent modification)
      {
        final Iterator<Integer> iter = keySet.iterator();
        assertEquals("1_1", cache.get(iter.next()));
        assertEquals("1_2", cache.get(iter.next()));
        assertEquals("1_3", cache.get(iter.next()));
        assertNoMoreElements(iter);
        assertNoMoreElements(iter);
      }
      // check an element expiring out of the cache during iteration
      {
        final Iterator<Integer> iter = keySet.iterator();
        assertEquals("1_1", cache.get(iter.next()));
        Clock.set(startTime+maxAge+11);  // move the clock forward just enough to expire the second element (12, "1_2")
        assertEquals("1_3", cache.get(iter.next()));
        assertNoMoreElements(iter);
      }
    }
  }

  private void assertNoMoreElements(final Iterator iter) {
    AssertUtils.assertThrows(NoSuchElementException.class, new Runnable() {
      public void run() {
        iter.next();
      }
    });
  }
}