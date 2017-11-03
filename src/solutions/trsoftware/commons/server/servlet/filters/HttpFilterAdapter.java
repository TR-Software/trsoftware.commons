package solutions.trsoftware.commons.server.servlet.filters;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Casts the request and response from {@link ServletRequest} and {@link ServletResponse} to {@link HttpServletRequest}
 * and {@link HttpServletResponse}, and delegates to {@link #doHttpFilter(HttpServletRequest, HttpServletResponse, FilterChain)}
 * with the cast objects as args.
 *
 * If un-castable, simply passes the request up the filter chain.
 *
 * @since Mar 27, 2010
 *
 * @author Alex
 */
public abstract class HttpFilterAdapter extends AbstractFilter {

  public final void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    if (servletRequest instanceof HttpServletRequest && servletResponse instanceof HttpServletResponse)
      doHttpFilter(((HttpServletRequest)servletRequest), ((HttpServletResponse)servletResponse), filterChain);
    else
      filterChain.doFilter(servletRequest, servletResponse);
  }

  /**
   * Called from {@link #doFilter(ServletRequest, ServletResponse, FilterChain)} with request and response cast
   * to their {@code Http*} subtypes.
   * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
   */
  public abstract void doHttpFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException;
}
