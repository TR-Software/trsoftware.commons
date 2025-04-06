package solutions.trsoftware.commons.rebind.bundle;

import com.google.common.net.PercentEscaper;
import solutions.trsoftware.commons.shared.BaseTestCase;
import solutions.trsoftware.commons.shared.annotations.ExcludeFromSuite;

import java.util.regex.Pattern;

/**
 * @author Alex
 * @since 2/23/2025
 */
public class SvgImageResourceGeneratorTest extends BaseTestCase {

  /* TODO(2/22/2025): can significantly reduce output size by *not* base64-encoding the SVG data:
     Just write the minified SVG text in the data url, only escape the triangle braces (<>)
     @see https://css-tricks.com/probably-dont-base64-svg/ and https://codepen.io/tigt/post/optimizing-svgs-in-data-uris
   */
  
  private String eyesSvg;
  private String trianglesSvg;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    eyesSvg = "<svg width=\"436.141\" height=\"436.141\" xmlns=\"http://www.w3.org/2000/svg\"><path d=\"M313.11 100c-27.18 0-49.21 22.03-49.21 49.21 0 15.28 6.97 28.94 17.9 37.96-.2-1.17-.16-2.34.18-3.48 1.73-5.85 10.48-8.42 19.53-5.73 9.05 2.68 14.98 9.61 13.24 15.46-.62 2.1-2.15 3.77-4.26 4.93.86.04 1.74.07 2.62.07 27.18 0 49.21-22.03 49.21-49.21S340.29 100 313.11 100m29.46 27.73c-1.74 5.86-10.49 8.43-19.54 5.74s-14.97-9.61-13.23-15.47c1.73-5.85 10.48-8.42 19.53-5.74 9.05 2.69 14.98 9.61 13.24 15.47M124.46 100c-27.18 0-49.21 22.03-49.21 49.21 0 15.28 6.97 28.94 17.9 37.96-.2-1.17-.16-2.34.18-3.48 1.73-5.85 10.48-8.42 19.53-5.73 9.05 2.68 14.98 9.61 13.24 15.46-.62 2.1-2.15 3.77-4.26 4.93.86.04 1.74.07 2.62.07 27.17 0 49.2-22.03 49.2-49.21.01-27.18-22.03-49.21-49.2-49.21m29.46 27.73c-1.74 5.86-10.48 8.43-19.54 5.74-9.05-2.69-14.98-9.61-13.23-15.47 1.73-5.85 10.48-8.42 19.53-5.74 9.05 2.69 14.98 9.61 13.24 15.47\"/></svg>";
    trianglesSvg = "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 200 200\" fill=\"#000\" fill-opacity=\"0.27\"><path d=\"M55 5 20 75h70zm90 0-35 70h70zM55 95l-35 70h70zm90 0-35 70h70z\"/></svg>";
  }

  @Override
  protected void tearDown() throws Exception {
    eyesSvg = null;
    trianglesSvg = null;
    super.tearDown();
  }

  @ExcludeFromSuite
  public void testMinification() {
    String[] svgStrings = new String[]{eyesSvg, trianglesSvg};
//    String svgString = this.eyesSvg;
    for (String svgString : svgStrings) {
      svgToDataURI(svgString, "image/svg+xml");
    }
  }

  public static void svgToDataURI(String svgString, String mimeType) {
    String singleQuoted = replaceQuotes(svgString);
    System.out.println("singleQuoted:\n" + singleQuoted);

    // TODO: experimental: find a suitable safeChars value for PercentEscaper:
    String URL_PATH_OTHER_SAFE_CHARS_LACKING_PLUS =
        "-._~" // Unreserved characters.
            + "!$'()*,;&=" // The subdelim characters (excluding '+').
            + "@:"; // The gendelim characters permitted in paths.
//    String safeChars = URL_PATH_OTHER_SAFE_CHARS_LACKING_PLUS + "+/? ";
//    String safeChars = URL_PATH_OTHER_SAFE_CHARS_LACKING_PLUS + "+/?";
    String safeChars = URL_PATH_OTHER_SAFE_CHARS_LACKING_PLUS /*+ "+/?"*/;
    PercentEscaper percentEscaper = new PercentEscaper(safeChars, false);
    String escapedSvg = percentEscaper.escape(singleQuoted);
    System.out.println("escapedSvg:\n" + escapedSvg);

    String dataUri = "\"data:" + mimeType.replaceAll("\"", "\\\\\"")
        + "," + escapedSvg + "\"";

    System.out.println("dataUri:\n" + dataUri);
  }

  /**
   * Replaces all double-quoted attributes with single quotes.
   * Example: {@code <tag a="foo", b="bar"/>} &rarr; {@code <tag a='foo', b='bar'/>}
   * @param markup XML/HTML fragment
   */
  public static String replaceQuotes(String markup) {
    Pattern pattern = Pattern.compile("(\\w+=)\"(.*?)\"");
    return pattern.matcher(markup).replaceAll("$1'$2'");
  }
}