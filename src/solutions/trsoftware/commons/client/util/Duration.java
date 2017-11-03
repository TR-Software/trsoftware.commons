package solutions.trsoftware.commons.client.util;

import static com.google.gwt.core.client.Duration.currentTimeMillis;

/**
 * Adapts GWT's Duration class (a utility class for measuring elapsed time)
 * for compatibility with the solutions.trsoftware.commons.client.bridge.util.Duration
 * interface (so the same interface can be used on both the client and server).
 */
public class Duration extends AbstractDuration {

  private double start;

  /**
   * Creates a new Duration whose start time is now.
   */
  public Duration() {
    this("", "");
  }

  /**
   * Creates a new Duration whose start time is now, with a name.
   * The toString method will return "{name} took {duration} {timeUnit}"
   */
  public Duration(String name) {
    this(name, "took");
  }


  /**
   * Creates a new Duration whose start time is now, with a name and action.
   * The toString method will return "{name} {action} {duration} {timeUnit}"
   */
  public Duration(String name, String action) {
    super(action, name);
    start = currentTimeMillis();
  }

  /**
   * Returns the number of milliseconds that have elapsed since this object was
   * created.
   */
  public double elapsedMillis() {
    return currentTimeMillis() - start;
  }


}