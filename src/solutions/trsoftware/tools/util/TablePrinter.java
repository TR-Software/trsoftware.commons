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

package solutions.trsoftware.tools.util;

import solutions.trsoftware.commons.server.memquery.output.FixedWidthPrinter;
import solutions.trsoftware.commons.server.memquery.output.HtmlTablePrinter;

import java.io.PrintStream;
import java.util.List;

import static solutions.trsoftware.commons.server.memquery.output.FixedWidthPrinter.*;
import static solutions.trsoftware.commons.shared.util.StringUtils.*;

/**
 * Utilities for printing tabular data.
 * <p style="color: #6495ed; font-weight: bold;">
 *   TODO: extract the functionality provided by {@link FixedWidthPrinter} and {@link HtmlTablePrinter}
 * </p>
 * @author Alex
 * @since 5/13/2018
 */
public class TablePrinter {

  /**
   * Prints a single-column table.
   * <h3>Example:</h3>
   *<pre>
   * ╔════ heading ════╗
   * ║ line 0          ║
   * ║ line 1          ║
   * ║ ...             ║
   * ║ line N          ║
   * ╚═════════════════╝
   *</pre>
   * @param out where to print
   * @param heading will be embedded in the top border
   * @param lines the body of the table
   */
  public static void printMenu(PrintStream out, String heading, List<String> lines) {
    // if the heading is not already padded with whitespace on both ends, do so now
    if (!heading.matches("\\s+.*?\\s+"))
      heading = pad(heading, 1);
    // now we want to wrap the heading with h-border symbols, such that it becomes the same length as the longest row in the body
    int maxLineLength = Math.max(heading.length()+10, lines.stream().mapToInt(String::length).max().orElse(0) + 2);
//    String wrappedHeading = surround(heading, repeat(H_BORDER_CHAR, (maxLineLength - heading.length()) / 2));
    heading = CORNER_CHARS[0][0] + padCenter(heading, maxLineLength, H_BORDER_CHAR) + CORNER_CHARS[0][2];
    out.println(heading);
    for (String line : lines) {
      out.printf("%c %-" + (maxLineLength-2) + "s %c%n", V_BORDER_CHAR, line, V_BORDER_CHAR);
    }
    String bottomBorder = CORNER_CHARS[2][0] + repeat(H_BORDER_CHAR, maxLineLength) + CORNER_CHARS[2][2];
    out.println(bottomBorder);
  }
}
