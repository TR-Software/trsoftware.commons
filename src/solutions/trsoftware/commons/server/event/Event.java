package solutions.trsoftware.commons.server.event;

import java.util.function.Predicate;

/**
 * Convenience marker interface for event objects.
 * <p>
 * Provides access to the event creation time (see {@link #getTimestamp()}) as well as utility methods
 * for filtering events by their creation time (see {@link #isTimestampBetween(long, long)} and
 * {@link #timestampFilter(long, long)}).
 *
 * @author Alex
 * @since 10/9/2019
 */
public interface Event {

  /**
   * @return the time (in epoch millis) when this event occurred.
   */
  long getTimestamp();

  /**
   * @param startTime the lower bound in epoch millis (inclusive)
   * @param endTime the upper bound in epoch millis (exclusive)
   * @return {@code true} iff the timestamp of this event is within the given bounds
   */
  default boolean isTimestampBetween(long startTime, long endTime) {
    long timestamp = getTimestamp();
    return timestamp >= startTime && timestamp < endTime;
  }

  /**
   * @param startTime the lower bound in epoch millis (inclusive)
   * @param endTime the upper bound in epoch millis (exclusive)
   * @return a predicate that returns {@code true} iff the timestamp of this event is within the given bounds
   */
  static Predicate<Event> timestampFilter(long startTime, long endTime) {
    return event -> event.isTimestampBetween(startTime, endTime);
  }
}
