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

package solutions.trsoftware.commons.server.servlet.config;

import javax.servlet.FilterConfig;
import java.util.Enumeration;

/**
 * Adapts a {@link FilterConfig} so that in can be consumed by {@link WebConfigParser}.
 * @see HasInitParameters
 *
 * @author Alex
 * @since 3/5/2018
 */
public class FilterConfigWrapper implements HasInitParameters {

  private FilterConfig filterConfig;

  public FilterConfigWrapper(FilterConfig filterConfig) {
    this.filterConfig = filterConfig;
  }

  public FilterConfig getFilterConfig() {
    return filterConfig;
  }

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
  @Override
  public String getInitParameter(String name) {
    return filterConfig.getInitParameter(name);
  }

  /**
   * Returns the names of the filter's initialization parameters as an
   * <code>Enumeration</code> of <code>String</code> objects, or an empty
   * <code>Enumeration</code> if the filter has no initialization parameters.
   *
   * @return <code>Enumeration</code> of <code>String</code> objects
   *         containing the names of the filter's initialization parameters
   */
  @Override
  public Enumeration<String> getInitParameterNames() {
    return filterConfig.getInitParameterNames();
  }
}
