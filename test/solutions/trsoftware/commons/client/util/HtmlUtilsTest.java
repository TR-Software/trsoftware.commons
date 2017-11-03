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

package solutions.trsoftware.commons.client.util;

import junit.framework.TestCase;

public class HtmlUtilsTest extends TestCase {

  public void testSpan() {
    assertEquals("<span id=\"foo\" class=\"bar\"></span>", HtmlUtils.span("foo", "bar", ""));
    assertEquals("<span class=\"bar\"></span>", HtmlUtils.span(null, "bar", null));
    assertEquals("<span></span>", HtmlUtils.span(null, null, null));
  }

  public void testDiv() {
    assertEquals("<div id=\"foo\" class=\"bar\"></div>", HtmlUtils.div("foo", "bar", ""));
    assertEquals("<div class=\"bar\"></div>", HtmlUtils.div(null, "bar", null));
    assertEquals("<div></div>", HtmlUtils.div(null, null, null));
  }

  public void testArbitraryElement() {
    assertEquals("<hr id=\"foo\" class=\"bar\"></hr>", HtmlUtils.element("hr", "foo", "bar", ""));
    assertEquals("<hr class=\"bar\"></hr>", HtmlUtils.element("hr", null, "bar", null));
    assertEquals("<hr></hr>", HtmlUtils.element("hr", null, null, null));
  }
}