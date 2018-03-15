package solutions.trsoftware.commons.server.servlet.config;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;

/**
 * Adapts a {@link ServletContext} so that in can be consumed by {@link WebConfigParser}.
 * @see HasInitParameters
 *
 * @author Alex
 * @since 3/5/2018
 */
public class ServletContextWrapper implements HasInitParameters {

  private ServletContext servletContext;

  public ServletContextWrapper(ServletContext servletContext) {
    this.servletContext = servletContext;
  }

  public ServletContext getServletContext() {
    return servletContext;
  }

  /**
   * Returns a <code>String</code> containing the value of the named
   * context-wide initialization parameter, or <code>null</code> if the
   * parameter does not exist.
   * <p>
   * This method can make available configuration information useful to an
   * entire "web application". For example, it can provide a webmaster's email
   * address or the name of a system that holds critical data.
   *
   * @param name
   *            a <code>String</code> containing the name of the parameter
   *            whose value is requested
   * @return a <code>String</code> containing the value of the initialization
   *         parameter
   * @see ServletConfig#getInitParameter
   */
  @Override
  public String getInitParameter(String name) {
    return servletContext.getInitParameter(name);
  }

  /**
   * Returns the names of the context's initialization parameters as an
   * <code>Enumeration</code> of <code>String</code> objects, or an empty
   * <code>Enumeration</code> if the context has no initialization parameters.
   *
   * @return an <code>Enumeration</code> of <code>String</code> objects
   *         containing the names of the context's initialization parameters
   * @see ServletConfig#getInitParameter
   */
  @Override
  public Enumeration<String> getInitParameterNames() {
    return servletContext.getInitParameterNames();
  }
}
