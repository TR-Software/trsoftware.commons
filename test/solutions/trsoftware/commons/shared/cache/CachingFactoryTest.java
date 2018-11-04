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

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.util.StringUtils;
import solutions.trsoftware.commons.shared.util.callables.Function1;
import solutions.trsoftware.commons.shared.util.mutable.MutableInteger;

/**
 * Dec 10, 2008
 *
 * @author Alex
 */
public class CachingFactoryTest extends TestCase {

  public void testGetOrInsert() throws Exception {
    final MutableInteger factoryMethodInvocationCount = new MutableInteger();
    CachingFactory<String, Integer> cachingFactory = new CachingFactory<String, Integer>(
        new Function1<String, Integer>() {
          public Integer call(String parameter) {
            factoryMethodInvocationCount.incrementAndGet();
            if (parameter == null)
              return 0;
            return parameter.length();
          }
        }
    );

    for (int i = 0; i < 10; i++) {
      assertEquals(i, factoryMethodInvocationCount.get());

      // try each result twice, to make sure the cached copy is used the second time
      String str = StringUtils.randString(i);
      Integer result = cachingFactory.getOrInsert(str);
      assertNotNull(result);
      assertEquals(i, result.intValue());
      assertEquals(i+1, factoryMethodInvocationCount.get());

      Integer result2 = cachingFactory.getOrInsert(str);
      assertNotNull(result2);
      assertSame(result, result2);
      assertEquals(i, result2.intValue());
      assertEquals(i + 1, factoryMethodInvocationCount.get());
    }
    assertEquals(10, factoryMethodInvocationCount.get());
  }
}