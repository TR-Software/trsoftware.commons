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

  /** The absolute datetime when this value will be evicted from the cache */
  public long getExpirationTime() {
    return expirationTime;
  }

  /** The number of milliseconds remaining until this value will be evicted from the cache */
  public long getExpirationTimeDelta() {
    return expirationTime - Clock.currentTimeMillis();
  }
}
