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

package solutions.trsoftware.commons.client.event.input;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayUtils;
import com.google.gwt.dom.client.*;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.user.client.ui.Widget;
import solutions.trsoftware.commons.client.dom.DomQuery;
import solutions.trsoftware.commons.client.dom.JsStaticRange;
import solutions.trsoftware.commons.client.event.DomEventTestCase;
import solutions.trsoftware.commons.client.jso.JsObject;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Alex
 * @since 12/11/2021
 */
public abstract class InputEventTestCase<E extends InputEventBase<H>, H extends EventHandler>
    extends DomEventTestCase<E, H> {

  /**
   * Create an empty native {@code InputEvent} of the appropriate type,
   * to be populated by {@link #createNativeEvent(Widget)}
   */
  protected abstract NativeEvent createNativeEvent();

  @Override
  protected NativeEvent createNativeEvent(Widget target) {
    if (target == txtInput) {
      /*
      Simulate an InputEvent(inputType="insertText") on <input type="text">: this contains a data attribute,
      but no dataTransfer and an empty array for getTargetRanges(); see spec: https://w3c.github.io/input-events/#overview
      */
      return createNativeEvent().<JsObject>cast()
          .set("inputType", "insertText")
          .set("data", "a")
          .set("isComposing", true)
          .cast();
    }
    else if (target == html) {
      /*
      Simulate an InputEvent(inputType="insertFromPaste") on a "Contenteditable" element: this contains a dataTransfer
      attribute, but no data and a non-empty array for getTargetRanges(); see spec: https://w3c.github.io/input-events/#overview
      */
      NativeEvent nativeEvent = createNativeEvent().<JsObject>cast()
          .set("inputType", "insertFromPaste")
          .set("isComposing", false)
          .set("dataTransfer", DomEventTestCase.createDataTransfer(JsObject.create()
              .set("text/plain", "foobar")
              .set("text/html", "<b>foobar</b>")))
          .cast();
      InputEventTestCase.setTargetRanges(nativeEvent, JsArrayUtils.readOnlyJsArray(new JsStaticRange[]{
          createStaticRange(html.getElement())
      }));
      return nativeEvent;
    } else {
      throw new IllegalArgumentException(target.toString());
    }
  }

  @Override
  protected void verifyReceivedEvent(E event) {
    Object source = event.getSource();
    EventTarget eventTarget = event.getNativeEvent().getEventTarget();
    if (source == txtInput) {
      assertEquals(txtInput.getElement(), Element.as(eventTarget));
      assertEquals("insertText", event.getInputType());
      assertEquals("a", event.getData());
      assertTrue(event.isComposing());
      assertNull(event.getDataTransfer());
      assertNull(event.getTargetRanges());
    }
    else if (source == html) {
      assertEquals(html.getElement(), Element.as(eventTarget));
      assertEquals("insertFromPaste", event.getInputType());
      assertNull(event.getData());
      assertFalse(event.isComposing());

      DataTransfer dataTransfer = event.getDataTransfer();
      assertEquals("foobar", dataTransfer.getData("text/plain"));
      assertEquals("<b>foobar</b>", dataTransfer.getData("text/html"));

      JsArray<JsStaticRange> targetRanges = event.getTargetRanges();
      assertEquals(1, targetRanges.length());
      JsStaticRange expectedRange = createStaticRange(html.getElement());
      JsStaticRange actualRange = targetRanges.get(0);
      assertEquals(expectedRange.startContainer(), actualRange.startContainer());
      assertEquals(expectedRange.startOffset(), actualRange.startOffset());
      assertEquals(expectedRange.endContainer(), actualRange.endContainer());
      assertEquals(expectedRange.endOffset(), actualRange.endOffset());
    }
    else {
      fail("Unknown source: " + source);
    }
  }

  /**
   * Creates an arbitrary range that spans the first 2 text nodes within the given element
   */
  private JsStaticRange createStaticRange(Element element) {
    List<Node> textNodes = DomQuery.walkNodeTree(element).filter(node -> node.getNodeType() == Node.TEXT_NODE).collect(Collectors.toList());
    assertTrue(textNodes.size() >= 2);
    Node startContainer = textNodes.get(0);
    Node endContainer = textNodes.get(1);
    return DomEventTestCase.createStaticRange(startContainer, startContainer.getNodeValue().length() / 2,
        endContainer, endContainer.getNodeValue().length() / 2);
  }

  public static native void setTargetRanges(NativeEvent inputEvent, JsArray<JsStaticRange> ranges) /*-{
    inputEvent.getTargetRanges = function () {
      return ranges;
    }
  }-*/;

}
