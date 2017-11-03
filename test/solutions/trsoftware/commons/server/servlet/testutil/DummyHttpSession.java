package solutions.trsoftware.commons.server.servlet.testutil;

import solutions.trsoftware.commons.client.util.StringUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Date: Aug 14, 2008 Time: 5:59:48 PM
 *
 * @author Alex
 */
public class DummyHttpSession implements HttpSession {

  Map<String, Object> attributeMap = new HashMap<String, Object>();

  private final String sessionId = "DummySession_" + StringUtils.randString(8);

  public long getCreationTime() {
    System.err.println("Method DummyHttpSession.getCreationTime has not been fully implemented yet.");
    return 0;
  }

  public String getId() {
    return sessionId; 
  }

  public long getLastAccessedTime() {
    System.err.println("Method DummyHttpSession.getLastAccessedTime has not been fully implemented yet.");
    return 0;
  }

  public ServletContext getServletContext() {
    System.err.println("Method DummyHttpSession.getServletContext has not been fully implemented yet.");
    return null;
  }

  public void setMaxInactiveInterval(int i) {
    System.err.println("Method DummyHttpSession.setMaxInactiveInterval has not been fully implemented yet.");

  }

  public int getMaxInactiveInterval() {
    System.err.println("Method DummyHttpSession.getMaxInactiveInterval has not been fully implemented yet.");
    return 0;
  }

  public HttpSessionContext getSessionContext() {
    System.err.println("Method DummyHttpSession.getSessionContext has not been fully implemented yet.");
    return null;
  }

  public Object getAttribute(String val) {
    return attributeMap.get(val);
  }

  public Object getValue(String val) {
    System.err.println("Method DummyHttpSession.getValue has not been fully implemented yet.");
    return null;
  }

  public Enumeration getAttributeNames() {
    System.err.println("Method DummyHttpSession.getAttributeNames has not been fully implemented yet.");
    return null;
  }

  public String[] getValueNames() {
    System.err.println("Method DummyHttpSession.getValueNames has not been fully implemented yet.");
    return new String[0];
  }

  public void setAttribute(String val, Object object) {
    attributeMap.put(val, object);
  }

  public void putValue(String val, Object object) {
    System.err.println("Method DummyHttpSession.putValue has not been fully implemented yet.");

  }

  public void removeAttribute(String val) {
    System.err.println("Method DummyHttpSession.removeAttribute has not been fully implemented yet.");

  }

  public void removeValue(String val) {
    System.err.println("Method DummyHttpSession.removeValue has not been fully implemented yet.");

  }

  public void invalidate() {
    System.err.println("Method DummyHttpSession.invalidate has not been fully implemented yet.");

  }

  public boolean isNew() {
    System.err.println("Method DummyHttpSession.isNew has not been fully implemented yet.");
    return false;
  }
}

