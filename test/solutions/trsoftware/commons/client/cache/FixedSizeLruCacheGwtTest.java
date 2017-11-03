package solutions.trsoftware.commons.client.cache;

import solutions.trsoftware.commons.client.CommonsGwtTestCase;
import solutions.trsoftware.commons.client.util.StringUtils;

import java.util.Map;

/**
 * Feb 20, 2009
 *
 * @author Alex
 */
public class FixedSizeLruCacheGwtTest extends CommonsGwtTestCase {

  public void testCache() throws Exception {
    final FixedSizeLruCache<String, Integer> cache = new FixedSizeLruCache<String, Integer>(3);
    checkLruConsistency(cache, cache.getSizeLimit());
  }

  /**
   * Adds a few elements to the given cache and ensures that the LRU property
   * holds.
   */
  public static void checkLruConsistency(Map<String, Integer> cache, int sizeLimit) {
    int initialSize = cache.size();
    String[] keys = new String[sizeLimit+1];
    for (int i = 0; i < keys.length; i++) {
      String key = keys[i] = StringUtils.randString(10);
      cache.put(key, i);
      assertEquals(Math.min(sizeLimit, initialSize + i + 1), cache.size());
    }
    // the first key should have been evicted at this point (least recently added)
    assertEquals(sizeLimit, cache.size());
    assertNull(cache.get(keys[0]));

    // examine (touch) the other values in reverse order
    for (int i = keys.length-1; i >= 1; i--) {
      assertEquals((Integer)i, cache.get(keys[i]));
    }
    cache.put("foo", -1);
    // the last key should have been evicted at this point (least recently accessed)
    assertEquals(sizeLimit, cache.size());
    assertNull(cache.get(keys[keys.length-1]));
  }

}