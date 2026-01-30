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

/*
 * Original copyright notice for the code in JDK8:
 * --------------------------------------------------------------------------------
 * Copyright (c) 2002, 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package solutions.trsoftware.commons.shared.util.chars;

import static solutions.trsoftware.commons.shared.util.chars.CharConstants.*;

/**
 * @author Alex
 * @since 9/15/2025
 */
public class CharUtils {
  // GWT-compatible emulated version of solutions.trsoftware.commons.shared.util.chars.CharUtils

  /**
   * Determines if the specified character is a letter or digit.
   * <p>
   * A character is considered to be a letter or digit if either
   * {@code Character.isLetter(char ch)} or
   * {@code Character.isDigit(char ch)} returns
   * {@code true} for the character.
   *
   * <p><b>Note:</b> This method cannot handle <a
   * href="#supplementary"> supplementary characters</a>. To support
   * all Unicode characters, including supplementary characters, use
   * the {@link #isLetterOrDigit(int)} method.
   *
   * @param   ch   the character to be tested.
   * @return  {@code true} if the character is a letter or digit;
   *          {@code false} otherwise.
   * @see     Character#isDigit(char)
   * @see     Character#isJavaIdentifierPart(char)
   * @see     Character#isJavaLetter(char)
   * @see     Character#isJavaLetterOrDigit(char)
   * @see     Character#isLetter(char)
   * @see     Character#isUnicodeIdentifierPart(char)
   * @since   1.0.2
   */
  // NOTE: the GWT-emulated version of this method doesn't properly handle characters outside of ASCII (see https://github.com/gwtproject/gwt/blob/c32238861c4d58bc559d303ab44f91ddd4e10685/user/super/com/google/gwt/emul/java/lang/Character.java#L56-L62)
  public static boolean isLetterOrDigit(char ch) {
      return isLetterOrDigit((int)ch);
  }

  /**
   * Determines if the specified character (Unicode code point) is a letter or digit.
   * <p>
   * A character is considered to be a letter or digit if either
   * {@link #isLetter(int) isLetter(codePoint)} or
   * {@link #isDigit(int) isDigit(codePoint)} returns
   * {@code true} for the character.
   *
   * @param codePoint the character (Unicode code point) to be tested.
   * @return {@code true} if the character is a letter or digit;
   *     {@code false} otherwise.
   * @see Character#isDigit(int)
   * @see Character#isJavaIdentifierPart(int)
   * @see Character#isLetter(int)
   * @see Character#isUnicodeIdentifierPart(int)
   * @since 1.5
   */
  public static boolean isLetterOrDigit(int codePoint) {
    return ((((1 << CharConstants.UPPERCASE_LETTER) |
        (1 << CharConstants.LOWERCASE_LETTER) |
        (1 << CharConstants.TITLECASE_LETTER) |
        (1 << CharConstants.MODIFIER_LETTER) |
        (1 << CharConstants.OTHER_LETTER) |
        (1 << CharConstants.DECIMAL_DIGIT_NUMBER)) >> getType(codePoint)) & 1)
        != 0;
  }

  /**
   * Returns a value indicating a character's general category.
   *
   * @param codePoint the character (Unicode code point) to be tested.
   * @return a value of type {@code int} representing the
   *     character's general category.
   * @see Character#COMBINING_SPACING_MARK COMBINING_SPACING_MARK
   * @see Character#CONNECTOR_PUNCTUATION CONNECTOR_PUNCTUATION
   * @see Character#CONTROL CONTROL
   * @see Character#CURRENCY_SYMBOL CURRENCY_SYMBOL
   * @see Character#DASH_PUNCTUATION DASH_PUNCTUATION
   * @see Character#DECIMAL_DIGIT_NUMBER DECIMAL_DIGIT_NUMBER
   * @see Character#ENCLOSING_MARK ENCLOSING_MARK
   * @see Character#END_PUNCTUATION END_PUNCTUATION
   * @see Character#FINAL_QUOTE_PUNCTUATION FINAL_QUOTE_PUNCTUATION
   * @see Character#FORMAT FORMAT
   * @see Character#INITIAL_QUOTE_PUNCTUATION INITIAL_QUOTE_PUNCTUATION
   * @see Character#LETTER_NUMBER LETTER_NUMBER
   * @see Character#LINE_SEPARATOR LINE_SEPARATOR
   * @see Character#LOWERCASE_LETTER LOWERCASE_LETTER
   * @see Character#MATH_SYMBOL MATH_SYMBOL
   * @see Character#MODIFIER_LETTER MODIFIER_LETTER
   * @see Character#MODIFIER_SYMBOL MODIFIER_SYMBOL
   * @see Character#NON_SPACING_MARK NON_SPACING_MARK
   * @see Character#OTHER_LETTER OTHER_LETTER
   * @see Character#OTHER_NUMBER OTHER_NUMBER
   * @see Character#OTHER_PUNCTUATION OTHER_PUNCTUATION
   * @see Character#OTHER_SYMBOL OTHER_SYMBOL
   * @see Character#PARAGRAPH_SEPARATOR PARAGRAPH_SEPARATOR
   * @see Character#PRIVATE_USE PRIVATE_USE
   * @see Character#SPACE_SEPARATOR SPACE_SEPARATOR
   * @see Character#START_PUNCTUATION START_PUNCTUATION
   * @see Character#SURROGATE SURROGATE
   * @see Character#TITLECASE_LETTER TITLECASE_LETTER
   * @see Character#UNASSIGNED UNASSIGNED
   * @see Character#UPPERCASE_LETTER UPPERCASE_LETTER
   * @since 1.5
   */
  public static int getType(int codePoint) {
    return CharacterData.of(codePoint).getType(codePoint);
  }


