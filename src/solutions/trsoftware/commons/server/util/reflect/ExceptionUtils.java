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

import com.google.common.base.Throwables;

import javax.annotation.Nullable;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * Provides reflection-based utilities for examining exceptions, which are not already provided
 * by other external libraries such as Guava's {@link Throwables} or Apache Commons'
 * {@link org.apache.commons.lang3.exception.ExceptionUtils}
 *
 * @author Alex
 * @since 7/30/2019
 */
public class ExceptionUtils {
  // TODO(7/5/2026): can move this class to shared, since all methods are GWT-compatible

  /** Static class not instantiable */
  private ExceptionUtils() {
  }

  /**
   * Finds the top-most exception of the given type in the causal chain of the given {@code throwable},
   * starting with the {@code throwable} itself.
   * Returns {@code null} if not found or a cyclic reference was detected in the causal chain.
   *
   * @param throwable the exception whose {@linkplain Throwable#getCause() causal chain} is to be searched
   * @param type class of the exception to find in the causal chain
   *   (exact match for {@link Throwable#getClass()}, excluding sub-types)
   * @return the first exception of the given type in the causal chain of the given throwable,
   *   or {@code null} if not found.
   * @see #findCause(Throwable, Predicate)
   * @see Throwables#getCausalChain(Throwable)
   */
  @SuppressWarnings("unchecked")
  @Nullable
  public static <E extends Throwable> E findCause(Throwable throwable, Class<E> type) {
    return (E)findCause(throwable, cause -> cause.getClass() == type);
  }

  /**
   * Finds the top-most exception that satisfies the given predicate in the causal chain of the given {@code throwable},
   * starting with the {@code throwable} itself.
   * If a cyclic reference is detected in the causal chain before finding the target, returns {@code null}.
   *
   * @param throwable the exception whose {@linkplain Throwable#getCause() causal chain} is to be searched
   * @param predicate the search criteria: applied to each cause in the chain (including the initial {@code throwable})
   * @return the top-most exception in the causal chain of the given throwable that satisfies the given predicate,
   *   or {@code null} if not found
   * @see #findCause(Throwable, Class)
   * @see Throwables#getCausalChain(Throwable)
   */
  @Nullable
  public static Throwable findCause(Throwable throwable, Predicate<Throwable> predicate) {
    // this code is based on com.google.common.base.Throwables.getCausalChain
    requireNonNull(throwable, "throwable");

    /* Comment from Throwables.getCausalChain:
         Keep a second pointer that slowly walks the causal chain. If the fast pointer ever catches
         the slower pointer, then there's a loop.
       Note(Alex): for a cycle of length n, this detects the cycle at depth n-2 into the 2nd pass over the cycle
    */
    Throwable slowPointer = throwable;
    boolean advanceSlowPointer = false;
    //int i = 0;  // temp debug

    while (true) {
      //System.out.printf("findCause(%d): %s%n", i++, throwable);  // temp debug
      if (predicate.test(throwable))
        return throwable;
      if ((throwable = throwable.getCause()) == null)
        return null;  // reached the end of causal chain
      if (throwable == slowPointer)
        return null;  // loop detected in causal chain
      if (advanceSlowPointer)
        slowPointer = slowPointer.getCause();
      advanceSlowPointer = !advanceSlowPointer; // only advance every other iteration
    }
  }

}
