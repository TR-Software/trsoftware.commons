package solutions.trsoftware.commons.client.util;

import solutions.trsoftware.commons.client.bridge.util.RandomGen;
import static solutions.trsoftware.commons.client.util.MemoryUnit.*;

import solutions.trsoftware.commons.client.testutil.AssertUtils;
import junit.framework.TestCase;

/**
 * Dec 10, 2010
 *
 * @author Alex
 */
public class MemoryUnitTest extends TestCase {

  public void testFrom() throws Exception {
    // check that converting 0 from any unit yields zero
    for (MemoryUnit unit1 : values()) {
      for (MemoryUnit unit2 : values()) {
        assertEquals(0d, unit1.from(unit2, 0));
      }
    }
    // check that converting 1 from any unit to BYTES simply yields the number of bytes in that unit
    for (MemoryUnit unit : values()) {
      assertEquals(unit.bytes, BYTES.from(unit, 1));
    }
    // check that converting any value from any unit to itself yields the given value
    for (MemoryUnit unit1 : values()) {
      assertEquals(1.234d, unit1.from(unit1, 1.234d));
    }

    // do some manual tests as a sanity check
    assertEquals(2.1484375, KILOBYTES.from(BYTES, 2200), AssertUtils.EPSILON);
    assertEquals(0.00209808349609375, MEGABYTES.from(KILOBYTES, 2.1484375), AssertUtils.EPSILON*1000);
    assertEquals(0.000002048909664154052734375, GIGABYTES.from(MEGABYTES, 0.00209808349609375), AssertUtils.EPSILON*1000*1000);
  }

  public void testTo() throws Exception {
    // check that converting 0 to any unit yields zero
    for (MemoryUnit unit1 : values()) {
      for (MemoryUnit unit2 : values()) {
        assertEquals(0d, unit1.to(unit2, 0));
      }
    }
    // check that converting 1 in any unit to MILLISECOND simply yields the number of milliseconds in that unit
    for (MemoryUnit unit : values()) {
      assertEquals(unit.bytes, unit.to(BYTES, 1));
    }
    // check that converting any value to any unit from itself yields the given value
    for (MemoryUnit unit1 : values()) {
      assertEquals(1.234d, unit1.to(unit1, 1.234d));
    }

    // do some manual tests as a sanity check
    assertEquals(2200, BYTES.from(KILOBYTES, 2.1484375), AssertUtils.EPSILON);
    assertEquals(2.1484375, KILOBYTES.from(MEGABYTES, 0.00209808349609375), AssertUtils.EPSILON*1000);
    assertEquals(0.00209808349609375, MEGABYTES.from(GIGABYTES, 0.000002048909664154052734375), AssertUtils.EPSILON*1000*1000);
  }

  public void testToBytes() throws Exception {
    RandomGen rnd = RandomGen.getInstance();
    for (int i = 0; i < 1000; i++) {
      double value = rnd.nextDouble() * rnd.nextInt(20);
      for (MemoryUnit unit : values()) {
        assertEquals(unit.toBytes(value), unit.to(BYTES, value));
      }
    }
  }

  public void testFromBytes() throws Exception {
    RandomGen rnd = RandomGen.getInstance();
    for (int i = 0; i < 1000; i++) {
      double value = rnd.nextDouble() * rnd.nextInt(20);
      for (MemoryUnit unit : values()) {
        assertEquals(unit.fromBytes(value), unit.from(BYTES, value));
      }
    }
  }
}