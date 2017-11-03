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

package solutions.trsoftware.commons.server.memquery;

import solutions.trsoftware.commons.client.util.ComparisonOperator;
import solutions.trsoftware.commons.server.memquery.aggregations.Avg;
import solutions.trsoftware.commons.server.memquery.aggregations.Count;
import solutions.trsoftware.commons.server.memquery.algebra.Join;
import solutions.trsoftware.commons.server.memquery.expressions.BooleanBinaryOperator;
import solutions.trsoftware.commons.server.memquery.expressions.ColValueComparison;
import solutions.trsoftware.commons.server.memquery.expressions.CompoundRowPredicate;
import solutions.trsoftware.commons.server.memquery.expressions.RowExpression;

/**
 * @author Alex, 1/9/14
 */
public class MemQueryTest extends MemQueryTestCase {


  /**
   * Checks the results of some simple queries
   */
  public void testSimpleProjection() throws Exception {
    assertResultSetEquals(runAndPrint(fromScore().select("uid", "modelId", "gameNumber", "score")), joinCsvRows(
        "a,foo,1,20",
        "a,foo,2,30",
        "a,foo,3,38",
        "a,bar,1,40",
        "a,bar,2,60",
        "b,foo,1,50",
        "b,foo,2,60",
        "b,bar,1,71",
        "b,bar,2,72",
        "b,bar,3,73",
        "b,bar,4,74",
        "b,bar,5,75",
        "c,foo,1,81",
        "c,bar,1,82",
        "c,baz,1,83"
    ));
    assertResultSetEquals(runAndPrint(fromUser().select("uid").select("name")), joinCsvRows(
        "a,Amy",
        "b,Ben",
        "d,Dick"
    ));
  }

  /**
   * Checks the sorting of a result set
   */
  public void testSorting() throws Exception {
    // 1)
    assertResultSetEquals(
        runAndPrint(fromScore()
                .selectAll()
                .sortBy("gameNumber", "-uid", "modelId")
        ),
        joinCsvRows(
            "c,bar,1,82",
            "c,baz,1,83",
            "c,foo,1,81",
            "b,bar,1,71",
            "b,foo,1,50",
            "a,bar,1,40",
            "a,foo,1,20",
            "b,bar,2,72",
            "b,foo,2,60",
            "a,bar,2,60",
            "a,foo,2,30",
            "b,bar,3,73",
            "a,foo,3,38",
            "b,bar,4,74",
            "b,bar,5,75"
            )
    );
    // 2)
    assertResultSetEquals(
        runAndPrint(fromScore()
                .selectAll()
                .sortBy("-uid", "score")
        ),
        joinCsvRows(
            "c,foo,1,81",
            "c,bar,1,82",
            "c,baz,1,83",
            "b,foo,1,50",
            "b,foo,2,60",
            "b,bar,1,71",
            "b,bar,2,72",
            "b,bar,3,73",
            "b,bar,4,74",
            "b,bar,5,75",
            "a,foo,1,20",
            "a,foo,2,30",
            "a,foo,3,38",
            "a,bar,1,40",
            "a,bar,2,60"
            )
    );
  }

  public void testExtendedProjection() throws Exception {
    assertResultSetEquals(runAndPrint(fromScore().select("uid", "modelId", "gameNumber", "score").selectExprAs(new ColValueComparison<Double>("score", ComparisonOperator.GT, 40.0), "gt40")), joinCsvRows(
        "a,foo,1,20,false",
        "a,foo,2,30,false",
        "a,foo,3,38,false",
        "a,bar,1,40,false",
        "a,bar,2,60,true",
        "b,foo,1,50,true",
        "b,foo,2,60,true",
        "b,bar,1,71,true",
        "b,bar,2,72,true",
        "b,bar,3,73,true",
        "b,bar,4,74,true",
        "b,bar,5,75,true",
        "c,foo,1,81,true",
        "c,bar,1,82,true",
        "c,baz,1,83,true"
    ));
    assertResultSetEquals(runAndPrint(fromUser().select("uid").select("name").selectExprAs(new RowExpression<String>(String.class) {
      @Override
      public String call(Row row) {
        return row.getValue(0) + "_" + row.getValue(1);
      }
    }, "concat")), joinCsvRows(
        "a,Amy,a_Amy",
        "b,Ben,b_Ben",
        "d,Dick,d_Dick"
    ));
  }

