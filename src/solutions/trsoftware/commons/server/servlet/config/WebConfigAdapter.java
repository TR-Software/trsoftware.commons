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
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * Base class for Servlet API object adapters that expose the underlying object's init-params.
 *
 * @param <T> the type of the source where the init-params come from: typically either
 *            {@link FilterConfig}, {@link ServletConfig}, or {@link ServletContext}
 *
 * @see FilterConfigWrapper
 * @see ServletConfigWrapper
 * @see ServletContextWrapper
 *
 * @author Alex
 * @since 7/24/2018
 */
public abstract class WebConfigAdapter<T> implements HasInitParameters<T> {

  private T source;

  public WebConfigAdapter(T source) {
    this.source = source;
  }

  @Override
  public T getSource() {
    return source;
  }
}
