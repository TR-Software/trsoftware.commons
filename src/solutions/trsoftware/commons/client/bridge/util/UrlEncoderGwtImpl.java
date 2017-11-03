package solutions.trsoftware.commons.client.bridge.util;

import solutions.trsoftware.commons.client.util.JavascriptUtils;

/**
 * Jun 30, 2012
 *
 * @author Alex
 */
public class UrlEncoderGwtImpl extends UrlEncoder {


  private static UrlEncoderGwtImpl instance = new UrlEncoderGwtImpl();

  public static UrlEncoderGwtImpl getInstance() {
    return instance;
  }

  private UrlEncoderGwtImpl() {
    // this class is a singleton
  }

  /**
   * Encodes a value for use in a URI component.  Same as Javascript's {@code encodeURIComponent}  function and Java's
   * {@link java.net.URLEncoder#encode(String, String)}. NOTE: it is not guaranteed that the aforementioned functions,
   * used to implement this class on the client and server respectively, will produce the same results for the same
   * inputs. The encoding used on the server will be UTF-8.
   */
  public String encode(String value) {
    // TODO: replace the method in JavascriptUtils with this class?
    return JavascriptUtils.encodeURIComponent(value);
  }

  /**
   * The opposite of {@link #encode(String)}.
   */
  public String decode(String value) {
    return JavascriptUtils.safeDecodeURIComponent(value);
  }
}
