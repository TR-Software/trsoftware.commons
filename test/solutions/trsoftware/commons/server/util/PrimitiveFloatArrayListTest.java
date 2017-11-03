package solutions.trsoftware.commons.server.util;

import solutions.trsoftware.commons.client.bridge.util.RandomGen;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Oct 22, 2012
 *
 * @author Alex
 */
public class PrimitiveFloatArrayListTest extends TestCase {

  public void testPrimitiveFloatArrayList() throws Exception {
    // test the class versus ArrayList<Float>
    ArrayList<Float> expectedList = new ArrayList<Float>();
    PrimitiveFloatArrayList ourList = new PrimitiveFloatArrayList();
    // first, add 1000 random elements to each
    RandomGen rnd = RandomGen.getInstance();
    for (int i = 0; i < 1000; i++) {
      float f = rnd.nextInt();
      expectedList.add(f);
      ourList.add(f);
      assertEquals(expectedList, ourList);
    }
    assertEquals(expectedList.size(), ourList.size());
    // now test in-place sorting
    Collections.sort(expectedList);
    Collections.sort(ourList);
    assertEquals(expectedList, ourList);
  }

}