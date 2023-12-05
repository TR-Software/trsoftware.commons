/*
 * Copyright 2022 TR Software Inc.
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
import javax.annotation.Nullable;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Base class for {@link ArgMax} and {@link ArgMin}.
 *
 * @param <A> the arg type
 * @param <V> the value type produced by the arg
 *
 * @author Alex
 */
public abstract class AbstractArgMinMax<A, V extends Comparable<V>> implements Supplier<A>, Mergeable<AbstractArgMinMax<A, V>> {
  /** The current max or min value of all the samples that have been given */
  private V bestValue;

  /** The current argument associated with the best value */
  private A bestArg;

  /** Updates the current argmax (or argmin) from the given sample, returning the current best arg */
  public V update(@Nonnull A arg, @Nonnull V value) {
    requireNonNull(arg, "arg");
    requireNonNull(value, "value");
    if (bestValue == null) {
      bestValue = value;
      bestArg = arg;
    }
    else if (getMultiplier() * bestValue.compareTo(value) < 0) {
      bestValue = value;
      bestArg = arg;
    }
    return bestValue;
  }

  protected abstract int getMultiplier();

  /**
   * @return the arg associated with the best value, or {@code null} if the {@link #update} method was never invoked.
   */
  @Nullable
  public A get() {
    return bestArg;
  }

  @Override
  public void merge(AbstractArgMinMax<A, V> other) {
    update(other.bestArg, other.bestValue);
  }
}