  /**
   * Determines if the specified character (Unicode code point) is a
   * lowercase character.
   * <p>
   * A character is lowercase if its general category type, provided
   * by {@link Character#getType getType(codePoint)}, is
   * {@code LOWERCASE_LETTER}, or it has contributory property
   * Other_Lowercase as defined by the Unicode Standard.
   * <p>
   * The following are examples of lowercase characters:
   * <blockquote><pre>
   * a b c d e f g h i j k l m n o p q r s t u v w x y z
   * '&#92;u00DF' '&#92;u00E0' '&#92;u00E1' '&#92;u00E2' '&#92;u00E3' '&#92;u00E4' '&#92;u00E5' '&#92;u00E6'
   * '&#92;u00E7' '&#92;u00E8' '&#92;u00E9' '&#92;u00EA' '&#92;u00EB' '&#92;u00EC' '&#92;u00ED' '&#92;u00EE'
   * '&#92;u00EF' '&#92;u00F0' '&#92;u00F1' '&#92;u00F2' '&#92;u00F3' '&#92;u00F4' '&#92;u00F5' '&#92;u00F6'
   * '&#92;u00F8' '&#92;u00F9' '&#92;u00FA' '&#92;u00FB' '&#92;u00FC' '&#92;u00FD' '&#92;u00FE' '&#92;u00FF'
   * </pre></blockquote>
   * <p> Many other Unicode characters are lowercase too.
   *
   * @param codePoint the character (Unicode code point) to be tested.
   * @return {@code true} if the character is lowercase;
   *     {@code false} otherwise.
   * @see Character#isLowerCase(int)
   * @see Character#isTitleCase(int)
   * @see Character#toLowerCase(int)
   * @see Character#getType(int)
   * @since 1.5
   */
  public static boolean isLowerCase(int codePoint) {
    return getType(codePoint) == LOWERCASE_LETTER ||
        CharacterData.of(codePoint).isOtherLowercase(codePoint);
  }

  /**
   * Determines if the specified character (Unicode code point) is an uppercase character.
   * <p>
   * A character is uppercase if its general category type, provided by
   * {@link Character#getType(int) getType(codePoint)}, is {@code UPPERCASE_LETTER},
   * or it has contributory property Other_Uppercase as defined by the Unicode Standard.
   * <p>
   * The following are examples of uppercase characters:
   * <blockquote><pre>
   * A B C D E F G H I J K L M N O P Q R S T U V W X Y Z
   * '&#92;u00C0' '&#92;u00C1' '&#92;u00C2' '&#92;u00C3' '&#92;u00C4' '&#92;u00C5' '&#92;u00C6' '&#92;u00C7'
   * '&#92;u00C8' '&#92;u00C9' '&#92;u00CA' '&#92;u00CB' '&#92;u00CC' '&#92;u00CD' '&#92;u00CE' '&#92;u00CF'
   * '&#92;u00D0' '&#92;u00D1' '&#92;u00D2' '&#92;u00D3' '&#92;u00D4' '&#92;u00D5' '&#92;u00D6' '&#92;u00D8'
   * '&#92;u00D9' '&#92;u00DA' '&#92;u00DB' '&#92;u00DC' '&#92;u00DD' '&#92;u00DE'
   * </pre></blockquote>
   * <p> Many other Unicode characters are uppercase too.<p>
   *
   * @param codePoint the character (Unicode code point) to be tested.
   * @return {@code true} if the character is uppercase;
   *     {@code false} otherwise.
   * @see Character#isLowerCase(int)
   * @see Character#isTitleCase(int)
   * @see Character#toUpperCase(int)
   * @see Character#getType(int)
   * @since 1.5
   */
  public static boolean isUpperCase(int codePoint) {
    return getType(codePoint) == UPPERCASE_LETTER ||
        CharacterData.of(codePoint).isOtherUppercase(codePoint);
  }

  /**
   * Determines if the specified character is a titlecase character.
   * <p>
   * A character is a titlecase character if its general
   * category type, provided by {@code Character.getType(ch)},
   * is {@code TITLECASE_LETTER}.
   * <p>
   * Some characters look like pairs of Latin letters. For example, there
   * is an uppercase letter that looks like "LJ" and has a corresponding
   * lowercase letter that looks like "lj". A third form, which looks like "Lj",
   * is the appropriate form to use when rendering a word in lowercase
   * with initial capitals, as for a book title.
   * <p>
   * These are some of the Unicode characters for which this method returns
   * {@code true}:
   * <ul>
   * <li>{@code LATIN CAPITAL LETTER D WITH SMALL LETTER Z WITH CARON}
   * <li>{@code LATIN CAPITAL LETTER L WITH SMALL LETTER J}
   * <li>{@code LATIN CAPITAL LETTER N WITH SMALL LETTER J}
   * <li>{@code LATIN CAPITAL LETTER D WITH SMALL LETTER Z}
   * </ul>
   * <p> Many other Unicode characters are titlecase too.
   *
   * <p><b>Note:</b> This method cannot handle <a
   * href="#supplementary"> supplementary characters</a>. To support
   * all Unicode characters, including supplementary characters, use
   * the {@link #isTitleCase(int)} method.
   *
   * @param ch the character to be tested.
   * @return {@code true} if the character is titlecase;
   *     {@code false} otherwise.
   * @see Character#isLowerCase(char)
   * @see Character#isUpperCase(char)
   * @see Character#toTitleCase(char)
   * @see Character#getType(char)
   * @since 1.0.2
   */
  public static boolean isTitleCase(char ch) {  // TODO(9/17/2025): this method is available in GWT
    return isTitleCase((int)ch);
  }

