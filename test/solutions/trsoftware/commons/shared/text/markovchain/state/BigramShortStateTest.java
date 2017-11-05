package solutions.trsoftware.commons.shared.text.markovchain.state;

import solutions.trsoftware.commons.client.testutil.AssertUtils;

/**
 * Oct 20, 2009
 *
 * @author Alex
 */
public class BigramShortStateTest extends ShortStateTest {

  public void testBigramShortState() throws Exception {
    final BigramShortState state = new BigramShortState("foo", "bar", dict);
    assertEquals("foo", state.getWord(0, dict));
    assertEquals("bar", state.getWord(1, dict));
    assertEquals(2, state.wordCount());
    AssertUtils.assertThrows(IndexOutOfBoundsException.class,
        new Runnable() {
          public void run() {
            state.getWord(2, dict);
          }
        });

    // check for proper implementation of equals and hashCode
    assertTrue(state.equals(new BigramShortState("foo", "bar", dict)));
    assertTrue(state.hashCode() == new BigramShortState("foo", "bar", dict).hashCode());

    assertFalse(state.equals(new BigramShortState("foo", "b", dict)));
    assertFalse(state.hashCode() == new BigramShortState("foo", "b", dict).hashCode());
  }
}