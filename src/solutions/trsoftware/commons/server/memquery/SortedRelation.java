package solutions.trsoftware.commons.server.memquery;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * A materialized sorted relation.
 *
 * @author Alex
 * @since 1/16/2019
 */
public interface SortedRelation extends Relation {
  /**
   * @return the {@link Comparator} used to sort this relation
   */
  Comparator<Row> getComparator();

  /**
   * @return the sort orders for this sorted relation
   */
  List<SortOrder> getSortOrders();

  /**
   * Iterates all the rows matching the given query according to the comparator.
   *
   * @param query a {@link Row} instance that would match the desired results (according to the comparator)
   *
   * @see #getComparator()
   * @see #getSortOrders()
   */
  Iterator<Row> iterMatches(Row query);
}
