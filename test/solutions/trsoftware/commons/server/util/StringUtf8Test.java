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

package solutions.trsoftware.commons.server.util;

import junit.framework.TestCase;
import solutions.trsoftware.commons.server.testutil.TestUtils;
import solutions.trsoftware.commons.shared.annotations.Slow;
import solutions.trsoftware.commons.shared.util.StringUtils;
import solutions.trsoftware.commons.shared.util.callables.Function0;

/**
 * Oct 21, 2009
 *
 * @author Alex
 */
public class StringUtf8Test extends TestCase {

  public void testConversions() throws Exception {
    checkConversion("Foo bar baz");
    checkConversion("?? ???????!");
  }

  private void checkConversion(String str) {
    assertEquals(str, new StringUtf8(str).toString());
  }

  /** Prints out how much memory and CPU the various parts of the system use in order to tune them */
  @Slow
  public void testPerformance() throws Exception {
    final int nStrings = 1000;
    final int strlen = 100;
    System.out.printf("Testing %d random strings of length %d:%n", nStrings, strlen);
    // check if storing strings as Utf8 byte arrays offers any advantages over Java strings
    TestUtils.printMemoryAndTimeUsage("  String",
        new Function0() {
          public Object call() {
            String[] result = new String[nStrings];
            for (int i = 0; i < result.length; i++)
              result[i] = StringUtils.randString(strlen);
            return result;
          }
        });

    TestUtils.printMemoryAndTimeUsage("  StringUtf8",
        new Function0() {
          public Object call() {
            StringUtf8[] result = new StringUtf8[nStrings];
            for (int i = 0; i < result.length; i++)
              result[i] = new StringUtf8(StringUtils.randString(strlen));
            return result;
          }
        });
  }
}