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

package solutions.trsoftware.tools.util;

import solutions.trsoftware.commons.server.util.collections.DefaultMapByReflection;
import solutions.trsoftware.commons.shared.util.collections.DefaultMap;
import solutions.trsoftware.commons.shared.util.template.*;

import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.Properties;

/**
 * Instruments {@link Properties#getProperty(String)} to provide Ant-like variable resolution, where variables
 * are represented as <code>${<i>var</i>}</code> and represent values of other properties defined in the same file.
 *
 * <p>
 * For example, {@link #getProperty(String) getProperty("bar")} would return {@code "123456"}
 * when loaded from a properties file that looks like this:
 * <pre>
 *     foo=123
 *     bar=${foo}456
 *   </pre>
 * </p>
 *
 * It is strongly encouraged to call {@link #resolveAll()} up-front after all the properties have been added
 * (e.g. with {@link #load(Reader)} or {@link #load(InputStream)})
 *
 * @author Alex
 * @since 2/25/2018
 */
public class ResolvedProperties extends Properties {

  public static final String VAR_OPEN = "${";
  public static final String VAR_CLOSE = "}";

  // TODO: do we want to allow comments in the values?
  private static final SimpleTemplateParser valueParser = new SimpleTemplateParser(VAR_OPEN, VAR_CLOSE, "/*", "*/");

  /**
   * Variables that are still in the process of being resolved. This map is used to prevent infinite recursion.
   */
  private DefaultMap<String, StringBuilder> valueBuilders = new DefaultMapByReflection<>(StringBuilder.class);

  public ResolvedProperties() {
  }

  public ResolvedProperties(Properties defaults) {
    super(defaults);
  }

  @Override
  public String getProperty(String key) {
    return resolve(key);
  }

  private String resolve(String key) {
    String value = super.getProperty(key);
    if (value == null)
      return null;  // prop doesn't exist
    if (!value.contains(VAR_OPEN))
      return value;  // no variables in this prop value
    // contains at least 1 variable; must resolve
    Template template = valueParser.parseTemplate(value);
    List<TemplatePart> parts = template.getParts();
    StringBuilder valueBuilder = valueBuilders.get(key);
    for (TemplatePart part : parts) {
      if (part instanceof VariablePart) {
        String varName = ((VariablePart)part).getVarName();
        if (valueBuilders.containsKey(varName)) {
          // recursive reference; this variable is already being resolved
          throw new IllegalStateException(String.format("Recursive variable reference (%s%s%s) in property '%s'", VAR_OPEN, varName, VAR_CLOSE, key));
        }
        else
          valueBuilder.append(resolve(varName));
      }
      else {
        assert part instanceof StringPart;
        valueBuilder.append(part);
      }
    }
    String resolvedValue = valueBuilder.toString();
    // mark this prop as having been resolved already
    valueBuilders.remove(key);
    setProperty(key, resolvedValue);
    return resolvedValue;
  }

  /**
   * Resolves all the variables at the same time.
   * Doing this up-front guarantees that no exceptions will be thrown by later calls to {@link #getProperty(String)}
   * (assuming that no new properties have been added).
   *
   * @throws RuntimeException any exception encountered during variable resolution
   */
  public void resolveAll() {
    for (String key : stringPropertyNames()) {
      resolve(key);
    }
  }

}
