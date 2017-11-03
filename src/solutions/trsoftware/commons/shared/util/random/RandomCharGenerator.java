package solutions.trsoftware.commons.shared.util.random;

import java.util.HashSet;
import java.util.Set;

import static solutions.trsoftware.commons.shared.util.RandomUtils.rnd;

/**
 * Generates random chars drawn from the given alphabet.
 *
 * @author Alex, 8/4/2017
 */
public class RandomCharGenerator {

  private String alphabet;

  public RandomCharGenerator(String alphabet) {
    this.alphabet = alphabet;
  }

  public char next() {
    return alphabet.charAt(rnd.nextInt(alphabet.length()));
  }

  public char next(Set<Character> exclusions) {
    Set<Character> exclusionSet = new HashSet<Character>(exclusions);
    char next;
    do {
      next = next();
    }
    while (!exclusionSet.add(next));
    return next;
  }

}
