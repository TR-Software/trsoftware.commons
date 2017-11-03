package solutions.trsoftware.commons.server.util;

import solutions.trsoftware.commons.client.util.AbstractDuration;

/**
 * Serverside implementation of solutions.trsoftware.commons.client.bridge.util.Duration
 * which uses nanosecond precision which is only available in a JVM.
 */
public class NanoDuration extends AbstractDuration {

  public static final double NANOS_IN_MILLIS = 1000000;  // 1 million nanoseconds in 1 millisecond

  private long startNanos;

  /**
   * Creates a new Duration whose start time is now.
   */
  public NanoDuration() {
    this("", "");
  }

  /**
   * Creates a new Duration whose start time is now, with a name.
   * The toString method will return "{name} took {duration} {timeUnit}"
   */
  public NanoDuration(String name) {
    this(name, "took");
  }


  /**
   * Creates a new Duration whose start time is now, with a name and action.
   * The toString method will return "{name} {action} {duration} {timeUnit}"
   */
  public NanoDuration(String name, String action) {
    super(name, action);
    startNanos = System.nanoTime();
  }

  /**
   * Returns the number of milliseconds that have elapsed since this object was
   * created.
   */
  @Override
  public double elapsedMillis() {
    return (double)(System.nanoTime() - startNanos) / NANOS_IN_MILLIS;
  }

}