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

import solutions.trsoftware.commons.server.io.csv.CSVWriter;
import solutions.trsoftware.commons.server.memquery.ResultSet;

import java.io.PrintStream;
import java.io.StringWriter;

/** Generates CSV output for the result set */
public class CsvPrinter extends ResultSetPrinter<CsvPrinter> {

  public CsvPrinter() {
  }

  @Override
  public void print(ResultSet rs, PrintStream out) {
    new Impl(rs, out).print();
  }

  protected class Impl extends PrinterImpl {

    private CSVWriter csvWriter;

    protected Impl(ResultSet resultSet, PrintStream out) {
      super(resultSet, out);
      csvWriter = new CSVWriter(new StringWriter()); // since we're only using the writeNextElement method of CSVWriter, we can just pass an empty StringWriter
    }

    @Override
    protected void printCell(String value, int col) {
      StringBuilder sb = new StringBuilder();
      csvWriter.writeNextElement(sb, value, isFirstCol(col));
      out.print(sb.toString());
    }
  }
}
