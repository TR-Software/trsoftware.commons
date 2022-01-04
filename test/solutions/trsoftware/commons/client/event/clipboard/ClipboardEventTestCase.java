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

package solutions.trsoftware.commons.client.event.clipboard;

import com.google.gwt.dom.client.DataTransfer;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.user.client.ui.Widget;
import solutions.trsoftware.commons.client.event.DomEventTestCase;
import solutions.trsoftware.commons.client.jso.JsObject;
import solutions.trsoftware.commons.client.jso.JsStringArray;

import static solutions.trsoftware.commons.client.event.DomEventTestCase.*;

/**
 * @author Alex
 * @since 12/11/2021
 */
public abstract class ClipboardEventTestCase<E extends ClipboardEvent<H>, H extends EventHandler>
    extends DomEventTestCase<E, H> {

  /**
   * Create an empty native {@code ClipboardEvent} of the appropriate type,
   * to be populated by {@link #createNativeEvent(Widget)}
   */
  protected abstract NativeEvent createNativeEvent();

  @Override
  protected NativeEvent createNativeEvent(Widget target) {
    return createNativeEvent().<JsObject>cast()
        .set("clipboardData", createDataTransfer(createClipboardDataItems()))
        .cast();
  }

  @Override
  protected void verifyReceivedEvent(E event) {
    DataTransfer clipboardData = event.getClipboardData();
    assertNotNull(clipboardData);
    JsObject expectedData = createClipboardDataItems();
    // TODO: extract static method to DomEventTestCase:
    String[] dataFormats = expectedData.keys().toJavaArray();
    for (String format : dataFormats) {
      assertEquals(expectedData.getString(format), clipboardData.getData(format));
    }
  }

  /**
   * Should be overridden if the subclass event type provides
   * access to the clipboard contents (this is only true for {@link PasteEvent paste} events).
   * Otherwise, returns an empty object.
   */
  protected JsObject createClipboardDataItems() {
    return JsObject.create();
  }

}
