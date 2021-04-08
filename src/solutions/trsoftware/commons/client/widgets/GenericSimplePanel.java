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

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Allows any {@link Element} to become a {@link SimplePanel} (by exposing the {@link SimplePanel#SimplePanel(Element)}
 * constructor).
 *
 * @author Alex, 3/24/2015
 */
public class GenericSimplePanel extends SimplePanel {

  /**
   * Constructor exposed to allow any element to become a {@link GenericSimplePanel}.
   *
   * @param elem the element to use for this widget.
   */
  protected GenericSimplePanel(Element elem) {
    super(elem);
  }

  /**
   * Constructor exposed to allow any element to become a {@link GenericSimplePanel}.
   *
   * @param elem the element to use for this widget.
   * @param child the child widget to add to it
   */
  protected GenericSimplePanel(Element elem, Widget child) {
    this(elem);
    setWidget(child);
  }

  /**
   * Creates an instance that wraps an existing element.
   *
   * This element must already be attached to the document. If the element is
   * removed from the document, you must call {@link RootPanel#detachNow(Widget)}.
   *
   * This method is implemented the same way as other {@code wrap} methods provided by GWT, such as
   * {@link HTML#wrap(Element)}.
   *
   * @param element the element to be wrapped
   */
  public static GenericSimplePanel wrap(Element element) {
    // Assert that the element is attached.
    assert Document.get().getBody().isOrHasChild(element);

    GenericSimplePanel widget = new GenericSimplePanel(element);

    // Mark it attached and remember it for cleanup.
    widget.onAttach();
    RootPanel.detachOnWindowClose(widget);

    return widget;
  }

  /**
   * Shortcut for {@link #wrap(Element)}.
   * @param elementId the id of the element to be wrapped
   */
  public static GenericSimplePanel wrap(String elementId) {
    return wrap(DOM.getElementById(elementId));
  }

}
