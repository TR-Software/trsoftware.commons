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

import solutions.trsoftware.commons.server.servlet.config.HasInitParameters;

import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

/**
 * @author Alex
 * @since 3/6/2018
 */
public abstract class DummyWebConfigObject implements HasInitParameters {

  protected Map<String, String> initParameterMap;
  protected ServletContext servletContext;

  protected DummyWebConfigObject() {
  }

  protected DummyWebConfigObject(Map<String, String> initParameters) {
    this(initParameters, null);
  }

  public DummyWebConfigObject(Map<String, String> initParameterMap, ServletContext servletContext) {
    this.initParameterMap = initParameterMap;
    this.servletContext = servletContext;
  }

  @Override
  public String getInitParameter(String name) {
    return initParameterMap.get(name);
  }

  @Override
  public Enumeration<String> getInitParameterNames() {
    return Collections.enumeration(initParameterMap.keySet());
  }

  public ServletContext getServletContext() {
    if (this instanceof ServletContext)
      return (ServletContext)this;
    return servletContext;
  }

  public Map<String, String> getInitParameterMap() {
    return initParameterMap;
  }

  public void setInitParameterMap(Map<String, String> initParameterMap) {
    this.initParameterMap = initParameterMap;
  }
}
