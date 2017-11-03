package solutions.trsoftware.commons.server.memquery.output;

import solutions.trsoftware.commons.client.util.StringUtils;
import solutions.trsoftware.commons.server.memquery.ResultSet;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;

/** Generates HTML table code containing the result set */
public class HtmlTablePrinter extends ResultSetPrinter<HtmlTablePrinter> {

  /**
   * Maps col number to a custom value of its style attribute (e.g. {@code "width: 40%;"}).
   * The values will be used to add a {@code style} attribute to each header cell ({@code <th>} ) corresponding
   * to that column number.
   */
  private Map<Integer, String> colStyles = new LinkedHashMap<Integer, String>();

  /**
   * This will be the value of the {@code style} attribute for the {@code <table>} element.
   */
  private String tableStyle;

  public HtmlTablePrinter() {
  }

  public HtmlTablePrinter setColStyle(int col, String style) {
    colStyles.put(col, style);
    return this; // for method chaining
  }

  public HtmlTablePrinter setTableStyle(String tableStyle) {
    this.tableStyle = tableStyle;
    return this; // for method chaining
  }

  @Override
  public void print(ResultSet rs, PrintStream out) {
    new Impl(rs, out).print();
  }

  protected class Impl extends PrinterImpl {

    protected Impl(ResultSet resultSet, PrintStream out) {
      super(resultSet, out);
    }

    @Override
    protected void beginTable() {
      out.printf("<table%s>%n",
          tableStyle != null ?
              String.format(" style=\"%s\"", tableStyle)
              : ""
      );
    }

    @Override
    protected void endTable() {
      out.println("</table>");
    }

    @Override
    protected void beginRow(int row) {
      indent(1);
      out.println("<tr>");
    }

    @Override
    protected void endRow(int row) {
      indent(1);
      out.println("</tr>");
    }

    @Override
    protected void printHeaderCell(String value, int col) {
      indent(2);
      out.printf("<th%s>%s</th>%n",
          colStyles.containsKey(col) ?
              String.format(" style=\"%s\"", colStyles.get(col))
              : "",
          value);
    }

    @Override
    protected void printCell(String value, int col) {
      indent(2);
      out.printf("<td>%s</td>%n", value);
    }

    private void indent(int level) {
      out.print(StringUtils.repeat(' ', level*2));
    }
  }
}
