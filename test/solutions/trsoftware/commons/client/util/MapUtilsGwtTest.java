package solutions.trsoftware.commons.client.util;

import solutions.trsoftware.commons.client.CommonsGwtTestCase;

/**
 * Date: Oct 20, 2008 Time: 6:53:29 PM
 *
 * @author Alex
 */
public class MapUtilsGwtTest extends CommonsGwtTestCase {

  MapUtilsTest delegate = new MapUtilsTest();

  public void testFilterMap() throws Exception {
    delegate.testFilterMap();
  }

  public void testRemoveNullValues() throws Exception {
    delegate.testRemoveNullValues();
  }

  public void testRetainAll() throws Exception {
    delegate.testRetainAll();
  }
}
