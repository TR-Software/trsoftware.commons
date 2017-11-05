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
 * Keeps track of the min of a sequence of double values.
 *
 * @author Alex
 */
public class MinDouble extends MinMaxDoubleBase implements Serializable, Mergeable<MinDouble> {

  @Override
  protected double absoluteWorst() {
    return Double.POSITIVE_INFINITY;
  }

  @Override
  protected double bestOf(double a, double b) {
    return Math.min(a, b);
  }

  public MinDouble() {
  }

  public MinDouble(double initialValue) {
    super(initialValue);
  }

  public MinDouble(Iterable<Double> candidates) {
    super(candidates);
  }

  public MinDouble(double... candidates) {
    super(candidates);
  }

  @Override
  public void merge(MinDouble other) {
    update(other.get());
  }
}