  /**
   * Determines if the specified character (Unicode code point) is a titlecase character.
   * <p>
   * A character is a titlecase character if its general
   * category type, provided by {@link Character#getType(int) getType(codePoint)},
   * is {@code TITLECASE_LETTER}.
   * <p>
   * Some characters look like pairs of Latin letters. For example, there
   * is an uppercase letter that looks like "LJ" and has a corresponding
   * lowercase letter that looks like "lj". A third form, which looks like "Lj",
   * is the appropriate form to use when rendering a word in lowercase
   * with initial capitals, as for a book title.
   * <p>
   * These are some of the Unicode characters for which this method returns
   * {@code true}:
   * <ul>
   * <li>{@code LATIN CAPITAL LETTER D WITH SMALL LETTER Z WITH CARON}
   * <li>{@code LATIN CAPITAL LETTER L WITH SMALL LETTER J}
   * <li>{@code LATIN CAPITAL LETTER N WITH SMALL LETTER J}
   * <li>{@code LATIN CAPITAL LETTER D WITH SMALL LETTER Z}
   * </ul>
   * <p> Many other Unicode characters are titlecase too.<p>
   *
   * @param codePoint the character (Unicode code point) to be tested.
   * @return {@code true} if the character is titlecase;
   *     {@code false} otherwise.
   * @see Character#isLowerCase(int)
   * @see Character#isUpperCase(int)
   * @see Character#toTitleCase(int)
   * @see Character#getType(int)
   * @since 1.5
   */
  public static boolean isTitleCase(int codePoint) {
    return getType(codePoint) == TITLECASE_LETTER;
  }

  /**
   * Determines if the specified character is a digit.
   * <p>
   * A character is a digit if its general category type, provided
   * by {@code Character.getType(ch)}, is
   * {@code DECIMAL_DIGIT_NUMBER}.
   * <p>
   * Some Unicode character ranges that contain digits:
   * <ul>
   * <li>{@code '\u005Cu0030'} through {@code '\u005Cu0039'},
   *     ISO-LATIN-1 digits ({@code '0'} through {@code '9'})
   * <li>{@code '\u005Cu0660'} through {@code '\u005Cu0669'},
   *     Arabic-Indic digits
   * <li>{@code '\u005Cu06F0'} through {@code '\u005Cu06F9'},
   *     Extended Arabic-Indic digits
   * <li>{@code '\u005Cu0966'} through {@code '\u005Cu096F'},
   *     Devanagari digits
   * <li>{@code '\u005CuFF10'} through {@code '\u005CuFF19'},
   *     Fullwidth digits
   * </ul>
   *
   * Many other character ranges contain digits as well.
   *
   * <p><b>Note:</b> This method cannot handle <a
   * href="#supplementary"> supplementary characters</a>. To support
   * all Unicode characters, including supplementary characters, use
   * the {@link #isDigit(int)} method.
   *
   * @param   ch   the character to be tested.
   * @return  {@code true} if the character is a digit;
   *          {@code false} otherwise.
   * @see     Character#digit(char, int)
   * @see     Character#forDigit(int, int)
   * @see     Character#getType(char)
   */
  // NOTE: the GWT-emulated version of this method doesn't properly handle characters outside of ASCII (see https://github.com/gwtproject/gwt/blob/c32238861c4d58bc559d303ab44f91ddd4e10685/user/super/com/google/gwt/emul/java/lang/Character.java#L56-L62)
  public static boolean isDigit(char ch) {
      return isDigit((int)ch);
  }

  /**
   * Determines if the specified character (Unicode code point) is a digit.
   * <p>
   * A character is a digit if its general category type, provided
   * by {@link Character#getType(int) getType(codePoint)}, is
   * {@code DECIMAL_DIGIT_NUMBER}.
   * <p>
   * Some Unicode character ranges that contain digits:
   * <ul>
   * <li>{@code '\u005Cu0030'} through {@code '\u005Cu0039'},
   *     ISO-LATIN-1 digits ({@code '0'} through {@code '9'})
   * <li>{@code '\u005Cu0660'} through {@code '\u005Cu0669'},
   *     Arabic-Indic digits
   * <li>{@code '\u005Cu06F0'} through {@code '\u005Cu06F9'},
   *     Extended Arabic-Indic digits
   * <li>{@code '\u005Cu0966'} through {@code '\u005Cu096F'},
   *     Devanagari digits
   * <li>{@code '\u005CuFF10'} through {@code '\u005CuFF19'},
   *     Fullwidth digits
   * </ul>
   * <p>
   * Many other character ranges contain digits as well.
   *
   * @param codePoint the character (Unicode code point) to be tested.
   * @return {@code true} if the character is a digit;
   *     {@code false} otherwise.
   * @see Character#forDigit(int, int)
   * @see Character#getType(int)
   * @since 1.5
   */
  public static boolean isDigit(int codePoint) {
    return getType(codePoint) == DECIMAL_DIGIT_NUMBER;
  }

  /**
   * Determines if a character (Unicode code point) is defined in Unicode.
   * <p>
   * A character is defined if at least one of the following is true:
   * <ul>
   * <li>It has an entry in the UnicodeData file.
   * <li>It has a value in a range defined by the UnicodeData file.
   * </ul>
   *
   * @param codePoint the character (Unicode code point) to be tested.
   * @return {@code true} if the character has a defined meaning
   *     in Unicode; {@code false} otherwise.
   * @see Character#isDigit(int)
   * @see Character#isLetter(int)
   * @see Character#isLetterOrDigit(int)
   * @see Character#isLowerCase(int)
   * @see Character#isTitleCase(int)
   * @see Character#isUpperCase(int)
   * @since 1.5
   */
  public static boolean isDefined(int codePoint) {
    return getType(codePoint) != UNASSIGNED;
  }

