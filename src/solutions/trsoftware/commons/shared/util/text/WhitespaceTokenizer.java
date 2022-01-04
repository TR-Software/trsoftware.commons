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

import solutions.trsoftware.commons.shared.util.CollectionUtils;
import solutions.trsoftware.commons.shared.util.StringTokenizer;

import java.util.List;

/**
 * Uses {@code " "} as the delimiter to tokenize a text into an array of individual words.
 *
 * @author Alex
 * @see StringTokenizer#StringTokenizer(String)
 * @see LogographicTokenizer
 */
public class WhitespaceTokenizer implements TextTokenizer {

  public static final transient WhitespaceTokenizer INSTANCE = new WhitespaceTokenizer();

  @Override
  public String getDelimiter() {
    return " ";
  }

  public String[] tokenize(String text) {
    List<String> tokenList = CollectionUtils.asList(new StringTokenizer(text));
    return tokenList.toArray(new String[tokenList.size()]);
  }

}
