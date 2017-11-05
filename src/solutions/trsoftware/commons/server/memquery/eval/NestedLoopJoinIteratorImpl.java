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

import solutions.trsoftware.commons.server.memquery.MaterializedRelation;
import solutions.trsoftware.commons.server.memquery.Relation;
import solutions.trsoftware.commons.server.memquery.Row;
import solutions.trsoftware.commons.server.memquery.algebra.Join;
import solutions.trsoftware.commons.shared.util.iterators.FilteringIterator;

import java.util.Iterator;

/**
 * Implements the brute force ("nested loop") algorithm for evaluating a join: for every row on the LHS, performs
 * a linear scan of the RHS to find the matching rows.  This results in O(n^2) time.
 */
class NestedLoopJoinIteratorImpl<J extends Join<? extends Join.Params>> extends NestedLoopJoinIterator<J> {


  NestedLoopJoinIteratorImpl(J joinOp, Relation leftInputRelation, MaterializedRelation rightInputRelation) {
    super(joinOp, leftInputRelation.iterator(), rightInputRelation);
  }

  @Override
  protected Iterator<Row> getRightMatchesIterator() {
    return new FilteringIterator<Row>(rightInputRelation.iterator()) {
      @Override
      protected boolean filter(Row elt) {
        return elt != null && joinOp.match(nextLeft, elt);
      }
    };

  }

}
