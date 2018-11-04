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

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import solutions.trsoftware.commons.client.util.WebUtils;
import solutions.trsoftware.commons.server.SuperTestCase;
import solutions.trsoftware.commons.shared.util.collections.DefaultArrayListMap;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Facilitates testing servlets that are running in a container.
 *
 * @see solutions.trsoftware.commons.server.testutil.EmbeddedTomcatServer
 * @see solutions.trsoftware.commons.server.testutil.EmbeddedJettyServer
 *
 * @author Alex
 * @since 7/24/2018
 */
public abstract class LiveServletTestCase extends SuperTestCase {

  protected WebClient webClient;
  protected Gson gson;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gson = new Gson();
    webClient = new WebClient();
  }

  protected void tearDown() throws Exception {
    gson = null;
    if (webClient != null) {
      webClient.close();
      webClient = null;
    }
    super.tearDown();
  }

  protected String doGet(String url) throws Exception {
    return printResponse(webClient.getPage(url));
  }

  protected String doPost(String url, String... params) throws Exception {
    ArrayList<NameValuePair> paramPairs = new ArrayList<>();
    assertTrue("Uneven number of params", params.length % 2 == 0);
    for (int i = 0; i < params.length; ) {
      paramPairs.add(new NameValuePair(params[i++], params[i++]));
    }
    WebRequest request = new WebRequest(new URL(url), HttpMethod.POST);
    request.setRequestParameters(paramPairs);
    return printResponse(webClient.getPage(request));
  }

  /**
   * Prints info about the {@link WebResponse} and {@link WebRequest} that resulted in the given page
   * @return the response content as a string
   */
  public static String printResponse(Page page) {
    WebResponse response = page.getWebResponse();
    WebRequest request = response.getWebRequest();
    int respCode = response.getStatusCode();
    assertEquals(200, respCode);
    String respContent = response.getContentAsString();
    System.out.printf("[Response from %s %s]: (%d, %s)%n", request.getHttpMethod(), request.getUrl(), respCode, respContent);
    return respContent;
  }

  /**
   * Verifies that an instance of {@link DummyHttpServlet} is running at the given URL, and tests its responses
   * with various parameter combinations
   * 
   * @param dummyServletURL a URL serviced by an instance of {@link DummyHttpServlet}
   */
  protected void testDummyServlet(String dummyServletURL) throws Exception {
    String[] params = {
        "foo", "a",
        "x", "bar",
        "x", "y"
    };
    // 1) test some GET requests
    assertEquals("{}", doGet(dummyServletURL));  // no parameters
    assertParsedResponseEquals(params, doGet(WebUtils.urlWithQueryString(dummyServletURL, params)));  // some parameters
    // 2) test some POST requests
    assertEquals("{}", doPost(dummyServletURL));  // no parameters
    assertParsedResponseEquals(params, doPost(dummyServletURL, params));  // some parameters
  }

  /**
   * Verifies that an instance of {@link HelloServlet} is running at the given URL, and tests its responses
   * with various parameter combinations
   * 
   * @param helloServletURL a URL serviced by an instance of {@link HelloServlet}
   */
  protected void testHelloServlet(String helloServletURL) throws Exception {
    String[] params = {"name", "Foo"};
    assertEquals("Hello GET", doGet(helloServletURL));  // no parameters
    assertEquals("Hello POST", doPost(helloServletURL));  // no parameters
    assertEquals("Hello Foo", doGet(WebUtils.urlWithQueryString(helloServletURL, params)));  // some parameters
    assertEquals("Hello Foo", doPost(helloServletURL, params));  // some parameters
  }

  private void assertParsedResponseEquals(String[] params, String responseBody) {
    Type type = new TypeToken<Map<String, List<String>>>(){}.getType();
    Map<String, List<String>> parsedResponse = gson.fromJson(responseBody, type);
    Map<String, List<String>> expectedParsedResponse = new DefaultArrayListMap<>();
    assertTrue("Uneven number of params", params.length % 2 == 0);
    for (int i = 0; i < params.length; ) {
      expectedParsedResponse.get(params[i++]).add(params[i++]);
    }
    assertEquals(expectedParsedResponse, parsedResponse);
  }
}
