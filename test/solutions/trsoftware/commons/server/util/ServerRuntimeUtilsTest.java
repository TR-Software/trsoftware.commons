package solutions.trsoftware.commons.server.util;
/**
 *
 * Date: Sep 17, 2008
 * Time: 6:34:59 PM
 * @author Alex
 */

import junit.framework.TestCase;

import java.util.concurrent.atomic.AtomicReference;

public class ServerRuntimeUtilsTest extends TestCase {

  public void testRunningInJUnit() throws Exception {
    // this thread is running in JUnit
    assertTrue(ServerRuntimeUtils.runningInJUnit());

    final AtomicReference<Boolean> innerThreadInJunit = new AtomicReference<Boolean>(null);

    // the inner thread is also consider "in JUnit" because it's enclosing class is a test case
    Thread innerThread = new Thread() {
      @Override
      public void run() {
        innerThreadInJunit.set(ServerRuntimeUtils.runningInJUnit());
      }
    };
    innerThread.start();
    innerThread.join();
    assertNotNull(innerThreadInJunit.get());
    assertTrue(innerThreadInJunit.get()); // the inner thread was not launched by JUnit

    // this thread will not be considered as running in JUnit because it's not defined inside a test class.
    ServerRuntimeUtilsTestThread outerThread = new ServerRuntimeUtilsTestThread();
    outerThread.start();
    outerThread.join();
    assertNotNull(outerThread.getRanInJUnit());
    assertFalse(outerThread.getRanInJUnit()); // the outer thread was not launched by JUnit
  }

}