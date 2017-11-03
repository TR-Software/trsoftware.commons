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

import solutions.trsoftware.commons.client.CommonsGwtTestCase;

import java.util.HashMap;
import java.util.Map;

/**
 * Feb 1, 2009
 *
 * @author Alex
 */
public class JavascriptUtilsTest extends CommonsGwtTestCase {

  /** Maps the URL special characters to their escape sequences */
  private static final Map<Character, String> specialChars;

  static {
    specialChars = new HashMap<Character, String>();
    specialChars.put('S', "%20");
    specialChars.put('#', "%23");
    specialChars.put('$', "%24");
    specialChars.put('%', "%25");
    specialChars.put('&', "%26");
    specialChars.put('/', "%2F");
    specialChars.put(':', "%3A");
    specialChars.put(';', "%3B");
    specialChars.put('<', "%3C");
    specialChars.put('=', "%3D");
    specialChars.put('>', "%3E");
    specialChars.put('?', "%3F");
    specialChars.put('@', "%40");
    specialChars.put('[', "%5B");
    specialChars.put('\\', "%5C");
    specialChars.put(']', "%5D");
    specialChars.put('^', "%5E");
    specialChars.put('`', "%60");
    specialChars.put('{', "%7B");
    specialChars.put('|', "%7C");
    specialChars.put('}', "%7D");
    specialChars.put('~', "%7E");
  }


  public void testDecodeURIComponent() throws Exception {
    // test a few string to make sure they're accurately translated
    assertEquals("foo", JavascriptUtils.safeDecodeURIComponent("foo"));
    assertEquals("[foo]", JavascriptUtils.safeDecodeURIComponent("%5bfoo%5D"));

    // now test a few deliberately malformed URI strings, known to throw exceptions,
    // and make sure they return the argument without performing the escaping,
    // rather than crashing
    assertEquals("% foo", JavascriptUtils.safeDecodeURIComponent("% foo"));
    assertEquals("foo %", JavascriptUtils.safeDecodeURIComponent("foo %"));
    assertEquals("foo % bar", JavascriptUtils.safeDecodeURIComponent("foo % bar"));
  }
}