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

package solutions.trsoftware.commons.server.servlet.filters;

import junit.framework.TestCase;
import solutions.trsoftware.commons.server.servlet.config.InitParameters;
import solutions.trsoftware.commons.server.servlet.testutil.DummyFilterConfig;
import solutions.trsoftware.commons.shared.util.ColorRGB;
import solutions.trsoftware.commons.shared.util.MapUtils;

import javax.servlet.*;
import java.io.IOException;

/**
 * @author Alex
 * @since 3/6/2018
 */
public class AbstractFilterTest extends TestCase {

  /**
   * Tests parsing init parameters from the filter's {@link FilterConfig} with {@link AbstractFilter#parse(InitParameters)}
   */
  public void testParse() throws Exception {
    TestFilter filter = new TestFilter();
    filter.init(new DummyFilterConfig(MapUtils.stringLinkedHashMap(
        "size", "MEDIUM",
        "color", "#123456",
        "logRequests", "true"
    )));
    assertEquals(0, filter.config.minValue);
    assertEquals(TestFilter.Size.MEDIUM, filter.config.size);
    assertEquals(new ColorRGB(0x12, 0x34, 0x56), filter.config.color);
    assertEquals(true, filter.config.logRequests);
  }


  private static class TestFilter extends AbstractFilter {
    private Config config;

    private enum Size {SMALL, MEDIUM, LARGE};

    /**
     * Specifies the {@code init-param} settings that will be parsed from this filter's {@link FilterConfig}
     */
    private static class Config implements InitParameters {
      private int minValue;
      private Size size;
      private ColorRGB color;
      private boolean logRequests;

      @Override
      public String toString() {
        final StringBuilder sb = new StringBuilder("Config{");
        sb.append("minValue=").append(minValue);
        sb.append(", size=").append(size);
        sb.append(", color=").append(color);
        sb.append(", logRequests=").append(logRequests);
        sb.append('}');
        return sb.toString();
      }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
      super.init(filterConfig);
      config = parse(new Config());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
      throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.filters.AbstractFilterTest.TestFilter.doFilter has not been fully implemented yet.");
    }
  }


}