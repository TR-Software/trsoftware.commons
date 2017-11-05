/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.shared.util.template;

import solutions.trsoftware.commons.client.util.GwtUtils;

import java.util.LinkedList;

import static solutions.trsoftware.commons.shared.util.template.SimpleTemplateParser.State.*;

/**
 * An FSA-based scanner (a.k.a lexer) that recognizes a simple regular language consisting simply of:
 * <ol>
 *   <li><b>text</b> spans</li>
 *   <li>
 *     <b>variable</b> spans between opening and closing tags defined by {@link #v0} and {@link #v1}
 *     (e.g. <code>${</code> and <code>}</code>)
 *   </li>
 *   <li>
 *     <i>comment</i> spans between opening and closing tags defined by {@link #c0} and {@link #c1}
 *     (e.g. {@code <!--} and {@code -->})
 *   </li>
 * </ol>
 * Only the <b>text</b> and <b>variable</b> spans are kept, while the <i>comment</i> spans are discarded, as well as the
 * the {@link #v0}/{@link #v1} and {@link #c0}/{@link #c1} tags themselves (they won't appear in the rendered output of the template).
 * <p>
 *   For example, suppose the variable {@code templateString} contains the following template string:
 *   <hr>
 *   <pre>
 *     {@code <!--} Example template {@code -->}
 *     Hello ${NAME},
 *     Your account number is ${ACCT_NUM}.
 *     Take care!</pre>
 *   <hr>
 * Parsed using {@link #DEFAULT_SYNTAX} and rendered by executing
 * <nobr>{@code DEFAULT_SYNTAX.parseTemplate(templateString).render("NAME", "Foo", "ACCT_NUM", "1234")}</nobr>
 * it would yield the following output:
 *   <hr>
 *   <pre>
 *
 *     Hello Foo,
 *     Your account number is 1234.
 *     Take care!</pre>
 *   <hr>
 * </p>
 * <p>
 *   Does not rely on regex facilities, and therefore works identically (and efficiently!) in both clientside GWT code and serverside Java code.
 *   Actually, there is just one minor use of a regex with {@link String#matches(String)} to test legality of variable names,
 *   but since the regex we use there is so minimal, there's pretty much no chance of it working differently in JS and Java.
 * <font color='red'>
 *   TODO: actually, we should just remove this regex, since there's really no need for variable names to match any specific pattern,
 *   as long as they don't conflict with the tags (if we do this, make sure to also update the javadoc for {@link #DEFAULT_SYNTAX}
 * </font>
 
 * </p>
 *
 * Instances of this class are immutable, and therefore thread-safe.
 *
 * @author Alex
 */
public class SimpleTemplateParser implements TemplateParser {

  /**
   * Parses instances of {@link Template} based on the following syntax:
   * <ol>
   *   <li><code>${variable}</code>, where {@code variable} &isin; <code>([A-Za-z0-1]+)</code></li>
   *   <li>{@code <!--comment-->}, where {@code comment} &isin; <code>(.*)</code></li>
   * </ol>
   *
   * TODO: make this a factory method or a lazy-init field to save memory in case this object is never used
   */
  public static final TemplateParser DEFAULT_SYNTAX = new SimpleTemplateParser("${", "}", "<!--", "-->");

  /** @return A template parsed from the argument based on {@link #DEFAULT_SYNTAX} */
  public static Template parseDefault(String str) {
    return DEFAULT_SYNTAX.parseTemplate(str);
  }

  static enum State {
    IN_TEXT,
    IN_VARIABLE,
    IN_COMMENT
  }

  /** <b>variable</b> opening tag (e.g. "<code>${</code>") */
  private final String v0;
  
  /** <b>variable</b> closing tag (e.g. "<code>}</code>") */
  private final String v1;
  
  /** <i>comment</i> opening tag (e.g. "{@code <!--}") */
  private final String c0;
  
  /** <i>comment</i> opening tag (e.g. "{@code <!--}") */
  private final String c1;

  /**
   * Creates a parser for the template language with the given syntax.
   *
   * @param v0 <b>variable</b> opening tag (e.g. "<code>${</code>")
   * @param v1 <b>variable</b> closing tag (e.g. "<code>}</code>")
   * @param c0 <i>comment</i> opening tag (e.g. "{@code <!--}")
   * @param c1 <i>comment</i> opening tag (e.g. "{@code <!--}")
   */
  public SimpleTemplateParser(String v0, String v1, String c0, String c1) {
    this.v0 = v0;
    this.v1 = v1;
    this.c0 = c0;
    this.c1 = c1;
  }

  /**
   * Parses the given template string into the internal representation that can be used to perform fast substitutions at run-time.
   * @param templateString should use the syntax defined by this {@link SimpleTemplateParser}'s
   * {@link SimpleTemplateParser#SimpleTemplateParser(String, String, String, String) constructor}
   */
  @Override
  public Template parseTemplate(String templateString) {
    int length = templateString.length();
    LinkedList<TemplatePart> parts = new LinkedList<TemplatePart>();

    int next = 0;
    State state = IN_TEXT;
    while (next < templateString.length()) {
      switch (state) {
        case IN_TEXT:
          String run;
          int nextVO = templateString.indexOf(v0, next);
          int nextCO = templateString.indexOf(c0, next);
          if (nextVO >= 0 && (nextCO < 0 || nextVO < nextCO)) {
            run = templateString.substring(next, nextVO);
            state = IN_VARIABLE;
            next = nextVO + v0.length();
          }
          else if (nextCO >= 0 && (nextVO < 0 || nextCO < nextVO)) {
            run = templateString.substring(next, nextCO);
            state = IN_COMMENT;
            next = nextCO + c0.length();
          } else {
            // reached the end of string
            run = templateString.substring(next, templateString.length());
            state = null;
            next = templateString.length();
          }
          if (run.length() > 0) {
            parts.add(new StringPart(run));
          }
          break;
        case IN_COMMENT:
          int nextCC = templateString.indexOf(c1, next);
          if (nextCC < 0)
            throw syntaxError("unterminated comment", templateString, next);
          next = nextCC + c1.length();
          state = IN_TEXT;
          break;
        case IN_VARIABLE:
          int nextVC = templateString.indexOf(v1, next);
          if (nextVC < 0)
            throw syntaxError("unterminated variable", templateString, next);
          String varName = templateString.substring(next, nextVC);
          // make sure the variable name matches [A-Za-z0-1_]+ (i.e. it doesn't span multiple lines, and stuff like that)
          String varNameRegex = "[A-Za-z0-1_]+"; // TODO: allow dots and maybe other chars in the name (Ant variables use lots of dots)
          if (!varName.matches(varNameRegex)) {
            throw syntaxError("'" + varName + "' is not a legal variable name (should match /" + varNameRegex + "/)", templateString, next);
          }
          parts.add(new VariablePart(varName));
          next = nextVC + v1.length();
          state = IN_TEXT;
          break;
      }
    }
    return new Template(parts, (int)(length*1.5));
  }

  private IllegalArgumentException syntaxError(String info, String templateString, int position) {
    // TODO: print position as a lineNum:colNum pair (should be able to accomplish this by breaking up templateString into lines)
    return new IllegalArgumentException(GwtUtils.getSimpleName(getClass()) + " parsing error at [" + position + "]: " + info + "; template: \"" + templateString + "\"");
  }

}