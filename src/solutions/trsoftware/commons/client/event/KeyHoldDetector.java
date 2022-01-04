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

import com.google.gwt.core.client.Duration;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registers itself as a handler on the event preview stack in order to find out whether a key is currently being held down.
 * <p>
 * <strong>Caveat:</strong>
 * Since we only receive keyboard events that happen while the page is in focus, don't know about keyDown or keyUp
 * that happen while the page is out of focus, prior to the page regaining focus (e.g. while a native {@code window.alert} dialog is showing).
 * Therefore, this class is not 100% reliable.  Although most browsers (except IE8) repeat keyDown events every few
 * milliseconds while a key is being held, there is no way to know if a key was released while the page is out of focus,
 * that's why this class provides the {@link #timeoutMillis} option.
 * <p>
 * <em>Browser Compatibility Notes:</em>
 * Recent versions of Chrome, FF, and IE7/9/10 seem to repeat keyDown events with interval < 100ms while a key is held,
 * but IE8 never repeats them if the key was pressed while the page wasn't focused.
 * @see <a href="http://www.quirksmode.org/dom/events/keys.html">http://www.quirksmode.org/dom/events/keys.html</a>
 * @author Alex
 */
public class KeyHoldDetector implements Event.NativePreviewHandler {

  /** For each keyDown event, will contain the time of that event, by key code */
  private final Map<Integer, Double> keysHeld = new LinkedHashMap<Integer, Double>();
  /** Will be used to remove the preview registration */
  private HandlerRegistration registration;

  /**
   * If no additional keyDown events received for a key code within this time frame, that key will be considered released.
   * A value of 0 means that keys are never considered held and {@link Double#MAX_VALUE} means no timeout.
   */
  private double timeoutMillis = 100;

  public KeyHoldDetector() {
  }

  /** Start listening for keyDown/keyUp events */
  public void start() {
    if (!isStarted())
      registration = Event.addNativePreviewHandler(this);
  }

  /** Stop listening for keyDown/keyUp events */
  public void stop() {
    if (isStarted()) {
      registration.removeHandler();
      registration = null;
      keysHeld.clear();
    }
  }

  public boolean isStarted() {
    return registration != null;
  }

  public void onPreviewNativeEvent(Event.NativePreviewEvent event) {
    final NativeEvent ev = event.getNativeEvent();
    switch (event.getTypeInt()) {
      case Event.ONKEYDOWN:
        keysHeld.put(ev.getKeyCode(), Duration.currentTimeMillis());
        break;
      case Event.ONKEYUP:
        keysHeld.remove(ev.getKeyCode());
        break;
    }
  }

  private static double millisSince(double keyDownTime) {
    return Duration.currentTimeMillis() - keyDownTime;
  }

  private boolean isFresh(double keyDownTime) {
    return millisSince(keyDownTime) <= timeoutMillis;
  }

  /**
   * @return true iff the given key is currently being held down
   */
  public boolean isKeyHeld(int keyCode) {
    Double keyDownTime = keysHeld.get(keyCode);
    if (keyDownTime != null) {
      if (isFresh(keyDownTime))
        return true;
      else
        keysHeld.remove(keyCode);  // proactively remove the expired entry
    }
    return false;
  }

  /**
   * @return true iff any key is currently being held down
   */
  public boolean isAnyKeyHeld() {
    boolean ret = false;
    // we check every entry to be sure it's not expired
    final Iterator<Map.Entry<Integer, Double>> it = keysHeld.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<Integer, Double> entry = it.next();
      Double keyDownTime = entry.getValue();
      if (isFresh(keyDownTime))
        ret = true;
      else
        it.remove();  // proactively remove the expired entry
    }
    return ret;
  }

  /**
   * @return {@link #timeoutMillis}
   */
  public double getTimeoutMillis() {
    return timeoutMillis;
  }

  /**
   * @param timeoutMillis value for {@link #timeoutMillis}
   */
  public void setTimeoutMillis(double timeoutMillis) {
    this.timeoutMillis = timeoutMillis;
  }
}
