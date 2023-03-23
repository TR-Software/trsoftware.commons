package solutions.trsoftware.commons.server.gwt;

import com.google.gwt.user.server.rpc.SerializationPolicy;

/**
 * Indicates that a {@link SerializationPolicy} for a particular GWT module or service interface was not found
 * or could not be loaded.
 *
 * @author Alex
 * @since 3/8/2023
 */
public class PolicyNotFoundException extends Exception {
  public PolicyNotFoundException() {
  }

  public PolicyNotFoundException(String message) {
    super(message);
  }

  public PolicyNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public PolicyNotFoundException(Throwable cause) {
    super(cause);
  }
}
