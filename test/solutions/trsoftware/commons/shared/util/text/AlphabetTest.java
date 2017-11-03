package solutions.trsoftware.commons.shared.util.text;

import junit.framework.TestCase;
import solutions.trsoftware.commons.client.util.StringUtils;

/**
 * @author Alex, 9/20/2017
 */
public class AlphabetTest extends TestCase {

  public void testGetPrettyName() throws Exception {
    assertEquals("Numbers", Alphabet.NUMBERS.getPrettyName());
    assertEquals("Home row", Alphabet.HOME_ROW.getPrettyName());
    assertEquals("Letters", Alphabet.LETTERS.getPrettyName());
    assertEquals("Letters and symbols", Alphabet.LETTERS_NUMBERS_AND_SYMBOLS.getPrettyName());
  }

  public void testLookup() throws Exception {
    assertLookupEquals(Alphabet.CUSTOM, "");
    assertLookupEquals(Alphabet.CUSTOM, "foobar");
    assertLookupEquals(Alphabet.NUMBERS, "0123456789");
    assertLookupEquals(Alphabet.HOME_ROW, "asdfghjkl;'");
    assertLookupEquals(Alphabet.LETTERS, Alphabet.getAllLowercaseAsciiSymbols('a', 'z'));
    assertLookupEquals(Alphabet.LETTERS_NUMBERS_AND_SYMBOLS, Alphabet.getAllLowercaseAsciiSymbols(' ', '~'));
  }

  private static void assertLookupEquals(Alphabet enumValue, String chars) {
    assertEquals(enumValue, Alphabet.lookup(chars));
    // attempt a lookup with the same chars but in a different order
    assertEquals(enumValue, Alphabet.lookup(StringUtils.reverse(chars)));
  }

}