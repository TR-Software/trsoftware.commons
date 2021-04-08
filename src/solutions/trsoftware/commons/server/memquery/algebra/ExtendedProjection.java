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

package solutions.trsoftware.commons.server.memquery.algebra;

import solutions.trsoftware.commons.server.memquery.Row;
import solutions.trsoftware.commons.server.memquery.expressions.Expression;
import solutions.trsoftware.commons.server.memquery.schema.ColSpec;
import solutions.trsoftware.commons.server.memquery.schema.ExpressionAccessorColSpec;
import solutions.trsoftware.commons.server.memquery.schema.NameAccessorColSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

/**
 * An extended projection operation: projects a set of expressions evaluated on the input relation (which may or may not
 * reference names of attributes from the input relation).
 *
 * @author Alex, 1/14/14
 */
public class ExtendedProjection extends StreamableUnaryOperation<Map<String, Expression<Row, ?>>> {

  /**
   * @param namedExpressions A mapping of col names to expressions that compute their values from a row, or nulls
   * if they are just simple projections.
   */
  public ExtendedProjection(RelationalExpression input, Map<String, Expression<Row, ?>> namedExpressions) {
    super(input, Collections.unmodifiableMap(namedExpressions));
  }

  @Override
  protected Iterable<String> getOutputColNames() {
    ArrayList<String> ret = new ArrayList<String>();
    if (params.keySet().contains("*"))
      ret.addAll(getInputSchema().getColNames()); // prepend all the cols from the input schema
    for (String name : params.keySet()) {
      if (!"*".equals(name)) // "*" is not an actual col
        ret.add(name);
    }
    return ret;
  }

  public Expression<Row, ?> getExpression(String name) {
    return params.get(name);
  }

  @Override
  protected ColSpec createColSpec(String name) {
    Expression<Row, ?> expr = getExpression(name);
    if (expr == null)
      return new NameAccessorColSpec(getInputSchema().get(name));
    return new ExpressionAccessorColSpec(name, expr);
  }
}
