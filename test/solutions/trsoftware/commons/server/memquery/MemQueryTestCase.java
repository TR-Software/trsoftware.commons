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

import junit.framework.TestCase;
import solutions.trsoftware.commons.server.io.StringPrintStream;
import solutions.trsoftware.commons.server.io.csv.CSVWriter;
import solutions.trsoftware.commons.server.memquery.output.CsvPrinter;
import solutions.trsoftware.commons.server.memquery.output.FixedWidthPrinter;
import solutions.trsoftware.commons.shared.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @author Alex, 4/16/2015
 */
public abstract class MemQueryTestCase extends TestCase {

  /** A dummy data model class for testing */
  class Score {
    private String uid;
    private String modelId;
    private int gameNumber;
    private double score;

    Score(String uid, String modelId, int gameNumber, double score) {
      this.uid = uid;
      this.modelId = modelId;
      this.gameNumber = gameNumber;
      this.score = score;
    }
  }

  /** Another dummy data model class for testing */
  class User {
    private String uid;
    private String name;

    User(String uid, String name) {
      this.uid = uid;
      this.name = name;
    }
  }

  private final List<Score> scores = Arrays.asList(
      new Score("a", "foo", 1, 20),
      new Score("a", "foo", 2, 30),
      new Score("a", "foo", 3, 38),
      new Score("a", "bar", 1, 40),
      new Score("a", "bar", 2, 60),
      new Score("b", "foo", 1, 50),
      new Score("b", "foo", 2, 60),
      new Score("b", "bar", 1, 71),
      new Score("b", "bar", 2, 72),
      new Score("b", "bar", 3, 73),
      new Score("b", "bar", 4, 74),
      new Score("b", "bar", 5, 75),
      new Score("c", "foo", 1, 81),
      new Score("c", "bar", 1, 82),
      new Score("c", "baz", 1, 83)
  );

  private final List<User> users = Arrays.asList(
      new User("a", "Amy"),
      new User("b", "Ben"),
      new User("d", "Dick")
  );

  protected MaterializedRelation scoreRelation;
  protected MaterializedRelation userRelation;
  protected FixedWidthPrinter resultPrinter;

  public void setUp() throws Exception {
    super.setUp();
    scoreRelation = QueryBuilder.createORM(Score.class, scores);
    userRelation = QueryBuilder.createORM(User.class, users);
    resultPrinter = new FixedWidthPrinter().setPreambleEnabled(false).setBordersEnabled(true).setOrdinalColEnabled(false);
  }

  public void tearDown() throws Exception {
    super.tearDown();
    Thread.sleep(10);  // do a short sleep to allow all the System.out stream writes to complete so that the test runner properly groups the output by test case
  }

  static String joinCsvRows(String...rows) {
    return StringUtils.join(CSVWriter.DEFAULT_LINE_END, rows) + CSVWriter.DEFAULT_LINE_END;
  }

  static void assertResultSetEquals(ResultSet resultSet, String expectedCsv) {
    CsvPrinter csvPrinter = new CsvPrinter().setHeaderRowEnabled(false).setOrdinalColEnabled(false).setPreambleEnabled(false);
    StringPrintStream resultSetOutput = new StringPrintStream();
    csvPrinter.print(resultSet, resultSetOutput);
    assertEquals(expectedCsv, resultSetOutput.toString());
  }


  protected QueryBuilder fromUser() {
    return new QueryBuilder().from(userRelation);
  }

  protected QueryBuilder fromScore() {
    return new QueryBuilder().from(scoreRelation);
  }

  protected ResultSet runAndPrint(QueryBuilder qb) {
    BoundQuery boundQuery = qb.boundQuery();
    System.out.println(boundQuery.getQuery());
    ResultSet resultSet = boundQuery.eval();
    resultPrinter.print(resultSet);

    return resultSet;
  }


}
