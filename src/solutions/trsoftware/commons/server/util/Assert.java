package solutions.trsoftware.commons.server.util;

import solutions.trsoftware.commons.server.ServerConstants;

/**
 * Dec 30, 2009
 *
 * @author Alex
 */
public class Assert {

  public static void assertInUnitTestOrCanStopClock() {
    if (!ServerConstants.IN_UNIT_TEST && !ServerConstants.IS_CLOCK_STOPPABLE)
      throw new IllegalStateException("This code is intended for testing and should not be invoked from this context.  Did you forget to inherit CanStopClock?");
  }

  public static void assertNotInUnitTest() {
    if (ServerConstants.IN_UNIT_TEST)
      throw new IllegalStateException("This code is not intended for unit testing and should not be invoked from this context.");
  }


  public static void assertTrue(boolean condition) {
    solutions.trsoftware.commons.client.util.Assert.assertTrue(condition);
  }


}
