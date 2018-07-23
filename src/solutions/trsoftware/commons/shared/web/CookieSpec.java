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

package solutions.trsoftware.commons.shared.web;

import solutions.trsoftware.commons.shared.util.TimeValue;

/**
 * Encapsulates all the info needed to set a particular cookie.
 *
 * @see javax.servlet.http.Cookie
 * @see <a href="https://www.ietf.org/rfc/rfc2109.txt">RFC 2109</a>
 * @see <a href="https://www.quirksmode.org/js/cookies.html">https://www.quirksmode.org/js/cookies.html</a>
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Cookies">HTTP cookies (MDN page)</a>
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Document/cookie">JavaScript document.cookie (MDN reference)</a>
 * @author Alex
 * @since 7/23/2018
 */
public class CookieSpec {

  /**
   * Name of the cookie.
   */
  private String name;

  /**
   * The version of the cookie protocol this cookie complies with:
   * <ol start=0>
   *   <li>version 0 complies with the original Netscape cookie specification.</li>
   *   <li>version 1 complies with <a href="https://www.ietf.org/rfc/rfc2109.txt">RFC 2109</a></li>
   * </ol>
   * @see javax.servlet.http.Cookie#setVersion(int)
   */
  private int version;

  /**
   * How long the cookie should persist on the client.
   * If {@code null}, cookie will be removed after browser shutdown.
   * @see javax.servlet.http.Cookie#getMaxAge()
   */
  private TimeValue maxAge;

  /**
   * Specifies the domain within which this cookie should be presented.
   * Defaults to the current request host if {@code null}.
   * @see javax.servlet.http.Cookie#setDomain(java.lang.String)
   */
  private String domain;

  /**
   * Specifies the subset of URLs to which this cookie applies.
   * <ul>
   *   <li>
   *     If not explicitly specified (i.e. {@code null}), defaults to the path of the current request URL,
   *     up to, but not including, the right-most {@code /}
   *   </li>
   *   <li>
   *     Must be a prefix of the current request URI (otherwise rejected)
   *   </li>
   *   <li>
   *     Value of {@code /} makes the cookie apply to all URIs in the domain
   *   </li>
   * </ul>
   */
  private String path;

  /**
   * Indicates to the browser whether the cookie should only be sent using a
   * secure protocol, such as HTTPS or SSL.  Defaults to {@code false}.
   *
   * <blockquote cite="https://www.ietf.org/rfc/rfc2109.txt">
   *   The user agent (possibly under the user's control) may determine what level of security
   *   it considers appropriate for "secure" cookies.
   *   The Secure attribute should be considered security advice from the server to the user agent,
   *   indicating that it is in the session's interest to protect the cookie contents.
   * </blockquote>
   */
  private boolean secure = false;


  /**
   * NOTE: might be easier to use {@link Builder} instead of this constructor.
   *
   * @param name {@link #name}
   * @param version {@link #version}
   * @param maxAge {@link #maxAge}
   * @param domain {@link #domain}
   * @param path {@link #path}
   * @param secure {@link #secure}
   *
   * @see #builder()
   * @see #builder(String)
   *
   */
  public CookieSpec(String name, int version, TimeValue maxAge, String domain, String path, boolean secure) {
    this.name = name;
    this.version = version;
    this.maxAge = maxAge;
    this.domain = domain;
    this.path = path;
    this.secure = secure;
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * @param name the cookie name
   */
  public static Builder builder(String name) {
    return new Builder(name);
  }

  public String getName() {
    return name;
  }

  public int getVersion() {
    return version;
  }

  public TimeValue getMaxAge() {
    return maxAge;
  }

  public String getDomain() {
    return domain;
  }

  public String getPath() {
    return path;
  }

  public boolean isSecure() {
    return secure;
  }

  /**
   * Builder for a {@link CookieSpec} instance.
   * @see #builder()
   * @see #builder(String)
   */
  public static class Builder {
    private String name;
    private int version;
    private TimeValue maxAge;
    private String domain;
    private String path;
    private boolean secure;

    public Builder() {
    }

    public Builder(String name) {
      this.name = name;
    }

    /**
     * @param name the cookie name
     */
    public Builder setName(String name) {
      this.name = name;
      return this;
    }

    /**
     * Optional, defaults to {@code 0}
     * @see CookieSpec#version
     */
    public Builder setVersion(int version) {
      this.version = version;
      return this;
    }

    /**
     * Optional, defaults to end of browser session.
     * @see CookieSpec#maxAge
     */
    public Builder setMaxAge(TimeValue maxAge) {
      this.maxAge = maxAge;
      return this;
    }

    /**
     * Optional, defaults to host of current page/request.
     * @see CookieSpec#domain
     */
    public Builder setDomain(String domain) {
      this.domain = domain;
      return this;
    }

    /**
     * Optional, defaults to path of current page/request URI.
     * @see CookieSpec#path
     */
    public Builder setPath(String path) {
      this.path = path;
      return this;
    }

    /**
     * Optional, defaults to {@code false}.
     * @see CookieSpec#secure
     */
    public Builder setSecure(boolean secure) {
      this.secure = secure;
      return this;
    }

    public CookieSpec create() {
      return new CookieSpec(name, version, maxAge, domain, path, secure);
    }
  }
}
