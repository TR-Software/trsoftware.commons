package solutions.trsoftware.commons.server;

import solutions.trsoftware.commons.server.util.CanStopClock;
import solutions.trsoftware.commons.server.util.Clock;

/**
 * All unit tests that use the Clock must extend this class so that
 * the clock is cleaned up after the test.
 * 
 * Jun 30, 2009
 *
 * @author Alex
 */
public abstract class TestCaseCanStopClock extends SuperTestCase implements CanStopClock {

  @Override
  protected void tearDown() throws Exception {
    Clock.resetToNormal(); // make sure to leave the clock in an un-tampered state
    super.tearDown();
  }
}
