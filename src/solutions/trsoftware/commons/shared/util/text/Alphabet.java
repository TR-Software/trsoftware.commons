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

import java.util.Set;

import static solutions.trsoftware.commons.shared.util.StringUtils.*;

/**
 * Defines some default alphabet presets.
 *
 * @author Alex, 9/20/2017
 */
public enum Alphabet {

  CUSTOM(""),
  NUMBERS("0123456789"),
  /** The chars on the QWERTY home row that fall under the standard finger positions */
  HOME_ROW_BASIC("asdfjkl;'") {
    @Override
    public String getPrettyName() {
      return "Home row (Qwerty, just the 8 fingers)";
    }
  },
  /** All chars on the QWERTY home row */
  HOME_ROW_FULL("asdfghjkl;'") {
    @Override
    public String getPrettyName() {
      return "Home row (Qwerty, full)";
    }
  },
  /** All lowercase ASCII letters (i.e. {@code [a-z]}) */
  LETTERS(new CharRange('a', 'z').toString()),
  /** All lowercase ASCII letters, numbers, and printable symbols (i.e. {@code [a-z0-9\[-~]}) */
  LETTERS_NUMBERS_AND_SYMBOLS(getAllAsciiLowercaseLettersAndPrintableSymbols(MIN_PRINTABLE_ASCII_CHAR, MAX_PRINTABLE_ASCII_CHAR)) {
    @Override
    public String getPrettyName() {
      return "Letters, numbers, and symbols";
    }
  },
  LETTERS_AND_NUMBERS(LETTERS.chars + NUMBERS.chars)
  ; // WARNING: if adding new enum values, add them at the end, so to not affect the values that might have already been persisted in a DB

  private String chars;

  Alphabet(String chars) {
    this.chars = chars;
  }

  public String getChars() {
    return chars;
  }
  
  public String getPrettyName() {
    return constantNameToTitleCase(name());
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

  public static String getAllAsciiLowercaseLettersAndPrintableSymbols(char start, char end) {
    int n = end - start + 1;  // number of chars in the alphabet
    StringBuilder str = new StringBuilder(n);
    for (int i = 0; i < n; i++) {
      char c = (char)(start + i);
      if (isAsciiLowercaseLetterOrPrintableSymbol(c))
        str.append(c);
    }
    return str.toString();
  }

  public static boolean isAsciiLowercaseLetterOrPrintableSymbol(char c) {
    return (c >= 32 && c < 65) || (c >= 91 && c < 127);
  }

  public static void main(String[] args) {
    for (Alphabet a : values()) {
      System.out.println(a.name() + ": \"" + a.chars + "\"");
    }
  }

}
