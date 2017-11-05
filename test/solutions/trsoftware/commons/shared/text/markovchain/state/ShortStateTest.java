package solutions.trsoftware.commons.shared.text.markovchain.state;

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.text.markovchain.dict.CodingDictionary;
import solutions.trsoftware.commons.shared.text.markovchain.dict.ShortHashArrayCodingDictionary;

/**
 * Oct 20, 2009
 *
 * @author Alex
 */
public abstract class ShortStateTest extends TestCase {
  protected CodingDictionary<Short> dict;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    dict = new ShortHashArrayCodingDictionary();
    dict.encode("a");
    dict.encode("foo");
    dict.encode("bar");
    dict.encode("foo");
    dict.encode("b");
    dict.encode("bar");
    dict.encode("baz");
    dict.encode("c");
  }

}