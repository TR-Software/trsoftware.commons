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

package solutions.trsoftware.commons.server.testutil;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A dummy {@link Runnable} that simply counts the number of times its {@link Runnable#run()} method was invoked.
 */
public class CountingRunnable implements Runnable {
  private AtomicInteger runCount = new AtomicInteger();

  @Override
  public void run() {
    runCount.incrementAndGet();
  }

  /**
   * @return the number of times that the {@link #run()} method was invoked.
   */
  public int getRunCount() {
    return runCount.get();
  }

  /**
   * @return {@code true} iff the {@link #run()} method invoked at least once.
   */
  public boolean ran() {
    return runCount.get() > 0;
  }
}
