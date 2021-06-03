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

package solutions.trsoftware.commons.shared.util.callables;

import java.util.concurrent.Callable;
import java.util.function.BooleanSupplier;

/**
 * A {@code boolean} function that takes no arguments.
 *
 * @author Alex
 * @since 11/29/2017
 */
@FunctionalInterface
public interface Condition extends BooleanSupplier, Callable<Boolean> {
  /**
   * @return {@code true} iff the condition is met
   */
  boolean check();

  @Override
  default boolean getAsBoolean() {
    return check();
  }

  @Override
  default Boolean call() throws Exception {
    return check();
  }
}
