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

package solutions.trsoftware.commons.server.memquery.schema;

import solutions.trsoftware.commons.server.memquery.Row;
import solutions.trsoftware.commons.server.memquery.aggregations.Aggregation;

/**
 * A col spec that fetches its values from a given row by attribute name.
 *
 * @param <T> the data type of the values in this column
 * @author Alex, 1/10/14
 */
public class NameAccessorColSpec<T> extends NamedTypedColSpec<T> {

  public NameAccessorColSpec(String name, Class<T> type) {
    super(name, type);
  }

  public NameAccessorColSpec(ColSpec inputCol) {
    this(inputCol.getName(), inputCol.getType());
  }

  @Override
  public T getValue(Row row) {
    Object value = doGetValue(row);
    if (value instanceof Aggregation)  // unpack aggregations
      value = ((Aggregation)value).get();
    return (T)value;
  }

  /** Subclasses can override this method */
  protected Object doGetValue(Row row) {
    return row.getValue(getName());
  }

}
