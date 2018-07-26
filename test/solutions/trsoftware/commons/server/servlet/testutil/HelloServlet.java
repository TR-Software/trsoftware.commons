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

import solutions.trsoftware.commons.server.servlet.BaseHttpServlet;
import solutions.trsoftware.commons.server.servlet.config.InitParameters;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static solutions.trsoftware.commons.shared.util.LogicUtils.firstNonNull;

/**
 * A dummy servlet that simply prints {@code "Hello {name}"}.
 * <p>
 * The name to print will be the value of the request parameter {@value #DEFAULT_NAME_PARAM};
 * if no value received for this parameter, will use the name of the HTTP method being invoked.
 * <p>
 * This servlet can also be configured to use a different request parameter name, which can be specified
 * by the {@code init-param} {@code nameParam}
 *
 * @author Alex
 * @since 7/24/2018
 */
public class HelloServlet extends BaseHttpServlet {
  private static final String DEFAULT_NAME_PARAM = "name";

  private Settings settings;

  private static class Settings implements InitParameters {
    private String nameParam = DEFAULT_NAME_PARAM;

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder("Settings{");
      sb.append("nameParam='").append(nameParam).append('\'');
      sb.append('}');
      return sb.toString();
    }
  }

  @Override
  public void init() throws ServletException {
    settings = parse(new Settings());
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    sayHello(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    sayHello(req, resp);
  }

  private void sayHello(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.getWriter().print("Hello " + firstNonNull(req.getParameter(settings.nameParam), req.getMethod()));
  }
}