  /**
   * Checks the results of some simple selection (i.e. filtering) queries.
   */
  public void testSelection() throws Exception {
    assertResultSetEquals(runAndPrint(fromScore().select("uid", "modelId", "gameNumber", "score")
            .where(new ColValueComparison<String>("modelId", ComparisonOperator.EQ, "foo"))),
        joinCsvRows(
            "a,foo,1,20",
            "a,foo,2,30",
            "a,foo,3,38",
            "b,foo,1,50",
            "b,foo,2,60",
            "c,foo,1,81"
        ));
    assertResultSetEquals(runAndPrint(fromScore().select("uid", "modelId", "gameNumber", "score")
            .where(new ColValueComparison<Integer>("gameNumber", ComparisonOperator.GTE, 2))),
        joinCsvRows(
            "a,foo,2,30",
            "a,foo,3,38",
            "a,bar,2,60",
            "b,foo,2,60",
            "b,bar,2,72",
            "b,bar,3,73",
            "b,bar,4,74",
            "b,bar,5,75"
        ));
    assertResultSetEquals(runAndPrint(fromScore().select("uid", "modelId", "gameNumber", "score")
            .where(new CompoundRowPredicate(
                new ColValueComparison<String>("modelId", ComparisonOperator.EQ, "foo"),
                BooleanBinaryOperator.AND,
                new ColValueComparison<Integer>("gameNumber", ComparisonOperator.EQ, 2)))),
        joinCsvRows(
            "a,foo,2,30",
            "b,foo,2,60"
        ));
  }

  /**
   * Checks "SELECT *" queries.
   */
  public void testSelectAll() throws Exception {
    assertResultSetEquals(runAndPrint(fromScore().select("*")
            .where(new ColValueComparison<String>("modelId", ComparisonOperator.EQ, "foo"))),
        joinCsvRows(
            "a,foo,1,20",
            "a,foo,2,30",
            "a,foo,3,38",
            "b,foo,1,50",
            "b,foo,2,60",
            "c,foo,1,81"
        ));
    assertResultSetEquals(runAndPrint(fromScore().selectAll()
            .where(new ColValueComparison<Integer>("gameNumber", ComparisonOperator.GTE, 2))),
        joinCsvRows(
            "a,foo,2,30",
            "a,foo,3,38",
            "a,bar,2,60",
            "b,foo,2,60",
            "b,bar,2,72",
            "b,bar,3,73",
            "b,bar,4,74",
            "b,bar,5,75"
        ));
    assertResultSetEquals(runAndPrint(fromScore().selectAll()
            .where(new CompoundRowPredicate(
                new ColValueComparison<String>("modelId", ComparisonOperator.EQ, "foo"),
                BooleanBinaryOperator.AND,
                new ColValueComparison<Integer>("gameNumber", ComparisonOperator.EQ, 2)))),
        joinCsvRows(
            "a,foo,2,30",
            "b,foo,2,60"
        ));
  }


  public void testAggregation() throws Exception {
    assertResultSetEquals(runAndPrint(fromScore()
        .select("uid", "modelId")
        .selectAggregatedCol(Count.class, "*")
        .selectAggregatedCol(Avg.class, "score").groupBy("uid", "modelId")),
        joinCsvRows(
            "a,foo,3,29.33",
            "a,bar,2,50",
            "b,foo,2,55",
            "b,bar,5,73",
            "c,foo,1,81",
            "c,bar,1,82",
            "c,baz,1,83"
        ));
    assertResultSetEquals(runAndPrint(fromScore().select("uid").selectAggregatedCol(Count.class, "*").selectAggregatedCol(Avg.class, "score").groupBy("uid")), joinCsvRows(
        "a,5,37.60",
        "b,7,67.86",
        "c,3,82"
    ));
    assertResultSetEquals(runAndPrint(fromScore().selectAggregatedCol(Count.class, "*").selectAggregatedCol(Avg.class, "score").groupBy()), joinCsvRows(
        "15,60.60"
    ));
    // now test aggregation with sorting
    assertResultSetEquals(
        runAndPrint(fromScore()
            .select("uid", "modelId")
            .selectAggregatedCol(Count.class, "*")
            .selectAggregatedCol(Avg.class, "score")
            .groupBy("uid", "modelId")
            .sortBy("-uid", "Count(*)", "-Avg(score)")
        ),
        joinCsvRows(
            "c,baz,1,83",
            "c,bar,1,82",
            "c,foo,1,81",
            "b,foo,2,55",
            "b,bar,5,73",
            "a,bar,2,50",
            "a,foo,3,29.33"
        ));
  }

