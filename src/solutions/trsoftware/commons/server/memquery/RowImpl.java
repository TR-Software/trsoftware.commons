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

import java.util.Arrays;

/**
* @author Alex, 1/14/14
*/
public class RowImpl extends AbstractRow implements MutableRow {

  private final Object[] data;

  public RowImpl(RelationSchema schema) {
    super(schema);
    this.data = new Object[size()];
  }

  @Override
  public <V> V getValue(int colIndex) {
    return (V)data[colIndex];
  }

  @Override
  public <V> V getValue(String colName) {
    return getValue(getColIndex(colName));
  }

  @Override
  public <V> void setValue(int colIndex, V value) {
    data[colIndex] = value;
  }

  @Override
  public <V> void setValue(String colName, V value) {
    setValue(getColIndex(colName), value);
  }

  @Override
  public String toString() {
    return Arrays.deepToString(data);
  }

}
