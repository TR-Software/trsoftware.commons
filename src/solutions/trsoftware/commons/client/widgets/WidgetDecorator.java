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

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.Widget;

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
  public static <T extends Widget> T setCssClassNames(T widget,
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
  public static <T extends Widget> T addCssClassNames(T widget, String... styleNames) {
    for (String styleName : styleNames)
      widget.addStyleName(styleName);
    return widget;
  }

  /**
   * Adds dependent styles to the given widget.
   * @return the widget itself to allow method chaining.
   */
  public static <T extends Widget> T addCssClassDependentName(T widget, String dependentStyleName) {
    widget.addStyleDependentName(dependentStyleName);
    return widget;
  }


  /**
   * Adds a title (help text) to the given widget.
   * @return the widget itself to allow method chaining.
   * TODO: can add a title property to WidgetStyle and get rid of this method instead
   */
  public static <T extends Widget> T setTitle(T widget, String title) {
    widget.setTitle(title);
    return widget;
  }


  /**
   * Sets a style property directly on a widget's host element.
   * @param propertyName WARNING: dashes in CSS property names are replaced with
   * camelCase in DOM style manipulation (e.g. background-color becomes backgroundColor).
   * You must use camel case in the value of this argument.
   * @return the widget itself to allow method chaining.
   */
  public static <T extends Widget> T setStyleProperty(T widget, String propertyName, String propertyValue) {
    setStyleProperty(widget.getElement(), propertyName, propertyValue);
    return widget;
  }

  /**
   * Sets a style property directly on a the given element.
   * @param propertyName WARNING: dashes in CSS property names are replaced with
   * camelCase in DOM style manipulation (e.g. background-color becomes backgroundColor).
   * You must use camel case in the value of this argument.
   * @return the widget itself to allow method chaining.
   */
  public static void setStyleProperty(Element element, String propertyName, String propertyValue) {
    // TODO: can do the conversion here silently
    // TODO: this error check will not be needed once http://code.google.com/p/google-web-toolkit/issues/detail?id=2667 becomes available
    if (propertyName.contains("-"))
      throw new IllegalArgumentException("style.setProperty name must be camel case (e.g. backgroundColor, not background-color).");
    element.getStyle().setProperty(propertyName, propertyValue);
  }

  public static String getStyleProperty(Element element, String propertyName) {
    // TODO: can do the conversion here silently
    // TODO: this error check will not be needed once http://code.google.com/p/google-web-toolkit/issues/detail?id=2667 becomes available
    if (propertyName.contains("-"))
      throw new IllegalArgumentException("style.setProperty name must be camel case (e.g. backgroundColor, not background-color).");
    return element.getStyle().getProperty(propertyName);
  }

  public static String getStyleProperty(Widget widget, String propertyName) {
    return getStyleProperty(widget.getElement(), propertyName);
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
  public static <T extends Widget> T addAttribute(T widget, String attrName, String attrValue) {
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
