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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.gwt.core.shared.GwtIncompatible;
import solutions.trsoftware.commons.server.memquery.output.FixedWidthPrinter;
import solutions.trsoftware.commons.server.memquery.output.HtmlTablePrinter;
import solutions.trsoftware.commons.shared.util.LogicUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;

import java.io.PrintStream;
import java.util.List;

import static solutions.trsoftware.commons.shared.util.StringUtils.*;

/**
 * Utilities for printing tabular data.
 * <p style="color: #6495ed; font-weight: bold;">
 *   TODO: extract the table-printing functionality from the MemQuery package (e.g {@link FixedWidthPrinter} and {@link HtmlTablePrinter})
 *     to create a general-purpose, GWT-compatible, rich table printing facility.
 *     Also see the limited implementation in {@link StringUtils#matrixToPrettyString(String[][], String)}
 * </p>
 * @author Alex
 * @since 5/13/2018
 * @see StringUtils#matrixToPrettyString(String[][], String)
 */
public class TablePrinter {

  // print config options:
  private boolean bordersEnabled = true;

  // builder fields:
  private final Table<Integer, String, String> table = HashBasedTable.create();
  private int rowIdx = 0;

  // builder methods:
  public TablePrinter newRow() {
    rowIdx++;
    return this;
  }

  public TablePrinter addCol(String name, String value) {
    table.put(0, name, name);  // insert colName into top row (for col headings)
    table.put(rowIdx, name, value);
    return this;
  }

  public TablePrinter addCol(String name, Object value) {
    return addCol(name, String.valueOf(value));
  }

  @GwtIncompatible
  public TablePrinter addCol(String name, String format, Object value) {
    return addCol(name, String.format(format, value));
    // TODO: maybe create a GWT-compatible version that uses a Function<Object, String> or a Renderer instead of String.format
  }

  public void printTable(PrintStream out) {
    new Printer(table, out).printTable();
  }
  

  /* NOTE:
      the printing code is based on FixedWidthPrinter from solutions.trsoftware.commons.server.memquery.output
      TODO: can create subclasses for other formats (e.g. CsvPrinter, HtmlTablePrinter, etc.);
        see solutions.trsoftware.commons.server.memquery.output.ResultSetPrinter
   */
  static class Printer {
    // TODO: can create subclasses for other formats (e.g. CsvPrinter, HtmlTablePrinter, etc.); see solutions.trsoftware.commons.server.memquery.output.ResultSetPrinter
    private boolean bordersEnabled = true;
    private final PrintStream out;

    private final Table<Integer, String, String> table;

    private final String[] colNames;
    private final int[] maxColWidths;
    private final int nRows;
    private final int nCols;

    int rowIdx = 0;

    public Printer(Table<Integer, String, String> table, PrintStream out) {
      this.table = table;
      this.out = out;

      // compute max col widths
      colNames = table.columnKeySet().toArray(new String[0]);
      maxColWidths = new int[colNames.length];
      for (int i = 0; i < colNames.length; i++) {
        String name = colNames[i];
        maxColWidths[i] = this.table.column(name).values().stream().mapToInt(String::length).max().orElse(0);
      }

      nRows = table.rowKeySet().size();
      nCols = colNames.length;
    }

    public void printTable() {
      beginTable();
      for (int i = 0; i < nRows; i++) {
        beginRow(i);
        for (int j = 0; j < nCols; j++) {
          String value = LogicUtils.firstNonNull(table.get(i, colNames[j]), "");
          printCell(value, j);
        }
        endRow(i);
      }
      endTable();
    }


    /**
     * @param rowType 0 for top row, 1 for middle, and 2 for last
     */
    private void maybePrintHorizontalBorder(int rowType) {
      if (isBordersEnabled()) {
        out.print(CORNER_CHARS[rowType][0]);
        for (int i = 0; i < nCols; i++) {
          out.print(repeat(H_BORDER_CHAR, getColWidth(i)));
          // colType: 0 for first col, 1 for middle, and 2 for last
          int colType = isLastCol(i) ? 2 : 1;
          out.print(CORNER_CHARS[rowType][colType]);
        }
        out.println();
      }
    }

    private int getColWidth(int col) {
      return maxColWidths[col];
    }

    protected void beginTable() {
    }

    protected void beginRow(int row) {
      if (isFirstRow(row))
        maybePrintHorizontalBorder(0);
    }

    private boolean isFirstRow(int row) {
      return row == 0;
    }

    protected void endRow(int row) {
      out.println();
      if (isFirstRow(row))
        maybePrintHorizontalBorder(1);
    }

    protected void endTable() {
      maybePrintHorizontalBorder(2);
    }

    protected void printCell(String value, int col) {
      if (isBordersEnabled()/* && isFirstCol(col)*/)
        out.print(getVBorder());
      out.print(StringUtils.justifyRight(value, getColWidth(col)));
      if (isBordersEnabled() && isLastCol(col))
        out.print(getVBorder());
    }

    private boolean isFirstCol(int col) {
      return col == 0;
    }

    private boolean isLastCol(int col) {
      return col == nCols-1;
    }

    private String getVBorder() {
      return isBordersEnabled() ? Character.toString(V_BORDER_CHAR) : "";
    }

    public boolean isBordersEnabled() {
      return bordersEnabled;
    }
  }

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
  // TODO(4/29/2024): maybe deprecate this method
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
