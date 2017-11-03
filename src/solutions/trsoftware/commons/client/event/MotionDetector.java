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

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.user.client.Event;
import com.google.web.bindery.event.shared.HandlerRegistration;
import solutions.trsoftware.commons.client.util.SmartTimer;
import solutions.trsoftware.commons.client.util.TimeUtils;

import java.util.HashSet;
import java.util.Iterator;

/**
 * A singleton that registers itself as a handler on the event preview stack to monitor user keyboard/mouse/touch activity.
 *
 * Consumers can register themselves to be notified of {@link IdlenessChangeEvent}s after various periods
 * of user inactivity, specified by {@link #addIdlenessHandler(double, IdlenessChangeEvent.Handler)}.
 *
 * Mar 21, 2013
 *
 * @author Alex
 */
public class MotionDetector implements Event.NativePreviewHandler {

  /**
   * A bitmask for events that we care about: those that constitute user activity
   * NOTE: we want to listen to as few event types as possible, to avoid CPU
   * overhead that would result from invoking (new Date()).getTime() for every event.
   */
  private static int ACTIVITY_EVENT_MASK = Event.ONMOUSEMOVE | Event.ONMOUSEDOWN | Event.ONKEYDOWN | Event.ONTOUCHSTART;
  /** The frequency with which {@link #activePeriods} will be checked by {@link #idlenessTimer} */
  private static final int IDLENESS_CHECK_FREQUENCY = 1000;  // TODO: set this to a less intrusive value after testing complete
  /** The singleton instance */
  private static MotionDetector instance;

  /** The time of the user's last mouse or keyboard event: used for tracking idleness vs. activity */
  private double lastActivityTime = TimeUtils.currentTimeMillis();
  /** This ensures we don't respond to duplicate "mousemove" events */
  private MouseMoveFilter mouseMoveFilter = new MouseMoveFilter();
  /**
   * Every invocation of {@link #addIdlenessHandler(double, IdlenessChangeEvent.Handler)} will result in a new instance
   * of {@link IdlePeriodRegistration} being added to this set.
   */
  private HashSet<IdlePeriodRegistration> activePeriods = new HashSet<IdlePeriodRegistration>();
  /**
   * Each {@link IdlePeriodRegistration} in {@link #activePeriods} will be moved to this set as soon as its
   * idle interval has been reached, and moved back to this set as soon as activity resumes again.
   */
  private HashSet<IdlePeriodRegistration> idlePeriods = new HashSet<IdlePeriodRegistration>();

  /** Periodically checks whether any of the active periods have gone idle */
  private SmartTimer idlenessTimer = new SmartTimer() {
    @Override
    public void doRun() {
      double idlenessDuration = getIdlenessDuration();
      if (!activePeriods.isEmpty()) {
        for (Iterator<IdlePeriodRegistration> iter = activePeriods.iterator(); iter.hasNext(); ) {
          IdlePeriodRegistration period = iter.next();
          if (idlenessDuration >= period.millis) {
            period.fireIdlenessChangeEvent(true);
            idlePeriods.add(period);
            iter.remove();
          }
        }
      }
    }
  };

  public static MotionDetector getInstance() {
    init();
    return instance;
  }

  /** Start listening for user events */
  public static void init() {
    if (instance == null)
      instance = new MotionDetector();
  }

  private MotionDetector() {
    // since this a singleton instance, we won't worry about removing this preview handler
    Event.addNativePreviewHandler(this);
  }

  public void onPreviewNativeEvent(Event.NativePreviewEvent event) {
    if ((event.getTypeInt() & ACTIVITY_EVENT_MASK) != 0 && !mouseMoveFilter.isDuplicate(Event.as(event.getNativeEvent()))) {
      motionDetected(); // this is one of the events that constitute user mouse or keyboard motion
    }
  }

  private void motionDetected() {
    lastActivityTime = TimeUtils.currentTimeMillis();
    if (!idlePeriods.isEmpty()) {
      // notify the subscribers of all the idle periods and move them back to activePeriods
      for (Iterator<IdlePeriodRegistration> iter = idlePeriods.iterator(); iter.hasNext(); ) {
        IdlePeriodRegistration period = iter.next();
        period.fireIdlenessChangeEvent(false);
        activePeriods.add(period);
        iter.remove();
      }
    }
  }

  public double getLastActivityTime() {
    return lastActivityTime;
  }

  public double getIdlenessDuration() {
    return TimeUtils.currentTimeMillis() - lastActivityTime;
  }

  /**
   * Add a handler which will be notified after each given period of user idleness (none of events specified by {@link #ACTIVITY_EVENT_MASK}
   * have occurred for the given period of time, and as soon as user activity resumes
   * @param periodMillis the period of user inactivity to get notified of
   * @return A memento for removing the handler
   */
  public <H extends IdlenessChangeEvent.Handler> HandlerRegistration addIdlenessHandler(double periodMillis, H handler) {
    return new IdlePeriodRegistration(periodMillis, handler);
  }

  /**
   * Records each idleness period that is being listened to by {@link #addIdlenessHandler(double, IdlenessChangeEvent.Handler)}
   */
  public class IdlePeriodRegistration implements HandlerRegistration {
    private final double millis;
    private final HandlerRegistration handlerRegistration;

    private <H extends IdlenessChangeEvent.Handler> IdlePeriodRegistration(double periodMillis, H handler) {
      this.millis = periodMillis;
      handlerRegistration = Events.BUS.addHandler(IdlenessChangeEvent.TYPE, handler);
      activePeriods.add(this);
      if (!idlenessTimer.isRepeating())
        idlenessTimer.scheduleRepeating(IDLENESS_CHECK_FREQUENCY);
    }

    private void fireIdlenessChangeEvent(boolean idle) {
      Events.BUS.fireEvent(new IdlenessChangeEvent(idle));
    }

    @Override
    public void removeHandler() {
      handlerRegistration.removeHandler();
      activePeriods.remove(this);
      idlePeriods.remove(this);
      if (activePeriods.isEmpty() && idlePeriods.isEmpty()) {
        idlenessTimer.cancel();
      }
    }
  }

  /**
   * Event fired by {@link MotionDetector} when no mouse or keyboard activity has been detected for a
   * specified period of time, and when activity resumes.
   */
  public static class IdlenessChangeEvent extends com.google.web.bindery.event.shared.Event<IdlenessChangeEvent.Handler> {
    /**
     * Handler interface for {@link IdlenessChangeEvent} events.
     */
    public static interface Handler extends EventHandler {
      void onIdlenessChange(IdlenessChangeEvent event);
    }

    public static final Type<Handler> TYPE = new Type<Handler>();

    /** True if this event signals that user has been idle for a specified period of time, false if activity has just resumed */
    private boolean idle;

    protected IdlenessChangeEvent(boolean idle) {
      this.idle = idle;
    }

    /**
     * @return {@link #idle}
     */
    public boolean isIdle() {
      return idle;
    }

    @Override
    public Type<Handler> getAssociatedType() {
      return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
      handler.onIdlenessChange(this);
    }
  }

}
