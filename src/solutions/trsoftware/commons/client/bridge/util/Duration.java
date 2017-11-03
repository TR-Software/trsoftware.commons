package solutions.trsoftware.commons.client.bridge.util;

import solutions.trsoftware.commons.client.util.TimeUnit;

/**
 * Apr 28, 2011
 *
 * @author Alex
 */
public interface Duration {

  /**
   * @return the number of milliseconds that have elapsed since this object was created.
   */
  double elapsedMillis();

  /**
   * @return the time elapsed since this object was created, expressed in the given unit.
   */
  double elapsed(TimeUnit timeUnit);

  /** @return true if more than the given time value has elapsed */
  boolean exceeds(long value, TimeUnit timeUnit);

  /**
   * Computes processing speed.
   * @param nOperations the number of operations carried out since this Duration was instantiated
   * @param timeUnit the time unit for the speed computation (e.g. per second, per minute, per hour, etc.)
   * @return the processing speed as {@code nOperations} per {@code timeUnit}
   */
  double computeSpeed(int nOperations, TimeUnit timeUnit);  // TODO(11/1/2016): use this new method everywhere Duration is used to compute processing speed

}
