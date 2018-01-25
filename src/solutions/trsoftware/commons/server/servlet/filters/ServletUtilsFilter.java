package solutions.trsoftware.commons.server.servlet.filters;

import solutions.trsoftware.commons.server.servlet.RequestCopy;
import solutions.trsoftware.commons.server.servlet.ServletUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Saves a copy of the current request by calling {@link ServletUtils#setThreadLocalRequestCopy(RequestCopy)}
 *
 * @author Alex
 * @since 11/14/2017
 */
public class ServletUtilsFilter extends HttpFilterAdapter {

  @Override
  public void doHttpFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
    ServletUtils.setThreadLocalRequestCopy(new RequestCopy(request));
    filterChain.doFilter(request, response);
    ServletUtils.setThreadLocalRequestCopy(null);  // clear the thread-local after the request
  }
}
