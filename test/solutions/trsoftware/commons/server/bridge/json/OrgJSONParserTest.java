package solutions.trsoftware.commons.server.bridge.json;

import solutions.trsoftware.commons.client.bridge.json.JSONParser;
import solutions.trsoftware.commons.client.bridge.json.JSONParserTest;

/**
 * Jan 14, 2009
 *
 * @author Alex
 */
public class OrgJSONParserTest extends JSONParserTest {

  // all test methods implemented by superclass

  protected JSONParser getParser() {
    return new OrgJSONParser();
  }
}