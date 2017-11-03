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
