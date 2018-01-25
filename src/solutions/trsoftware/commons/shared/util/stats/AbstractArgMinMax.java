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

/**
 * A superclass for the {@link ArgMax} and {@link ArgMin} classes.
 *
 * @param <A> the arg type
 * @param <V> the value type produced by the arg
 *
 * @author Alex
 */
public abstract class AbstractArgMinMax<A, V extends Comparable<V>> {
  /** The current max or min value of all the samples that have been given */
  private V bestValue;

  /** The current argument associated with the best value */
  private A bestArg;

  /** Updates the mean with a new sample, returning the new argmax */
  public V update(A arg, V value) {
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

  public A get() {
    return bestArg;
  }

}