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

import solutions.trsoftware.commons.server.management.monitoring.RequestStats;
import solutions.trsoftware.commons.server.stats.HierarchicalCounter;
import solutions.trsoftware.commons.server.stats.SimpleCounter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Gathers statistics about processed requests.
 *
 * Stores a hierarchy of {@link HierarchicalCounter counters} with roughly the following structure:
 * <pre>
 *   ["RequestCounts"]
 *     - [URI_0]
 *     - ...
 *     - [URI_n]
 * </pre>
 *
 * TODO: start using {@link RequestStats} as the data structure for recording
 *
 *
 * @author Alex, 10/31/2017
 */
public class RequestStatsFilter extends HttpFilterAdapter {

  private HierarchicalCounter requestCounts = new HierarchicalCounter(new SimpleCounter("RequestCounts"), null);

  @Override
  public void doHttpFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
    String name = request.getRequestURI();
    HierarchicalCounter counter;
    if (requestCounts.containsChild(name))
      counter = requestCounts.getChild(name);
    else
      counter = new HierarchicalCounter(new SimpleCounter(name), requestCounts); // the HierarchicalCounter constructor will automatically add this child to rootCounter
    counter.incr();
    filterChain.doFilter(request, response);
    // TODO: count the number of errors (i.e. if an exception was thrown from the filter chain)
  }

  public HierarchicalCounter getRequestCounts() {
    return requestCounts;
  }

}
