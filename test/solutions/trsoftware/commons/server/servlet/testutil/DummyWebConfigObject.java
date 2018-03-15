package solutions.trsoftware.commons.server.servlet.testutil;

import solutions.trsoftware.commons.server.servlet.config.HasInitParameters;

import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

/**
 * @author Alex
 * @since 3/6/2018
 */
public abstract class DummyWebConfigObject implements HasInitParameters {

  protected Map<String, String> initParameterMap;
  protected ServletContext servletContext;

  protected DummyWebConfigObject() {
  }

  protected DummyWebConfigObject(Map<String, String> initParameters) {
    this(initParameters, null);
  }

  public DummyWebConfigObject(Map<String, String> initParameterMap, ServletContext servletContext) {
    this.initParameterMap = initParameterMap;
    this.servletContext = servletContext;
  }

  @Override
  public String getInitParameter(String name) {
    return initParameterMap.get(name);
  }

  @Override
  public Enumeration<String> getInitParameterNames() {
    return Collections.enumeration(initParameterMap.keySet());
  }

  public ServletContext getServletContext() {
    if (this instanceof ServletContext)
      return (ServletContext)this;
    return servletContext;
  }

  public Map<String, String> getInitParameterMap() {
    return initParameterMap;
  }

  public void setInitParameterMap(Map<String, String> initParameterMap) {
    this.initParameterMap = initParameterMap;
  }
}
