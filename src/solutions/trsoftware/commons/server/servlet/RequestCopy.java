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

package solutions.trsoftware.commons.server.servlet;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.*;

/**
 * Saves the parts of the underlying HttpServletRequest to make them available after the response has been committed
 * (e.g. from an asynchronous thread). We can't save the underlying request in a ThreadLocal - it's highly unsafe
 * because Tomcat will reuse the underlying object after the response has been comitted.
 *
 * Nov 14, 2009
 *
 * @author Alex
 */
public class RequestCopy implements HttpServletRequest {
  private static final String UOE_MSG = "RequestCopy doesn't support this method.";

  private String remoteAddr;
  private String queryString;
  private Locale locale;
  private Map<String, String[]> params;
  private String method;
  private StringBuffer requestURL;

  public RequestCopy(HttpServletRequest originalRequest) {
    remoteAddr = originalRequest.getRemoteAddr();
    queryString = originalRequest.getQueryString();
    locale = originalRequest.getLocale();
    params = originalRequest.getParameterMap();
    method = originalRequest.getMethod();
    requestURL = new StringBuffer(originalRequest.getRequestURL());  // defensive copy, since the original buffer likely to get reused
  }

  public Object getAttribute(String s) {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public Enumeration getAttributeNames() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public String getCharacterEncoding() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public void setCharacterEncoding(String s) throws UnsupportedEncodingException {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public int getContentLength() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  @Override
  public long getContentLengthLong() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public String getContentType() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public ServletInputStream getInputStream() throws IOException {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public String getParameter(String s) {
    return params.get(s)[0];
  }

  public Enumeration getParameterNames() {
    return Collections.enumeration(params.keySet());
  }

  public String[] getParameterValues(String s) {
    return params.get(s);
  }

  public Map getParameterMap() {
    return params;
  }

  public String getProtocol() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public String getScheme() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public String getServerName() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public int getServerPort() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public BufferedReader getReader() throws IOException {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public String getRemoteAddr() {
    return remoteAddr;
  }

  public String getRemoteHost() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public void setAttribute(String s, Object o) {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public void removeAttribute(String s) {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public Locale getLocale() {
    return locale;
  }

  public Enumeration getLocales() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public boolean isSecure() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public RequestDispatcher getRequestDispatcher(String s) {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public String getRealPath(String s) {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public int getRemotePort() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public String getLocalName() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public String getLocalAddr() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public int getLocalPort() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  @Override
  public ServletContext getServletContext() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  @Override
  public AsyncContext startAsync() throws IllegalStateException {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  @Override
  public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  @Override
  public boolean isAsyncStarted() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  @Override
  public boolean isAsyncSupported() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  @Override
  public AsyncContext getAsyncContext() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  @Override
  public DispatcherType getDispatcherType() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public String getAuthType() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public Cookie[] getCookies() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public long getDateHeader(String s) {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public String getHeader(String s) {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public Enumeration getHeaders(String s) {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public Enumeration getHeaderNames() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public int getIntHeader(String s) {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public String getMethod() {
    return method;
  }

  public String getPathInfo() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public String getPathTranslated() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public String getContextPath() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public String getQueryString() {
    return queryString;
  }

  public String getRemoteUser() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public boolean isUserInRole(String s) {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public Principal getUserPrincipal() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public String getRequestedSessionId() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public String getRequestURI() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public StringBuffer getRequestURL() {
    return requestURL;
  }

  public String getServletPath() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public HttpSession getSession(boolean b) {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public HttpSession getSession() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  @Override
  public String changeSessionId() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public boolean isRequestedSessionIdValid() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public boolean isRequestedSessionIdFromCookie() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public boolean isRequestedSessionIdFromURL() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  public boolean isRequestedSessionIdFromUrl() {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  @Override
  public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  @Override
  public void login(String username, String password) throws ServletException {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  @Override
  public void logout() throws ServletException {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  @Override
  public Collection<Part> getParts() throws IOException, ServletException {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  @Override
  public Part getPart(String name) throws IOException, ServletException {
    throw new UnsupportedOperationException(UOE_MSG);
  }

  @Override
  public <T extends HttpUpgradeHandler> T upgrade(Class<T> httpUpgradeHandlerClass) throws IOException, ServletException {
    throw new UnsupportedOperationException(UOE_MSG);
  }

}
