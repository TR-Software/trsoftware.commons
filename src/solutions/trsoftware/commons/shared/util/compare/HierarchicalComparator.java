package solutions.trsoftware.commons.shared.util.compare;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static solutions.trsoftware.commons.shared.util.ListUtils.isEmpty;

/**
 * Performs a pairwise comparison on list elements based on their order of significance
 * (the first element is considered most-significant).
 *
 * Examples (given {@link SortOrder#DESC} - the default):
 * <ul>
 *   <li>{@code [1, 2, 3] == [1, 2, 3]}</li>
 *   <li>{@code [1, 2, 3] < [1, 2, 3, 4]}</li>
 *   <li>{@code [1, 2, 3] < [1, 2, 4]}</li>
 *   <li>{@code [1, 2, 3] < [2]}</li>
 *   <li>{@code [1, 2, 3] > [1, 2]}</li>
 *   <li>{@code [1, 2, 3] > []}</li>
 *   <li>{@code [1, 2, 3] > null}</li>
 * </ul>
 *
 * @param <E> the element type of the lists
 *
 * @author Alex
 * @since 8/9/2018
 */
public class HierarchicalComparator<E extends Comparable<E>> implements Comparator<List<E>> {

  /**
   * Ascending or descending order
   */
  private SortOrder sortOrder;

  public HierarchicalComparator() {
    this(SortOrder.ASC);
  }

  public HierarchicalComparator(SortOrder sortOrder) {
    this.sortOrder = sortOrder;
  }

  @Override
  public int compare(List<E> list1, List<E> list2) {
    // 1) check for null/empty lists
    int cmp = sortOrder.compareNull(
        isEmpty(list1) ? null : list1,
        isEmpty(list2) ? null : list2
    );
    if (cmp != 0 || list1 == null || list2 == null)
      return cmp;
    // 2) compare element-by-element in order of significance
    Iterator<E> iter1 = list1.iterator();
    Iterator<E> iter2 = list2.iterator();
    boolean hasNext1;
    boolean hasNext2;
    do {
      hasNext1 = iter1.hasNext();
      hasNext2 = iter2.hasNext();

      E e1 = hasNext1 ? iter1.next() : null;
      E e2 = hasNext2 ? iter2.next() : null;
      cmp = sortOrder.compareNull(e1, e2);
    }
    while (cmp == 0 && hasNext1 && hasNext2);
    return cmp;
  }

  public SortOrder getSortOrder() {
    return sortOrder;
  }
}
