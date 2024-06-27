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

package solutions.trsoftware.commons.server.servlet.filters;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Casts the request and response from {@link ServletRequest} and {@link ServletResponse} to {@link HttpServletRequest}
 * and {@link HttpServletResponse}, and delegates to {@link #doHttpFilter(HttpServletRequest, HttpServletResponse, FilterChain)}
 * with the cast objects as args.
 *
 * If un-castable, simply passes the request up the filter chain.
 *
 * @since Mar 27, 2010
 *
 * @author Alex
 */
public abstract class HttpFilterAdapter extends AbstractFilter {

  public final void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    if (servletRequest instanceof HttpServletRequest && servletResponse instanceof HttpServletResponse)
      doHttpFilter(((HttpServletRequest)servletRequest), ((HttpServletResponse)servletResponse), filterChain);
    else
      filterChain.doFilter(servletRequest, servletResponse);
  }

  /**
   * Called from {@link #doFilter(ServletRequest, ServletResponse, FilterChain)} with request and response cast
   * to their {@code Http*} subtypes.
   * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
   */
  public abstract void doHttpFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException;

  /* TODO(5/17/2024): it's easy for a new subclass implementing doHttpFilter to forget to invoke filterChain.doFilter
       in order to allow the request processing to continue.
       Maybe change our abstract doHttpFilter method signature to return a boolean, and invoke filterChain
       automatically if it returns true.  Requiring a return value would make developer realize they need to make this decision.
   */
}
