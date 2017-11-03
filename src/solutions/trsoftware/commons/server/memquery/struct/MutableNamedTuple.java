package solutions.trsoftware.commons.server.memquery.struct;

/**
 * A tuple whose elements can be accessed by name.
 *
 * @author Alex, 1/15/14
 */
public interface MutableNamedTuple<T extends MutableOrderedTuple> extends NamedTuple {

  /**
   * @param <V> the value type
   * @param <T> the type of this tuple-like object
   * @return {@code this}, for method chaining
   */
  <V, T> T setValue(String name, V value);
}
