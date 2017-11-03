package solutions.trsoftware.commons.client.util.stats;

/**
 * Indicates that an instance can update itself from a value of type double.
 *
 * @author Alex, 1/7/14
 */
public interface UpdatableDouble {
  /**
   * Updates itself with the given value.
   */
  public void update(double x);
}
