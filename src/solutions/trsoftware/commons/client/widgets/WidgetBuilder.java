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

package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.user.client.ui.Widget;

/**
 * Uses the builder pattern to provide method chaining for setting properties on a {@link Widget}.
 * @author Alex, 3/6/2016
 */
public class WidgetBuilder<T extends Widget> {

  // TODO: try to replace some usages of WidgetDecorator and Widgets with this class, or perhaps move this functionality to WidgetDecorator; can also extract some duplicated code from CellPanelEntry and WidgetStyle

  private final T widget;

  public WidgetBuilder(T widget) {
    this.widget = widget;
  }

  /** Factory method to avoid explicitly specifying the type parameter value */
  public static <T extends Widget> WidgetBuilder<T> build(T widget) {
    return new WidgetBuilder<T>(widget);
  }

  /**
   * Call this at the end of the method chain.
   *
   * @return The widget passed to the constructor.
   */
  public T get() {
    return widget;
  }
  
  // provide chained versions of Widget's mutators whose return type is void 
  // (it doesn't make sense to include those whose return type isn't void):

  public WidgetBuilder<T> addStyleDependentName(String styleSuffix) {
    widget.addStyleDependentName(styleSuffix);
    return this;
  }

  public WidgetBuilder<T> addStyleName(String style) {
    widget.addStyleName(style);
    return this;
  }

  public WidgetBuilder<T> setHeight(String height) {
    widget.setHeight(height);
    return this;
  }

  public WidgetBuilder<T> setPixelSize(int width, int height) {
    widget.setPixelSize(width, height);
    return this;
  }

  public WidgetBuilder<T> setSize(String width, String height) {
    widget.setSize(width, height);
    return this;
  }

  public WidgetBuilder<T> setStyleDependentName(String styleSuffix, boolean add) {
    widget.setStyleDependentName(styleSuffix, add);
    return this;
  }

  public WidgetBuilder<T> setStyleName(String style, boolean add) {
    widget.setStyleName(style, add);
    return this;
  }

  public WidgetBuilder<T> setStyleName(String style) {
    widget.setStyleName(style);
    return this;
  }

  public WidgetBuilder<T> setStylePrimaryName(String style) {
    widget.setStylePrimaryName(style);
    return this;
  }

  public WidgetBuilder<T> setTitle(String title) {
    widget.setTitle(title);
    return this;
  }

  public WidgetBuilder<T> setVisible(boolean visible) {
    widget.setVisible(visible);
    return this;
  }

  public WidgetBuilder<T> setWidth(String width) {
    widget.setWidth(width);
    return this;
  }

}
