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

import javax.annotation.Nonnull;

/**
 * Indicates that an instance can incorporate into itself data from another instance (for map-reduce-style processing).
 *
 * @author Alex, 1/7/14
 */
public interface Mergeable<T> {
  /**
   * Merges in data from another instance.
   */
  void merge(@Nonnull T other);

  /**
   * Merges the second arg into the first and returns it.
   *
   * This operation can be used as a "combiner" function for a {@link java.util.stream.Collector},
   * when passed as a method reference.
   *
   * @return the result of merging {@code b} into {@code a}
   */
  static <T extends Mergeable<T>> T combine(T a, T b) {
    a.merge(b);
    return a;
  }
}
