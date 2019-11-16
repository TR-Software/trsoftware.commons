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

package solutions.trsoftware.commons.shared.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Helps ensure that a certain event occurs a limited number of times.
 * Provides an alternative to MutableInteger or AtomicInteger for situations
 * where the limit check will be performed in more than 1 place (encapsulates
 * the limit and the limit checking logic).
 *
 * @since Jan 21, 2010
 * @author Alex
 */
public class LimitedCounter {
  private final AtomicInteger count = new AtomicInteger();
  private final int limit;

  public LimitedCounter(int limit) {
    // it doesn't make sense to have a counter with a limit of less than 1
    if (limit < 1)
      throw new IllegalArgumentException("LimitedCounter limit must be positive");
    this.limit = limit;
  }

  /**
   * Increments the counter.
   * @return {@code true} if this call broke the barrier, i.e. the count was less than
   * the limit <i>prior</i> to incrementation and now is equal to the limit
   */
  public boolean increment() {
    return count.incrementAndGet() == limit;
  }

  /** @return true if the count is greater or equal to the limit */
  public boolean metLimit() {
    return count.get() >= limit;
  }

  public int getCount() {
    return count.get();
  }
}