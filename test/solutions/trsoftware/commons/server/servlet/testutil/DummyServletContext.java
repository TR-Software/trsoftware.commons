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

package solutions.trsoftware.commons.server.servlet.testutil;

import solutions.trsoftware.commons.server.io.SplitterOutputStream;

import javax.servlet.*;
import javax.servlet.descriptor.JspConfigDescriptor;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * @author Alex
 * @since 1/2/2018
 */
public class DummyServletContext extends DummyWebConfigObject<ServletContext> implements ServletContext {

  /**
   * Records messages received by {@link #log(String)} and {@link #log(String, Throwable)}.
   */
  private ByteArrayOutputStream logRecorder = new ByteArrayOutputStream();

  /**
   * Records messages received by {@link #log(String)} and {@link #log(String, Throwable)} to {@link #logRecorder}
   * and also prints them to {@link System#out}.
   */
  private PrintStream log = new PrintStream(new SplitterOutputStream(System.out, logRecorder));

  public DummyServletContext() {
  }

  public DummyServletContext(Map<String, String> initParameters) {
    super(initParameters);
  }

  /**
   * @return string containing all the log messages recorded by {@link #log(String)} and {@link #log(String, Throwable)}.
   */
  public String getLog() {
    return logRecorder.toString();
  }

  @Override
  public String getContextPath() {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.getContextPath has not been fully implemented yet.");
  }

