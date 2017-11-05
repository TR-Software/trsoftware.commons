package solutions.trsoftware.commons.shared.text.markovchain.state;

import solutions.trsoftware.commons.client.testutil.AssertUtils;

/**
 * Oct 20, 2009
 *
 * @author Alex
 */
public class NgramShortStateTest extends ShortStateTest {

  public void testNgramShortState() throws Exception {
    final NgramShortState state = new NgramShortState(dict, "foo", "bar", "baz");
    assertEquals("foo", state.getWord(0, dict));
    assertEquals("bar", state.getWord(1, dict));
    assertEquals("baz", state.getWord(2, dict));
    assertEquals(3, state.wordCount());
    AssertUtils.assertThrows(IndexOutOfBoundsException.class,
        new Runnable() {
          public void run() {
            state.getWord(3, dict);
          }
        });

    // check for proper implementation of equals and hashCode
    assertTrue(state.equals(new NgramShortState(dict, "foo", "bar", "baz")));
    assertTrue(state.hashCode() == new NgramShortState(dict, "foo", "bar", "baz").hashCode());

    assertFalse(state.equals(new NgramShortState(dict, "foo", "b", "baz")));
    assertFalse(state.hashCode() == new NgramShortState(dict, "foo", "b", "baz").hashCode());
  }
}