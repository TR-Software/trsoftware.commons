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

import junit.framework.TestCase;

/**
 * Apr 20, 2010
 *
 * @author Alex
 */
public class ModuloCounterTest extends TestCase {

  public void testCounterWithNoMin() throws Exception {
    ModuloCounter mc = new ModuloCounter(0, 3);
    assertFalse(mc.increment());
    assertFalse(mc.increment());
    assertTrue(mc.increment());
    assertFalse(mc.increment());
    assertFalse(mc.increment());
    assertFalse(mc.check());
    assertTrue(mc.increment());
    assertTrue(mc.check());
    assertEquals(6, mc.getCount());
  }

  public void testCounterWithMin() throws Exception {
    ModuloCounter mc = new ModuloCounter(4, 3);
    assertFalse(mc.increment());
    assertFalse(mc.increment());
    assertFalse(mc.increment());
    assertFalse(mc.increment());
    assertFalse(mc.increment());
    assertTrue(mc.increment());
    assertFalse(mc.increment());
    assertFalse(mc.increment());
    assertFalse(mc.check());
    assertTrue(mc.increment());
    assertTrue(mc.check());
    assertEquals(9, mc.getCount());
  }
}