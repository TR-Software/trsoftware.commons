package solutions.trsoftware.commons.shared.text.markovchain.state;

import solutions.trsoftware.commons.client.testutil.AssertUtils;

/**
 * Oct 20, 2009
 *
 * @author Alex
 */
public class UnigramShortStateTest extends ShortStateTest {

  public void testUnigramShortState() throws Exception {
    final UnigramShortState state = new UnigramShortState("foo", dict);
    assertEquals("foo", state.getWord(0, dict));
    assertEquals(1, state.wordCount());
    AssertUtils.assertThrows(IndexOutOfBoundsException.class,
        new Runnable() {
          public void run() {
            state.getWord(1, dict);
          }
        });

    // check for proper implementation of equals and hashCode
    assertTrue(state.equals(new UnigramShortState("foo", dict)));
    assertTrue(state.hashCode() == new UnigramShortState("foo", dict).hashCode());

    assertFalse(state.equals(new UnigramShortState("b", dict)));
    assertFalse(state.hashCode() == new UnigramShortState("b", dict).hashCode());
  }
}