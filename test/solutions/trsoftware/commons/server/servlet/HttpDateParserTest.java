/*
 * Copyright 2020 TR Software Inc.
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

package solutions.trsoftware.commons.server.servlet;

import junit.framework.TestCase;

import static solutions.trsoftware.commons.server.servlet.HttpDateParser.parseDate;

/**
 * @author Alex
 * @since 12/19/2020
 */
public class HttpDateParserTest extends TestCase {

  public void testParseDate() throws Exception {
    /*
      According to RFC 2616, HTTP servers are required to support all 3 of the following date formats
      (see https://tools.ietf.org/html/rfc2616#section-3.3)
     */
    String[] dateStrings = {
        "Sun, 06 Nov 1994 08:49:37 GMT",  // RFC 822, updated by RFC 1123
        "Sunday, 06-Nov-94 08:49:37 GMT", // RFC 850, obsoleted by RFC 1036
        "Sun Nov  6 08:49:37 1994"        // ANSI C's asctime() format
    };
    for (String dateString : dateStrings) {
      assertEquals(dateString,784111777000L, parseDate(dateString));
    }
    assertEquals(-1, parseDate("foo"));
    assertEquals(-1, parseDate(""));
    assertEquals(-1, parseDate(null));

  }
}