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

package solutions.trsoftware.commons.server.cache;

import solutions.trsoftware.commons.server.util.Clock;

/**
 * Jun 30, 2009
*
* @author Alex
*/
public class FixedTimeCacheValue<V> {
  private V value;
  /** The time when the value will expire */
  private long expirationTime;

  FixedTimeCacheValue(V value, long expirationTime) {
    this.value = value;
    this.expirationTime = expirationTime;
//    System.out.printf("FixedTimeCacheValue(%s, %d)%n", value, expirationTime);
  }

  public V getValue() {
    return value;
  }

  void setValue(V value) {
    this.value = value;
  }

  /** The absolute datetime when this value will be evicted from the cache */
  public long getExpirationTime() {
    return expirationTime;
  }

  /** The number of milliseconds remaining until this value will be evicted from the cache */
  public long getExpirationTimeDelta() {
    return expirationTime - Clock.currentTimeMillis();
  }

  /**
   * @return {@code true} iff if this value's {@link #getExpirationTime() expiration time} is older than the
   * current clock time.
   */
  public boolean isExpired() {
    return getExpirationTime() < Clock.currentTimeMillis();
  }
}
