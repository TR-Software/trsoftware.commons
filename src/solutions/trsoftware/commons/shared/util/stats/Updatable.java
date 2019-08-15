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

package solutions.trsoftware.commons.shared.util.stats;

import java.util.function.Consumer;

/**
 * Indicates that an instance can update itself from single values of some data type.
 *
 * @author Alex, 1/7/14
 */
public interface Updatable<T> extends Consumer<T> {
  /**
   * Updates itself with the given value.
   */
  void update(T x);

  /**
   * Updates itself with the given value.
   */
  @Override
  default void accept(T t) {
    update(t);
  }

  /**
   * Equivalent to calling {@link #update(Object)} for each element in the given items
   */
  default void updateAll(Iterable<T> items) {
    for (T item : items) {
      update(item);
    }
  }

  /**
   * Equivalent to calling {@link #update(Object)} for each element in the given items
   */
  default void updateAll(T... items) {
    for (T item : items) {
      update(item);
    }
  }
}
