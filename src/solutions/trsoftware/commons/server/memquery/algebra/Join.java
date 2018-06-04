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

import solutions.trsoftware.commons.server.memquery.RelationSchema;
import solutions.trsoftware.commons.server.memquery.Row;
import solutions.trsoftware.commons.server.memquery.RowImpl;
import solutions.trsoftware.commons.server.memquery.schema.ColSpec;
import solutions.trsoftware.commons.server.memquery.schema.NameAccessorColSpec;
import solutions.trsoftware.commons.shared.util.SetUtils;
import solutions.trsoftware.commons.shared.util.callables.Function2;

import static solutions.trsoftware.commons.server.memquery.util.NameUtils.getUniqueNames;

/**
 * @author Alex, 1/10/14
 */
public abstract class Join<P extends Join.Params> extends BinaryOperation<P> implements Function2<Row, Row, Row> {

  protected Join(RelationalExpression lhs, RelationalExpression rhs, P parameters) {
    super(lhs, rhs, parameters);
  }

  @Override
  protected Iterable<String> getOutputColNames() {
    return SetUtils.union(getUniqueNames(getLHSchema()), getUniqueNames(getRHSchema()));
  }

  @Override
  protected ColSpec createColSpec(String name) {
    return new NameAccessorColSpec(getInputCol(name));
  }

  /** @return the named col from one of the two input schemas that contains it. */
  private ColSpec getInputCol(String name) {
    RelationSchema lhSchema = getLHSchema();
    RelationSchema rhSchema = getRHSchema();
    if (lhSchema.contains(name)) {
      if (rhSchema.contains(name)) {
        // if both inputs contain the column of the same name, disambiguate by joinType
        if (getParams().getType() == Type.RIGHT_OUTER)
          return rhSchema.get(name);
      }
      return lhSchema.get(name);
    }
    return rhSchema.get(name);
  }

  /**
   * @return The input row that sources the given output attribute name.
   */
  private Row chooseInputRow(String name, Row leftRow, Row rightRow) {
    ColSpec inputCol = getInputCol(name);
    if (leftRow.getSchema().contains(inputCol))
      return leftRow;
    else
      return rightRow;
  }

  /**
   * @return A row of the output created by joining the given input rows, or null if the given inputs don't satisfy
   * the join predicate.
   */
  @Override
  public Row call(Row leftRow, Row rightRow) {
    RelationSchema outputSchema = getOutputSchema();
    RowImpl outputRow = new RowImpl(outputSchema);
    for (ColSpec outputCol : outputSchema) {
      String name = outputCol.getName();
      outputRow.setValue(name, outputCol.getValue(chooseInputRow(name, leftRow, rightRow)));
    }
    return outputRow;  // TODO: see if can replace this with RowImpl.transform
  }

  /**
   * The join predicate.
   * @return true iff the joined result set should have an output row for the given two input rows (one from
   * each table participating in the join).
   */
  public abstract boolean match(Row leftRow, Row rightRow);

  public enum Type {
    INNER, LEFT_OUTER, RIGHT_OUTER, FULL_OUTER
  }

  /** Defines the join parameters */
  public static class Params {
    private final Type type;

    public Params(Type type) {
      this.type = type;
    }

    public Type getType() {
      return type;
    }

    @Override
    public String toString() {
      return type.name().replace('_', ' ');
    }
  }
}
