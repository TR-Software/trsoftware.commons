/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.client.useragent;

import junit.framework.TestCase;
import solutions.trsoftware.commons.client.util.Predicate;
import solutions.trsoftware.commons.server.io.ServerIOUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Dec 14, 2008
 *
 * @author Alex
 */
public class UserAgentJavaTest extends TestCase {

  private static List<String> readLinesFromFile(String filename) {
    return ServerIOUtils.readLinesFromResource(
        ServerIOUtils.resourceNameFromFilenameInSamePackage(filename, UserAgentJavaTest.class),
        true);
  }

  public void testIsIE() throws Exception {
    // load all the strings expected to match Firefox
    List<String> positiveExamples = new ArrayList<String>();
    positiveExamples.addAll(readLinesFromFile("ie.txt"));

    List<String> negativeExamples = new ArrayList<String>();
    negativeExamples.addAll(readLinesFromFile("ff.txt"));

    // Opera and Safari can emulate IE - make an exception for these strings
    List<String> safariAndOperaStrings = readLinesFromFile("safari.txt");
    safariAndOperaStrings.addAll(readLinesFromFile("opera.txt"));
    for (String s : safariAndOperaStrings) {
      String sl = s.toLowerCase();
      if (sl.contains("msie") && (sl.contains("webkit") || sl.contains("opera")))
        positiveExamples.add(s); // override the expectation
      else
        negativeExamples.add(s);
    }

    checkPredicate(positiveExamples, negativeExamples, new Predicate<String>() {
      public boolean apply(String str) {
        return new UserAgent(str).isIE();
      }
    });
  }

  private void checkPredicate(List<String> positiveStrings, List<String> negativeStrings, Predicate<String> predicate) {
    assertTrue(positiveStrings.size() > 5);
    assertTrue(negativeStrings.size() > 5);
    for (String str : positiveStrings) {
      assertTrue("UA expected to pass: " + str, predicate.apply(str));
    }
    for (String str : negativeStrings) {
      assertFalse("UA expected to fail: " + str, predicate.apply(str));
    }
  }
}