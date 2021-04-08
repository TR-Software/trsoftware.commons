/*
 * Copyright 2021 TR Software Inc.
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
 */

package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.user.client.ui.Widget;

/**
 * Uses the builder pattern to provide method chaining for setting properties on a {@link Widget}.
 * @author Alex, 3/6/2016
 */
public class WidgetBuilder<W extends Widget> {

  // TODO: try to replace some usages of WidgetDecorator and Widgets with this class, or perhaps move this functionality to WidgetDecorator; can also extract some duplicated code from CellPanelEntry and WidgetStyle

  /**
   * The widget being constructed
   */
  protected final W delegate;

  /**
   * @param delegate the {@link Widget} being constructed
   */
  public WidgetBuilder(W delegate) {
    this.delegate = delegate;
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
  public W get() {
    return delegate;
  }
  
  /*
    We provide chained versions of Widget's mutators whose return type is void
    (it doesn't make sense to include those whose return type isn't void):
  */

  public WidgetBuilder<W> addStyleDependentName(String styleSuffix) {
    delegate.addStyleDependentName(styleSuffix);
    return this;
  }

  public WidgetBuilder<W> addStyleName(String style) {
    delegate.addStyleName(style);
    return this;
  }

  public WidgetBuilder<W> setHeight(String height) {
    delegate.setHeight(height);
    return this;
  }

  public WidgetBuilder<W> setPixelSize(int width, int height) {
    delegate.setPixelSize(width, height);
    return this;
  }

  public WidgetBuilder<W> setSize(String width, String height) {
    delegate.setSize(width, height);
    return this;
  }

  public WidgetBuilder<W> setStyleDependentName(String styleSuffix, boolean add) {
    delegate.setStyleDependentName(styleSuffix, add);
    return this;
  }

  public WidgetBuilder<W> setStyleName(String style, boolean add) {
    delegate.setStyleName(style, add);
    return this;
  }

  public WidgetBuilder<W> setStyleName(String style) {
    delegate.setStyleName(style);
    return this;
  }

  public WidgetBuilder<W> setStylePrimaryName(String style) {
    delegate.setStylePrimaryName(style);
    return this;
  }

  public WidgetBuilder<W> setTitle(String title) {
    delegate.setTitle(title);
    return this;
  }

  public WidgetBuilder<W> setVisible(boolean visible) {
    delegate.setVisible(visible);
    return this;
  }

  public WidgetBuilder<W> setWidth(String width) {
    delegate.setWidth(width);
    return this;
  }

}
