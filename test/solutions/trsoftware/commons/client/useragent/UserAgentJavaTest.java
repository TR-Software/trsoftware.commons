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

package solutions.trsoftware.commons.client.useragent;

import com.google.common.collect.ImmutableMap;
import junit.framework.TestCase;
import solutions.trsoftware.commons.server.io.ResourceLocator;
import solutions.trsoftware.commons.shared.util.VersionNumber;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.google.common.base.Strings.lenientFormat;
import static solutions.trsoftware.commons.client.useragent.UserAgent.parseVersionNumber;
import static solutions.trsoftware.commons.server.io.ServerIOUtils.readLines;
import static solutions.trsoftware.commons.shared.util.StringUtils.methodCallToString;

/**
 * Dec 14, 2008
 *
 * @author Alex
 */
public class UserAgentJavaTest extends TestCase {

  /*
    TODO: get an updated UA list from
    - https://udger.com/resources/ua-list
    - https://docs.microsoft.com/en-us/previous-versions/windows/internet-explorer/ie-developer/compatibility/hh869301(v=vs.85)
  */

  private List<String> readLinesFromFile(String filename) {
    return readLines(new ResourceLocator(filename, getClass()).getReaderUTF8(), true);
  }

  public void testIsIE() throws Exception {
    // load all the strings expected to match Firefox
    List<String> positiveExamples = new ArrayList<>(readLinesFromFile("ie.txt"));
    List<String> negativeExamples = new ArrayList<>(readLinesFromFile("ff.txt"));

    // Opera and Safari can emulate IE - make an exception for these strings
    List<String> safariAndOperaStrings = new ArrayList<>(readLinesFromFile("safari.txt"));
    safariAndOperaStrings.addAll(readLinesFromFile("opera.txt"));
    for (String s : safariAndOperaStrings) {
      String sl = s.toLowerCase();
      if (sl.contains("msie") && (sl.contains("webkit") || sl.contains("opera")))
        positiveExamples.add(s); // override the expectation
      else
        negativeExamples.add(s);
    }

    checkPredicate(positiveExamples, negativeExamples, str -> new UserAgent(str).isIE());
  }

  private void checkPredicate(List<String> positiveStrings, List<String> negativeStrings, Predicate<String> predicate) {
    assertTrue(positiveStrings.size() > 5);
    assertTrue(negativeStrings.size() > 5);
    for (String str : positiveStrings) {
      assertTrue("UA expected to pass: " + str, predicate.test(str));
    }
    for (String str : negativeStrings) {
      assertFalse("UA expected to fail: " + str, predicate.test(str));
    }
  }

  public void testParseVersionNumber() throws Exception {
    String uaString = "Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36";
    UserAgent instance = new UserAgent(uaString);
    Map<String, VersionNumber> expectations = new LinkedHashMap<String, VersionNumber>();
    expectations.put("Mozilla", new VersionNumber(5, 0));
    expectations.put("AppleWebKit", new VersionNumber(537, 36));
    expectations.put("Chrome", new VersionNumber(67, 0, 3396, 99));
    expectations.put("Safari", new VersionNumber(537, 36));
    expectations.put("Edge", null);
    expectations.put("Trident", null);

    for (String browserName : expectations.keySet()) {
      for (String arg : new String[]{browserName, browserName.toLowerCase()}) {
        // 1) test the static version of the method
        {
          VersionNumber result = parseVersionNumber(uaString, arg);
          System.out.println(lenientFormat("Parsed %s version %s from '%s'", browserName, result, uaString));
          assertEquals(expectations.get(browserName), result);
        }
        // 2) test the instance version of the method
        {
          VersionNumber result = instance.parseVersionNumber(arg);
          assertEquals(expectations.get(browserName), result);
        }
      }
    }
  }

  public void testParseVersionNumbers() throws Exception {
    // Chrome 67:
    testParseVersionNumbers(
        "Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36",
        ImmutableMap.of(
            "Mozilla", new VersionNumber(5, 0),
            "AppleWebKit", new VersionNumber(537, 36),
            "Chrome", new VersionNumber(67, 0, 3396, 99),
            "Safari", new VersionNumber(537, 36)
        )
    );
    // IE 11:
    testParseVersionNumbers(
        "Mozilla/5.0 (Windows NT 6.3; WOW64; Trident/7.0; .NET4.0E; .NET4.0C; .NET CLR 3.5.30729; .NET CLR 2.0.50727; .NET CLR 3.0.30729; MALCJS; Zoom 3.6.0; rv:11.0) like Gecko",
        ImmutableMap.of(
            "Mozilla", new VersionNumber(5),
            "Trident", new VersionNumber(7)
        )
    );

  }

  private void testParseVersionNumbers(String uaString, Map<String, VersionNumber> expected) {
    // 1) static method
    System.out.println(methodCallToString("parseVersionNumbers", uaString));
    ImmutableMap<String, VersionNumber> result = UserAgent.parseVersionNumbers(uaString);
    System.out.println("  -> " + result);
    assertEquals(expected, result);

    // 2) instance method
    assertEquals(expected, new UserAgent(uaString).parseVersionNumbers());
  }
}