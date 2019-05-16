package solutions.trsoftware.commons.shared.util;

import junit.framework.TestCase;

import java.util.Arrays;

import static solutions.trsoftware.commons.shared.util.StringUtils.codePoints;

/**
 * Implements the test cases not already covered in {@link StringUtilsTest} due to GWT restrictions (using classes or
 * methods missing from the JRE Emulation Library).
 *
 * @author Alex
 * @since 5/14/2019
 */
@SuppressWarnings("NonJREEmulationClassesInClientCode")
public class StringUtilsJavaTest extends TestCase {

  public void testCodePoints() throws Exception {
    String inputString = StringUtilsTest.THREE_MONKEYS;
    assertTrue(Arrays.equals(
        inputString.codePoints().toArray(),
        codePoints(inputString)
    ));
  }
}