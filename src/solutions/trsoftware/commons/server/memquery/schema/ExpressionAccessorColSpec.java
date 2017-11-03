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

package solutions.trsoftware.commons.server.memquery.schema;

import solutions.trsoftware.commons.server.memquery.Row;
import solutions.trsoftware.commons.server.memquery.expressions.Expression;

/**
 * A col spec that fetches its values from a given row by evaluating an expression on that row.
 *
 * @author Alex, 1/13/14
 */
public class ExpressionAccessorColSpec<T> extends NamedTypedColSpec<T> {

  private final Expression<Row, T> expression;

  public ExpressionAccessorColSpec(String name, Expression<Row, T> expression) {
    super(name, expression.getResultType());
    this.expression = expression;
  }

  public Expression getExpression() {
    return expression;
  }

  @Override
  public T getValue(Row row) {
    return expression.call(row);
  }
}
