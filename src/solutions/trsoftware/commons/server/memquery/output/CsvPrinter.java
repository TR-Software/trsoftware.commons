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
