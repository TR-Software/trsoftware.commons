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

package solutions.trsoftware.commons.server.memquery.schema;

import solutions.trsoftware.commons.server.memquery.AggregationSpec;
import solutions.trsoftware.commons.server.memquery.aggregations.Aggregation;
import solutions.trsoftware.commons.server.memquery.util.AggregationUtils;

/**
 * A column in the output relation of an aggregation operation.  Sets its type based on the value type of the
 * aggregation's output.
 *
 * @author Alex, 1/8/14
 */
public class AggregatedColSpec extends NameAccessorColSpec<Aggregation> {

  private final AggregationSpec aggregationSpec;

  @SuppressWarnings("unchecked")
  public AggregatedColSpec(AggregationSpec aggregationSpec) {
    super(aggregationSpec.getName(), AggregationUtils.getAggregationValueType(aggregationSpec.getType()));
    this.aggregationSpec = aggregationSpec;
  }

  public AggregationSpec getAggregationSpec() {
    return aggregationSpec;
  }
}
