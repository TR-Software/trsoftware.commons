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

package solutions.trsoftware.commons.server.memquery.algebra;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import solutions.trsoftware.commons.server.memquery.Row;
import solutions.trsoftware.commons.server.memquery.schema.ColSpec;
import solutions.trsoftware.commons.server.memquery.schema.NameAccessorColSpec;
import solutions.trsoftware.commons.server.memquery.schema.RenamedColSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A relational rename (&rho;) operation.  Produces an output relation with the same rows as the input relation,
 * but uses a different name for the output relation and/or its attributes.
 *
 * @author Alex, 1/14/14
 */
public class Rename extends StreamableUnaryOperation<Rename.Params> {

  public Rename(RelationalExpression input, String newRelationName, Map<String, String> newAttributeNames) {
    super(input, new Params(newRelationName, newAttributeNames));
    getOutputSchema(); // fail early if the output schema can't be generated (the renaming params cause a conflict)
  }

  @Override
  protected String createOutputName() {
    String newRelationName = this.params.getNewRelationName();
    if (newRelationName != null)
      return newRelationName;
    return super.createOutputName();
  }

  @Override
  protected Iterable<String> getOutputColNames() {
    List<String> ret = new ArrayList<String>();
    for (ColSpec colSpec : getInputSchema()) {
      ret.add(this.params.getNewAttributeName(colSpec.getName()));
    }
    return ret;
  }

  @Override
  protected ColSpec createColSpec(String name) {
    String oldName = this.params.getOldAttributeName(name);
    ColSpec oldCol = getInputSchema().get(oldName);
    if (name.equals(oldName))
      return new NameAccessorColSpec(oldCol); // this col is not being renamed
    else
      return new RenamedColSpec(name, oldCol);
  }

  @Override
  public Row call(Row inputRow) {
    // TODO: rather than create a new row, this operation should just modify the schema (otherwise too slow)
    return super.call(inputRow);
  }

  /**
   * @author Alex, 1/14/14
   */
  public static class Params {

    /**
     * The output relation will have this name.
     */
    private String newRelationName;

    /**
     * Gives the output attribute names for each input attribute name.
     */
    private final BiMap<String, String> oldToNewAttrMap;


    public Params(String newRelationName, Map<String, String> oldToNewAttrMap) {
      this.newRelationName = newRelationName;
      this.oldToNewAttrMap = ImmutableBiMap.copyOf(oldToNewAttrMap);
    }

    public String getNewRelationName() {
      return newRelationName;
    }

    public void setNewRelationName(String newRelationName) {
      this.newRelationName = newRelationName;
    }

    /**
     * @return the renamed version of the original attribute name.
     */
    public String getNewAttributeName(String oldName) {
      return lookup(oldName, oldToNewAttrMap);
    }

    /**
     * @return the original name for the given renamed attribute name.
     */
    public String getOldAttributeName(String newName) {
      return lookup(newName, oldToNewAttrMap.inverse());
    }

    private String lookup(String name, BiMap<String, String> map) {
      if (map.containsKey(name))
        return map.get(name);
      return name;
    }

  }
}
