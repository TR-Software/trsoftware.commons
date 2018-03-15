package solutions.trsoftware.commons.server.servlet.testutil;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Map;

/**
 * @author Alex
 * @since 1/2/2018
 */
public class DummyServletConfig extends DummyWebConfigObject implements ServletConfig {

  private String servletName;

  public DummyServletConfig(Map<String, String> initParameters) {
    this(initParameters, new DummyServletContext());
  }

  public DummyServletConfig(Map<String, String> initParameterMap, ServletContext servletContext) {
    super(initParameterMap, servletContext);
  }

  @Override
  public String getServletName() {
    return servletName;
  }

  public DummyServletConfig setServletName(String servletName) {
    this.servletName = servletName;
    return this;
  }

  public DummyServletConfig setServletContext(ServletContext servletContext) {
    this.servletContext = servletContext;
    return this;
  }

}
