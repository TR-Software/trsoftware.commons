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

package solutions.trsoftware.commons.shared.util.stats;

import java.util.function.IntConsumer;

/**
 * Indicates that an instance can update itself from a value of type int.
 *
 * @author Alex, 10/26/2024
 */
public interface UpdatableInt extends Updatable<Integer>, IntConsumer {
  /**
   * Updates itself with the given value.
   */
  void update(int x);

  @Override
  default void accept(int value) {
    update(value);
  }

  @Override
  default void update(Integer x) {
    update(x.intValue());
  }

  default void updateAll(int... candidates) {
    for (int x : candidates) {
      update(x);
    }
  }
}
