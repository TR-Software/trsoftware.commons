package solutions.trsoftware.commons.client.util.time;

/**
 * @author Alex, 3/24/2015
 */
public abstract class Time {

  public abstract double currentTimeMillis();

  /**
   * @return the number of millis remaining until the given timestamp.
   */
  public final double getMillisUntil(double timestamp) {
    return timestamp - currentTimeMillis();
  }

  /**
   * @return the number of millis elapsed since the given timestamp.
   */
  public final double getMillisSince(double timestamp) {
    return currentTimeMillis() - timestamp ;
  }
}
