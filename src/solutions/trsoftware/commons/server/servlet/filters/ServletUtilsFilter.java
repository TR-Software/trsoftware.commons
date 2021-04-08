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

import solutions.trsoftware.commons.server.servlet.RequestCopy;
import solutions.trsoftware.commons.server.servlet.ServletUtils;
import solutions.trsoftware.commons.server.servlet.listeners.CommonsRequestListener;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Saves a copy of the current request by calling {@link ServletUtils#setThreadLocalRequestCopy(RequestCopy)}
 * <p>
 * NOTE: this code is duplicated in {@link CommonsRequestListener}: so use one or the other, but not both!
 *
 * @author Alex
 * @since 11/14/2017
 */
public class ServletUtilsFilter extends HttpFilterAdapter {

  @Override
  public void doHttpFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
    // CAUTION: don't try to save the original request in a ThreadLocal - it's highly unsafe because Tomcat will reuse the underlying object after the response has been committed
    ServletUtils.setThreadLocalRequestCopy(new RequestCopy(request));
    filterChain.doFilter(request, response);
    ServletUtils.setThreadLocalRequestCopy(null);  // clear the thread-local after the request
  }
}
