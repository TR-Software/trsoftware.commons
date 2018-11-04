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

import solutions.trsoftware.commons.shared.util.CollectionUtils;
import solutions.trsoftware.commons.shared.util.StringTokenizer;
import solutions.trsoftware.commons.shared.util.StringUtils;

import java.util.List;


/**
 * Oct 19, 2009
 * @author Alex
 */
public class WhitespaceTokenizer implements TextTokenizer {

  @Override
  public String getDelimiter() {
    return " ";
  }

  public String[] tokenize(String text) {
    List<String> tokenList = CollectionUtils.asList(new StringTokenizer(text));
    return tokenList.toArray(new String[tokenList.size()]);
  }

  public String join(String[] tokens) {
    return StringUtils.join(getDelimiter(), tokens);
  }

}
