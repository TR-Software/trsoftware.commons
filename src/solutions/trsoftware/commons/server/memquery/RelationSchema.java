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

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import solutions.trsoftware.commons.server.memquery.schema.ColSpec;
import solutions.trsoftware.commons.shared.util.LazyReference;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static solutions.trsoftware.commons.server.memquery.util.NameUtils.mapNamesToOrdinals;

/**
 * Immutable specification of the attributes of a relational tuple.
 *
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
    this.colSpecs = ImmutableList.copyOf(colSpecs);
    // TODO: ColSpec should probably implement equals/hashCode in order for BiMap to make sense here (unless we only care about identity equality)
    colSpecByName = ImmutableBiMap.copyOf(mapByName(colSpecs));
    colIndexByName = ImmutableMap.copyOf(mapNamesToOrdinals(colSpecs));
    colNames = ImmutableList.copyOf(colSpecByName.keySet());
  }

  private Map<String, ? extends ColSpec> mapByName(List<? extends ColSpec> colSpecs) {
    LinkedHashMap<String, ColSpec> ret = new LinkedHashMap<>();
    for (ColSpec colSpec : colSpecs) {
      String name = colSpec.getName();
      ColSpec existing = ret.put(name, colSpec);
      if (existing != null) {
        throw new IllegalArgumentException(String.format("Duplicated col name '%s' in schema %s", name, this));
      }
    }
    return ret;
  }

  public RelationSchema(String name, Iterable<? extends ColSpec> colSpecs) {
    this(name, ImmutableList.copyOf(colSpecs));
  }

  public List<String> getColNames() {
    return colNames;
  }

  /**
   * Lazily-initialized mapping of col names to their types
   */
  private final LazyReference<Map<String, Class>> colTypesByName = new LazyReference<Map<String, Class>>() {
    @Override
    protected Map<String, Class> create() {
      ImmutableMap.Builder<String, Class> builder = ImmutableMap.builder();
      for (ColSpec colSpec : colSpecs) {
        builder.put(colSpec.getName(), colSpec.getType());
      }
      return builder.build();
    }
  };

  /**
   * Lazily-initialized list of col types (listed in the order they appear in the schema)
   */
  private final LazyReference<List<Class>> colTypes = new LazyReference<List<Class>>() {
    @Override
    protected List<Class> create() {
      return ImmutableList.copyOf(colTypesByName.get().values());
    }
  };

  /**
   * Returns a "canonical" form of this schema, as an ordered collection of names and data types of the attributes.
   * <p>
   * The returned object can be used to compare 2 instances of this class when the implementation details of the
   * individual {@link ColSpec}s are irrelevant (i.e. when only the name and data type of a column are important),
   * and can be safely used as a hash key (because it's immutable).
   * <p>
   * <strong>CAUTION:</strong> comparing {@link Map} instances doesn't take the ordering into account.
   *
   * @return an immutable mapping of col names to their types
   * @see #getColTypes()
   * @see <a href="https://github.com/google/guava/wiki/ImmutableCollectionsExplained">Immutable Collections</a>
   */
  public Map<String, Class> getColTypesByName() {
    return colTypesByName.get();
  }

  /**
   * @return an immutable list of the col types (in the order they appear in the schema)
   * @see <a href="https://github.com/google/guava/wiki/ImmutableCollectionsExplained">Immutable Collections</a>
   */
  public List<Class> getColTypes() {
    return colTypes.get();
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

  @Nonnull
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
