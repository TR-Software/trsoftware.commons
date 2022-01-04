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

package solutions.trsoftware.commons.client.event.composition;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.user.client.ui.Widget;
import solutions.trsoftware.commons.client.event.DomEventTestCase;
import solutions.trsoftware.commons.client.jso.JsObject;

/**
 * @author Alex
 * @since 12/22/2021
 */
public abstract class CompositionEventTestCase<E extends CompositionEvent<H>, H extends EventHandler>
    extends DomEventTestCase<E, H> {

  /**
   * Create an empty native {@code CompositionEvent} of the appropriate type,
   * to be populated by {@link #createNativeEvent(Widget)}
   */
  protected abstract NativeEvent createNativeEvent();

  /**
   * @return value for the {@code CompositionEvent.data} attribute
   */
  protected abstract String getData();

  @Override
  protected NativeEvent createNativeEvent(Widget target) {
    return JsObject.as(createNativeEvent())
        .set("data", getData())
        .cast();
  }

  @Override
  protected void verifyReceivedEvent(E event) {
    DomEvent.Type<H> expectedType = getType();
    NativeEvent nativeEvent = event.getNativeEvent();
    assertEquals(expectedType.getName(), nativeEvent.getType());
    assertSame(expectedType, event.getAssociatedType());
    assertEquals(getData(), event.getData());
  }
}
