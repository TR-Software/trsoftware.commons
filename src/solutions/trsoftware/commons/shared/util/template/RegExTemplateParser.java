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

package solutions.trsoftware.commons.shared.util.template;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.regexp.shared.SplitResult;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Uses the cross-GWT-Java {@link RegExp} facility to parse a templates from a syntax that's more powerful than
 * {@link SimpleTemplateParser}, which among its many limitations, requires all variables to have an explicit termination
 * character sequence.  With this class you can have variables terminated by whitespace or EOF, for example.
 *
 * }
 * @author Alex, 3/17/2016
 */
public class RegExTemplateParser implements TemplateParser {

  /**
   * Parses instances of {@link Template} based on a syntax similar to printf:
   * <ol>
   *   <li>{@code %variable}, where {@code variable} &isin; {@code ([A-Za-z0-1]+)} and is terminated by a space.</li>
   *   <li>{@code /*comment*}<code>/</code>, where {@code comment} &isin; <code>(.*)</code></li>
   * </ol>
   */
  public static final TemplateParser PRINTF_SYNTAX = new RegExTemplateParser("%(\\w+)", null);

  /** @return A template parsed from the argument based on {@link #PRINTF_SYNTAX} */
  public static Template parsePrintf(String str) {
    return PRINTF_SYNTAX.parseTemplate(str);
  }

  /** The flags to use for instances of {@link RegExp} compiled from the given patterns */
  private static final String FLAGS = "gm";  // "global" and "multi-line"

  private final String variablePattern, commentPattern;

  /**
   * Creates a regex-based template parser that recognizes the following patterns.
   * @param variablePattern the pattern of template variables: should contain exactly 1 capturing group (the variable name)
   * @param commentPattern the pattern of comments - matches will not be included in the resulting template object.
   */
  public RegExTemplateParser(String variablePattern, String commentPattern) {
     // The RegExp class appears to be mutable despite it providing a "compile" method (its internal state is still
    // updated after every match) so we can't save instances of RegExp in a field - they must be created for every template we parse
    this.variablePattern = variablePattern;
    this.commentPattern = commentPattern;
  }

  @Override
  public Template parseTemplate(String templateString) {
    if (commentPattern != null)
      templateString = stripComments(templateString);
    if (variablePattern != null)
      return new Template(new VariableParser(templateString).getParts());
    else
      return new Template(Arrays.<TemplatePart>asList(new StringPart(templateString)));
  }

  private String stripComments(String templateString) {
    RegExp commentRegex = RegExp.compile(commentPattern, FLAGS);
    SplitResult splits = commentRegex.split(templateString);
    if (splits.length() == 0)
      return "";  // the comment regex matched the entire string
    else {
      StringBuilder ret = new StringBuilder();
      for (int i = 0; i < splits.length(); i++) {
         ret.append(splits.get(i));
      }
      return ret.toString();
    }
  }

  /**
   * Instances are considered equal iff they use the same parsing regexps
   */
  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    RegExTemplateParser that = (RegExTemplateParser)o;

    if (variablePattern != null ? !variablePattern.equals(that.variablePattern) : that.variablePattern != null)
      return false;
    return commentPattern != null ? commentPattern.equals(that.commentPattern) : that.commentPattern == null;
  }

  @Override
  public int hashCode() {
    int result = variablePattern != null ? variablePattern.hashCode() : 0;
    result = 31 * result + (commentPattern != null ? commentPattern.hashCode() : 0);
    return result;
  }

  /** Helper class that hides the awful API provided by {@link RegExp} */
  private class VariableParser {
    private final String input;
    private final RegExp regex;
    private final ArrayList<TemplatePart> parts = new ArrayList<TemplatePart>();
    private int pos;

    private VariableParser(String input) {
      this.input = input;
      regex = RegExp.compile(variablePattern, FLAGS);
    }

    public ArrayList<TemplatePart> getParts() {
      while (pos < input.length()) {
        MatchResult match = regex.exec(input);
        if (match == null)
          break;
        if (match.getGroupCount() != 2)
          throw new IllegalArgumentException("Bad Template variable pattern: " + variablePattern); // needs to contain exactly 1 capturing group (for the variable name)
        int matchStart = match.getIndex();
        maybeEmitStringPart(matchStart);
        parts.add(new VariablePart(match.getGroup(1)));
        pos = regex.getLastIndex();
      }
      maybeEmitStringPart(input.length());
      return parts;
    }

    public void maybeEmitStringPart(int end) {
      if (pos < end) {
        parts.add(new StringPart(input.substring(pos, end)));
        pos = end;
      }
    }
  }


}
