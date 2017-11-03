package solutions.trsoftware.commons.shared.util.collections;

import solutions.trsoftware.commons.client.testutil.AssertUtils;
import solutions.trsoftware.commons.client.util.StringUtils;
import junit.framework.TestCase;

import java.util.List;

/**
 * Mar 14, 2011
 *
 * @author Alex
 */
public class EditSequenceTest extends TestCase {

  public void testEditSequence() throws Exception {
    // These examples are from org.apache.commons.lang3.StringUtilsTest.java
    checkSequence(0, "", "");
    checkSequence(1, "", "a");
    checkSequence(3, "bar", "f");
    checkSequence(7, "aaapppp", "");
    checkSequence(1, "frog", "fog");
    checkSequence(3, "fly", "ant");
    checkSequence(7, "elephant", "hippo");
    checkSequence(7, "hippo", "elephant");
    checkSequence(8, "hippo", "zzzzzzzz");
    checkSequence(8, "zzzzzzzz", "hippo");
    checkSequence(1, "hello", "hallo");
  }

  private static void checkSequence(int expectedDistance, String s, String t) {
    List<Character> sList = StringUtils.asList(s);
    List<Character> tList = StringUtils.asList(t);
    checkSequence(expectedDistance, sList, tList);
    // try swapping the arguments to check for symmetry
    checkSequence(expectedDistance, tList, sList);
  }

  private static <T> void checkSequence(int expectedDistance, final List<T> s, final List<T> t) {
    assertNotNull(s);
    assertNotNull(t);
    EditSequence<T> editSequence = EditSequence.create(s, t);
    System.out.println(StringUtils.methodCallToStringWithResult("EditSequence.create", editSequence, s, t));
    assertEquals(expectedDistance, editSequence.length());
    assertEquals(t, editSequence.transformCopy(s));
    assertThrowsNPE(s, null);
    assertThrowsNPE(null, t);
    assertThrowsNPE(null, null);
  }

  private static <T> void assertThrowsNPE(final List<T> s, final List<T> t) {
    assertTrue(s == null || t == null);
    AssertUtils.assertThrows(NullPointerException.class, new Runnable() {
      @Override
      public void run() {
        EditSequence.create(null, t);
      }
    });
  }

}