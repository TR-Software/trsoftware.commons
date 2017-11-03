package solutions.trsoftware.commons.client.cache;

import solutions.trsoftware.commons.client.util.mutable.MutableInteger;
import solutions.trsoftware.commons.client.util.StringUtils;
import solutions.trsoftware.commons.client.util.callables.Function1;
import junit.framework.TestCase;

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