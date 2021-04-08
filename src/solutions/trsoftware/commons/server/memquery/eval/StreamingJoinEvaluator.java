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

package solutions.trsoftware.commons.server.memquery.eval;

import solutions.trsoftware.commons.server.memquery.ArrayListRelation;
import solutions.trsoftware.commons.server.memquery.MaterializedRelation;
import solutions.trsoftware.commons.server.memquery.Relation;
import solutions.trsoftware.commons.server.memquery.StreamingRelation;
import solutions.trsoftware.commons.server.memquery.algebra.EquiJoin;
import solutions.trsoftware.commons.server.memquery.algebra.Join;

/**
 * Uses a modified nested loop algorithm for evaluating a join in a streaming fashion (emits an output row as soon
 * as it's available).
 *
 * Implements an optimization if the join is an equi-join: sorts the RHS beforehand, so that the join can be
 * evaluated in O(n*log(n)) time instead of O(n^2).
 *
 *  Does not perform any of the following potential optimizations:
 *  <ul>
 *    <li> Place the smaller relation on the RHS (to exploit cache locality), since the inner loop will be executed many times, whereas the outer loop is executed only once).</li>
 *    <li> If the join is an equijoin, and the LHS is materialized, could sort the LHS also and then use the merge join algorithm
 *    to decrease the time further. Furthermore, the inputs can be sorted in parallel by different threads.</li>
 *    <li> Could make it multi-threaded by partitioning the LHS into T segments, with T threads merging them in parallel.</li>
 *  </ul>
 * TODO: implement these optimizations
 *
 * @author Alex, 1/15/14
 */
public class StreamingJoinEvaluator<J extends Join<? extends Join.Params>> extends BinaryOperationEvaluator<J, Relation, Relation, StreamingRelation> {

  public StreamingJoinEvaluator(J op, RelationalEvaluator<Relation> lhsEvaluator, RelationalEvaluator<Relation> rhsEvaluator) {
    super(op, lhsEvaluator, rhsEvaluator);
  }

  @Override
  public StreamingRelation call(Relation leftInputRelation, Relation rightInputRelation) {
    NestedLoopJoinIterator<? extends Join> joinIterator;
    if (op instanceof EquiJoin)
      joinIterator = new NestedLoopEquiJoinIterator<EquiJoin>((EquiJoin)op, leftInputRelation, rightInputRelation);
    else {
      MaterializedRelation materializedRHS = (rightInputRelation instanceof MaterializedRelation)
          ? (MaterializedRelation)rightInputRelation
          : new ArrayListRelation(rightInputRelation);
        joinIterator = new NestedLoopJoinIteratorImpl<J>(op, leftInputRelation, materializedRHS);
    }
    return new StreamingRelation(op.getOutputSchema(), joinIterator);
  }


}
