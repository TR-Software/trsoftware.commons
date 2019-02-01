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

package solutions.trsoftware.commons.server.memquery;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import solutions.trsoftware.commons.server.memquery.aggregations.Aggregation;
import solutions.trsoftware.commons.server.memquery.algebra.*;
import solutions.trsoftware.commons.server.memquery.expressions.Expression;
import solutions.trsoftware.commons.server.memquery.schema.FieldAccessorColSpec;
import solutions.trsoftware.commons.server.memquery.schema.MethodAccessorColSpec;
import solutions.trsoftware.commons.server.memquery.schema.ReflectionAccessorColSpec;
import solutions.trsoftware.commons.shared.util.ArrayUtils;
import solutions.trsoftware.commons.shared.util.iterators.TransformingIterator;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static java.util.Collections.addAll;
import static solutions.trsoftware.commons.server.memquery.util.NameUtils.mapNamesToValues;
import static solutions.trsoftware.commons.shared.util.StringUtils.*;

/**
 * @author Alex, 1/11/14
 */
public class QueryBuilder {

  private interface ProjectionSpec extends HasName, HasValue<Expression<Row, ?>> {
  }

  private static class SimpleProjectionSpec implements ProjectionSpec {
    private String name;

    private SimpleProjectionSpec(String name) {
      this.name = name;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public String toString() {
      return name;
    }

    @Override
    public Expression<Row, ?> get() {
      return null;
    }
  }

  private static class ExtendedProjectionSpec extends SimpleProjectionSpec {
    private Expression<Row, ?> expr;

    private ExtendedProjectionSpec(String name, Expression<Row, ?> expr) {
      super(name);
      this.expr = expr;
    }

    @Override
    public Expression<Row, ?> get() {
      return expr;
    }

    @Override
    public String toString() {
      return expr.toString();
    }
  }

  private static enum JoinType { CROSS, NATURAL }

  private static class JoinSpec {
    private final JoinType type;
    private final Join.Params params;

    private JoinSpec(JoinType type, Join.Params params) {
      this.type = type;
      this.params = params;
    }
  }


  private static class FromSpec {
    private JoinSpec joinSpec;
    private RelationSchema schema;

    private FromSpec(JoinSpec joinSpec, RelationSchema schema) {
      this.schema = schema;
      this.joinSpec = joinSpec;
    }

    @Override
    public String toString() {
      StringBuilder str = new StringBuilder();
      if (joinSpec != null) {
        if (joinSpec.type == JoinType.CROSS)
          str.append(", ");
        else
          str.append(joinSpec.type.name()).append(' ').append(joinSpec.params).append(" JOIN ");
      }
      appendSurrounded(str, schema.getName(), "`");
      return str.toString();
    }
  }

  /** The FROM clause: maps the given inputs by their schema (the values can be null if only a schema is given) */
  private List<FromSpec> inputs = new ArrayList<FromSpec>();

  /** The join type to use for concatenating the next input; can be overridden via the {@link #naturalJoin()} method. */
  private JoinSpec nextJoinSpec = new JoinSpec(JoinType.CROSS, null);

  /** If any of the FROM arguments are relations, they will be stored here by name */
  private Map<String, Relation> inputBindings = new LinkedHashMap<String, Relation>();

  /** The SELECT clause; projections bucketed by their type. */
  private Multimap<Class<? extends ProjectionSpec>, ProjectionSpec> projections = LinkedHashMultimap.create();

  /** Aliases for attributes listed in the SELECT clause */
  private Map<String, String> attrRenamings = new LinkedHashMap<String, String>();

  /** Name of the output schema (optional) */
  private String outputSchemaName;

  /** Aggregations listed by their output attr names */
  private List<AggregationSpec> aggregations = new ArrayList<AggregationSpec>();

  /** The GROUP BY clause */
  private Set<String> groupingAttrs = new LinkedHashSet<String>();

  /** The selection expression */
  private Expression<Row, Boolean> filter;

  /**
   * Col-name-based sort orders in shorthand notation (e.g. "foo" means sort by column "foo" in ascending order and
   * "-foo" means sort by column "foo" in descending order).
   */
  private Set<String> sortOrders = new LinkedHashSet<String>();

  /** The LIMIT clause */
  private int limit = Integer.MAX_VALUE;

  /** Describes what this query does for human consumption */
  private String description;

