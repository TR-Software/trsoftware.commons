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

package solutions.trsoftware.tools.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Facilitates testing regular expressions.
 *
 * @author Alex
 * @since 11/27/2017
 */
public class RegexTester {

  private final String input;
  private final Pattern pattern;
  private final Matcher matcher;

  public RegexTester(String regex, String input) {
    this(Pattern.compile(regex), input);
  }

  public RegexTester(Pattern regex, String input) {
    pattern = regex;
    matcher = pattern.matcher(input);
    this.input = input;
  }

  public List<String> listGroups() {
    if (!matcher.matches())
      return null;
    int n = matcher.groupCount();
    ArrayList<String> groups = new ArrayList<>();
    for (int i = 1; i <= n; i++) {
      groups.add(matcher.group(i));
    }
    return groups;
  }
}
