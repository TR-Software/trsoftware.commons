package solutions.trsoftware.commons.server.util.reflect;

import junit.framework.TestCase;

import java.io.Serializable;
import java.util.*;

public class ClassMapTest extends TestCase {

  private ClassMap<String> classMap;

  public void setUp() throws Exception {
    super.setUp();
    classMap = new ClassMap<String>();
  }

  private void addEntry(Class key) {
    classMap.put(key, key.getSimpleName());
  }

  public void tearDown() throws Exception {
    super.tearDown();
    classMap = null;
  }

  public void testGetKeysAssignableFrom() throws Exception {
    addEntry(Map.class);
    assertKeysAssignableFrom(new Class[]{HashMap.class},
        Map.class);
    assertKeysAssignableFrom(new Class[]{HashMap.class, TreeMap.class, LinkedHashMap.class},
        Map.class);
    assertKeysAssignableFrom(new Class[]{HashMap.class, TreeMap.class},
        Map.class);

    assertNoKeysAssignableFrom();
    assertNoKeysAssignableFrom(Object.class);
    assertNoKeysAssignableFrom(TreeMap.class, Object.class);
    assertNoKeysAssignableFrom(TreeMap.class, Map.class, Object.class);
    assertNoKeysAssignableFrom(Serializable.class);  // Map does not extend Serializable

    // now add some more specific entries
    addEntry(AbstractMap.class);
    addEntry(HashMap.class);

    assertKeysAssignableFrom(new Class[]{HashMap.class},
        HashMap.class, AbstractMap.class, Map.class);  // the results should be sorted in order of decreasing complexity
    assertKeysAssignableFrom(new Class[]{HashMap.class, LinkedHashMap.class},
        HashMap.class, AbstractMap.class, Map.class);
    assertKeysAssignableFrom(new Class[]{HashMap.class, TreeMap.class, LinkedHashMap.class},
        AbstractMap.class, Map.class);
    assertKeysAssignableFrom(new Class[]{HashMap.class, TreeMap.class},
        AbstractMap.class, Map.class);

    // the following assertions should still hold
    assertNoKeysAssignableFrom();
    assertNoKeysAssignableFrom(Object.class);
    assertNoKeysAssignableFrom(TreeMap.class, Object.class);
    assertNoKeysAssignableFrom(TreeMap.class, Map.class, Object.class);
    assertNoKeysAssignableFrom(Serializable.class);
  }

  private void assertKeysAssignableFrom(Class<?>[] args, Class<?>... expected) {
    assertEquals(Arrays.asList(expected), classMap.getKeysAssignableFrom(args));
    assertEquals(expected[0].getSimpleName(), classMap.getBestAssignableFrom(args));
  }

  private void assertNoKeysAssignableFrom(Class<?>... args) {
    assertTrue(classMap.getKeysAssignableFrom(args).isEmpty());
    assertNull(classMap.getBestAssignableFrom(args));
  }

}