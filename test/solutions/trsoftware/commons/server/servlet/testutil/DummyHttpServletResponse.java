package solutions.trsoftware.commons.server.servlet.testutil;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * Jul 8, 2009
 *
 * @author Alex
 */
public class DummyHttpServletResponse implements HttpServletResponse {
  private StringWriter output = new StringWriter();
  private int statusCode;
  private String statusMessage;
  final HashMap<String,String> responseHeaders = new HashMap<String, String>();
  private List<Cookie> cookies = new ArrayList<>();
  private String contentType;

  public String getOutput() {
    output.flush();
    return output.toString();
  }

  public int getStatus() {
    return statusCode;
  }

  @Override
  public String getHeader(String name) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletResponse.getHeader has not been fully implemented yet.");
  }

  @Override
  public Collection<String> getHeaders(String name) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletResponse.getHeaders has not been fully implemented yet.");
  }

  @Override
  public Collection<String> getHeaderNames() {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletResponse.getHeaderNames has not been fully implemented yet.");
  }

  public void setStatus(int i) {
    statusCode = i;
  }

  public String getStatusMessage() {
    return statusMessage;
  }

  public void setStatus(int i, String s) {
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
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletResponse.containsHeader has not been fully implemented yet.");
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
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletResponse.setDateHeader has not been fully implemented yet.");
  }

  @Override
  public void addDateHeader(String name, long date) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletResponse.addDateHeader has not been fully implemented yet.");
  }

  public void setHeader(String name, String value) {
    responseHeaders.put(name, value);
  }

  @Override
  public void addHeader(String name, String value) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletResponse.addHeader has not been fully implemented yet.");
  }

  @Override
  public void setIntHeader(String name, int value) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletResponse.setIntHeader has not been fully implemented yet.");
  }

  @Override
  public void addIntHeader(String name, int value) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletResponse.addIntHeader has not been fully implemented yet.");
  }

  public HashMap<String, String> getResponseHeaders() {
    return responseHeaders;
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
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletResponse.reset has not been fully implemented yet.");
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
