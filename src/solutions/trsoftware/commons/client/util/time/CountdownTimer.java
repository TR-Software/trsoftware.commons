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

package solutions.trsoftware.commons.client.util.time;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.user.client.Timer;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.web.bindery.event.shared.SimpleEventBus;
import solutions.trsoftware.commons.shared.util.time.Time;

/**
 * Every time it fires, the timer checks for changes in the referenced {@link Time}
 * instance and updates itself accordingly.  Once the target time is reached, the timer cancels itself.
 * Supports external listeners for started, changed, and finished events.
 *
 * @author Alex, 3/22/2015
 */
public class CountdownTimer extends Timer {

  public static final int DEFAULT_UPDATE_INTERVAL_MILLIS = 200;

  private final Time clock;
  /** The countdown runs until this time value is reached. */
  private double targetTimestamp;
  /** The timer's firing frequency */
  private int refreshInterval;

  private EventBus eventBus = new SimpleEventBus();

  public CountdownTimer(Time clock, final int refreshInterval) {
    this.clock = clock;
    this.refreshInterval = refreshInterval;
  }

  /** Starts or restarts the countdown until the given absolute timestamp. */
  public CountdownTimer startCountdownUntil(double targetTimestamp) {
    this.targetTimestamp = targetTimestamp;
    scheduleRepeating(refreshInterval);
    eventBus.fireEvent(new StartedEvent(getTimeRemaining()));
    return this;
  }

  /** Starts or restarts the countdown until the given number of milliseconds from now. */
  public CountdownTimer startCountdownInterval(double delta) {
    startCountdownUntil(clock.currentTimeMillis() + delta);
    return this;
  }

  private double getTimeRemaining() {
    return clock.getMillisUntil(targetTimestamp);
  }

  @Override
  public void run() {
    double timeRemaining = Math.max(0d, getTimeRemaining());
    eventBus.fireEvent(new TickEvent(timeRemaining));
    if (timeRemaining == 0d) {
      eventBus.fireEvent(new FinishedEvent());
      cancel();
    }
  }

  /** Receive notifications about the new time remaining value after every {@link #refreshInterval}, until the countdown reaches 0. */
  public HandlerRegistration addTickHandler(TickEvent.Handler handler) {
    return eventBus.addHandler(TickEvent.TYPE, handler);
  }

  /** Receive a notification when the countdown has started or has been restarted. */
  public HandlerRegistration addStartedHandler(StartedEvent.Handler handler) {
    return eventBus.addHandler(StartedEvent.TYPE, handler);
  }

  /** Receive a notification when the countdown has reached its target, and the timer will be cancelled. */
  public HandlerRegistration addFinishedHandler(FinishedEvent.Handler handler) {
    return eventBus.addHandler(FinishedEvent.TYPE, handler);
  }

  /** Base class for all events defined by {@link CountdownTimer}. */
  private static abstract class CountdownEvent<H extends CountdownEvent.Handler> extends Event<H> {
    /** Marker interface for handlers of event subclasses */
    interface Handler extends EventHandler { }

    private double timeRemaining;

    protected CountdownEvent() {
    }

    protected CountdownEvent(double timeRemaining) {
      this.timeRemaining = timeRemaining;
    }

    public double getTimeRemaining() {
      return timeRemaining;
    }
  }

  /**
   * Register for this event to receive notifications about the new time remaining value after every
   * {@link #refreshInterval}, until the countdown reaches 0.
   */
  public static class TickEvent extends CountdownEvent<TickEvent.Handler> {

    public interface Handler extends CountdownEvent.Handler {
      void onTick(TickEvent event);
    }

    public static Type<Handler> TYPE = new Type<Handler>();

    public TickEvent(double timeRemaining) {
      super(timeRemaining);
    }

    @Override
    public Type<Handler> getAssociatedType() {
      return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
      handler.onTick(this);
    }
  }

  /** Signals that the countdown has started (or restarted). */
  public static class StartedEvent extends CountdownEvent<StartedEvent.Handler> {

    public interface Handler extends CountdownEvent.Handler {
      void onCountdownStarted(StartedEvent event);
    }

    public static Type<Handler> TYPE = new Type<Handler>();

    public StartedEvent(double timeRemaining) {
      super(timeRemaining);
    }

    @Override
    public Type<Handler> getAssociatedType() {
      return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
      handler.onCountdownStarted(this);
    }
  }

  /** Signals that the countdown has reached its target and the timer will be cancelled. */
  public static class FinishedEvent extends CountdownEvent<FinishedEvent.Handler> {

    public interface Handler extends CountdownEvent.Handler {
      void onCountdownFinished(FinishedEvent event);
    }

    public static Type<Handler> TYPE = new Type<Handler>();

    @Override
    public Type<Handler> getAssociatedType() {
      return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
      handler.onCountdownFinished(this);
    }
  }

}
