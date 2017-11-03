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

package solutions.trsoftware.commons.server.util;

import solutions.trsoftware.commons.client.util.template.SimpleTemplateParser;
import solutions.trsoftware.commons.client.util.template.Template;
import solutions.trsoftware.commons.client.util.template.TemplateParser;
import solutions.trsoftware.commons.server.io.ServerIOUtils;
import solutions.trsoftware.commons.shared.util.collections.DefaultMap;

import java.io.File;
import java.io.IOException;

/**
 * Provides a lightweight templating facility that reads a template file written in the syntax recognized
 * by {@link SimpleTemplateParser#parseDefault(String)}.
 *
 * Loading and parsing template file usually takes longer than rendering the template, therefore this class
 * caches the loaded {@link Template} instances (which makes sense, because they are immutable).
 *
 * This class is also immutable, so instances may be freely shared by threads.
 *
 * The name of the template should be a resource file name,
 * starting with "/".  This name is loaded via ServerStringUtils.class.getResource()
 * which is for some reason different from ServerStringUtils.class.getClassLoader().getResource()
 *
 * @author Alex
 */
public final class FileTemplateParser implements TemplateParser {

  // TODO: make this lazy-init to save memory when this class is not needed
  private static final FileTemplateParser instance = new FileTemplateParser();

  private final DefaultMap<File, Template> cache = new DefaultMap<File, Template>() {
    @Override
    public Template computeDefault(File key) {
      return templateFromFile(key);
    }
  };

  public static FileTemplateParser getInstance() {
    return instance;
  }

  /** This class should not be instantiated */
  private FileTemplateParser() {
  }

  private Template templateFromFile(File templateFile) {
    try {
      return parseTemplate(ServerIOUtils.readFileIntoStringUTF8(templateFile));
    }
    catch (IOException e) {
      throw new IllegalArgumentException("Error parsing parsing template file " + templateFile.getPath(), e);
    }
  }

  @Override
  public Template parseTemplate(String templateString) {
    return SimpleTemplateParser.parseDefault(templateString);
  }

  public final Template getTemplate(String resourceName) {
    return cache.get(ServerIOUtils.resourceNameToFile(resourceName));
  }

  public final Template getTemplate(File file) {
    return cache.get(file);
  }

}