package solutions.trsoftware.commons.server.servlet.testutil;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

/**
 * @author Alex
 * @since 1/2/2018
 */
public class DummyFilterConfig implements FilterConfig {

  private String filterName;
  private ServletContext servletContext;
  private Map<String, String> initParameters;

  public DummyFilterConfig(Map<String, String> initParameters) {
    this.initParameters = initParameters;
  }

  @Override
  public String getFilterName() {
    return filterName;
  }

  @Override
  public ServletContext getServletContext() {
    return servletContext;
  }

  @Override
  public String getInitParameter(String name) {
    return initParameters.get(name);
  }

  @Override
  public Enumeration<String> getInitParameterNames() {
    return Collections.enumeration(initParameters.keySet());
  }

  public DummyFilterConfig setFilterName(String filterName) {
    this.filterName = filterName;
    return this;
  }

  public DummyFilterConfig setServletContext(ServletContext servletContext) {
    this.servletContext = servletContext;
    return this;
  }

  public DummyFilterConfig setInitParameters(Map<String, String> initParameters) {
    this.initParameters = initParameters;
    return this;
  }
}
