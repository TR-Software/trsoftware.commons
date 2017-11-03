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

package solutions.trsoftware.commons.client.util.mutable;

/**
 * A GWT-compatible immitation of java.util.concurrent's AtomicInteger
 * and {@link org.apache.commons.lang3.mutable.MutableInt}.
 *
 * This class isn't really threadsafe, but that doesn't matter for
 * client-side GWT code.  In pure Java code, it's better to use AtomicInteger,
 * or to externally synchronize operations on this class.
 *
 * @author Alex
 */
public class MutableInteger extends MutableNumber {

  private volatile int n;

  public MutableInteger() {
    n = 0;
  }

  public MutableInteger(int n) {
    this.n = n;
  }

  public int get() {
    return n;
  }

  public int incrementAndGet() {
    return ++n;
  }

  public void increment() {
    ++n;
  }

  public int getAndIncrement() {
    return n++;
  }

  public int decrementAndGet() {
    return --n;
  }

  public int getAndDecrement() {
    return n--;
  }

  public int addAndGet(int delta) {
    return n += delta;
  }

  public int getAndAdd(int delta) {
    int old = n;
    n += delta;
    return old;
  }

  public int setAndGet(int newValue) {
    n = newValue;
    return n;
  }

  public int getAndSet(int newValue) {
    int old = n;
    n = newValue;
    return old;
  }

  public int intValue() {
    return get();
  }

  public long longValue() {
    return get();
  }

  public float floatValue() {
    return get();
  }

  public double doubleValue() {
    return get();
  }

  public Number toPrimitive() {
    return n;
  }

  @Override
  public void merge(MutableNumber other) {
    n += other.intValue();
  }
}
