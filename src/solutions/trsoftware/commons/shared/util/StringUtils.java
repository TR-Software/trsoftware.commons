/*
 * Copyright 2022 TR Software Inc.
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

package solutions.trsoftware.commons.shared.util;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import solutions.trsoftware.commons.shared.io.TablePrinter;
import solutions.trsoftware.commons.shared.util.function.FunctionalUtils;
import solutions.trsoftware.commons.shared.util.iterators.CharSequenceIterator;
import solutions.trsoftware.commons.shared.util.iterators.CodePointIterator;
import solutions.trsoftware.commons.shared.util.template.SimpleTemplateParser;
import solutions.trsoftware.commons.shared.util.template.Template;
import solutions.trsoftware.commons.shared.util.text.CharRange;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Static utility methods pertaining to {@link String} or {@link CharSequence} instances.
 *
 * @author Alex
 * @see com.google.common.base.Strings
 */
public class StringUtils {

  /** The lowest printable ASCII char (code 32) */
  public static final char MIN_PRINTABLE_ASCII_CHAR = ' ';
  /** The highest printable ASCII char (code 126) */
  public static final char MAX_PRINTABLE_ASCII_CHAR = '~';
  /** The alphabet {@code [A-Za-z]} */
  public static final String ASCII_LETTERS = new CharRange('A', 'Z').toString() + new CharRange('a', 'z').toString();
  /** The alphabet {@code [0-9]} */
  public static final String ASCII_DIGITS = new CharRange('0', '9').toString();
  /** The alphabet {@code [A-Za-z0-9]} */
  public static final String ASCII_LETTERS_AND_NUMBERS = ASCII_LETTERS + ASCII_DIGITS;
  /** The alphabet of all printable {@code ASCII} chars */
  public static final String ASCII_PRINTABLE_CHARS = new CharRange(MIN_PRINTABLE_ASCII_CHAR, MAX_PRINTABLE_ASCII_CHAR).toString();

  /**
   * Canonical name for the UTF-8 encoding.
   * @see java.nio.charset.StandardCharsets#UTF_8
   * @see <a href="https://en.wikipedia.org/wiki/UTF-8">UTF-8 (Wikipedia)</a>
   * @see <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/intl/encoding.doc.html">Supported Encodings</a>
   */
  public static final String UTF8_CHARSET_NAME = "UTF-8";

  // box drawing characters
  /**
   * <ul>
   * <li><b>row indices</b>: 0 for top row, 1 for middle, and 2 for last</li>
   * <li><b>col indices</b>: 0 for first col, 1 for middle, and 2 for last</li>
   * </ul>
   * @see <a href="https://en.wikipedia.org/wiki/Box-drawing_character#Block_Elements">Box-drawing characters</a>
   */
  public static final char[][] CORNER_CHARS = {
      new char[]{'\u2554', '\u2566', '\u2557'},
      new char[]{'\u2560', '\u256C', '\u2563'},
      new char[]{'\u255A', '\u2569', '\u255D'}
  };
  public static final char H_BORDER_CHAR = '\u2550';
  public static final char V_BORDER_CHAR = '\u2551';

  public static String capitalize(String str) {
    if (isBlank(str) || isCapitalized(str) || !Character.isLetter(str.charAt(0)))
      return str;
    return str.substring(0, 1).toUpperCase() + str.substring(1);
  }

  /** @return true iff {@code str} starts with an uppercase letter */
  public static boolean isCapitalized(String str) {
    return str != null && !str.isEmpty() && Character.isUpperCase(str.charAt(0));
  }

  /** Capitalizes {@code str} iff {@code reference} starts with an uppercase letter */
  public static String maybeCapitalize(String str, String reference) {
    if (isCapitalized(reference))
      return capitalize(str);
    return str;
  }

  /**
   * @return A pretty string matching a name of a (Java code style) constant.  Example: {@code "FOO_BAR" >>> "Foo bar"}
   */
  public static String constantNameToTitleCase(String name) {
    return capitalize(name.replaceAll("_", " ").trim().toLowerCase());
  }

  /**
   * @return {@code true} iff the given string is {@code null}, empty, or consists entirely of whitespace
   * (as determined by {@link String#trim()})
   * @see #notBlank(String)
   * @see #isEmpty(String)
   */
  public static boolean isBlank(String str) {
    return str == null || str.trim().isEmpty();
  }

  /**
   * @return {@code true} iff the given string is neither {@code null}, empty, nor consists entirely of
   * whitespace.
   * @see #isBlank(String)
   * @see #notEmpty(String)
   */
  public static boolean notBlank(String str) {
    return !isBlank(str);
  }
  
  /**
   * Returns the given string if it's not {@linkplain #notBlank(String) blank},
   * otherwise returns the string produced by the given supplier.
   *
   * @return the given string if it's not {@linkplain #notBlank(String) blank},
   * otherwise {@code supplier.get()}
   * @see LogicUtils#nonNullOrElse(Object, Supplier)
   * @see #applyIfNotBlank(String, Consumer)
   */
  @Nullable
  public static String notBlankOrElse(String s, Supplier<String> other) {
    if (notBlank(s))
      return s;
    return other.get();
  }

  /**
   * Applies the given consumer to the given string if it's not {@linkplain #notBlank(String) blank}.
   *
   * @return {@code true} if the consumer was invoked (i.e. the value wasn't {@linkplain #notBlank(String) blank})
   * @see #notBlankOrElse(String, Supplier)
   * @see FunctionalUtils#applyIfNotNull(Object, Consumer)
   */
  public static boolean applyIfNotBlank(String value, Consumer<String> consumer) {
    if (notBlank(value)) {
      consumer.accept(value);
      return true;
    }
    return false;
  }

  /**
   * Similar to {@link #isBlank(String)}, but doesn't use {@link String#trim()} to check whether the string contains
   * only whitespace.
   *
   * @return {@code true} iff the given string is either {@code null} or {@code ""}
   * @see #isBlank(String)
   * @see #notEmpty(String)
   */
  public static boolean isEmpty(String str) {
    return str == null || str.isEmpty();
  }

  /**
   * @return {@code true} iff the given string is neither {@code null} nor {@code ""}
   * @see #notBlank(String)
   * @see #isEmpty(String)
   */
  public static boolean notEmpty(String str) {
    return !isEmpty(str);
  }

  /**
   * @return {@code true} iff the string doesn't contain any uppercase letters (as determined by {@link Character#isUpperCase(char)});
   * if the argument is {@code null} or an empty string, returns {@code false}
   */
  public static boolean isLowercase(String s) {
    if (isEmpty(s)) {
      return false;
    }
    for (int i = 0; i < s.length(); i++) {
      if (Character.isUpperCase(s.charAt(i)))
        return false;
    }
    return true;
    /*
      NOTE: can generalize this method to test for uppercase, camelCase, etc.
    */
  }

  /**
   * @return the given string if it's not {@code null}, otherwise an empty string
   * @see #trim(String)
   * @see Strings#nullToEmpty(String)
   */
  public static String nonNull(String str) {
    return str != null ? str : "";
  }

