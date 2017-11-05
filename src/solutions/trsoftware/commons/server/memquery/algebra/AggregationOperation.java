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

package solutions.trsoftware.commons.server.memquery.algebra;

import solutions.trsoftware.commons.server.memquery.AggregationSpec;
import solutions.trsoftware.commons.server.memquery.aggregations.Aggregation;
import solutions.trsoftware.commons.server.memquery.schema.AggregatedColSpec;
import solutions.trsoftware.commons.server.memquery.schema.ColSpec;
import solutions.trsoftware.commons.server.memquery.schema.NameAccessorColSpec;
import solutions.trsoftware.commons.server.memquery.util.NameUtils;
import solutions.trsoftware.commons.server.util.reflect.ReflectionUtils;
import solutions.trsoftware.commons.shared.util.CollectionUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * The relational aggregation operation (&#x1d4a2;): takes a (possibly empty) set of grouping attributes and
 * a set of renamed aggregation functions on the rest of the attributes.
 *
 * @author Alex, 1/15/14
 */
public class AggregationOperation extends UnaryOperation<AggregationOperation.Params> {

  public AggregationOperation(RelationalExpression input, Params params) {
    super(input, params);
  }

  @Override
  protected Iterable<String> getOutputColNames() {
    return CollectionUtils.concat(this.params.groupingAttrs, this.params.aggregationsByName.keySet());
  }

  @Override
  protected ColSpec createColSpec(String name) {
    AggregationSpec aggSpec = this.params.aggregationsByName.get(name);
    if (aggSpec != null)
      return new AggregatedColSpec(aggSpec);
    // must the name of a grouping attr
    ColSpec srcColSpec = getInputSchema().get(name);
    return new NameAccessorColSpec(srcColSpec);
  }

  /**
   * Factory method to instantiate all the aggregations for a new group.
   * @return a mapping of srcAttrName -> (aggInstance, newAttrName)
   */
  public Map<AggregationSpec, Aggregation> createAggregations() {
    LinkedHashMap<AggregationSpec, Aggregation> ret = new LinkedHashMap<AggregationSpec, Aggregation>();
    for (AggregationSpec aggSpec : params.aggregationsByName.values()) {
      // TODO: if the aggregation is a mean or variance, check for the presence its counterpart in the map and reuse the same underlying MeanAndVariance instance
      ret.put(aggSpec, ReflectionUtils.newInstanceUnchecked(aggSpec.getType()));
    }
    return ret;
  }

  /**
   * The parameters for relational aggregation operation (&#x1d4a2;): specifies a (possibly empty) set of grouping attributes
   * and a set of aggregation function classes on a subset of the attributes, each one possibly renamed (the "as" syntactic sugar).
   *
   * @author Alex, 1/15/14
   */
  public static class Params {
  
    /** The result set will be grouped by this set of columns */
    private final Set<String> groupingAttrs;
  
    /** A list of the aggregations mapped by their output attribute name */
    private final Map<String, AggregationSpec> aggregationsByName;
  
    public Params(Set<String> groupingAttrs, Iterable<AggregationSpec> aggregations) {
      this.groupingAttrs = Collections.unmodifiableSet(groupingAttrs);
      this.aggregationsByName = Collections.unmodifiableMap(NameUtils.mapByName(aggregations));
    }
  
    public Set<String> getGroupingAttrs() {
      return groupingAttrs;
    }
  
    public Map<String, AggregationSpec> getAggregationsByName() {
      return aggregationsByName;
    }
  }

}
