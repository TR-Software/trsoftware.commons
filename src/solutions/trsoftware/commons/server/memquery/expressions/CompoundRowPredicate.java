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

package solutions.trsoftware.commons.server.memquery.expressions;

import solutions.trsoftware.commons.server.memquery.Row;

/**
 * @author Alex, 4/17/2015
 */
public class CompoundRowPredicate extends RowPredicate {

  protected final RowPredicate lhs;
  protected final RowPredicate rhs;
  protected final BooleanBinaryOperator op;

  public CompoundRowPredicate(RowPredicate lhs, BooleanBinaryOperator op, RowPredicate rhs) {
    this.lhs = lhs;
    this.op = op;
    this.rhs = rhs;
  }

  @Override
  public Boolean apply(Row arg) {
    return op.apply(lhs.apply(arg), rhs.apply(arg));
  }

  @Override
  public String toString() {
    return String.valueOf(lhs) + ' ' + op.name() + ' ' + rhs;
  }
}
