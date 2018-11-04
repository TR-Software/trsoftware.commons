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

/**
 * Computes the Cartesian product of two relations that have no shared attribute names.
 *
 * @author Alex, 1/12/14
 */
public class CrossJoin extends Join<Join.Params> {

  public CrossJoin(RelationalExpression lhs, RelationalExpression rhs) {
    super(lhs, rhs, new Params(Type.INNER));
    if (!getAttributeNamesIntersection(getLHSchema(), getRHSchema()).isEmpty())
      throw new IllegalArgumentException("The Cartesian product is not defined when the input relations have any attribute names in common");
  }

  @Override
  public boolean match(Row leftRow, Row rightRow) {
    return true;  // every row on either side participates in the cross product
  }

}
