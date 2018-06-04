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

import solutions.trsoftware.commons.server.servlet.config.InitParameters;
import solutions.trsoftware.commons.server.util.ThreadUtils;
import solutions.trsoftware.commons.shared.util.MathUtils;
import solutions.trsoftware.commons.shared.util.RandomUtils;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Random;

/**
 * Blocks the request processing thread to simulate network lag, before passing the request down the filter chain.
 * <p>
 * The duration of the sleep is determined by the {@code init-param} values {@link Config#lagMillis lagMillis},
 * {@link Config#random random}, and {@link Config#randomMode randomMode}.
 * </p>
 * <p>
 * If {@link Config#random random} is {@code true}, the sleep duration will be randomly chosen based on the algorithm
 * specified by {@link Config#randomMode randomMode}:
 * <ul>
 *   <li>
 *     {@link Config#randomMode randomMode} = {@link RandomMode#GAUSSIAN GAUSSIAN} (default):
 *     Gaussian probability distribution with {@code mean} = {@code lagMillis} and {@code stdev} = {@code lagMillis / 2}
 *   </li>
 *   <li>
 *      {@link Config#randomMode randomMode} = {@link RandomMode#UNIFORM UNIFORM}:
  *     Uniform random number from the range {@code [0, lagMillis*2]}
 *   </li>
 * </ul>
 *
 * </p>
 * @see Config
 * @see Random#nextGaussian()
 * @author Alex
 * @since 1/2/2018
 *
 *
 */
public class LagSimulationFilter extends HttpFilterAdapter {

  private Config config;

  /**
   * Specifies the {@code init-param} settings for this filter's {@link FilterConfig}
   */
  private static class Config implements InitParameters {
    @Param(required = true)
    private int lagMillis;
    private boolean random;
    private RandomMode randomMode;

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder("Config{");
      sb.append("lagMillis=").append(lagMillis);
      sb.append(", random=").append(random);
      sb.append(", randomMode=").append(randomMode);
      sb.append('}');
      return sb.toString();
    }
  }

  public enum RandomMode {
    /**
     * Gaussian probability distribution with {@code mean} = {@code lagMillis} and {@code stdev} = {@code lagMillis / 2}
     */
    GAUSSIAN,
    /**
     * Uniform random number from the range {@code [0, lagMillis*2]}
     */
    UNIFORM
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    super.init(filterConfig);
    config = parse(new Config());
  }

  @Override
  public void doHttpFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
    long lag = getSleepTime();
    System.out.printf("LagSimulationFilter sleep(%d) for %s%n", lag, request.getRequestURI());  // TODO: temp
    ThreadUtils.sleepUnchecked(lag);
    filterChain.doFilter(request, response);
  }

  /**
   * See the {@link LagSimulationFilter} doc for an explanation of how the sleep duration is determined.
   * @return the duration to sleep, in millis
   */
  protected long getSleepTime() {
    long lag = config.lagMillis;
    if (config.random) {
      switch (config.randomMode) {
        case UNIFORM:
          lag = RandomUtils.nextIntInRange(0, config.lagMillis * 2);
          break;
        case GAUSSIAN:
          lag = MathUtils.restrict(RandomUtils.nextGaussian(config.lagMillis, config.lagMillis / 2), 0, config.lagMillis * 2);
          break;
      }
    }
    return lag;
  }
}
