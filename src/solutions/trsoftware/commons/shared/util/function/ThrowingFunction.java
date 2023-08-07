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

import solutions.trsoftware.commons.shared.util.MapUtils;

import java.util.Map;
import java.util.function.Function;

/**
 * Allows using a lambda or method reference that throws a checked exception with APIs like
 * {@link Map#computeIfAbsent(Object, Function)} that expect a regular {@link Function}
 * that doesn't throw any checked exceptions.
 * <p>
 * The {@link #apply(Object)} method of this function is implemented using a {@code try}/{@code catch}
 * block that rethrows any checked exception as a {@link WrappedException} (a subclass of {@link RuntimeException}),
 * which can be caught to obtain the original checked exception.
 *
 * @see MapUtils#computeIfAbsent(Map, Object, ThrowingFunction)
 * @see ThrowingRunnable
 * @author Alex
 * @since 1/12/2023
 */
@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Exception> extends Function<T, R> {

  R applyUnsafe(T t) throws E;

  @Override
  default R apply(T t) {
    try {
      return applyUnsafe(t);
    }
    catch (Exception e) {
      throw (e instanceof RuntimeException)
          ? (RuntimeException)e
          : new WrappedException(e);
    }
  }

  class WrappedException extends RuntimeException {
    private WrappedException() {  // default private constructor for serialization
    }

    public WrappedException(String message, Throwable cause) {
      super(message, cause);
    }

    public WrappedException(Throwable cause) {
      super(cause);
    }
  }

  /**
   * Facilitates using a reference to a method that throws a checked exception with an API that expects a normal
   * {@link Function}.
   *
   * @param function the function or method reference that throws a checked exception
   * @return the throwing function cast to a normal function, such that any checked exceptions thrown by the given function
   *   will be rethrown as unchecked {@link WrappedException} exceptions
   */
  static <T, R, E extends Exception> Function<T, R> unchecked(ThrowingFunction<T, R, E> function) {
    return function;
  }
}
