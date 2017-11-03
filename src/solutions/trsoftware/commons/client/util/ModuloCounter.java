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

package solutions.trsoftware.commons.client.util;

/**
 * Helps ensure that a certain event occurs at certain intervals
 *
 * Example: (do something every 3rd time through a loop, but not unil the 10th iteration)
 *
 * <pre><code>
 * ModuloCounter mc = new ModuloCounter(10, 3);
 * for (int i = 0; i < n; i++) {
 *   if (mc.increment())
 *     doSomething();
 * }
 * </pre></code>
 * 
 * Provides an alternative to MutableInteger or AtomicInteger for situations
 * where the modulo check will be perfomed in more than 1 place (encapsulates
 * the modulus and the modulo checking logic).
 *
 * Jan 21, 2010
 *
 * @author Alex
 */
public class ModuloCounter {
  private int count;
  private final int modulus;
  private final int min;

  /**
   * @param min Don't trigger until the counter reaches at least this number.
   * @param modulus The increment() method will return true when the counter
   * mod this number is equal to 0.
   */
  public ModuloCounter(int min, int modulus) {
    this.min = min;
    // it doesn't make sense to have a counter with limit of less than 1 or more than the modulus
    if (min < 0 || modulus < 1)
      throw new IllegalArgumentException("ModuloCounter args must be positive");
    this.modulus = modulus;
  }

  /**
   * Increments the counter and then checks it.
   *
   * @return true if this call broke the barrier: the count is now equal
   * to 0 mod modulus and is greater or equal to min.
   */
  public boolean increment() {
    count++;
    return check();
  }

  /**
   * Checks the counter without incrementing it.
   * @return true if this call broke the barrier: the count is now equal
   * to 0 mod modulus and is greater or equal to min.
   */
  public boolean check() {
    return count >= min && count % modulus == 0;
  }

  public int getCount() {
    return count;
  }
}