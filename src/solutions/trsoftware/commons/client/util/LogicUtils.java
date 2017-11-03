/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.client.util;

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

  /** Like {@link Object#equals(Object)}, but allows {@code null} values */
  public static boolean eq(Object o1, Object o2) {
    if (o1 != null)
      return o1.equals(o2);
    else if (o2 == null)
      return true;  // both null
    return false; // one is null
  }

  // TODO: unit test these new methods
  /**
   * Similar to a the Javascript expression {@code o1 || o2} when applied to non-boolean objects.
   * @return o1 if it's not null, otherwise o2.
   */
  public static <T> T firstNonNull(T o1, T o2) {
    if (o1 != null)
      return o1;
    return o2;
  }

  /**
   * @return the first of the given elements which is not null, or {@code null} if there isn't a non-null input element.
   */
  public static <T> T firstNonNull(T... objects) {
    for (T x : objects) {
      if (x != null)
        return x;
    }
    return null;
  }
}
