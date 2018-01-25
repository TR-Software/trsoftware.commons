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

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import solutions.trsoftware.commons.shared.util.mutable.MutableInteger;

/**
 * A panel that renders its children in a {@code <ol>} element.
 * @author Alex, 9/25/2017
 */
public class OrderedList extends ComplexPanel {

  /**
   * Creates an empty flow panel.
   */
  public OrderedList() {
    setElement(Document.get().createOLElement());
  }

  /**
   * Adds a new child widget to the panel.
   *
   * @param w the widget to be added
   */
  @Override
  public void add(Widget w) {
    Element li = Document.get().createLIElement().cast();
    DOM.appendChild(getElement(), li);
    add(w, li);
  }

  @Override
  public boolean remove(Widget w) {
    /*
     * Get the LI to be removed before calling super.remove() because
     * super.remove() will detach the child widget's element from its parent.
     */
    Element li = DOM.getParent(w.getElement());
    boolean removed = super.remove(w);
    if (removed) {
      DOM.removeChild(getElement(), li);
    }
    return removed;
  }

  //  @Override
//  public void clear() {
//    try {
//      doLogicalClear();
//    }
//    finally {
//      // Remove all existing child nodes.
//      Node child = getElement().getFirstChild();
//      while (child != null) {
//        getElement().removeChild(child);
//        child = getElement().getFirstChild();
//      }
//    }
//  }

//  public void insert(IsWidget w, int beforeIndex) {
//    insert(asWidgetOrNull(w), beforeIndex);
//  }
//
//  /**
//   * Inserts a widget before the specified index.
//   *
//   * @param w the widget to be inserted
//   * @param beforeIndex the index before which it will be inserted
//   * @throws IndexOutOfBoundsException if <code>beforeIndex</code> is out of range
//   */
//  public void insert(Widget w, int beforeIndex) {
//    LIElement liElement = Document.get().createLIElement();
//    liElement.appendChild(w.getElement());
//    insert(w, getElement(), beforeIndex, true);
//  }

  // TODO: temp
  public static Widget createTestWidget() {
    final MutableInteger next = new MutableInteger(1);
    final OrderedList ol = new OrderedList();
    return Widgets.flowPanel(
        ol,
        new Button("Add list item", new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            Button btnRemove = new Button("Remove");
            final FlowPanel item = Widgets.flowPanel(
                new InlineLabel("Item " + next.getAndIncrement()),
                btnRemove
            );
            btnRemove.addClickHandler(new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                ol.remove(item);
              }
            });
            ol.add(item);
          }
        }),
        new Button("Remove Index", new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            int idx = Integer.parseInt(Window.prompt("Enter item index", "1"));
            ol.remove(idx);
          }
        }),
        new Button("Clear", new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            ol.clear();
          }
        })
    );
  }
}
