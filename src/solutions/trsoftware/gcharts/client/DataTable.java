package solutions.trsoftware.gcharts.client;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * @author Alex, 5/6/2017
 */
public class DataTable extends JavaScriptObject {

  // Overlay types always have protected, zero-arg constructors, because the object must have been instantiated in javascript
  protected DataTable() { }

  public static native DataTable create()/*-{
    return new $wnd.google.visualization.DataTable()
  }-*/;

  public native final void addColumn(String type, String opt_label)/*-{
    this.addColumn(type, opt_label);
  }-*/;


  public native final void addRow()/*-{
    this.addRow();
  }-*/;

  public native final void setCell(int rowIndex, int columnIndex, double value, String formattedValue)/*-{
    this.setCell(rowIndex, columnIndex, value, formattedValue);
  }-*/;


  public static class Cell extends JavaScriptObject {

    protected Cell() {
    }

    public static native Cell create(Object value, String description)/*-{
      return {v: value, f: description}
    }-*/;

  }
}
