package solutions.trsoftware.commons.shared.text.markovchain.dict;

import junit.framework.TestCase;

/**
 * Oct 20, 2009
 *
 * @author Alex
 */
public class IntHashArrayCodingDictionaryTest extends TestCase {
  IntHashArrayCodingDictionary codingDictionary;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    codingDictionary = new IntHashArrayCodingDictionary();
  }

  public void testCoding() throws Exception {
    // encode a few strings
    assertEquals((Integer)0, codingDictionary.encode("foo"));
    assertEquals((Integer)1, codingDictionary.encode("bar"));
    assertEquals((Integer)0, codingDictionary.encode("foo"));  // no duplicates allowed
    assertEquals(2, codingDictionary.size());
    assertEquals((Integer)2, codingDictionary.encode("baz"));
    assertEquals(3, codingDictionary.size());

    // now decode the same strings
    assertEquals("foo", codingDictionary.decode(0));
    assertEquals("bar", codingDictionary.decode(1));
    assertEquals("baz", codingDictionary.decode(2));
  }

}