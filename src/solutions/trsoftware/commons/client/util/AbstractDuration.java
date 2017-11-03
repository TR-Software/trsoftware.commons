package solutions.trsoftware.commons.client.util;

import solutions.trsoftware.commons.client.bridge.util.Duration;

/**
 * Apr 29, 2011
 *
 * @author Alex
 */
public abstract class AbstractDuration implements Duration {
  /** Optional name for pretty printing the duration */
  protected String action;
  /** Optional action for pretty printing the duration */
  protected String name;

  public AbstractDuration(String action, String name) {
    this.action = action;
    this.name = name;
  }

  public String getName() {
    return name;
  }

  /** @return reference to itself for method chaining */
  public AbstractDuration setName(String name) {
    this.name = name;
    return this;
  }

  /**
   * Returns the time elapsed since this object was created, expressed in the given unit.
   */
  @Override
  public double elapsed(TimeUnit timeUnit) {
    return timeUnit.fromMillis(elapsedMillis()) ;
  }

  /** @return true if more than the given time value has elapsed */
  @Override
  public boolean exceeds(long value, TimeUnit timeUnit) {
    return elapsedMillis() > timeUnit.toMillis(value);
  }

  @Override
  public String toString() {
    int elapsed = (int)elapsedMillis();
    return name + " " + action  + " " + elapsed + " ms.";
  }

  /**
   * Computes processing speed.
   * @param nOperations the number of operations carried out since this Duration was instantiated
   * @param timeUnit the time unit for the speed computation (e.g. per second, per minute, per hour, etc.)
   * @return the processing speed as {@code nOperations} per {@code timeUnit}
   */
  @Override
  public double computeSpeed(int nOperations, TimeUnit timeUnit) {
    return (double)nOperations / elapsed(timeUnit);
  }
}
