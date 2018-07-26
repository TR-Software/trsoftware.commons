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

import com.google.gson.Gson;
import solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServlet;
import solutions.trsoftware.commons.server.servlet.testutil.HelloServlet;
import solutions.trsoftware.commons.server.servlet.testutil.LiveServletTestCase;
import solutions.trsoftware.commons.shared.annotations.Slow;

import static solutions.trsoftware.commons.server.net.NetUtils.*;

/**
 * @author Alex
 * @since 3/26/2018
 */
public class EmbeddedJettyServerTest extends LiveServletTestCase {

  private static final String DUMMY_SERVLET_URI = "/" + DummyHttpServlet.class.getSimpleName() + "/servlet";
  private static final String HELLO_SERVLET_URI = "/" + HelloServlet.class.getSimpleName() + "/servlet";

  private EmbeddedJettyServer container;
  private int port;
  private String dummyServletURL;
  private String helloServletURL;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    port = findAvailableLocalPort(MIN_USER_PORT, MIN_DYNAMIC_PORT);
    container = new EmbeddedJettyServer(port);
    dummyServletURL = "http://localhost:" + port + DUMMY_SERVLET_URI;
    helloServletURL = "http://localhost:" + port + HELLO_SERVLET_URI;
    gson = new Gson();
  }

  public void tearDown() throws Exception {
    container.stop();
    container = null;
    dummyServletURL = null;
    helloServletURL = null;
    gson = null;
    super.tearDown();
  }

  @Slow
  public void testAddServlet() throws Exception {
    container.addServlet(DummyHttpServlet.class, DUMMY_SERVLET_URI);
    container.start();
    doTestDummyServlet();
    // now test adding another servlet to the container while it's running
    container.addServlet(HelloServlet.class, HELLO_SERVLET_URI);
    doTestHelloServlet();
    // make sure the first servlet still works
    doTestDummyServlet();
  }

  private void doTestDummyServlet() throws Exception {
    testDummyServlet(dummyServletURL);
  }

  private void doTestHelloServlet() throws Exception {
    testHelloServlet(helloServletURL);
  }

}