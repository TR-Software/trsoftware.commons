package solutions.trsoftware.commons.shared.util;

import junit.framework.TestCase;

import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertThrows;

/**
 * @author Alex
 * @since 4/18/2019
 */
public class RunOnlyOnceGuardTest extends TestCase {

  public void testCheck() throws Exception {
    RunOnlyOnceGuard guard = new RunOnlyOnceGuard();
    assertFalse(guard.isLocked());
    String msg = "RunOnlyOnceGuardTest.testCheck already ran";
    guard.check(msg);  // first invocation shouldn't throw exception
    assertTrue(guard.isLocked());
    // but all subsequent invocations should throw ISE
    for (int i = 0; i < 2; i++) {
      assertThrows(new IllegalStateException(msg), (Runnable)() -> guard.check(msg));
      assertTrue(guard.isLocked());
    }
  }
}