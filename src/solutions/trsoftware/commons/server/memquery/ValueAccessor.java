package solutions.trsoftware.commons.server.memquery;

/**
 * Accesses a value from a given row.
 *
 * @param <V> the type of the returned value.
 * @author Alex, 1/5/14
 */
public interface ValueAccessor<V> {
  /**
   * @return the value of this column for the given row (e.g. the value of a field)
   */
  V getValue(Row row);
}
