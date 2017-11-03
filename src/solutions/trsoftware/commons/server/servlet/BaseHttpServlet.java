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

package solutions.trsoftware.commons.server.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static solutions.trsoftware.commons.client.util.StringUtils.isBlank;

/**
 * Provides basic utility methods that can be helpful when writing a new {@link HttpServlet}
 *
 * @author Alex, 10/31/2017
 */
public abstract class BaseHttpServlet extends HttpServlet {

  // TODO: extract this method to a base class
  protected static String getRequiredParameter(HttpServletRequest request, String paramName) throws IOException, RequestException {
    String value = request.getParameter(paramName);
    if (isBlank(value)) {
      throw new RequestException(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter " + paramName);  // Bad Request (missing required parameters)
    }
    return value;
  }

  /**
   * This override simply escalates the method visibility from {@code protected} to {@code public}, to facilitate
   * unit testing outside of a servlet container.
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    super.doGet(req, resp);
  }

  /**
   * This override simply escalates the method visibility from {@code protected} to {@code public}, to facilitate
   * unit testing outside of a servlet container.
   */
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    super.doPost(req, resp);
  }
}
