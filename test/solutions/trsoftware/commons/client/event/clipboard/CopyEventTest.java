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

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.DomEvent;

/**
 * @author Alex
 * @since 12/11/2021
 */
public class CopyEventTest extends ClipboardEventTestCase<CopyEvent, CopyEvent.Handler> {

  @Override
  protected CopyEvent.Handler createHandler() {
    return this::handleEvent;
  }

  @Override
  protected DomEvent.Type<CopyEvent.Handler> getType() {
    return CopyEvent.getType();
  }

  @Override
  protected NativeEvent createNativeEvent() {
    return Document.get().createHtmlEvent("copy", true, true);
  }


}