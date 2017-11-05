package solutions.trsoftware.commons.shared.text.markovchain.dict;

import junit.framework.TestCase;

/**
 * Oct 21, 2009
 *
 * @author Alex
 */
public class ShortArrayCodingDictionaryTest extends TestCase {
  ShortArrayCodingDictionary codingDictionary;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    codingDictionary = new ShortArrayCodingDictionary();
  }

  public void testCoding() throws Exception {
    // encode a few strings
    assertEquals((Short)(short)0, codingDictionary.encode("foo"));
    assertEquals((Short)(short)1, codingDictionary.encode("bar"));
    assertEquals((Short)(short)0, codingDictionary.encode("foo"));  // no duplicates allowed
    assertEquals(2, codingDictionary.size());
    assertEquals((Short)(short)2, codingDictionary.encode("baz"));
    assertEquals(3, codingDictionary.size());

    // now decode the same strings
    assertEquals("foo", codingDictionary.decode((short)0));
    assertEquals("bar", codingDictionary.decode((short)1));
    assertEquals("baz", codingDictionary.decode((short)2));
  }
}