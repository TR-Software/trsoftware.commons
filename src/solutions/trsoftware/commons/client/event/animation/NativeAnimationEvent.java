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

package solutions.trsoftware.commons.client.event.animation;

import com.google.gwt.dom.client.NativeEvent;

/**
 * Overlay for the native <a href="https://developer.mozilla.org/en-US/docs/Web/API/AnimationEvent">
 *   {@code AnimationEvent}</a> interface.
 */
public class NativeAnimationEvent extends NativeEvent {

  protected NativeAnimationEvent() {
  }

  /**
   * @return the value of the {@code animation-name} CSS property that generated the animation.
   */
  public final native String getAnimationName() /*-{
    return this.animationName;
  }-*/;

  /**
   * A float giving the amount of time the animation has been running, in seconds, when this event fired,
   * excluding any time the animation was paused.
   * <p>
   * For an {@code animationstart} event, {@code elapsedTime} is {@code 0.0} unless there was a negative
   * value for {@code animation-delay}, in which case the event will be fired with {@code elapsedTime}
   * containing {@code (-1 * delay)}.
   * @return
   */
  public final native double getElapsedTime() /*-{
    return this.elapsedTime;
  }-*/;

  /**
   * A string, starting with {@code "::"}, containing the name of the pseudo-element the animation runs on.
   * If the animation doesn't run on a pseudo-element but on the element, an empty string.
   */
  public final native String getPseudoElement() /*-{
    return this.pseudoElement;
  }-*/;
  
}
