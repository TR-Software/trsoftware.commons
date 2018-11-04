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
import solutions.trsoftware.commons.server.servlet.RequestCopy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * A simple servlet implementation for testing purposes.
 * Provides {@code GET} and {@code POST} methods that simply output info about the incoming request as JSON
 * (i.e. a {@link RequestCopy} object serialized with {@link Gson}).
 *
 * @author Alex
 * @since 5/25/2018
 */
public class RequestEchoServlet extends DummyHttpServlet {

  @Override
  protected void writeJsonResponse(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    PrintWriter writer = resp.getWriter();
    gson.toJson(new RequestCopy(req), writer);
    writer.flush();
  }
}