  /**
   * Determines if the specified character is a letter.
   * <p>
   * A character is considered to be a letter if its general
   * category type, provided by {@code Character.getType(ch)},
   * is any of the following:
   * <ul>
   * <li> {@code UPPERCASE_LETTER}
   * <li> {@code LOWERCASE_LETTER}
   * <li> {@code TITLECASE_LETTER}
   * <li> {@code MODIFIER_LETTER}
   * <li> {@code OTHER_LETTER}
   * </ul>
   *
   * Not all letters have case. Many characters are
   * letters but are neither uppercase nor lowercase nor titlecase.
   *
   * <p><b>Note:</b> This method cannot handle <a
   * href="#supplementary"> supplementary characters</a>. To support
   * all Unicode characters, including supplementary characters, use
   * the {@link #isLetter(int)} method.
   *
   * @param   ch   the character to be tested.
   * @return  {@code true} if the character is a letter;
   *          {@code false} otherwise.
   * @see     Character#isDigit(char)
   * @see     Character#isJavaIdentifierStart(char)
   * @see     Character#isJavaLetter(char)
   * @see     Character#isJavaLetterOrDigit(char)
   * @see     Character#isLetterOrDigit(char)
   * @see     Character#isLowerCase(char)
   * @see     Character#isTitleCase(char)
   * @see     Character#isUnicodeIdentifierStart(char)
   * @see     Character#isUpperCase(char)
   */
  // NOTE: the GWT-emulated version of this method doesn't properly handle characters outside of ASCII (see https://github.com/gwtproject/gwt/blob/c32238861c4d58bc559d303ab44f91ddd4e10685/user/super/com/google/gwt/emul/java/lang/Character.java#L56-L62)
  public static boolean isLetter(char ch) {
      return isLetter((int)ch);
  }

  /**
   * Determines if the specified character (Unicode code point) is a letter.
   * <p>
   * A character is considered to be a letter if its general
   * category type, provided by {@link Character#getType(int) getType(codePoint)},
   * is any of the following:
   * <ul>
   * <li> {@code UPPERCASE_LETTER}
   * <li> {@code LOWERCASE_LETTER}
   * <li> {@code TITLECASE_LETTER}
   * <li> {@code MODIFIER_LETTER}
   * <li> {@code OTHER_LETTER}
   * </ul>
   * <p>
   * Not all letters have case. Many characters are
   * letters but are neither uppercase nor lowercase nor titlecase.
   *
   * @param codePoint the character (Unicode code point) to be tested.
   * @return {@code true} if the character is a letter;
   *     {@code false} otherwise.
   * @see Character#isDigit(int)
   * @see Character#isJavaIdentifierStart(int)
   * @see Character#isLetterOrDigit(int)
   * @see Character#isLowerCase(int)
   * @see Character#isTitleCase(int)
   * @see Character#isUnicodeIdentifierStart(int)
   * @see Character#isUpperCase(int)
   * @since 1.5
   */
  public static boolean isLetter(int codePoint) {
    return ((((1 << UPPERCASE_LETTER) |
        (1 << LOWERCASE_LETTER) |
        (1 << TITLECASE_LETTER) |
        (1 << MODIFIER_LETTER) |
        (1 << OTHER_LETTER)) >> getType(codePoint)) & 1)
        != 0;
  }

  /**
   * Determines if the specified character (Unicode code point) is an alphabet.
   * <p>
   * A character is considered to be alphabetic if its general category type,
   * provided by {@link Character#getType(int) getType(codePoint)}, is any of
   * the following:
   * <ul>
   * <li> <code>UPPERCASE_LETTER</code>
   * <li> <code>LOWERCASE_LETTER</code>
   * <li> <code>TITLECASE_LETTER</code>
   * <li> <code>MODIFIER_LETTER</code>
   * <li> <code>OTHER_LETTER</code>
   * <li> <code>LETTER_NUMBER</code>
   * </ul>
   * or it has contributory property Other_Alphabetic as defined by the
   * Unicode Standard.
   *
   * @param codePoint the character (Unicode code point) to be tested.
   * @return <code>true</code> if the character is a Unicode alphabet
   *     character, <code>false</code> otherwise.
   * @since 1.7
   */
  public static boolean isAlphabetic(int codePoint) {
    return (((((1 << UPPERCASE_LETTER) |
        (1 << LOWERCASE_LETTER) |
        (1 << TITLECASE_LETTER) |
        (1 << MODIFIER_LETTER) |
        (1 << OTHER_LETTER) |
        (1 << LETTER_NUMBER)) >> getType(codePoint)) & 1) != 0) ||
        CharacterData.of(codePoint).isOtherAlphabetic(codePoint);
  }

  /**
   * Determines if the specified character (Unicode code point) is a CJKV
   * (Chinese, Japanese, Korean and Vietnamese) ideograph, as defined by
   * the Unicode Standard.
   *
   * @param codePoint the character (Unicode code point) to be tested.
   * @return <code>true</code> if the character is a Unicode ideograph
   *     character, <code>false</code> otherwise.
   * @since 1.7
   */
  public static boolean isIdeographic(int codePoint) {
    return CharacterData.of(codePoint).isIdeographic(codePoint);
  }

  /**
   * Converts the character argument to lowercase using case
   * mapping information from the UnicodeData file.
   * <p>
   * Note that
   * {@code Character.isLowerCase(Character.toLowerCase(ch))}
   * does not always return {@code true} for some ranges of
   * characters, particularly those that are symbols or ideographs.
   *
   * <p>In general, {@link String#toLowerCase()} should be used to map
   * characters to lowercase. {@code String} case mapping methods
   * have several benefits over {@code Character} case mapping methods.
   * {@code String} case mapping methods can perform locale-sensitive
   * mappings, context-sensitive mappings, and 1:M character mappings, whereas
   * the {@code Character} case mapping methods cannot.
   *
   * <p><b>Note:</b> This method cannot handle <a
   * href="#supplementary"> supplementary characters</a>. To support
   * all Unicode characters, including supplementary characters, use
   * the {@link #toLowerCase(int)} method.
   *
   * @param   ch   the character to be converted.
   * @return  the lowercase equivalent of the character, if any;
   *          otherwise, the character itself.
   * @see     Character#isLowerCase(char)
   * @see     String#toLowerCase()
   */
  // NOTE: the GWT-emulated version of this method doesn't properly handle characters outside of ASCII (see https://github.com/gwtproject/gwt/blob/c32238861c4d58bc559d303ab44f91ddd4e10685/user/super/com/google/gwt/emul/java/lang/Character.java#L56-L62)
  public static char toLowerCase(char ch) {
      return (char)toLowerCase((int)ch);
  }

