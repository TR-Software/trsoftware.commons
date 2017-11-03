package solutions.trsoftware.commons.client.util.stats;

/**
 * Indicates that an instance can update itself from single values of some data type.
 *
 * @author Alex, 1/7/14
 */
public interface Updatable<T> {
  /**
   * Updates itself with the given value.
   */
  public void update(T x);
}
