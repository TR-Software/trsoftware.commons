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

package solutions.trsoftware.commons.server.io;

import solutions.trsoftware.commons.shared.util.StringUtils;

import java.io.PrintStream;
import java.util.Objects;

/**
 * Encapsulates a {@link PrintStream} and provides methods that are useful for printing generated code.
 *
 * @see #indent()
 * @author Alex, 10/10/2016
 */
public class CodeWriter {

  /**
   * The number of spaces to print for each {@link #indentLevel}.  E.g. 2, 4, etc.
   */
  private int indentSpaces = 2;

  protected PrintStream out;

  protected int indentLevel;

  public CodeWriter(PrintStream out) {
    this.out = Objects.requireNonNull(out);
  }

  /**
   * @param out the starting value for {@link #out}
   * @param indentSpaces the number of spaces to print for each indent level
   * @param indentLevel the starting value for {@link #indentLevel}
   */
  public CodeWriter(PrintStream out, int indentSpaces, int indentLevel) {
    this.indentSpaces = indentSpaces;
    this.indentLevel = indentLevel;
    this.out = out;
  }

  /**
   * @return a string containing the right number of space chars for the current {@link #indentLevel}
   */
  protected String indent() {
    return StringUtils.indent(indentLevel * indentSpaces);
  }

  /**
   * Prints the given string prefixed by the current indentation.
   * @param x the string to print
   */
  public void println(String x) {
    out.println(indent() + x);
  }

  /**
   * @see PrintStream#println()
   */
  public void println() {
    out.println();
  }
}
