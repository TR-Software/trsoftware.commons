/*
 * Copyright 2021 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

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