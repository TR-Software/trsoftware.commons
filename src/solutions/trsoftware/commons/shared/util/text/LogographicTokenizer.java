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

package solutions.trsoftware.commons.shared.util.text;

/**
 * Treats every char in a text as an individual word for logographic languages.
 * <p>
 * <strong>NOTE</strong>: this implementation might fail if the text uses "supplemental" Unicode characters
 * (which are represented by 2-char "code pairs" in Java).
 * If that ever becomes an issue, something like {@link java.text.BreakIterator} might be more appropriate.
 *
 * @author Alex
 * @see WhitespaceTokenizer
 */
public class LogographicTokenizer implements TextTokenizer {

  public static final transient LogographicTokenizer INSTANCE = new LogographicTokenizer();

  @Override
  public String getDelimiter() {
    return "";
  }

  public String[] tokenize(String text) {
    String[] words = new String[text.length()];
      for (int i = 0; i < words.length; i++) {
        words[i] = text.substring(i, i+1);
      }
    return words;
  }

}
