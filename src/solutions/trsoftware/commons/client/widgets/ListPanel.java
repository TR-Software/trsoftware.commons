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

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.LIElement;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

/**
 * Panel that displays its children within {@code <li>} elements nested in a parent {@code <ol>} or {@code <ul>}
 * (determined by the {@link Type} arg passed to the constructor).
 *
 * @author Alex, 9/25/2017
 */
public class ListPanel extends ComplexPanel {

  private final Type listType;

  /**
   * HTML list type ({@code <ol>} or {@code <ul>})
   */
  public enum Type {
    /**
     * Specifies using the HTML element {@code <ul>}
     */
    UL,
    /**
     * Specifies using the HTML element {@code <ol>}
     */
    OL;
  }

  /**
   * @param listType determines whether to use {@code <ul>} or {@code <ol>} as the main element
   */
  public ListPanel(@Nonnull Type listType) {
    this.listType = requireNonNull(listType, "listType");
    switch (listType) {
      case UL:
        setElement(Document.get().createULElement());
        break;
      case OL:
        setElement(Document.get().createOLElement());
        break;
      default:
        // should never happen
        throw new IllegalArgumentException(listType.toString());
    }
  }

  public Type getListType() {
    return listType;
  }

  /**
   * Adds a new child widget to the panel.
   *
   * @param w the widget to be added
   */
  @Override
  public void add(Widget w) {
    LIElement li = Document.get().createLIElement();
    DOM.appendChild(getElement(), li);
    add(w, li);
  }

  @Override
  public boolean remove(Widget w) {
    /*
     * Get the LI to be removed before calling super.remove() because
     * super.remove() will detach the child widget's element from its parent.
     */
    com.google.gwt.dom.client.Element li = w.getElement().getParentElement();
    boolean removed = super.remove(w);  // physically detaches w from li and logically detaches it from the panel
    if (removed) {
      getElement().removeChild(li);
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

}
