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

package solutions.trsoftware.commons.client.animations;

import com.google.gwt.animation.client.Animation;

/**
 * This class extends {@link Animation} to expose whether it's running, since the {@link Animation#isRunning} field
 * is private.
 *
 * @author Alex
 */
public abstract class SmartAnimation extends Animation {
  private boolean running;

  @Override
  protected void onStart() {
    super.onStart();
    running = true;
  }

  @Override
  protected void onComplete() {
    super.onComplete();
    running = false;
  }

  @Override
  protected void onCancel() {
    super.onCancel();
    running = false;
  }

  public boolean isRunning() {
    return running;
  }
}
