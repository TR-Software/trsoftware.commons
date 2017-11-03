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

import solutions.trsoftware.commons.server.memquery.*;
import solutions.trsoftware.commons.server.memquery.algebra.EquiJoin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Implements an optimization if the join is an equi-join: sorts the RHS beforehand, so that the join can be
 * evaluated in O(n*log(n)) time instead of O(n^2).
 *
 * @author Alex, 4/17/2015
 */
public class NestedLoopEquiJoinIterator<J extends EquiJoin> extends NestedLoopJoinIterator<J> {

  NestedLoopEquiJoinIterator(J joinOp, Relation leftInputRelation, Relation rightInputRelation) {
    super(joinOp, leftInputRelation.iterator(), sortRightInputRelation(rightInputRelation, joinOp.getParams().getColNameCorrespondence()));
    // TODO: check if RHS is already sorted in the order we need it to be
  }

  private static MaterializedRelation sortRightInputRelation(Relation rightInputRelation, Map<String, String> colNameCorrespondence) {
    List<SortOrder> sortOrders = new ArrayList<SortOrder>();
    for (String colName : colNameCorrespondence.values()) {
      sortOrders.add(new SortOrder(colName, false));
    }
    return new SortedArrayListRelation(rightInputRelation.getSchema(), rightInputRelation.iterator(), sortOrders);
  }


  @Override
  protected Iterator<Row> getRightMatchesIterator() {
    // create the search query
    RowImpl query = new RowImpl(rightInputRelation.getSchema());
    for (Map.Entry<String, String> colPair : joinOp.getParams().getColNameCorrespondence().entrySet()) {
      query.setValue(colPair.getValue(), nextLeft.getValue(colPair.getKey()));
    }
    return ((SortedArrayListRelation)rightInputRelation).iterator(query);
  }

}
