package solutions.trsoftware.commons.shared.util.compare;

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
  default boolean isGreaterThan(T o) {
    return GT.compare(this, o);
  }

  /**
   * A "rich" comparison method using the result of {@link Comparable#compareTo(Object)}
   *
   * @return {@code true} iff this instance is greater than or equal to the given arg, in accordance with the
   * <i>natural ordering</i> imposed by this {@link Comparable}.
   */
  default boolean isGreaterThanOrEqualTo(T o) {
    return GE.compare(this, o);
  }

  /**
   * A "rich" comparison method using the result of {@link Comparable#compareTo(Object)}
   *
   * @return {@code true} iff this instance is equal to the given arg, in accordance with the
   * <i>natural ordering</i> imposed by this {@link Comparable}.
   */
  default boolean isEqualTo(T o) {
    return EQ.compare(this, o);
  }

  /**
   * A "rich" comparison method using the result of {@link Comparable#compareTo(Object)}
   *
   * @return {@code true} iff this instance is not equal to the given arg, in accordance with the
   * <i>natural ordering</i> imposed by this {@link Comparable}.
   */
  default boolean isNotEqualTo(T o) {
    return NE.compare(this, o);
  }

  /**
   * A "rich" comparison method using the result of {@link Comparable#compareTo(Object)}
   *
   * @return {@code true} iff this instance is less than or equal to the given arg, in accordance with the
   * <i>natural ordering</i> imposed by this {@link Comparable}.
   */
  default boolean isLessThanOrEqualTo(T o) {
    return LE.compare(this, o);
  }

  /**
   * A "rich" comparison method using the result of {@link Comparable#compareTo(Object)}
   *
   * @return {@code true} iff this instance is less than the given arg, in accordance with the
   * <i>natural ordering</i> imposed by this {@link Comparable}.
   */
  default boolean isLessThan(T o) {
    return LT.compare(this, o);
  }
}