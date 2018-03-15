package solutions.trsoftware.commons.server.servlet.testutil;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.util.Map;

/**
 * @author Alex
 * @since 1/2/2018
 */
public class DummyFilterConfig extends DummyWebConfigObject implements FilterConfig {

  private String filterName;

  public DummyFilterConfig(Map<String, String> initParameters) {
    this(initParameters, new DummyServletContext());
  }

  public DummyFilterConfig(Map<String, String> initParameterMap, ServletContext servletContext) {
    super(initParameterMap, servletContext);
  }

  @Override
  public String getFilterName() {
    return filterName;
  }

  public DummyFilterConfig setFilterName(String filterName) {
    this.filterName = filterName;
    return this;
  }

  public DummyFilterConfig setServletContext(ServletContext servletContext) {
    this.servletContext = servletContext;
    return this;
  }
}
