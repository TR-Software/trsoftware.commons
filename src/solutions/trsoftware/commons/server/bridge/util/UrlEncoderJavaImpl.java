package solutions.trsoftware.commons.server.bridge.util;

import solutions.trsoftware.commons.client.bridge.util.UrlEncoder;
import solutions.trsoftware.commons.server.util.ServerStringUtils;

/**
 * Jun 30, 2012
 *
 * @author Alex
 */
public class UrlEncoderJavaImpl extends UrlEncoder {


  private static UrlEncoderJavaImpl instance = new UrlEncoderJavaImpl();

  public static UrlEncoderJavaImpl getInstance() {
    return instance;
  }

  private UrlEncoderJavaImpl() {
    // this class is a singleton
  }

  /**
   * Encodes a value for use in a URI component.  Same as Javascript's {@code encodeURIComponent}  function and Java's
   * {@link java.net.URLEncoder#encode(String, String)}. NOTE: it is not guaranteed that the aforementioned functions,
   * used to implement this class on the client and server respectively, will produce the same results for the same
   * inputs. The encoding used on the server will be UTF-8.
   */
  public String encode(String value) {
    // TODO: replace the method in ServerStringUtils with this class?
    return ServerStringUtils.urlEncode(value);
  }

  /**
   * The opposite of {@link #encode(String)}.
   */
  public String decode(String value) {
    return ServerStringUtils.urlDecode(value);
  }
}