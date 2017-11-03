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

package solutions.trsoftware.gcharts.client;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * @author Alex, 5/8/2017
 */
public abstract class Chart extends JavaScriptObject {

  protected Chart() {
  }

  public interface SelectionHandler {
    void onSelection(Selection selection);
  }

  public static class Selection extends JavaScriptObject {

    protected Selection() {
    }

    /**
     * @return the value of the row field of the selection object, or -1 if row is null.
     */
    public final native int getRow() /*-{
      var val = this.row;
      if (val == null)
        return -1;
      return val;
    }-*/;

    /**
     * @return the value of the column field of the selection object, or -1 if column is null.
     */
    public final native int getColumn() /*-{
      var val = this.column;
      if (val == null)
        return -1;
      return val;

    }-*/;
  }

  /**
   * @see <a href="https://developers.google.com/chart/interactive/docs/events#the-select-event">select event in Google Charts docs</a>
   */
  public final native void addSelectionHandler(SelectionHandler handler) /*-{
    var chart = this;
    var handlerFcn = $entry(function () {
      // NOTE: chart.getSelection() returns an array, with each element representing a selected item
      // the size of this array is 1 unless it's a multi-selection (which not all charts allow)
      // for simplicity here we just use the 1st element in the selection, which should suffice for most purposes
      var selection = chart.getSelection()[0];
      handler.@solutions.trsoftware.gcharts.client.Chart.SelectionHandler::onSelection(Lsolutions/trsoftware/gcharts/client/Chart$Selection;)(selection);
    });
    $wnd.google.visualization.events.addListener(chart, 'select', handlerFcn);
  }-*/;
}
