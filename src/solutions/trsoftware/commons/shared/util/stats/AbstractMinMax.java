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

import solutions.trsoftware.commons.shared.util.HasValue;

import javax.annotation.Nullable;
import java.util.Comparator;

import static java.util.Objects.requireNonNull;

/**
 * Base class for the {@link Min} and {@link Max} classes.
 *
 * @param <T> type of input elements (for {@link Updatable#update(Object)})
 * @param <R> the concrete subclass of this class (for {@link Mergeable#merge(Object)})
 *
 * @author Alex
 * @since 10/1/2023
 */
abstract class AbstractMinMax<T, R extends AbstractMinMax<T, R>> implements CollectableStats<T, R>, HasValue<T> {
  protected final Comparator<T> comparator;

  /** The current max or min value of all the samples that have been given */
  private T best;

  public AbstractMinMax(Comparator<T> comparator) {
    this.comparator = requireNonNull(comparator, "comparator");
  }

  /** Updates the current best value with a new sample, returning the new best value */
  public T updateAndGet(T candidate) {
    update(candidate);
    return getValue();
  }

  public void update(T candidate) {
    if (best == null)
      best = candidate;
    else if ((getMultiplier() * comparator.compare(best, candidate)) < 0)
      best = candidate;
  }

  abstract int getMultiplier();

  @Override
  @Nullable
  public T getValue() {
    return best;
  }

  public void merge(R other) {
    update(other.getValue());
  }
}