/*
 * Copyright 2022 TR Software Inc.
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

import com.google.common.base.MoreObjects;
import solutions.trsoftware.commons.client.bridge.util.URIComponentEncoder;
import solutions.trsoftware.commons.shared.util.LogicUtils;
import solutions.trsoftware.commons.shared.util.TimeUnit;
import solutions.trsoftware.commons.shared.util.TimeValue;

import javax.annotation.Nullable;
import javax.servlet.http.Cookie;
import java.util.Date;
import java.util.Objects;

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
   * Default value for {@link #maxAge}: expire cookie at the end of browser session.
   * This is the same default value as {@link javax.servlet.http.Cookie#maxAge}
   */
  private static final int DEFAULT_MAX_AGE = -1;

  /**
   * Name of the cookie.
   */
  @Nullable
  private final String name;

  /**
   * Value (data) of the cookie. If you use a binary value, you may want to use BASE64 encoding.
   * <p>
   * With Version 0 cookies, values should not contain white space, brackets,
   * parentheses, equals signs, commas, double quotes, slashes, question
   * marks, @ symbols, colons, and semicolons. Empty values may not behave the
   * same way on all browsers.
   * <p>
   * If the cookie might be read client-side with {@link com.google.gwt.user.client.Cookies}, the value
   * should be escaped with {@link URIComponentEncoder#encode(String)}
   *
   * @see Cookie#setValue(String)
   */
  @Nullable
  private final String value;

  /**
   * The version of the cookie protocol this cookie complies with:
   * <ol start=0>
   *   <li>version 0 complies with the original Netscape cookie specification.</li>
   *   <li>version 1 complies with <a href="https://www.ietf.org/rfc/rfc2109.txt">RFC 2109</a></li>
   * </ol>
   * @see Cookie#setVersion(int)
   */
  private final int version;

  /**
   * How long the cookie should persist on the client, in seconds.
   * <p>
   * A positive value indicates that the cookie will expire after that many
   * seconds have passed. Note that the value is the <i>maximum</i> age when
   * the cookie will expire, not the cookie's current age.
   * <p>
   * A negative value means that the cookie is not stored persistently and
   * will be deleted when the Web browser exits.
   * <p>
   * A zero value causes the cookie to be deleted.
   *
   * @see Cookie#setMaxAge(int)
   * @see Cookie#getMaxAge()
   */
  // Note(9/4/2025): field type changed from TimeValue to int, to allow the special meanings for 0 and -1
  private final int maxAge;

  /**
   * Specifies the domain within which this cookie should be presented.
   * Defaults to the current request host if {@code null}.
   * @see Cookie#setDomain(String)
   */
  @Nullable
  private final String domain;

  /**
   * Specifies the subset of URLs to which this cookie applies.
   * <ul>
   *   <li>
   *     If not explicitly specified (i.e. {@code null}), defaults to the path of the current request URL,
   *     up to, but not including, the right-most slash
   *   </li>
   *   <li>
   *     Must be a prefix of the current request URI (otherwise rejected)
   *   </li>
   *   <li>
   *     Value of {@code "/"} makes the cookie apply to all paths in the domain
   *   </li>
   * </ul>
   */
  @Nullable
  private final String path;

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
  private final boolean secure;

  /**
   * Flag that controls if this cookie will be hidden from scripts on the client side.
   * <p>
   * NOTE: this option is not part of RFC 2109, but most browsers do support it
   */
  private final boolean httpOnly;

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
  private CookieSpec(String name, String value, int version, int maxAge, String domain, String path, boolean secure, boolean httpOnly) {
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

  @Nullable
  public String getName() {
    return name;
  }

  @Nullable
  public String getValue() {
    return value;
  }

  public int getVersion() {
    return version;
  }

  @Nullable
  public int getMaxAge() {
    return maxAge;
  }

  /**
   * @param currentTimeMillis time in epoch millis
   * @return a {@link Date} constructed by adding {@link #maxAge} to the current time,
   *     or {@code null} if {@link #maxAge} &le; {@code 0}
   */
  @Nullable
  public Date getExpirationDate(long currentTimeMillis) {
    // Note: this method is provided to support GWT's Cookies.setCookie method, which takes an expiration Date instead of maxAge
    return maxAge > 0 ? new Date(currentTimeMillis + maxAge * 1000L) : null;
  }

  @Nullable
  public String getDomain() {
    return domain;
  }

  @Nullable
  public String getPath() {
    return path;
  }

  public boolean isSecure() {
    return secure;
  }

  public boolean isHttpOnly() {
    return httpOnly;
  }

  /**
   * @return a new instance of {@link Cookie}, initialized from this object.
   */
  public Cookie toCookie() {
    Cookie cookie = new Cookie(name, value);
    cookie.setVersion(version);
    cookie.setMaxAge(maxAge);
    if (domain != null)
      cookie.setDomain(domain);
    if (path != null)
      cookie.setPath(path);
    cookie.setSecure(secure);
    cookie.setHttpOnly(httpOnly);
    return cookie;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    CookieSpec that = (CookieSpec)o;
    return version == that.version &&
        maxAge == that.maxAge &&
        secure == that.secure &&
        httpOnly == that.httpOnly &&
        LogicUtils.eq(name, that.name) &&
        LogicUtils.eq(value, that.value) &&
        LogicUtils.eq(domain, that.domain) &&
        LogicUtils.eq(path, that.path);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, value, version, maxAge, domain, path, secure, httpOnly);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("name", name)
        .add("value", value)
        .add("version", version)
        .add("maxAge", maxAge)
        .add("domain", domain)
        .add("path", path)
        .add("secure", secure)
        .add("httpOnly", httpOnly)
        .toString();
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
    private int maxAge = DEFAULT_MAX_AGE;
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
     * Optional, defaults to {@code 0}.
     * <p>
     * <em>Note:</em> IE7 & Safari don't recognize expiration dates in version 1, and hence don't retain the cookies past the session
     * @see CookieSpec#version
     */
    public Builder setVersion(int version) {
      this.version = version;
      return this;
    }

    /**
     * Sets {@link CookieSpec#maxAge}.
     * <p>
     * A positive value indicates that the cookie will expire after that many
     * seconds have passed. Note that the value is the <i>maximum</i> age when
     * the cookie will expire, not the cookie's current age.
     * <p>
     * A negative value means that the cookie is not stored persistently and
     * will be deleted when the Web browser exits.
     * <p>
     * A zero value causes the cookie to be deleted.
     * @param maxAge the maximum age of the cookie in seconds
     * @see Cookie#setMaxAge(int)
     */
    public Builder setMaxAge(int maxAge) {
      this.maxAge = maxAge;
      return this;
    }

    /**
     * Optional, defaults to end of browser session.
     * @see Cookie#setMaxAge(int)
     * @deprecated use {@link #setMaxAge(int)}
     */
    public Builder setMaxAge(TimeValue timeValue) {
      this.maxAge = timeValue != null ? (int)timeValue.to(TimeUnit.SECONDS).getValue() : DEFAULT_MAX_AGE;
      return this;
    }

    /**
     * Optional, defaults to end of browser session.
     * @param maxAgeMillis the max age specified in milliseconds (should be a positive integer; otherwise
     *  will set {@link #maxAge} to {@code -1} if negative)
     * @see Cookie#setMaxAge(int)
     * @see #setMaxAge(TimeValue)
     * @deprecated use {@link #setMaxAge(int)}
     */
    public Builder setMaxAgeMillis(long maxAgeMillis) {
      if (maxAgeMillis > 0)
        maxAge = (int)(maxAgeMillis / 1000L);
      // Note: this preserves the special meanings of maxAge 0 and -1
      else if (maxAgeMillis < 0)
        maxAge = -1;
      else
        maxAge = 0;
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

    public CookieSpec build() {
      return new CookieSpec(name, value, version, maxAge, domain, path, secure, httpOnly);
    }
  }
}
