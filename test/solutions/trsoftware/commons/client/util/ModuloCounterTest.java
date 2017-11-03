package solutions.trsoftware.commons.client.util;

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