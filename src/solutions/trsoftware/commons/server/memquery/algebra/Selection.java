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
import solutions.trsoftware.commons.server.memquery.expressions.Expression;
import solutions.trsoftware.commons.server.memquery.schema.ColSpec;
import solutions.trsoftware.commons.server.memquery.schema.NameAccessorColSpec;

/**
 * A relational selection (&sigma;) operation.  Produces an output relation with the same schema as the input relation
 * and containing the subset of rows matching the filter expression.
 * <p>
 * Corresponds to the {@code WHERE} clause in SQL (but <strong>not</strong> the {@code SELECT} clause - see {@link Projection})
 *
 * @author Alex, 1/14/14
 */
public class Selection extends StreamableUnaryOperation<Expression<Row, Boolean>> {

  public Selection(RelationalExpression input, Expression<Row, Boolean> filterExpression) {
    super(input, filterExpression);
  }

  @Override
  protected Iterable<String> getOutputColNames() {
    return getInputSchema().getColNames();
  }

  @Override
  protected ColSpec createColSpec(String name) {
    return new NameAccessorColSpec(getInputSchema().get(name));
  }

}
