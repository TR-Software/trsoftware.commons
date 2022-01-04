/*
 * Copyright 2022 TR Software Inc.
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

package solutions.trsoftware.commons.client.event;

import com.google.gwt.dom.client.DataTransfer;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import solutions.trsoftware.commons.client.CommonsGwtTestCase;
import solutions.trsoftware.commons.client.dom.JsStaticRange;
import solutions.trsoftware.commons.client.jso.JsObject;

/**
 * @author Alex
 * @since 11/27/2021
 */
public abstract class DomEventTestCase<E extends DomEvent<H>, H extends EventHandler> extends CommonsGwtTestCase {

  /**
   * {@code <input type="text">}
   */
  protected TextBox txtInput;
  /**
   * {@code <div contenteditable="true">}
   */
  protected HTML html;

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    txtInput = new TextBox();
    RootPanel.get().add(txtInput);
    html = new HTML("<b>Hello</b> world");
    html.getElement().setPropertyBoolean("contentEditable", true);
    RootPanel.get().add(html);

    H handler = createHandler();
    addDomHandler(txtInput, handler);
    addDomHandler(html, handler);
  }

  /**
   * Create an instance of the appropriate handler type.
   * This handler should simply call {@link #handleEvent(DomEvent)} with the received event object.
   */
  protected abstract H createHandler();

  protected abstract DomEvent.Type<H> getType();

  protected abstract NativeEvent createNativeEvent(Widget target);

  protected abstract void verifyReceivedEvent(E event);

  /**
   * Can override, if needed for HtmlUnit.
   */
  protected void addDomHandler(Widget widget, H handler) {
    widget.addDomHandler(handler, getType());
  }

  /**
   * Tests creating and handling an event on {@link #txtInput}, which is a {@code <input type="text">} element.
   */
  public void testDispatchToTextBox() throws Exception {
    delayTestFinish();
    createAndDispatchEvent(txtInput);
  }

  /**
   * Tests creating and handling an event on {@link #html}, which is a {@code <div contenteditable="true">} element.
   */
  public void testDispatchToHTML() throws Exception {
    delayTestFinish();
    createAndDispatchEvent(html);
  }

  protected final void handleEvent(E event) {
    log("Received " + event.toDebugString());
    verifyReceivedEvent(event);
    finishTest();
  }

  private void createAndDispatchEvent(Widget target) {
    NativeEvent nativeEvent = createNativeEvent(target);
    dispatchEventTo(target, nativeEvent);
  }

  protected void dispatchEventTo(Widget target, NativeEvent nativeEvent) {
    Element element = target.getElement();
    log("Dispatching native " + nativeEvent.getType() + " event to " + element);
    element.dispatchEvent(nativeEvent);
  }

  /**
   * Calls {@link #delayTestFinish(int)} with a 200ms timout.
   * Subclasses may override if more time needed for debugging.
   */
  protected void delayTestFinish() {
    delayTestFinish(200);
  }

  // utility methods for subclasses:

  public static native DataTransfer createDataTransfer(JsObject items) /*-{
    var dataTransfer;

    try {
      dataTransfer = new DataTransfer();
      for (var key in items) {
        if (items.hasOwnProperty(key)) {
          dataTransfer.setData(key, items[key])
        }
      }
    }
    catch (e) {
      // DataTransfer constructor not available in current browser
      console.warn(e);
      // emulate the DataTransfer type
      dataTransfer = {
        getData: function (format) {
          return items[format] || '';
        },
        setData: function (format, data) {
          items[format] = data;
        }
      };
    }
    return dataTransfer;
  }-*/;

  public static native JsStaticRange createStaticRange(Node startContainer, int startOffset, Node endContainer, int endOffset) /*-{
    var rangeData = {
      startContainer: startContainer,
      startOffset: startOffset,
      endContainer: endContainer,
      endOffset: endOffset
    }

    try {
      return new StaticRange(rangeData);
    }
    catch (e) {
      // StaticRange constructor not available in current browser
      console.warn(e);
      rangeData.collapsed = startContainer === endContainer && startOffset === endOffset;
      return rangeData;
    }
  }-*/;
}