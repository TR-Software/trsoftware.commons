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
import java.util.concurrent.atomic.AtomicLong;

/**
 * A {@link Ticker} that returns a fixed {@linkplain Ticker#read() value}, which can be changed arbitrarily.
 *
 * @author Alex, 10/6/2017
 * @see com.google.common.testing.FakeTicker
 */
public class FakeTicker extends Ticker implements SettableTicker {
  private final AtomicLong time = new AtomicLong();

  public FakeTicker() {
  }

  /**
   * @param nanos the fixed value to be returned from {@link #read()}
   */
  public FakeTicker(long nanos) {
    time.set(nanos);
  }

  /**
   * @return the fixed value of this ticker
   */
  @Override
  public long read() {
    return time.get();
  }

  @Override
  public SettableTicker setTime(long nanos) {
    time.set(nanos);
    return this;
  }

  @Override
  public void advance(long deltaNanos) {
    time.addAndGet(deltaNanos);
  }

  @Override
  public void advance(long duration, TimeUnit timeUnit) {
    advance(timeUnit.toNanos(duration));
  }
}
