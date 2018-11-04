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

import solutions.trsoftware.commons.server.memquery.Row;
import solutions.trsoftware.commons.shared.util.LogicUtils;

import java.util.Map;

/**
 * Matches rows whose columns have the same value for a given column mapping.
 *
 * @author Alex, 1/10/14
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

    private final Map<String, String> colNameCorrespondence;

    public Params(Type type, Map<String, String> colNameCorrespondence) {
      super(type);
      this.colNameCorrespondence = colNameCorrespondence;
    }

    public Map<String, String> getColNameCorrespondence() {
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
