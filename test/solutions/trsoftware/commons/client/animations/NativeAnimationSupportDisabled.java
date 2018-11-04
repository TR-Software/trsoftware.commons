/*
 * Copyright 2018 TR Software Inc.
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
 *
 */

package solutions.trsoftware.commons.client.animations;

import com.google.gwt.animation.client.AnimationScheduler;

/**
 * Workaround for animations not working under HtmlUnit-2.19 (bug).
 * <p>
 * Overrides {@link #isNativelySupported()} to always return {@code false}, so that {@link AnimationScheduler#get()}
 * uses the timer-based implementation instead of the browser's native {@code requestAnimationFrame} API.
 * <p>
 * <b>Usage:</b>
 * <pre>{@code
 *   <replace-with class="solutions.trsoftware.commons.client.animations.NativeAnimationSupportDisabled">
 *     <when-type-is class="com.google.gwt.animation.client.AnimationScheduler.AnimationSupportDetector"/>
 *   </replace-with>
 * }</pre>
 *
 * @see <a href="https://github.com/gwtproject/gwt/issues/9616">Animation can't be tested with GWTTestCase (GWT bug)</a>
 * @see com.google.gwt.animation.client.AnimationSchedulerImplTimer
 * @see com.google.gwt.animation.client.AnimationSchedulerImplStandard
 *
 * @author Alex
 * @since 5/1/2018
 */
public class NativeAnimationSupportDisabled extends AnimationScheduler.AnimationSupportDetector {
  @Override
  public boolean isNativelySupported() {
    return false;
  }
}
