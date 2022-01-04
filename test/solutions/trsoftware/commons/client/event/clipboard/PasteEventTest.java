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
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;
import solutions.trsoftware.commons.client.testutil.RunStyleInfo;
import solutions.trsoftware.commons.client.jso.JsObject;

/**
 * @author Alex
 * @since 12/11/2021
 */
public class PasteEventTest extends ClipboardEventTestCase<PasteEvent, PasteEvent.Handler> {

  @Override
  protected PasteEvent.Handler createHandler() {
    return this::handleEvent;
  }

  @Override
  protected DomEvent.Type<PasteEvent.Handler> getType() {
    return PasteEvent.getType();
  }

  @Override
  protected NativeEvent createNativeEvent() {
    return Document.get().createHtmlEvent("paste", true, true);
  }

  /**
   * Overriding this method to use {@link Widget#addBitlessDomHandler} instead of {@link Widget#addDomHandler}
   * when running under HtmlUnit, which doesn't seem to recognize certain event listeners added via the attributes
   * (e.g. {@code element.onpaste} instead of {@code element.addEventListener}).
   * <p>
   * The reason this fails only for our {@link PasteEvent paste} event is that GWT already has pre-defined event bits for
   * it ({@link Event#ONPASTE}), which forces it down the legacy {@link Widget#sinkEvents(int)} pathway.  As a result,
   * the listener is added via {@code element.onpaste} and isn't recognized by HtmlUnit.
   * This problem was mentioned in the HtmlUnit 2.47.0 Release Notes.
   * <p>
   * NOTE: an equivalent work-around for HtmlUnit would be to test this event using
   * {@link DomEvent#fireNativeEvent(NativeEvent, HasHandlers)} rather than {@link Element#dispatchEvent(NativeEvent)}.
   *
   * @see <a href="https://htmlunit.sourceforge.io/changes-report.html#:~:text=Properties%20oncopy%2C%20oncut%2C-,onpaste,-%2C%20classList%20and%20hasAttribute">
   *   HtmlUnit 2.47.0 Release Notes</a>
   */
  @Override
  protected void addDomHandler(Widget widget, PasteEvent.Handler handler) {
    // special case for HtmlUnit (see the javadoc comment above)
    if (RunStyleInfo.INSTANCE.isHtmlUnit())
      widget.addBitlessDomHandler(handler, getType());
    else
      super.addDomHandler(widget, handler);
  }

  @Override
  protected JsObject createClipboardDataItems() {
    return JsObject.create()
        .set("text/plain", "foobar")
        .set("text/html", "<b>foobar</b>");
  }
}