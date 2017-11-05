/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.shared.util.stats;

import java.io.Serializable;

/**
 * A superclass for the MaxComparable and MinComparable classes.
 *
 * @author Alex
 */
public abstract class MinMaxComparableBase<T extends Comparable<T>> implements Serializable {
  /** The current max or min value of all the samples that have been given */
  private T best;
  
  private int multiplier;

  protected MinMaxComparableBase(int multiplier) {
    this.multiplier = multiplier;
  }

  protected MinMaxComparableBase(int multiplier, Iterable<T> candidates) {
    this.multiplier = multiplier;
    updateAll(candidates);
  }

  private MinMaxComparableBase() {
    // default constructor for serialization
  }

  /** Updates the current best value with a new sample, returning the new best value */
  public T update(T candidate) {
    if (best == null)
      best = candidate;
    else if ((multiplier * best.compareTo(candidate)) < 0)
      best = candidate;
    return best;
  }

  public T updateAll(Iterable<T> candidates) {
    for (T candidate : candidates) {
      update(candidate);
    }
    return get();
  }

  public T updateAll(T... candidates) {
    for (T candidate : candidates) {
      update(candidate);
    }
    return get();
  }

  public T get() {
    return best;
  }

}