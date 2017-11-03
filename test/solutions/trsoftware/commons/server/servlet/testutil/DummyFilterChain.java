package solutions.trsoftware.commons.server.servlet.testutil;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simply counts the number of times {@link #doFilter(ServletRequest, ServletResponse)} was invoked.
 *
 * @author Alex, 10/31/2017
 */
public class DummyFilterChain implements FilterChain {
  private AtomicInteger count = new AtomicInteger();

  @Override
  public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
    count.incrementAndGet();
  }

  public int getInvocationCount() {
    return count.get();
  }
}
