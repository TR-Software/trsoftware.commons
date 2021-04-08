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

/**
 * Combines {@link ArgMin} and {@link ArgMax} under one data structure.
 *
 * @param <A> the arg type
 * @param <V> the value type produced by the arg
 * @see MinAndMaxComparable
 * @author Alex
 */
public class ArgMinAndMax<A, V extends Comparable<V>> {
  private ArgMin<A, V> argMin = new ArgMin<A, V>();
  private ArgMax<A, V> argMax = new ArgMax<A, V>();

  /** Updates the min and max with a new value */
  public void update(A arg, V value) {
    argMin.update(arg, value);
    argMax.update(arg, value);
  }

  public ArgMin<A, V> getArgMin() {
    return argMin;
  }

  public ArgMax<A, V> getArgMax() {
    return argMax;
  }
}