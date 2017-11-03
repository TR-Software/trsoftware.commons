package solutions.trsoftware.commons.server.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static solutions.trsoftware.commons.client.util.StringUtils.isBlank;

/**
 * Provides basic utility methods that can be helpful when writing a new {@link HttpServlet}
 *
 * @author Alex, 10/31/2017
 */
public abstract class BaseHttpServlet extends HttpServlet {

  // TODO: extract this method to a base class
  protected static String getRequiredParameter(HttpServletRequest request, String paramName) throws IOException, RequestException {
    String value = request.getParameter(paramName);
    if (isBlank(value)) {
      throw new RequestException(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter " + paramName);  // Bad Request (missing required parameters)
    }
    return value;
  }

  /**
   * This override simply escalates the method visibility from {@code protected} to {@code public}, to facilitate
   * unit testing outside of a servlet container.
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    super.doGet(req, resp);
  }

  /**
   * This override simply escalates the method visibility from {@code protected} to {@code public}, to facilitate
   * unit testing outside of a servlet container.
   */
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    super.doPost(req, resp);
  }
}
