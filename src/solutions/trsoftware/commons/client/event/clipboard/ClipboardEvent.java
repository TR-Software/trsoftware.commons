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
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.shared.EventHandler;
import solutions.trsoftware.commons.client.jso.JsObject;

/**
 * Base class for {@link CutEvent cut}, {@link CopyEvent copy}, and {@link PasteEvent paste} events.
 *
 * Provides an implementation of the native {@code ClipboardEvent} API.
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/ClipboardEvent">ClipboardEvent on MDN</a>
 * @see <a href="https://w3c.github.io/clipboard-apis/#clipboard-events-and-interfaces">W3C Spec</a>
 */
public abstract class ClipboardEvent<H extends EventHandler> extends DomEvent<H> {

  /**
   * Returns a {@link DataTransfer} object containing the data affected by the user-initiated cut, copy, or paste operation,
   * along with its MIME type.
   * <p>
   * NOTE: only {@link PasteEvent paste} events provide read access to the clipboard contents, but
   * {@link CutEvent cut} and {@link CopyEvent copy} events do allow modifying
   *
   *
   * @return the {@code clipboardData} property of this {@code ClipboardEvent}
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/ClipboardEvent/clipboardData">MDN Reference</a>
   */
  public DataTransfer getClipboardData() {
    return getNativeEvent().<JsObject>cast().getObject("clipboardData");
  }

}
