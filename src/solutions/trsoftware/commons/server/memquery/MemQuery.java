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

import solutions.trsoftware.commons.server.memquery.algebra.RelationalExpression;
import solutions.trsoftware.commons.server.memquery.eval.EvalPipelineBuilder;
import solutions.trsoftware.commons.server.memquery.eval.RelationalEvaluator;
import solutions.trsoftware.commons.server.memquery.output.FixedWidthPrinter;
import solutions.trsoftware.commons.server.memquery.output.HtmlTablePrinter;
import solutions.trsoftware.commons.server.memquery.schema.ColSpec;
import solutions.trsoftware.commons.shared.util.compare.CompositeComparator;
import solutions.trsoftware.commons.shared.util.iterators.CountingIterator;

import java.io.PrintStream;
import java.util.*;

import static java.util.Arrays.asList;
import static solutions.trsoftware.commons.server.memquery.PrintFormat.FIXED_WIDTH;
import static solutions.trsoftware.commons.server.memquery.PrintFormat.HTML;
import static solutions.trsoftware.commons.server.memquery.util.NameUtils.mapByName;


// TODO: nice extras:
// - allow printing output without ordinal column
// - allow wrapping col names
// - support the MySql format for printing fixed-width tables (include borders)


/**
 * The core class of this framework.  Defines a query on a list of objects.  Also executes the query and prints the
 * result set as a fixed-width text table or as HTML.
 *
 * Instances of this class are fully-specified at construction time and are immutable (but you can use QueryBuilder
 * to produce different instances from similar reusable building blocks).
 *
 * TODO: call this framework "SmallQuery", to contrast Google's "BigQuery", and extract to a separate open-source project
 *
 * @author Alex, 1/5/14
 */
public class MemQuery {

  // configuration fields

  /** The relational algebra expression representing this query */
  private final RelationalExpression expr;
  /** The max number of results to return */
  private final int limit;
  /** Describes what this query does for human consumption */
  private final String description;
  /** A SQL-like representation of this query */
  private final String sql;
  private final List<SortOrder> sortOrders;


  public MemQuery(RelationalExpression queryExpression, List<SortOrder> sortOrders, int limit, String sql, String description) {
    this.sql = sql;
    this.limit = limit;
    this.description = description;
    this.sortOrders = sortOrders;
    // TODO: optimize the expression
    this.expr = queryExpression;
  }

  public static CompositeComparator<Row> makeComparator(List<SortOrder> sortOrders, RelationSchema schema) {
    List<Comparator<Row>> comparators = new ArrayList<Comparator<Row>>();
    for (SortOrder order : sortOrders) {
      comparators.add(makeComparator(order, schema));
    }
    return new CompositeComparator<Row>(comparators);
  }

  /**
   * Creates a row comparator based on the given sort order spec for a column in the given schema.
   */
  private static Comparator<Row> makeComparator(SortOrder order, RelationSchema schema) {
    String colName = order.getName();
    ColSpec colSpec = schema.get(colName);
    Class valueType = colSpec.getType();
    if (!(valueType.isPrimitive() || Comparable.class.isAssignableFrom(valueType)))
      throw new IllegalArgumentException("Cannot create a default comparator for a type that's not comparable.");
    Comparator<Row> sortComparator = new Comparator<Row>() {
      @Override
      public int compare(Row r1, Row r2) {
        /*
        We treat null values as "less than" all others;
        this matches the behavior of MySQL/SQLServer, but the opposite of Oracle/Postgres/DB2, and in HSQLDB nulls always come first, regardless of ASC/DESC
        (see https://docs.mendix.com/refguide/null-ordering-behavior)
        */
        Comparable v1 = r1.getValue(colName);
        Comparable v2 = r2.getValue(colName);
        if (v1 == null)
          return v2 == null ? 0 : -1;
        else if (v2 == null)
          return 1;
        else
          return v1.compareTo(v2);
      }
    };
    if (order.isReversed())
      return sortComparator.reversed();
    else
      return sortComparator;
  }

  @Override
  public String toString() {
    return sql;
  }

