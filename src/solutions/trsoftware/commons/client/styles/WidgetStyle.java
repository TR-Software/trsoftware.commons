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

package solutions.trsoftware.commons.client.styles;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Widget;
import solutions.trsoftware.commons.client.widgets.WidgetBuilder;
import solutions.trsoftware.commons.client.widgets.WidgetDecorator;
import solutions.trsoftware.commons.client.widgets.Widgets;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Implements a builder pattern for declaring a widget's inline style properties.
 * <p>
 * The {@link #apply(Widget)} method is used by the factory methods in {@link Widgets} or {@link WidgetBuilder}
 * to set the style properties after a widget is constructed.
 *
 * @author Alex
 */
public class WidgetStyle {
  private String styleName, width, height;
  /** Allows specifying individual {@link Style} properties as name-value pairs */
  private Map<String, String> styleProperties;
  /** Allows direct access to the {@link Style} object */
  private Consumer<Style> styleSetter;


  public WidgetStyle() {
  }

  public WidgetStyle(String styleName) {
    this.styleName = styleName;
  }

  /**
   * @param styleSetter to set inline styles directly on the widget's elements
   */
  public WidgetStyle(Consumer<Style> styleSetter) {
    this.styleSetter = styleSetter;
  }

  /**
   * Factory method that can be used as shorthand for the common use case of specifying only the primary
   * {@linkplain Widget#setStyleName(String) style name} of a widget.
   */
  public static WidgetStyle styleName(String styleName) {
    return new WidgetStyle(styleName);
  }

  /** Applies the style to the widget and returns it (for chaining) */
  public <T extends Widget> T apply(T widget) {
    if (width != null)
      widget.setWidth(width);
    if (height != null)
      widget.setHeight(height);
    if (styleName != null)
      widget.setStyleName(styleName);
    if (styleProperties != null || styleSetter != null) {
      Style style = widget.getElement().getStyle();
      if (styleProperties != null)
        styleProperties.forEach(style::setProperty);
      if (styleSetter != null)
        styleSetter.accept(style);
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

  /**
   * Will apply the given a {@linkplain Style#setProperty(String, String) style property} to the widget's
   * {@linkplain Widget#getElement() element}.
   * @see #withStyleSetter(Consumer)
   */
  public WidgetStyle setStyleProperty(String name, String value) {
    if (styleProperties == null) {
      styleProperties = new HashMap<>();
    }
    styleProperties.put(name, value);
    return this;
  }

  /**
   * Allows specifying a function that manipulates the {@link Style} object directly.
   * This function will be applied after setting all the individual name-value properties specified by
   * {@link #setStyleProperty(String, String)}.  Subsequent invocations of this method will overwrite this value.
   *
   * @since 10/7/2024
   * @see WidgetDecorator#applyInlineStyles(Widget, Consumer)
   */
  public WidgetStyle withStyleSetter(Consumer<Style> styleSetter) {
    this.styleSetter = styleSetter;
    return this;
  }
}
