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

package solutions.trsoftware.commons.server.servlet.listeners;

import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Alex
 * @since 7/25/2018
 */
public abstract class HttpServletRequestListener implements ServletRequestListener {

  @Override
  public final void requestDestroyed(ServletRequestEvent sre) {
    ServletRequest servletRequest = sre.getServletRequest();
    if (servletRequest instanceof HttpServletRequest)
      onHttpRequestDestroyed((HttpServletRequest)servletRequest, sre);
  }

  @Override
  public final void requestInitialized(ServletRequestEvent sre) {
    ServletRequest servletRequest = sre.getServletRequest();
    if (servletRequest instanceof HttpServletRequest)
      onHttpRequestInitialized((HttpServletRequest)servletRequest, sre);
  }

  /**
   * Convenience method to avoid having to cast {@link ServletRequestEvent#getServletRequest()}
   * to {@link HttpServletRequest}.
   * <p>
   * Will be called from {@link #requestDestroyed(ServletRequestEvent)} iff
   * the {@link ServletRequestEvent} contains an instance of {@link HttpServletRequest}.
   *
   * @param request the request from the event
   * @param sre the underlying event
   */
  protected abstract void onHttpRequestDestroyed(HttpServletRequest request, ServletRequestEvent sre);

  /**
   * Convenience method to avoid having to cast {@link ServletRequestEvent#getServletRequest()} 
   * to {@link HttpServletRequest}.
   * <p>
   * Will be called from {@link #requestInitialized(ServletRequestEvent)} iff
   * the {@link ServletRequestEvent} contains an instance of {@link HttpServletRequest}.
   *
   * @param request the request from the event
   * @param sre the underlying event
   */
  protected abstract void onHttpRequestInitialized(HttpServletRequest request, ServletRequestEvent sre);

}
