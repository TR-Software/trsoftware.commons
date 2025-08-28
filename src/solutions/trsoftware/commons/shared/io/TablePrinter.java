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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.gwt.core.shared.GwtIncompatible;
import solutions.trsoftware.commons.shared.util.StringUtils;

import javax.annotation.Nonnull;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;
import static solutions.trsoftware.commons.shared.util.LogicUtils.firstNonNull;
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
  public static final TextAlignment DEFAULT_TEXT_ALIGNMENT = TextAlignment.RIGHT;

  /*
   * TODO: extract the table-printing functionality from the MemQuery package (e.g {@link FixedWidthPrinter} and {@link HtmlTablePrinter})
   *   to create a general-purpose, GWT-compatible, rich table printing facility.
   *   Also see the limited implementation in {@link StringUtils#matrixToPrettyString(String[][], String)}
   */

  // print config options:
  private boolean bordersEnabled = true;  // TODO: allow setting this to false for a fixed-width table without borders
  private final Map<String, TextAlignment> colTextAlignments = new LinkedHashMap<>();
  /**
   * The default text alignment for columns that don't have explicit alignments specified
   * @see #setColAlignment(String, TextAlignment)
   * @see #colTextAlignments
   */
  private TextAlignment defaultTextAlignment = DEFAULT_TEXT_ALIGNMENT;

  // data builder fields:
  private final Table<Integer, String, String> table = HashBasedTable.create();
  private int rowIdx = 0;  // row 0 is reserved for the col headings

  // builder methods:

  /**
   * Starts a new row in the table.  This method should be invoked before adding any column data for a row.
   * Subsequent calls to {@link #addCol} will set the cell values in this new row.
   * <p>
   * <i>Note:</i> to insert an empty row, invoke this method followed by a single {@link #addCol(String, String)}
   * with an existing column name and an empty string value.
   * @throws IllegalStateException if no data has been written to the preceding row
   *   (see above comment about inserting an empty row)
   */
  public TablePrinter newRow() {
    // verify that the current row has some data (see above comment about inserting an empty row)
    checkState(rowIdx == 0 || table.row(rowIdx).size() > 0,
        "Row %s contains no data (to insert an empty row, invoke addCol with an empty value)", rowIdx);
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
    checkState(rowIdx > 0, "Must invoke newRow() before writing any column data");
    table.put(0, name, name);  // insert colName into top row (for col headings)
    table.put(rowIdx, name, value);
    return this;
  }

  // TODO(5/24/2025): allow grouping columns (to be printed as a super-heading above several columns), s.t. groups can have duplicated col names

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
   * Adds a formatted column value for the {@linkplain #newRow() current row}.
   * <p><b>Note:</b> this method is not GWT-compatible.
   * @param name the column name
   * @param format {@linkplain String#format format string} for the value
   * @param value the cell value (will be converted with {@link String#format(String, Object...)})
   * @throws IllegalStateException if the {@link #newRow()} hasn't been invoked yet
   */
  @GwtIncompatible("String.format")
  public TablePrinter addCol(String name, String format, Object value) {
    return addCol(name, String.format(format, value));
    /* TODO: maybe create a GWT-compatible version that uses a Function<Object, String> or a Renderer instead of String.format
         - replace format parameter with a method to set format for the whole column (similar to setColAlignment)
    */
  }

  /**
   * Sets the text alignment for all cells in the specified column.
   *
   * @param colName the column name
   * @param alignment the alignment to use for the named column,
   *   or {@code null} to remove the current setting for this column (i.e. revert to {@link #defaultTextAlignment})
   */
  public TablePrinter setColAlignment(String colName, TextAlignment alignment) {
    if (alignment != null)
      colTextAlignments.put(colName, alignment);
    else
      colTextAlignments.remove(colName);  // o/w storing a null value would break Map.getOrDefault
    return this;
  }

  public TextAlignment getDefaultTextAlignment() {
    return defaultTextAlignment;
  }

  /**
   * Changes the default text alignment for all cells.
   * This can be overridden on a per-column basis via {@link #setColAlignment(String, TextAlignment)}.
   * @see #DEFAULT_TEXT_ALIGNMENT
   */
  public TablePrinter setDefaultTextAlignment(@Nonnull TextAlignment textAlignment) {
    this.defaultTextAlignment = requireNonNull(textAlignment, "defaultTextAlignment");
    return this;
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
   * Prints the table to the given stream, using the {@linkplain OutputType#GRAPHIC default format}.
   */
  public void printTable(PrintStream out) {
    printTable(out, OutputType.GRAPHIC);
  }

  /**
   * Prints the table to {@link System#out} in the specified format.
 * @param outputType the output format
   */
  public void printTable(OutputType outputType) {
    printTable(System.out, outputType);
  }

  /**
   * Prints the table to the given stream in the specified format.
   * @param outputType the output format
   */
  public void printTable(PrintStream out, OutputType outputType) {
    Printer printer = outputType == OutputType.CSV
        ? new CsvPrinter(out, table, colTextAlignments, defaultTextAlignment)
        : new Printer(out, table, colTextAlignments, defaultTextAlignment);
    printer.printTable();
  }

  /**
   * Prints the table to the given logger in the {@linkplain OutputType#GRAPHIC default format}.
   */
  public void printTableToLogger(Logger logger) {
    printTableToLogger(logger, OutputType.GRAPHIC);
  }

  /**
   * Prints the table to the given logger in the specified format.
   * @param outputType the output format
   */
  public void printTableToLogger(Logger logger, OutputType outputType) {
    logger.info("\n" // start logger message on a new line
        + printTableToString(outputType));
  }

  /**
   * Prints the table to a string in the {@linkplain OutputType#GRAPHIC default format}.
   */
  public String printTableToString() {
    return printTableToString(OutputType.GRAPHIC);
  }

  /**
   * Prints the table to a string in the specified format.
   * @param outputType the output format
   */
  public String printTableToString(OutputType outputType) {
    StringPrintStream out = new StringPrintStream();
    printTable(out, outputType);
    return out.toString();
  }

  public enum OutputType {
    GRAPHIC, CSV;
  }

  public enum TextAlignment implements AlignmentFunction {
    LEFT(StringUtils::justifyLeft),
    RIGHT(StringUtils::justifyRight),
    CENTER(StringUtils::justifyCenter);

    private transient final AlignmentFunction aligner;

    TextAlignment(AlignmentFunction aligner) {
      this.aligner = aligner;
    }

    public String apply(String str, int width) {
      return aligner.apply(str, width);
    }
  }

  @FunctionalInterface
  public interface AlignmentFunction {
    String apply(String str, int width);
  }


  /* NOTE:
      the printing code is based on FixedWidthPrinter from solutions.trsoftware.commons.server.memquery.output
      TODO: can create subclasses for other formats (e.g. CsvPrinter, HtmlTablePrinter, etc.);
        see solutions.trsoftware.commons.server.memquery.output.ResultSetPrinter
   */
  static class Printer {
    // TODO: can create subclasses for other formats (e.g. CsvPrinter, HtmlTablePrinter, etc.); see solutions.trsoftware.commons.server.memquery.output.ResultSetPrinter
    private boolean bordersEnabled = true;
    protected final PrintStream out;

    protected final Table<Integer, String, String> table;

    protected final String[] colNames;
    private final int[] maxColWidths;
    protected final int nRows;
    protected final int nCols;

    private Map<String, TextAlignment> colAlignments;
    /**
     * The default text alignment for columns that don't have explicit alignments specified in {@link #colTextAlignments}
     * @see #setColAlignment(String, TextAlignment)
     * @see #colTextAlignments
     */
    private final TextAlignment defaultAlignment;

    int rowIdx = 0;

    public Printer(PrintStream out, Table<Integer, String, String> table, Map<String, TextAlignment> colTextAlignments, TextAlignment defaultTextAlignment) {
      this.out = out;
      this.table = table;
      this.colAlignments = colTextAlignments;
      this.defaultAlignment = defaultTextAlignment;

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
          String value = firstNonNull(table.get(i, colNames[j]), "");
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
      out.print(justifyText(value, col));
      if (isBordersEnabled() && isLastCol(col))
        out.print(getVBorder());
    }

    protected String justifyText(String value, int col) {
      TextAlignment alignment = colAlignments.getOrDefault(colNames[col], defaultAlignment);
      return alignment.apply(value, getColWidth(col));
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


  static class CsvPrinter extends Printer {

    private CSVWriter csvWriter;

    public CsvPrinter(PrintStream out, Table<Integer, String, String> table, Map<String, TextAlignment> colTextAlignments, TextAlignment defaultTextAlignment) {
      super(out, table, colTextAlignments, defaultTextAlignment);
      csvWriter = new CSVWriter(null); // since we're only using the writeNextElement method of CSVWriter, we can just pass a null Writer
    }

    public void printTable() {
      if (table.isEmpty()) {
        out.println("<Empty table>");
        return;
      }
      for (int i = 0; i < nRows; i++) {
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < nCols; j++) {
          String value = firstNonNull(table.get(i, colNames[j]), "");
          csvWriter.writeNextElement(sb, value, j == 0);
        }
        out.println(sb);
      }
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
  @SuppressWarnings("NonJREEmulationClassesInClientCode")
  @GwtIncompatible("printf")
  public static void printMenu(PrintStream out, String heading, List<String> lines) {
    // if the heading is not already padded with whitespace on both ends, do so now
    if (!heading.matches("\\s+.*?\\s+"))
      heading = pad(heading, 1);
    // now we want to wrap the heading with h-border symbols, such that it becomes at least the same length as the longest row in the body
    int maxLineLength = Math.max(heading.length()+2, lines.stream().mapToInt(String::length).max().orElse(0) + 2);
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
