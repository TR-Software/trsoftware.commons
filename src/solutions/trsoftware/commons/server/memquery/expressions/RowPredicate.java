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

import java.util.function.Predicate;

/**
 * @author Alex, 4/17/2015
 */
public abstract class RowPredicate extends RowExpression<Boolean> implements Predicate<Row> {

  public RowPredicate() {
    super(Boolean.TYPE);
  }

  public static And and(RowPredicate lhs, RowPredicate rhs) {
    return new And(lhs, rhs);
  }

  public static Or or(RowPredicate lhs, RowPredicate rhs) {
    return new Or(lhs, rhs);
  }

  @Override
  public boolean test(Row row) {
    return apply(row);
  }
}
