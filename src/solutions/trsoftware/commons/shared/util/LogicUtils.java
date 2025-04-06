/*
 * Copyright 2022 TR Software Inc.
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

package solutions.trsoftware.commons.shared.util;

import com.google.common.base.MoreObjects;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Can be used to shorten some if statements that are too long.
 *
 * @author Alex
 */
public class LogicUtils {

  public static boolean bothNotNullAndEqual(Object o1, Object o2) {
    return bothNotNull(o1, o2) && o1.equals(o2);
  }

  public static boolean bothNotNullAndNotEqual(Object o1, Object o2) {
    return bothNotNull(o1, o2) && !o1.equals(o2);
  }

  public static boolean bothNotNull(Object o1, Object o2) {
    return o1 != null && o2 != null;
  }

  public static boolean bothNull(Object o1, Object o2) {
    return o1 == null && o2 == null;
  }

  /**
   * Same as {@link Objects#equals(Object, Object)}.
   * <p>
   * <b>Notes:</b>
   * <ul>
   *   <li>
   *   This method provides a workaround for a <a href="https://github.com/gwtproject/gwt/issues/9522">GWT bug</a>
   *   that makes it impossible to use {@link Objects#equals(Object, Object)} with {@link String} arguments in classic DevMode.
   *   <li>
   *   The performance of this method (as well as of {@link java.util.Objects#equals(Object, Object)})
   *   could be worse than writing the equivalent logical expression inline where the argument types can be determined
   *   statically (see <a href="https://bugs.openjdk.java.net/browse/JDK-8015417">JDK-8015417</a>).
   * </ul>
   * @return true if the specified arguments are equal, or both null
   * @see java.util.Objects#equals(Object, Object)
   */
  public static boolean eq(Object a, Object b) {
    //noinspection EqualsReplaceableByObjectsCall
    return (a == b) || (a != null && a.equals(b));
  }

  /**
   * Returns the first of the two given args that is not {@code null}. If both are {@code null}, returns {@code null}.
   * <p>
   * NOTE: this is different from {@link MoreObjects#firstNonNull(Object, Object)}, which throws a {@link NullPointerException}
   * when both args are {@code null}.
   *
   * @return {@code o1} if it's not {@code null}, otherwise {@code o2} (which could be {@code null})
   * @see #firstNonNull(Object[])
   * @see #nonNullOrElse(Object, Supplier)
   * @see MoreObjects#firstNonNull(Object, Object)
   */
  @Nullable
  public static <T> T firstNonNull(T o1, T o2) {
    if (o1 != null)
      return o1;
    return o2;
  }

  /**
   * Returns the first of the given args that is not {@code null}. If all are {@code null}, returns {@code null}.
   *
   * @return the first of the given args which is not {@code null},
   *     or {@code null} if there isn't a non-{@code null} input element.
   * @see #firstNonNull(Object, Object)
   * @see #nonNullOrElse(Object, Supplier)
   */
  @Nullable
  @SafeVarargs
  public static <T> T firstNonNull(T... objects) {
    for (T x : objects) {
      if (x != null)
        return x;
    }
    return null;
  }

  /**
   * Returns the given object if it's not {@code null}, otherwise returns the value produced by the given supplier.
   * <p>
   * Equivalent to <pre>
   *   Optional.of(value).orElseGet(other)
   * </pre>
   *
   * @return {@code obj} if it's not {@code null}, otherwise {@code supplier.get()}
   * @see #firstNonNull(Object, Object)
   * @see Optional#orElseGet(Supplier)
   * @see StringUtils#notBlankOrElse(String, Supplier)
   */
  @Nullable
  public static <T> T nonNullOrElse(T value, Supplier<T> other) {
    if (value != null)
      return value;
    return other.get();
  }
}
