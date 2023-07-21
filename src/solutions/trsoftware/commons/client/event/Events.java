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

package solutions.trsoftware.commons.client.event;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.web.bindery.event.shared.EventBus;
import solutions.trsoftware.commons.shared.util.StringUtils;

import static solutions.trsoftware.commons.shared.util.StringUtils.methodCallToString;

/**
 * Event-handling utils.
 *
 * @author Alex, 10/8/2015
 */
public abstract class Events {

  /** Singleton global event bus to be used across the application. */
  public static final EventBus BUS = GWT.create(EventBus.class);

  private Events() {
  }

  /**
   * Stops event propagation (bubbling) and prevents browser from executing its default action for this event.
   *
   * @see <a href="http://www.quirksmode.org/js/events_order.html">Browser event model explaination</a>
   * @see <a href="http://stackoverflow.com/questions/5963669/whats-the-difference-between-event-stoppropagation-and-event-preventdefault">stopPropagation() vs. preventDefault()</a>
   * @see MenuBar#eatEvent(Event)
   * */
  public static void eatEvent(NativeEvent event) {
    event.stopPropagation();
    event.preventDefault();
  }

  /**
   * Simulates a mouse click on the given element.
   * 
   * @see elemental.dom.Element#click()
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/click">HTMLElement.click() on MDN</a>
   */
  public static native void click(Element element) /*-{
    element.click();
  }-*/;


  /** Debugging method */
  public static String toDebugString(MouseEvent<?> event) {
    return event.toDebugString() + "[" + StringUtils.join(",",
        methodCallToString("coords", event.getX(), event.getY()),
        methodCallToString("screen=", event.getScreenX(), event.getScreenY()),
        methodCallToString("client=", event.getClientX(), event.getClientY())
    ) + "]";
  }

}
