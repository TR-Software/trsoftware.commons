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

package solutions.trsoftware.commons.shared.util.time;

import com.google.common.base.Ticker;

import java.util.concurrent.TimeUnit;

/**
 * Interface that can be implemented by a {@link Ticker} subclass that allows changing its
 * {@linkplain Ticker#read() value}.
 *
 * @see FakeTicker
 *
 * @author Alex
 * @since 12/5/2022
 */
public interface SettableTicker {
  /**
   * Overrides {@link Ticker#read()}
   */
  long read();

  /**
   * Sets the value to be returned by {@link #read()}
   */
  SettableTicker setTime(long nanos);

  /**
   * Increments the current value by the given number of nanos.
   */
  void advance(long deltaNanos);

  /**
   * Increments the current value by the given duration.
   */
  default void advance(long duration, TimeUnit timeUnit) {
    advance(timeUnit.toNanos(duration));
  }
}
