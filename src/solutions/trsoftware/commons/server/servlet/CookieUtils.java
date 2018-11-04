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

package solutions.trsoftware.commons.server.servlet;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URL;

/**
 * Date: Aug 27, 2008 Time: 1:52:35 PM
 *
 * @author Alex
 */
public abstract class CookieUtils {
  public static final String JSESSIONID_COOKIE_NAME = "JSESSIONID";

  /**
   * Adds a cookie to the {@link HttpServletResponse response}.
   * @param domain should prefix the domain name with a dot to support subdomains (this should only be necessary to support older browsers)
   * @see
   *   <a href="https://stackoverflow.com/questions/9618217/what-does-the-dot-prefix-in-the-cookie-domain-mean">Stack Overflow</a>,
   *   <a href="https://www.mxsasha.eu/blog/2014/03/04/definitive-guide-to-cookie-domains/">The definitive guide to cookie domains</a>,
   *   <a href="https://tools.ietf.org/html/rfc6265#section-4.1.2.3">RFC 6265</a>,
   *   <a href="https://tools.ietf.org/html/rfc2965">RFC 2965</a>
   */
  public static void addCookie(HttpServletRequest request, HttpServletResponse response, String domain, String cookieName, String cookieValue, long maxAgeMillis) {
    // SECURITY: the only way to secure the cookies is to send them over SSL (Secure=True) and set http-only=True so they cannot be accessed from  malicious injected javascript
    // a partial solution without SSL is to check the IP address of every request (but will not work if the attacker has the same IP address, such as at a coffee shop, and will be a nuisance to people)
    // conclusion: without SSL, do not use cookie authentication for important functions (such as changing your email or password)
    Cookie cookie = new Cookie(cookieName, cookieValue);
    cookie.setVersion(0);  // IE7 & Safari don't recognize expiration dates in version 1, and hence don't retain the cookies past the session
    if (domain != null) {
      URL requestURL = ServletUtils.getRequestURL(request);
      if (!"localhost".equals(requestURL.getHost()))  // localhost is not a valid value of the cookie domain attribute
        cookie.setDomain(domain);
    }
    cookie.setPath("/");  // send cookie to all URIs in the domain
    // NOTE: because we're using version 0 cookies, the server will actually output something like "Expires=Wed, 12-Jan-2011 22:00:21 GMT" instead of "Max-Age=12345"
    cookie.setMaxAge((int)(maxAgeMillis / 1000)); // delta seconds from now
    // NOTE: might be able to save some bandwidth by restricting the login cookie path; would probably have to set multiple cookies - one for each path we want
    // "The path "/foo" would match "/foobar" and "/foo/bar.html". The path "/" is the most general path. If the path is not specified, it as assumed to be the same path as the document being described by the header which contains the cookie. - http://curl.haxx.se/rfc/cookie_spec.html )
    // but unfortunately, the browser must reject if "The value for the Path attribute is not a prefix of the request-URI." - http://www.ietf.org/rfc/rfc2109.txt
    // so to do that, would have to make sure that both the Java server and the GAE server take the cookie at the same path
    // conclusion: probably not worth the effort
    response.addCookie(cookie);
  }

  /**
   * @return the value of the HTTP cookie with the given name if the request contains such a cookie; otherwise {@code null}
   * @see Cookie#getValue()
   */
  public static String getCookieValue(HttpServletRequest request, String cookieName) {
    Cookie cookie = getCookie(request, cookieName);
    if (cookie != null)
      return cookie.getValue();
    return null;
  }

  /**
   * @return the HTTP cookie with the given name if the request contains such a cookie; otherwise {@code null}
   */
  public static Cookie getCookie(HttpServletRequest request, String cookieName) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookieName.equals(cookie.getName())) {
          return cookie;
        }
      }
    }
    return null;
  }

  /**
   * If the request contains a {@code JSESSIONID} cooke, deletes it by setting its {@code Max-Age=0}.
   */
  public static void deleteJsessionidCookieIfPresent(HttpServletRequest request, HttpServletResponse response) {
    Cookie[] requestCookies = request.getCookies();
    if (requestCookies != null) {
      for (Cookie cookie : requestCookies) {
        if (JSESSIONID_COOKIE_NAME.equalsIgnoreCase(cookie.getName())) {
          cookie.setMaxAge(0); // deletes the cookie on the browser
          if (cookie.getPath() == null)
            cookie.setPath("/");  // (for some reason) a path must be present for the browser to react to the change
          response.addCookie(cookie);
        }
      }
    }
  }

}
