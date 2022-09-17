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

import solutions.trsoftware.commons.shared.util.StringUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Date: Aug 14, 2008 Time: 5:59:48 PM
 *
 * @author Alex
 */
public class DummyHttpSession implements HttpSession {

  private Map<String, Object> attributeMap = new HashMap<>();

  private final String sessionId = "DummySession_" + StringUtils.randString(8);

  private final long creationTime;

  public DummyHttpSession() {
    creationTime = System.currentTimeMillis();
  }

  public long getCreationTime() {
    return creationTime;
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

  public Object getAttribute(String name) {
    return attributeMap.get(name);
  }

  @Deprecated
  public Object getValue(String name) {
    return getAttribute(name);
  }

  public Enumeration<String> getAttributeNames() {
    return Collections.enumeration(attributeMap.keySet());
  }

  @Deprecated
  public String[] getValueNames() {
    return attributeMap.keySet().toArray(new String[0]);
  }

  public void setAttribute(String name, Object value) {
    attributeMap.put(name, value);
  }

  public void putValue(String name, Object value) {
    setAttribute(name, value);
  }

  public void removeAttribute(String name) {
    attributeMap.remove(name);
  }

  public void removeValue(String name) {
    removeAttribute(name);
  }

  public void invalidate() {
    attributeMap.clear();
  }

  public boolean isNew() {
    System.err.println("Method DummyHttpSession.isNew has not been fully implemented yet.");
    return false;
  }
}

