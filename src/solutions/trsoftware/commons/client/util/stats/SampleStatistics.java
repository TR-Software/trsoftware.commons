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

package solutions.trsoftware.commons.client.util.stats;

/**
 * Mar 26, 2009
 *
 * @author Alex
 */
public interface SampleStatistics<N extends Number> extends Updatable<N> {
  int size();

  N min();

  N max();

  double sum();

  double mean();

  /** The upper median of the dataset (if there are 2 medians) */
  N median();

  double stdev();

  double variance();

  ImmutableStats<N> summarize();
}
