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

package solutions.trsoftware.commons.server.servlet.filters;

import solutions.trsoftware.commons.server.servlet.filters.config.FilterConfigParser;
import solutions.trsoftware.commons.server.servlet.filters.config.FilterParameters;
import solutions.trsoftware.commons.server.servlet.filters.config.ParseException;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

/**
 * Provides empty default implementations of {@link Filter#init(FilterConfig)} and {@link Filter#destroy()}}
 *  
 * @since Jul 29, 2009
 * @author Alex
 */
public abstract class AbstractFilter implements Filter {

  protected static FilterConfigParser configParser = new FilterConfigParser();

  protected FilterConfig filterConfig;

  public void init(FilterConfig filterConfig) throws ServletException {
    // subclasses may override
    this.filterConfig = filterConfig;
  }

  public void destroy() {
    // subclasses may override
  }

  protected <P extends FilterParameters> P parse(P parameters) throws ServletException {
    try {
      P parsedParams = configParser.parse(filterConfig, parameters);
      System.out.printf("Filter '%s' (%s) init-params: %s", filterConfig.getFilterName(), getClass(), parsedParams);
      return parsedParams;
    }
    catch (ParseException e) {
      throw new ServletException(String.format("Invalid configuration for filter '%s' (%s): %s",
          filterConfig.getFilterName(), getClass(), e.getMessage()),
          e);
    }
  }
}
