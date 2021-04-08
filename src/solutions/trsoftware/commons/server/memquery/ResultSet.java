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

/**
* @author Alex, 1/9/14
*/
public interface ResultSet extends MaterializedRelation {

  /** The query that produced this result set */
  MemQuery getQuery();

  void print(PrintStream out);

  /** Transforms this result set so it can be used as an input to another query */
  Map<String, Relation> asQueryInput();
}
