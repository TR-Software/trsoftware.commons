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

package solutions.trsoftware.commons.server.servlet.testutil;

import com.google.common.collect.*;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.Nullable;
import solutions.trsoftware.commons.server.servlet.HttpHeaders;
import solutions.trsoftware.commons.shared.util.MapUtils;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.*;

import static java.util.Collections.enumeration;
import static java.util.Collections.singletonList;
import static solutions.trsoftware.commons.shared.util.CollectionUtils.isEmpty;


/**
 * Date: Jul 17, 2007
 * Time: 6:53:01 PM
 *
 * @author Alex
 */
public class DummyHttpServletRequest implements HttpServletRequest {

  private HttpSession session;
  private String requestedSessionId;
  private String uri;
  private String url;
  private String queryString;
  private final Multimap<String, String> paramMap = LinkedHashMultimap.create();
  private String remoteAddr = "127.0.0.1";
  private List<Locale> locales;
  private String method;
  private List<Cookie> cookies = new ArrayList<>();
  private Map<String, Object> attributes = new LinkedHashMap<>();
  private HttpHeaders headers = new HttpHeaders(ArrayListMultimap.create());  // start with empty multimap for headers

  public DummyHttpServletRequest() {
  }

  public DummyHttpServletRequest(Map<String, String> paramMap) {
    this(null, null, paramMap);
  }

  public DummyHttpServletRequest(Multimap<String, String> paramMap) {
    this(null, null, paramMap);
  }

  public DummyHttpServletRequest(String url, String queryString) {
    this.url = url;
    this.queryString = queryString;
  }

  public DummyHttpServletRequest(String url, String queryString, Map<String, String> paramMap) {
    this(url, queryString);
    MapUtils.putAllToMultimap(this.paramMap, paramMap);
  }

  public DummyHttpServletRequest(String url, String queryString, Multimap<String, String> paramMap) {
    this(url, queryString);
    this.paramMap.putAll(paramMap);
  }

  public DummyHttpServletRequest(String method, String url, String queryString, Map<String, String> paramMap) {
    this(url, queryString, paramMap);
    this.method = method;
  }

  public DummyHttpServletRequest(String method, String url, String queryString, Multimap<String, String> paramMap) {
    this(url, queryString, paramMap);
    this.method = method;
  }

  public DummyHttpServletRequest(HttpSession session) {
    this.session = session;
  }

  public DummyHttpServletRequest(String url, SortedMap<String, String> params) {
    this(url, null, params);
  }

  public DummyHttpServletRequest setQueryString(String queryString) {
    this.queryString = queryString;
    return this;
  }

  public DummyHttpServletRequest setMethod(String method) {
    this.method = method;
    return this;
  }

  public DummyHttpServletRequest setSession(HttpSession session) {
    this.session = session;
    return this;
  }

  public DummyHttpServletRequest setRequestURL(String url) {
    this.url = url;
    return this;
  }

  public DummyHttpServletRequest setRequestURI(String uri) {
    this.uri = uri;
    return this;
  }

  public DummyHttpServletRequest setRemoteAddr(String remoteAddr) {
    this.remoteAddr = remoteAddr;
    return this;
  }

  public DummyHttpServletRequest addCookie(Cookie cookie) {
    cookies.add(cookie);
    return this;
  }

  public String getAuthType() {
    System.err.println("Method DummyHttpServletRequest.getAuthType has not been fully implemented yet.");
    return null;
  }

  public Cookie[] getCookies() {
    return cookies.toArray(new Cookie[cookies.size()]);
  }

  public DummyHttpServletRequest setHeaders(Multimap<String, String> headers) {
    this.headers = new HttpHeaders(headers);
    return this;
  }

  @Override
  public long getDateHeader(String name) {
    return headers.getDateHeader(name);
  }

  @Override
  public String getHeader(String name) {
    return headers.getHeader(name);
  }

