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

package solutions.trsoftware.commons.server.memquery.algebra;

import com.google.common.collect.BiMap;
import solutions.trsoftware.commons.server.memquery.Row;
import solutions.trsoftware.commons.shared.util.LogicUtils;

import java.util.Map;

/**
 * Matches rows whose columns have the same value for a given column mapping.
 * <p>
 * <em>NOTE</em>:: the Wikipedia article on <a href="https://en.wikipedia.org/wiki/Join_(SQL)#Equi-join">Join (SQL)</a>
 * makes it seem like <em>equi-join</em> is a type of <em>inner</em> join, but in reality,
 * the term <em>equi-join</em> simply means a special-case of &theta;-join (relational algebra),
 * where the join condition is equality of attribute values for for some mapping of attributes from the input relations
 * (see Database Systems Concepts Chapter 13.5)
 *
 * @author Alex, 1/10/14
 * @see <a href="https://www.amazon.com/Database-Systems-Concepts-Henry-Korth/dp/0072283637/">
 *     Database Systems Concepts ("DSC"), 4th Edition Edition, by Silberschatz, et. al. (ISBN 0072283637)</a>
 */
public class EquiJoin extends Join<EquiJoin.Params> {

  public EquiJoin(RelationalExpression lhs, RelationalExpression rhs, Params params) {
    super(lhs, rhs, params);
  }

  @Override
  public boolean match(Row leftRow, Row rightRow) {
    for (Map.Entry<String, String> colNames : params.getColNameCorrespondence().entrySet()) {
      String leftName = colNames.getKey();
      String rightName = colNames.getValue();
      if (!LogicUtils.eq(leftRow.getValue(leftName), rightRow.getValue(rightName)))
        return false;
    }
    return true;
  }

  public static class Params extends Join.Params {

    private final BiMap<String, String> colNameCorrespondence;

    public Params(Type type, BiMap<String, String> colNameCorrespondence) {
      super(type);
      this.colNameCorrespondence = colNameCorrespondence;
    }

    public BiMap<String, String> getColNameCorrespondence() {
      return colNameCorrespondence;
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder(super.toString());
      sb.append(" ON ").append(colNameCorrespondence);
      return sb.toString();
    }
  }

}
