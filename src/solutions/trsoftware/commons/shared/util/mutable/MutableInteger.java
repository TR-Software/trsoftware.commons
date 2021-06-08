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

package solutions.trsoftware.commons.shared.util.mutable;

/**
 * A GWT-compatible replacement for {@link java.util.concurrent.atomic.AtomicInteger AtomicInteger}
 * and {@code org.apache.commons.lang3.mutable.MutableInt}.
 *
 * This class uses locking to synchronize updates, so in pure Java code it's more efficient to use {@code AtomicInteger}
 * (in client-side GWT code the {@code synchronized} keyword is simply ignored).
 *
 * @author Alex
 */
public class MutableInteger extends MutableNumber {

  private volatile int n;

  public MutableInteger() {
  }

  public MutableInteger(int n) {
    this.n = n;
  }

  public int get() {
    return n;
  }

  public synchronized int incrementAndGet() {
    return ++n;
  }

  public synchronized void increment() {
    ++n;
  }

  public synchronized int getAndIncrement() {
    return n++;
  }

  public synchronized int decrementAndGet() {
    return --n;
  }

  public synchronized int getAndDecrement() {
    return n--;
  }

  public synchronized int addAndGet(int delta) {
    return n += delta;
  }

  public int getAndAdd(int delta) {
    int old = n;
    n = old + delta;
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

  public Number numberValue() {
    return get();
  }

  @Override
  public synchronized void merge(MutableNumber other) {
    n += other.intValue();
  }

  @Override
  public int compareTo(Number o) {
    return intValue() - o.intValue();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    MutableInteger that = (MutableInteger)o;

    return n == that.n;
  }

  @Override
  public int hashCode() {
    return n;
  }
}
