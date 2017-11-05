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

package solutions.trsoftware.commons.shared.cache;

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.util.StringUtils;

/**
 * Apr 29, 2011
 *
 * @author Alex
 */
public class AbstractCachingFactoryTest extends TestCase {

  private Mock cache;

  private String result100, result200;

  private static class Mock extends AbstractCachingFactory<Integer, String> {
    protected String _compute(Integer key) {
      // generate a random string of that length
      return StringUtils.randString(key);
    }
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    cache = new Mock();
    assertEquals(0, cache.getCacheSize());
    assertEquals(0, cache.getAccessCount());
    assertEquals(0, cache.getHits());
    assertEquals(0, cache.getMisses());
    // put 1 entry into the cache
    result100 = cache.compute(100);
    assertEquals(100, result100.length());
    assertEquals(1, cache.getCacheSize());
    assertEquals(1, cache.getAccessCount());
    assertEquals(0, cache.getHits());
    assertEquals(1, cache.getMisses());
  }

  public void testHit() throws Exception {
    assertSame(result100, cache.compute(100));  // the same result should have been reused
    // cache size should not have changed - only the access count
    assertEquals(1, cache.getCacheSize());
    assertEquals(2, cache.getAccessCount());
    assertEquals(1, cache.getHits());  // should have 1 hit now
    assertEquals(1, cache.getMisses());
  }


  public void testMiss() throws Exception {
    result200 = cache.compute(200);
    assertEquals(200, result200.length());  // a new result should have been generated
    // all stats except hit count should have been incremented
    assertEquals(2, cache.getCacheSize());
    assertEquals(2, cache.getAccessCount());
    assertEquals(0, cache.getHits());  // should have 1 hit now
    assertEquals(2, cache.getMisses());
  }

  public void testExpiration() throws Exception {
    int limit = cache.getSizeLimit();
    assertEquals(16, limit);
    // make sure that cache hits don't count toward size limit
    int newHits = 20;
    for (int i = 1; i <= newHits; i++) {
      assertSame(result100, cache.compute(100));  // the same result should have been reused
      // cache size should not have changed - only the access count
      assertEquals(1, cache.getCacheSize());
      assertEquals(i+1, cache.getAccessCount());
      assertEquals(i, cache.getHits());  // should have 1 hit now
      assertEquals(1, cache.getMisses());
    }
    int hitCount = cache.getHits();
    int accessCount = cache.getAccessCount();
    // saturate cache with misses
    for (int i = 1; i < limit; i++) {
      int len = 100 + i;
      assertEquals(len, cache.compute(len).length());  // a new result should have been computed
      // cache size should not have changed - only the access count
      assertEquals(i+1, cache.getCacheSize());
      assertEquals(accessCount+i, cache.getAccessCount());
      assertEquals(hitCount, cache.getHits());  // hit count should stay the same
      assertEquals(i+1, cache.getMisses());
    }
    // at this point the cache should be saturated, but still contain the first result
    // if we add another result to the cahce, the first one should expire
    assertEquals(limit, cache.getCacheSize());
    assertEquals(5, cache.compute(5).length());
    assertEquals(limit, cache.getCacheSize());
    assertNotSame(result100, cache.compute(100));
  }

}