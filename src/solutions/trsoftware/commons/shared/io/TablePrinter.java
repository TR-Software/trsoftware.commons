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

package solutions.trsoftware.commons.shared.io;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.gwt.core.shared.GwtIncompatible;
import solutions.trsoftware.commons.shared.util.LogicUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;

import java.io.PrintStream;
import java.util.List;

import static solutions.trsoftware.commons.shared.util.StringUtils.*;

/**
 * Utilities for printing tabular data.
 * <p><b>Example</b>:
 * <pre>
 *   TablePrinter printer = new TablePrinter();
 *   for (int i = 0; i < 5; i++) {
 *     printer.newRow()
 *         .addCol("x", "%d", i)
 *         .addCol("sin(x)", "%f", Math.sin(i))
 *         .addCol("cos(x)", "%.2f", Math.cos(i));
 *   }
 *   printer.printTable();
 * </pre>
 * <i>Output:</i>
 * <pre>
 *   ╔═╦═════════╦══════╗
 *   ║x║   sin(x)║cos(x)║
 *   ╠═╬═════════╬══════╣
 *   ║0║ 0.000000║  1.00║
 *   ║1║ 0.841471║  0.54║
 *   ║2║ 0.909297║ -0.42║
 *   ║3║ 0.141120║ -0.99║
 *   ║4║-0.756802║ -0.65║
 *   ╚═╩═════════╩══════╝
 * </pre>
 * @author Alex
 * @since 5/13/2018
 * @see StringUtils#matrixToPrettyString(String[][], String)
 */
public class TablePrinter {

  /*
   * TODO: extract the table-printing functionality from the MemQuery package (e.g {@link FixedWidthPrinter} and {@link HtmlTablePrinter})
   *   to create a general-purpose, GWT-compatible, rich table printing facility.
   *   Also see the limited implementation in {@link StringUtils#matrixToPrettyString(String[][], String)}
   */

  // print config options:
  private boolean bordersEnabled = true;  // TODO: allow setting this to false for a fixed-width table without borders

  // builder fields:
  private final Table<Integer, String, String> table = HashBasedTable.create();
  private int rowIdx = 0;  // row 0 is reserved for the col headings

  // builder methods:

  /**
   * Starts a new row in the table.  This method should be invoked before adding any column data for a row.
   * Subsequent calls to {@link #addCol} will set the cell values in this new row.
   */
  public TablePrinter newRow() {
    rowIdx++;
    return this;
  }

  /**
   * Adds a column value for the {@linkplain #newRow() current row}.
   * @param name the column name
   * @param value the cell value
   * @throws IllegalStateException if the {@link #newRow()} hasn't been invoked yet
   */
  public TablePrinter addCol(String name, String value) {
    Preconditions.checkState(rowIdx > 0, "Must invoke newRow() before writing any column data");
    table.put(0, name, name);  // insert colName into top row (for col headings)
    table.put(rowIdx, name, value);
    return this;
  }

  /**
   * Adds a column value for the {@linkplain #newRow() current row}.
   * @param name the column name
   * @param value the cell value (will be converted with {@link String#valueOf(Object)})
   * @throws IllegalStateException if the {@link #newRow()} hasn't been invoked yet
   */
  public TablePrinter addCol(String name, Object value) {
    return addCol(name, String.valueOf(value));
  }

  /**
   * Adds a column value for the {@linkplain #newRow() current row}.
   * @param name the column name
   * @param format {@linkplain String#format format string} for the value
   * @param value the cell value (will be converted with {@link String#format(String, Object...)})
   * @throws IllegalStateException if the {@link #newRow()} hasn't been invoked yet
   */
  @GwtIncompatible("String.format")
  public TablePrinter addCol(String name, String format, Object value) {
    return addCol(name, String.format(format, value));
    // TODO: maybe create a GWT-compatible version that uses a Function<Object, String> or a Renderer instead of String.format
  }

  /**
   * @return true if no data has been entered yet (can check this to avoid printing an "Empty table" message)
   */
  public boolean isEmpty() {
    return table.isEmpty();
  }

  /**
   * Prints the table to {@link System#out}
   */
  public void printTable() {
    printTable(System.out);
  }

  /**
   * Prints the table to the given stream
   */
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
      if (table.isEmpty()) {
        out.println("<Empty table>");
        return;
      }
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
  @GwtIncompatible("printf")
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
