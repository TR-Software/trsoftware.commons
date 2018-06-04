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

package solutions.trsoftware.commons.server.memquery.expressions;

import solutions.trsoftware.commons.server.memquery.Row;

/**
 * @author Alex, 11/2/2016
 */
public class Or extends CompoundRowPredicate {

  public Or(RowPredicate lhs, RowPredicate rhs) {
    super(lhs, BooleanBinaryOperator.OR, rhs);
  }


  @Override
  public Boolean call(Row arg) {
    // we override this method to short-circuit the evaluation when the LHS expression is true
    return lhs.call(arg) || rhs.call(arg);
  }
}
