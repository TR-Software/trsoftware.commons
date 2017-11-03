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

import solutions.trsoftware.commons.server.servlet.ServletUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static solutions.trsoftware.commons.client.util.TimeUnit.SECONDS;
import static solutions.trsoftware.commons.client.util.TimeUnit.YEARS;

/**
 * Sets the HTTP response headers for GWT's .nocache.js files to
 *
 * Pragma: no-cache
 * Cache-Control: max-age=0, no-store, no-cache, must-revalidate
 * Expires: Fri, 02 Jan 1970 00:00:00 GMT
 *
 * Sets the HTTP response headers for GWT's .cache.* files to
 *
 * Pragma: no-cache
 * Cache-Control: public, max-age=315360000
 * Expires: <10 years from now>
 *
 * @author Alex
 * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html">RFC 2616</a>
 */
public class CachePolicyFilter extends HttpFilterAdapter {
  // TODO: fix the TODO items in this class and unit test it


  /** A date which is 10 years from the time this class was loaded by the JVM */
  private static final long TEN_YEARS_FROM_NOW_DATE;
  private static final String CACHE_CONTROL_HEADER_VALUE_10YEARS;

  static {
    long tenYearsInSeconds = (long)YEARS.to(SECONDS, 10);
    TEN_YEARS_FROM_NOW_DATE = System.currentTimeMillis() + (tenYearsInSeconds * 1000L);
    // NOTE: the no-transform directive was added for reasons described in http://stackoverflow.com/questions/4113268/how-to-stop-javascript-injection-from-vodafone-proxy
    CACHE_CONTROL_HEADER_VALUE_10YEARS = "public, max-age=" + tenYearsInSeconds + ", no-transform";
  }

  public void doHttpFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
//    System.out.println("CachePolicyFilter.doFilter" + ((HttpServletRequest)request).getRequestURI());
    String url = request.getRequestURI();
    int lastIndexOfDotCacheDot = url.lastIndexOf(".cache.");

    if (lastIndexOfDotCacheDot >= 0) {  // we have a *.cache.* URL
      int indexOfExtension = lastIndexOfDotCacheDot + ".cache.".length();
      boolean isCacheDotHtml;
      boolean isCacheDotCss = false;
      if ((isCacheDotHtml = url.startsWith("html", indexOfExtension))
          || (isCacheDotCss = url.startsWith("css", indexOfExtension))
          // TODO: why is this code treating png/gif differently? what about jpeg, etc.? It should just apply to any file ending in .cache.*
          || url.startsWith("png", indexOfExtension)
          || url.startsWith("gif", indexOfExtension)) {
        response.setHeader("Cache-Control", CACHE_CONTROL_HEADER_VALUE_10YEARS);
        response.setDateHeader("Expires", TEN_YEARS_FROM_NOW_DATE);
        // the .cache.html needs to be handled differently - we redirect to the
        // gzipped version of the resource, b/c Tomcat has a bug preventing gzipping it on the fly
        // also, returning the pre-gzipped file saves CPU at runtime
        // NOTE: for completeness could also disable gzip if user agent is gozilla or traviata, but, really who cares about these?
        // we do the same for .cache.css files even though Tomcat can gzip them on the fly, because we might as well do it, to save CPU cycles
        boolean preGzippedCopyAvailable = isCacheDotHtml || isCacheDotCss;
        // TODO: make sure the pre-gzipped file actually exists
        if (preGzippedCopyAvailable) {
          String acceptEncodingHeader = request.getHeader("Accept-Encoding");
          if (acceptEncodingHeader != null && acceptEncodingHeader.contains("gzip")) {
            response.setHeader("Content-Encoding", "gzip");
            // the gzipped version is in gz/ subdirectory under the original's directory
            int lastSlash = url.lastIndexOf("/");
            String gzippedFileUrl = url.substring(0, lastSlash) + "/gz/" + url.substring(lastSlash + 1);
            request.getRequestDispatcher(gzippedFileUrl).forward(request, response);
            return;  // no need to complete the filter chain
          }
        }
      }
    }
    else if (url.endsWith(".nocache.js")) {
      // TODO: should we do this for all *.nocache.* files?
      ServletUtils.disableResponseBrowserCaching(request, response);
    }
    else {
      // either way, we set the no-transform directive for reasons described in http://stackoverflow.com/questions/4113268/how-to-stop-javascript-injection-from-vodafone-proxy
      response.setHeader("Cache-Control", "no-transform");
    }
    filterChain.doFilter(request, response);
  }

}