  @Override
  public ServletContext getContext(String uripath) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.getContext has not been fully implemented yet.");
  }

  @Override
  public int getMajorVersion() {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.getMajorVersion has not been fully implemented yet.");
  }

  @Override
  public int getMinorVersion() {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.getMinorVersion has not been fully implemented yet.");
  }

  @Override
  public int getEffectiveMajorVersion() {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.getEffectiveMajorVersion has not been fully implemented yet.");
  }

  @Override
  public int getEffectiveMinorVersion() {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.getEffectiveMinorVersion has not been fully implemented yet.");
  }

  @Override
  public String getMimeType(String file) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.getMimeType has not been fully implemented yet.");
  }

  @Override
  public Set<String> getResourcePaths(String path) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.getResourcePaths has not been fully implemented yet.");
  }

  @Override
  public URL getResource(String path) throws MalformedURLException {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.getResource has not been fully implemented yet.");
  }

  @Override
  public InputStream getResourceAsStream(String path) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.getResourceAsStream has not been fully implemented yet.");
  }

  @Override
  public RequestDispatcher getRequestDispatcher(String path) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.getRequestDispatcher has not been fully implemented yet.");
  }

  @Override
  public RequestDispatcher getNamedDispatcher(String name) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.getNamedDispatcher has not been fully implemented yet.");
  }

  @Override
  public Servlet getServlet(String name) throws ServletException {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.getServlet has not been fully implemented yet.");
  }

  @Override
  public Enumeration<Servlet> getServlets() {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.getServlets has not been fully implemented yet.");
  }

  @Override
  public Enumeration<String> getServletNames() {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.getServletNames has not been fully implemented yet.");
  }

  @Override
  public void log(String msg) {
    logMessage(msg);
  }

  private void logMessage(String msg) {
    log.printf("[%s.log (%s)]: %s%n", getClass().getSimpleName(), new Date(), msg);
  }

  @Override
  public void log(String message, Throwable throwable) {
    logMessage(message);
    throwable.printStackTrace(log);
  }

  @Override
  public void log(Exception exception, String msg) {
    // this method is deprecated
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.log has not been fully implemented yet.");
  }

  @Override
  public String getRealPath(String path) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.getRealPath has not been fully implemented yet.");
  }

  @Override
  public String getServerInfo() {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.getServerInfo has not been fully implemented yet.");
  }

  @Override
  public boolean setInitParameter(String name, String value) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.setInitParameter has not been fully implemented yet.");
  }

  @Override
  public Object getAttribute(String name) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.getAttribute has not been fully implemented yet.");
  }

  @Override
  public Enumeration<String> getAttributeNames() {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.getAttributeNames has not been fully implemented yet.");
  }

  @Override
  public void setAttribute(String name, Object object) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.setAttribute has not been fully implemented yet.");
  }

  @Override
  public void removeAttribute(String name) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.removeAttribute has not been fully implemented yet.");
  }

  @Override
  public String getServletContextName() {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.getServletContextName has not been fully implemented yet.");
  }

  @Override
  public ServletRegistration.Dynamic addServlet(String servletName, String className) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.addServlet has not been fully implemented yet.");
  }

  @Override
  public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.addServlet has not been fully implemented yet.");
  }

  @Override
  public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.addServlet has not been fully implemented yet.");
  }

  @Override
  public <T extends Servlet> T createServlet(Class<T> c) throws ServletException {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.createServlet has not been fully implemented yet.");
  }

  @Override
  public ServletRegistration getServletRegistration(String servletName) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.getServletRegistration has not been fully implemented yet.");
  }

  @Override
  public Map<String, ? extends ServletRegistration> getServletRegistrations() {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.getServletRegistrations has not been fully implemented yet.");
  }

  @Override
  public FilterRegistration.Dynamic addFilter(String filterName, String className) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.addFilter has not been fully implemented yet.");
  }

  @Override
  public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.addFilter has not been fully implemented yet.");
  }

  @Override
  public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.addFilter has not been fully implemented yet.");
  }

  @Override
  public <T extends Filter> T createFilter(Class<T> c) throws ServletException {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.createFilter has not been fully implemented yet.");
  }

  @Override
  public FilterRegistration getFilterRegistration(String filterName) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.getFilterRegistration has not been fully implemented yet.");
  }

  @Override
  public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.getFilterRegistrations has not been fully implemented yet.");
  }

  @Override
  public SessionCookieConfig getSessionCookieConfig() {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.getSessionCookieConfig has not been fully implemented yet.");
  }

  @Override
  public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.setSessionTrackingModes has not been fully implemented yet.");
  }

  @Override
  public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.getDefaultSessionTrackingModes has not been fully implemented yet.");
  }

  @Override
  public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.getEffectiveSessionTrackingModes has not been fully implemented yet.");
  }

  @Override
  public void addListener(String className) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.addListener has not been fully implemented yet.");
  }

  @Override
  public <T extends EventListener> void addListener(T t) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.addListener has not been fully implemented yet.");
  }

  @Override
  public void addListener(Class<? extends EventListener> listenerClass) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.addListener has not been fully implemented yet.");
  }

  @Override
  public <T extends EventListener> T createListener(Class<T> c) throws ServletException {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.createListener has not been fully implemented yet.");
  }

  @Override
  public JspConfigDescriptor getJspConfigDescriptor() {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.getJspConfigDescriptor has not been fully implemented yet.");
  }

  @Override
  public ClassLoader getClassLoader() {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.getClassLoader has not been fully implemented yet.");
  }

  @Override
  public void declareRoles(String... roleNames) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.declareRoles has not been fully implemented yet.");
  }

  @Override
  public String getVirtualServerName() {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.getVirtualServerName has not been fully implemented yet.");
  }

  @Override
  public void setResponseCharacterEncoding(String encoding) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.setResponseCharacterEncoding has not been fully implemented yet.");
  }

  @Override
  public String getResponseCharacterEncoding() {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.getResponseCharacterEncoding has not been fully implemented yet.");
  }

  @Override
  public void setRequestCharacterEncoding(String encoding) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.setRequestCharacterEncoding has not been fully implemented yet.");
  }

  @Override
  public String getRequestCharacterEncoding() {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.getRequestCharacterEncoding has not been fully implemented yet.");
  }

  @Override
  public void setSessionTimeout(int sessionTimeout) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.setSessionTimeout has not been fully implemented yet.");
  }

  @Override
  public int getSessionTimeout() {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.getSessionTimeout has not been fully implemented yet.");
  }

  @Override
  public ServletRegistration.Dynamic addJspFile(String servletName, String jspFile) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyServletContext.addJspFile has not been fully implemented yet.");
  }
}
