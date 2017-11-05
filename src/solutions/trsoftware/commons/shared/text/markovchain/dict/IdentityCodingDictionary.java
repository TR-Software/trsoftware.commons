package solutions.trsoftware.commons.shared.text.markovchain.dict;

/**
 * This coder does nothing.  The output is the same instance as the input.
 *
 * Oct 26, 2009
 *
 * @author Alex
 */
public class IdentityCodingDictionary implements CodingDictionary<String> {

  /** Returns a canonical code for the word */
  public String encode(String word) {
    return word;
  }

  /** Translates a canonical code back to the word */
  public String decode(String code) {
    return code;
  }

  /** @return the number of unique words in the dictionary */
  public int size() {
    return 0;
  }
}