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

import solutions.trsoftware.commons.server.servlet.ServletUtils;
import solutions.trsoftware.commons.server.servlet.config.ClassNameParameterParser;
import solutions.trsoftware.commons.server.servlet.config.InitParameters;
import solutions.trsoftware.commons.shared.util.StringUtils;

import javax.annotation.Nonnull;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static solutions.trsoftware.commons.shared.util.TimeUnit.SECONDS;
import static solutions.trsoftware.commons.shared.util.TimeUnit.YEARS;

/**
 * Sets the HTTP response caching headers for mapped resources according to the {@link CachePolicy} value returned
 * by the configured instance of {@link CachePolicyMatcher} as follows:
 * <ul>
 * <li> {@link CachePolicy#NO_CACHE} (should be used for GWT's .nocache.js files):
 * <pre>
 *   Pragma: no-cache
 *   Cache-Control: max-age=0, no-store, no-cache, must-revalidate, no-transform
 *   Expires: Fri, 02 Jan 1970 00:00:00 GMT
 * </pre>
 * </li>
 *
 * <li> {@link CachePolicy#CACHE_FOREVER} (should be used for GWT's .cache.* files):
 * <pre>
 *   Pragma: no-cache
 *   Cache-Control: public, max-age=315360000, no-transform
 *   Expires: $DATE(10 years from now)$
 * </pre>
 * (where <i>$DATE(10 years from now)$</i> is a date formatted according to
 * <a href="https://tools.ietf.org/html/rfc822#section-5">RFC 822 section 5</a>; see
 * {@link org.apache.catalina.connector.Response})
 * </li>
 * </ul>
 *
 * <li> {@link CachePolicy#DEFAULT} (for all other files):
 * <pre>
 *   Cache-Control: no-transform
 * </pre>
 * </li>
 * </ul>
 *
 * <p style="font-style: italic;">
 *   NOTE: while placing the {@code no-transform} directive in the {@code Cache-Control} header is not required,
 *   we do it simply to prevent script injection from 3rd-party proxies
 *   (see <a href="https://stackoverflow.com/questions/4113268/stop-mobile-network-proxy-from-injecting-javascript">
 *     StackOverflow</a>)
 * </p>
 *
 * <h3>Configuration</h3>
 * By default, this filter applies the {@link CachePolicy#NO_CACHE} setting for all files whose URI ends with a
 * <i>.nocache.*</i> extension, and {@link CachePolicy#CACHE_FOREVER} for all files whose URI ends with a
 * <i>.cache.*</i> extension.
 * <p>
 * The matching algorithm is encapsulated in the configured instance of {@link CachePolicyMatcher} (which defaults to
 * {@link DfaScannerMatcher}).  This default algorithm uses a hard-coded DFA scanner to ensure the path is matched
 * only if the string occurs at the end of the URI (e.g. matches {@code "/foo/bar.nocache.js"}
 * but not {@code "/foo.nocache.js/bar"}).  While this implementation very fast (about 45x faster than an equivalent regex),
 * we also provide {@link SimpleStringMatcher}, which is even faster, but cannot differentiate between
 * {@code "/foo/bar.nocache.js"} and {@code "/foo.nocache.js/bar"}), so should be used with caution, unless the
 * the filter is mapped only to url patterns like {@code "*.js"}.
 *
 * <p>
 *   <b>Example configuration:</b>
 * <pre>{@code
      <filter>
        <filter-name>CachePolicyFilter</filter-name>
        <filter-class>solutions.trsoftware.commons.server.servlet.filters.CachePolicyFilter</filter-class>
        <!-- The init-param section is optional -->
        <init-param>
          <param-name>cachePolicyMatcher</param-name>
          <param-value>solutions.trsoftware.commons.server.servlet.filters.CachePolicyFilter$SimpleStringMatcher</param-value>
        </init-param>
      </filter>
      <!--
        NOTE: unfortunately the Servlet Spec only supports simple extensions (like *.js) in url-pattern values,
        so we can't map this filter only to the complex extensions that we need (like *.nocache.js)
       -->
      <filter-mapping>
        <filter-name>CachePolicyFilter</filter-name>
        <url-pattern>*.js</url-pattern>
      </filter-mapping>
      <filter-mapping>
        <filter-name>CachePolicyFilter</filter-name>
        <url-pattern>*.html</url-pattern>
      </filter-mapping>
      <filter-mapping>
        <filter-name>CachePolicyFilter</filter-name>
        <url-pattern>*.png</url-pattern>
      </filter-mapping>
 * }</pre>
 *
 *
 * @author Alex
 * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html">RFC 2616</a>
 */
public class CachePolicyFilter extends HttpFilterAdapter {

  // TODO: finish unit testing this class

  /** A date which is 10 years from the time this class was loaded by the JVM */
  static final long TEN_YEARS_FROM_NOW_DATE;  // exposed for unit testing
  static final String CACHE_CONTROL_HEADER_VALUE_10YEARS;  // exposed for unit testing

