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

package solutions.trsoftware.commons.shared.util.template;

import java.util.Map;

/**
 * Replaces its variable with the given substitution, or if not there,
 * writes nothing to the buffer.
 */
public class VariablePart implements TemplatePart {
  private final String varName;

  public VariablePart(String varName) {
    this.varName = varName;
  }

  public StringBuilder write(StringBuilder buffer, Map<String, String> substitutions) {
    if (substitutions.containsKey(varName))
      return buffer.append(String.valueOf(substitutions.get(varName)));
    return buffer;
  }

  public String getVarName() {
    return varName;
  }

  @Override
  public String toString() {
    return "${" + varName + '}';
  }
}
