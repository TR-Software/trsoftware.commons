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

package solutions.trsoftware.commons.client.util;

import solutions.trsoftware.commons.client.CommonsGwtTestCase;
import solutions.trsoftware.commons.client.bridge.util.URIComponentEncoder;
import solutions.trsoftware.commons.shared.testutil.AssertUtils;

import static solutions.trsoftware.commons.client.util.WebUtils.*;
import static solutions.trsoftware.commons.shared.util.MapUtils.stringLinkedHashMap;

/**
 *
 * Date: Oct 20, 2008
 * Time: 8:17:16 PM
 * @author Alex
 */
public class WebUtilsGwtTest extends CommonsGwtTestCase {

//  @Override
//  protected void gwtSetUp() throws Exception {
//    super.gwtSetUp();
//    String hostPageBaseURL = GWT.getHostPageBaseURL();
//    System.out.println("hostPageBaseURL = " + hostPageBaseURL);
//  }

  public void testUrlQueryString() throws Exception {
    assertEquals("foo=1&bar=baz", urlQueryString(
        stringLinkedHashMap("foo", "1", "bar", "baz")));
  }

  public void testUrlQueryStringEncodeParameters() throws Exception {
    String result = urlQueryStringEncode(stringLinkedHashMap(
        "size", "3/4", "phone#=", "617 123 4567"));
    // the GWT version will encode space as %20, while the Java version will encode it as "+"
    // both forms are valid (see http://en.wikipedia.org/wiki/Percent-encoding)
    String expectedResult;
    if ("+".equals(URIComponentEncoder.getInstance().encode(" ")))
      expectedResult = "size=3%2F4&phone%23%3D=617+123+4567";
    else
      expectedResult = "size=3%2F4&phone%23%3D=617%20123%204567";
    assertEquals(expectedResult, result);
    // test the overloaded version of the method (which takes varargs instead of a hash map)
    assertEquals(result, urlQueryStringEncode("size", "3/4", "phone#=", "617 123 4567"));
  }

  public void testUrlWithQueryString() throws Exception {
    assertEquals("http://example.com?foo=1&bar=baz", urlWithQueryString("http://example.com", "foo", "1", "bar", "baz"));
    // Exception should be thrown for uneven number of arguments
    AssertUtils.assertThrows(IllegalArgumentException.class, new Runnable() {
      public void run() {
        urlWithQueryString("http://example.com", "foo", "1", "bar");
      }
    });
  }

}