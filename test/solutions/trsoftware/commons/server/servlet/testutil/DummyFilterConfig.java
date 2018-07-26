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

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.util.Map;

/**
 * @author Alex
 * @since 1/2/2018
 */
public class DummyFilterConfig extends DummyWebConfigObject<FilterConfig> implements FilterConfig {

  private String filterName;

  public DummyFilterConfig(Map<String, String> initParameters) {
    this(initParameters, new DummyServletContext());
  }

  public DummyFilterConfig(Map<String, String> initParameterMap, ServletContext servletContext) {
    super(initParameterMap, servletContext);
  }

  @Override
  public String getFilterName() {
    return filterName;
  }

  public DummyFilterConfig setFilterName(String filterName) {
    this.filterName = filterName;
    return this;
  }

  public DummyFilterConfig setServletContext(ServletContext servletContext) {
    this.servletContext = servletContext;
    return this;
  }
}
