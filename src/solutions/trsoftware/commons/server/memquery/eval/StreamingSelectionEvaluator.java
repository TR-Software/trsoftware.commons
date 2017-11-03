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

import solutions.trsoftware.commons.client.util.Predicate;
import solutions.trsoftware.commons.client.util.iterators.PredicatedIterator;
import solutions.trsoftware.commons.client.util.iterators.TransformingIterator;
import solutions.trsoftware.commons.server.memquery.Relation;
import solutions.trsoftware.commons.server.memquery.Row;
import solutions.trsoftware.commons.server.memquery.StreamingRelation;
import solutions.trsoftware.commons.server.memquery.algebra.Selection;

/**
 * An evaluator that can be used in a pipeline that doesn't produce any intermediate materialized relations.
 *
 * @author Alex, 1/15/14
 */
public class StreamingSelectionEvaluator extends
    UnaryOperationEvaluator<Selection, Relation, StreamingRelation> {

  public StreamingSelectionEvaluator(Selection op, RelationalEvaluator<Relation> inputEvaluator) {
    super(op, inputEvaluator);
  }

  @Override
  public StreamingRelation call(Relation input) {
    return new StreamingRelation(op.getOutputSchema(),
        new TransformingIterator<Row, Row>(
            new PredicatedIterator<Row>(input.iterator(), new Predicate<Row>() {
              @Override
              public boolean apply(Row item) {
                return op.getParams().call(item);
              }}) {
            }) {
          @Override
          protected Row transform(Row inputRow) {
            return op.call(inputRow);
          }
        }
    );
  }
}
