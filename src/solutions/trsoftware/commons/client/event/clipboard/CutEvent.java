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

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.ui.Widget;

/**
 * Represents a native {@code cut} event.
 * <p>
 * Compatible with {@link Widget#addDomHandler(EventHandler, DomEvent.Type)}.
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Element/cut_event">MDN Reference</a>
 */
public class CutEvent extends ClipboardEvent<CutEvent.Handler> {

  public interface Handler extends EventHandler {
    void onCut(CutEvent event);
  }

  private static final Type<Handler> TYPE = new Type<>("cut", new CutEvent());

  public static Type<Handler> getType() {
    return TYPE;
  }

  /**
   * Protected constructor, use
   * {@link DomEvent#fireNativeEvent(NativeEvent, HasHandlers)} to fire {@code cut} events.
   */
  protected CutEvent() {
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onCut(this);
  }

}
