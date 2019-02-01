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
import solutions.trsoftware.commons.server.memquery.RowFactory;
import solutions.trsoftware.commons.shared.util.callables.Function1;

/**
 * An unary operation that can be evaluated one row at a time.  Implements {@link Function1} to do
 * just that.
 *
 * @author Alex, 1/15/14
 */
public abstract class StreamableUnaryOperation<P> extends UnaryOperation<P> implements Function1<Row, Row> {

  public StreamableUnaryOperation(RelationalExpression input, P parameters) {
    super(input, parameters);
  }

  /**
   * Applies the encapsulated operation to a single row of the input relation.
   * @param inputRow a row of the input relation.
   * @return the corresponding row of the output relation.
   */
  @Override
  public Row call(Row inputRow) {
    return RowFactory.getInstance().transformRow(getOutputSchema(), inputRow);
  }


}
