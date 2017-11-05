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

import junit.framework.TestCase;

import java.util.Arrays;

import static solutions.trsoftware.commons.shared.util.MapUtils.stringMap;

/**
 * Dec 10, 2008
 *
 * @author Alex
 */
public class TemplateTest extends TestCase {

  private final Template template = new Template(Arrays.asList(
      new VariablePart("foo"),
      new StringPart(" is a type of "),
      new VariablePart("bar"),
      new StringPart(".")
  ));

  public void testRender() throws Exception {
    assertEquals("Bonobo is a type of ape.", template.render(
        stringMap(
            "foo", "Bonobo",
            "bar", "ape")));
  }

  public void testRenderPositional() throws Exception {
    assertEquals("5 is a type of number.", template.renderPositional(5, "number"));
  }

  public void testPrintf() throws Exception {
    assertEquals("5 is a type of number.", Template.printf("%d is a type of %s.", 5, "number"));
    assertEquals("5 is a type of number", Template.printf("%foobar is a type of %bar", 5, "number"));
  }

}