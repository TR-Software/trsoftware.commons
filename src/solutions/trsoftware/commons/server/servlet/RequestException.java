package solutions.trsoftware.commons.server.servlet;

import javax.servlet.ServletException;

/**
 * Encapsulates a status code and error message to be returned as a HTTP failure response.
 * 
 * @author Alex
 */
public class RequestException extends ServletException {

  private int statusCode;

  public RequestException(int statusCode, String message) {
    super(message);
    this.statusCode = statusCode;
  }

  public int getStatusCode() {
    return statusCode;
  }
}
