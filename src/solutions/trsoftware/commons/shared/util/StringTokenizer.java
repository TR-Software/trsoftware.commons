/*
 * Copyright 2021 TR Software Inc.
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

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A GWT-compatible version of {@link java.util.StringTokenizer}.
 * <p>
 *   Notable differences from {@link java.util.StringTokenizer} are that this class:
 *   <ol>
 *     <li>implements {@code Iterator<String>} instead of {@code Enumeration<Object>}</li>
 *     <li>is <strong>unable to handle Unicode <i>supplementary characters</i></strong>
 *     (code points represented by <i>surrogate pairs</i> of 2 chars in Java) as delimiters</li>
 *   </ol>
 * </p>
 * @author Alex
 */
public class StringTokenizer implements Iterator<String> {
  /** A sorted array of delimiter characters */
  private final char[] delims;
  private final String str;
  private String nextToken;
  private int nextPosition = 0;

  /**
   * Constructs a string tokenizer for the specified string. The
   * tokenizer uses the default delimiter set, which is
   * {@code " \t\n\r\f"}: the space character,
   * the tab character, the newline character, the carriage-return character,
   * and the form-feed character. Delimiter characters themselves will
   * not be treated as tokens.
   *
   * @param   str   a string to be parsed.
   */
  public StringTokenizer(String str) {
    this(str, " \t\n\r\f");
  }

  /**
   * Constructs a string tokenizer for the specified string using the given
   * string of characters that should be treated as delimiters.
   * @param str the string to tokenize
   * @param delims the delimiter chars to use (must not contain any surrogate pairs)
   */
  public StringTokenizer(String str, String delims) {
    // TODO: throw IAE if delims contains any surrogate pairs
    this.str = str;
    this.delims = delims.toCharArray();
    Arrays.sort(this.delims);
  }

  /**
   * Reads the next token from the string.
   *
   * @return true if a new token was read, false if reached end of input
   */
  private boolean tryToComputeNextToken() {
    int tokenStart = -1;
    for (int i = nextPosition; i < str.length(); i++) {
      char c = str.charAt(i);
      if (Arrays.binarySearch(delims, c) >= 0) {
        // c is a delimiter: ignore it if not started token yet, otherwise close the current token
        if (tokenStart >= 0) {
          nextToken = str.substring(tokenStart, i);
          nextPosition = i + 1;
          return true;
        }
      }
      else if (tokenStart < 0) {
        // c is not a delimiter, and we haven't started a token yet
        tokenStart = i; // just starting the token
      }
    }
    // reached end of string without returning a token; check whether we have a token to return
    if (tokenStart >= 0) {
      nextToken = str.substring(tokenStart);
      nextPosition = str.length();
      return true;
    }
    return false;
  }

  public boolean hasNext() {
    return nextToken != null || tryToComputeNextToken();
  }

  public String next() {
    if (!hasNext())  // will read the next token if there is one
      throw new NoSuchElementException();
    String ret = nextToken;
    nextToken = null;  // clear the field to advance state machine to the next token
    return ret;
  }

  public void remove() {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.shared.util.StringTokenizer.remove has not been fully implemented yet.");
  }

  public String getInputString() {
    return str;
  }
}
