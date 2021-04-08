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

import solutions.trsoftware.commons.server.servlet.config.InitParameters;
import solutions.trsoftware.commons.server.servlet.config.WebConfigException;
import solutions.trsoftware.commons.server.servlet.config.WebConfigParser;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * Provides empty default implementations of {@link Filter#init(FilterConfig)} and {@link Filter#destroy()}}
 *  
 * @since Jul 29, 2009
 * @author Alex
 */
public abstract class AbstractFilter implements Filter {

  protected FilterConfig filterConfig;

  public void init(FilterConfig filterConfig) throws ServletException {
    // subclasses may override
    this.filterConfig = filterConfig;
  }

  public void destroy() {
    // subclasses may override
  }

  /**
   * Sets the fields of the given {@link InitParameters} object from the {@code init-param} values present in the {@link
   * FilterConfig}.
   *
   * @param parameters the object to be populated via {@link WebConfigParser#parse(FilterConfig, InitParameters)}
   * @return the same instance that was passed as the argument, after its fields have been populated.
   * @see WebConfigParser#parse(FilterConfig, InitParameters)
   */
  protected <P extends InitParameters> P parseInitParams(P parameters) throws ServletException {
    Class<? extends Filter> filterClass = getClass();
    FilterConfig filterConfig = this.filterConfig;
    return parseInitParams(parameters, filterConfig, filterClass);
  }

  /**
   * Sets the fields of the given {@link InitParameters} object from the {@code init-param} values present in the given
   * {@link FilterConfig}.
   *
   * @param parameters the object to be populated via {@link WebConfigParser#parse(FilterConfig, InitParameters)}
   * @param filterConfig contains the {@code init-param} values
   * @param filterClass the filter's class: will use its name for logging messages
   * @return the same instance that was passed as the argument, after its fields have been populated.
   * @see WebConfigParser#parse(FilterConfig, InitParameters)
   */
  public static <P extends InitParameters> P parseInitParams(P parameters, FilterConfig filterConfig, Class<? extends Filter> filterClass) throws ServletException {
    try {
      P parsedParams = WebConfigParser.parse(filterConfig, parameters);
      filterConfig.getServletContext().log(
          String.format("Filter config (filter-name: \"%s\", filter-class: %s) parsed from init-param values: %s",
              filterConfig.getFilterName(), filterClass.getSimpleName(), parsedParams));
      return parsedParams;
    }
    catch (WebConfigException e) {
      throw new ServletException(String.format("Invalid configuration for filter '%s' (%s): %s",
          filterConfig.getFilterName(), filterClass, e.getMessage()),
          e);
    }
  }

  protected ServletContext getServletContext() {
    return filterConfig.getServletContext();
  }
}
