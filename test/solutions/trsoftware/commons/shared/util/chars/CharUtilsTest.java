/*
 * Copyright 2025 TR Software Inc.
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
 */

package solutions.trsoftware.commons.shared.util.chars;

import solutions.trsoftware.commons.client.CommonsGwtTestCase;
import solutions.trsoftware.commons.shared.util.StringUtils;
import solutions.trsoftware.commons.shared.util.StringUtilsTest;

import static com.google.common.base.Strings.lenientFormat;

/**
 * @author Alex
 * @since 9/15/2025
 */
public class CharUtilsTest extends CommonsGwtTestCase {

  public void testIsLetterOrDigit() throws Exception {
    String alphanum = "aбвгё123";
    StringUtils.codePointsStream(alphanum).forEach(codePoint ->
        assertTrue(Integer.toHexString(codePoint), isLetterOrDigit(codePoint)));

    String nonAlphanum = StringUtilsTest.THREE_MONKEYS + StringUtilsTest.RTL_OVERRIDE + "!@#$%^&*()";
    getLogger().info("nonAlphanum = " + nonAlphanum);
    StringUtils.codePointsStream(nonAlphanum).forEach(codePoint ->
        assertFalse(Integer.toHexString(codePoint), isLetterOrDigit(codePoint)));

  }

  private boolean isLetterOrDigit(int codePoint) {
    boolean result = CharUtils.isLetterOrDigit(codePoint);
    getLogger().info(lenientFormat("isLetterOrDigit('%s') = %s",
        StringUtils.codePointsToString(new int[]{codePoint}), result));
    return result;
  }
}