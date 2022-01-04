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

import com.google.common.base.MoreObjects;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.DataTransfer;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.shared.EventHandler;
import solutions.trsoftware.commons.client.dom.JsStaticRange;

import javax.annotation.Nullable;

import static solutions.trsoftware.commons.shared.util.StringUtils.valueToString;

/**
 * Base class for {@link InputEvent input} and {@link BeforeInputEvent beforeinput} events.
 * Provides an implementation of the native {@code InputEvent} interface, which represents
 * an event notifying the user of editable content changes.
 * <p>
 * <i>Internet Explorer Note:</i> Although IE doesn't support {@link BeforeInputEvent beforeinput} events,
 * it does fire {@link InputEvent input} events (starting with version 9).  However, since it doesn't
 * actually implement the {@code InputEvent} spec, the native event object will not have any of the attributes
 * defined in this interface.
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/InputEvent">InputEvent on MDN</a>
 * @see <a href="https://w3c.github.io/input-events/#interface-InputEvent">InputEvent interface spec</a>
 * @see <a href="https://w3c.github.io/uievents/#events-inputevents">W3C spec for input events</a>
 */
public abstract class InputEventBase<H extends EventHandler> extends DomEvent<H> {

  /**
   * Returns a string with the inserted characters.
   * This may be an empty string if the change doesn't insert text (such as when deleting characters, for example),
   * or {@code null} if this attribute is not applicable for the current event
   * (see <a href="https://w3c.github.io/input-events/#interface-InputEvent">this table</a>).
   *
   * @return a string with the inserted characters, or {@code null} if not applicable for this event.
   * @see <a href="https://w3c.github.io/uievents/#dom-inputevent-data">InputEvent.data spec</a>
   * @see <a href="https://w3c.github.io/input-events/#interface-InputEvent">InputEvent.data attribute applicability</a>
   */
  public String getData() {
    return getNativeInputEvent().getData();
  }

  /**
   * Returns the type of change for editable content such as, for example, inserting, deleting, or formatting text.
   * See <a href="https://rawgit.com/w3c/input-events/v1/index.html#interface-InputEvent-Attributes">this spec</a>
   * for a complete list of input types.
   *
   *
   * @return a string that identifies the type of input associated with the event.
   * @see <a href="https://w3c.github.io/uievents/#dom-inputevent-inputtype">InputEvent.inputType spec</a>
   */
  public String getInputType() {
    return getNativeInputEvent().getInputType();
  }

  /**
   * Returns a boolean value indicating if this event is fired after {@code compositionstart}
   * and before {@code compositionend}.
   *
   * @return {@code true} if the input event occurs as part of a composition session, otherwise {@code false},
   * or {@code null} if the event doesn't have this property (e.g. Internet Explorer).
   *
   * @see <a href="https://w3c.github.io/uievents/#dom-inputevent-iscomposing">InputEvent.isComposing spec</a>
   */
  @Nullable
  public Boolean isComposing() {
    return getNativeInputEvent().isComposing();
  }

  /**
   * Returns a {@link DataTransfer} object containing information about richtext or plaintext data being added to or
   * removed from editable content, if there is relevant data.
   *
   * @return the {@code dataTransfer} property of this {@code InputEvent} or {@code null} if not applicable for this
   *     event (see the spec).
   * @see <a href="https://w3c.github.io/input-events/#dom-inputevent-datatransfer">InputEvent.dataTransfer spec</a>
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/InputEvent/dataTransfer">MDN Reference</a>
   * @see <a href="https://w3c.github.io/input-events/#interface-InputEvent">InputEvent.dataTransfer attribute applicability</a>
   */
  public DataTransfer getDataTransfer() {
    return getNativeInputEvent().getDataTransfer();
  }

  /**
   * Returns an array of static ranges that will be affected by a change to the DOM if the input event is not canceled.
   * This array could be empty, depending on the context
   * (see <a href="https://w3c.github.io/input-events/#interface-InputEvent">this table</a>).
   *
   * @return an array of <a href="https://developer.mozilla.org/en-US/docs/Web/API/StaticRange">{@code StaticRange}</a> objects, if applicable, or {@code null} if not supported by current browser
   * @see <a href="https://w3c.github.io/input-events/#dom-inputevent-gettargetranges">InputEvent.getTargetRanges() spec</a>
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/InputEvent/gettargetranges">MDN Reference</a>
   */
  public JsArray<JsStaticRange> getTargetRanges() {
    return getNativeInputEvent().getTargetRanges();
  }

  /**
   * Replaces {@link #getNativeEvent()} for this type of event.
   */
  public NativeInputEvent getNativeInputEvent() {
    return getNativeEvent().cast();
  }

  @Override
  public String toDebugString() {
    return MoreObjects.toStringHelper(this)
        .add("type", valueToString(getNativeEvent().getType()))
        .add("data", valueToString(getData()))
        .add("inputType", valueToString(getInputType()))
        .add("isComposing", isComposing())
        .toString();
  }

}
