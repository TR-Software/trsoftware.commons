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

package solutions.trsoftware.commons.server.memquery;

import java.io.PrintStream;
import java.util.Map;

import static solutions.trsoftware.commons.server.memquery.PrintFormat.FIXED_WIDTH;

/**
 * A query plus its bindings (a mapping of the input relations, by name).
 *
 * @author Alex, 1/16/14
 */
public class BoundQuery {

  private MemQuery query;

  private Map<String, Relation> inputBindings;

  public BoundQuery(MemQuery query, Map<String, Relation> inputBindings) {
    this.query = query;
    this.inputBindings = inputBindings;
  }

  public MemQuery getQuery() {
    return query;
  }

  public Map<String, Relation> getInputBindings() {
    return inputBindings;
  }

  /** Runs the encapsulated query over the encapsulated inputs */
  public ResultSet eval() {
    return query.eval(inputBindings);
  }

  /** Evaluates the encapsulated query over the encapsulated inputs.
   * @see MemQuery#evalAsUnsortedStream(Map)
   */
  public Relation evalAsUnsortedStream() {
    return query.evalAsUnsortedStream(inputBindings);
  }

  /** Runs the encapsulated query and prints the results to the given stream using the default print format */
  public void evalPrint(PrintStream out) {
    evalPrint(out, FIXED_WIDTH);
  }

  /** Runs the encapsulated query and prints the results to the given stream using the given format */
  public void evalPrint(PrintStream out, PrintFormat printFormat) {
    MemQuery.printResultSet(eval(), out, printFormat);
  }

  @Override
  public String toString() {
    return query.toString();
  }
}
