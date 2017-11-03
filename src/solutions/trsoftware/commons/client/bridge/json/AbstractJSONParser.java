package solutions.trsoftware.commons.client.bridge.json;

/**
 * @author Alex, 3/4/2016
 */
public abstract class AbstractJSONParser implements JSONParser {

  /**
   * {@inheritDoc}
   */
  @Override
  public final String safeUrlDecode(String str) {
    if (str == null)
      return null;
    try {
      return unsafeUrlDecode(str);
    }
    catch (Throwable e) {
      return str;  // return the original string, which could not be decoded
    }
  }

  protected abstract String unsafeUrlDecode(String str);
}
