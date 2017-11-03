package solutions.trsoftware.commons.client.util;

import solutions.trsoftware.commons.client.CommonsGwtTestCase;
import static solutions.trsoftware.commons.client.util.GwtUtils.getSimpleName;
import static solutions.trsoftware.commons.client.util.GwtUtils.isAssignableFrom;

import java.util.Map;


/**
 * Mar 21, 2011
 *
 * @author Alex
 */
public class GwtUtilsTest extends CommonsGwtTestCase {

  public void testIsAssignableFrom() throws Exception {
    Class<Number> number = Number.class;
    Class<Integer> integer = Integer.class;

    assertTrue(isAssignableFrom(number, integer));
    assertFalse(isAssignableFrom(integer, number));
  }

  public void testGetClassSimpleName() throws Exception {
    Class<Integer> integer = Integer.class;
    assertEquals("java.lang.Integer", integer.getName());
    assertEquals("Integer", getSimpleName(integer));

    Class<Map.Entry> mapEntry = Map.Entry.class;
    assertEquals("java.util.Map$Entry", mapEntry.getName());
    assertEquals("Entry", getSimpleName(mapEntry));
  }
}