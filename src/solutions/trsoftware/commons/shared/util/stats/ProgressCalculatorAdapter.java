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
 * Adapts ProgressCalculator for use with other data types.
 *
 * @author Alex, 1/7/14
 */
public abstract class ProgressCalculatorAdapter<T> {
  // TODO: rewrite NumberSample, MaxComparable, Mean, etc. using this kind of adapter to primitive pattern
  private ProgressCalculator delegate;

  public ProgressCalculatorAdapter(T min, T max) {
    delegate = new ProgressCalculator(doubleValue(min), doubleValue(max));
  }

  /** @return the fraction of the total distance between min and max covered by x. If x is null, returns 0. */
  public final double calculate(T x) {
    if (x != null)
      return delegate.calcProgress(doubleValue(x));
    return 0;
  }

  /** Sublcasses only need to implement this method, to convert a T to a double */
  protected abstract double doubleValue(T x);
}