  @Override
  public Enumeration<String> getHeaders(String name) {
    return headers.getHeaders(name);
  }

  @Override
  public Enumeration<String> getHeaderNames() {
    return headers.getHeaderNames();
  }

  @Override
  public int getIntHeader(String name) {
    return headers.getIntHeader(name);
  }

  public Multimap<String, String> getHeadersAsMultimap() {
    return headers.getHeadersAsMultimap();
  }

  @Override
  public String getMethod() {
    return method;
  }

  @Override
  public String getPathInfo() {
    System.err.println("Method DummyHttpServletRequest.getPathInfo has not been fully implemented yet.");
    return null;
  }

  @Override
  public String getPathTranslated() {
    System.err.println("Method DummyHttpServletRequest.getPathTranslated has not been fully implemented yet.");
    return null;
  }

  @Override
  public String getContextPath() {
    System.err.println("Method DummyHttpServletRequest.getContextPath has not been fully implemented yet.");
    return null;
  }

  @Override
  public String getQueryString() {
    return queryString;
  }

  @Override
  public String getRemoteUser() {
    System.err.println("Method DummyHttpServletRequest.getRemoteUser has not been fully implemented yet.");
    return null;
  }

  @Override
  public boolean isUserInRole(String val) {
    System.err.println("Method DummyHttpServletRequest.isUserInRole has not been fully implemented yet.");
    return false;
  }

  @Override
  public Principal getUserPrincipal() {
    System.err.println("Method DummyHttpServletRequest.getUserPrincipal has not been fully implemented yet.");
    return null;
  }

  @Override
  public String getRequestedSessionId() {
    return requestedSessionId;
  }

  public DummyHttpServletRequest setRequestedSessionId(String requestedSessionId) {
    this.requestedSessionId = requestedSessionId;
    return this;
  }

  @Override
  public String getRequestURI() {
    return uri;
  }

  @Override
  public StringBuffer getRequestURL() {
    return new StringBuffer(url != null ? url : ""); 
  }

  @Override
  public String getServletPath() {
    System.err.println("Method DummyHttpServletRequest.getServletPath has not been fully implemented yet.");
    return null;
  }

  @Override
  public HttpSession getSession(boolean create) {
    if (session == null && create)
      session = new DummyHttpSession();
    return session;
  }

  @Override
  public HttpSession getSession() {
    return getSession(true);
  }

  @Override
  public String changeSessionId() {
    System.err.println("Method DummyHttpServletRequest.changeSessionId has not been fully implemented yet.");
    return null;
  }

  @Override
  public boolean isRequestedSessionIdValid() {
    System.err.println("Method DummyHttpServletRequest.isRequestedSessionIdValid has not been fully implemented yet.");
    return false;
  }

  @Override
  public boolean isRequestedSessionIdFromCookie() {
    System.err.println("Method DummyHttpServletRequest.isRequestedSessionIdFromCookie has not been fully implemented yet.");
    return false;
  }

  @Override
  public boolean isRequestedSessionIdFromURL() {
    System.err.println("Method DummyHttpServletRequest.isRequestedSessionIdFromURL has not been fully implemented yet.");
    return false;
  }

  @Override
  public boolean isRequestedSessionIdFromUrl() {
    System.err.println("Method DummyHttpServletRequest.isRequestedSessionIdFromUrl has not been fully implemented yet.");
    return false;
  }

