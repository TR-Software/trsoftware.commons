/*
 * Copyright 2020 TR Software Inc.
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

package solutions.trsoftware.commons.server.net;

import com.google.common.cache.CacheStats;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Range;
import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.util.IpAddress;
import solutions.trsoftware.commons.shared.util.RandomUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * @author Alex
 * @since 5/20/2020
 */
public class RandomIpAddressAllocatorTest extends TestCase {

  private RandomIpAddressAllocator<String> allocator;

  public void tearDown() throws Exception {
    allocator = null;
    super.tearDown();
  }

  public void testGet() throws Exception {
    Range<IpAddress> targetRange = Range.closed(new IpAddress("240.0.0.0"), new IpAddress("255.255.255.255"));
    allocator = new RandomIpAddressAllocator<>("240.0.0.0", "255.255.255.255",
        .75, TimeUnit.HOURS.toMillis(12));
    BiMap<String, IpAddress> results = HashBiMap.create();
    int n = 1_000;
    for (int i = 0; i < n; i++) {
      InetAddress rndIp6Addr = InetAddress.getByAddress(RandomUtils.randBytes(16));
      String ip6AddrStr = rndIp6Addr.toString();
      IpAddress result = doGet(ip6AddrStr, false);
      assertTrue(targetRange.contains(result));  // should be in the correct range
      assertFalse(results.containsValue(result)); // should be unique (not already assigned)
      if (i % 100 == 0) {
        System.out.println(
            StringUtils.methodCallToStringWithResult("allocator.get", result, ip6AddrStr));
      }
      results.put(ip6AddrStr, result);
    }
    // make sure that subsequent calls for existing keys return the same value
    for (String key : results.keySet()) {
      assertEquals(results.get(key), doGet(key, true));
    }
  }

  /**
   * Test cache eviction based on the desired load factor
   */
  // TODO: fix this test
  public void TODO_testCacheLoadFactor() throws Exception {
    // using an address space of size 100 and load factor of .8
    Range<IpAddress> targetRange = Range.closed(new IpAddress("240.0.0.0"), new IpAddress("240.0.0.99"));
    allocator = new RandomIpAddressAllocator<>("240.0.0.0", "240.0.0.99",
        .8, TimeUnit.HOURS.toMillis(12));
    // these settings should recycle allocated addresses after the size limit of 80 has been exceeded
    int expectedMaxSize = 80;
    /*
      TODO:
        This hit/miss assertions in this unit test are failing when the actual cache size is only 65,
        even though the max size setting is 80, because (according to the Guava Wiki):
          "the cache may evict entries before this limit is exceeded -- typically when the cache size is approaching the limit"
        It appears there's no reliable way to unit test eviction based on max size, because entries may be evicted
        prematurely. So to get deterministic eviction behavior that can be unit tested, would need to write a custom cache impl.

      TODO:
        Rewrite this test case to check only that the allocations never fail due to address space exhaustion
     */
    BiMap<String, IpAddress> results = HashBiMap.create();
    while (results.size() < expectedMaxSize) {
      InetAddress rndIp6Addr = InetAddress.getByAddress(RandomUtils.randBytes(16));
      String ip6AddrStr = rndIp6Addr.toString();
      IpAddress result = doGet(ip6AddrStr, false);
      System.out.println(StringUtils.methodCallToStringWithResult(
          "allocator.get", result, ip6AddrStr));
      assertTrue(targetRange.contains(result));  // should be in the correct range
      assertFalse(results.containsValue(result)); // should be unique (not already assigned)
      results.put(ip6AddrStr, result);
    }
    assertEquals(expectedMaxSize, results.size());
    // all allocations up to this point should have resulted in cache misses
    int expectedMissCount = results.size();
    int expectedHitCount = 0;
    assertEquals(expectedHitCount, allocator.stats().hitCount());
    assertEquals(expectedMissCount, allocator.stats().missCount());
    // make sure that subsequent calls for existing keys result in cache hits
    for (String key : results.keySet()) {
      assertEquals(results.get(key), doGet(key, true));
      expectedHitCount++;
    }
    assertEquals(expectedHitCount, allocator.stats().hitCount());
    // the miss count should still be the same as before
    assertEquals(expectedMissCount, allocator.stats().missCount());

    // the next allocation should evict the oldest entry, but return the same value for the others
    ArrayList<String> keys = new ArrayList<>(results.keySet());
    Iterator<String> keysIter = keys.iterator();
    String oldestKey = keysIter.next();
    IpAddress newResult = doGet(oldestKey, false);
    // NOTE: although the above operation should have resulted in a cache miss,
    //  the result may still be the same as before, due to the random nature of the allocator
    // however, the rest of the mappings should still be the same, and should all produce cache hits
    while (keysIter.hasNext()) {
      String key = keysIter.next();
      assertEquals(results.get(key), doGet(key, true));
    }
  }

  /**
   * Calls {@link RandomIpAddressAllocator#get(Object)} and asserts that the operation resulted in a hit or miss, as
   * expected.
   *
   * @param expectHit  {@code true} if the operation should have resulted in a cache hit, otherwise miss.
   * @return the result of the operation
   */
  private IpAddress doGet(String key, boolean expectHit) {
    CacheStats statsBefore = allocator.stats();
    IpAddress result = allocator.get(key);
    CacheStats statsAfter = allocator.stats();
    if (expectHit) {
      // expected cache hit
      assertEquals(statsBefore.hitCount() + 1, statsAfter.hitCount());
      assertEquals(statsBefore.missCount(), statsAfter.missCount());
    }
    else {
      // expected cache miss
      assertEquals(statsBefore.hitCount(), statsAfter.hitCount());
      assertEquals(statsBefore.missCount() + 1, statsAfter.missCount());
    }
    return result;
  }
}