  public void testCrossJoin() throws Exception {
    // cross product of 2 relations
    ResultSet scoresGroupedByUid = runAndPrint(fromScore().select("uid").selectAggregatedCol(Count.class, "*").selectAggregatedCol(Avg.class, "score").groupBy("uid"));
    ResultSet users = runAndPrint(fromUser().selectColAs("uid", "User.uid").select("name"));
    assertResultSetEquals(
        runAndPrint(new QueryBuilder()
            .from(scoresGroupedByUid, users)
            .select("uid").select("name").select("Count(*)").select("Avg(score)")),
        joinCsvRows(
            "a,Amy,5,37.60",
            "a,Ben,5,37.60",
            "a,Dick,5,37.60",
            "b,Amy,7,67.86",
            "b,Ben,7,67.86",
            "b,Dick,7,67.86",
            "c,Amy,3,82",
            "c,Ben,3,82",
            "c,Dick,3,82"
        ));
    // same thing with the LHS and RHS reversed
    assertResultSetEquals(
        runAndPrint(new QueryBuilder()
            .from(users, scoresGroupedByUid)
            .select("uid").select("name").select("Count(*)").select("Avg(score)")),
        joinCsvRows(
            "a,Amy,5,37.60",
            "b,Amy,7,67.86",
            "c,Amy,3,82",
            "a,Ben,5,37.60",
            "b,Ben,7,67.86",
            "c,Ben,3,82",
            "a,Dick,5,37.60",
            "b,Dick,7,67.86",
            "c,Dick,3,82"
        ));
  }

  public void testNaturalInnerJoin() throws Exception {
    // inner natural join of two relations without duplicate entries
    ResultSet scoresGroupedByUid = runAndPrint(fromScore().select("uid").selectAggregatedCol(Count.class, "*").selectAggregatedCol(Avg.class, "score").groupBy("uid"));
    ResultSet users = runAndPrint(fromUser().select("uid").select("name"));
    assertResultSetEquals(
        runAndPrint(new QueryBuilder()
            .from(scoresGroupedByUid).naturalJoin().from(users)
            .select("uid").select("name").select("Count(*)").select("Avg(score)")),
        joinCsvRows(
            "a,Amy,5,37.60",
            "b,Ben,7,67.86"
        ));
    // same thing with the LHS and RHS reversed
    assertResultSetEquals(
        runAndPrint(new QueryBuilder()
            .from(users).naturalJoin().from(scoresGroupedByUid)
            .select("uid").select("name").select("Count(*)").select("Avg(score)")),
        joinCsvRows(
            "a,Amy,5,37.60",
            "b,Ben,7,67.86"
        ));
    // same thing but with sorting
    assertResultSetEquals(
        runAndPrint(new QueryBuilder()
            .from(scoresGroupedByUid).naturalJoin().from(users)
            .select("uid").select("name").select("Count(*)").select("Avg(score)").sortBy("-Avg(score)")),
        joinCsvRows(
            "b,Ben,7,67.86",
            "a,Amy,5,37.60"
        ));
    // same thing with the LHS and RHS reversed
    assertResultSetEquals(
        runAndPrint(new QueryBuilder()
            .from(users).naturalJoin().from(scoresGroupedByUid)
            .select("uid").select("name").select("Count(*)").select("Avg(score)").sortBy("-Avg(score)")),
        joinCsvRows(
            "b,Ben,7,67.86",
            "a,Amy,5,37.60"
        ));
    // inner natural join of two relations with duplicate entries
    ResultSet scores = runAndPrint(fromScore().select("uid", "modelId", "gameNumber", "score"));
    assertResultSetEquals(
        runAndPrint(new QueryBuilder()
            .from(scores).naturalJoin().from(users)
            .select("uid", "name", "modelId", "gameNumber", "score")),
        joinCsvRows(
            "a,Amy,foo,1,20",
            "a,Amy,foo,2,30",
            "a,Amy,foo,3,38",
            "a,Amy,bar,1,40",
            "a,Amy,bar,2,60",
            "b,Ben,foo,1,50",
            "b,Ben,foo,2,60",
            "b,Ben,bar,1,71",
            "b,Ben,bar,2,72",
            "b,Ben,bar,3,73",
            "b,Ben,bar,4,74",
            "b,Ben,bar,5,75"
        ));
    // same thing with the LHS and RHS reversed
    assertResultSetEquals(
        runAndPrint(new QueryBuilder()
            .from(users).naturalJoin().from(scores)
            .select("uid", "name", "modelId", "gameNumber", "score")),
        joinCsvRows(
            "a,Amy,foo,1,20",
            "a,Amy,foo,2,30",
            "a,Amy,foo,3,38",
            "a,Amy,bar,1,40",
            "a,Amy,bar,2,60",
            "b,Ben,foo,1,50",
            "b,Ben,foo,2,60",
            "b,Ben,bar,1,71",
            "b,Ben,bar,2,72",
            "b,Ben,bar,3,73",
            "b,Ben,bar,4,74",
            "b,Ben,bar,5,75"
        ));
  }

