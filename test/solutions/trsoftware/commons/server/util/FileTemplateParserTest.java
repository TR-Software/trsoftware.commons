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

import solutions.trsoftware.commons.server.testutil.TempFileTestCase;
import solutions.trsoftware.commons.shared.util.template.Template;

import java.io.FileOutputStream;
import java.io.PrintStream;

import static solutions.trsoftware.commons.shared.util.MapUtils.hashMap;

public class FileTemplateParserTest extends TempFileTestCase {

  private String templateString = "<!-- Example template -->\nHello ${NAME}, \nYour account number is ${ACCT_NUM}.\nTake care!";

  @Override
  public void setUp() throws Exception {
    super.setUp();
    PrintStream out = new PrintStream(new FileOutputStream(tempFile));
    out.print(templateString);
    out.close();
    System.out.println("Wrote the following template to temp file @ " + tempFile + ":");
    System.out.println(templateString);
  }

  /** Checks variable substitution in a template file located in the project {@code resources} directory */
  public void testFileTemplate() throws Exception {
    FileTemplateParser instance = FileTemplateParser.getInstance();
    Template t = instance.getTemplate(tempFile);
    assertNotNull(t);
    // make sure the parsed template files are cached
    assertSame(t, instance.getTemplate(tempFile));
    // apply and print the templates
    System.out.println();
    System.out.println("Rendering template file " + tempFile + ":");
    String name = "Foo";
    int number = 1234;
    String result = t.render(hashMap("NAME", name, "ACCT_NUM", number));
    System.out.println(result);
    assertEquals("Hello " + name + ", \nYour account number is " + number + ".\nTake care!",
        result.trim());
  }

}