  /**
   * Converts the character (Unicode code point) argument to
   * lowercase using case mapping information from the UnicodeData
   * file.
   *
   * <p> Note that
   * {@code Character.isLowerCase(Character.toLowerCase(codePoint))}
   * does not always return {@code true} for some ranges of
   * characters, particularly those that are symbols or ideographs.
   *
   * <p>In general, {@link String#toLowerCase()} should be used to map
   * characters to lowercase. {@code String} case mapping methods
   * have several benefits over {@code Character} case mapping methods.
   * {@code String} case mapping methods can perform locale-sensitive
   * mappings, context-sensitive mappings, and 1:M character mappings, whereas
   * the {@code Character} case mapping methods cannot.
   *
   * @param codePoint the character (Unicode code point) to be converted.
   * @return the lowercase equivalent of the character (Unicode code
   *     point), if any; otherwise, the character itself.
   * @see Character#isLowerCase(int)
   * @see String#toLowerCase()
   * @since 1.5
   */
  public static int toLowerCase(int codePoint) {
    return CharacterData.of(codePoint).toLowerCase(codePoint);
  }

  /**
   * Converts the character argument to uppercase using case mapping
   * information from the UnicodeData file.
   * <p>
   * Note that
   * {@code Character.isUpperCase(Character.toUpperCase(ch))}
   * does not always return {@code true} for some ranges of
   * characters, particularly those that are symbols or ideographs.
   *
   * <p>In general, {@link String#toUpperCase()} should be used to map
   * characters to uppercase. {@code String} case mapping methods
   * have several benefits over {@code Character} case mapping methods.
   * {@code String} case mapping methods can perform locale-sensitive
   * mappings, context-sensitive mappings, and 1:M character mappings, whereas
   * the {@code Character} case mapping methods cannot.
   *
   * <p><b>Note:</b> This method cannot handle <a
   * href="#supplementary"> supplementary characters</a>. To support
   * all Unicode characters, including supplementary characters, use
   * the {@link #toUpperCase(int)} method.
   *
   * @param   ch   the character to be converted.
   * @return  the uppercase equivalent of the character, if any;
   *          otherwise, the character itself.
   * @see     Character#isUpperCase(char)
   * @see     String#toUpperCase()
   */
  // NOTE: the GWT-emulated version of this method doesn't properly handle characters outside of ASCII (see https://github.com/gwtproject/gwt/blob/c32238861c4d58bc559d303ab44f91ddd4e10685/user/super/com/google/gwt/emul/java/lang/Character.java#L56-L62)
  public static char toUpperCase(char ch) {
      return (char)toUpperCase((int)ch);
  }

  /**
   * Converts the character (Unicode code point) argument to
   * uppercase using case mapping information from the UnicodeData
   * file.
   *
   * <p>Note that
   * {@code Character.isUpperCase(Character.toUpperCase(codePoint))}
   * does not always return {@code true} for some ranges of
   * characters, particularly those that are symbols or ideographs.
   *
   * <p>In general, {@link String#toUpperCase()} should be used to map
   * characters to uppercase. {@code String} case mapping methods
   * have several benefits over {@code Character} case mapping methods.
   * {@code String} case mapping methods can perform locale-sensitive
   * mappings, context-sensitive mappings, and 1:M character mappings, whereas
   * the {@code Character} case mapping methods cannot.
   *
   * @param codePoint the character (Unicode code point) to be converted.
   * @return the uppercase equivalent of the character, if any;
   *     otherwise, the character itself.
   * @see Character#isUpperCase(int)
   * @see String#toUpperCase()
   * @since 1.5
   */
  public static int toUpperCase(int codePoint) {
    return CharacterData.of(codePoint).toUpperCase(codePoint);
  }

  /**
   * Converts the character argument to titlecase using case mapping
   * information from the UnicodeData file. If a character has no
   * explicit titlecase mapping and is not itself a titlecase char
   * according to UnicodeData, then the uppercase mapping is
   * returned as an equivalent titlecase mapping. If the
   * {@code char} argument is already a titlecase
   * {@code char}, the same {@code char} value will be
   * returned.
   * <p>
   * Note that
   * {@code Character.isTitleCase(Character.toTitleCase(ch))}
   * does not always return {@code true} for some ranges of
   * characters.
   *
   * <p><b>Note:</b> This method cannot handle <a
   * href="#supplementary"> supplementary characters</a>. To support
   * all Unicode characters, including supplementary characters, use
   * the {@link #toTitleCase(int)} method.
   *
   * @param ch the character to be converted.
   * @return the titlecase equivalent of the character, if any;
   *     otherwise, the character itself.
   * @see Character#isTitleCase(char)
   * @see Character#toLowerCase(char)
   * @see Character#toUpperCase(char)
   * @since 1.0.2
   */
  public static char toTitleCase(char ch) {
    return (char)toTitleCase((int)ch);
  }

  /**
   * Converts the character (Unicode code point) argument to titlecase using case mapping
   * information from the UnicodeData file. If a character has no
   * explicit titlecase mapping and is not itself a titlecase char
   * according to UnicodeData, then the uppercase mapping is
   * returned as an equivalent titlecase mapping. If the
   * character argument is already a titlecase
   * character, the same character value will be
   * returned.
   *
   * <p>Note that
   * {@code Character.isTitleCase(Character.toTitleCase(codePoint))}
   * does not always return {@code true} for some ranges of
   * characters.
   *
   * @param codePoint the character (Unicode code point) to be converted.
   * @return the titlecase equivalent of the character, if any;
   *     otherwise, the character itself.
   * @see Character#isTitleCase(int)
   * @see Character#toLowerCase(int)
   * @see Character#toUpperCase(int)
   * @since 1.5
   */
  public static int toTitleCase(int codePoint) {
    return CharacterData.of(codePoint).toTitleCase(codePoint);
  }