  public QueryBuilder() {
  }

  public QueryBuilder(String outputSchemaName) {
    this.outputSchemaName = outputSchemaName;
  }

  /** Adds a binding of an input name to its data */
  public QueryBuilder bind(String name, Relation inputRelation) {
    inputBindings.put(name, inputRelation);
    return this;
  }

  /**
   * Adds a relation to the from clause (join).
   */
  public QueryBuilder from(HasSchema input) {
    RelationSchema schema = input.getSchema();
    inputs.add(new FromSpec(getNextJoinSpec(), schema));
    if (input instanceof Relation)
      bind(schema.getName(), (Relation)input);
    return this;
  }

  private JoinSpec getNextJoinSpec() {
    if (inputs.isEmpty())
      return null;
    JoinSpec ret = nextJoinSpec;
    nextJoinSpec = new JoinSpec(JoinType.CROSS, null);  // reset back to default value every time this field is used
    return ret;
  }

  /**
   * Adds relations to the from clause (join).
   */
  public QueryBuilder from(HasSchema... inputs) {
    for (HasSchema input : inputs) {
      from(input);
    }
    return this;
  }

  /**
   * Adds an ORM relation schema to the from clause (join).
   */
  public QueryBuilder from(Class cls) {
    return from(createORMSchema(cls));
  }

  /**
   * Adds a materialized ORM relation to the from clause (join) based on the given inputs.
   */
  public <T> QueryBuilder from(Class<? extends T> cls, Collection<T> instances) {
    return from(createORM(cls, instances));
  }

  /**
   * Adds a streaming ORM relation to the from clause (join) based on the given input iterator.
   */
  public <T> QueryBuilder from(Class<? extends T> cls, Iterator<T> instancesIter) {
    return from(createORM(cls, instancesIter));
  }

  /**
   * Indicates that the argument of the next call to {@link #from} should be connected to the last {@link #from}
   * input by an inner natural join operator instead of the default cross join.
   */
  public QueryBuilder naturalJoin() {
    return naturalJoin(Join.Type.INNER);
  }

  /**
   * Indicates that the argument of the next call to {@link #from} should be connected to the last {@link #from}
   * input by a natural join operator instead of the default cross join.
   * @param joinType
   */
  public QueryBuilder naturalJoin(Join.Type joinType) {
    nextJoinSpec = new JoinSpec(JoinType.NATURAL, new Join.Params(joinType));
    return this;
  }

  /**
   * Adds named columns to the projection.
   */
  public QueryBuilder select(String... names) {
    for (String name : names)
      addProjection(new SimpleProjectionSpec(name));
    return this;
  }

  /**
   * Adds all the columns from input schema to the projection.
   * See {@link ExtendedProjection#getOutputColNames()}
   */
  public QueryBuilder selectAll() {
    return select("*");
  }

  private void addProjection(ProjectionSpec spec) {
    projections.put(spec.getClass(), spec);
  }

  /**
   * Adds a renamed column to the projection.
   */
  public QueryBuilder selectColAs(String inputName, String outputName) {
    attrRenamings.put(inputName, outputName);
    return select(outputName);
  }

  /**
   * @param outputColName a name of an attribute in the output schema
   * @return the original name of the attribute from the input schema (i.e. the value of {@code X} in {@code "SELECT X AS Y"})
   */
  private String getInputColName(String outputColName) {
    for (Map.Entry<String, String> entry : attrRenamings.entrySet()) {
      if (outputColName.equals(entry.getValue()))
        return entry.getKey();
    }
    return outputColName;  // there is no rename mapping for this attribute
  }

  /**
   * Adds an aggregated column for the given input attribute.  The output attribute will have a default name for this
   * aggregation type.
   *
   * @param aggType The type of aggregation to be performed on this column.
   * @param name the name of the input attribute
   */
  public QueryBuilder selectAggregatedCol(Class<? extends Aggregation> aggType, String name) {
    return addAggregation(new AggregationSpec(aggType, name));
  }

  private QueryBuilder addAggregation(AggregationSpec aggSpec) {
    aggregations.add(aggSpec);
    return select(aggSpec.getName());
  }

  private AggregationSpec getAggregationByName(String name) {
    for (AggregationSpec aggregation : aggregations) {
      if (aggregation.getName().equals(name))
        return aggregation;
    }
    return null;
  }

