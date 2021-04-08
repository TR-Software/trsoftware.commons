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

package solutions.trsoftware.commons.server.servlet.listeners;

import solutions.trsoftware.commons.server.servlet.RequestCopy;
import solutions.trsoftware.commons.server.servlet.ServletUtils;
import solutions.trsoftware.commons.server.servlet.filters.ServletUtilsFilter;

import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServletRequest;

/**
 * Performs the same function as {@link ServletUtilsFilter}:
 * <ol>
 *   <li><i>request initialized</i>: sets {@link ServletUtils#threadLocalRequestCopy}</li>
 *   <li><i>request destroyed</i>: nulls out {@link ServletUtils#threadLocalRequestCopy}</li>
 * </ol>
 * NOTE: this code is duplicated in {@link ServletUtilsFilter}: so use one or the other, but not both!
 * @author Alex
 * @since 7/25/2018
 */
public class CommonsRequestListener extends HttpServletRequestListener {

  @Override
  protected void onHttpRequestInitialized(HttpServletRequest request, ServletRequestEvent sre) {
    // CAUTION: don't try to save the original request in a ThreadLocal - it's highly unsafe because Tomcat will reuse the underlying object after the response has been committed
    ServletUtils.setThreadLocalRequestCopy(new RequestCopy(request));
  }

  @Override
  protected void onHttpRequestDestroyed(HttpServletRequest request, ServletRequestEvent sre) {
    ServletUtils.setThreadLocalRequestCopy(null);
  }

}
