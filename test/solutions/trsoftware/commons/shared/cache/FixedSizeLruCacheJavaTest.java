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

package solutions.trsoftware.commons.shared.cache;

import com.google.gwt.core.shared.GwtIncompatible;
import junit.framework.TestCase;
import solutions.trsoftware.commons.server.testutil.MultithreadedTestHarness;
import solutions.trsoftware.commons.shared.annotations.Slow;
import solutions.trsoftware.commons.shared.util.RandomUtils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Feb 20, 2009
 *
 * @author Alex
 */
public class FixedSizeLruCacheJavaTest extends TestCase {

  public void testCache() throws Exception {
    final FixedSizeLruCache<String, Integer> cache = new FixedSizeLruCache<String, Integer>(3);
    FixedSizeLruCacheGwtTest.checkLruConsistency(cache, cache.getSizeLimit());
  }

  @Slow
  @GwtIncompatible
  public void testCacheMultithreaded() throws Exception {
    checkCacheMultithreading(3, 2, 10000);
    checkCacheMultithreading(3, 32, 10000);
    checkCacheMultithreading(1000, 32, 10000);
  }

  @GwtIncompatible
  public static void checkCacheMultithreading(int sizeLimit, int nThreads, int iterationsPerThread) throws Exception {
    // make sure that no exceptions arise from multithreading
    final Map<Integer, Integer> cache = new FixedSizeLruCache<Integer, Integer>(sizeLimit);
    final int nKeys = sizeLimit * 2;
    final AtomicInteger[] lastValueForEachKey = new AtomicInteger[nKeys];
    for (int i = 0; i < nKeys; i++) {
      lastValueForEachKey[i] = new AtomicInteger();
    }
    assertTrue(
        new MultithreadedTestHarness(new Runnable() {
          public void run() {
            int idx = RandomUtils.rnd().nextInt(nKeys);
            synchronized (cache) {
              cache.put(idx, lastValueForEachKey[idx].incrementAndGet());
            }
          }
        }).run(nThreads, iterationsPerThread).isEmpty());
    // check consistency: make sure that for each key in the cache, it's the last value that was put in there
    synchronized (cache) {
      Set<Map.Entry<Integer, Integer>> cacheEntries = cache.entrySet();
      for (Map.Entry<Integer, Integer> entry : cacheEntries) {
        assertEquals(lastValueForEachKey[entry.getKey()].get(), (int)entry.getValue());
      }
    }
  }
}