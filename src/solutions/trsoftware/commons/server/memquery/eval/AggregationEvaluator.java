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

package solutions.trsoftware.commons.server.memquery.eval;

import solutions.trsoftware.commons.client.util.iterators.MapEntryTransformingIterator;
import solutions.trsoftware.commons.server.memquery.*;
import solutions.trsoftware.commons.server.memquery.aggregations.Aggregation;
import solutions.trsoftware.commons.server.memquery.aggregations.RowAggregation;
import solutions.trsoftware.commons.server.memquery.algebra.AggregationOperation;
import solutions.trsoftware.commons.server.util.Duration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An evaluator for the aggregation operation.  Needs to process the entire input relation prior to emitting
 * any rows of the output relation.
 *
 * @author Alex, 1/15/14
 */
public class AggregationEvaluator extends UnaryOperationEvaluator<AggregationOperation, Relation, StreamingRelation> {


  public AggregationEvaluator(AggregationOperation op, RelationalEvaluator<Relation> inputEvaluator) {
    super(op, inputEvaluator);
  }

  @Override
  public StreamingRelation call(Relation input) {
    // 1) process the input rows, grouping and aggregating accordingly
    List<String> groupingAttrs = new ArrayList<String>(op.getParams().getGroupingAttrs());
    GroupingMap groups = new GroupingMap();

    // TODO: extract these two variables to superclass (where the "verbose" variable resides)
    int rowCount = 0;
    Duration processingDuration = null;

    for (Row inputRow : input) {
      if (verbose) {
        if (processingDuration == null)
          processingDuration = new Duration(getClass().getSimpleName(), "");
        rowCount++;
        if (rowCount % 100000 == 0)
          System.out.printf("      %s: %,d LHS rows processed, time elapsed: %s %n",
              getClass().getSimpleName(), rowCount, processingDuration.formatAsClockTime(false));
      }
      List<Object> groupingKey = inputRow.getValues(groupingAttrs);
      Map<AggregationSpec, Aggregation> aggs = groups.get(groupingKey);  // TODO: optimization: it might be faster to group via sorting instead of hashing lists
      if (aggs == null) {
        // instantiate the aggregations for a new group
        aggs = op.createAggregations();
        groups.put(groupingKey, aggs);
      }
      for (Map.Entry<AggregationSpec, Aggregation> ae : aggs.entrySet()) {
        Aggregation agg = ae.getValue();
        if (agg instanceof RowAggregation)
          agg.update(inputRow);
        else
          agg.update(inputRow.getValue(ae.getKey().getInputAttrName()));
      }
    }
    // 2) produce the output relation
    final RelationSchema outputSchema = op.getOutputSchema();
    return new StreamingRelation(outputSchema,
        new MapEntryTransformingIterator<List<Object>, Map<AggregationSpec, Aggregation>, Row>(groups) {
          @Override
          public Row transformEntry(List<Object> key, Map<AggregationSpec, Aggregation> value) {
            RowImpl ret = new RowImpl(outputSchema);
            int i = 0;
            for (Object val : key)
              ret.setValue(i++, val);
            for (Map.Entry<AggregationSpec, Aggregation> ae : value.entrySet())
              ret.setValue(i++, ae.getValue().get());
            return ret;
          }
        }
    );
  }

  private static class GroupingMap extends LinkedHashMap<List<Object>, Map<AggregationSpec, Aggregation>> {
    // this class is just syntactic sugar to avoid writing out the type args every time
    // TODO: can do the same for its key and value types
  }


}
