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

package solutions.trsoftware.commons.shared.util.mutable;

import junit.framework.TestCase;

public class MutableFloatTest extends TestCase {

  private MutableFloat one;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    one = new MutableFloat(1);
  }

  public void testGet() throws Exception {
    assertEquals(0f, new MutableFloat().get());
    assertEquals(1f, one.get());
    assertEquals(1f, one.get());  // make sure get() doesn't mutate
    assertEquals(5f, new MutableFloat(5).get());
  }

  public void testIncrementAndGet() throws Exception {
    assertEquals(2f, one.incrementAndGet());
    assertEquals(2f, one.get());
  }

  public void testGetAndIncrement() throws Exception {
    assertEquals(1f, one.getAndIncrement());
    assertEquals(2f, one.get());
  }

  public void testDecrementAndGet() throws Exception {
    assertEquals(0f, one.decrementAndGet());
    assertEquals(0f, one.get());
  }

  public void testGetAndDecrement() throws Exception {
    assertEquals(1f, one.getAndDecrement());
    assertEquals(0f, one.get());
  }

  public void testAddAndGet() throws Exception {
    assertEquals(3f, one.addAndGet(2));
    assertEquals(3f, one.get());
    assertEquals(-1f, one.addAndGet(-4));
    assertEquals(-1f, one.get());
  }

  public void testGetAndAdd() throws Exception {
    assertEquals(1f, one.getAndAdd(2));
    assertEquals(3f, one.get());
    assertEquals(3f, one.getAndAdd(-4));
    assertEquals(-1f, one.get());
  }

  public void testSetAndGet() throws Exception {
    assertEquals(3f, one.setAndGet(3));
    assertEquals(3f, one.get());
    assertEquals(-1f, one.setAndGet(-1));
    assertEquals(-1f, one.get());
  }

  public void testGetAndSet() throws Exception {
    assertEquals(1f, one.getAndSet(3));
    assertEquals(3f, one.get());
    assertEquals(3f, one.getAndSet(-1));
    assertEquals(-1f, one.get());
  }

  public void testIntValue() throws Exception {
    assertEquals(1, one.intValue());
  }

  public void testLongValue() throws Exception {
    assertEquals(1L, one.longValue());
  }

  public void testFloatValue() throws Exception {
    assertEquals(1f, one.floatValue());
  }

  public void testDoubleValue() throws Exception {
    assertEquals(1d, one.doubleValue());
  }

  public void testToPrimitive() throws Exception {
    assertNotNull(one.numberValue());
    assertTrue(one.numberValue() instanceof Float);
  }

  public void testEquals() throws Exception {
    assertEquals(one, new MutableFloat(1));
  }

  public void testMerge() throws Exception {
    MutableFloat result = one;
    result.merge(new MutableInteger(2));
    result.merge(new MutableFloat(3));
    assertEquals(new MutableFloat(6), result);
  }
}