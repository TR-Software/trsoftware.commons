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

import junit.framework.TestCase;
import solutions.trsoftware.commons.server.io.ResourceLocator;
import solutions.trsoftware.commons.shared.util.template.SimpleTemplateParser;
import solutions.trsoftware.commons.shared.util.template.Template;

import java.io.File;
import java.nio.file.Path;

import static solutions.trsoftware.commons.shared.util.MapUtils.hashMap;

public class FileTemplateParserTest extends TestCase {

  private FileTemplateParser defaultSyntaxInstance;
  private FileTemplateParser djangoSyntaxInstance;

  public void setUp() throws Exception {
    super.setUp();
    defaultSyntaxInstance = FileTemplateParser.getInstance();
    djangoSyntaxInstance = FileTemplateParser.getInstance(new SimpleTemplateParser("{{ ", " }}", "{# ", " #}"));
  }

  public void testGetInstance() throws Exception {
    // make sure the same instance of FileTemplateParser is returned for each equivalent instance of SimpleTemplateParser
    assertSame(defaultSyntaxInstance, FileTemplateParser.getInstance());
    assertSame(defaultSyntaxInstance, FileTemplateParser.getInstance(new SimpleTemplateParser(
        SimpleTemplateParser.DEFAULT_SYNTAX.getVarStartSyntax(),
        SimpleTemplateParser.DEFAULT_SYNTAX.getVarEndSyntax(),
        SimpleTemplateParser.DEFAULT_SYNTAX.getCommentStartSyntax(),
        SimpleTemplateParser.DEFAULT_SYNTAX.getCommentEndSyntax())));
    assertSame(djangoSyntaxInstance, FileTemplateParser.getInstance(new SimpleTemplateParser("{{ ", " }}", "{# ", " #}")));
  }

  /**
   * Checks that all versions of the {@link FileTemplateParser#getTemplate} method load the same template
   * regardless of how the source file is referenced (e.g. resource name, {@link File} object, etc.).
   * This is confirmed by asserting that the returned {@link Template} objects produce the same result when
   * rendered with the same variables.
   */
  public void testGetTemplate() throws Exception {
    FileTemplateParser loader = FileTemplateParser.getInstance();

    // the following variables are all the different ways of referencing the same template file
    ResourceLocator resourceLocator = new ResourceLocator("FileTemplateParserTest_template.txt", getClass());
    String resourceName = resourceLocator.getCanonicalName();
    File templateFile = resourceLocator.toFile();
    Path templatePath = resourceLocator.toPath();

    // calling getTemplate with each of those args should produce the same result:
    renderTemplate(loader.getTemplate(resourceLocator), String.format("ResourceLocator(%s)", resourceLocator));
    // if we use the resource FQN instead of the ResourceLocator we should get the same result
    renderTemplate(loader.getTemplate(resourceName), String.format("resource name \"%s\"", resourceName));
    // should also be the same with a File or Path object
    renderTemplate(loader.getTemplate(templateFile), String.format("File(%s)", templateFile));
    renderTemplate(loader.getTemplate(templatePath), String.format("Path(%s)", templatePath));
  }

  /**
   * Renders the given {@link Template} object to make sure it matches {@code FileTemplateParserTest_template.txt}
   */
  private void renderTemplate(Template t, String dataSourceName) {
    assertNotNull(t);
    String name = "Foo";
    int number = 1234;
    String result = t.render(hashMap("NAME", name, "ACCT_NUM", number));
    // apply and print the templates
    System.out.printf("------%nRendering template from %s:%n------%n", dataSourceName);
    System.out.println(result);
    assertEquals(
        String.format("Hello %s,%nYour account number is %d.%nTake care!", name, number),
        result.trim());
  }
}