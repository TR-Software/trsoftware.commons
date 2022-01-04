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


/**
 * @author Alex
 * @since 12/22/2021
 */
public class CompositionStartEventTest extends CompositionEventTestCase<CompositionStartEvent, CompositionStartEvent.Handler> {

  @Override
  protected CompositionStartEvent.Handler createHandler() {
    return this::handleEvent;
  }

  @Override
  protected DomEvent.Type<CompositionStartEvent.Handler> getType() {
    return CompositionStartEvent.getType();
  }

  @Override
  protected NativeEvent createNativeEvent() {
    return Document.get().createHtmlEvent("compositionstart", true, true);
  }

  @Override
  protected String getData() {
    return "";  // compositionstart events generally don't have any data (b/c the IME session hasn't started yet)
  }
}