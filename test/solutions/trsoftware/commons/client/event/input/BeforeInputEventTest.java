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

import com.google.gwt.dom.client.*;
import com.google.gwt.event.dom.client.DomEvent;

/**
 * @author Alex
 * @since 12/6/2021
 */
public class BeforeInputEventTest extends InputEventTestCase<BeforeInputEvent, BeforeInputEvent.Handler> {

  @Override
  protected BeforeInputEvent.Handler createHandler() {
    return this::handleEvent;
  }

  @Override
  protected DomEvent.Type<BeforeInputEvent.Handler> getType() {
    return BeforeInputEvent.getType();
  }

  @Override
  protected NativeEvent createNativeEvent() {
    return Document.get().createHtmlEvent("beforeinput", true, true);
  }

}
