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

package solutions.trsoftware.commons.server.util.function;

import java.util.function.Predicate;

/**
 * Utility methods for working with Java 8 {@link java.util.function functions}
 *
 * @author Alex
 * @since 5/3/2018
 */
public class FunctionalUtils {


  /**
   * Equivalent to
   * <pre>
   *   predicate1.test(arg) && predicate2.test(arg) && ... && predicateN.test(arg)
   * </pre>
   * @return {@code true} iff all the predicates match the given arg.
   */
  public static <T> boolean testAll(Iterable<? extends Predicate<T>> predicates, T arg) {
    for (Predicate<T> predicate : predicates) {
      if (!predicate.test(arg))
        return false;
    }
    return true;
  }

}
