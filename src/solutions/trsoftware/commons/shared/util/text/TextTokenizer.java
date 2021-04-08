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

package solutions.trsoftware.commons.shared.util.text;

import solutions.trsoftware.commons.shared.util.StringUtils;

/**
 * A way to convert text to tokens and back to text again, which can
 * be implemented depending on source language (e.g. English vs. Chinese).
 * <p>
 * All implementing classes should be stateless (the same instance can be
 * reused and shared between threads)
 *
 * <p style="color: #0073BF; font-weight: bold;">
 * TODO: see {@link java.text.BreakIterator} which might be a better choice than this class
 * </p>
 *
 * @author Alex
 */
public interface TextTokenizer {

  String getDelimiter();

  /** Breaks up the given text into tokens */
  String[] tokenize(String text);

  /**
   * Reconstructs text from the given tokens by inserting the language-specific
   * word delimiter (e.g. space for English)
   */
  default String join(String[] tokens) {
    return StringUtils.join(getDelimiter(), tokens);
  }
}
