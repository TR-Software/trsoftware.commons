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

package solutions.trsoftware.commons.shared.util.text;

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.util.StringUtils;

/**
 * @author Alex, 9/20/2017
 */
public class AlphabetTest extends TestCase {

  public void testGetPrettyName() throws Exception {
    assertEquals("Numbers", Alphabet.NUMBERS.getPrettyName());
    assertEquals("Home row", Alphabet.HOME_ROW_FULL.getPrettyName());
    assertEquals("Letters", Alphabet.LETTERS.getPrettyName());
    assertEquals("Letters and numbers", Alphabet.LETTERS_AND_NUMBERS.getPrettyName());
    assertEquals("Letters, numbers, and symbols", Alphabet.LETTERS_NUMBERS_AND_SYMBOLS.getPrettyName());
  }

  public void testLookup() throws Exception {
    assertLookupEquals(Alphabet.CUSTOM, "");
    assertLookupEquals(Alphabet.CUSTOM, "foobar");
    assertLookupEquals(Alphabet.NUMBERS, "0123456789");
    assertLookupEquals(Alphabet.HOME_ROW_FULL, "asdfghjkl;'");
    assertLookupEquals(Alphabet.LETTERS, Alphabet.getAllAsciiLowercaseLettersAndPrintableSymbols('a', 'z'));
    assertLookupEquals(Alphabet.LETTERS_NUMBERS_AND_SYMBOLS, Alphabet.getAllAsciiLowercaseLettersAndPrintableSymbols(' ', '~'));
    assertLookupEquals(Alphabet.LETTERS_AND_NUMBERS, Alphabet.getAllAsciiLowercaseLettersAndPrintableSymbols('a', 'z') + Alphabet.getAllAsciiLowercaseLettersAndPrintableSymbols('0', '9'));
  }

  private static void assertLookupEquals(Alphabet enumValue, String chars) {
    assertEquals(enumValue, Alphabet.lookup(chars));
    // attempt a lookup with the same chars but in a different order
    assertEquals(enumValue, Alphabet.lookup(StringUtils.reverse(chars)));
  }

}