package solutions.trsoftware.commons.server.servlet.config;

import javax.servlet.ServletConfig;
import java.util.Enumeration;

/**
 * Adapts a {@link ServletConfig} so that in can be consumed by {@link WebConfigParser}.
 * @see HasInitParameters
 *
 * @author Alex
 * @since 3/5/2018
 */
public class ServletConfigWrapper implements HasInitParameters {

  private ServletConfig servletConfig;

  public ServletConfigWrapper(ServletConfig servletConfig) {
    this.servletConfig = servletConfig;
  }

  public ServletConfig getServletConfig() {
    return servletConfig;
  }

  /**
   * Returns a <code>String</code> containing the value of the named
   * initialization parameter, or <code>null</code> if the parameter does not
   * exist.
   *
   * @param name
   *            a <code>String</code> specifying the name of the
   *            initialization parameter
   * @return a <code>String</code> containing the value of the initialization
   *         parameter
   */
  @Override
  public String getInitParameter(String name) {
    return servletConfig.getInitParameter(name);
  }

  /**
   * Returns the names of the servlet's initialization parameters as an
   * <code>Enumeration</code> of <code>String</code> objects, or an empty
   * <code>Enumeration</code> if the servlet has no initialization parameters.
   *
   * @return an <code>Enumeration</code> of <code>String</code> objects
   *         containing the names of the servlet's initialization parameters
   */
  @Override
  public Enumeration<String> getInitParameterNames() {
    return servletConfig.getInitParameterNames();
  }
}
