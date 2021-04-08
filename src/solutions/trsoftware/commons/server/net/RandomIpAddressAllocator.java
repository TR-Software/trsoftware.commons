/*
 * Copyright 2021 TR Software Inc.
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

package solutions.trsoftware.commons.server.net;

import com.google.common.base.Preconditions;
import com.google.common.cache.*;
import solutions.trsoftware.commons.shared.util.IpAddress;
import solutions.trsoftware.commons.shared.util.IpAddressUtils;
import solutions.trsoftware.commons.shared.util.RandomUtils;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static solutions.trsoftware.commons.shared.util.IpAddressUtils.ip4LongToInt;
import static solutions.trsoftware.commons.shared.util.IpAddressUtils.ip4StringToInt;

/**
 * Associates keys with randomly-generated {@linkplain IpAddress IPv4 addresses} in a particular range.
 * <p>
 * For example, this can be used to map IPv6 addresses to unique "pseudo" addresses in the
 * <a href="https://en.wikipedia.org/wiki/Classful_network">Class E (reserved) IPv4 address space</a>:
 * <pre>{@code
 *   RandomIpAddressAllocator<String> allocator = new RandomIpAddressAllocator<>(
 *       "240.0.0.0", "255.255.255.255", .75, TimeUnit.HOURS.toMillis(12));
 *   allocator.get("/b027:49e2:e8d9:98cf:ea47:d3db:55f0:40b1");  // 242.136.19.66
 * }
 * </pre>
 * This class uses a (thread-safe) Guava {@link Cache} to store the address mappings.
 * <p>
 * <b>NOTE</b>: to ensure longevity of the cached mappings, it's important to specify a sufficiently-large address range.
 * The cache will start to evict entries that haven't been used "recently or very often" when the cache size
 * is approaching the maximum load factor, which could be much earlier than their lease time expires.
 *
 * @param <K> arbitrary type of key to use for the address mapping cache.  For example, this might be a {@link String}
 * specifying an IPv6 address or a {@link java.net.Inet6Address} instance.
 *
 * @author Alex
 * @since 5/20/2020
 * @see <a href="https://support.cloudflare.com/hc/en-us/articles/229666767-Understanding-and-configuring-Cloudflare-s-IPv6-support">
 *   Cloudflare's IPv6 to IPv4 translation service ("Pseudo IPv4")</a>
 */
public class RandomIpAddressAllocator<K> {
  /**
   * The maximum percentage of the addresses in the range that can be allocated, to ensure that the allocation
   * loop will terminate.
   */
  private static final double MAX_LOAD_FACTOR = .99;

  private Random rnd = new Random();

  private Set<Integer> assignedAddresses = ConcurrentHashMap.newKeySet();

  private LoadingCache<K, IpAddress> mappingCache;

  /** Lowest address in the range */
  private final int minAddress;
  /** Highest address in the range */
  private final int maxAddress;
  /** The total number of unique addresses in the range */
  private final int rangeSize;

  /**
   * @param minAddress lowest IPv4 address in the target range in dot-decimal notation (e.g. "240.0.0.0")
   * @param maxAddress highest IPv4 address in the target range (e.g. "255.255.255.255")
   * @param loadFactor maximum fraction of the address space that can be allocated (between 0 and {@value MAX_LOAD_FACTOR}).
   *   This parameter is used to tune the performance of address allocation, which is proportional to the size of the cache.
   *   <b>Warning:</b> Due to the nature of Guava's cache implementation, older entries may be evicted much earlier than their
   *   lease time expires, when the cache size is approaching the limit specified by the {@code loadFactor}.
   * @param durationMillis the lease duration of each address
   */
  public RandomIpAddressAllocator(String minAddress, String maxAddress, double loadFactor, long durationMillis) {
    this(ip4StringToInt(minAddress), ip4StringToInt(maxAddress), loadFactor, durationMillis);
  }

  /**
   * @param minAddress lowest IPv4 address in the target range
   * @param maxAddress highest IPv4 address in the target range
   * @param loadFactor maximum fraction of the address space that can be allocated (between 0 and {@value MAX_LOAD_FACTOR}).
   *   This parameter is used to tune the performance of address allocation, which is proportional to the size of the cache.
   *   <b>Warning:</b> Due to the nature of Guava's cache implementation, older entries may be evicted much earlier than their
   *   lease time expires, when the cache size is approaching the limit specified by the {@code loadFactor}.
   * @param durationMillis the lease duration of each address
   * @see IpAddressUtils#ip4StringToLong(String)
   */
  public RandomIpAddressAllocator(long minAddress, long maxAddress, double loadFactor, long durationMillis) {
    this(ip4LongToInt(minAddress), ip4LongToInt(maxAddress), loadFactor, durationMillis);
  }