  public void testNaturalLeftOuterJoin() throws Exception {
    // inner natural left outer join of two relations without duplicate entries
    ResultSet scoresGroupedByUid = runAndPrint(fromScore().select("uid").selectAggregatedCol(Count.class, "*").selectAggregatedCol(Avg.class, "score").groupBy("uid"));
    ResultSet users = runAndPrint(fromUser().select("uid").select("name"));
    assertResultSetEquals(
        runAndPrint(new QueryBuilder()
            .from(scoresGroupedByUid).naturalJoin(Join.Type.LEFT_OUTER).from(users)
            .select("uid").select("name").select("Count(*)").select("Avg(score)")),
        joinCsvRows(
            "a,Amy,5,37.60",
            "b,Ben,7,67.86",
            "c,null,3,82"
        ));
    // same thing with the LHS and RHS reversed
    assertResultSetEquals(
        runAndPrint(new QueryBuilder()
            .from(users).naturalJoin(Join.Type.LEFT_OUTER).from(scoresGroupedByUid)
            .select("uid").select("name").select("Count(*)").select("Avg(score)")),
        joinCsvRows(
            "a,Amy,5,37.60",
            "b,Ben,7,67.86",
            "d,Dick,null,null"
        ));
    // same thing but with sorting
    assertResultSetEquals(
        runAndPrint(new QueryBuilder()
            .from(scoresGroupedByUid).naturalJoin(Join.Type.LEFT_OUTER).from(users)
            .select("uid").select("name").select("Count(*)").select("Avg(score)").sortBy("-Avg(score)")),
        joinCsvRows(
            "c,null,3,82",
            "b,Ben,7,67.86",
            "a,Amy,5,37.60"
        ));
    // same thing with the LHS and RHS reversed
    assertResultSetEquals(
        runAndPrint(new QueryBuilder()
            .from(users).naturalJoin(Join.Type.LEFT_OUTER).from(scoresGroupedByUid)
            .select("uid").select("name").select("Count(*)").select("Avg(score)").sortBy("-Avg(score)")),
        joinCsvRows(
            "b,Ben,7,67.86",
            "a,Amy,5,37.60",
            "d,Dick,null,null"
        ));
    // left outer natural join of two relations with duplicate entries
    ResultSet scores = runAndPrint(fromScore().select("uid", "modelId", "gameNumber", "score"));
    assertResultSetEquals(
        runAndPrint(new QueryBuilder()
            .from(scores).naturalJoin(Join.Type.LEFT_OUTER).from(users)
            .select("uid", "name", "modelId", "gameNumber", "score")),
        joinCsvRows(
            "a,Amy,foo,1,20",
            "a,Amy,foo,2,30",
            "a,Amy,foo,3,38",
            "a,Amy,bar,1,40",
            "a,Amy,bar,2,60",
            "b,Ben,foo,1,50",
            "b,Ben,foo,2,60",
            "b,Ben,bar,1,71",
            "b,Ben,bar,2,72",
            "b,Ben,bar,3,73",
            "b,Ben,bar,4,74",
            "b,Ben,bar,5,75",
            "c,null,foo,1,81",
            "c,null,bar,1,82",
            "c,null,baz,1,83"
        ));
    // same thing with the LHS and RHS reversed
    assertResultSetEquals(
        runAndPrint(new QueryBuilder()
            .from(users).naturalJoin(Join.Type.LEFT_OUTER).from(scores)
            .select("uid", "name", "modelId", "gameNumber", "score")),
        joinCsvRows(
            "a,Amy,foo,1,20",
            "a,Amy,foo,2,30",
            "a,Amy,foo,3,38",
            "a,Amy,bar,1,40",
            "a,Amy,bar,2,60",
            "b,Ben,foo,1,50",
            "b,Ben,foo,2,60",
            "b,Ben,bar,1,71",
            "b,Ben,bar,2,72",
            "b,Ben,bar,3,73",
            "b,Ben,bar,4,74",
            "b,Ben,bar,5,75",
            "d,Dick,null,null,null"
        ));
  }

