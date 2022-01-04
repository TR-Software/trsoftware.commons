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
import com.google.gwt.dom.client.NativeEvent;
import solutions.trsoftware.commons.client.dom.JsStaticRange;

import javax.annotation.Nullable;

/**
 * Overlay for the native <a href="https://w3c.github.io/input-events/#interface-InputEvent">{@code InputEvent}</a> interface.
 * <p>
 * <i>Internet Explorer Note:</i> Although IE (9+) fires {@link InputEvent input} events, it doesn't
 * actually implement {@code InputEvent} spec, hence the native event object will not have any of the attributes
 * defined in that interface and all the methods defined in this class will return {@code null}
 *
 * @see InputEventBase
 */
public class NativeInputEvent extends NativeEvent {

  protected NativeInputEvent() {
  }

  /**
   * @see InputEventBase#getData()
   */
  public final native String getData() /*-{
    return this.data;
  }-*/;

  /**
   * @see InputEventBase#getInputType()
   */
  public final native String getInputType() /*-{
    return this.inputType;
  }-*/;

  /**
   * @return {@code true} if the input event occurs as part of a composition session, otherwise {@code false},
   * or {@code null} if the event doesn't have this property (e.g. Internet Explorer).
   *
   * @see InputEventBase#isComposing()
   */
  @Nullable
  public final native Boolean isComposing() /*-{
    return (typeof this.isComposing === "boolean") ? @java.lang.Boolean::valueOf(Z)(this.isComposing) : null;
  }-*/;

  /**
   * @see InputEventBase#getTargetRanges()
   */
  public final native JsArray<JsStaticRange> getTargetRanges() /*-{
    if (this.getTargetRanges)
      return this.getTargetRanges();
    return null;  // not supported by current browser
  }-*/;

}
