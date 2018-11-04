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

package solutions.trsoftware.commons.server.servlet.testutil;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import solutions.trsoftware.commons.server.SuperTestCase;
import solutions.trsoftware.commons.shared.util.MapUtils;

import java.util.Map;

import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertArraysEqual;

/**
 * @author Alex
 * @since 3/26/2018
 */
public class DummyHttpServletTest extends SuperTestCase {

  private DummyHttpServlet servlet;
  private DummyHttpServletRequest request;
  private DummyHttpServletResponse response;
  private Gson gson;

  public void setUp() throws Exception {
    super.setUp();
    servlet = new DummyHttpServlet();
    request = new DummyHttpServletRequest(MapUtils.stringMap(
        "foo", "1",
        "bar", "2"));
    response = new DummyHttpServletResponse();
    gson = new Gson();
  }

  public void tearDown() throws Exception {
    servlet = null;
    request = null;
    response = null;
    gson = null;
    super.tearDown();
  }

  public void testDoGet() throws Exception {
    servlet.doGet(request, response);
    verifyResponse();
  }

  private void verifyResponse() {
    Map<String, String[]> responseObj = gson.fromJson(response.getOutputAsString(),
        new TypeToken<Map<String, String[]>>() {}.getType());
    assertEquals(2, responseObj.size());
    assertArraysEqual(new String[] {"1"}, responseObj.get("foo"));
    assertArraysEqual(new String[] {"2"}, responseObj.get("bar"));
  }

  public void testDoPost() throws Exception {
    servlet.doPost(request, response);
    verifyResponse();
  }
}