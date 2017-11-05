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

package solutions.trsoftware.commons.shared.util;

import junit.framework.TestCase;

/**
 * @author Alex, 10/16/2017
 */
public class HtmlElementBuilderTest extends TestCase {

  public void testSelfClosingTag() throws Exception {
    assertEquals("<foo id=\"1\" class=\"bar\" />", new HtmlElementBuilder("foo")
        .setAttribute("id", "1")
        .setAttribute("class", "bar").selfClosingTag());
  }

  public void testOpenTag() throws Exception {
    assertEquals("<foo id=\"1\" class=\"bar\">", new HtmlElementBuilder("foo")
        .setAttribute("id", "1")
        .setAttribute("class", "bar").openTag());
  }

  public void testCloseTag() throws Exception {
    assertEquals("</foo>", new HtmlElementBuilder("foo")
        .setAttribute("id", "1")
        .setAttribute("class", "bar").closeTag());
  }

}