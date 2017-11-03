/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.client.event;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;

/**
 * Some browsers periodically send "mousemove" events as long as the mouse is located over the element that's
 * listening for these events (or if it's {@link Event.NativePreviewEvent}, then anywhere withing the browser window),
 * even when the mouse hasn't actually moved since the last time this event was fired.
 * This is true with IE and Chrome on Windows (and maybe other platforms).
 *
 * To mitigate this behavior, you can use an instance of this class in the event handling routine.
 */
public class MouseMoveFilter {

  private int lastX, lastY;

  /**
   * @return true iff this is a "mousemove" event with the same coordinates as the last one passed to this method.
   */
  public boolean isDuplicate(Event event) {
    boolean ret = false;
    if (DOM.eventGetType(event) == Event.ONMOUSEMOVE) {
      int x = event.getClientX();
      int y = event.getClientY();
      // Some browsers periodically send mousemove events even when the mouse hasn't actually moved;
      // we use these variable to check whether it has actually moved to avoid doing extra work.
      if (x == lastX && y == lastY)
        ret = true;
      lastX = x;
      lastY = y;
    }
    return ret;
  }

}
