package solutions.trsoftware.commons.shared.util.random;

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.util.random.RandomCharGenerator;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Alex, 8/4/2017
 */
public class RandomCharGeneratorTest extends TestCase {

  public void testNext() throws Exception {
    String alphabet = "12345";
    RandomCharGenerator gen = new RandomCharGenerator(alphabet);
    int iterations = 100;
    for (int i = 0; i < iterations; i++) {
      assertTrue(alphabet.indexOf(gen.next()) >= 0);
    }
  }

  public void testNextWithExclusions() throws Exception {
    String alphabet = "12345";
    RandomCharGenerator gen = new RandomCharGenerator(alphabet);
    int iterations = 100;
    for (int nCharsToExclude = 1; nCharsToExclude < alphabet.length() - 1; nCharsToExclude++) {
      Set<Character> exclusionSet = new HashSet<Character>();
      for (int i = 0; i < nCharsToExclude; i++) {
         exclusionSet.add(alphabet.charAt(i));
      }
      for (int i = 0; i < iterations; i++) {
        assertFalse(exclusionSet.contains(gen.next(exclusionSet)));
      }
    }
  }
}