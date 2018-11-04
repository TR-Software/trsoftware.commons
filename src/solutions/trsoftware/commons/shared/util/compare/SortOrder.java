package solutions.trsoftware.commons.shared.util.compare;

import java.util.Comparator;

/**
 * Defines the 2 possible comparison orders for comparable objects:
 * ascending (aka "natural") and descending (aka "reversed").
 *
 * Also implements {@link Comparable} for convenience.
 *
 * @see java.util.Comparators.NaturalOrderComparator
 *
 * @author Alex
 * @since 8/9/2018
 */
public enum SortOrder implements Comparator<Comparable> {
  /**
   * Ascending (aka "natural") ordering.
   * @see Comparable#compareTo(Object)
   * @see Comparator#naturalOrder()
   */
  ASC(1),
  /**
   * Descending (aka "reversed") ordering.
   * @see Comparator#reverseOrder()
   */
  DESC(-1);
  /**
   * The multiplier to use when comparing 2 elements according to this sort order:
   * {@code 1} for natural ordering, {@code -1} for reversed ordering
   */
  public final int multiplier;

  SortOrder(int multiplier) {
    this.multiplier = multiplier;
  }

  @Override
  @SuppressWarnings("unchecked")
  public int compare(Comparable o1, Comparable o2) {
    return multiplier * o1.compareTo(o2);
  }

  /**
   * Same as {@link #compare(Comparable, Comparable)}, but allows {@code null} values.  The ordering of {@code null}s
   * is determined as follows:
   * <ul>
   *   <li>{@link #ASC}: nulls first</li>
   *   <li>{@link #DESC}: nulls last</li>
   * </ul>
   * This method first checks for null values (as defined above), and then delegates to {@link #compare(Comparable, Comparable)}
   * if the given objects implement {@link Comparable}.
   *
   * @see java.util.Comparators.NullComparator
   */
  @SuppressWarnings("unchecked")
  public int compareNull(Object a, Object b) {
    boolean nullFirst = multiplier == 1;
    if (a == null) {
        return (b == null) ? 0 : (nullFirst ? -1 : 1);
    } else if (b == null) {
        return nullFirst ? 1 : -1;
    } else {
      if (a instanceof Comparable && b instanceof Comparable)
        return compare((Comparable)a, (Comparable)b);
      return 0;
    }
  }
}
