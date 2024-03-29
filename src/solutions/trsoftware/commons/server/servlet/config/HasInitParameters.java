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

package solutions.trsoftware.commons.server.servlet.config;

import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;

/**
 * Adapter for all webapp configuration objects defined in a deployment descriptor (e.g. {@code web.xml})
 * that provide the methods {@link #getInitParameter(String)} and {@link #getInitParameterNames()}.
 *
 * Since {@link FilterConfig}, {@link ServletContext}, and {@link ServletConfig} (defined by the <i>Java Servlet API</i>)
 * don't share a common super-interface, we need wrappers that extend {@link HasInitParameters} so that
 * {@link WebConfigParser} is able to parse init-parameters from any such config object.
 *
 * <p>
 *   The init parameters are typically specified in {@code web.xml} using {@code param-name} and {@code param-value}
 *   tags under the following XML elements:
 *   <ol>
 *     <li>{@code init-param} element for a {@link FilterConfig} or {@link ServletConfig}</li>
 *     <li>{@code context-param} element for a {@link ServletContext}</li>
 *   </ol>
 * </p>
 *
 * @param <T> the type of the source where the init-params come from: typically either
 *            {@link FilterConfig}, {@link ServletConfig}, or {@link ServletContext}
 *
 *
 * @see FilterConfigWrapper
 * @see ServletConfigWrapper
 * @see ServletContextWrapper
 * @author Alex
 * @since 3/5/2018
 */
public interface HasInitParameters<T> {

  /**
   * Returns a <code>String</code> containing the value of the named
   * initialization parameter, or <code>null</code> if the parameter does not
   * exist.
   *
   * @param name
   *            <code>String</code> specifying the name of the initialization
   *            parameter
   *
   * @return <code>String</code> containing the value of the initialization
   *         parameter
   */
  String getInitParameter(String name);

  /**
   * Returns the names of the filter's initialization parameters as an
   * <code>Enumeration</code> of <code>String</code> objects, or an empty
   * <code>Enumeration</code> if the filter has no initialization parameters.
   *
   * @return <code>Enumeration</code> of <code>String</code> objects
   *         containing the names of the filter's initialization parameters
   */
  Enumeration<String> getInitParameterNames();

  /**
   * @return the underlying object where the init-params come from: typically either a
   *         {@link FilterConfig}, {@link ServletConfig}, or {@link ServletContext}
   */
  T getSource();

  /**
   * Returns a reference to the {@link ServletContext} of the underlying object.
   *
   * @return a {@link ServletContext} object, used by the caller to interact
   *         with its servlet container
   * @see #getSource()
   */
  ServletContext getServletContext();
}