  static {
    long tenYearsInSeconds = (long)YEARS.to(SECONDS, 10);
    TEN_YEARS_FROM_NOW_DATE = System.currentTimeMillis() + (tenYearsInSeconds * 1000L);
    // NOTE: the no-transform directive was added for reasons described in http://stackoverflow.com/questions/4113268/how-to-stop-javascript-injection-from-vodafone-proxy
    CACHE_CONTROL_HEADER_VALUE_10YEARS = "public, max-age=" + tenYearsInSeconds + ", no-transform";
  }

  /**
   * Specifies the {@code init-param} settings for this filter's {@link FilterConfig}
   */
  private static class Config implements InitParameters {
    /**
     * This value of this <i>init-param</i> should be the name of a subclass of {@link CachePolicyMatcher}.
     * Defaults to {@link DfaScannerMatcher}
     */
    @InitParameters.Param(parser = ClassNameParameterParser.class)
    private CachePolicyMatcher cachePolicyMatcher = new DfaScannerMatcher();

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder("Config{");
      sb.append("cachePolicyMatcher=").append(cachePolicyMatcher);
      sb.append('}');
      return sb.toString();
    }
  }

  private Config config;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    super.init(filterConfig);
    config = parse(new Config());
  }

  public void doHttpFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
    String uri = request.getRequestURI();
    CachePolicy cachePolicy = inferCachePolicy(uri);
    switch (cachePolicy) {
      case CACHE_FOREVER:
        response.setHeader("Cache-Control", CACHE_CONTROL_HEADER_VALUE_10YEARS);
        response.setDateHeader("Expires", TEN_YEARS_FROM_NOW_DATE);
        break;
      case NO_CACHE:
        ServletUtils.disableResponseBrowserCaching(request, response);
        break;
      default:
        // either way, we set the no-transform directive for reasons described in http://stackoverflow.com/questions/4113268/how-to-stop-javascript-injection-from-vodafone-proxy
        response.setHeader("Cache-Control", "no-transform");
    }
    filterChain.doFilter(request, response);
  }

  /**
   * Determines if the requested file should be cached "forever" or not cached at all.
   *
   * @param uri path of the incoming request (obtained from {@link HttpServletRequest#getRequestURI()}
   * @return {@link CachePolicy#NO_CACHE} if the filename has a <i>.nocache.*</i> extension,
   *     {@link CachePolicy#CACHE_FOREVER} if the filename has a <i>.cache.*</i> extension,
   *     or {@link CachePolicy#DEFAULT} if neither.
   */
  @Nonnull
  protected CachePolicy inferCachePolicy(String uri) {
    return getCachePolicyMatcher().inferCachePolicy(uri);
  }

  /**
   * Exposed for unit testing.
   * @return {@link Config#cachePolicyMatcher}
   */
  CachePolicyMatcher getCachePolicyMatcher() {
    return config.cachePolicyMatcher;
  }


  /**
   * Defines how {@link CachePolicyFilter} should set the response headers for matched resources.
   * @see CachePolicyMatcher#inferCachePolicy(String)
   */
  enum CachePolicy {
    /**
     * Should use the default cache policy for the requested file
     */
    DEFAULT,
    /**
     * The requested file should never be cached
     */
    NO_CACHE,
    /**
     * The requested file should be cached indefinitely
     */
    CACHE_FOREVER
  }


  /**
   * Determines if the requested file should be cached "forever" or not cached at all,
   * given the path of the incoming request (obtained from {@link HttpServletRequest#getRequestURI()}).
   */
  public interface CachePolicyMatcher {
    /**
     * Determines if the requested file should be cached "forever" or not cached at all.
     *
     * @param uri path of the incoming request (obtained from {@link HttpServletRequest#getRequestURI()}
     * @return {@link CachePolicy#NO_CACHE} if the filename has a <i>.nocache.*</i> extension,
     *     {@link CachePolicy#CACHE_FOREVER} if the filename has a <i>.cache.*</i> extension,
     *     or {@link CachePolicy#DEFAULT} if neither.
     */
    @Nonnull
    CachePolicy inferCachePolicy(String uri);
  }

  /**
   * Applies the {@link CachePolicy#NO_CACHE} setting for all files whose URI ends with a
   * <i>.nocache.*</i> extension, and {@link CachePolicy#CACHE_FOREVER} for all files whose URI ends with a
   * <i>.cache.*</i> extension.
   *
   * <p style="font-style: italic;"><strong>CAUTION:</strong>
   * While this implementation very fast (about 124x faster than an equivalent regex),
   * it cannot differentiate between {@code "/foo/bar.nocache.js"} and {@code "/foo.nocache.js/bar"}),
   * so should be used with caution, unless the the filter is mapped only to url patterns like {@code "*.js"}.
   * </p>
   */
  public static class SimpleStringMatcher implements CachePolicyMatcher {
    /**
     * Determines if the requested file should be cached "forever" or not cached at all.
     *
     * @param uri path of the incoming request (obtained from {@link HttpServletRequest#getRequestURI()}
     * @return {@link CachePolicy#NO_CACHE} if the filename has a <i>.nocache.*</i> extension,
     *     {@link CachePolicy#CACHE_FOREVER} if the filename has a <i>.cache.*</i> extension,
     *     or {@link CachePolicy#DEFAULT} if neither.
     */
    @Override
    public CachePolicy inferCachePolicy(String uri) {
      int iLastDot = uri.lastIndexOf('.');
      if (iLastDot > 6) { // here we test > 6 rather than >= 0 because we need at least 6 char in front of this
        // determine if the preceding substring is ".cache" or ".nocache"
        if (StringUtils.endsWith(uri, iLastDot, ".cache"))
          return CachePolicy.CACHE_FOREVER;  // has a .cache.* extension
        if (StringUtils.endsWith(uri, iLastDot, ".nocache"))
          return CachePolicy.NO_CACHE;  // has a .nocache.* extension
      }
      return CachePolicy.DEFAULT;
    }
  }

  /**
   * Implements the regex {@code .*[^/]+\.(cache|nocache)\.[^/]+} using a hard-coded DFA for speed.
   * <p>
   * While slightly slower than {@link SimpleStringMatcher}, this algorithm ensures that a path is matched
   * only if the string occurs at the end of the URI (e.g. matches {@code "/foo/bar.nocache.js"}.
   * This algorithm is still about 45x faster than using an equivalent regular expression.
   */
  public static class DfaScannerMatcher implements CachePolicyMatcher {
    /**
     * Determines if the requested file should be cached "forever" or not cached at all.
     * @param uri path of the incoming request (obtained from {@link HttpServletRequest#getRequestURI()}
     * @return {@link CachePolicy#NO_CACHE} if the filename has a <i>.nocache.*</i> extension,
     *  {@link CachePolicy#CACHE_FOREVER} if the filename has a <i>.cache.*</i> extension,
     *  or {@code null} if neither.
     */
    @Override
    public CachePolicy inferCachePolicy(String uri) {
      Scanner.State scanResult = new Scanner().scan(uri);
      switch (scanResult) {
        case CACHE:
          return CachePolicy.CACHE_FOREVER;  // has a .cache.* extension
        case NOCACHE:
          return CachePolicy.NO_CACHE;  // has a .nocache.* extension
        default:
          return CachePolicy.DEFAULT;
      }
    }

    /**
     * Implements the regex {@code .*[^/]+\.(cache|nocache)\.[^/]+} using a hard-coded DFA for speed.
     */
    private static class Scanner {
      enum State {
        START {
          @Override
          State next(char c) {
            switch (c) {
              case '/':
              case '.':
                return FAIL;
              default:
                return SUFFIX;
            }
          }
        },
        SUFFIX {
          @Override
          State next(char c) {
            switch (c) {
              case '/':
                return FAIL;
              case '.':
                return DOT;
              default:
                return SUFFIX;
            }
          }
        },
        DOT {
          @Override
          State next(char c) {
            return c == 'e' ? e : FAIL;
          }
        },
        e {
          @Override
          State next(char c) {
            return c == 'h' ? h : FAIL;
          }
        },
        h {
          @Override
          State next(char c) {
            return c == 'c' ? c1 : FAIL;
          }
        },
        c1 {
          @Override
          State next(char c) {
            return c == 'a' ? a : FAIL;
          }
        },
        a {
          @Override
          State next(char c) {
            return c == 'c' ? c2 : FAIL;
          }
        },
        c2 {
          @Override
          State next(char c) {
            // the string "cache.*" has been scanned at this point
            switch (c) {
              case '.':
                return DOT_CACHE;
              case 'o':
                return o;
              default:
                return FAIL;
            }
          }
        },
        DOT_CACHE {
          @Override
          State next(char c) {
            return c != '/' ? CACHE : FAIL;
          }
        },
        o {
          @Override
          State next(char c) {
            return c == 'n' ? n : FAIL;
          }
        },
        n {
          @Override
          State next(char c) {
            return c == '.' ? DOT_NOCACHE : FAIL;
          }
        },
        DOT_NOCACHE {
          @Override
          State next(char c) {
            return c != '/' ? NOCACHE : FAIL;
          }
        },
        /**
         * {@code [^/]\.cache\.[^/]+} has been scanned
         */
        CACHE(true),
        /**
         * {@code [^/]\.nocache\.[^/]+} has been scanned
         */
        NOCACHE(true),
        FAIL(true);

        private boolean terminal;

        State() {
        }

        State(boolean terminal) {
          this.terminal = terminal;
        }

        public boolean isTerminal() {
          return terminal;
        }

        /**
         * State transition function.
         * @param c the next char of the input (consumed)
         * @return the state transition
         */
        State next(char c) {
          if (terminal)
            throw new IllegalStateException("terminal state");
          throw new UnsupportedOperationException("non-terminal states must override this method");
        }
      }

      private State state = State.START;

      public State scan(String str) {
        for (int i = str.length() - 1; i >= 0 && !state.isTerminal(); i--) {
          char c = str.charAt(i);
          state = state.next(c);
        }
        return state;
      }
    }
  }

}
