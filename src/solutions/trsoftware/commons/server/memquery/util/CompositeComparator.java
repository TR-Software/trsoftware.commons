package solutions.trsoftware.commons.server.memquery.util;

import java.util.Comparator;
import java.util.List;

/**
 * Encapsulates a list of internal Comparators to which it delegates, using each subsequent Comparator to as a tie-breaker
 * for its predecessors.
 *
 * @author Alex, 1/9/14
 */
public class CompositeComparator<T> implements Comparator<T> {

  private final List<Comparator<T>> comparators;

  public CompositeComparator(List<Comparator<T>> delegates) {
    this.comparators = delegates;
  }

  @Override
  public int compare(T o1, T o2) {
    for (Comparator<T> next : comparators) {
      int result = next.compare(o1, o2);
      if (result != 0)
        return result;
    }
    return 0;
  }
}
