package solutions.trsoftware.commons.shared.util.iterators;

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.util.StringUtilsTest;

import java.util.stream.IntStream;

/**
 * @author Alex
 * @since 5/14/2019
 */
@SuppressWarnings("NonJREEmulationClassesInClientCode")
public class CodePointIteratorJavaTest extends TestCase {

  /**
   * Compares the elements returned by this iterator to that of {@link CharSequence#codePoints()}
   * @throws Exception
   */
  public void testCodePointIterator() throws Exception {
    // compare the output of this iterator to that
    String inputString = StringUtilsTest.THREE_MONKEYS;
    CodePointIterator codePointIterator = new CodePointIterator(inputString);
    IntStream codePointsStream = inputString.codePoints();
    codePointsStream.forEachOrdered(codePoint ->
        assertEquals(codePoint, codePointIterator.nextInt()));
    assertFalse(codePointIterator.hasNext());  // our iterator should be exhausted at this point
  }
}