  public String getDescription() {
    // NOTE: we could generate the name automatically based on the name of the sort order column
    return description;
  }

  public int getLimit() {
    return limit;
  }

  public boolean hasSortOrders() {
    return sortOrders != null && !sortOrders.isEmpty();
  }

  /**
   * Executes the query over the given input relations, resolved by their schema names.
   * @return the evaluated result set (which will be sorted iff {@link #hasSortOrders()} is {@code true}
   */
  public SortedResultSet eval(Relation... inputs) {
    return new SortedResultSet(evalAsUnsortedStream(inputs));
  }

  /**
   * Executes the query over the given input relations, resolved by the keys of the given map.
   * @return the evaluated result set (which will be sorted iff {@link #hasSortOrders()} is {@code true}
   */
  public SortedResultSet eval(Map<String, Relation> inputs) {
    return new SortedResultSet(evalAsUnsortedStream(inputs));
  }

  /**
   * Evaluates the query over the given input relations, resolved by their schema names.
   * @see #evalAsUnsortedStream(Map)
   */
  public Relation evalAsUnsortedStream(Relation... inputs) {
    return evalAsUnsortedStream(mapByName(asList(inputs)));
  }

  /**
   * Evaluates the query over the given input relations, resolved by the keys of the given map.
   * <p>
   * <strong>NOTE:</strong> unlike the {@link #eval} method, this might return an unsorted {@link StreamingRelation},
   * which means that that the results will NOT be sorted.  If you want the query's sort orders to be applied,
   * use the {@link #eval} method instead (to get a {@link SortedResultSet}).
   * </p>
   */
  public Relation evalAsUnsortedStream(Map<String, Relation> inputs) {
    // create the evaluator pipeline tree
    RelationalEvaluator<Relation> evaluator = new EvalPipelineBuilder(this, inputs).visit(expr);

    // now run the pipeline
    // TODO: use a thread pool to run the pipeline (pass the pool to each evaluator so it may schedule itself
    Relation evaluationResult;
    try {
      evaluationResult = evaluator.call();
    }
    catch (Exception e) {
      throw new RuntimeException("Unable to evaluate MemQuery " + this, e);
    }
    return evaluationResult;
  }


  /* Represents an iterable of this query's results. The constructor performs the sorting and limiting. */
  public class SortedResultSet implements ResultSet {

    // TODO: try to extend SortedArrayListRelation here (and use a different subclass for result sets that don't need sorting)

    private ArrayListRelation results;

    public SortedResultSet(Relation evaluatedRelation) {
      Iterable<Row> rows = evaluatedRelation;
      if (sortOrders != null && !sortOrders.isEmpty()) {
        // we need to sort the results first
        rows = new SortedArrayListRelation(evaluatedRelation, sortOrders);
      }
      results = new ArrayListRelation(evaluatedRelation.getSchema(), new CountingIterator<Row>(rows.iterator(), limit));
    }

    @Override
    public MemQuery getQuery() {
      return MemQuery.this;
    }

    @Override
    public void print(PrintStream out) {
      printResultSet(this, out, FIXED_WIDTH);
    }

    @Override
    public Iterator<Row> iterator() {
      return results.iterator();
    }

    @Override
    public List<Row> getRows() {
      return results.getRows();
    }

    public int size() {
      return results.size();
    }

    /** Transforms this result set so it can be used as an input to another query */
    public Map<String, Relation> asQueryInput() {
      return Collections.singletonMap(getName(), (Relation)this);
    }

    @Override
    public String getName() {
      return results.getName();
    }

    @Override
    public RelationSchema getSchema() {
      return results.getSchema();
    }
  }

  // shortcut methods

  public void evalPrint(Map<String, Relation> inputs, PrintStream out) {
    printResultSet(eval(inputs), out, FIXED_WIDTH);
  }

  public static void printResultSet(ResultSet rs, PrintStream out, PrintFormat printFormat) {
    if (printFormat == HTML)
      new HtmlTablePrinter().print(rs, out);
    else
      new FixedWidthPrinter().print(rs, out);
  }

}
