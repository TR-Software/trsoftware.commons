package solutions.trsoftware.commons.server.servlet.config;

import javax.servlet.FilterConfig;
import java.util.Enumeration;

/**
 * Adapts a {@link FilterConfig} so that in can be consumed by {@link WebConfigParser}.
 * @see HasInitParameters
 *
 * @author Alex
 * @since 3/5/2018
 */
public class FilterConfigWrapper implements HasInitParameters {

  private FilterConfig filterConfig;

  public FilterConfigWrapper(FilterConfig filterConfig) {
    this.filterConfig = filterConfig;
  }

  public FilterConfig getFilterConfig() {
    return filterConfig;
  }

  /**
   * Returns a <code>String</code> containing the value of the named
   * initialization parameter, or <code>null</code> if the parameter does not
   * exist.
   *
   * @param name
   *            <code>String</code> specifying the name of the initialization
   *            parameter
   *
   * @return <code>String</code> containing the value of the initialization
   *         parameter
   */
  @Override
  public String getInitParameter(String name) {
    return filterConfig.getInitParameter(name);
  }

  /**
   * Returns the names of the filter's initialization parameters as an
   * <code>Enumeration</code> of <code>String</code> objects, or an empty
   * <code>Enumeration</code> if the filter has no initialization parameters.
   *
   * @return <code>Enumeration</code> of <code>String</code> objects
   *         containing the names of the filter's initialization parameters
   */
  @Override
  public Enumeration<String> getInitParameterNames() {
    return filterConfig.getInitParameterNames();
  }
}