  /**
   * Returns the first of the two given strings for which {@link #notBlank(String)} is {@code true}.
   * If neither arg satisfies {@link #notBlank(String)}, will return the second arg (which could be blank).
   *
   * @see LogicUtils#firstNonNull(Object, Object)
   */
  @Nullable
  public static String firstNotBlank(String a, String b) {
    return notBlank(a) ? a : b;
  }

  /**
   * Returns the first of the given strings for which {@link #notBlank(String)} is {@code true}.
   * If none of the strings satisfy {@link #notBlank(String)}, will return the last element (which could be blank),
   * or {@code null} if the array is empty.
   *
   * @see LogicUtils#firstNonNull(Object[])
   */
  @Nullable
  public static String firstNotBlank(String... strings) {
    String ret = null;
    for (String str : strings) {
      ret = str;
      if (notBlank(str))
        break;
    }
    return ret;
  }
  
  /**
   * @return the given string, trimmed by removing surrounding whitespace, if it's not null, otherwise an empty string.
   */
  public static String trim(String str) {
    if (notEmpty(str))
      return str.trim();
    else
      return "";
  }

//  /**
//   * Converts any special xml char escape sequences in the given string to the
//   * original characters:
//   * & - &amp;
//   * < - &lt;
//   * > - &gt;
//   * " - &quot;
//   * ' - &apos;
//   */
//  public static String unescapeXML() {
//  }

  /**
   * Abbreviates the input string if it's longer than {@code maxLen} by replacing the last
   * {@code maxLen-suffix.length()} characters with {@code suffix}.
   *
   * @param str the string to abbreviate, may be null
   * @param maxLen maximum length of result, should be a positive integer in order for this method to makes sense.
   * However, if it's not positive, will return an empty string rather than throwing an exception.
   * @param suffix overflow characters will be replaced with this, with a {@code null} value for this arg being equivalent
   * to passing an empty string.
   * @return the abbreviated string if it needs to be abbreviated (in which case the result will contain one or more
   * characters from the original plus as many chars from the suffix as we can fit in while satisfying the
   * {@code maxLen} constraint), the original string if it doesn't need to be abbreviated, {@code null} if the original
   * was {@code null}, or an empty string if {@code maxLen} is not a positive integer.
   */
  public static String abbreviate(String str, int maxLen, String suffix) {
    // TODO: rethink the str.trim() performed by isBlank: is that we we really want? And if so, should we trim the substring as well before adding the suffix?
    // 1) validate the args
    if (isBlank(str) || str.length() <= maxLen)
      return str;
    if (maxLen <= 0)
      return "";
    if (suffix == null)
      suffix = "";
    // at this point we have to abbreviate; to figure out where to cut the string we can solve the equation: newStrLen + suffixLen = maxLen => newStrLen = maxLen - suffixLen
    int newStrLen = Math.max(1, maxLen - suffix.length());  // we want to leave at least 1 char from the original str
    // however, since we're forcing newStrLen to be at least 1, we might have to trim the suffix according to
    // this equation: newStrLen + newSuffixLen = maxLen => newSuffixLen = maxLen - newStrLen
    return str.substring(0, newStrLen) + suffix.substring(0, maxLen - newStrLen);
  }

  /**
   * Same as {@link #abbreviate(String, int, String) abbreviate(str, maxLen, "...")}.
   */
  public static String abbreviate(String str, int maxLen) {
    return abbreviate(str, maxLen, "...");
  }

  /**
   * Truncates the given string to the desired maximum length.
   * @param str The input string, can be null
   * @param length the maximum length of the string to be returned
   * @return a string containing up to length chars from the input string
   */
  public static String truncate(String str, int length) {
    if (str == null)
      return null;
    if (str.length() <= length)
      return str;
    else
      return str.substring(0, length);
  }

  /**
   * @return The substring of s ending before the first occurrence of q; if
   * s doesn't contain q, returns s.
   */
  public static String substringBefore(String s, String q) {
    int i = s.indexOf(q);
    if (i > -1)
      return s.substring(0, i);
    else
      return s;
  }

  /**
   * @return The substring of s starting after the first occurrence of q; if
   * s doesn't contain q, returns s.
   */
  public static String substringAfter(String s, String q) {
    int i = s.indexOf(q);
    if (i > -1)
      return s.substring(i + q.length());
    else
      return s;
  }

  /**
   * @return The substring of s in-between the first occurrences of a and b; if
   * s doesn't contain a, returns substringBefore(s, b); if s doesn't contain b
   * after a, returns substringAfter(s,a);
   */
  public static String substringBetween(String s, String a, String b) {
    int aStart = s.indexOf(a);
    if (aStart < 0)
      return substringBefore(s, b);
    int aEnd = aStart + a.length();
    int bStart = s.indexOf(b, aEnd);
    if (bStart < 0)
      return substringAfter(s, a);
    return s.substring(aEnd, bStart);
  }

  /**
   * Provides a very limited form of string templating. The symbols {@code $1}...{@code $9}
   * are replaced with the given args.  Supports at most 9 arguments (to make parsing simpler).
   * For a more powerful implementation can use {@link SimpleTemplateParser} instead.
   * <p>
   * This method is intended as a substitute for {@link String#format(String, Object...)}, which is not available in GWT.
   *
   * @param format string which may contain symbols {@code $1}...{@code $9} to be substituted with the positional
   *     {@code args}
   * @param args the replacements for the symbols {@code $1}...{@code $9} in the {@code format} string.
   * @return the {@code format} string with symbols {@code $1}...{@code $9} replaced by the positional {@code args}
   * @see Template
   * @see SimpleTemplateParser
   * @see com.google.common.base.Strings#lenientFormat(String, Object...)
   */
  public static String template(String format, Object... args) {
    if (args.length > 9)
      throw new IllegalArgumentException("template called with more than 9 args");
    // Not using regex here for Java/Javascript compatibility (and also speed)
    StringBuilder result = new StringBuilder(512);
    int nextCopyRegionStart = 0;
    int lastMatch = format.indexOf("$", 0);
    while (lastMatch >= 0 && lastMatch < format.length()-1) {
      char indexChar = format.charAt(lastMatch + 1);
      if (indexChar >= '1' && indexChar <= '9') {
        int index = indexChar - '1';
        result.append(format, nextCopyRegionStart, lastMatch);
        result.append(args[index]);
        nextCopyRegionStart = lastMatch+2;
        lastMatch = format.indexOf("$", lastMatch+2);
      }
      else
        lastMatch = format.indexOf("$", lastMatch+1); // handle strings like "$$1"
    }
    result.append(format.substring(nextCopyRegionStart));  // copy the end of the format string
    return result.toString();
  }

  /** Returns strings like 1st, 2nd, 3rd, 4th, etc. for the English language */
  public static String ordinal(int i) {
    String str = String.valueOf(i);
    String suffix;
    if (str.endsWith("1") && !str.endsWith("11"))
      suffix = "st";
    else if (str.endsWith("2") && !str.endsWith("12"))
      suffix = "nd";
    else if (str.endsWith("3") && !str.endsWith("13"))
      suffix = "rd";
    else
      suffix = "th";
    return str + suffix;
  }

