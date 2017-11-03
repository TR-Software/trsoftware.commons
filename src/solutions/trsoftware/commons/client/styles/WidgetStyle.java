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

package solutions.trsoftware.commons.client.styles;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Widget;
import solutions.trsoftware.commons.client.widgets.Widgets;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements a builder pattern for declaring a widget's style properties.
 * The apply method will be called by a factory method in {@link Widgets} or {@link solutions.trsoftware.commons.client.widgets.WidgetBuilder}
 * to set the style properties after the widget is constructed.
 *
 * @author Alex
 */
public class WidgetStyle {
  private String styleName, width, height;
  private Map<String,String> styleProperties;


  public WidgetStyle() {
  }

  public WidgetStyle(String styleName) {
    this.styleName = styleName;
  }


  /** Applies the style to the widget and returns it (for chaining) */
  public <T extends Widget> T apply(T widget) {
    if (width != null)
      widget.setWidth(width);
    if (height != null)
      widget.setHeight(height);
    if (styleName != null)
      widget.setStyleName(styleName);
    if (styleProperties != null) {
      Style style = widget.getElement().getStyle();
      for (String styleProp : styleProperties.keySet()) {
        style.setProperty(styleProp, styleProperties.get(styleProp));
      }
    }
    return widget;
  }

  /** Sets the CSS class attribute of the element */
  public WidgetStyle setStyleName(String styleName) {
    this.styleName = styleName;
    return this;
  }

  public WidgetStyle setHeight(String height) {
    this.height = height;
    return this;
  }

  public WidgetStyle setWidth(String width) {
    this.width = width;
    return this;
  }

  public WidgetStyle setDimensions(String width, String height) {
    this.width = width;
    this.height = height;
    return this;
  }

  /** Sets a CSS style property directly on the underlying element */
  public WidgetStyle setStyleProperty(String name, String value) {
    if (styleProperties == null) {
      styleProperties = new HashMap<String, String>();  // lazy init
    }
    styleProperties.put(name, value);
    return this;
  }
}
