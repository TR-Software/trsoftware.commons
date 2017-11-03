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

package solutions.trsoftware.commons.server.util;

import junit.framework.TestCase;
import solutions.trsoftware.commons.client.util.CollectionUtils;
import solutions.trsoftware.commons.server.io.ServerIOUtils;
import solutions.trsoftware.commons.server.testutil.PerformanceComparison;

import java.util.Collections;
import java.util.List;


/**
 * Oct 19, 2009
 *
 * @author Alex
 */
public class StringTokenizerJavaTest extends TestCase {
  private String aliceText;


  protected void setUp() throws Exception {
    super.setUp();
    aliceText = ServerIOUtils.readResourceFileIntoString(
        ServerIOUtils.resourceNameFromFilenameInSamePackage("aliceInWonderlandCorpus.txt", getClass()));
  }

  /**
   * Compares performance of solutions.trsoftware.commons.client.util.StringTokenizer
   * to java.util.StringTokenizer.
   */
  public void testCompareTokenization() throws Exception {
    assertEquals(javaUtilTokenize(aliceText), typeracerCommonsUtilTokenize(aliceText));
  }

  /**
   * Compares performance of solutions.trsoftware.commons.client.util.StringTokenizer
   * to java.util.StringTokenizer.
   */
  public void testComparePerformance() throws Exception {
    // assert that our implementation is at most twice as fast as java.util's
    assertTrue(2 >=
        PerformanceComparison.compare(
            new JavaTokenizeAction(), "java.util.StringTokenizer",
            new TyperacerTokenizeAction(), "solutions.trsoftware.commons.client.util.StringTokenizer",
            200));
    assertTrue(.5 <=
        PerformanceComparison.compare(
            new TyperacerTokenizeAction(), "solutions.trsoftware.commons.client.util.StringTokenizer",
            new JavaTokenizeAction(), "java.util.StringTokenizer",
            200));
  }

  /**
   * Tokenizes the given string using java.util.StringTokenizer
   *
   * @return the tokens from the given string
   */
  private List<String> typeracerCommonsUtilTokenize(String text) {
    return CollectionUtils.asList(new solutions.trsoftware.commons.client.util.StringTokenizer(text));
  }

  /**
   * Tokenizes the given string using java.util.StringTokenizer
   *
   * @return the tokens from the given string
   */
  private List javaUtilTokenize(String text) {
    return Collections.list(new java.util.StringTokenizer(text));
  }

  private class JavaTokenizeAction implements Runnable {
    public void run() {
      javaUtilTokenize(aliceText);
    }
  }

  private class TyperacerTokenizeAction implements Runnable {
    public void run() {
      typeracerCommonsUtilTokenize(aliceText);
    }
  }
}