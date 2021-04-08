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

package solutions.trsoftware.commons.shared.util.template;

import com.google.gwt.text.shared.AbstractRenderer;
import solutions.trsoftware.commons.shared.util.MapUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static solutions.trsoftware.commons.shared.util.StringUtils.join;

/**
 * A lightweight string template abstraction.  Instances of this
 * class could be constructed programmatically, but it's really intended
 * to be constructed by a separate factory/parser object.
 * <p>
 * For instance, on the server, you'd want to construct templates by parsing
 * files, and on the client (GWT), you might want to construct templates
 * by extracting the contents of a {@code div}.
 * <p>
 * Either way, the parsing is decoupled from representation and application
 * (this class is responsible for the representation and application).
 * Therefore the syntax of the template (language) is defined by the parser,
 * not by this class.  For example some parsers could treat {@code ${var}} as variables
 * while others could treat {@code @@var@@} as variables.
 * <p>
 * This class is immutable, so instances may be cached and shared among threads.
 *
 * @author Alex
 */
public final class Template extends AbstractRenderer<Map<String, ?>> {

  private final List<TemplatePart> parts;

  /** Used to set the initial string buffer size when applying this template */
  private final int bufferSizeHint;

  public Template(List<TemplatePart> parts) {
    this(parts, 128);
  }

  public Template(List<TemplatePart> parts, int bufferSizeHint) {
    this.parts = Collections.unmodifiableList(parts);
    this.bufferSizeHint = bufferSizeHint;
  }

  /** For templates that don't have any variables, this method can be used */
  public String render() {
    return render(Collections.emptyMap());
  }

  /**
   * Renders the template by applying each value contained in an odd-numbered elements of the arg array to a variable
   * whose name is given by the element preceding it.
   */
  public String render(String... nameValuePairs) {
    return render(MapUtils.stringMap(nameValuePairs));
  }

  /**
   * Renders the template by applying each value in the given map to a variable whose name is given by key of the map entry.
   */
  @Override
  public String render(Map<String, ?> substitutions) {
    // TODO: throw an exception if variables don't match?
    StringBuilder out = new StringBuilder(bufferSizeHint);
    for (TemplatePart part : parts) {
      part.write(out, substitutions);
    }
    return out.toString();
  }

  /**
   * Renders the template by applying each given value to the next available variable, ignoring the names of the variables
   * defined in the template.
   * @deprecated should use this method with caution when the template contains multiple instances of a variable with the same name (the second arg might overwrite the first)
   */
  public String renderPositional(Object... values) {
    // TODO: throw an exception if the number of args doesn't match the number of variables?
    HashMap<String, String> substitutions = new HashMap<String, String>();
    int i = 0;
    for (TemplatePart part : parts) {
      if (part instanceof VariablePart) {
        String varName = ((VariablePart)part).getVarName();
        if (!substitutions.containsKey(varName))
          substitutions.put(varName, String.valueOf(values[i++]));
      }
    }
    return render(substitutions);
  }

  public List<TemplatePart> getParts() {
    // NOTE: this list is "unmodifiable" so we don't need to make a defensive copy to preserve immutability
    return parts;
  }

  @Override
  public String toString() {
    return new StringBuilder("Template(\"").append(join("", parts)).append("\")").toString();
  }

  /** Creates an instance from the given arg parsed with {@link SimpleTemplateParser#DEFAULT_SYNTAX} */
  public static Template parse(String template) {
    return SimpleTemplateParser.parseDefault(template);
  }

}
