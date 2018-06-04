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

package solutions.trsoftware.commons.shared.util;

import junit.framework.TestCase;

public class HtmlBuilderTest extends TestCase {


  public void testHtmlBuilder() throws Exception {
    HtmlBuilder builder = new HtmlBuilder()
        .openTag("a").attr("href", "http://example.com").attr("title", "Example link")
          .openTag("img").attr("src", "foo.jpg").closeTag()
          .innerHtml("Test link")
        .closeTag();
    assertEquals("<a href=\"http://example.com\" title=\"Example link\"><img src=\"foo.jpg\"/>Test link</a>",
        builder.toString());
  }

  public void testFlatStructure() throws Exception {
    // test the builder with no nesting of elements
    HtmlBuilder builder = new HtmlBuilder()
        .openTag("a").attr("href", "http://example.com").attr("title", "Example link").innerHtml("link 1").closeTag()
        .openTag("a").attr("href", "http://example2.com").innerHtml("link 2").closeTag();
    assertEquals(
        "<a href=\"http://example.com\" title=\"Example link\">link 1</a><a href=\"http://example2.com\">link 2</a>",
        builder.toString());
  }
}