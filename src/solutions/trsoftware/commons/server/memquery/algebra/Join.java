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

import solutions.trsoftware.commons.server.memquery.MutableRow;
import solutions.trsoftware.commons.server.memquery.RelationSchema;
import solutions.trsoftware.commons.server.memquery.Row;
import solutions.trsoftware.commons.server.memquery.RowFactory;
import solutions.trsoftware.commons.server.memquery.schema.ColSpec;
import solutions.trsoftware.commons.server.memquery.schema.NameAccessorColSpec;
import solutions.trsoftware.commons.shared.util.SetUtils;
import solutions.trsoftware.commons.shared.util.callables.Function2;

import static solutions.trsoftware.commons.server.memquery.util.NameUtils.getUniqueNames;

/**
 * A <em>join</em> is a subset of the <em>Cartesian product</em> of 2 input relations
 * (<a href="https://en.wikipedia.org/wiki/Relational_algebra">relational algebra</a>).
 * <p>
 * Database Systems Concepts Chapter 4.10.2 lists SQL joins types as:
 * <ul>
 *   <li>inner join</li>
 *   <li>left outer join</li>
 *   <li>right outer join</li>
 *   <li>full outer join</li>
 * </ul>
 * where each join type can have one of the following conditions:
 * <ul>
 *   <li>
 *     <b>natural</b>: special case of <em>equi-join</em>:
 *     the set of all combinations of tuples in R and S that are equal on their common attribute names.
 *   </li>
 *   <li>
 *     <b>on</b> <i>{@code <predicate>}</i>: general form of &theta;-join (relational algebra), which uses any kind of
 *     predicate on the tuples in R and S.
 *   </li>
 *   <li>
 *     <b>using</b> <i>(A1, A2, ... An)</i>: similar to <i>natural join</i>, except that the join attributes
 *     are the attributes <i>A1, A2, ... An</i> rather than <em>all</em> attributes that are common to both relations
 *     (i.e. this is like a natural join, but uses only a subset of the shared attributes)
 *   </li>
 * </ul>
 * <p>
 * <em>NOTE</em>: the Wikipedia article on <a href="https://en.wikipedia.org/wiki/Join_(SQL)#Natural_join">Join (SQL)</a>
 * makes it seem like <i>natural</i> applies only to <i>inner</i> joins, but in reality, any type of <i>outer</i> join
 * can also be <i>natural</i>.
 *
 * @see Join.Type
 * @see <a href="https://www.amazon.com/Database-Systems-Concepts-Henry-Korth/dp/0072283637/">
 *     Database Systems Concepts ("DSC"), 4th Edition Edition, by Silberschatz, et. al. (ISBN 0072283637)</a>
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
   * Constructs a new output row (i.e. a row in the JOIN result) from joining the given input rows.
   * <strong>IMPORTANT</strong>: <em>this method should only be called for rows that satisfy the JOIN predicate.</em>
   *
   * @return A row of the output created by joining the given input rows
   * (<em>which are assumed to satisfy the JOIN predicate</em>)
   */
  @Override
  public Row call(Row leftRow, Row rightRow) {
    RelationSchema outputSchema = getOutputSchema();
    MutableRow outputRow = RowFactory.getInstance().newRow(outputSchema);
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

  /**
   * The types of joins supported by SQL.
   * <p>
   * NOTE: any of these types can be a <i>natural join</i> / <i>equi-join</i>, or, more generally, a join <b>on</b>
   * any predicate (<i>&theta;-join</i> / the general case, using (using any predicate).
   *
   * @see Join
   * @see EquiJoin
   * @see NaturalJoin
   */
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
