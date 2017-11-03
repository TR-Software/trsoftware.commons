package solutions.trsoftware.commons.client.bridge.util;

import solutions.trsoftware.commons.bridge.BridgeTypeFactory;

/**
 * Jun 30, 2012
 *
 * @author Alex
 */
public abstract class UrlEncoder {

  /**
   * Encodes a value for use in a URI component.  Same as Javascript's {@code encodeURIComponent} function and Java's
   * {@link java.net.URLEncoder#encode(String, String)}. NOTE: it is not guaranteed that the aforementioned functions,
   * used to implement this class on the client and server respectively, will produce the same results for the same
   * inputs. The encoding used on the server will be UTF-8.
   */
  public abstract String encode(String value);


  /**
   * The opposite of {@link #encode(String)}.
   */
  public abstract String decode(String value);

  /**
   * Returns an instance of this class appropriate for the current execution environment.
   */
  public static UrlEncoder get() {
    return BridgeTypeFactory.getUrlEncoder();
  }
}
