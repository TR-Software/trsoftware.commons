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

import solutions.trsoftware.commons.server.memquery.struct.NamedTuple;
import solutions.trsoftware.commons.server.memquery.struct.OrderedTuple;

import java.util.List;

/**
* @author Alex, 1/8/14
*/
public interface Row extends NamedTuple, OrderedTuple {

  RelationSchema getSchema();  // TODO: why does each row need a pointer to its schema? this wastes memory

  /** Gets multiple named values at once */
  List<Object> getValues(List<String> names);

  Object getRawData();


}
