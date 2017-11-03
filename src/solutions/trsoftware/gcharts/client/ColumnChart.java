package solutions.trsoftware.gcharts.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;

/**
 * @author Alex, 5/6/2017
 */
public class ColumnChart extends Chart {

  // Overlay types always have protected, zero-arg constructors, because the object must have been instantiated in javascript
  protected ColumnChart() { }

  public static native ColumnChart create(Element container)/*-{
    return new $wnd.google.visualization.ColumnChart(container);
  }-*/;

  public native final void draw(DataTable data)/*-{
    this.draw(data);
  }-*/;

  public native final void draw(DataTable data, JavaScriptObject options)/*-{
    this.draw(data, options);
  }-*/;

}
