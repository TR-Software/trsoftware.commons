package solutions.trsoftware.commons.bridge;

import solutions.trsoftware.commons.client.bridge.json.JSONParser;
import solutions.trsoftware.commons.client.bridge.json.impl.GwtJSONParser;
import solutions.trsoftware.commons.client.bridge.text.NumberFormatter;
import solutions.trsoftware.commons.client.bridge.text.impl.NumberFormatterGwtImpl;
import solutions.trsoftware.commons.client.bridge.util.URIComponentEncoder;
import solutions.trsoftware.commons.client.bridge.util.URIComponentEncoderGwtImpl;
import solutions.trsoftware.commons.shared.util.Duration;
import solutions.trsoftware.commons.shared.util.text.SharedNumberFormat;

import java.util.Random;

/**
 * GWT Version -- GWT Version -- GWT Version -- GWT Version -- GWT Version
 *
 * IMPORTANT: This the "emulated" GWT (non-Java) version of this class.
 * The Java version is located in the standard directory for this package.
 * <p>
 * This class serves to overcome the problem of having references to non-GWT-compilable classes from those that
 * are GWT-compilable, by keeping all occurrences of such in a central place - namely the
 * implementation of this class.
 * <p>
 * There are actually two different implementations of this class in two different directories:
 * <ol>
 *   <li>The Java version in the normal directory for this package</li>
 *   <li>
 *     The GWT version in the {@code translatable} subdirectory of the module, which will replace all usages
 *     of the Java version when running in web mode (the same way the JRE emulation classes replace their original Java
 *     counterparts in web mode. This is configured using the {@code <super-source>} element of the module XML.
 *   </li>
 * </ol>
 *
 * Note that we could technically do away with this factory class and
 * instead emulate each class directly using the {@code <super-source>} technique,
 * but this interface/factory approach seems easier - so that we just
 * have this emulation weirdness happening with one file instead of many.
 *
 * @see <a href="http://code.google.com/docreader/#p=google-web-toolkit-doc-1-5&s=google-web-toolkit-doc-1-5&t=DevGuideModuleXml">DevGuideModuleXml</a>
 *
 * @author Alex
 */
public class BridgeTypeFactory {

 // GWT Version -- GWT Version -- GWT Version -- GWT Version -- GWT Version

  /** This class should not be instantiated */
  private BridgeTypeFactory() {
  }

  public static JSONParser newJSONParser() {
    return new GwtJSONParser();
  }

  /**
   * @deprecated use {@link SharedNumberFormat} instead.
   */
  public static NumberFormatter newNumberFormatter(int minIntegerDigits, int minFractionalDigits, int maxFractionalDigits, boolean digitGrouping, boolean percent) {
    return new NumberFormatterGwtImpl(minIntegerDigits, minFractionalDigits, maxFractionalDigits, digitGrouping, percent);
  }

  public static Duration newDuration() {
    return new solutions.trsoftware.commons.client.util.Duration();
  }

  public static Duration newDuration(String name) {
    return new solutions.trsoftware.commons.client.util.Duration(name);
  }

  public static Duration newDuration(String name, String action) {
    return new solutions.trsoftware.commons.client.util.Duration(name, action);
  }

  public static URIComponentEncoder getURIComponentEncoder() {
    return URIComponentEncoderGwtImpl.getInstance();
  }

  public static Random newSecureRandom() {
    return new Random();  // java.security.SecureRandom is not available under GWT
  }
}
