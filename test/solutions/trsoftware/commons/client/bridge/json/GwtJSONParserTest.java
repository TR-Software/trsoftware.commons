package solutions.trsoftware.commons.client.bridge.json;

import solutions.trsoftware.commons.client.CommonsGwtTestCase;
import solutions.trsoftware.commons.client.bridge.json.impl.GwtJSONParser;

/**
 * Jan 14, 2009
 *
 * @author Alex
 */
public class GwtJSONParserTest extends CommonsGwtTestCase {

  JSONParserTest delegate = new JSONParserTest() {
    protected JSONParser getParser() {
      return new GwtJSONParser();
    }
  };

  public void testParseArray() throws Exception {
    delegate.testParseArray();
  }

  public void testParseObject() throws Exception {
    delegate.testParseObject();
  }

  public void testNulls() throws Exception {
    delegate.testNulls();
  }

  public void testMalformattedURIEncodings() {
    delegate.testMalformattedURIEncodings();
  }
}
