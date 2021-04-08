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

import com.google.common.collect.BiMap;
import solutions.trsoftware.commons.server.memquery.*;
import solutions.trsoftware.commons.server.memquery.algebra.EquiJoin;
import solutions.trsoftware.commons.server.memquery.schema.ColSpec;
import solutions.trsoftware.commons.server.memquery.schema.NameAccessorColSpec;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Implements an optimization if the join is an equi-join: sorts the RHS beforehand, so that the join can be
 * evaluated in O(n*log(n)) time instead of O(n^2).
 *
 * @author Alex, 4/17/2015
 */
public class NestedLoopEquiJoinIterator<J extends EquiJoin> extends NestedLoopJoinIterator<J> {

  private final BiMap<String, String> joinColNames;
  /**
   * schema that can be used to construct a query Row when searching the RHS for matches
   * @see #getRightMatchesIterator()
   */
  private final RelationSchema rightMatchesQuerySchema;

  NestedLoopEquiJoinIterator(J joinOp, Relation leftInputRelation, Relation rightInputRelation) {
    super(joinOp, leftInputRelation.iterator(), sortRightInputRelation(rightInputRelation, joinOp.getParams().getColNameCorrespondence()));
    joinColNames = joinOp.getParams().getColNameCorrespondence();
    RelationSchema rSchema = rightInputRelation.getSchema();
    // construct a schema that can be used to construct a query Row when searching the RHS for matches
    rightMatchesQuerySchema = new RelationSchema(rSchema.getName(),
        StreamSupport.stream(rSchema.spliterator(), false)
            // TODO: experiment: can probably limit this schema to just the col names that are being joined
            .filter(colSPec -> joinColNames.containsValue(colSPec.getName()))
            .map((Function<ColSpec, ColSpec>)NameAccessorColSpec::new).collect(Collectors.toList()));

  }

  private static MaterializedRelation sortRightInputRelation(Relation rightInputRelation, Map<String, String> colNameCorrespondence) {
    List<SortOrder> sortOrders = new ArrayList<SortOrder>(colNameCorrespondence.size());
    for (String colName : colNameCorrespondence.values()) {
      sortOrders.add(new SortOrder(colName, false));
    }
    // check if RHS is already sorted in the order we need it to be
    if (rightInputRelation instanceof SortedRelation && rightInputRelation instanceof MaterializedRelation) {
      SortedRelation sortedRelation = (SortedRelation)rightInputRelation;
      if (sortedRelation.getSortOrders().equals(sortOrders))
        return (MaterializedRelation)sortedRelation;
    }
    // not already sorted or not already sorted in the order that we need: create a new sorted relation with the same rows
    return new SortedArrayListRelation(rightInputRelation.getSchema(), rightInputRelation.iterator(), sortOrders);
  }


  @Override
  protected Iterator<Row> getRightMatchesIterator() {
    // create the search query
    MutableRow query = RowFactory.getInstance().newRow(rightMatchesQuerySchema);
    for (Map.Entry<String, String> colPair : joinColNames.entrySet()) {
      query.setValue(colPair.getValue(), nextLeft.getValue(colPair.getKey()));
    }
    return ((SortedArrayListRelation)rightInputRelation).iterMatches(query);
  }

}