  /**
   * @return A string displaying the given quantity, with the unit name
   * pluralized if needed.  Example: {@code pluralize(2, "second")} returns {@code "2 seconds"}.
   */
  public static String quantity(int value, String unit) {
    return String.valueOf(value) + ' ' + pluralize(unit, value);
  }

  /** Removes the given suffix from the string if it's present */
  public static String stripSuffix(String str, String suffix) {
    if (str.endsWith(suffix))
      return str.substring(0, str.length() - suffix.length());
    return str;
  }

  /** Removes the given prefix from the string if it's present */
  public static String stripPrefix(String str, String prefix) {
    if (str.startsWith(prefix))
      return str.substring(prefix.length());
    return str;
  }

  /**
   * Creates a string that contains some number of repetitions of a single character
   * @param fillChar the {@code char} to be repeated
   * @param repetitions the number of times to repeat the given {@code char}
   * @return a string that contains the requested number of repetitions of {@code fillChar}
   * @see Arrays#fill(char[], char)
   */
  public static String repeat(char fillChar, int repetitions) {
    if (repetitions < 0)
      throw new IllegalArgumentException("negative repetitions");
    char[] chars = new char[repetitions];
    Arrays.fill(chars, fillChar);
    return new String(chars);
  }

  /**
   * Creates a string that contains some number of repetitions of a single Unicode
   * {@link Character#isValidCodePoint(int) code point}
   *
   * @param codePoint the Unicode code point to be repeated
   * @param repetitions the number of times to repeat the given code point
   * @return a string that contains the requested number of repetitions of {@code fillChar}
   * @see Arrays#fill(char[], char)
   */
  public static String repeat(int codePoint, int repetitions) {
    if (repetitions < 0)
      throw new IllegalArgumentException("negative repetitions");
    int[] codePoints = new int[repetitions];
    Arrays.fill(codePoints, codePoint);
    return new String(codePoints, 0, codePoints.length);
  }

  /**
   * Creates a string that contains some number of repetitions of a substring
   * @param fillStr the substring to be repeated
   * @param repetitions the number of times to repeat the given substring
   * @return a string that contains the requested number of repetitions of {@code fillStr}
   */
  public static String repeat(String fillStr, int repetitions) {
    StringBuilder buf = new StringBuilder(fillStr.length()*repetitions);
    for (int i = 0; i < repetitions; i++) {
      buf.append(fillStr);
    }
    return buf.toString();
  }

  /**
   * @return a string that contains the given number of spaces ({@code ' '} chars).
   */
  public static String indent(int nSpaces) {
    return repeat(' ', nSpaces);
  }

  /**
   * Prepends the given number of space chars to the given string.
   * @return the given string prefixed with the given number of spaces ({@code ' '} chars).
   */
  public static String indent(int nSpaces, String toIndent) {
    return nSpaces > 0 ? indent(nSpaces) + toIndent : toIndent;
  }

  /**
   * Returns a function that prepends the given number of space chars to a string:
   * <pre>
   *   s -> {@link #indent(int, String) indent(nSpaces, s)}
   * </pre>
   * Code example that prints a string array with each line indented by 2 spaces:
   * <pre>
   *   Arrays.stream(stringArray).map({@link #indenting}(2)).forEach(System.out::println);
   * </pre>
   *
   * @param width the desired length of the result string
   * @return a function that applies {@link #justifyRight(String, int) justifyRight(s, width)} to a string {@code s}.
   * @see #justifyRight(String, int)
   */
  // TODO(1/23/2025): rename this method to just "indent" (avoid naming conflict by renaming the original indent(int) to spaces(int))
  public static UnaryOperator<String> indenting(int nSpaces) {
    return s -> indent(nSpaces, s);
  }

  /**
   * Returns a function that prepends the given prefix to a string:
   * <pre>
   *   s -> prefix + s
   * </pre>
   * This can be used, for example, to print a string array as a bulleted list:
   * <pre>
   *   Arrays.stream(stringArray).map({@link #prefixing}("  - ")).forEach(System.out::println);
   * </pre>
   *
   * @param width the desired length of the result string
   * @return a function that applies {@link #justifyRight(String, int) justifyRight(s, width)} to a string {@code s}.
   * @see #justifyRight(String, int)
   */
  public static UnaryOperator<String> prefixing(String prefix) {
    return s -> prefix + s;
  }

  /** @return a string of the given length using randomly-selected chars from the alphabet {@code [A-Za-z]} */
  public static String randString(int length) {
    return RandomUtils.randString(length, ASCII_LETTERS);
  }

  /**
   * Converts a string to camel-case, using the given word separator.
   * <h3>Example:</h3>
   * <pre>
   *   toCamelCase("MY_STRING", "_") &rarr; "myString"
   * </pre>
   * @param str the string to convert
   * @param wordSeparatorRegex the string will be split into words using this regex; <b>WARNING</b>: do not pass values
   * that can't be used as a regex (e.g. passing {@code "*"} would trigger a "Dangling meta character" exception)
   * @return a camel-case version of the given string
   * @see com.google.common.base.CaseFormat
   */
  public static String toCamelCase(String str, String wordSeparatorRegex) {
    if (isBlank(str))
      return str;
    String[] parts = str.split(wordSeparatorRegex);
    StringBuilder out = new StringBuilder(str.length());
    for (int i = 0; i < parts.length; i++) {
      String word = parts[i].toLowerCase();
      if (i > 0)
        word = capitalize(word);
      out.append(word);
    }
    return out.toString();
  }

