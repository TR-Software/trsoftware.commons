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
 * An arithmetic mean that can be updated with new samples;
 *
 * @author Alex
 */
public class Mean<N extends Number> {
  /** The current mean value of all the samples that have been given */
  private double mean = 0;
  /** Number of samples */
  private int n = 0;

  /** Updates the mean with a new sample, returning the new mean */
  public double update(N sample) {
    return update(sample.doubleValue());
  }

  /** Updates the mean with a new sample, returning the new mean */
  public double update(double sample) {
    return mean = ((mean * n) + sample) / ++n;
  }

  public double getMean() {
    return mean;
  }

  public int getNumSamples() {
    return n;
  }
}
