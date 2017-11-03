package solutions.trsoftware.commons.server.servlet.filters;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

/**
 * Provides empty default implementations of {@link Filter#init(FilterConfig)} and {@link Filter#destroy()}}
 *  
 * @since Jul 29, 2009
 * @author Alex
 */
public abstract class AbstractFilter implements Filter {

  public void init(FilterConfig filterConfig) throws ServletException {
    // subclasses may override
  }

  public void destroy() {
    // subclasses may override
  }
}
