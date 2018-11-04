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

package solutions.trsoftware.commons.server.util;

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.annotations.Slow;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;

public class SimpleUUIDTest extends TestCase {

  public void testSomeExamples() throws Exception {
    // simply print out a few generated UID to the console, so we can visually see what they look like
    System.out.println(generateUUIDs(10));
  }

  @Slow
  public void testUniqueness() throws Exception {
    generateUUIDs(1000000);
  }

  private static Set<String> generateUUIDs(int iterations) {
    // this set will be used to enforce uniqueness of each generated uuid
    Set<String> uuidSet = new HashSet<>(iterations * 2);
    for (int i = 0; i < iterations; i++) {
      String uuid = SimpleUUID.randomUUID();
      assertTrue(uuidSet.add(uuid));
      // make sure the uuid is url-safe
      assertEquals(uuid, ServerStringUtils.urlEncode(uuid));
    }
    return uuidSet;
  }

  @Slow
  public void testUniquenessMultithreaded() throws Exception {
    final int nThreads = 10;
    final int iterations = 100000;
    final CyclicBarrier barrier = new CyclicBarrier(nThreads+1);
    final ConcurrentHashMap<String, Boolean> uuidSet = new ConcurrentHashMap<>(iterations * nThreads, 1.0f, 10);
    for (int i = 0; i < nThreads; i++) {
      new Thread(() -> {
        try {
          // this set will be used to enforce uniqueness of each generated uuid
          for (int i1 = 0; i1 < iterations; i1++) {
            String uuid = SimpleUUID.randomUUID();
            // make sure the uuid is url-safe
            assertNull(uuidSet.put(uuid, true));
            // make sure the uuid is url-safe
            assertEquals(uuid, ServerStringUtils.urlEncode(uuid));
          }
          barrier.await();
        }
        catch (Exception e) {
          e.printStackTrace();
          fail();
        }
      }).start();
    }
    barrier.await();
  }
}