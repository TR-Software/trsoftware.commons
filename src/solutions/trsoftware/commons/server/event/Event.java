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

package solutions.trsoftware.commons.server.event;

import java.util.function.Predicate;

/**
 * Convenience marker interface for event objects.
 * <p>
 * Provides access to the event creation time (see {@link #getTimestamp()}) as well as utility methods
 * for filtering events by their creation time (see {@link #isTimestampBetween(long, long)} and
 * {@link #timestampFilter(long, long)}).
 *
 * @author Alex
 * @since 10/9/2019
 */
public interface Event {

  /**
   * @return the time (in epoch millis) when this event occurred.
   */
  long getTimestamp();

  /**
   * @param startTime the lower bound in epoch millis (inclusive)
   * @param endTime the upper bound in epoch millis (exclusive)
   * @return {@code true} iff the timestamp of this event is within the given bounds
   */
  default boolean isTimestampBetween(long startTime, long endTime) {
    long timestamp = getTimestamp();
    return timestamp >= startTime && timestamp < endTime;
  }

  /**
   * @param startTime the lower bound in epoch millis (inclusive)
   * @param endTime the upper bound in epoch millis (exclusive)
   * @return a predicate that returns {@code true} iff the timestamp of this event is within the given bounds
   */
  static Predicate<Event> timestampFilter(long startTime, long endTime) {
    return event -> event.isTimestampBetween(startTime, endTime);
  }
}