  /** @return the number of the times the given char appears in the given string */
  public static int count(String s, char c) {
    if (s == null)
      return 0;
    int n = 0;
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) == c)
        n++;
    }
    return n;
  }

  @SafeVarargs
  public static <T> String join(String delimiter, T... array) {
    return join(delimiter, Arrays.asList(array));
  }

  public static String join(String delimiter, int... array) {
    StringBuilder str = new StringBuilder();
    for (int i = 0; i < array.length; i++) {
      str.append(array[i]);
      if (i < array.length - 1)
        str.append(delimiter);
    }
    return str.toString();
  }

  public static <T> String join(String delimiter, Iterator<T> iter) {
    return join(delimiter, null, iter);
  }

  public static <T> String join(String delimiter, Iterable<T> iterable) {
    return join(delimiter, iterable.iterator());
  }

  /**
   * Similar to {@link #join(String, Iterator)}, but can specify a different delimiter for the last element.
   * This is useful for printing values such as {@code "a, b, and c"} or {@code "a, b, or c"}.
   *
   * @param delimiter the delimiter to use for concatenating the elements
   * @param lastDelimiter the delimiter to use for concatenating the last element (pass {@code null} if the last delimiter
   * should be the same as {@code delimiter}
   * @param iter the elements to join
   */
  public static <T> String join(String delimiter, String lastDelimiter, Iterator<T> iter) {
    return appendJoined(new StringBuilder(128), delimiter, lastDelimiter, iter).toString();
  }

  /**
   * Returns the given strings ({@code s1} and {@code s2}) concatenated using the given delimiter if both inputs non-null.
   * If one of the inputs
   * is {@code null}, returns the other input, and if both {@code null}, returns {@code null}.
   */
  @Nullable
  public static String joinNullable(@Nonnull String delimiter, @Nullable String s1, @Nullable String s2) {
    if (s1 == null)
      return s2;
    if (s2 == null)
      return s1;
    return s1 + delimiter + s2;
  }

  /**
   * Generates a string enumerating the given items using proper English punctuation, such that the delimiter is
   * placed between each item and the conjunction is inserted before the last item (e.g. "a, b, and c").
   * If there are only 2 items, the delimiter is omitted (e.g. "a or b").  Whitespace will be added after each delimiter
   * and around the conjunction as needed.
   * <p>
   * Examples:
   * <pre>{@code
   *   joinEnumerated(",", "and", Arrays.asList("a", "b", "c", "d"));  // returns "a, b, c, and d"
   *   joinEnumerated(",", "or",  Arrays.asList("a", "b", "c"));       // returns "a, b, or c"
   *   joinEnumerated(",", "and", Arrays.asList("a", "b"));            // returns "a and b"
   *   joinEnumerated(",", "and", Arrays.asList("a"));                 // returns "a"
   * }</pre>
   *
   * NOTE: this method differs from {@link #join(String, String, Iterator)} in the special case of the delimiter being
   * omitted when there are only 2 elements.
   *
   * @param delimiter the delimiter to use for concatenating the elements (shouldn't contain any trailing whitespace)
   * @param conjunction the word to be inserted before the last element (shouldn't be surrounded by any whitespace)
   * @param items the elements to join
   */
  public static <T> String joinEnumerated(String delimiter, String conjunction, List<T> items) {
    return appendEnumerated(new StringBuilder(128), delimiter, conjunction, items).toString();
  }

  /**
   * Same as {@link #join(String, String, Iterator)}, but appends the joined result to the given builder
   * instead of creating a new string.
   *
   * @param builder the output will be appended to this builder
   * @param delimiter the delimiter to use for concatenating the elements
   * @param lastDelimiter the delimiter to use for concatenating the last element (pass {@code null} if the last delimiter
   * should be the same as {@code delimiter}
   * @param iter the elements to join
   * @return the given {@code builder} (after the joined result has been appended to it)
   */
  public static <T> StringBuilder appendJoined(StringBuilder builder, String delimiter, String lastDelimiter, Iterator<T> iter) {
    int iLastDelim = -1;
    while (iter.hasNext()) {
      T item = iter.next();
      builder.append(item);
      if (iter.hasNext()) {
        iLastDelim = builder.length();
        builder.append(delimiter);
      }
    }
    if (iLastDelim >= 0 && lastDelimiter != null)
      builder.replace(iLastDelim, iLastDelim + delimiter.length(), lastDelimiter);
    return builder;
  }

  /**
   * Same as {@link #joinEnumerated(String, String, List)}, but appends the joined result to the given builder
   * instead of creating a new string.
   *
   * @param builder the enumerated elements will be appended to this builder
   * @param delimiter the delimiter to use for concatenating the elements (shouldn't contain any trailing whitespace)
   * @param conjunction the word to be inserted before the last element (shouldn't be surrounded by any whitespace)
   * @param items the elements to join
   * @return the given {@code builder} (after the joined result has been appended to it)
   */
  public static <T> StringBuilder appendEnumerated(StringBuilder builder, String delimiter, String conjunction, List<T> items) {
    int n = items.size();
    if (n == 2)
      return builder.append(items.get(0)).append(' ').append(conjunction).append(' ').append(items.get(1));
    for (int i = 0; i < n; i++) {
      builder.append(items.get(i));
      if (i < n-1) {
        builder.append(delimiter).append(' ');
        if (i == n-2)
          builder.append(conjunction).append(' ');
      }
    }
    return builder;
  }


  /**
   * Same as {@link #join(String, Iterator)}, but appends the joined result to the given builder
   * instead of creating a new string.
   *
   * @param builder the output will be appended to this builder
   * @param delimiter the delimiter to use for concatenating the elements
   * @param iter the elements to join
   * @return the given {@code builder} (after the joined result has been appended to it)
   */
  public static <T> StringBuilder appendJoined(StringBuilder builder, String delimiter, Iterator<T> iter) {
    return appendJoined(builder, delimiter, null, iter);
  }

  /**
   * @return the integer represented by the last sequence of consecutive decimal digits
   * contained by the string, or {@code null} if the string does not contain any digits.
   */
  // TODO: this method might be too special-purpose for inclusion in this class (also easy-enough to implement with a regex)
  public static Integer lastIntegerInString(String str) {
    int startIndex = -1, endIndex = -1;

    for (int i = str.length()-1; i >= 0; i--) {
      char c = str.charAt(i);
      if (c >= '0' && c <= '9') {
        if (endIndex < 0)
          endIndex = i;
        startIndex = i;
      }
      else if (endIndex >= 0) {
        break;  // stop looking - we found a non-digit character while scanning a run of digits
      }
    }
    if (endIndex >= 0)
      return Integer.parseInt(str.substring(startIndex, endIndex+1)); // let the Integer class do the parsing (to properly handle signs, etc.)
    return null;
  }

  /** Maps some irregular words to their plural forms */
  private static final Map<String, String> pluralDict = ImmutableMap.of(
      "is", "are",
      "its", "their"
  );

  /**
   * @return the plural form of the given word if it needs to be pluralized (number > 1).  If the given word is in
   * {@link #pluralDict}, its mapped counterpart will be used for the plural form, otherwise will simply append
   * the letter "s" to make the plural form.
   */
  public static String pluralize(String singular, int number) {
    if (number == 1)
      return singular;
    String singularLowerCase = singular.toLowerCase();
    // TODO: for more flexibility (and to support non-English languages) allow passing a custom plural form for irregular words (maybe even a full dict of pluralization rules)
    if (pluralDict.containsKey(singularLowerCase)) {
      String plural = pluralDict.get(singularLowerCase);
      return maybeCapitalize(plural, singular);
    }
    return singular + "s";
  }

  /**
   * @return {@code number == 1 ? singular : plural}
   */
  public static String pluralize(int number, String singular, String plural) {
    return number == 1 ? singular : plural;
  }

  /**
   * @return the longest common prefix shared by the two given strings,
   * which could be the empty string.
   */
  public static String commonPrefix(String s, String t) {
    int prefixLast = -1;
    int limit = Math.min(s.length(), t.length());  // will throw NPE if either string is null (as expected)
    for (int i = 0; i < limit; i++) {
      if (s.charAt(i) == t.charAt(i))
        prefixLast = i;
      else
        break;
    }
    return s.substring(0, prefixLast+1);
  }

  /**
   * @return the longest common suffix (at the end) shared by the two given strings,
   * which could be the empty string.
   */
  public static String commonSuffix(String s, String t) {
    int sNext = s.length();
    int tNext = t.length();
    while (--sNext >= 0 && --tNext >= 0) {
      if (s.charAt(sNext) != t.charAt(tNext))
        break;
    }
    return s.substring(sNext+1, s.length());
  }

  /**
   * Useful for printing debug info.
   * @return a string that looks like "(arg1, arg2, arg3)"
   */
  public static String tupleToString(Object... args) {
    StringBuilder buf = new StringBuilder(64).append('(');
    appendArgs(buf, args);
    return buf.append(')').toString();
  }

  /**
   * @return the given string surrounded by parentheses
   * @see #bracket(String, char)
   * @see #bracket(String, String)
   */
  public static String parenthesize(String str) {
    return "(" + str + ')';
  }

  /**
   * Useful for printing debug info.
   * @return a string that looks like {@code "methodName(arg1, arg2, ...)"}
   */
  public static String methodCallToString(String methodName, Object... args) {
    return methodName + tupleToString(args);
  }

  /**
   * Useful for printing debug info.
   * @return a string that looks like {@code "ClassName.methodName(arg1, arg2, ...)"}
   */
  public static String methodCallToString(Class<?> cls, String methodName, Object... args) {
    return methodCallToString(cls.getSimpleName() + "." + methodName, args);
  }

  /**
   * Useful for printing debug info.
   * @return a string that looks like {@code "methodName(arg1, arg2, ...) = result"}
   */
  public static String methodCallToStringWithResult(String methodName, Object result, Object... args) {
    StringBuilder buf = new StringBuilder(64).append(methodCallToString(methodName, args)).append(" = ");
    appendValue(buf, result);
    return buf.toString();
  }

  /**
   * Writes the {@linkplain #valueToString(Object) string values} of the given args joined by to the given buffer,
   * joined by ", ".
   * <p>
   * Each value will be converted to a string in the most useful form for printing debug info or implementing {@link Object#toString()}:
   * (strings/chars quoted, arrays printed using Arrays.toString/deepToString).
   * 
   * @return the same buffer after appending the generated string
   */
  public static StringBuilder appendArgs(StringBuilder buf, Object... args) {
    for (int i = 0; i < args.length; i++) {
      appendValue(buf, args[i]);
      if (i < args.length - 1)
        buf.append(", ");
    }
    return buf;
  }

  /**
   * Appends a {@linkplain #valueToString(Object) verbose string value} of the given arg to the given buffer.
   * 
   * @see #valueToString(Object)
   * @return the same buffer after appending the generated string
   */
  public static StringBuilder appendValue(StringBuilder buf, Object value) {
    if (value instanceof CharSequence)
      appendSurrounded(buf, value, "\"");
    else if (value instanceof Character)
      appendSurrounded(buf, value, "'");
    else if (value instanceof Object[])
      /* TODO: Arrays.deepToString doesn't quote strings and chars the way we want;
       *   might want to implement our own version (similar to the private method Arrays.deepToString(Object[], StringBuilder, Set<Object[]>))
       */
      buf.append(Arrays.deepToString((Object[])value));
    else if (value instanceof byte[])
      buf.append(Arrays.toString((byte[])value));
    else if (value instanceof short[])
      buf.append(Arrays.toString((short[])value));
    else if (value instanceof int[])
      buf.append(Arrays.toString((int[])value));
    else if (value instanceof long[])
      buf.append(Arrays.toString((long[])value));
    else if (value instanceof float[])
      buf.append(Arrays.toString((float[])value));
    else if (value instanceof double[])
      buf.append(Arrays.toString((double[])value));
    else if (value instanceof boolean[])
      buf.append(Arrays.toString((boolean[])value));
    else if (value instanceof char[])
      buf.append(Arrays.toString((char[])value));
    else
      buf.append(value);
    return buf;
  }

  /**
   * Appends s to the buffer surrounded by prefixAndSuffix.
   * @return the given buffer, for method chaining.
   */
  public static StringBuilder appendSurrounded(StringBuilder buf, Object s, String prefixAndSuffix) {
    return buf.append(prefixAndSuffix).append(s).append(prefixAndSuffix);
  }

  /**
   * @return {@code str} surrounded by {@code wrapper} on both sides.
   * @see #bracket(String, String)
   */
  public static String surround(String str, String wrapper) {
    return wrapper + str + wrapper;
  }

  /**
   * @return {@code str} surrounded by {@code "}.
   * @see #surround(String, String)
   */
  public static String quote(String str) {
    return surround(str, "\"");
  }

  /**
   * A bidirectional mapping of the various ASCII bracketing symbol pairs such as
   * <code>( )</code>, <code>{ }</code>, <code>[ ]</code>, and {@code < >}
   */
  public static final ImmutableBiMap<Character, Character> BRACKET_SYMBOLS = ImmutableBiMap.<Character, Character>builder()
      .put('(', ')')
      .put('{', '}')
      .put('[', ']')
      .put('<', '>')
      .build();

  /**
   * A general-purpose method for bracketing a string of text.
   * <p>
   *   Specifically, if the given char parameter is in {@link #BRACKET_SYMBOLS}, will use the corresponding closing
   *   bracket symbol to terminate the result; otherwise will use the same char on both sides.
   * </p>
   * <p>
   *   <b>Examples</b>:
   *   <pre>
   *     bracket("foo", '(') &rarr; "(foo)"
   *     bracket("foo", '{') &rarr; "{foo}"
   *     bracket("foo", '[') &rarr; "[foo]"
   *     bracket("foo", '<') &rarr;{@code "<foo>"}
   *     bracket("foo", 'X') &rarr; "XfooX"
   *   </pre>
   * </p>
   * @param str the string to be wrapped with brackets
   * @param openingBracket the opening bracket symbol
   * @return the string surrounded with the appropriate brackets for the given arguments
   * @see #bracket(String, String)
   * @see #parenthesize(String)
   */
  public static String bracket(String str, char openingBracket) {
    char closingBracket = BRACKET_SYMBOLS.getOrDefault(openingBracket, openingBracket);
    return openingBracket + str + closingBracket;
  }

  /**
   * Recursively applies {@link #bracket(String, char)} for each char in the {@code openingBrackets} argument.
   * <p>
   *   <b>Examples</b>:
   *   <pre>
   *     bracket("foo", "([") &rarr; "([foo])"
   *     bracket("foo", "<{{") &rarr; "<{{foo}}>"
   *     bracket("foo", "/* ") &rarr; "/* foo &#42;/"
   *     bracket("foo", "XyZ") &rarr; "XyZfooZyX"
   *   </pre>
   * </p>
   * </p>
   * @param str the string to be wrapped with brackets
   * @param openingBrackets the opening bracket symbols
   * @return the string surrounded with the appropriate brackets for the given arguments
   * @see #bracket(String, char)
   */
  public static String bracket(String str, String openingBrackets) {
    // apply the brackets from right to left
    for (int i = openingBrackets.length() - 1; i >= 0; i--) {
      char bracket = openingBrackets.charAt(i);
      str = bracket(str, bracket);
    }
    return str;
  }

  /**
   * @return {@code str} surrounded by the desired number of space chars.
   * @see #surround(String, String)
   * @see #padLeft(String, int)
   * @see #padRight(String, int)
   */
  public static String pad(String str, int nSpaces) {
    return pad(str, nSpaces, ' ');
  }

  /**
   * @return {@code str} surrounded by {@code n} {@code pad} chars.
   * @see #surround(String, String)
   * @see #pad(String, int)
   * @see #padLeft(String, int, char)
   * @see #padRight(String, int)
   */
  public static String pad(String str, int n, char pad) {
    return surround(str, repeat(pad, n));
  }

  /**
   * @return {@code str} with the desired number of space chars prepended on the left.
   * @see #pad(String, int)
   * @see #padRight(String, int)
   * @see Strings#padStart(String, int, char)
   */
  public static String padLeft(String str, int nSpaces) {
    return padLeft(str, nSpaces, ' ');
  }

  /**
   * @return {@code str} with {@code n} {@code pad} chars prepended on the left.
   * @see #padLeft(String, int)
   * @see Strings#padStart(String, int, char)
   */
  public static String padLeft(String str, int n, char pad) {
    return repeat(pad, n) + str;
  }

  /**
   * @return {@code str} with the desired number of space chars appended on the right.
   * @see #pad(String, int)
   * @see #padLeft(String, int)
   * @see Strings#padEnd(String, int, char)
   */
  public static String padRight(String str, int nSpaces) {
    return padRight(str, nSpaces, ' ');
  }

  /**
   * @return {@code str} with {@code n} {@code pad} chars appended on the right.
   * @see #pad(String, int)
   * @see #padLeft(String, int)
   * @see Strings#padEnd(String, int, char)
   */
  public static String padRight(String str, int n, char pad) {
    return str + repeat(pad, n);
  }

  /**
   * Pads the given string with {@code pad} chars, such that the result has the desired {@code width}.  In other words,
   * this method performs a <i>center-justify</i> operation with a custom padding char
   * (similar to the <i>left-justify</i> and <i>right-justify</i> capabilities offered by {@link String#format} 
   * with the {@code %-Ns} and {@code %Ns} patterns).
   * <p>
   * This method never truncates the given string: if {@code str.length() <= width}, then it is returned unmodified.
   * <p>
   * If the string can't be padded evenly on both sides (to the desired {@code width}), then
   * the padding on the right will be longer by 1.
   *
   * <h3>Examples:</h3>
   * <pre>
   *   padCenter("foo", 2, '-') &rarr; "foo"
   *   padCenter("foo", 4, '-') &rarr; "foo-"
   *   padCenter("foo", 5, '-') &rarr; "-foo-"
   *   padCenter("foo", 6, '-') &rarr; "-foo--"
   * </pre>
   *
   * @param str the string to be center-justified
   * @param width the desired length of the result
   * @param pad the padding char
   *
   * @return the given string padded and center-justified to the desired width.  The result is guaranteed to have
   * the desired width unless the input string is longer than that, in which case it will not be truncated.
   *
   * @see <a href="https://stackoverflow.com/a/8155547/1965404">Solution given on StackOverflow</a>
   */
  public static String padCenter(String str, int width, char pad) {
    if (str == null || width <= str.length())
      return str;

    StringBuilder sb = new StringBuilder(width);
    for (int i = 0; i < (width - str.length()) / 2; i++) {
      sb.append(pad);
    }
    sb.append(str);
    while (sb.length() < width) {
      sb.append(pad);
    }
    return sb.toString();
  }

  /**
   * Pads the given string with spaces, such that the result has the desired {@code width}.
   * This is similar to the <i>left-justify</i> and <i>right-justify</i> capabilities offered by {@link String#format} 
   * (with the {@code %-Ns} and {@code %Ns} patterns).
   * <p>
   * This method never truncates the given string: if {@code str.length() <= width}, then it is returned unmodified.
   * <p>
   * If the string can't be padded evenly on both sides (to the desired {@code width}), then
   * the padding on the right will be longer by 1.
   *
   * <h3>Examples:</h3>
   * <pre>
   *   justifyCenter("foo", 2) &rarr; "foo"
   *   justifyCenter("foo", 4) &rarr; "foo "
   *   justifyCenter("foo", 5) &rarr; " foo "
   *   justifyCenter("foo", 6) &rarr; " foo  "
   * </pre>
   *
   * @param str the string to be center-justified
   * @param width the desired length of the result
   *
   * @return the given string padded and center-justified to the desired width.  The result is guaranteed to have
   * the desired width unless the input string is longer than that, in which case it will not be truncated.
   */
  public static String justifyCenter(String str, int width) {
    return padCenter(str, width, ' ');
  }

  /**
   * Pads the given string with spaces on the right, such that the result has the desired {@code width}.
   * This is similar to the <i>left-justify</i> capability offered by {@link String#format}
   * (with the {@code %-Ns} pattern).
   * <p>
   * This method never truncates the given string: if {@code str.length() <= width}, then it is returned unmodified.
   *
   * <h3>Examples:</h3>
   * <pre>
   *   justifyLeft("foo", 2) &rarr; "foo"
   *   justifyLeft("foo", 4) &rarr; "foo "
   *   justifyLeft("foo", 5) &rarr; "foo  "
   *   justifyLeft("foo", 6) &rarr; "foo   "
   * </pre>
   *
   * @param str the string to be left-justified
   * @param width the desired length of the result
   *
   * @return the given string padded on the right to the desired width.  The result is guaranteed to have
   * the desired width unless the input string is longer than that, in which case it will not be truncated.
   * @see Strings#padEnd(String, int, char)
   */
  public static String justifyLeft(String str, int width) {
    if (str == null || width <= str.length())
      return str;
    return padRight(str, width - str.length(), ' ');
  }

  /**
   * Pads the given string with spaces on the left, such that the result has the desired {@code width}.
   * This is similar to the <i>right-justify</i> capability offered by {@link String#format}
   * (with the {@code %Ns} pattern).
   * <p>
   * This method never truncates the given string: if {@code str.length() <= width}, then it is returned unmodified.
   *
   * <h3>Examples:</h3>
   * <pre>
   *   justifyRight("foo", 2) &rarr; "foo"
   *   justifyRight("foo", 4) &rarr; " foo"
   *   justifyRight("foo", 5) &rarr; "  foo"
   *   justifyRight("foo", 6) &rarr; "   foo"
   * </pre>
   *
   * @param str the string to be right-justified
   * @param width the desired length of the result
   *
   * @return the given string padded on the left to the desired width.  The result is guaranteed to have
   * the desired width unless the input string is longer than that, in which case it will not be truncated.
   * @see Strings#padStart(String, int, char)
   * @see #justifyRight(int)
   */
  public static String justifyRight(String str, int width) {
    if (str == null || width <= str.length())
      return str;
    return padLeft(str, width - str.length(), ' ');
  }

  /**
   * Returns a function that can be used to apply a fixed <i>right-justify</i> transformation to a stream of strings:
   * <pre>
   *   s -> {@link #justifyRight(String, int) justifyRight(s, width)}
   * </pre>
   *
   * @param width the desired length of the result string
   * @return a function that applies {@link #justifyRight(String, int) justifyRight(s, width)} to a string {@code s}.
   * @see #justifyRight(String, int)
   */
  public static UnaryOperator<String> justifyRight(int width) {
    return s -> justifyRight(s, width);
  }  // TODO: maybe provide matching higher-order functions for the other methods in this class (e.g. justifyLeft, etc.)

  /**
   * @return a string which represents a mirror image of the given string
   * @see StringBuilder#reverse()
   */
  public static String reverse(String s) {
    // NOTE: this method was written because earlier versions of GWT didn't emulate StringBuilder.reverse
    char[] chars = s.toCharArray();
    for (int i = 0, j = chars.length-1; i < j; i++, j--) {
      // swap the symmetric characters at indices i and j
      char temp = chars[i];
      chars[i] = chars[j];
      chars[j] = temp;
    }
    return new String(chars);
  }

  /**
   * Pretty-prints a 2-dimensional array into a multiline string, such that columns are fixed-width and
   * right-justified.  The column values will be delimited by the given separator string.
   *
   * @param colSep the column values will be delimited by this string
   *     (e.g. blank space, a box-drawing character like {@code \u2551}, etc.)
   * @see <a href="https://en.wikipedia.org/wiki/Box-drawing_character#Block_Elements">Box-drawing characters</a>
   * @see TablePrinter
   * @deprecated this implementation might be replaced by {@link TablePrinter} in the future
   */
  public static String matrixToPrettyString(String[][] matrix, String colSep) {
    checkArgument(!ArrayUtils.isEmpty(matrix), "Empty or null array given");
    // 1) construct the matrix to print from the given input (computing the min col widths, replacing null elements with empty strings, etc.)
    final int nRows = matrix.length;
    int nCols = -1;  // will be computed in the loop body
    // ensure that we have a perfectly square array (filling missing cells with empty strings)
    String[][] table = new String[nRows][];
    int[] colWidths = null;  // the min width required for each column
    for (int i = 0; i < nRows; i++) {
      String[] row = matrix[i];
      // make sure the row is not null or empty TODO: perhaps just fill with empty strings in that case?
      checkArgument(!ArrayUtils.isEmpty(row), "Empty or null row (%s)", i);
      if (colWidths == null) {
        assert i == 0;
        // this is the 1st iteration: will use the 1st row's length for the entire table
        nCols = row.length;
        colWidths = new int[nCols];
      }
      else {
        // make sure this row has the same size as the preceding rows
        checkArgument(row.length == nCols,
            "Length of row %s (%s) doesn't match the others (%s)", i, row.length, nCols);
      }
      String[] rowCopy = new String[nCols];
      // copy the row, replacing any null elements with empty strings, and updating the required column widths
      for (int j = 0; j < row.length; j++) {
        String val = nonNull(row[j]);
        colWidths[j] = Math.max(colWidths[j], val.length());
        rowCopy[j] = val;
      }
      table[i] = rowCopy;
    }

    // 2) print the result
    StringBuilder out = new StringBuilder();
    for (int i = 0; i < nRows; i++) {
      // print a newline after the previous row
      if (i > 0)
        out.append('\n');
      for (int j = 0; j < nCols; j++) {
        String val = table[i][j];
        out.append(justifyRight(val, colWidths[j])).append(colSep);
      }
    }
    return out.toString();
  }

  /**
   * Pretty-prints a 2-dimensional array into a multiline string, such that columns are fixed-width and
   * right-justified.  The column values will be delimited by a single empty space character.
   *
   * @param matrix a non-empty rectangular array such that all rows have the same length
   * @throws IllegalArgumentException if the array is {@code null}, empty, or not all rows have the same length
   *
   * @see TablePrinter
   * @deprecated this implementation might be replaced by {@link TablePrinter} in the future
   */
  public static String matrixToPrettyString(String[][] matrix) {
    return matrixToPrettyString(matrix, " ");
  }

  /**
   * Splits the given string on the given delimiter, returning a list of the tokens (each one trimmed). Ignores elements
   * whose value is an empty string after trimming.
   * @param str a string like "a, b, c"
   * @param delimRegex a regular expression like ","
   * @return a list like ["a", "b", "c"], or an empty list if the string didn't contain any non-blank tokens after the split.
   * @see Splitter
   */
  @Nonnull
  public static List<String> splitAndTrim(String str, String delimRegex) {
    String[] parts = str.trim().split(delimRegex);
    List<String> ret = new ArrayList<String>(parts.length);
    for (String part : parts) {
      String trimmedPart = part.trim();
      if (notBlank(trimmedPart))
        ret.add(trimmedPart);
    }
    return ret;
  }

  /**
   * Similar to {@link String#split(String, int)} with a {@code limit} argument of {@code -1}, except the
   * {@code separator} is not treated as a regex.  Since it doesn't compile and execute a regex, this method
   * will probably run faster than {@link String#split(String, int)}.
   *
   * <p>
   *  <b>NOTE:</b> this method differs from {@link String#split(String)} in that it includes the trailing empty strings,
   *  therefore its behavior is more like the JavaScript version of {@code split} than the Java version.
   * </p>
   *
   * @param str the string to split
   * @param separator the string will be split on each occurrence of this exact argument, which is <b>not</b> treated as a regex
   * @return the list of strings computed by splitting {@code str} string around exact matches of {@code separator}
   * <p>Example: given the args {@code "__a___b___c___"} and {@code "_"}, returns
   * <pre>["","","a","","","b","","","c","","",""]</pre>
   *
   * @see <a href="https://stackoverflow.com/q/31670822">StackOverflow: "Java vs JavaScript split behavior"</a>
   * @see Splitter
   */
  public static List<String> split(String str, String separator) {
    int start = 0;
    int end = str.indexOf(separator, start);
    if (end < 0)
      return Collections.singletonList(str);  // string not empty and contains no delimiters: treat the whole string as a token
    else {
      ArrayList<String> ret = new ArrayList<String>();
      while (end != -1) {
        String token = str.substring(start, end);
        ret.add(token);
        start = end + separator.length();
        end = str.indexOf(separator, start);
      }
      // might need to add the last token (after the last delimiter)
      if (start <= str.length())
        ret.add(str.substring(start));
      /*
        What should we do with empty tokens at the end of the string? Java and JavaScript differ in this regard.
        Example: "__a___b___c___".split("_")
          Java: [, , a, , , b, , , c]
          JavaScript: [, , a, , , b, , , c, , , ]
        However, Java's String also provides an overloaded method: String#split(String, int), which, when invoked
        with limit = -1, produces the same result as the JS version with no limit:
          "__a___b___c___".split("_", -1) --> [, , a, , , b, , , c, , , ]
       */
      return ret;
    }
  }

  public static List<Character> asList(CharSequence str) {
    return CollectionUtils.asList(new CharSequenceIterator(str));
  }

  /**
   * @return the characters in the given {@link String} sorted in alphabetical order.
   */
  public static String sorted(String str) {
    char[] chars = str.toCharArray();
    Arrays.sort(chars);
    return new String(chars);
  }

  /**
   * @return a set of the unique characters in the given string, whose iterator will return the chars in the same
   * order that they appeared in the sequence
   */
  public static Set<Character> toCharacterSet(CharSequence str) {
    return toCharacterSet(str, new LinkedHashSet<Character>());
  }

  private static Set<Character> toCharacterSet(CharSequence str, Set<Character> charSet) {
    for (int i = 0; i < str.length(); i++)
      charSet.add(str.charAt(i));
    return charSet;
  }

  /**
   * If the argument is a {@link CharSequence} or {@link Character}, it will be quoted to resemble a string or char literal.
   * If it's an array, it will be converted using the appropriate version of {@link Arrays#toString} / {@link Arrays#deepToString}.
   * Otherwise same as {@link String#valueOf(Object)}.
   * <p>
   * This can be useful for printing field values in a {@link #toString()} method.
   *
   * @param value the value to print
   * @return the result of {@link String#valueOf(Object)}, quoted if {@code value} is a string.
   */
  public static String valueToString(Object value) {
    StringBuilder buf = new StringBuilder();
    appendValue(buf, value);
    return buf.toString();
  }

  /**
   * Tests if a substring ends with the specified suffix.
   * <p>
   * This is equivalent to {@code str.substring(0, endIndex).endsWith(suffix)}, but faster, because it doesn't
   * call {@link String#substring(int, int)} to create an intermediate object.
   * @param str the string to test
   * @param endIndex the end index of the substring within {@code str}
   * @param suffix the suffix
   * @return {@code true} iff {@code str.substring(0, endIndex).endsWith(suffix) == true}
   * @throws StringIndexOutOfBoundsException if {@code endIndex} either negative or larger than the length of {@code str}
   * (to emulate the behavior of {@link String#substring(int, int)})
   * @see String#endsWith(String)
   * @see String#startsWith(String, int)
   */
  public static boolean endsWith(String str, int endIndex, String suffix) throws StringIndexOutOfBoundsException {
    if (endIndex < 0 || endIndex > str.length())
      throw new StringIndexOutOfBoundsException(endIndex);
    return str.startsWith(suffix, endIndex - suffix.length());
  }

  /**
   * Returns the last Unicode code point in the given string, which may or may not be the same as
   * {@code str.charAt(str.length()-1)}, depending on whether the last {@code char} in the string is a
   * {@link Character#isSurrogate(char) surrogate}.
   *
   * @return the last Unicode code point in the string
   * @throws IllegalArgumentException if the string is {@code null} or empty
   * @see String#codePointBefore(int)
   * @see #codePoints(String)
   * @see CodePointIterator
   */
  public static int lastCodePoint(String str) {
    if (isEmpty(str))
      throw new IllegalArgumentException(valueToString(str));
    return str.codePointBefore(str.length());
  }

  /**
   * Returns the last {@code char} in the given string (i.e. {@code str.charAt(str.length()-1)}).
   *
   * @return {@code str.charAt(str.length()-1)}
   * @throws IllegalArgumentException if the string is {@code null} or empty
   * @see #lastCodePoint(String)
   */
  public static char lastChar(String str) {
    if (isEmpty(str))
      throw new IllegalArgumentException(valueToString(str));
    return str.charAt(str.length()-1);
  }

  /**
   * Converts a string to an array of code points.
   * <p>
   *   <em>NOTE</em>: if you simply want to iterate over the code points, it would be more efficient to use
   *   {@link #codePointsStream(String)} or {@link CodePointIterator}
   *   (assuming you can't use {@link CharSequence#codePoints()}, which isn't implemented in GWT).
   * </p>
   * @return the code points in the given string as an array
   * @see CodePointIterator
   * @see CodePointIterator#codePointsStream(CharSequence)
   * @see String#codePoints()
   */
  public static int[] codePoints(String str) {
    int codePointCount = str.codePointCount(0, str.length());
    int[] ret = new int[codePointCount];
    CodePointIterator codePointIterator = new CodePointIterator(str);
    for (int i = 0; codePointIterator.hasNext(); i++) {
      ret[i] = codePointIterator.nextInt();
    }
    return ret;
  }

  public static String codePointsToString(int[] codePoints) {
    return new String(codePoints, 0, codePoints.length);
  }

  /**
   * Emulates the behavior of {@link String#codePoints()} for GWT code.
   * @return an IntStream of Unicode code points from the given string
   */
  public static IntStream codePointsStream(String str) {
    return CodePointIterator.codePointsStream(str);
  }


  /**
   * Generates the same default string representation of the given object as the one that would been produced by
   * {@link Object#toString()} if the object's class didn't override either {@link Object#toString()} or {@link Object#hashCode()}).
   * <p>
   * More precisely, this method returns a string equal to the value of:
   * <pre>
   *   o.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(o))
   * </pre>
   * <p>
   * Examples:
   * <pre>
   * {@link #identityToString}(null)         = "null"
   * {@link #identityToString}("")           = "java.lang.String@1e23"
   * {@link #identityToString}(Boolean.TRUE) = "java.lang.Boolean@7fa"
   * </pre>
   *
   * @param o the object to create a toString for, may be {@code null}
   * @return the default toString text, or {@code "null"} if the arg was {@code null}
   *
   * @see Object#toString()
   * @see System#identityHashCode(Object)
   * @see org.apache.commons.lang3.ObjectUtils#identityToString(java.lang.Object)
   */
  public static String identityToString(Object o) {
    if (o == null)
      return "null";
    return o.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(o));
  }

  /**
   * Similar to {@link #identityToString(Object)}, but produces shorter strings by using {@link Class#getSimpleName()}
   * (instead of {@link Class#getName()}) and including only the first 4 hex chars of {@link System#identityHashCode(Object)}.
   * <p>
   * In other words, this method returns a string equal to the value of:
   * <pre>
   *   o.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(o)).substring(0, 4)
   * </pre>
   * <p>
   * Examples:
   * <pre>
   *   {@link #idToString}(null)         = "null"
   *   {@link #idToString}("")           = "String@7b1d"
   *   {@link #idToString}(Boolean.TRUE) = "Boolean@299a"
   * </pre>
   */
  public static String idToString(Object o) {
    if (o == null)
      return "null";
    return o.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(o)).substring(0, 4);
  }

  /**
   * Same as {@link Character#isSurrogate(char)}, provided for GWT compatibility.
   */
  public static boolean isSurrogate(char ch) {
    // copied verbatim from the source code in Character.java
    return ch >= Character.MIN_SURROGATE && ch < (Character.MAX_SURROGATE + 1);
  }

  /**
   * @return binary string representing the bits in the given byte
   */
  public static String byteToBinary(byte b) {
    return Integer.toBinaryString((b & 0xFF) + 0x100).substring(1);  // see https://stackoverflow.com/a/17496691
  }
}