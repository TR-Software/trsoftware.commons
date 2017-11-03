package solutions.trsoftware.commons.client.util;

/**
 * Helps ensure that a certain event occurs a limited number of times.
 * Provides an alternative to MutableInteger or AtomicInteger for situations
 * where the limit check will be perfomed in more than 1 place (encapsulates
 * the limit and the limit checking logic).
 *
 * Jan 21, 2010
 *
 * @author Alex
 */
public class LimitedCounter {
  private int count;
  private final int limit;

  public LimitedCounter(int limit) {
    // it doesn't make sense to have a counter with limit of less than 1
    if (limit < 1)
      throw new IllegalArgumentException("LimitedCounter limit must be positive");
    this.limit = limit;
  }

  /**
   * Increments the counter.
   * @return true if this call broke the barrier, i.e. the count was less than
   * the limit *prior* to incrementation and now is equal to the limit
   */
  public boolean increment() {
    count++;
    return count == limit;
  }

  /** @return true if the count is greater or equal to the limit */
  public boolean metLimit() {
    return count >= limit;
  }

  public int getCount() {
    return count;
  }
}