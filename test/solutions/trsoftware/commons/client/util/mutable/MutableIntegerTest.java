package solutions.trsoftware.commons.client.util.mutable;
/**
 *
 * Date: Nov 28, 2008
 * Time: 4:19:45 PM
 * @author Alex
 */

import junit.framework.TestCase;

public class MutableIntegerTest extends TestCase {

  private MutableInteger one;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    one = new MutableInteger(1);
  }

  public void testGet() throws Exception {
    assertEquals(0, new MutableInteger().get());
    assertEquals(1, one.get());
    assertEquals(1, one.get());  // make sure get() doesn't mutate
    assertEquals(5, new MutableInteger(5).get());
  }

  public void testIncrementAndGet() throws Exception {
    assertEquals(2, one.incrementAndGet());
    assertEquals(2, one.get());
  }

  public void testGetAndIncrement() throws Exception {
    assertEquals(1, one.getAndIncrement());
    assertEquals(2, one.get());
  }

  public void testDecrementAndGet() throws Exception {
    assertEquals(0, one.decrementAndGet());
    assertEquals(0, one.get());
  }

  public void testGetAndDecrement() throws Exception {
    assertEquals(1, one.getAndDecrement());
    assertEquals(0, one.get());
  }

  public void testAddAndGet() throws Exception {
    assertEquals(3, one.addAndGet(2));
    assertEquals(3, one.get());
    assertEquals(-1, one.addAndGet(-4));
    assertEquals(-1, one.get());
  }

  public void testGetAndAdd() throws Exception {
    assertEquals(1, one.getAndAdd(2));
    assertEquals(3, one.get());
    assertEquals(3, one.getAndAdd(-4));
    assertEquals(-1, one.get());
  }

  public void testSetAndGet() throws Exception {
    assertEquals(3, one.setAndGet(3));
    assertEquals(3, one.get());
    assertEquals(-1, one.setAndGet(-1));
    assertEquals(-1, one.get());
  }

  public void testGetAndSet() throws Exception {
    assertEquals(1, one.getAndSet(3));
    assertEquals(3, one.get());
    assertEquals(3, one.getAndSet(-1));
    assertEquals(-1, one.get());
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
    assertNotNull(one.toPrimitive());
    assertTrue(one.toPrimitive() instanceof Integer);
  }

  public void testEquals() throws Exception {
    assertEquals(one, new MutableInteger(1));
  }

  public void testMerge() throws Exception {
    MutableInteger result = one;
    result.merge(new MutableInteger(2));
    result.merge(new MutableFloat(3));
    assertEquals(new MutableInteger(6), result);
  }
}