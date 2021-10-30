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

package solutions.trsoftware.commons.server.util;

import solutions.trsoftware.commons.server.io.DataResource;
import solutions.trsoftware.commons.server.io.ResourceLocator;
import solutions.trsoftware.commons.server.io.ServerIOUtils;
import solutions.trsoftware.commons.shared.util.collections.DefaultMap;
import solutions.trsoftware.commons.shared.util.template.SimpleTemplateParser;
import solutions.trsoftware.commons.shared.util.template.Template;
import solutions.trsoftware.commons.shared.util.template.TemplateParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Provides a lightweight file-based templating facility.
 * <p>
 * The default instance (obtained by {@link #getInstance()}) will parse files according to the
 * {@linkplain SimpleTemplateParser#DEFAULT_SYNTAX default syntax}.  A custom syntax can be specified
 * by passing a custom {@link TemplateParser} instance to {@link #getInstance(TemplateParser)}.
 * <p>
 * The template source file can be specified either directly (see {@link #getTemplate(File)} and {@link #getTemplate(Path)}),
 * or as a classpath resource (see {@link #getTemplate(String)} and {@link #getTemplate(ResourceLocator)}).
 * It's also possible to use a custom data source by passing a custom {@link DataResource} implementation
 * to {@link #getTemplate(DataResource)}.
 *
 * @author Alex
 */
public final class FileTemplateParser implements TemplateParser {
  // TODO(10/27/2021): rename to TemplateFileLoader or TemplateFileParser (in a separate commit)

  /**
   * Caches parsed templates
   */
  private static final DefaultMap<TemplateParser, FileTemplateParser> instances = new DefaultMap<TemplateParser, FileTemplateParser>() {
    @Override
    public FileTemplateParser computeDefault(TemplateParser templateSyntax) {
      return new FileTemplateParser(templateSyntax);
    }
  };

  public static FileTemplateParser getInstance() {
    return instances.get(SimpleTemplateParser.DEFAULT_SYNTAX);
  }

  public static FileTemplateParser getInstance(TemplateParser templateSyntax) {
    return instances.get(templateSyntax);
  }

  private final TemplateParser templateSyntax;

  /** This class should not be instantiated directly; use the {@link #getInstance()} method instead */
  private FileTemplateParser() {
    templateSyntax = SimpleTemplateParser.DEFAULT_SYNTAX;
  }

  private FileTemplateParser(TemplateParser templateSyntax) {
    this.templateSyntax = templateSyntax;
  }

  private Template parseTemplate(DataResource templateData) {
    try {
      return parseTemplate(ServerIOUtils.readCharactersIntoString(templateData.getInputStream()));
    }
    catch (IOException e) {
      throw new IllegalArgumentException("Error reading template from " + templateData, e);
    }
  }

  @Override
  public Template parseTemplate(String templateString) {
    return templateSyntax.parseTemplate(templateString);
  }

  /**
   * Loads a template from the given data source.
   *
   * @param resource the template resource
   * @return the template compiled from the given data source
   */
  public Template getTemplate(DataResource resource) {
    return parseTemplate(resource);
  }

  /**
   * Loads a template from the classpath resource specified by the given {@link ResourceLocator}.
   *
   * @param resourceLocator the template resource
   * @return the template compiled from the given resource file
   */
  public final Template getTemplate(ResourceLocator resourceLocator) {
    return getTemplate(new DataResource.JavaResource(resourceLocator));
  }

  /**
   * Loads a template from the classpath resource specified by the given string.
   *
   * @param resourceName a string suitable for {@link ClassLoader#getResource(String)} (i.e. it should include the full
   * path relative to the root package without a leading {@code '/'} char)
   * @return the template compiled from the given resource file
   */
  public final Template getTemplate(String resourceName) {
    return getTemplate(new ResourceLocator(resourceName));
  }

  /**
   * Loads a template from the given file.
   *
   * @param file the template file
   * @return the template compiled from the given file
   */
  public final Template getTemplate(File file) {
    return getTemplate(new DataResource.FileResource(file));
  }

  /**
   * Loads a template from the given file path.
   *
   * @param path the template file path
   * @return the template compiled from the given file
   */
  public final Template getTemplate(Path path) {
    return getTemplate(new DataResource.NioFileResource(path));
  }

}