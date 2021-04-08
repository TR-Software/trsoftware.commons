/*
 * Copyright 2021 TR Software Inc.
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

package solutions.trsoftware.commons.client.animations;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.event.shared.EventHandler;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.web.bindery.event.shared.SimpleEventBus;

/**
 * This class extends {@link Animation} to add the following features:
 * <ol>
 *   <li>expose whether it's running, since {@link Animation#isRunning} is a {@code private} field</li>
 *   <li>save the current progress of the animation (the last value passed to {@link #onUpdate(double)})</li>
 *   <li>adding event handlers to listen for progress updates</li>
 * </ol>
 *
 * <p style="color: #6495ed; font-weight: bold;">
 *   TODO(3/6/2018): it's no longer necessary for this class to keep track of whether the animation is running,
 *   because the newer versions of GWT provide a public {@link #isRunning()} method.
 * </p>
 *
 * @see #isRunning()
 * @see #getProgress()
 * @see #addUpdateHandler(UpdateEvent.Handler)
 * @author Alex
 */
public abstract class SmartAnimation extends Animation {
  private boolean running;
  private double progress;

  private EventBus eventBus;

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

  @Override
  protected void onUpdate(double progress) {
    this.progress = progress;
    if (eventBus != null) {
      // UpdateEvent handlers have been added
      eventBus.fireEvent(new UpdateEvent(progress));
    }
  }

  public HandlerRegistration addUpdateHandler(UpdateEvent.Handler handler) {
    return ensureEventBus().addHandler(UpdateEvent.TYPE, handler);
  }

  private EventBus ensureEventBus() {
    if (eventBus == null)
      eventBus = new SimpleEventBus();
    return eventBus;
  }

  public boolean isRunning() {
    return running;
  }

  public double getProgress() {
    return progress;
  }


  public static class UpdateEvent extends Event<UpdateEvent.Handler> {
    private final double progress;

    interface Handler extends EventHandler {
      void onUpdate(UpdateEvent event);
    }

    public static final Type<Handler> TYPE = new Type<Handler>();

    public UpdateEvent(double progress) {
      this.progress = progress;
    }

    public double getProgress() {
      return progress;
    }

    @Override
    public Type<Handler> getAssociatedType() {
      return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
      handler.onUpdate(this);
    }


  }
}
