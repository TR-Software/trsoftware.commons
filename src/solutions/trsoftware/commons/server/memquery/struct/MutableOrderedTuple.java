package solutions.trsoftware.commons.server.memquery.struct;

/**
 * A collection of items indexed by their ordinal number.
 *
 * @author Alex, 1/10/14
 */
public interface MutableOrderedTuple<T extends MutableOrderedTuple> extends OrderedTuple {

  /**
   * @param <V> the value type
   * @param <T> the type of this tuple-like object
   * @return {@code this}, for method chaining
   */
  <V, T> T setValue(int index, V value);
}