  /**
   * Adds an aggregated column for the given input attribute, aliased by the given output inputName.
   *
   * @param aggType The type of aggregation to be performed on this column.
   * @param inputName the name of the input attribute
   */
  public QueryBuilder selectAggregatedColAs(Class<? extends Aggregation> aggType, String inputName, String outputName) {
    return addAggregation(new AggregationSpec(aggType, inputName, outputName));
  }

  /** Adds an extended projection aliased by the given output name */
  public QueryBuilder selectExprAs(Expression<Row, ?> expr, String name) {
    addProjection(new ExtendedProjectionSpec(name, expr));
    return this;
  }


  public QueryBuilder groupBy(String... names) {
    addAll(groupingAttrs, names);
    return this;
  }

  public QueryBuilder removeGroupBy(String name) {
    groupingAttrs.remove(name);
    return this;
  }

  /** Adds a query filter */
  public QueryBuilder where(final Expression<Row, Boolean> filterExpr) {
    filter = filterExpr;
    return this;
  }

  /**
   * @param sortOrders each string in this arg array is a colName-based sort order in shorthand notation;
   * <em>Example:</em>
   * "foo" means sort by column "foo" in ascending order and "-foo" means sort by column "foo" in descending order.
   */
  public QueryBuilder sortBy(String... sortOrders) {
    Collections.addAll(this.sortOrders, sortOrders);
    return this;
  }

  /**
   * @param sortShorthand a col-name-based sort order in shorthand notation (e.g. "foo" means sort by column "foo" in
   * ascending order and "-foo" means sort by column "foo" in descending order.
   */
  public QueryBuilder removeSortBy(String sortShorthand) {
    sortOrders.remove(sortShorthand);
    return this;
  }

  public QueryBuilder setLimit(int limit) {
    this.limit = limit;
    return this;
  }

  public QueryBuilder setDescription(String description) {
    this.description = description;
    return this;
  }

  public String getOutputSchemaName() {
    return outputSchemaName;
  }

  public QueryBuilder setOutputSchemaName(String outputSchemaName) {
    this.outputSchemaName = outputSchemaName;
    return this;
  }

  /**
   * Builds the query and returns it paired with its bindings.
   * @return An object that can be evaluated without requiring any explicit bindings.
   */
  public BoundQuery boundQuery() {
    return new BoundQuery(query(), inputBindings);
  }

  /**
   * Creates a fully-specified relational algebra query based on the current state of the encapsulated configuration
   * settings
   */
  public MemQuery query() {
    // intercept exceptions for more useful reporting
    try {
      return buildQuery();
    }
    catch (Exception e) {
      throw new RuntimeException("Unable to build query based on the current state of this QueryBuilder: " + this, e);
    }
  }

  /**
   * Creates a fully-specified relational algebra expression based on the current state of the encapsulated configuration
   * settings.
   */
  private MemQuery buildQuery() {
    // create a relational algebra expression tree for the query
    // 1) start with leaf nodes (i.e. the FROM clause)
    RelationalExpression expr = null;
    for (FromSpec input : inputs) {
      RelationalValue inputExpr = new RelationalValue(input.schema);
      if (expr == null)
        expr = inputExpr;
      else {
        JoinSpec joinSpec = input.joinSpec;
        switch (joinSpec.type) {
          case CROSS:
            expr = new CrossJoin(expr, inputExpr);
            break;
          case NATURAL:
            expr = new NaturalJoin(expr, inputExpr, joinSpec.params.getType());
            break;
          default:
            throw new IllegalStateException("Invalid join type specified for " + input.schema);
        }
      }
    }
    if (expr == null)
      throw new IllegalStateException("No FROM inputs specified");
    // 2) now prepend the renamings
    expr = new Rename(expr, outputSchemaName, attrRenamings);
    // 3) now prepend the selection filter (WHERE clause)
    if (filter != null)
      expr = new Selection(expr, filter);
    // 4) now prepend the aggregations, if any
    if (!groupingAttrs.isEmpty() || !aggregations.isEmpty())
      expr = new AggregationOperation(expr, new AggregationOperation.Params(groupingAttrs, aggregations));
    // 5) now prepend the projections
    // we have the projections split into two buckets: simple and extended, so we'll add the corresponding op for each type
//    if (projections.containsKey(SimpleProjectionSpec.class))
//      expr = new Projection(expr, getNames(projections.get(SimpleProjectionSpec.class)));
//    if (projections.containsKey(ExtendedProjectionSpec.class))
//      expr = new ExtendedProjection(expr, mapNamesToValues((Collection)projections.get(ExtendedProjectionSpec.class)));
    // TODO: get rid of the above commented-out code if the following works:
    expr = new ExtendedProjection(expr, mapNamesToValues(projections.values()));
    return new MemQuery(expr, makeSortOrders(), limit, toSql(), description);
  }


