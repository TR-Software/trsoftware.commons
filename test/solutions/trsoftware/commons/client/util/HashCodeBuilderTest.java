package solutions.trsoftware.commons.client.util;

import junit.framework.TestCase;

/**
 * Oct 17, 2012
 *
 * @author Alex
 */
public class HashCodeBuilderTest extends TestCase {

  public void testHashCode() throws Exception {
    HashCodeBuilder b1 = new HashCodeBuilder();
    HashCodeBuilder b2 = new HashCodeBuilder();
    assertTrue(b1.hashCode() == b2.hashCode());
    Object o1 = new Object();
    Object o2 = new Object();
    Object o3 = new Object();
    b1.update(o1);
    assertTrue(b1.hashCode() != b2.hashCode());
    b2.update(o1);
    assertTrue(b1.hashCode() == b2.hashCode());
    b1.update(o2,o3);
    assertTrue(b1.hashCode() != b2.hashCode());
    b2.update(o2);
    assertTrue(b1.hashCode() != b2.hashCode());
    b2.update(o3);
    assertTrue(b1.hashCode() == b2.hashCode());

    // check that objects that have the same equals methods have the same hash code
    assertTrue(
        new HashCodeBuilder().update("asdf").hashCode() ==
        new HashCodeBuilder().update("asdf").hashCode());
    assertTrue(
        new HashCodeBuilder().update("asdf").update("foo").hashCode() ==
        new HashCodeBuilder().update("asdf").update("foo").hashCode());
    assertTrue(
        new HashCodeBuilder().update(1.1).update(2.2).hashCode() ==
        new HashCodeBuilder().update(1.1).update(2.2).hashCode());
  }
}