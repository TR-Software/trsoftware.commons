package solutions.trsoftware.commons.server.util.gql;

import junit.framework.TestCase;

import static solutions.trsoftware.commons.server.util.gql.SortOrder.Direction.*;

/**
 * @author Alex
 * @since 1/7/2020
 */
public class SortOrderTest extends TestCase {

  public void testToGql() throws Exception {
    assertEquals("foo", new SortOrder("foo").toGql());
    assertEquals("foo", new SortOrder("foo", ASC).toGql());
    assertEquals("foo DESC", new SortOrder("foo", DESC).toGql());
  }

  public void testValueOf() throws Exception {
    assertEquals(new SortOrder("foo"), SortOrder.valueOf("foo ASC"));
    assertEquals(new SortOrder("foo", DESC), SortOrder.valueOf("foo DESC"));
    assertEquals(new SortOrder("foo"), SortOrder.valueOf("foo"));
    assertEquals(new SortOrder("foo", DESC), SortOrder.valueOf("-foo"));
  }

}