  /**
   * Creates the ORM schema for the given class, which includes all of its declared fields.
   *
   * @param projectedMethods optional list of methods of cls to be included in the schema along with the fields
   * of the class.  This is an alternative to having methods annotated with {@link ProjectedField}.
   */
  public static RelationSchema createORMSchema(Class cls, Method... projectedMethods) {
    List<ReflectionAccessorColSpec> colSpecs = new ArrayList<ReflectionAccessorColSpec>();
    for (Field field : cls.getDeclaredFields()) {
      int modifiers = field.getModifiers();
      // we exclude static and synthetic (compiler-generated) fields from the ORM
      if (!field.isSynthetic() && !Modifier.isStatic(modifiers))
        colSpecs.add(new FieldAccessorColSpec(field));
    }
    for (Method method : cls.getDeclaredMethods()) {
      if (method.isAnnotationPresent(ProjectedField.class) || ArrayUtils.contains(projectedMethods, method))
        colSpecs.add(new MethodAccessorColSpec(method));
    }
    return new RelationSchema(cls.getSimpleName(), colSpecs);
  }

  /**
   * Implements the O-R mapping for the given input class.
   *
   * @return A materialized relation corresponding to the given instances of the class
   */
  public static <T> MaterializedRelation createORM(Class<? extends T> cls, Collection<T> instances, Method... projectedMethods) {
    RelationSchema schema = createORMSchema(cls, projectedMethods);
    return new ArrayListRelation(schema, new ORMapper<T>(schema, instances.iterator()));
  }

  /**
   * Implements the O-R mapping for the given input class.
   *
   * @return A streaming relation corresponding to the given iterator for the instances of the class
   */
  public static <T> StreamingRelation createORM(Class<? extends T> cls, Iterator<T> instancesIter, Method... projectedMethods) {
    RelationSchema schema = createORMSchema(cls, projectedMethods);
    return new StreamingRelation(schema, new ORMapper<T>(schema, instancesIter));
  }

  private static class ORMapper<T> extends TransformingIterator<T, Row> {
    private final RelationSchema schema;

    public ORMapper(RelationSchema schema, Iterator<T> inputIter) {
      super(inputIter);
      this.schema = schema;
    }

    @Override
    protected Row transform(T input) {
      return new ObjectRow(schema, input);
    }
  }

  /**
   * @throws java.lang.IllegalStateException if the query is not valid.
   */
  private void validate() {
    // TODO: impl
    // if there are any aggregations present, all the non-aggregated cols must be grouped by
    // (otherwise those cols would have nonsensical results)
//    if (hasAggregatedProjections) {
//      Set<String> ungroupedNonAggregatedCols = difference(
//          getUniqueNames(getNonAggregatedProjections()),
//          groupingCols);
//      if (!ungroupedNonAggregatedCols.isEmpty()) {
//        int n = ungroupedNonAggregatedCols.size();
//        throw new IllegalStateException(String.format(
//            "The %s %s %s neither grouped by nor aggregated, which means %s results are unlikely to be useful.",
//            pluralize("projection", n), ungroupedNonAggregatedCols, pluralize("is", n), pluralize("its", n)));
//      }
//    }
  }

