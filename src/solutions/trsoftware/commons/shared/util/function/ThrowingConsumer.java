/*
 * Copyright 2023 TR Software Inc.
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

package solutions.trsoftware.commons.shared.util.function;

import java.util.function.Consumer;

/**
 * This interface facilitates the use of method references for consumer-like methods that declare checked exceptions.
 * <p>
 * The {@link #accept(Object)} method of this consumer is implemented using a {@code try}/{@code catch}
 * block that rethrows any checked exception as a {@link WrappedException} (a subclass of {@link RuntimeException}),
 * which can be caught to obtain the original checked exception.
 *
 * @see ThrowingFunction
 * @see ThrowingSupplier
 * @see ThrowingRunnable
 *
 * @author Alex
 * @since 9/15/2026
 */
@FunctionalInterface
public interface ThrowingConsumer<T, E extends Exception> extends Consumer<T> {

  // TODO(11/13/2025): maybe rename to acceptThrowing (to match naming in ThrowingFunction, ThrowingSupplier, etc.)
  void acceptOrThrow(T t) throws E;

  @Override
  default void accept(T t) {
    try {
      acceptOrThrow(t);
    }
    catch (Exception e) {
      throw (e instanceof RuntimeException)
          ? (RuntimeException)e
          : new WrappedException(e);
    }
  }

  /**
   * Facilitates passing a method reference for a method that declares a checked exception to an API that expects a normal
   * {@link Consumer}.
   *
   * @param consumer a lambda or method reference that throws a checked exception
   * @return the throwing consumer cast to a normal consumer, such that any checked exceptions thrown by the given consumer
   *   will be rethrown as unchecked {@link WrappedException} exceptions
   */
  static <T, E extends Exception> Consumer<T> unchecked(ThrowingConsumer<T, E> consumer) {
    return consumer;
  }
  // TODO(10/21/2025): maybe extract the static "unchecked" methods from this class and ThrowingFunction to FunctionalUtils?

}
