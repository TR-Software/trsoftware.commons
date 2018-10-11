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

package solutions.trsoftware.commons.shared.util.template;

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.testutil.AssertUtils;

/**
 * Apr 13, 2010
 *
 * @author Alex
 */
public class SimpleTemplateParserTest extends TestCase {

  public void testWithCustomSyntax() throws Exception {
    assertTemplateRenderResult(
        new SimpleTemplateParser("{*", "*}", "/*", "*/"),
        "{*name*} {*lastName*} is the number {*foo*} {*bar*} in the world/* this is a template */");
  }

  public void testWithDefaultSyntax() throws Exception {
    assertTemplateRenderResult(
        SimpleTemplateParser.DEFAULT_SYNTAX,
        "${name} ${lastName} is the number ${foo} ${bar} in the world<!-- this is a template -->");
  }

  public void testWithDjangoSyntax() throws Exception {
    assertTemplateRenderResult(
        SimpleTemplateParser.DJANGO_SYNTAX,
        "{{name}} {{lastName}} is the number {{foo}} {{bar}} in the world{# this is a template#}");
  }

  private void assertTemplateRenderResult(TemplateParser parser, String templateString) {
    assertEquals(
        "Adam Lyons is the number 1 player in the world",
        parser.parseTemplate(templateString)
            .render("lastName", "Lyons", "name", "Adam", "foo", "1", "bar", "player"));
  }

  public void testEqualsAndHashCode() throws Exception {
    // two instances are equal iff they represent the same syntax
    SimpleTemplateParser a = new SimpleTemplateParser("{*", "*}", "/*", "*/");
    SimpleTemplateParser b = new SimpleTemplateParser("{*", "*}", "<!--", "-->");
    AssertUtils.assertEqualsAndHashCode(
        a,
        new SimpleTemplateParser("{*", "*}", "/*", "*/"));
    AssertUtils.assertEqualsAndHashCode(
        b,
        new SimpleTemplateParser("{*", "*}", "<!--", "-->"));
    AssertUtils.assertNotEqual(a, b);
  }
}