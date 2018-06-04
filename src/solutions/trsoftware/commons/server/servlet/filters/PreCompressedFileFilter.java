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

import solutions.trsoftware.commons.server.servlet.config.ClassNameParameterParser;
import solutions.trsoftware.commons.server.servlet.config.InitParameters;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Serves pre-gzipped copies of certain files.  The algorithm for rewriting URIs is encapsulated in a
 * {@link RewriteRule} subclass, whose name is given by the <i>init-param</i> named <i>rewriteRule</i>,
 * which defaults to {@link DefaultRewriteRule} (see {@link Config#rewriteRule}).
 *
 * <h3>Background</h3>
 * This logic was originally written for TypeRacer back in 2008 for 2 reasons:
 * <ol>
 *   <li>
 *      It was running on Tomcat 5.5, which had a bug that prevented it from gzipping GWT's {@code .cache.html}
 *      artifacts on the fly.
 *   </li>
 *   <li>to save CPU cycles that would be spent compressing files at runtime</li>
 * </ol>
 *
 * <p style="font-style: italic;">
 *   NOTE: Pre-gzipping resources with Tomcat is probably no longer necessary, because Tomcat (at least as of 8.5)
 *   caches pre-compressed copies of resources in {@link org.apache.catalina.servlets.DefaultServlet}
 *   (see org.apache.catalina.servlets.DefaultServlet:838)
 * </p>
 *
 * @author Alex
 * @see org.apache.catalina.servlets.DefaultServlet
 */
public class PreCompressedFileFilter extends HttpFilterAdapter {

  // TODO: unit test this class if it's ever used again

  private Config config;

  /**
   * Specifies the {@code init-param} settings for this filter's {@link FilterConfig}
   */
  private static class Config implements InitParameters {
    /**
     * This value of this <i>init-param</i> should be the name of a subclass of {@link RewriteRule}.
     * Defaults to {@link DefaultRewriteRule}
     */
    @Param(parser = ClassNameParameterParser.class)
    private RewriteRule rewriteRule = new DefaultRewriteRule();
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    super.init(filterConfig);
    config = parse(new Config());
  }


  public void doHttpFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
    String acceptEncodingHeader = request.getHeader("Accept-Encoding");
    if (acceptEncodingHeader != null && acceptEncodingHeader.contains("gzip")) {
      String gzippedFileUrl = config.rewriteRule.maybeRewrite(request.getRequestURI());
      if (gzippedFileUrl != null) {
        response.setHeader("Content-Encoding", "gzip");
        request.getRequestDispatcher(gzippedFileUrl).forward(request, response);
        return;  // no need to complete the filter chain
      }
    }
    filterChain.doFilter(request, response);
  }

  /**
   * Determines if a pre-gzipped copy of the requested file is available, given the path of the incoming request
   * (obtained from {@link HttpServletRequest#getRequestURI()}), and if so,
   * returns a modified URI pointing to that resource.
   */
  public interface RewriteRule {
    /**
     * Determines if a pre-gzipped copy of the requested file is available, and if yes,
     * returns a modified URI pointing to that resource.
     *
     * @param uri path of the incoming request (obtained from {@link HttpServletRequest#getRequestURI()}
     * @return a modified URI pointing to the pre-gzipped copy of the requested resource, or {@code null} if the request
     * should proceed unmodified.
     */
    String maybeRewrite(String uri);
  }

  /**
   * Rewrites URIs ending in <i>.cache.html</i>, <i>.cache.js</i>, or <i>.cache.css</i>, to a subdirectory named <i>gz</i>
   */
  public static class DefaultRewriteRule implements RewriteRule {

    /**
     * The suffixes of matching URIs.  Subclasses can replace these values.
     */
    protected List<String> suffixes = Arrays.asList(
        ".cache.html",
        ".cache.js",
        ".cache.css"
    );

    /**
     * The matching URIs will be rewritten to a subdirectory with this name.  Subclasses can replace this value.
     */
    protected String subdirName = "gz";

    /**
     * Rewrites URIs ending in one of the entries in {@link #suffixes} to a subdirectory named
     * {@link #subdirName}
     * @param uri path of the incoming request (obtained from {@link HttpServletRequest#getRequestURI()}
     */
    @Override
    public String maybeRewrite(String uri) {
      if (suffixes.stream().anyMatch(uri::endsWith)) {
        // the gzipped version is assumed to be in the gz/ subdirectory under the original's directory
        int lastSlash = uri.lastIndexOf("/");
        return uri.substring(0, lastSlash) + "/" + subdirName + "/" + uri.substring(lastSlash + 1);
      }
      return null;
    }
  }

}
