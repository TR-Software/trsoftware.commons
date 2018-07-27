/*
 * Copyright 2018 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.server.bridge.util;

import junit.framework.TestCase;
import solutions.trsoftware.commons.server.testutil.PerformanceComparison;
import solutions.trsoftware.commons.shared.annotations.Slow;
import solutions.trsoftware.commons.shared.text.Language;
import solutions.trsoftware.commons.shared.util.MapUtils;
import solutions.trsoftware.commons.shared.util.RandomUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;
import solutions.trsoftware.commons.shared.util.callables.Function1t;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.net.URLEncoder;
import java.util.*;

/**
 * @author Alex
 * @since 7/19/2018
 */
public class URIComponentEncoderJavaImplTest extends TestCase {

  private static final String JSON_STR = "{\"a\": 1.245, \"b\": \"foo\", \"c\": [1, 2, 3]}";
  /** An alphabet containing a mix of ASCII and unicode chars (for testing random inputs to the encoder) */
  public static final String ALPHABET = StringUtils.ASCII_PRINTABLE_CHARS + Language.RUSSIAN.getAlphabet() + Language.RUSSIAN.getAlphabet().toUpperCase();

  private URIComponentEncoderJavaImpl encoder;
  private ScriptEngine jsEngine;

  public void setUp() throws Exception {
    super.setUp();
    encoder = URIComponentEncoderJavaImpl.getInstance();
    jsEngine = new ScriptEngineManager().getEngineByName("javascript");
  }

  public void tearDown() throws Exception {
    encoder = null;
    jsEngine = null;
    super.tearDown();
  }

  /**
   * Tests that {@link URIComponentEncoderJavaImpl#encode(String)} is compatible with the JS {@code decodeURIComponent} function.
   */
  public void testEncode() throws Exception {
    // 1) make sure it produces the same output as JavaScript encodeURIComponent
    assertEncodersEqual(
        "JavaScript encodeURIComponent", this::encodeURIComponent,
        "UrlEncoderJavaImpl", encoder::encode
        );
    // 2) test encoding a simple JSON object
    testEncode(JSON_STR);
    // 3) test some random strings
    for (int i = 0; i < 1000; i++) {
      testEncode(RandomUtils.randString(40, ALPHABET));
    }
  }

  @Slow
  public void testBenchmarks() throws Exception {
    // 1) compare performance of our encoder vs calling encodeURIComponent via the JS engine
    {
      // create some test data
      List<String> testStrings = new ArrayList<>();
      // use a mix of ASCII and unicode chars in our random strings
      for (int i = 0; i < 1000; i++) {
        testStrings.add(RandomUtils.randString(ALPHABET, 20, 100));
      }
      PerformanceComparison.BenchmarkTask<String> ourEncoderTask = new PerformanceComparison.BenchmarkTask<String>("UrlEncoderJavaImpl", testStrings) {
        @Override
        protected void doIteration(String arg) throws Exception {
          encoder.encode(arg);
        }
      };
      PerformanceComparison.BenchmarkTask<String> encodeURIComponentTask = new PerformanceComparison.BenchmarkTask<String>("JavaScript encodeURIComponent", testStrings) {
        @Override
        protected void doIteration(String arg) throws Exception {
          encodeURIComponent(arg);
        }
      };
      PerformanceComparison.BenchmarkTask<String> javaEncoderTask = new PerformanceComparison.BenchmarkTask<String>(URLEncoder.class.getName(), testStrings) {
        @Override
        protected void doIteration(String arg) throws Exception {
          URLEncoder.encode(arg, "UTF-8");
        }
      };
      PerformanceComparison.compare(
          ourEncoderTask,
          encodeURIComponentTask,
          20
      );
      PerformanceComparison.compare(
          ourEncoderTask,
          javaEncoderTask,
          20
      );
    }
  }

  private void testEncode(String rawString) throws Exception {
    String javaEncoded = encoder.encode(rawString);
    String jsDecoded = decodeURIComponent(javaEncoded);
    String msg = String.format("'%s' --java encode-> '%s' --js decode-> '%s'", rawString, javaEncoded, jsDecoded);
    System.out.println(msg);
    assertEquals(msg, rawString, jsDecoded);
  }

  public void testDecode() throws Exception {
    // 1) test decoding a simple JSON object encoded in javascript
    testDecode(JSON_STR);
    // 2) test some random strings
    for (int i = 0; i < 1000; i++) {
      testDecode(RandomUtils.randString(40, ALPHABET));
    }
  }

  private void testDecode(String rawString) throws ScriptException {
    String jsEncoded = encodeURIComponent(rawString);
    String javaDecoded = encoder.decode(jsEncoded);
    String msg = String.format("'%s' --js encode-> '%s' --java decode-> '%s'", rawString, jsEncoded, javaDecoded);
    System.out.println(msg);
    assertEquals(msg, rawString, javaDecoded);
  }

  private String decodeURIComponent(String str) throws ScriptException {
    return (String)jsEngine.eval("decodeURIComponent(str)", new SimpleBindings(MapUtils.hashMap("str", str)));
  }

  private String encodeURIComponent(String str) throws ScriptException {
    return (String)jsEngine.eval("encodeURIComponent(str)", new SimpleBindings(MapUtils.hashMap("str", str)));
  }


  private static void assertEncodersEqual(String encoder1Name, Function1t<String, String, Exception> encoder1Fcn,
                                          String encoder2Name, Function1t<String, String, Exception> encoder2Fcn) throws Exception {
    System.out.println("--------------------------------------------------------------------------------");
    System.out.printf("char -> %s / %s%n", encoder1Name, encoder2Name);
    System.out.println("--------------------------------------------------------------------------------");
    Set<Character> equalChars = new LinkedHashSet<>();
    Map<Character, String> diffEncoder1 = new LinkedHashMap<>();
    Map<Character, String> diffEncoder2 = new LinkedHashMap<>();
    String testChars = StringUtils.ASCII_PRINTABLE_CHARS;
    for (int i = 0; i < testChars.length(); i++) {
      char c = testChars.charAt(i);
      String cStr = String.valueOf(c);
      String cEnc1 = encoder1Fcn.call(cStr);
      String cEnc2 = encoder2Fcn.call(cStr);
      boolean eq = cEnc1.equals(cEnc2);
      if (eq) {
        equalChars.add(c);
      }
      else {
        diffEncoder1.put(c, cEnc1);
        diffEncoder2.put(c, cEnc2);
      }
      System.out.printf("'%s' -> '%s' / '%s' %s%n", cStr, cEnc1, cEnc2, eq ? "" : "(DIFF!)");
    }
    assertEquals(testChars.length(), equalChars.size());
  }

}