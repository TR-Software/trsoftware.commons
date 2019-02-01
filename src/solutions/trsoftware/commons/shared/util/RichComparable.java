package solutions.trsoftware.commons.shared.util;

import static solutions.trsoftware.commons.shared.util.compare.ComparisonOperator.*;

/**
 * @author Alex
 * @since 1/10/2019
 */
public interface RichComparable<T> extends Comparable<T> {

  /**
   * A "rich" comparison method using the result of {@link Comparable#compareTo(Object)}
   *
   * @return {@code true} iff this instance is greater than the given arg, in accordance with the
   * <i>natural ordering</i> imposed by this {@link Comparable}.
   */
  default boolean greaterThan(T o) {
    return GT.compare(this, o);
  }

  /**
   * A "rich" comparison method using the result of {@link Comparable#compareTo(Object)}
   *
   * @return {@code true} iff this instance is greater than or equal to the given arg, in accordance with the
   * <i>natural ordering</i> imposed by this {@link Comparable}.
   */
  default boolean greaterThanOrEqualTo(T o) {
    return GEQ.compare(this, o);
  }

  /**
   * A "rich" comparison method using the result of {@link Comparable#compareTo(Object)}
   *
   * @return {@code true} iff this instance is equal to the given arg, in accordance with the
   * <i>natural ordering</i> imposed by this {@link Comparable}.
   */
  default boolean equalTo(T o) {
    return EQ.compare(this, o);
  }

  /**
   * A "rich" comparison method using the result of {@link Comparable#compareTo(Object)}
   *
   * @return {@code true} iff this instance is not equal to the given arg, in accordance with the
   * <i>natural ordering</i> imposed by this {@link Comparable}.
   */
  default boolean notEqualTo(T o) {
    return NEQ.compare(this, o);
  }

  /**
   * A "rich" comparison method using the result of {@link Comparable#compareTo(Object)}
   *
   * @return {@code true} iff this instance is less than or equal to the given arg, in accordance with the
   * <i>natural ordering</i> imposed by this {@link Comparable}.
   */
  default boolean lessThanOrEqualTo(T o) {
    return LEQ.compare(this, o);
  }

  /**
   * A "rich" comparison method using the result of {@link Comparable#compareTo(Object)}
   *
   * @return {@code true} iff this instance is less than the given arg, in accordance with the
   * <i>natural ordering</i> imposed by this {@link Comparable}.
   */
  default boolean lessThan(T o) {
    return LT.compare(this, o);
  }
}