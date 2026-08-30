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

/**
 * This interface facilitates the use of method references for {@code void} methods that declare checked exceptions,
 * or creating a {@link Runnable} from a lambda whose code could throw a checked exception.
 * <p>
 * The {@code default} {@link #run()} method is implemented using a {@code try}/{@code catch}
 * block that rethrows any checked exception as a {@link WrappedException} (a subclass of {@link RuntimeException}),
 * which can be caught to obtain the original checked exception.
 *
 * @see ThrowingFunction
 * @see ThrowingSupplier
 */
@FunctionalInterface
public interface ThrowingRunnable extends Runnable {

  @Override
  default void run() {
    try {
      doRun();
    }
    catch (Throwable e) {
      throw (e instanceof RuntimeException)
          ? (RuntimeException)e
          : new WrappedException(e);
    }
  }

  void doRun() throws Throwable;
}
