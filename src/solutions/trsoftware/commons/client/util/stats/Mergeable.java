package solutions.trsoftware.commons.client.util.stats;

/**
 * Indicates that an instance can incorporate into itself data from another instance (for map-reduce-style processing).
 *
 * @author Alex, 1/7/14
 */
public interface Mergeable<T> {
  /**
   * Merges in data from another instance.
   */
  public void merge(T other);
}
