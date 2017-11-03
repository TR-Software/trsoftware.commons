package solutions.trsoftware.commons.server.util;

import solutions.trsoftware.commons.client.util.callables.Function0;
import solutions.trsoftware.commons.client.util.callables.Function1;
import solutions.trsoftware.commons.client.util.callables.Function2;
import static solutions.trsoftware.commons.server.util.ServerMapUtils.getOrInsertConcurrent;
import junit.framework.TestCase;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Oct 28, 2009
 *
 * @author Alex
 */
public class ServerMapUtilsTest extends TestCase {
  ServerMapUtils serverMapUtils;

  public void testgetOrInsertConcurrentConcurrent() throws Exception {
    ConcurrentMap<String, String> map = new ConcurrentHashMap<String, String>();
    map.put("a", "x");
    final String foo = "foo";
    String bar = "bar";
    assertEquals("x", getOrInsertConcurrent(map, "a", foo));  // the prior value is returned
    assertSame(bar, getOrInsertConcurrent(map, "b", bar));  // the new value is returned for a new key

    // repeat the same experiment with a factory method
    Function0<String> factoryNoArgs = new Function0<String>() {
      public String call() {
        return foo;
      }
    };
    assertEquals(bar, getOrInsertConcurrent(map, "b", factoryNoArgs));
    assertSame(foo, getOrInsertConcurrent(map, "c", factoryNoArgs));

    // test the factory with the 1 args version
    Function1<Integer, String> factory1Arg = new Function1<Integer, String>() {
      public String call(Integer arg) {
        return foo + arg;
      }
    };
    assertEquals(bar, getOrInsertConcurrent(map, "b", factory1Arg, 123));
    assertEquals("foo123", getOrInsertConcurrent(map, "d", new Function1<Integer, String>() {
      public String call(Integer arg) {
        return foo+arg;
      }
    }, 123));

    // test the factory with args version
    Function2<Integer, Double, String> factory2Args = new Function2<Integer, Double, String>() {
      public String call(Integer arg1, Double arg2) {
        return foo + arg1 + arg2;
      }
    };
    assertEquals(bar, getOrInsertConcurrent(map, "b", factory2Args, 123, 2.3));
    assertEquals("foo1232.3", getOrInsertConcurrent(map, "e", factory2Args, 123, 2.3));
  }

}