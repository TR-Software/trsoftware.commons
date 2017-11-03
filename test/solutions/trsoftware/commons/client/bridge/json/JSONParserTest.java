package solutions.trsoftware.commons.client.bridge.json;

import junit.framework.TestCase;

/**
 * Jan 14, 2009
 *
 * @author Alex
 */
public abstract class JSONParserTest extends TestCase {
  private static final String SAMPLE_ARRAY_JSON = "[1, \"two\", {\"a\":\"b\"}]";

  /** Should be implemented by subclasses to provide the appropriate instance */
  protected abstract JSONParser getParser();

  public void testParseArray() throws Exception {
    verifyArray(getParser().parseArray(SAMPLE_ARRAY_JSON));
  }

  public void testParseObject() throws Exception {
    String json = "{\"foo\":1, \"bar\": \"two\", \"arr\": " + SAMPLE_ARRAY_JSON + "}";
    JSONObject jsonObject = getParser().parseObject(json);
    assertNotNull(jsonObject);
    assertEquals(1, jsonObject.getInteger("foo"));
    assertEquals("two", jsonObject.getString("bar"));
    verifyArray(jsonObject.getArray("arr"));
  }

  /** Checks the proper parsing of SAMPLE_ARRAY_JSON */
  private void verifyArray(JSONArray jsonArray) {
    assertNotNull(jsonArray);
    assertEquals(3, jsonArray.size());
    assertEquals(1, jsonArray.getInteger(0));
    assertEquals("two", jsonArray.getString(1));
    JSONObject object = jsonArray.getObject(2);
    assertNotNull(object);
    assertEquals("b", object.getString("a"));
  }

  public void testNulls() throws Exception {
    String objWithEmptyStringJson = "{\"firstName\":\"Bono\", \"lastName\": \"\"}";
    JSONObject objectWithEmptyString = getParser().parseObject(objWithEmptyStringJson);
    assertTrue(objectWithEmptyString.hasKey("firstName"));
    assertTrue(objectWithEmptyString.hasKey("lastName"));
    assertEquals("Bono", objectWithEmptyString.getString("firstName"));
    assertEquals("", objectWithEmptyString.getString("lastName"));

    String objWithNullJson = "{\"firstName\":\"Bono\", \"lastName\": null}";
    JSONObject objectWithNull = getParser().parseObject(objWithNullJson);
    assertTrue(objectWithNull.hasKey("firstName"));
    assertTrue(objectWithNull.hasKey("lastName"));
    assertEquals("Bono", objectWithNull.getString("firstName"));
    // make sure that getting this field as any non-primitive returns a plain null
    assertNull(objectWithNull.getString("lastName"));
    assertNull(objectWithNull.getObject("lastName"));
    assertNull(objectWithNull.getArray("lastName"));

    String arrWithNullJson = "[\"foo\", null]";
    JSONArray arrayWithNull = getParser().parseArray(arrWithNullJson);
    assertEquals(2, arrayWithNull.size());
    assertEquals("foo", arrayWithNull.getString(0));
    // make sure that getting this field as any non-primitive returns a plain null
    assertNull(arrayWithNull.getString(1));
    assertNull(arrayWithNull.getObject(1));
    assertNull(arrayWithNull.getArray(1));
  }

  public void testMalformattedURIEncodings() {
    // this encoding will throw an exception which should be intercepted and fail gracefully
    assertEquals("120% bar", getParser().safeUrlDecode("120% bar")); // the % symbol could cause the URI decoding to break
    // this encoding should get properly decoded
    assertEquals("1=bar", getParser().safeUrlDecode("1%3Dbar"));
  }
}
