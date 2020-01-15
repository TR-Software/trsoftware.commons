/*
 * Copyright 2018 TR Software Inc.
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
 *
 */

package solutions.trsoftware.commons.shared.util;

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
   * Like {@link Object#equals(Object)}, but allows {@code null} values.
   * <p>
   * <b>NOTE:</b> the performance of this method (as well as of {@link java.util.Objects#equals(Object, Object)})
   * might be worse than writing the equivalent logical expression inline where the argument types can be determined
   * statically (see <a href="https://bugs.openjdk.java.net/browse/JDK-8015417">JDK-8015417</a>).
   *
   * @return true if the specified arguments are equal, or both null
   * @see java.util.Objects#equals(Object, Object)
   */
  public static boolean eq(Object o1, Object o2) {
    return o1 == null ? o2 == null : o1.equals(o2);
  }

  /**
   * Similar to the Javascript expression {@code o1 || o2} when applied to non-boolean objects.
   * @return {@code o1} if it's not {@code null}, otherwise {@code o2} (which could be null).
   */
  public static <T> T firstNonNull(T o1, T o2) {
    if (o1 != null)
      return o1;
    return o2;
  }

  /**
   * @return the first of the given elements which is not {@code null},
   * or {@code null} if there isn't a non-{@code null} input element.
   */
  @SafeVarargs
  public static <T> T firstNonNull(T... objects) {
    for (T x : objects) {
      if (x != null)
        return x;
    }
    return null;
  }
}
