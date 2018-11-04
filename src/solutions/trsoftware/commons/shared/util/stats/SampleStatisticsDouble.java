/*
 * Copyright 2018 TR Software Inc.
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
 *
 */

package solutions.trsoftware.commons.shared.util.stats;

/**
 * Mar 26, 2009
 *
 * @author Alex
 */
public interface SampleStatisticsDouble extends Updatable<Double>, UpdatableDouble {
  int size();

  double min();

  double max();

  double sum();

  double mean();

  /** The upper median of the dataset (if there are 2 medians) */
  double median();

  double stdev();

  double variance();

  ImmutableStats<Double> summarize();
}
