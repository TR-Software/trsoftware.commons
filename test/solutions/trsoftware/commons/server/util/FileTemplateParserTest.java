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

package solutions.trsoftware.commons.server.util;

import junit.framework.TestCase;
import solutions.trsoftware.commons.server.io.ResourceLocator;
import solutions.trsoftware.commons.shared.util.template.Template;

import java.io.File;

import static solutions.trsoftware.commons.shared.util.MapUtils.hashMap;

public class FileTemplateParserTest extends TestCase {

  /** Checks variable substitution in a template referenced by either resource name or {@link File} object */
  public void testGetTemplate() throws Exception {
    FileTemplateParser instance = FileTemplateParser.getInstance();
    ResourceLocator templateResource = new ResourceLocator("FileTemplateParserTest_template.txt", getClass());
    {
      Template t = instance.getTemplate(templateResource);
      assertNotNull(t);
      // make sure the parsed template files are cached
      assertSame(t, instance.getTemplate(templateResource));
      renderTemplate(t, "resource " + templateResource);

      String templateResourceName = templateResource.getCanonicalName();
      // if we use the resource FQN instead of the ResourceLocator, should return the same cached result
      assertSame(t, instance.getTemplate(templateResourceName));
      renderTemplate(t, "resource " + templateResourceName);
    }
    File templateFile = templateResource.toFile();
    {
      Template t = instance.getTemplate(templateFile);
      assertNotNull(t);
      // make sure the parsed template files are cached
      assertSame(t, instance.getTemplate(templateFile));
      renderTemplate(t, "file " + templateFile);
    }
  }

  private void renderTemplate(Template t, String dataSourceName) {
    String name = "Foo";
    int number = 1234;
    String result = t.render(hashMap("NAME", name, "ACCT_NUM", number));
    // apply and print the templates
    System.out.println();
    System.out.println("Rendering template from " + dataSourceName + ":");
    System.out.println(result);
    assertEquals(
        String.format("Hello %s,%nYour account number is %d.%nTake care!", name, number),
        result.trim());
  }

}