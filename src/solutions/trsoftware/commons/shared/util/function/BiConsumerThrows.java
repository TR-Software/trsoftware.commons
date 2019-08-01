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

package solutions.trsoftware.commons.shared.util.function;

import java.util.function.BiConsumer;

/**
 * Same as {@link BiConsumer}, but allows the function to throw a declared exception.
 *
 * @param <A> the type of the first argument to the operation
 * @param <B> the type of the second argument to the operation
 * @param <E> the type of exception declared by the {@code throws} clause of this function
 *
 * @author Alex
 * @since 2/27/2018
 */
@FunctionalInterface
public interface BiConsumerThrows<A, B, E extends Exception>  {

  void accept(A a, B b) throws E;

  default BiFunctionThrows<A, B, Void, E> toBiFunctionThrows() {
    return (a, b) -> {
      accept(a, b);
      return null;
    };
  }
}
