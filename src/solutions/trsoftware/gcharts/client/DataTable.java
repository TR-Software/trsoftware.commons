/*
 * Copyright 2018 TR Software Inc.
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
 *
 */

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

  public native final void setCell(int rowIndex, int columnIndex, String value)/*-{
    this.setCell(rowIndex, columnIndex, value);
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