  public void testNaturalRightOuterJoin() throws Exception {
    // inner natural right outer join of two relations without duplicate entries
    ResultSet scoresGroupedByUid = runAndPrint(fromScore().select("uid").selectAggregatedCol(Count.class, "*").selectAggregatedCol(Avg.class, "score").groupBy("uid"));
    ResultSet users = runAndPrint(fromUser().select("uid").select("name"));
    assertResultSetEquals(
        runAndPrint(new QueryBuilder()
            .from(scoresGroupedByUid).naturalJoin(Join.Type.RIGHT_OUTER).from(users)
            .select("uid").select("name").select("Count(*)").select("Avg(score)")),
        joinCsvRows(
            "a,Amy,5,37.60",
            "b,Ben,7,67.86",
            "d,Dick,null,null"
        ));
    // same thing with the LHS and RHS reversed
    assertResultSetEquals(
        runAndPrint(new QueryBuilder()
            .from(users).naturalJoin(Join.Type.RIGHT_OUTER).from(scoresGroupedByUid)
            .select("uid").select("name").select("Count(*)").select("Avg(score)")),
        joinCsvRows(
            "a,Amy,5,37.60",
            "b,Ben,7,67.86",
            "c,null,3,82"
        ));
    // same thing but with sorting
    assertResultSetEquals(
        runAndPrint(new QueryBuilder()
            .from(scoresGroupedByUid).naturalJoin(Join.Type.RIGHT_OUTER).from(users)
            .select("uid").select("name").select("Count(*)").select("Avg(score)").sortBy("-Avg(score)")),
        joinCsvRows(
            "b,Ben,7,67.86",
            "a,Amy,5,37.60",
            "d,Dick,null,null"
        ));
    // same thing with the LHS and RHS reversed
    assertResultSetEquals(
        runAndPrint(new QueryBuilder()
            .from(users).naturalJoin(Join.Type.RIGHT_OUTER).from(scoresGroupedByUid)
            .select("uid").select("name").select("Count(*)").select("Avg(score)").sortBy("-Avg(score)")),
        joinCsvRows(
            "c,null,3,82",
            "b,Ben,7,67.86",
            "a,Amy,5,37.60"
        ));
    // right outer natural join of two relations with duplicate entries
    ResultSet scores = runAndPrint(fromScore().select("uid", "modelId", "gameNumber", "score"));
    assertResultSetEquals(
        runAndPrint(new QueryBuilder()
            .from(scores).naturalJoin(Join.Type.RIGHT_OUTER).from(users)
            .select("uid", "name", "modelId", "gameNumber", "score")),
        joinCsvRows(
            "a,Amy,foo,1,20",
            "a,Amy,foo,2,30",
            "a,Amy,foo,3,38",
            "a,Amy,bar,1,40",
            "a,Amy,bar,2,60",
            "b,Ben,foo,1,50",
            "b,Ben,foo,2,60",
            "b,Ben,bar,1,71",
            "b,Ben,bar,2,72",
            "b,Ben,bar,3,73",
            "b,Ben,bar,4,74",
            "b,Ben,bar,5,75",
            "d,Dick,null,null,null"
        ));
    // same thing with the LHS and RHS reversed
    assertResultSetEquals(
        runAndPrint(new QueryBuilder()
            .from(users).naturalJoin(Join.Type.RIGHT_OUTER).from(scores)
            .select("uid", "name", "modelId", "gameNumber", "score")),
        joinCsvRows(
            "a,Amy,foo,1,20",
            "a,Amy,foo,2,30",
            "a,Amy,foo,3,38",
            "a,Amy,bar,1,40",
            "a,Amy,bar,2,60",
            "b,Ben,foo,1,50",
            "b,Ben,foo,2,60",
            "b,Ben,bar,1,71",
            "b,Ben,bar,2,72",
            "b,Ben,bar,3,73",
            "b,Ben,bar,4,74",
            "b,Ben,bar,5,75",
            "c,null,foo,1,81",
            "c,null,bar,1,82",
            "c,null,baz,1,83"
        ));
  }