  /**
   * Returns the numeric value of the character {@code ch} in the
   * specified radix.
   * <p>
   * If the radix is not in the range {@code MIN_RADIX} &le;
   * {@code radix} &le; {@code MAX_RADIX} or if the
   * value of {@code ch} is not a valid digit in the specified
   * radix, {@code -1} is returned. A character is a valid digit
   * if at least one of the following is true:
   * <ul>
   * <li>The method {@code isDigit} is {@code true} of the character
   *     and the Unicode decimal digit value of the character (or its
   *     single-character decomposition) is less than the specified radix.
   *     In this case the decimal digit value is returned.
   * <li>The character is one of the uppercase Latin letters
   *     {@code 'A'} through {@code 'Z'} and its code is less than
   *     {@code radix + 'A' - 10}.
   *     In this case, {@code ch - 'A' + 10}
   *     is returned.
   * <li>The character is one of the lowercase Latin letters
   *     {@code 'a'} through {@code 'z'} and its code is less than
   *     {@code radix + 'a' - 10}.
   *     In this case, {@code ch - 'a' + 10}
   *     is returned.
   * <li>The character is one of the fullwidth uppercase Latin letters A
   *     ({@code '\u005CuFF21'}) through Z ({@code '\u005CuFF3A'})
   *     and its code is less than
   *     {@code radix + '\u005CuFF21' - 10}.
   *     In this case, {@code ch - '\u005CuFF21' + 10}
   *     is returned.
   * <li>The character is one of the fullwidth lowercase Latin letters a
   *     ({@code '\u005CuFF41'}) through z ({@code '\u005CuFF5A'})
   *     and its code is less than
   *     {@code radix + '\u005CuFF41' - 10}.
   *     In this case, {@code ch - '\u005CuFF41' + 10}
   *     is returned.
   * </ul>
   *
   * <p><b>Note:</b> This method cannot handle <a
   * href="#supplementary"> supplementary characters</a>. To support
   * all Unicode characters, including supplementary characters, use
   * the {@link #digit(int, int)} method.
   *
   * @param   ch      the character to be converted.
   * @param   radix   the radix.
   * @return  the numeric value represented by the character in the
   *          specified radix.
   * @see     Character#forDigit(int, int)
   * @see     Character#isDigit(char)
   */
  // NOTE: the GWT-emulated version of this method doesn't properly handle characters outside of ASCII (see https://github.com/gwtproject/gwt/blob/c32238861c4d58bc559d303ab44f91ddd4e10685/user/super/com/google/gwt/emul/java/lang/Character.java#L56-L62)
  public static int digit(char ch, int radix) {
      return digit((int)ch, radix);
  }

  /**
   * Returns the numeric value of the specified character (Unicode
   * code point) in the specified radix.
   *
   * <p>If the radix is not in the range {@code MIN_RADIX} &le;
   * {@code radix} &le; {@code MAX_RADIX} or if the
   * character is not a valid digit in the specified
   * radix, {@code -1} is returned. A character is a valid digit
   * if at least one of the following is true:
   * <ul>
   * <li>The method {@link #isDigit(int) isDigit(codePoint)} is {@code true} of the character
   *     and the Unicode decimal digit value of the character (or its
   *     single-character decomposition) is less than the specified radix.
   *     In this case the decimal digit value is returned.
   * <li>The character is one of the uppercase Latin letters
   *     {@code 'A'} through {@code 'Z'} and its code is less than
   *     {@code radix + 'A' - 10}.
   *     In this case, {@code codePoint - 'A' + 10}
   *     is returned.
   * <li>The character is one of the lowercase Latin letters
   *     {@code 'a'} through {@code 'z'} and its code is less than
   *     {@code radix + 'a' - 10}.
   *     In this case, {@code codePoint - 'a' + 10}
   *     is returned.
   * <li>The character is one of the fullwidth uppercase Latin letters A
   *     ({@code '\u005CuFF21'}) through Z ({@code '\u005CuFF3A'})
   *     and its code is less than
   *     {@code radix + '\u005CuFF21' - 10}.
   *     In this case,
   *     {@code codePoint - '\u005CuFF21' + 10}
   *     is returned.
   * <li>The character is one of the fullwidth lowercase Latin letters a
   *     ({@code '\u005CuFF41'}) through z ({@code '\u005CuFF5A'})
   *     and its code is less than
   *     {@code radix + '\u005CuFF41'- 10}.
   *     In this case,
   *     {@code codePoint - '\u005CuFF41' + 10}
   *     is returned.
   * </ul>
   *
   * @param codePoint the character (Unicode code point) to be converted.
   * @param radix the radix.
   * @return the numeric value represented by the character in the
   *     specified radix.
   * @see Character#forDigit(int, int)
   * @see Character#isDigit(int)
   * @since 1.5
   */
  public static int digit(int codePoint, int radix) {
    return CharacterData.of(codePoint).digit(codePoint, radix);
  }

  /**
   * Returns the {@code int} value that the specified
   * character (Unicode code point) represents. For example, the character
   * {@code '\u005Cu216C'} (the Roman numeral fifty) will return
   * an {@code int} with a value of 50.
   * <p>
   * The letters A-Z in their uppercase ({@code '\u005Cu0041'} through
   * {@code '\u005Cu005A'}), lowercase
   * ({@code '\u005Cu0061'} through {@code '\u005Cu007A'}), and
   * full width variant ({@code '\u005CuFF21'} through
   * {@code '\u005CuFF3A'} and {@code '\u005CuFF41'} through
   * {@code '\u005CuFF5A'}) forms have numeric values from 10
   * through 35. This is independent of the Unicode specification,
   * which does not assign numeric values to these {@code char}
   * values.
   * <p>
   * If the character does not have a numeric value, then -1 is returned.
   * If the character has a numeric value that cannot be represented as a
   * nonnegative integer (for example, a fractional value), then -2
   * is returned.
   *
   * @param codePoint the character (Unicode code point) to be converted.
   * @return the numeric value of the character, as a nonnegative {@code int}
   *     value; -2 if the character has a numeric value that is not a
   *     nonnegative integer; -1 if the character has no numeric value.
   * @see Character#forDigit(int, int)
   * @see Character#isDigit(int)
   * @since 1.5
   */
  public static int getNumericValue(int codePoint) {
    return CharacterData.of(codePoint).getNumericValue(codePoint);
  }

