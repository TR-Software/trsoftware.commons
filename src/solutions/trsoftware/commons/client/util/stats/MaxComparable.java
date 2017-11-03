package solutions.trsoftware.commons.client.util.stats;

import java.io.Serializable;

/**
 * Keeps track of the maximum in a sequence of Comparable objects.
 *
 * @author Alex
 */
public class MaxComparable<T extends Comparable<T>> extends MinMaxComparableBase<T> implements Serializable {
  public MaxComparable() {
    super(1);
  }

  public MaxComparable(Iterable<T> candidates) {
    super(1, candidates);
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MaxComparable max = (MaxComparable)o;

    if (get() != null ? !get().equals(max.get()) : max.get() != null) return false;

    return true;
  }

  public int hashCode() {
    return (get() != null ? get().hashCode() : 0);
  }

  /**
   * @return The max of the given comparable objects.
   */
  public static <T extends Comparable<T>> T eval(T... candidates) {
    return new MaxComparable<T>().updateAll(candidates);
  }
}