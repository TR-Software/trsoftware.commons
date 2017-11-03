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

package solutions.trsoftware.commons.server.memquery.output;

import solutions.trsoftware.commons.server.memquery.MemQuery;
import solutions.trsoftware.commons.server.memquery.RelationSchema;
import solutions.trsoftware.commons.server.memquery.ResultSet;
import solutions.trsoftware.commons.server.memquery.Row;
import solutions.trsoftware.commons.server.memquery.util.Formatters;

import java.io.PrintStream;
import java.util.Iterator;

import static solutions.trsoftware.commons.client.util.StringUtils.capitalize;

/** Defines a result set printing configuration.  Instances may be reused to print multiple result sets. */
public abstract class ResultSetPrinter<T extends ResultSetPrinter> {

  private PrintStream out;
  private boolean headerRowEnabled = true;
  private boolean ordinalColEnabled = true;
  private String ordinalColName = "#";
  /** Whether to print the name of the query before printing out the results */
  private boolean preambleEnabled = false;

  protected ResultSetPrinter() {
  }

  /** Prints the given result set to the stream configured for this instance, or stdout if not specified. */
  public void print(ResultSet rs) {
    print(rs, getOutputStream());
  }

  /** Prints the given result set to the given stream */
  public abstract void print(ResultSet rs, PrintStream out);

  /** Subclasses may override to provide custom cell formatting logic */
  protected String formatValue(Object value, int colIndex) {
    if (value == null)
      return "null";
    if ((value instanceof Integer) && (Integer)value < 10000)  // don't print the thousands grouping char (',') if the value is an integer with 4 digits or less
      return value.toString();
    return Formatters.getFor(value.getClass()).format(value); // TODO: pre-compute these
  }

  // the following methods define the printing configuration

  public PrintStream getOutputStream() {
    return out != null ? out : System.out;
  }

  protected boolean isHeaderRowEnabled() {
    return headerRowEnabled;
  }

  protected boolean isOrdinalColEnabled() {
    return ordinalColEnabled;
  }

  public boolean isPreambleEnabled() {
    return preambleEnabled;
  }

  protected String getOrdinalColName() {
    return ordinalColName;
  }

  // setters for the printing configuration

  public T setOutputStream(PrintStream out) {
    this.out = out;
    return (T)this;
  }

  public T setHeaderRowEnabled(boolean headerRowEnabled) {
    this.headerRowEnabled = headerRowEnabled;
    return (T)this;
  }

  public T setOrdinalColEnabled(boolean ordinalColEnabled) {
    this.ordinalColEnabled = ordinalColEnabled;
    return (T)this;
  }

  public T setPreambleEnabled(boolean preambleEnabled) {
    this.preambleEnabled = preambleEnabled;
    return (T)this;
  }

  public T setOrdinalColName(String ordinalColName) {
    this.ordinalColName = ordinalColName;
    return (T)this;
  }

  /** Implements the printing logic for a specific ResultSet */
  protected abstract class PrinterImpl {

    protected final ResultSet resultSet;
    protected final PrintStream out;

    protected PrinterImpl(ResultSet resultSet, PrintStream out) {
      this.resultSet = resultSet;
      this.out = out;
    }

    // methods that may be overridden by subclasses

    protected void prepareForPrinting() {
    }

    protected void beginTable() {
    }

    protected void endTable() {
    }

    protected void beginRow(int row) {
    }

    protected void endRow(int row) {
      out.println();
    }

    /**
     * Print the given value of column col in the header row of the results table.
     *
     * @param col -1 denotes the ordinal column
     */
    protected void printHeaderCell(String value, int col) {
      printCell(value, col);
    }

    /**
     * Print the given value at position (row,col) in the results table
     *
     * @param col -1 denotes the ordinal column
     */
    protected abstract void printCell(String value, int col);

    // reference methods for subclasses

    /** @return true iff the given column ordinal represents the first column of the table */
    protected boolean isFirstCol(int col) {
      return isFirst(col, isOrdinalColEnabled());
    }

    /** @return true iff the given column ordinal represents the first row of the table */
    protected boolean isFirstRow(int row) {
      return isFirst(row, isHeaderRowEnabled());
    }

    private boolean isFirst(int index, boolean condition) {
      if (condition)
        return index == -1;
      return index == 0;
    }


    // the following method implements the actual printing logic

    /** Prints the encapsulated {@link ResultSet} with fixed-width columns */
    public final void print() {
      prepareForPrinting();
      if (isPreambleEnabled()) {
        // 1) Print the name of the result set
        MemQuery query = resultSet.getQuery();
        int limit = query.getLimit();
        if (limit != Integer.MAX_VALUE)
          out.printf("Top %d %s%n", limit, query.getDescription());
        else
          out.printf("%s%n", capitalize(query.getDescription()));
      }

      // 2) print the data
      beginTable();
      RelationSchema schema = resultSet.getSchema();
      int nCols = schema.size();
      // 2a) print the header row
      if (isHeaderRowEnabled()) {
        beginRow(-1);
        if (isOrdinalColEnabled())
          printHeaderCell(getOrdinalColName(), -1);
        for (int j = 0; j < nCols; j++) {
          printHeaderCell(schema.get(j).getName(), j);
        }
        endRow(-1);
      }
      // 2b) print the data rows
      Iterator<Row> resultSetIterator = resultSet.iterator();
      for (int i = 0; resultSetIterator.hasNext(); i++) {
        beginRow(i);
        Row row = resultSetIterator.next();
        if (isOrdinalColEnabled())
          printCell(Integer.toString(i + 1), -1);
        for (int j = 0; j < nCols; j++) {
          printCell(formatValue(row.getValue(j), j), j);
        }
        endRow(i);
      }
      endTable();
    }
  }

}
