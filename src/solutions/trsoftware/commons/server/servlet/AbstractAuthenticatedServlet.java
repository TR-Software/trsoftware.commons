/*
 * Copyright 2021 TR Software Inc.
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
 */

package solutions.trsoftware.commons.server.servlet;

import solutions.trsoftware.commons.server.auth.RequestAuth;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Provides a starting point for implementing a secure web API.
 *
 * Uses an instance of {@link RequestAuth} to authenticate incoming requests.
 *
 *
 * @author Alex
 * @since Jun 25, 2012
 */
public abstract class AbstractAuthenticatedServlet extends BaseHttpServlet {

  @Override
  public final void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      // first of all, authenticate the request (to not give away any API details if the caller doesn't know the secret key)
      authenticate(request);
      doAuthenticatedGet(request, response);
    }
    catch (RequestException ex) {
      response.sendError(ex.getStatusCode(), ex.getMessage());
    }
  }

  @Override
  public final void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      // first of all, authenticate the request (to not give away any API details if the caller doesn't know the secret key)
      authenticate(request);
      doAuthenticatedPost(request, response);
    }
    catch (RequestException ex) {
      response.sendError(ex.getStatusCode(), ex.getMessage());
    }
  }

  /** Subclasses should implement GET logic in this method */
  protected void doAuthenticatedGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, RequestException {
    // subclasses should override
  }

  /** Subclasses should implement POST logic in this method */
  protected void doAuthenticatedPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, RequestException {
    // subclasses should override
  }

  /**
   * Authenticates the request and commits the appropriate error response if
   * the validation fails.
   *
   * @return true if authentication succeeds, otherwise false.
   */
  protected void authenticate(HttpServletRequest request) throws IOException, RequestException {
    try {
      getRequestSigner(request).authenticateIncomingRequest(request);
    }
    catch (SecurityException authException) {
      throw new RequestException(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
    }
  }

  /** @return the request authenticator to be used by {@link #authenticate(HttpServletRequest)} */
  protected abstract RequestAuth getRequestSigner(HttpServletRequest request);

}
