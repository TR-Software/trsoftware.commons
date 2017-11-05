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

package solutions.trsoftware.commons.server.memquery.aggregations;

import solutions.trsoftware.commons.shared.util.stats.NumberSampleOnlineDouble;

/**
 * @author Alex, 6/4/2014
 */
public abstract class ColStats extends ColAggregation<Double, Number> {

  protected NumberSampleOnlineDouble numberSample = new NumberSampleOnlineDouble();

  @Override
  public void update(Number x) {
    numberSample.update(x.doubleValue());
  }
}
