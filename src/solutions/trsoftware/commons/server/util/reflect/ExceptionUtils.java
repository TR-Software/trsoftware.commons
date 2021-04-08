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

package solutions.trsoftware.commons.server.util.reflect;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

import javax.annotation.Nullable;

/**
 * Provides reflection-based utilities for examining exceptions, which are not already provided
 * by other external libraries such as Guava's {@link Throwables} or Apache Commons'
 * {@link org.apache.commons.lang3.exception.ExceptionUtils}
 *
 * @author Alex
 * @since 7/30/2019
 */
public class ExceptionUtils {

  /** Static class not instantiable */
  private ExceptionUtils() {
  }

  /**
   * Finds the top-most exception in the causal chain of the given throwable that matches the given type.
   * Similar to {@link Throwables#getCausalChain(Throwable)}, but does not check for cycles in the causal chain,
   * thus a reasonable recursion depth limit should be specified.
   * <p>
   *   NOTE: this could also be accomplished by filtering the list returned by {@link Throwables#getCausalChain(Throwable)},
   *   but that would be slower in most cases.
   * </p>
   *
   * @param throwable the topmost exception whose causal chain is to be searched
   * @param type the type of the exception to find in the causal chain (will be treated as an upper-bound, i.e.
   * subclasses of this type will also match)
   * @param recursionDepthLimit will stop looking after this depth is exceeded (0 corresponds to the topmost level)
   * @param <E> the type of the exception to find in the causal chain
   * @return the top-most exception in the causal chain of the given throwable that matches the given type
   * (using {@link Class#isInstance(Object)}), or {@code null} if not found.
   */
  @Nullable
  @SuppressWarnings("unchecked")
  public static <E extends Throwable> E getFirstByType(Throwable throwable, Class<E> type, int recursionDepthLimit) {
    Preconditions.checkArgument(recursionDepthLimit >= 0, "recursionDepthLimit should be >= 0 (given: %s)", recursionDepthLimit);
    if (throwable == null)
      return null;  // return null rather than throw NPE
    else if (type.isInstance(throwable)) {
      // base case
      return (E)throwable;
    }
    else if (recursionDepthLimit > 0) {
      // recursive case
      return getFirstByType(throwable.getCause(), type, recursionDepthLimit-1);
    }
    else
      return null;  // not found
  }

}
