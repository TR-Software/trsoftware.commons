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

import javax.servlet.http.HttpServlet;

/**
 * Starts an instance of {@link EmbeddedServletContainer} on a given port with a given servlet.
 *
 * @since Mar 8, 2010
 * @author Alex
 */
public class EmbeddedServletContainerTestDelegate extends SetUpTearDownDelegate {

  private EmbeddedServletContainer server;

  public EmbeddedServletContainerTestDelegate(Class<? extends HttpServlet> servletClass, int portNumber, String uri) {
    server = new EmbeddedServletContainer(portNumber);
    server.addServlet(servletClass, uri);
  }

  @Override
  public void setUp() throws Exception {
    server.start();
  }

  @Override
  public void tearDown() throws Exception {
    server.stop();
  }
}
