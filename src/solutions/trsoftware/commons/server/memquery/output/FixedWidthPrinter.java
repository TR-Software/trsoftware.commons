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

package solutions.trsoftware.commons.server.memquery.output;

import solutions.trsoftware.commons.server.memquery.RelationSchema;
import solutions.trsoftware.commons.server.memquery.ResultSet;
import solutions.trsoftware.commons.server.memquery.Row;

import java.io.PrintStream;
import java.util.Iterator;

import static solutions.trsoftware.commons.shared.util.StringUtils.repeat;

/** Uses printf to print the data as fixed-width columns of text */
public class FixedWidthPrinter extends ResultSetPrinter<FixedWidthPrinter> {

  private boolean bordersEnabled = false;
  /** The number of extra spaces to offset each column on the left */
  private int cellHorizontalPadding = 1;
  private String verticalBorderSeparator = "|";

  public FixedWidthPrinter() {
  }

  @Override
  public void print(ResultSet rs, PrintStream out) {
    new Impl(rs, out).print();
  }

  public boolean isBordersEnabled() {
    return bordersEnabled;
  }

  public FixedWidthPrinter setBordersEnabled(boolean bordersEnabled) {
    this.bordersEnabled = bordersEnabled;
    return this;
  }

  public int getCellHorizontalPadding() {
    return cellHorizontalPadding;
  }

  public FixedWidthPrinter setCellHorizontalPadding(int cellHorizontalPadding) {
    this.cellHorizontalPadding = cellHorizontalPadding;
    return this;
  }

  public String getVerticalBorderSeparator() {
    return verticalBorderSeparator;
  }

  public FixedWidthPrinter setVerticalBorderSeparator(String verticalBorderSeparator) {
    this.verticalBorderSeparator = verticalBorderSeparator;
    return this;
  }

  /**
   * <ul>
   * <li><b>row indices</b>: 0 for top row, 1 for middle, and 2 for last</li>
   * <li><b>col indices</b>: 0 for first col, 1 for middle, and 2 for last</li>
   * </ul>
   */
  public static final char[][] CORNER_CHARS = {
      new char[]{'\u2554', '\u2566', '\u2557'},
      new char[]{'\u2560', '\u256C', '\u2563'},
      new char[]{'\u255A', '\u2569', '\u255D'}
  };

  public static final char H_BORDER_CHAR = '\u2550';
  public static final char V_BORDER_CHAR = '\u2551';


  protected class Impl extends PrinterImpl {

    private int[] maxColWidths;
    private String[] colFormats;

    protected Impl(ResultSet resultSet, PrintStream out) {
      super(resultSet, out);
    }

    private int getColWidth(int col) {
      return maxColWidths[col] + cellHorizontalPadding;
    }

    private String getColFormat(int col) {
      if (isOrdinalColEnabled())
        return colFormats[col + 1];
      return colFormats[col];
    }

    @Override
    protected void beginRow(int row) {
      super.beginRow(row);
      if (isFirstRow(row))
        maybePrintHorizontalBorder(0);
    }

    @Override
    protected void endRow(int row) {
      super.endRow(row);
      if (isFirstRow(row))
        maybePrintHorizontalBorder(1);
    }

    @Override
    protected void endTable() {
      maybePrintHorizontalBorder(2);
      super.endTable();
    }

    @Override
    protected void printCell(String value, int col) {
      if (bordersEnabled && isFirstCol(col))
        out.print(getVBorder());
      out.printf(getColFormat(col), value);
    }


    /**
     * @param rowType 0 for top row, 1 for middle, and 2 for last
     */
    private void maybePrintHorizontalBorder(int rowType) {
      if (bordersEnabled) {
        out.print(CORNER_CHARS[rowType][0]);
        int lastCol = maxColWidths.length-1;
        for (int i = 0; i <= lastCol; i++) {
          out.print(repeat(H_BORDER_CHAR, getColWidth(i)+1));
          // colType: 0 for first col, 1 for middle, and 2 for last
          int colType = i == lastCol ? 2 : 1;
          out.print(CORNER_CHARS[rowType][colType]);
        }
        out.println();
      }
    }

    private String getVBorder() {
      return isBordersEnabled() ? Character.toString(V_BORDER_CHAR) : "";
    }


    @Override
    protected void prepareForPrinting() {
      // 1) calculate the max width of each column
      // to do so, we must iterate over the result set and get the formatted value of each table cell
      RelationSchema schema = resultSet.getSchema();
      int nCols = schema.size();
      int extraCols;
      if (isOrdinalColEnabled()) {
        maxColWidths = new int[nCols + 1];
        maxColWidths[0] = getOrdinalColName().length();
        extraCols = 1;
      }
      else {
        maxColWidths = new int[nCols];
        extraCols = 0;
      }
      if (isHeaderRowEnabled()) {
        // 1a) init maxColWidths to the width of each header row value
        for (int j = extraCols; j < maxColWidths.length; j++) {
          maxColWidths[j] = schema.get(j - extraCols).getName().length();
        }
      }
      // 1b) compute maxColWidths by iterating over the result set once
      Iterator<Row> resultSetIterator = resultSet.iterator();
      for (int i = 0; resultSetIterator.hasNext(); i++) {
        Row row = resultSetIterator.next();
        if (isOrdinalColEnabled())
          maxColWidths[0] = Integer.toString(i).length();
        for (int j = 0; j < nCols; j++) {
          int vLen = formatValue(row.getValue(j), j).length();
          int m = j + extraCols;
          if (vLen > maxColWidths[m])
            maxColWidths[m] = vLen;
        }
      }
      // 2) compute the format string for each column, adding 2 extra spaces to each for better readability
      colFormats = new String[maxColWidths.length];
      String vBorder = getVBorder();
      for (int j = 0; j < maxColWidths.length; j++) {
        String colFormat = String.format("%%%ds%s", getColWidth(j),
            String.format("%" + (getCellHorizontalPadding()+1) + "s", vBorder));
        colFormats[j] = colFormat;
      }
    }

  }
}
