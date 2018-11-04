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

package solutions.trsoftware.commons.shared.util.time;

import solutions.trsoftware.commons.client.util.time.ClientTime;
import solutions.trsoftware.commons.shared.util.StringUtils;
import solutions.trsoftware.commons.shared.util.TimeUnit;

import java.util.LinkedList;

/**
 * Can be used to limit the number of operations performed during a specified window of time.
 *
 * For example, this can be used to avoid an accidental self-inflicted DDoS attack, by limiting the rate of
 * outgoing requests.
 *
 * Sep 12, 2010
 * @author Alex
 */
public class RateLimiter {

  /** The number of events to allow within the specified window of time */
  private final int max;
  /** Length of the time window expressed in {@link #timeWindowUnit} units. */
  private final double timeWindowDuration;
  /** The unit of time used to specify {@link #timeWindowDuration} */
  private final TimeUnit timeWindowUnit;
  /** Length of the time window expressed in milliseconds */
  private final double timeWindowMillis;
  /** Clock used to perform the time calculations */
  private final Time clock;
  /** This optional name is used in the exception message. */
  private final String eventName;

  private final LinkedList<Double> eventTimes = new LinkedList<Double>();

  /**
   * @param clock The clock used to perform the time calculations
   * @see #RateLimiter(int, double, TimeUnit, String)
   */
  public RateLimiter(int max, double timeWindowDuration, TimeUnit timeWindowUnit, Time clock, String eventName) {
    if (max < 1 || timeWindowDuration <= 0)
      throw new IllegalArgumentException();
    this.max = max;
    this.timeWindowDuration = timeWindowDuration;
    this.timeWindowUnit = timeWindowUnit;
    this.clock = clock;
    this.eventName = StringUtils.isBlank(eventName) ? "event" : eventName;
    timeWindowMillis = timeWindowUnit.toMillis(timeWindowDuration);
  }

  /**
   * @param max The number of events to allow within the specified window of time
   * @param timeWindowDuration Size of the time window expressed in the given units.
   * @param timeWindowUnit The unit of time used to specify the window duration
   * @param eventName This name is used in the exception message (can be null)
   */
  public RateLimiter(int max, double timeWindowDuration, TimeUnit timeWindowUnit, String eventName) {
    this(max, timeWindowDuration, timeWindowUnit, ClientTime.INSTANCE, eventName);
  }

  /**
   * Records a new event, throwing an exception if it exceeds {@link #max} within the recent time window.
   * The event will only be recorded if it's not exceeding the limit (there's enough room for it, which means
   * no exception will be thrown).  It is expected that throwing the exception will cause the event to be aborted
   * by the caller.
   *
   * @throws RateLimitException if the latest window already contains at least {@link #max} event times.
   */
  public synchronized void checkRateLimit() {
    // 1) clear all the events past the horizon
    while (!eventTimes.isEmpty() && clock.getMillisSince(eventTimes.peek()) >= timeWindowMillis)
      eventTimes.poll();
    // 2) now check if there's room for another event
    if (eventTimes.size() >= max)
      throw new RateLimitException();
    // there's enough room for this event
    eventTimes.addLast(clock.currentTimeMillis());
  }

  /**
   * @return the time remaining until the window will have room for one more event, assuming no more events will come
   * in between now and then.
   */
  public synchronized double millisUntilCanProceed() {
    if (eventTimes.size() < max)
      return 0;  // the window is not full yet, can proceed immediately
    // count off max events from the back of the queue
    return clock.getMillisUntil(eventTimes.get(eventTimes.size()-max) + timeWindowMillis);
  }

  /**
   * Thrown when the rate limit has been exceeded.
   */
  public class RateLimitException extends RuntimeException {
    public RateLimitException() {
      super(new StringBuilder("Exceeded the maximum rate of ")
          .append(max).append(" ").append(StringUtils.pluralize(eventName, max))
          .append(" per").append(timeWindowDuration == 1 ? "" : " " + timeWindowDuration).append(" ").append(timeWindowUnit.getPrettyName(timeWindowDuration))
          .toString());
    }
  }
}
