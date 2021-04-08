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

package solutions.trsoftware.commons.shared.web;

import solutions.trsoftware.commons.client.bridge.util.URIComponentEncoder;
import solutions.trsoftware.commons.shared.util.TimeUnit;
import solutions.trsoftware.commons.shared.util.TimeValue;

import javax.servlet.http.Cookie;

/**
 * Encapsulates all the info needed to set a particular cookie.
 * <p>
 * This is an immutable version of {@link javax.servlet.http.Cookie}, intended to be used as a constant.
 * For example, a servlet that needs to set a particular cookie could use a {@link CookieSpec} constant, call
 * {@link #toCookie()} on it, followed by setting the value:
 * <pre>{@code
 *   private void setCookie(CookieSpec spec, String cookieValue, HttpServletResponse response) {
 *     Cookie cookie = cookieSpec.toCookie();
 *     cookie.setValue(cookieValue);
 *     response.addCookie(cookie);
 *   }
 *
 * }</pre>
 *
 * @see Cookie
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
   * Value (data) of the cookie. If you use a binary value, you may want to use BASE64 encoding.
   * <p>
   * With Version 0 cookies, values should not contain white space, brackets,
   * parentheses, equals signs, commas, double quotes, slashes, question
   * marks, at signs, colons, and semicolons. Empty values may not behave the
   * same way on all browsers.
   * <p>
   * If the cookie might be read client-side with {@link com.google.gwt.user.client.Cookies}, the value
   * should be escaped with {@link URIComponentEncoder#encode(String)}
   *
   * @see Cookie#setValue(String)
   */
  private String value;

  /**
   * The version of the cookie protocol this cookie complies with:
   * <ol start=0>
   *   <li>version 0 complies with the original Netscape cookie specification.</li>
   *   <li>version 1 complies with <a href="https://www.ietf.org/rfc/rfc2109.txt">RFC 2109</a></li>
   * </ol>
   * @see Cookie#setVersion(int)
   */
  private int version;

  /**
   * How long the cookie should persist on the client.
   * If {@code null}, cookie will be removed after browser shutdown.
   * @see Cookie#getMaxAge()
   */
  private TimeValue maxAge;

  /**
   * Specifies the domain within which this cookie should be presented.
   * Defaults to the current request host if {@code null}.
   * @see Cookie#setDomain(java.lang.String)
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
   * Flag that controls if this cookie will be hidden from scripts on the client side.
   * <p>
   * NOTE: this option is not part of RFC 2109, but most browsers do support it
   */
  private boolean httpOnly;

  /**
   * NOTE: might be easier to use {@link Builder} instead of this constructor.
   *
   * @param name {@link #name}
   * @param value {@link #value}
   * @param version {@link #version}
   * @param maxAge {@link #maxAge}
   * @param domain {@link #domain}
   * @param path {@link #path}
   * @param secure {@link #secure}
   * @param httpOnly {@link #httpOnly}
   * @see #builder(String)
   * @see #builder()
   */
  public CookieSpec(String name, String value, int version, TimeValue maxAge, String domain, String path, boolean secure, boolean httpOnly) {
    this.name = name;
    this.value = value;
    this.version = version;
    this.maxAge = maxAge;
    this.domain = domain;
    this.path = path;
    this.secure = secure;
    this.httpOnly = httpOnly;
  }

  /**
   * @param name the cookie name
   */
  public static Builder builder(String name) {
    return new Builder(name);
  }

  /**
   * @return a builder to create a copy of this {@link CookieSpec}, with some fields possibly modified.
   */
  public Builder builder() {
    return new Builder(this);
  }

  /**
   * @return a new instance of {@link Cookie}, initialized from this object.
   */
  public Cookie toCookie() {
    Cookie cookie = new Cookie(name, value);
    cookie.setVersion(version);
    if (maxAge != null)
      cookie.setMaxAge((int)maxAge.to(TimeUnit.SECONDS).getValue());
    if (domain != null)
      cookie.setDomain(domain);
    if (path != null)
      cookie.setPath(path);
    cookie.setSecure(secure);
    cookie.setHttpOnly(httpOnly);
    return cookie;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
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

  public boolean isHttpOnly() {
    return httpOnly;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    CookieSpec that = (CookieSpec)o;

    if (version != that.version)
      return false;
    if (secure != that.secure)
      return false;
    if (httpOnly != that.httpOnly)
      return false;
    if (name != null ? !name.equals(that.name) : that.name != null)
      return false;
    if (value != null ? !value.equals(that.value) : that.value != null)
      return false;
    if (maxAge != null ? !maxAge.equals(that.maxAge) : that.maxAge != null)
      return false;
    if (domain != null ? !domain.equals(that.domain) : that.domain != null)
      return false;
    return path != null ? path.equals(that.path) : that.path == null;
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (value != null ? value.hashCode() : 0);
    result = 31 * result + version;
    result = 31 * result + (maxAge != null ? maxAge.hashCode() : 0);
    result = 31 * result + (domain != null ? domain.hashCode() : 0);
    result = 31 * result + (path != null ? path.hashCode() : 0);
    result = 31 * result + (secure ? 1 : 0);
    result = 31 * result + (httpOnly ? 1 : 0);
    return result;
  }

  /**
   * Builder for a {@link CookieSpec} instance.
   * @see #builder()
   * @see #builder(String)
   */
  public static class Builder {
    private String name;
    private String value;
    private int version;
    private TimeValue maxAge;
    private String domain;
    private String path;
    private boolean secure;
    private boolean httpOnly;

    public Builder() {
    }

    public Builder(String name) {
      this.name = name;
    }

    /**
     * Inits builder to match the given spec.
     */
    public Builder(CookieSpec spec) {
      name = spec.name;
      version = spec.version;
      maxAge = spec.maxAge;
      domain = spec.domain;
      path = spec.path;
      secure = spec.secure;
      httpOnly = spec.httpOnly;
    }

    /**
     * @param name the cookie name
     * @see CookieSpec#name
     */
    public Builder setName(String name) {
      this.name = name;
      return this;
    }

    /**
     * @param value the cookie value
     * @see CookieSpec#value
     */
    public Builder setValue(String value) {
      this.value = value;
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

    /**
     * Optional flag that controls if this cookie will be hidden from scripts on the client side.
     * @see CookieSpec#httpOnly
     */
    public Builder setHttpOnly(boolean httpOnly) {
      this.httpOnly = httpOnly;
      return this;
    }

    public CookieSpec create() {
      return new CookieSpec(name, value, version, maxAge, domain, path, secure, httpOnly);
    }
  }
}
