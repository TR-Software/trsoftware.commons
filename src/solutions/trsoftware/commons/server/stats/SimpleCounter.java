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

package solutions.trsoftware.commons.server.stats;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Uses an {@link AtomicInteger} to implement {@link Counter}, and therefore, should be thread-safe.
 *
 * @author Alex, 10/31/2017
 */
public class SimpleCounter extends Counter {

  private AtomicInteger count = new AtomicInteger();

  public SimpleCounter(String name) {
    super(name);
  }

  @Override
  public void add(int delta) {
    count.addAndGet(delta);
  }

  @Override
  public int getCount() {
    return count.get();
  }
}
