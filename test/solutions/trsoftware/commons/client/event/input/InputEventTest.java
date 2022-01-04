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

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;

/**
 * @author Alex
 * @since 11/27/2021
 */
public class InputEventTest extends InputEventTestCase<InputEvent, InputEvent.Handler> {

  @Override
  protected InputEvent.Handler createHandler() {
    return this::handleEvent;
  }

  @Override
  protected DomEvent.Type<InputEvent.Handler> getType() {
    return InputEvent.getType();
  }

  @Override
  protected NativeEvent createNativeEvent() {
    return Document.get().createInputEvent();
  }

  /**
   * Tests an {@link InputEvent input} with no attributes (which is the case in IE).
   */
  public void testEmptyEvent() throws Exception {
    TextArea textArea = new TextArea();
    RootPanel.get().add(textArea);
    delayTestFinish();
    textArea.addDomHandler(new InputEvent.Handler() {
      @Override
      public void onInput(InputEvent event) {
        log(event.toDebugString());
        assertEquals("input", event.getNativeInputEvent().getType());
        assertNull(event.getData());
        assertNull(event.getInputType());
        assertNull(event.isComposing());
        assertNull(event.getDataTransfer());
        assertNull(event.getTargetRanges());
        finishTest();
      }
    }, InputEvent.getType());
    dispatchEventTo(textArea, createNativeEvent());
  }

}