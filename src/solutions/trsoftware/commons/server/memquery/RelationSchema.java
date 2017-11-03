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

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import solutions.trsoftware.commons.server.memquery.schema.ColSpec;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static solutions.trsoftware.commons.client.util.CollectionUtils.asList;
import static solutions.trsoftware.commons.client.util.CollectionUtils.immutableList;
import static solutions.trsoftware.commons.server.memquery.util.NameUtils.mapByName;
import static solutions.trsoftware.commons.server.memquery.util.NameUtils.mapNamesToOrdinals;

/**
* @author Alex, 1/10/14
*/
public class RelationSchema implements HasName, HasSchema, Iterable<ColSpec> {

  private final String name;
  private final List<ColSpec> colSpecs;
  private final List<String> colNames;
  private final Map<String, Integer> colIndexByName;
  private final BiMap<String, ColSpec> colSpecByName;

  public RelationSchema(String name, List<? extends ColSpec> colSpecs) {
    this.name = name;
    this.colSpecs = unmodifiableList(colSpecs);
    colSpecByName = ImmutableBiMap.copyOf(mapByName(colSpecs));
    colIndexByName = unmodifiableMap(mapNamesToOrdinals(colSpecs));
    colNames = immutableList(colSpecByName.keySet());
  }

  public RelationSchema(String name, Iterable<? extends ColSpec> colSpecs) {
    this(name, asList(colSpecs));
  }

  public List<String> getColNames() {
    return colNames;
  }

  public int getColIndex(String colName) {
    try {
      return colIndexByName.get(colName);
    }
    catch (NullPointerException ex) {
      throw new IllegalArgumentException(String.format("There is no column named '%s' in relation %s", colName, this));
    }
  }

  public Map<String, ColSpec> getContext() {
    return colSpecByName;
  }

  public ColSpec get(int colIndex) {
    return colSpecs.get(colIndex);
  }

  public ColSpec get(String colName) {
    return get(getColIndex(colName));
  }

  public boolean contains(String colName) {
    return colSpecByName.containsKey(colName);
  }

  public boolean contains(ColSpec colSpec) {
    return colSpecByName.inverse().containsKey(colSpec);
  }

  public int size() {
    return colSpecs.size();
  }

  @Override
  public Iterator<ColSpec> iterator() {
    return colSpecs.iterator();
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name + colSpecs;
  }

  @Override
  public RelationSchema getSchema() {
    return this;
  }
}