  /**
   * Determines if the specified character (Unicode code point) is a
   * Unicode space character.  A character is considered to be a
   * space character if and only if it is specified to be a space
   * character by the Unicode Standard. This method returns true if
   * the character's general category type is any of the following:
   *
   * <ul>
   * <li> {@link #SPACE_SEPARATOR}
   * <li> {@link #LINE_SEPARATOR}
   * <li> {@link #PARAGRAPH_SEPARATOR}
   * </ul>
   *
   * @param codePoint the character (Unicode code point) to be tested.
   * @return {@code true} if the character is a space character;
   *     {@code false} otherwise.
   * @see Character#isWhitespace(int)
   * @since 1.5
   */
  public static boolean isSpaceChar(int codePoint) {
    return ((((1 << SPACE_SEPARATOR) |
        (1 << LINE_SEPARATOR) |
        (1 << PARAGRAPH_SEPARATOR)) >> getType(codePoint)) & 1)
        != 0;
  }

  /**
   * Determines if the specified character (Unicode code point) is
   * white space according to Java.  A character is a Java
   * whitespace character if and only if it satisfies one of the
   * following criteria:
   * <ul>
   * <li> It is a Unicode space character ({@link #SPACE_SEPARATOR},
   *      {@link #LINE_SEPARATOR}, or {@link #PARAGRAPH_SEPARATOR})
   *      but is not also a non-breaking space ({@code '\u005Cu00A0'},
   *      {@code '\u005Cu2007'}, {@code '\u005Cu202F'}).
   * <li> It is {@code '\u005Ct'}, U+0009 HORIZONTAL TABULATION.
   * <li> It is {@code '\u005Cn'}, U+000A LINE FEED.
   * <li> It is {@code '\u005Cu000B'}, U+000B VERTICAL TABULATION.
   * <li> It is {@code '\u005Cf'}, U+000C FORM FEED.
   * <li> It is {@code '\u005Cr'}, U+000D CARRIAGE RETURN.
   * <li> It is {@code '\u005Cu001C'}, U+001C FILE SEPARATOR.
   * <li> It is {@code '\u005Cu001D'}, U+001D GROUP SEPARATOR.
   * <li> It is {@code '\u005Cu001E'}, U+001E RECORD SEPARATOR.
   * <li> It is {@code '\u005Cu001F'}, U+001F UNIT SEPARATOR.
   * </ul>
   * <p>
   *
   * @param codePoint the character (Unicode code point) to be tested.
   * @return {@code true} if the character is a Java whitespace
   *     character; {@code false} otherwise.
   * @see Character#isSpaceChar(int)
   * @since 1.5
   */
  public static boolean isWhitespace(int codePoint) {  // TODO(9/17/2025): available in GWT
    return CharacterData.of(codePoint).isWhitespace(codePoint);
  }

  /**
   * Determines if the referenced character (Unicode code point) is an ISO control
   * character.  A character is considered to be an ISO control
   * character if its code is in the range {@code '\u005Cu0000'}
   * through {@code '\u005Cu001F'} or in the range
   * {@code '\u005Cu007F'} through {@code '\u005Cu009F'}.
   *
   * @param codePoint the character (Unicode code point) to be tested.
   * @return {@code true} if the character is an ISO control character;
   *     {@code false} otherwise.
   * @see Character#isSpaceChar(int)
   * @see Character#isWhitespace(int)
   * @since 1.5
   */
  public static boolean isISOControl(int codePoint) {
    // Optimized form of:
    //     (codePoint >= 0x00 && codePoint <= 0x1F) ||
    //     (codePoint >= 0x7F && codePoint <= 0x9F);
    return codePoint <= 0x9F &&
        (codePoint >= 0x7F || (codePoint >>> 5 == 0));
  }

  /**
   * Returns the Unicode directionality property for the given
   * character (Unicode code point).  Character directionality is
   * used to calculate the visual ordering of text. The
   * directionality value of undefined character is {@link
   * #DIRECTIONALITY_UNDEFINED}.
   *
   * @param codePoint the character (Unicode code point) for which
   *     the directionality property is requested.
   * @return the directionality property of the character.
   * @see Character#DIRECTIONALITY_UNDEFINED DIRECTIONALITY_UNDEFINED
   * @see Character#DIRECTIONALITY_LEFT_TO_RIGHT DIRECTIONALITY_LEFT_TO_RIGHT
   * @see Character#DIRECTIONALITY_RIGHT_TO_LEFT DIRECTIONALITY_RIGHT_TO_LEFT
   * @see Character#DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC
   * @see Character#DIRECTIONALITY_EUROPEAN_NUMBER DIRECTIONALITY_EUROPEAN_NUMBER
   * @see Character#DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR
   * @see Character#DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR
   * @see Character#DIRECTIONALITY_ARABIC_NUMBER DIRECTIONALITY_ARABIC_NUMBER
   * @see Character#DIRECTIONALITY_COMMON_NUMBER_SEPARATOR DIRECTIONALITY_COMMON_NUMBER_SEPARATOR
   * @see Character#DIRECTIONALITY_NONSPACING_MARK DIRECTIONALITY_NONSPACING_MARK
   * @see Character#DIRECTIONALITY_BOUNDARY_NEUTRAL DIRECTIONALITY_BOUNDARY_NEUTRAL
   * @see Character#DIRECTIONALITY_PARAGRAPH_SEPARATOR DIRECTIONALITY_PARAGRAPH_SEPARATOR
   * @see Character#DIRECTIONALITY_SEGMENT_SEPARATOR DIRECTIONALITY_SEGMENT_SEPARATOR
   * @see Character#DIRECTIONALITY_WHITESPACE DIRECTIONALITY_WHITESPACE
   * @see Character#DIRECTIONALITY_OTHER_NEUTRALS DIRECTIONALITY_OTHER_NEUTRALS
   * @see Character#DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING
   * @see Character#DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE
   * @see Character#DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING
   * @see Character#DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE
   * @see Character#DIRECTIONALITY_POP_DIRECTIONAL_FORMAT DIRECTIONALITY_POP_DIRECTIONAL_FORMAT
   * @since 1.5
   */
  public static byte getDirectionality(int codePoint) {
    return CharacterData.of(codePoint).getDirectionality(codePoint);
  }