  /**
   * @param minAddress lowest IPv4 address in the target range
   * @param maxAddress highest IPv4 address in the target range
   * @param loadFactor maximum fraction of the address space that can be allocated (between 0 and {@value MAX_LOAD_FACTOR}).
   *   This parameter is used to tune the performance of address allocation, which is proportional to the size of the cache.
   *   <b>Warning:</b> Due to the nature of Guava's cache implementation, older entries may be evicted much earlier than their
   *   lease time expires, when the cache size is approaching the limit specified by the {@code loadFactor}.
   * @param durationMillis the lease duration of each address
   * @see IpAddress
   */
  public RandomIpAddressAllocator(IpAddress minAddress, IpAddress maxAddress, double loadFactor, long durationMillis) {
    this(minAddress.toInt(), maxAddress.toInt(), loadFactor, durationMillis);
  }

  /**
   * @param minAddress lowest IPv4 address in the target range
   * @param maxAddress highest IPv4 address in the target range
   * @param loadFactor maximum fraction of the address space that can be allocated (between 0 and {@value MAX_LOAD_FACTOR}).
   *   This parameter is used to tune the performance of address allocation, which is proportional to the size of the cache.
   *   <b>Warning:</b> Due to the nature of Guava's cache implementation, older entries may be evicted much earlier than their
   *   lease time expires, when the cache size is approaching the limit specified by the {@code loadFactor}.
   * @param durationMillis the lease duration of each address
   * @see IpAddressUtils#ip4StringToInt(String)
   */
  public RandomIpAddressAllocator(int minAddress, int maxAddress, double loadFactor, long durationMillis) {
    Preconditions.checkArgument(minAddress < maxAddress,
        "minAddress must be lower than maxAddress");
    Preconditions.checkArgument(loadFactor > 0 && loadFactor <= MAX_LOAD_FACTOR,
        "loadFactor must be between 0 and %s", MAX_LOAD_FACTOR);
    this.minAddress = minAddress;
    this.maxAddress = maxAddress;
    // number of unique addresses in this range
    rangeSize = maxAddress - minAddress + 1;
    int maxCacheSize = (int)(rangeSize * loadFactor);
    mappingCache = CacheBuilder.newBuilder()
        .expireAfterAccess(durationMillis, TimeUnit.MILLISECONDS)
        .maximumSize(maxCacheSize)
        // remove the value from the set of used values when the entry expires
        .removalListener(new RemovalListener<K, IpAddress>() {
          @Override
          public void onRemoval(RemovalNotification<K, IpAddress> notification) {
            assignedAddresses.remove(notification.getValue().toInt());
          }
        })
        .recordStats()
        .build(new CacheLoader<K, IpAddress>() {
          @Override
          public IpAddress load(K key) {
            // 1) make sure the assignment loop will terminate
            double load = (double)assignedAddresses.size() / rangeSize;
            if (load > MAX_LOAD_FACTOR)
              // should never happen, due to the max cache size setting above
              throw new IllegalStateException("Address space exhausted");

            // 2) generate a random address in the given range that hasn't already been assigned
            int randAddress;
            do {
              randAddress = RandomUtils.nextIntInRange(rnd, minAddress, maxAddress);
            }
            while (!assignedAddresses.add(randAddress));
            return new IpAddress(randAddress);
          }
        });
  }

  /**
   * Returns the IPv4 address assigned to the given key, allocating a new one if not already assigned.
   *
   * @return the IPv4 address mapping corresponding to the given key
   */
  public IpAddress get(K key) {
    // NOTE: we can use getUnchecked here because our CacheLoader doesn't throw any checked exceptions
    return mappingCache.getUnchecked(key);
  }

  /**
   * @return the approximate number of addresses that have already been allocated
   */
  public long size() {
    return mappingCache.size();
  }

  /**
   * @return the stats of the internal address cache
   */
  public CacheStats stats() {
    return mappingCache.stats();
  }
}
