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

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static solutions.trsoftware.commons.shared.util.CollectionUtils.first;

/**
 * Jul 8, 2009
 *
 * @author Alex
 */
public class DummyHttpServletResponse implements HttpServletResponse {
  private StringWriter output = new StringWriter();
  private int statusCode;
  private String statusMessage;
  private Multimap<String, Object> headers = LinkedHashMultimap.create();
  private List<Cookie> cookies = new ArrayList<>();
  private String contentType;

  public String getOutputAsString() {
    output.flush();
    return output.toString();
  }

  public int getStatus() {
    return statusCode;
  }

  @Override
  public String getHeader(String name) {
    if (headers.containsKey(name))
      return String.valueOf(first(headers.get(name)));
    return null;
  }

  @Override
  public Collection<String> getHeaders(String name) {
    return headers.get(name).stream().map(String::valueOf).collect(Collectors.toList());
  }

  @Override
  public Collection<String> getHeaderNames() {
    return headers.keySet();
  }

  public void setStatus(int i) {
    statusCode = i;
  }

  public String getStatusMessage() {
    return statusMessage;
  }

  public void setStatus(int i, String s) {
    // NOTE: this method is deprecated since Servlet API 2.1+
    statusCode = i;
    statusMessage = s;
  }

  @Override
  public void addCookie(Cookie cookie) {
    cookies.add(cookie);
  }

  public List<Cookie> getCookies() {
    return cookies;
  }

  @Override
  public boolean containsHeader(String name) {
    return headers.containsKey(name);
  }

  @Override
  public String encodeURL(String url) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletResponse.encodeURL has not been fully implemented yet.");
  }

  @Override
  public String encodeRedirectURL(String url) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletResponse.encodeRedirectURL has not been fully implemented yet.");
  }

  @Override
  public String encodeUrl(String url) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletResponse.encodeUrl has not been fully implemented yet.");
  }

  @Override
  public String encodeRedirectUrl(String url) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletResponse.encodeRedirectUrl has not been fully implemented yet.");
  }

  public void sendError(int i, String s) throws IOException {
    setStatus(i, s);
  }

  public void sendError(int i) throws IOException {
    sendError(i, "");
  }

  @Override
  public void sendRedirect(String location) throws IOException {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletResponse.sendRedirect has not been fully implemented yet.");
  }

  @Override
  public void setDateHeader(String name, long date) {
    replaceHeader(name, date);
  }

  @Override
  public void addDateHeader(String name, long date) {
    headers.put(name, date);
  }

  public void setHeader(String name, String value) {
    // this replaces any existing values for this header
    replaceHeader(name, value);
  }

  private void replaceHeader(String name, Object value) {
    headers.removeAll(name);
    headers.put(name, value);
  }

  @Override
  public void addHeader(String name, String value) {
    headers.put(name, value);
  }

  @Override
  public void setIntHeader(String name, int value) {
    replaceHeader(name, value);
  }

  @Override
  public void addIntHeader(String name, int value) {
    headers.put(name, value);
  }

  public Multimap<String, Object> getHeaders() {
    return headers;
  }

  @Override
  public String getCharacterEncoding() {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletResponse.getCharacterEncoding has not been fully implemented yet.");
  }

  @Override
  public String getContentType() {
    return contentType;
  }

  public ServletOutputStream getOutputStream() throws IOException {
    return new ServletOutputStream() {
      @Override
      public boolean isReady() {
        throw new UnsupportedOperationException("Method .isReady has not been fully implemented yet.");
      }

      @Override
      public void setWriteListener(WriteListener listener) {
        throw new UnsupportedOperationException("Method .setWriteListener has not been fully implemented yet.");
      }

      public void write(int b) throws IOException {
        output.write(b);
      }
    };
  }

  public PrintWriter getWriter() throws IOException {
    return new PrintWriter(output);
  }

  @Override
  public void setCharacterEncoding(String charset) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletResponse.setCharacterEncoding has not been fully implemented yet.");
  }

  @Override
  public void setContentLength(int len) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletResponse.setContentLength has not been fully implemented yet.");
  }

  @Override
  public void setContentLengthLong(long length) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletResponse.setContentLengthLong has not been fully implemented yet.");
  }

  @Override
  public void setContentType(String type) {
    this.contentType = type;
  }

  @Override
  public void setBufferSize(int size) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletResponse.setBufferSize has not been fully implemented yet.");
  }

  @Override
  public int getBufferSize() {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletResponse.getBufferSize has not been fully implemented yet.");
  }

  @Override
  public void flushBuffer() throws IOException {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletResponse.flushBuffer has not been fully implemented yet.");
  }

  @Override
  public void resetBuffer() {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletResponse.resetBuffer has not been fully implemented yet.");
  }

  @Override
  public boolean isCommitted() {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletResponse.isCommitted has not been fully implemented yet.");
  }

  @Override
  public void reset() {
    /* In compliance with Servlet spec:
       "Clears any data that exists in the buffer as well as the status code and
       headers."
     */
    output.getBuffer().setLength(0);  // see https://stackoverflow.com/questions/7168881/what-is-more-efficient-stringbuffer-new-or-delete0-sb-length
    statusCode = 0;
    statusMessage = null;  // NOTE: status message is deprecated since Servlet API 2.1+
    headers.clear();
  }

  @Override
  public void setLocale(Locale loc) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletResponse.setLocale has not been fully implemented yet.");
  }

  @Override
  public Locale getLocale() {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletResponse.getLocale has not been fully implemented yet.");
  }


}