  /**
   * Determines whether the specified character (Unicode code point)
   * is mirrored according to the Unicode specification.  Mirrored
   * characters should have their glyphs horizontally mirrored when
   * displayed in text that is right-to-left.  For example,
   * {@code '\u005Cu0028'} LEFT PARENTHESIS is semantically
   * defined to be an <i>opening parenthesis</i>.  This will appear
   * as a "(" in text that is left-to-right but as a ")" in text
   * that is right-to-left.
   *
   * @param codePoint the character (Unicode code point) to be tested.
   * @return {@code true} if the character is mirrored, {@code false}
   *     if the character is not mirrored or is not defined.
   * @since 1.5
   */
  public static boolean isMirrored(int codePoint) {
    return CharacterData.of(codePoint).isMirrored(codePoint);
  }

  /**
   * Converts the character (Unicode code point) argument to uppercase using case
   * mapping information from the SpecialCasing file in the Unicode
   * specification. If a character has no explicit uppercase
   * mapping, then the {@code char} itself is returned in the
   * {@code char[]}.
   *
   * @param codePoint the character (Unicode code point) to be converted.
   * @return a {@code char[]} with the uppercased character.
   * @since 1.4
   */
  static char[] toUpperCaseCharArray(int codePoint) {
    // As of Unicode 6.0, 1:M uppercasings only happen in the BMP.
    assert Character.isBmpCodePoint(codePoint);
    return CharacterData.of(codePoint).toUpperCaseCharArray(codePoint);
  }

  /**
   * Determines whether the specified code point is a valid
   * <a href="http://www.unicode.org/glossary/#code_point">
   * Unicode code point value</a>.
   *
   * @param codePoint the Unicode code point to be tested
   * @return {@code true} if the specified code point value is between
   *     {@link #MIN_CODE_POINT} and
   *     {@link #MAX_CODE_POINT} inclusive;
   *     {@code false} otherwise.
   * @since 1.5
   */
  public static boolean isValidCodePoint(int codePoint) {
    // Optimized form of:
    //     codePoint >= MIN_CODE_POINT && codePoint <= MAX_CODE_POINT
    int plane = codePoint >>> 16;
    return plane < ((MAX_CODE_POINT + 1) >>> 16);
  }

  /**
   * Returns the leading surrogate (a
   * <a href="http://www.unicode.org/glossary/#high_surrogate_code_unit">
   * high surrogate code unit</a>) of the
   * <a href="http://www.unicode.org/glossary/#surrogate_pair">
   * surrogate pair</a>
   * representing the specified supplementary character (Unicode
   * code point) in the UTF-16 encoding.  If the specified character
   * is not a
   * <a href="Character.html#supplementary">supplementary character</a>,
   * an unspecified {@code char} is returned.
   *
   * <p>If
   * {@link #isSupplementaryCodePoint isSupplementaryCodePoint(x)}
   * is {@code true}, then
   * {@link #isHighSurrogate isHighSurrogate}{@code (highSurrogate(x))} and
   * {@link #toCodePoint toCodePoint}{@code (highSurrogate(x), }{@link #lowSurrogate lowSurrogate}{@code (x)) == x}
   * are also always {@code true}.
   *
   * @param codePoint a supplementary character (Unicode code point)
   * @return the leading surrogate code unit used to represent the
   *     character in the UTF-16 encoding
   * @since 1.7
   */
  public static char highSurrogate(int codePoint) {
    return (char)((codePoint >>> 10)
        + (MIN_HIGH_SURROGATE - (MIN_SUPPLEMENTARY_CODE_POINT >>> 10)));
  }

  /**
   * Returns the trailing surrogate (a
   * <a href="http://www.unicode.org/glossary/#low_surrogate_code_unit">
   * low surrogate code unit</a>) of the
   * <a href="http://www.unicode.org/glossary/#surrogate_pair">
   * surrogate pair</a>
   * representing the specified supplementary character (Unicode
   * code point) in the UTF-16 encoding.  If the specified character
   * is not a
   * <a href="Character.html#supplementary">supplementary character</a>,
   * an unspecified {@code char} is returned.
   *
   * <p>If
   * {@link #isSupplementaryCodePoint isSupplementaryCodePoint(x)}
   * is {@code true}, then
   * {@link #isLowSurrogate isLowSurrogate}{@code (lowSurrogate(x))} and
   * {@link #toCodePoint toCodePoint}{@code (}{@link #highSurrogate highSurrogate}{@code (x), lowSurrogate(x)) == x}
   * are also always {@code true}.
   *
   * @param codePoint a supplementary character (Unicode code point)
   * @return the trailing surrogate code unit used to represent the
   *     character in the UTF-16 encoding
   * @since 1.7
   */
  public static char lowSurrogate(int codePoint) {
    return (char)((codePoint & 0x3ff) + MIN_LOW_SURROGATE);
  }

}
