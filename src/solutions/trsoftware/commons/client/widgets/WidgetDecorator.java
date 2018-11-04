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

package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.Widget;
import solutions.trsoftware.commons.shared.util.StringUtils;

/**
 * Provides static methods to apply various properties to widgets using method chaining,
 * so that large widget tree structures can be constructed within a single statement.
 *
 * Date: Jul 24, 2007
 * Time: 8:17:11 PM
 *
 * @author Alex
 */
public class WidgetDecorator {

  /**
   * Sets the primary and secondary styles (CSS class names) for the given widget.
   * @return the widget itself to allow method chaining.
   */
  public static <W extends Widget> W setCssClassNames(W widget,
                                                   String primaryStyleName,
                                                   String... secondaryStyleNames) {
    widget.setStyleName(primaryStyleName);
    for (String styleName : secondaryStyleNames)
      widget.addStyleName(styleName);
    return widget;
  }

  /**
   * Adds secondary styles (CSS class names) to the given widget.
   * @return the widget itself to allow method chaining.
   */
  public static <W extends Widget> W addCssClassNames(W widget, String... styleNames) {
    for (String styleName : styleNames)
      widget.addStyleName(styleName);
    return widget;
  }

  /**
   * Adds dependent styles to the given widget.
   * @return the widget itself to allow method chaining.
   */
  public static <W extends Widget> W addCssClassDependentName(W widget, String dependentStyleName) {
    widget.addStyleDependentName(dependentStyleName);
    return widget;
  }


  /**
   * Adds a title (help text) to the given widget.
   * @return the widget itself to allow method chaining.
   * TODO: can add a title property to WidgetStyle and get rid of this method instead
   */
  public static <W extends Widget> W setTitle(W widget, String title) {
    widget.setTitle(title);
    return widget;
  }


  /**
   * Sets a {@link Style} property of the given {@link Widget}'s {@link Element}.
   * @param propertyName the property name, will be camel-cased if needed
   * (e.g. a background color is {@code background-color} in CSS, but {@code backgroundColor} in the DOM API)
   */
  public static <W extends Widget> W setStyleProperty(W widget, String propertyName, String propertyValue) {
    setStyleProperty(widget.getElement(), propertyName, propertyValue);
    return widget;
  }

  /**
   * Sets a {@link Style} property of the given {@link Element}.
   * @param propertyName the property name, will be camel-cased if needed
   * (e.g. a background color is {@code background-color} in CSS, but {@code backgroundColor} in the DOM API)
   */
  public static void setStyleProperty(Element element, String propertyName, String propertyValue) {
    element.getStyle().setProperty(sanitizeStylePropertyName(propertyName), propertyValue);
  }

  /**
   * Looks up a {@link Style} property of the given {@link Element}.
   * @param propertyName the property name, will be camel-cased if needed
   * (e.g. a background color is {@code background-color} in CSS, but {@code backgroundColor} in the DOM API)
   * @return the result of {@link Style#getProperty(String)}
   */
  public static String getStyleProperty(Element element, String propertyName) {
    return element.getStyle().getProperty(sanitizeStylePropertyName(propertyName));
  }

  /**
   * Looks up a {@link Style} property of the given {@link Widget}'s {@link Element}.
   * @param propertyName the property name, will be camel-cased if needed
   * (e.g. a background color is {@code background-color} in CSS, but {@code backgroundColor} in the DOM API)
   * @return the result of {@link Style#getProperty(String)}
   */
  public static String getStyleProperty(Widget widget, String propertyName) {
    return getStyleProperty(widget.getElement(), propertyName);
  }

  private static String sanitizeStylePropertyName(String propertyName) {
    if (propertyName.contains("-"))
      return StringUtils.toCamelCase(propertyName, "-");
    return propertyName;
  }


  /**
   * Controlling the visibility of an element with a "visibility: hidden" style
   * preserves the element's size (shows a blank area where the element was
   * supposed to be), unlike "display: none".
   * @return the widget itself to allow method chaining.
   */
  public static <T extends Widget> T setVisibilityHidden(T widget, boolean hidden) {
    setStyleProperty(widget, "visibility", hidden ? "hidden" : "");
    return widget;
  }

  /**
   * Controlling the visibility of an element with a "visibility: hidden" style
   * preserves the element's size (shows a blank area where the element was
   * supposed to be), unlike "display: none".
   * @return the widget itself to allow method chaining.
   */
  public static boolean isVisibilityHidden(Widget widget) {
    return "hidden".equals(getStyleProperty(widget, "visibility"));
  }

  /** Adds a custom DOM attribute to the widget's underlying element */ 
  public static <W extends Widget> W addAttribute(W widget, String attrName, String attrValue) {
    widget.getElement().setAttribute(attrName, attrValue);
    return widget;
  }

  /**
   * Sets the custom attributes autocorrect="off" and autocapitalize="off"
   * that are supported by iOS devices
   * (see https://developer.apple.com/library/safari/#codinghowtos/Mobile/UserExperience/_index.html )
   */
  public static <T extends TextBoxBase> T disableAutoTextForIOS(T textBoxBase) {
    addAttribute(textBoxBase, "autocorrect", "off");
    return addAttribute(textBoxBase, "autocapitalize", "off");
  }
}