  @Override
  public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
    System.err.println("Method DummyHttpServletRequest.authenticate has not been fully implemented yet.");
    return false;
  }

  @Override
  public void login(String username, String password) throws ServletException {
    System.err.println("Method DummyHttpServletRequest.login has not been fully implemented yet.");

  }

  @Override
  public void logout() throws ServletException {
    System.err.println("Method DummyHttpServletRequest.logout has not been fully implemented yet.");

  }

  @Override
  public Collection<Part> getParts() throws IOException, ServletException {
    System.err.println("Method DummyHttpServletRequest.getParts has not been fully implemented yet.");
    return null;
  }

  @Override
  public Part getPart(String name) throws IOException, ServletException {
    System.err.println("Method DummyHttpServletRequest.getPart has not been fully implemented yet.");
    return null;
  }

  @Override
  public <T extends HttpUpgradeHandler> T upgrade(Class<T> httpUpgradeHandlerClass) throws IOException, ServletException {
    System.err.println("Method DummyHttpServletRequest.upgrade has not been fully implemented yet.");
    return null;
  }

  @Override
  public Object getAttribute(String name) {
    return attributes.get(name);
  }

  @Override
  public Enumeration<String> getAttributeNames() {
    return enumeration(attributes.keySet());
  }

  @Override
  public String getCharacterEncoding() {
    System.err.println("Method DummyHttpServletRequest.getCharacterEncoding has not been fully implemented yet.");
    return null;
  }

  @Override
  public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
    System.err.println("Method DummyHttpServletRequest.setCharacterEncoding has not been fully implemented yet.");
  }

  @Override
  public int getContentLength() {
    System.err.println("Method DummyHttpServletRequest.getContentLength has not been fully implemented yet.");
    return 0;
  }

  @Override
  public long getContentLengthLong() {
    System.err.println("Method DummyHttpServletRequest.getContentLengthLong has not been fully implemented yet.");
    return 0;
  }

  @Override
  public String getContentType() {
    System.err.println("Method DummyHttpServletRequest.getContentType has not been fully implemented yet.");
    return null;
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    System.err.println("Method DummyHttpServletRequest.getInputStream has not been fully implemented yet.");
    return null;
  }

  @Override
  public String getParameter(String name) {
    return Iterables.getFirst(paramMap.get(name), null);
  }

  /**
   * Removes all values of the given request parameter.
   *
   * @return the values that were removed (possibly empty)
   */
  public Collection<String> removeParameter(String name) {
    return paramMap.removeAll(name);
  }

  /**
   * Adds a new request parameter mapping.
   *
   * @return {@code true} if the parameter multimap was changed as a result,
   *     or {@code false} if the given mapping already existed
   */
  public boolean putParameter(String name, String value) {
    return paramMap.put(name, value);
  }

  /**
   * Replaces any existing values for the given parameter with the given values.
   * @return the (possibly empty) collection of the previous values of this parameter
   */
  @CanIgnoreReturnValue
  public Collection<String> replaceParameterValues(@Nullable String name, Iterable<? extends String> newValues) {
    return paramMap.replaceValues(name, newValues);
  }

  /**
   * Replaces any existing values for the given parameter with the given single value.
   * @return the (possibly empty) collection of the previous values of this parameter
   */
  @CanIgnoreReturnValue
  public Collection<String> replaceParameterValues(@Nullable String name, String... newValues) {
    return paramMap.replaceValues(name, Arrays.asList(newValues));
  }

  @Override
  public Enumeration<String> getParameterNames() {
    return enumeration(paramMap.keySet());
  }

  @Override
  public String[] getParameterValues(String name) {
    if (paramMap.containsKey(name))
      return paramMap.get(name).toArray(new String[0]);
    return null;
  }

  @Override
  public Map<String, String[]> getParameterMap() {
    // must return a map with array values to match the Servlet API spec
    ImmutableMap.Builder<String, String[]> builder = ImmutableMap.builder();
    for (String name : paramMap.keySet()) {
      builder.put(name, getParameterValues(name));
    }
    return builder.build();
  }

  @Override
  public String getProtocol() {
    System.err.println("Method DummyHttpServletRequest.getProtocol has not been fully implemented yet.");
    return null;
  }

  @Override
  public String getScheme() {
    System.err.println("Method DummyHttpServletRequest.getScheme has not been fully implemented yet.");
    return null;
  }

  @Override
  public String getServerName() {
    System.err.println("Method DummyHttpServletRequest.getServerName has not been fully implemented yet.");
    return null;
  }

  @Override
  public int getServerPort() {
    System.err.println("Method DummyHttpServletRequest.getServerPort has not been fully implemented yet.");
    return 0;
  }

  @Override
  public BufferedReader getReader() throws IOException {
    System.err.println("Method DummyHttpServletRequest.getReader has not been fully implemented yet.");
    return null;
  }

  @Override
  public String getRemoteAddr() {
    return remoteAddr;
  }

  @Override
  public String getRemoteHost() {
    System.err.println("Method DummyHttpServletRequest.getRemoteHost has not been fully implemented yet.");
    return null;
  }

  @Override
  public void setAttribute(String name, Object object) {
    // setting a null value is the same as removeAttribute (see org.apache.catalina.connector.Request.setAttribute)
    if (object == null)
      removeAttribute(name);
    else
      attributes.put(name, object);
  }

  @Override
  public void removeAttribute(String name) {
    attributes.remove(name);
  }

  @Override
  public Locale getLocale() {
    if (!isEmpty(locales))
      return locales.get(0);
    else
      return Locale.getDefault();
  }

  @Override
  public Enumeration<Locale> getLocales() {
    if (!isEmpty(locales))
      return enumeration(locales);
    else
      return enumeration(singletonList(Locale.getDefault()));
  }

  public DummyHttpServletRequest setLocales(List<Locale> locales) {
    this.locales = locales;
    return this;
  }

  public DummyHttpServletRequest setLocale(Locale locale) {
    this.locales = singletonList(locale);
    return this;
  }

  @Override
  public boolean isSecure() {
    System.err.println("Method DummyHttpServletRequest.isSecure has not been fully implemented yet.");
    return false;
  }

  @Override
  public RequestDispatcher getRequestDispatcher(String path) {
    System.err.println("Method DummyHttpServletRequest.getRequestDispatcher has not been fully implemented yet.");
    return null;
  }

  @Override
  public String getRealPath(String val) {
    System.err.println("Method DummyHttpServletRequest.getRealPath has not been fully implemented yet.");
    return null;
  }

  @Override
  public int getRemotePort() {
    System.err.println("Method DummyHttpServletRequest.getRemotePort has not been fully implemented yet.");
    return 0;
  }

  @Override
  public String getLocalName() {
    System.err.println("Method DummyHttpServletRequest.getLocalName has not been fully implemented yet.");
    return null;
  }

  @Override
  public String getLocalAddr() {
    System.err.println("Method DummyHttpServletRequest.getLocalAddr has not been fully implemented yet.");
    return null;
  }

  @Override
  public int getLocalPort() {
    System.err.println("Method DummyHttpServletRequest.getLocalPort has not been fully implemented yet.");
    return 0;
  }

  @Override
  public ServletContext getServletContext() {
    System.err.println("Method DummyHttpServletRequest.getServletContext has not been fully implemented yet.");
    return null;
  }

  @Override
  public AsyncContext startAsync() throws IllegalStateException {
    System.err.println("Method DummyHttpServletRequest.startAsync has not been fully implemented yet.");
    return null;
  }

  @Override
  public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
    System.err.println("Method DummyHttpServletRequest.startAsync has not been fully implemented yet.");
    return null;
  }

  @Override
  public boolean isAsyncStarted() {
    System.err.println("Method DummyHttpServletRequest.isAsyncStarted has not been fully implemented yet.");
    return false;
  }

  @Override
  public boolean isAsyncSupported() {
    System.err.println("Method DummyHttpServletRequest.isAsyncSupported has not been fully implemented yet.");
    return false;
  }

  @Override
  public AsyncContext getAsyncContext() {
    System.err.println("Method DummyHttpServletRequest.getAsyncContext has not been fully implemented yet.");
    return null;
  }

  @Override
  public DispatcherType getDispatcherType() {
    System.err.println("Method DummyHttpServletRequest.getDispatcherType has not been fully implemented yet.");
    return null;
  }

}
