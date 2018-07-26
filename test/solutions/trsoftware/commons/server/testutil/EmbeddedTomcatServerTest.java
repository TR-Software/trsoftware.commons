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

package solutions.trsoftware.commons.server.testutil;

import org.apache.catalina.Context;
import solutions.trsoftware.commons.client.util.WebUtils;
import solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServlet;
import solutions.trsoftware.commons.server.servlet.testutil.HelloServlet;
import solutions.trsoftware.commons.server.servlet.testutil.LiveServletTestCase;
import solutions.trsoftware.commons.shared.annotations.Slow;

/**
 * @author Alex
 * @since 7/24/2018
 */
@Slow
public class EmbeddedTomcatServerTest extends LiveServletTestCase {

  private EmbeddedTomcatServer embeddedTomcat;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    embeddedTomcat = new EmbeddedTomcatServer();
  }

  @Override
  protected void tearDown() throws Exception {
    if (embeddedTomcat != null) {
      embeddedTomcat.stop();
      embeddedTomcat = null;
    }
    super.tearDown();
  }

  public void testAddContext() throws Exception {
    validateContext(embeddedTomcat.addContext("/"), "/");
    validateContext(embeddedTomcat.addContext("/foo"), "/foo");
  }

  private void validateContext(Context context, String expectedPath) {
    validateContext(context, expectedPath, null);
  }

  private void validateContext(Context context, String expectedPath, String expectedDocBase) {
    if (expectedPath.equals("/"))
      assertEquals("", context.getPath());
    else
      assertEquals(expectedPath, context.getPath());
    assertEquals(expectedDocBase, context.getDocBase());
    // make sure the context can be looked up by its path
    assertSame(context, embeddedTomcat.getContext(expectedPath));
  }

  public void testGetContext() throws Exception {
    Context fooContext = embeddedTomcat.getContext("/foo");
    assertNull(fooContext);
    fooContext = embeddedTomcat.getContext("/foo", false);
    assertNull(fooContext);
    // now try the overloaded version that automatically creates the context if it doesn't already exist
    fooContext = embeddedTomcat.getContext("/foo", true);
    assertNotNull(fooContext);
    validateContext(fooContext, "/foo");
  }

  public void testAddServlet() throws Exception {
    embeddedTomcat.start();
    {
      EmbeddedTomcatServer.ServletHandle dummyServletHandle = embeddedTomcat.addServlet(
              new DummyHttpServlet(), "/dummyServlet");
      assertEquals(DummyHttpServlet.class.getName(), dummyServletHandle.getServletRegistration().getName());
      validateContext(dummyServletHandle.getContext(), "/");
      testDummyServlet(dummyServletHandle.getUrlBuilder().toString());

      EmbeddedTomcatServer.ServletHandle helloServletHandle = embeddedTomcat.addServlet(
          "HelloServlet", HelloServlet.class.getName(), new String[]{"/helloServlet"});
      assertEquals("HelloServlet", helloServletHandle.getServletRegistration().getName());
      assertSame(dummyServletHandle.getContext(), helloServletHandle.getContext());
      testHelloServlet(helloServletHandle.getUrlBuilder().toString());
    }
    // now add the same servlets to a different webapp (contextPath /foo)
    {
      Context fooContext = embeddedTomcat.addContext("/foo");
      validateContext(fooContext, "/foo");
      EmbeddedTomcatServer.ServletHandle dummyServletHandle = embeddedTomcat.addServlet(
          fooContext, "DummyServlet", new DummyHttpServlet(), "/dummyServlet");
      assertEquals("DummyServlet", dummyServletHandle.getServletRegistration().getName());
      testDummyServlet(dummyServletHandle.getUrlBuilder().toString());

      EmbeddedTomcatServer.ServletHandle helloServletHandle = embeddedTomcat.addServlet(
          fooContext, "HelloServlet", new HelloServlet(), "/helloServlet");
      assertEquals("HelloServlet", helloServletHandle.getServletRegistration().getName());
      assertSame(dummyServletHandle.getContext(), helloServletHandle.getContext());
      // now see if we're able to give this servlet a new init-param value
      helloServletHandle.getServletRegistration().setInitParameter("nameParam", "xyz");
      String helloServletURL = helloServletHandle.getUrlBuilder().toString();
      // the name should now be specified as "xyz" (since we changed the init-param), ...
      assertEquals("Hello Foo", doGet(WebUtils.urlWithQueryString(helloServletURL, "xyz", "Foo")));  // some parameters
      assertEquals("Hello Foo", doPost(helloServletURL, "xyz", "Foo"));  // same parameters
      // ... and the original parameter "name" should no longer work
      assertEquals("Hello GET", doGet(WebUtils.urlWithQueryString(helloServletURL, "name", "Foo")));  // some parameters
      assertEquals("Hello POST", doPost(helloServletURL, "name", "Foo"));  // same parameters
    }

  }

  public void testGetUrlBuilder() throws Exception {
    String expectedBaseUrl = "http://localhost:" + embeddedTomcat.getPortNumber();
    assertEquals(expectedBaseUrl, embeddedTomcat.getUrlBuilder().toString());
    assertEquals(expectedBaseUrl, embeddedTomcat.getUrlBuilder(embeddedTomcat.addContext("/")).toString());
    assertEquals(expectedBaseUrl + "/foo", embeddedTomcat.getUrlBuilder("/foo").toString());
    assertEquals(expectedBaseUrl + "/foo", embeddedTomcat.getUrlBuilder(embeddedTomcat.addContext("/foo")).toString());
  }


}