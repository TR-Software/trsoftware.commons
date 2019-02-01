package solutions.trsoftware.commons.shared.util.iterators;

import java.util.Iterator;

/**
 * An {@link Iterator} optimized for primitive {@code int} elements:
 * provides the {@link #nextInt()} method to avoid auto-boxing.
 *
 * @author Alex
 * @since 1/11/2019
 */
public interface IntIterator extends Iterator<Integer> {

  /**
   * Same as {@link #next()}, but returns a primitive {@code int}.
   * <p>
   * <em>Implementation Note</em>: must obey the contract of {@link #next()}
   *
   * @return the next element in the iteration
   */
  int nextInt();
}
