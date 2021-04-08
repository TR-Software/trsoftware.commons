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

import com.google.common.base.MoreObjects;
import solutions.trsoftware.commons.server.util.Clock;

import javax.annotation.Nonnull;
import java.time.Instant;

/**
 * Convenience base class for event objects.
 * Stores the event creation time (see {@link #getTimestamp()}).
 *
 * @author Alex
 * @since 9/30/2019
 */
public abstract class AbstractEvent implements Event {

  /**
   * The time (in epoch millis) when this object was created.
   */
  protected final long timestamp;

  protected AbstractEvent() {
    this.timestamp = Clock.currentTimeMillis();
  }

  @Override
  public long getTimestamp() {
    return timestamp;
  }

  /**
   * Uses the {@link MoreObjects.ToStringHelper} instance returned by {@link #getToStringHelper()} to generate
   * the string representation of this event, which defaults to {@code "<classSimpleName>{<timestamp>}"},
   * e.g. {@code "LoginEvent{2019-10-29T05:00:25.229Z}"}
   * @implNote Subclasses are encouraged to override {@link #getToStringHelper()} instead of this method.
   */
  @Override
  public String toString() {
    return getToStringHelper().toString();
  }

  /**
   * Subclasses are encouraged to override this method to add other fields
   * to the returned {@link MoreObjects.ToStringHelper} instance.
   * @return a new instance that will build the string {@code "<classSimpleName>{<timestamp>}"};
   * example: {@code "LoginEvent{2019-10-29T05:00:25.229Z}"}
   */
  @Nonnull
  protected MoreObjects.ToStringHelper getToStringHelper() {
    return MoreObjects.toStringHelper(this)
        .addValue(Instant.ofEpochMilli(timestamp));
  }
}
