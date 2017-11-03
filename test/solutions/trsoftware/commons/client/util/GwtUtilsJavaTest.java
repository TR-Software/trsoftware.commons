package solutions.trsoftware.commons.client.util;

import static solutions.trsoftware.commons.client.util.GwtUtils.getSimpleName;
import static solutions.trsoftware.commons.client.util.GwtUtils.isAssignableFrom;
import junit.framework.TestCase;

import java.util.Map;


/**
 * Mar 21, 2011
 *
 * @author Alex
 */
public class GwtUtilsJavaTest extends TestCase {

  public void testIsAssignableFrom() throws Exception {
    Class<Number> number = Number.class;
    Class<Integer> integer = Integer.class;

    assertTrue(number.isAssignableFrom(integer));
    assertTrue(isAssignableFrom(number, integer));

    assertFalse(integer.isAssignableFrom(number));
    assertFalse(isAssignableFrom(integer, number));
  }

  public void testGetClassSimpleName() throws Exception {
    Class<Integer> integer = Integer.class;
    assertEquals("java.lang.Integer", integer.getName());
    assertEquals("Integer", integer.getSimpleName());
    assertEquals("Integer", getSimpleName(integer));

    Class<Map.Entry> mapEntry = Map.Entry.class;
    assertEquals("java.util.Map$Entry", mapEntry.getName());
    assertEquals("Entry", mapEntry.getSimpleName());
    assertEquals("Entry", getSimpleName(mapEntry));
  }
}