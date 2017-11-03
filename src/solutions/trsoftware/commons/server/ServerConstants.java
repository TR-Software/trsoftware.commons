package solutions.trsoftware.commons.server;

import solutions.trsoftware.commons.server.util.CanStopClock;
import solutions.trsoftware.commons.server.util.ServerRuntimeUtils;

/**
 * Jun 30, 2009
 *
 * @author Alex
 */
public interface ServerConstants {
  // save the value of this method as a constant so that the code relying on this check can be optimized by the compiler
  boolean IN_UNIT_TEST = ServerRuntimeUtils.runningInJUnit();
  /** requires each unit test that needs to stop the clock to explicitly inherit from CanStopClock */
  boolean IS_CLOCK_STOPPABLE = ServerRuntimeUtils.isClassOnStack(CanStopClock.class);
}
