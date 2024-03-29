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

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Extends {@link WidgetBuilder} to provide method chaining for adding child widgets to a {@link Panel}.
 *
 * @see #add(Widget)
 * @see #add(IsWidget)
 * @since 8/7/2018
 * @author Alex
 */
public class PanelBuilder<P extends Panel> extends WidgetBuilder<P> {

  /**
   * @param delegate the {@link Panel} being constructed
   */
  public PanelBuilder(P delegate) {
    super(delegate);
  }

  public PanelBuilder<P> add(Widget child) {
    delegate.add(child);
    return this;
  }

  public PanelBuilder<P> add(IsWidget child) {
    delegate.add(child);
    return this;
  }

  /**
   * Convenience methods that adds the given widget only if the given condition is true.
   * <p>
   * Allows call to keep all the logic within a single call chain (thereby avoiding a separate {@code if} stmt)
   */
  public PanelBuilder<P> addIf(boolean condition, IsWidget child) {
    if (condition)
      delegate.add(child);
    return this;
  }

  // NOTE: we override all the methods defined by WidgetBuilder to change their return type to PanelBuilder (rather than WidgetBuilder)

  /*
    TODO: get rid of the methods overridden just for the sake of changing the return type;
    @see how they did it in com.google.gwt.dom.builder.shared.HtmlElementBuilderBase
  */

  @Override
  public PanelBuilder<P> addStyleDependentName(String styleSuffix) {
    super.addStyleDependentName(styleSuffix);
    return this;
  }

  @Override
  public PanelBuilder<P> addStyleName(String style) {
    super.addStyleName(style);
    return this;
  }

  @Override
  public PanelBuilder<P> setHeight(String height) {
    super.setHeight(height);
    return this;
  }

  @Override
  public PanelBuilder<P> setPixelSize(int width, int height) {
    super.setPixelSize(width, height);
    return this;
  }

  @Override
  public PanelBuilder<P> setSize(String width, String height) {
    super.setSize(width, height);
    return this;
  }

  @Override
  public PanelBuilder<P> setStyleDependentName(String styleSuffix, boolean add) {
    super.setStyleDependentName(styleSuffix, add);
    return this;
  }

  @Override
  public PanelBuilder<P> setStyleName(String style, boolean add) {
    super.setStyleName(style, add);
    return this;
  }

  @Override
  public PanelBuilder<P> setStyleName(String style) {
    super.setStyleName(style);
    return this;
  }

  @Override
  public PanelBuilder<P> setStylePrimaryName(String style) {
    super.setStylePrimaryName(style);
    return this;
  }

  @Override
  public PanelBuilder<P> setTitle(String title) {
    super.setTitle(title);
    return this;
  }

  @Override
  public PanelBuilder<P> setVisible(boolean visible) {
    super.setVisible(visible);
    return this;
  }

  @Override
  public PanelBuilder<P> setWidth(String width) {
    super.setWidth(width);
    return this;
  }
}