  public void testNaturalFullOuterJoin() throws Exception {
    // inner natural full outer join of two relations without duplicate entries
    ResultSet scoresGroupedByUid = runAndPrint(fromScore().select("uid").selectAggregatedCol(Count.class, "*").selectAggregatedCol(Avg.class, "score").groupBy("uid"));
    ResultSet users = runAndPrint(fromUser().select("uid").select("name"));
    assertResultSetEquals(
        runAndPrint(new QueryBuilder()
            .from(scoresGroupedByUid).naturalJoin(Join.Type.FULL_OUTER).from(users)
            .select("uid").select("name").select("Count(*)").select("Avg(score)")),
        joinCsvRows(
            "a,Amy,5,37.60",
            "b,Ben,7,67.86",
            "c,null,3,82",
            "null,Dick,null,null"
        ));
    // same thing with the LHS and RHS reversed
    assertResultSetEquals(
        runAndPrint(new QueryBuilder()
            .from(users).naturalJoin(Join.Type.FULL_OUTER).from(scoresGroupedByUid)
            .select("uid").select("name").select("Count(*)").select("Avg(score)")),
        joinCsvRows(
            "a,Amy,5,37.60",
            "b,Ben,7,67.86",
            "d,Dick,null,null",
            "null,null,3,82"
        ));
    // same thing but with sorting
    assertResultSetEquals(
        runAndPrint(new QueryBuilder()
            .from(scoresGroupedByUid).naturalJoin(Join.Type.FULL_OUTER).from(users)
            .select("uid").select("name").select("Count(*)").select("Avg(score)").sortBy("-Avg(score)")),
        joinCsvRows(
            "c,null,3,82",
            "b,Ben,7,67.86",
            "a,Amy,5,37.60",
            "null,Dick,null,null"
        ));
    // same thing with the LHS and RHS reversed
    assertResultSetEquals(
        runAndPrint(new QueryBuilder()
            .from(users).naturalJoin(Join.Type.FULL_OUTER).from(scoresGroupedByUid)
            .select("uid").select("name").select("Count(*)").select("Avg(score)").sortBy("-Avg(score)")),
        joinCsvRows(
            "null,null,3,82",
            "b,Ben,7,67.86",
            "a,Amy,5,37.60",
            "d,Dick,null,null"
        ));
    // full outer natural join of two relations with duplicate entries
    ResultSet scores = runAndPrint(fromScore().select("uid", "modelId", "gameNumber", "score"));
    assertResultSetEquals(
        runAndPrint(new QueryBuilder()
            .from(scores).naturalJoin(Join.Type.FULL_OUTER).from(users)
            .select("uid", "name", "modelId", "gameNumber", "score")),
        joinCsvRows(
            "a,Amy,foo,1,20",
            "a,Amy,foo,2,30",
            "a,Amy,foo,3,38",
            "a,Amy,bar,1,40",
            "a,Amy,bar,2,60",
            "b,Ben,foo,1,50",
            "b,Ben,foo,2,60",
            "b,Ben,bar,1,71",
            "b,Ben,bar,2,72",
            "b,Ben,bar,3,73",
            "b,Ben,bar,4,74",
            "b,Ben,bar,5,75",
            "c,null,foo,1,81",
            "c,null,bar,1,82",
            "c,null,baz,1,83",
            "null,Dick,null,null,null"
        ));
    // same thing with the LHS and RHS reversed
    assertResultSetEquals(
        runAndPrint(new QueryBuilder()
            .from(users).naturalJoin(Join.Type.FULL_OUTER).from(scores)
            .select("uid", "name", "modelId", "gameNumber", "score")),
        joinCsvRows(
            "a,Amy,foo,1,20",
            "a,Amy,foo,2,30",
            "a,Amy,foo,3,38",
            "a,Amy,bar,1,40",
            "a,Amy,bar,2,60",
            "b,Ben,foo,1,50",
            "b,Ben,foo,2,60",
            "b,Ben,bar,1,71",
            "b,Ben,bar,2,72",
            "b,Ben,bar,3,73",
            "b,Ben,bar,4,74",
            "b,Ben,bar,5,75",
            "d,Dick,null,null,null",
            "null,null,foo,1,81",
            "null,null,bar,1,82",
            "null,null,baz,1,83"
        ));
  }

  // TODO: test more join types (predicated join, equijoin)
}
