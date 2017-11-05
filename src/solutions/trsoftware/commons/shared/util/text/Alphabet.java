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

import solutions.trsoftware.commons.shared.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Defines some default alphabet presets.
 *
 * @author Alex, 9/20/2017
 */
public enum Alphabet {
  /**
   * This value exists only to support using this enum in the {@code Score.alphabet} column of the DB.
   * Instead of storing the actual alphabet string, we save storage space by storing only the {@code int} value of the enum.
   */
  CUSTOM(""),
  NUMBERS("0123456789"),
  HOME_ROW("asdfghjkl;'"),
  LETTERS(getAllLowercaseAsciiSymbols('a', 'z')),
  LETTERS_NUMBERS_AND_SYMBOLS(getAllLowercaseAsciiSymbols(StringUtils.MIN_PRINTABLE_ASCII_CHAR, StringUtils.MAX_PRINTABLE_ASCII_CHAR)) {
    @Override
    public String getPrettyName() {
      return "Letters, numbers, and symbols";
    }
  },
  LETTERS_AND_NUMBERS(LETTERS.chars + NUMBERS.chars)
  ; // WARNING: if adding new enum values, add them at the end, so to not affect the values persisted in DB under the Score table `alphabet` column

  private String chars;

  Alphabet(String chars) {
    this.chars = chars;
  }

  public String getChars() {
    return chars;
  }
  
  public String getPrettyName() {
    return StringUtils.constantNameToTitleCase(name());
  }

  /** @return The enum value matching the given chars, or {@code null} if there's no match */
  public static Alphabet lookup(String chars) {
    Set<Character> givenCharSet = toCharacterSet(chars);
    for (Alphabet alpha : values()) {
      if (givenCharSet.equals(toCharacterSet(alpha.chars)))
        return alpha;
    }
    return CUSTOM;
  }

  public static LinkedHashSet<Character> toCharacterSet(String chars) {
    return new LinkedHashSet<Character>(StringUtils.asList(chars));
  }

  public static String getAllLowercaseAsciiSymbols(char start, char end) {
    int n = end - start + 1;  // number of chars in the alphabet
    StringBuilder str = new StringBuilder(n);
    for (int i = 0; i < n; i++) {
      char c = (char)(start + i);
      if (isPrintableLowercaseAsciiSymbol(c))
        str.append(c);
    }
    return str.toString();
  }

  public static String getAllPrintableAsciiChars() {
    char start = StringUtils.MIN_PRINTABLE_ASCII_CHAR;
    char end = StringUtils.MAX_PRINTABLE_ASCII_CHAR;
    int n = end - start + 1;  // number of chars in the alphabet
    StringBuilder str = new StringBuilder(n);
    for (int i = 0; i < n; i++) {
      str.append((char)(start + i));
    }
    return str.toString();
  }

  public static boolean isPrintableLowercaseAsciiSymbol(char c) {
    return (c >= 32 && c < 65) || (c >= 91 && c < 127);
  }

  public static void main(String[] args) {
    for (Alphabet a : values()) {
      System.out.println(a.name() + ": \"" + a.chars + "\"");
    }
  }

}