  /** Generates a SQL-like representation of this query */
  private String toSql() {
    StringBuilder sql = new StringBuilder("SELECT ");
    {
      for (Iterator<ProjectionSpec> it = projections.values().iterator(); it.hasNext(); ) {
        ProjectionSpec projection = it.next();
        String name = projection.getName();
        AggregationSpec aggSpec = getAggregationByName(name);
        if (aggSpec != null) {
          sql.append(aggSpec).append(" AS ").append("'").append(aggSpec.getName()).append("'");
        }
        else if (projection instanceof ExtendedProjectionSpec) {
          sql.append("`").append(projection.toString()).append("`").append(" AS ").append("'").append(projection.getName()).append("'");
        }
        else {
          String originalName = getInputColName(name);
          if ("*".equals(name))
            sql.append(name);
          else if (originalName.equals(name))
            sql.append("`").append(projection.toString()).append("`");
          else
            sql.append("`").append(originalName).append("`").append(" AS ").append("'").append(name).append("'");
        }
        if (it.hasNext())
          sql.append(", ");
      }
    }
    appendSqlClause(sql, "FROM", inputs);
    if (filter != null)
      sql.append(" WHERE (").append(filter).append(')');
    appendSqlClause(sql, "GROUP BY", groupingAttrs);
    appendSqlClause(sql, "ORDER BY", sortOrders);
    if (limit != Integer.MAX_VALUE)
      sql.append(" LIMIT ").append(limit);
    return sql.toString();
  }

  private static void appendSqlClause(StringBuilder dest, String clauseName, Collection args) {
    if (!args.isEmpty()) {
      appendSurrounded(dest, clauseName, " ");
      Object[] argsArr = args.toArray();
      if (args.size() > 1) {
        dest.append(tupleToString(argsArr));
      }
      else
        appendArgs(dest, argsArr);
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("QueryBuilder{");
    sb.append("inputs=").append(join("", inputs));
    sb.append(", inputBindings=").append(inputBindings);
    sb.append(", projections=").append(projections);
    sb.append(", attrRenamings=").append(attrRenamings);
    sb.append(", aggregations=").append(aggregations);
    sb.append(", groupingAttrs=").append(groupingAttrs);
    sb.append(", filter=").append(filter);
    sb.append(", sortOrders=").append(sortOrders);
    sb.append(", limit=").append(limit);
    sb.append(", description='").append(description).append('\'');
    sb.append('}');
    return sb.toString();
  }

  //  /**
//   * @return A list of all the given cols wrapped inside a ResultColSpec.
//   */
//  private static List<ColSpec> wrapNonAggregatedCols(Iterable<ColSpec> cols) {
//    List<ColSpec> ret = new ArrayList<ColSpec>();
//    for (ColSpec colSpec : cols) {
//      if (colSpec instanceof AggregatedColSpec)
//        ret.add(colSpec);
//      else
//        ret.add(new NameAccessorColSpec(colSpec));
//    }
//    return ret;
//  }
//
//  // helper methods
//
//  private void maybeSaveProjection(ColSpec colSpec) {
//    String name = colSpec.getName();
//    if (!colSpecsByName.containsKey(name))
//      colSpecsByName.put(name, colSpec);
//  }
//
//  private ColSpec getOrCreateProjectionByName(String name) {
//    ColSpec projection = colSpecsByName.get(name);
//    if (projection == null) {
//      projection = createDefaultProjection(name);
//      colSpecsByName.put(name, projection);
//    }
//    return projection;
//  }
//
//  private ColSpec createDefaultProjection(String fieldName) {
//    if (inputType != null) {
//      try {
//        return new FieldAccessorColSpec(inputType.getDeclaredField(fieldName));
//      }
//      catch (NoSuchFieldException e) {
//        // the rowType doesn't have a field with this name, but perhaps it has a method annotated with @ProjectedField
//        try {
//          return new MethodAccessorColSpec(inputType.getDeclaredMethod(fieldName));
//        }
//        catch (NoSuchMethodException e1) {
//          throw new IllegalArgumentException(
//              String.format("Field '%s' is not defined for class '%s'", fieldName, inputType.getName()), e);
//        }
//      }
//    }
//    else {
//      return schema.get(fieldName);
//    }
//  }
//

  /**
   * Converts the shorthand strings in {@link #sortOrders} into a comparator.
   */
  private List<SortOrder> makeSortOrders() {
    if (sortOrders.isEmpty())
      return null;
    List<SortOrder> ret = new ArrayList<SortOrder>();
    for (String sortSpec : sortOrders) {
      ret.add(sortSpec.charAt(0) == '-'
          ? makeSortOrder(sortSpec.substring(1), true)
          : makeSortOrder(sortSpec, false));
    }
    return ret;
  }

  private static SortOrder makeSortOrder(String colName, boolean reversed) {
    return new SortOrder(colName, reversed);
  }


}
