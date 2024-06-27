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

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.*;

/**
 * Allows any {@link Element} to behave like a {@link FlowPanel}
 *
 * @author Alex, 3/24/2015
 */
public class GenericFlowPanel extends ComplexPanel implements InsertPanel.ForIsWidget {

  /**
   * Constructor exposed to allow any element to become a {@link GenericFlowPanel}.
   *
   * @param elem the element to use for this widget.
   */
  // TODO(4/14/2024): make sure that making this public doesn't break anything
  public GenericFlowPanel(Element elem) {
    setElement(elem);
  }

  /**
   * Creates an empty flow panel using a new {@code div} element
   */
  public GenericFlowPanel() {
    this(DivElement.TAG);
  }
  /**
   * Creates an empty flow panel with a custom tag.
   */
  public GenericFlowPanel(String tag) {
    setElement(Document.get().createElement(tag));
  }

  /**
   * Adds a new child widget to the panel.
   *
   * @param w the widget to be added
   */
  @Override
  public void add(Widget w) {
    add(w, getElement());
  }

  /**
   * TODO: should this method replicate the behavior of {@link FlowPanel#clear()}, which calls
   *  the package-private and semi-deprecated {@link #doLogicalClear()} method?
   */
  @Override
  public void clear() {
    super.clear();
  }

  @Override
  public void insert(IsWidget w, int beforeIndex) {
    insert(asWidgetOrNull(w), beforeIndex);
  }

  /**
   * Inserts a widget before the specified index.
   *
   * @param w the widget to be inserted
   * @param beforeIndex the index before which it will be inserted
   * @throws IndexOutOfBoundsException if <code>beforeIndex</code> is out of
   *           range
   */
  @Override
  public void insert(Widget w, int beforeIndex) {
    insert(w, getElement(), beforeIndex, true);
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
  public static GenericFlowPanel wrap(Element element) {
    // Assert that the element is attached.
    assert Document.get().getBody().isOrHasChild(element);

    GenericFlowPanel widget = new GenericFlowPanel(element);

    // Mark it attached and remember it for cleanup.
    widget.onAttach();
    RootPanel.detachOnWindowClose(widget);

    return widget;
  }

  /**
   * Shortcut for {@link #wrap(Element)}.
   *
   * @param elementId the id of the element to be wrapped
   */
  public static GenericFlowPanel wrap(String elementId) {
    return wrap(DOM.getElementById(elementId));
  }

}
