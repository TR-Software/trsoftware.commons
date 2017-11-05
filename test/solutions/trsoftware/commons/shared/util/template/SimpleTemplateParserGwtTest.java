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

import solutions.trsoftware.commons.client.CommonsGwtTestCase;
import solutions.trsoftware.commons.client.testutil.AssertUtils;
import solutions.trsoftware.commons.shared.util.MapUtils;

import java.util.Map;

/**
 * Dec 10, 2008
 *
 * @author Alex
 */
public class SimpleTemplateParserGwtTest extends CommonsGwtTestCase {

  public void testStringTemplate() throws Exception {
    assertEquals("Bonobo",
        SimpleTemplateParser.parseDefault("${foo}").render(
            map("foo", "Bonobo")));

    assertEquals(" Bonobo",
        SimpleTemplateParser.parseDefault(" ${foo}").render(
            map("foo", "Bonobo")));
    
    assertEquals(" Bonobo ",
        SimpleTemplateParser.parseDefault(" ${foo}").render(
            map("foo", "Bonobo ")));

    assertEquals("",
        SimpleTemplateParser.parseDefault("<!-- stuff -->").render(
            map("foo", "Bonobo ")));

    assertEquals("",
        SimpleTemplateParser.parseDefault("<!-- \nstuff \n-->").render(
            map("foo", "Bonobo ")));

    assertEquals("  ",
        SimpleTemplateParser.parseDefault(" <!-- stuff --> ").render(
            map("foo", "Bonobo ")));


    assertEquals("asdf rwer Bonobo def chimp",
        SimpleTemplateParser.parseDefault("asdf rwer ${foo} def ${bar}").render(
            map("foo", "Bonobo",
                "bar", "chimp")));

    // check multiline templates
    assertEquals("asdf \nrwer Bonobo\n def chimp",
        SimpleTemplateParser.parseDefault("asdf \nrwer ${foo}\n def ${bar}").render(
            map("foo", "Bonobo",
                "bar", "chimp")));

    // check multiline templates with comments
    assertEquals("asdf chimp",
        SimpleTemplateParser.parseDefault("asdf <!--\nrwer ${foo}\n def -->${bar}").render(
            map("foo", "Bonobo",
                "bar", "chimp")));

    // missing substitutions should result in an empty string inserted
    assertEquals("asdf rwer  def chimp",
        SimpleTemplateParser.parseDefault("asdf rwer ${foo} def ${bar}").render(
            map("bar", "chimp")));

    // make sure variables can't span multiple lines
    AssertUtils.assertThrows(IllegalArgumentException.class,
        new Runnable() {
          public void run() {
            SimpleTemplateParser.parseDefault("asdf rwer ${f\noo} def ${bar}");
          }
        });

    // make sure comments and variables hve to be terminated
    AssertUtils.assertThrows(IllegalArgumentException.class,
        new Runnable() {
          public void run() {
            SimpleTemplateParser.parseDefault("asdf rwer ${f\noo} def ${bar");
          }
        });
    AssertUtils.assertThrows(IllegalArgumentException.class,
        new Runnable() {
          public void run() {
            SimpleTemplateParser.parseDefault("asdf rwer ${f\noo} def <!--bar}");
          }
        });
  }

  /** Creates a map given the args in order key1, value2, key2, value2, ... */
  private static Map<String, String> map(String... keyValuePairs) {
    return MapUtils.stringMap(keyValuePairs);
  }
}