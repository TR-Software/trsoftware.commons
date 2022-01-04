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

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.ui.Widget;

/**
 * Represents a native {@code compositionupdate} event.
 * <p>
 * Compatible with {@link Widget#addDomHandler(EventHandler, Type)}.
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Element/compositionupdate_event">MDN Reference</a>
 * @see <a href="https://w3c.github.io/uievents/#events-compositionevents">W3C Spec</a>
 */
public class CompositionUpdateEvent extends CompositionEvent<CompositionUpdateEvent.Handler> {

  public interface Handler extends EventHandler {
    void onCompositionUpdate(CompositionUpdateEvent event);
  }

  private static final Type<Handler> TYPE = new Type<>("compositionupdate", new CompositionUpdateEvent());

  public static Type<Handler> getType() {
    return TYPE;
  }

  /**
   * Protected constructor, use
   * {@link DomEvent#fireNativeEvent(NativeEvent, HasHandlers)} to fire {@code compositionupdate} events.
   */
  protected CompositionUpdateEvent() {
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onCompositionUpdate(this);
  }

}
