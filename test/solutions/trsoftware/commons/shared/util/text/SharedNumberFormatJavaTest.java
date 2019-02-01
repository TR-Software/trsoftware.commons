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

package solutions.trsoftware.commons.shared.util.text;

import junit.framework.TestCase;

import java.text.DecimalFormat;

import static solutions.trsoftware.commons.shared.util.text.SharedNumberFormat.buildPattern;

/**
 * @author Alex, 10/31/2017
 */
public class SharedNumberFormatJavaTest extends TestCase {

  SharedNumberFormatGwtTest delegate = new SharedNumberFormatGwtTest();

  public void testFormat() throws Exception {
    delegate.testFormat();
  }

  public void testParse() throws Exception {
    delegate.testParse();
  }

  public void testPercentages() throws Exception {
    delegate.testPercentages();
  }

  public void testDigitGrouping() throws Exception {
    delegate.testDigitGrouping();
  }

  public void testRounding() {
    delegate.testRounding();
  }

  public void testPct() throws Exception {
    DecimalFormat fmt = new DecimalFormat("#.##%");
    String str = fmt.format(.1234); // returns "12.34%"
    fmt.parse(str);  // returns 0.1234
    System.out.println(str);  // returns "12.34%"
    System.out.println(fmt.parse("12.34%"));  // returns 12.34
  }

  public void testBuildPattern() throws Exception {
    assertEquals("#.###", buildPattern(0, 0, 3, false));
    assertEquals("0.0#", buildPattern(1, 1, 2, false));
    assertEquals("0.00", buildPattern(1, 2, 2, false));
    assertEquals("0.00", buildPattern(1, 2, 0, false));
    assertEquals("0.#", buildPattern(1, 0, 1, false));
    assertEquals("0", buildPattern(1, 0, 0, false));
    assertEquals("#", buildPattern(0, 0, 0, false));
    assertEquals("#.###%", buildPattern(0, 0, 3, true));
    assertEquals("0.0#%", buildPattern(1, 1, 2, true));
    assertEquals("0.#%", buildPattern(1, 0, 1, true));
    assertEquals("0%", buildPattern(1, 0, 0, true));
    assertEquals("#%", buildPattern(0, 0, 0, true));
  }
}