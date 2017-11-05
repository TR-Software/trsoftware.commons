package solutions.trsoftware.commons.shared.text.markovchain.dict;

import solutions.trsoftware.commons.shared.util.MapUtils;

import java.util.HashMap;

/**
 * Implements functionality similar to String.intern, but allows the String
 * instances to be garbage collected when the instance of this class
 * is no longer referenced.
 *
 * String.intern() on the other hand, puts the strings in PermGen, not on the
 * heap, so they will likely never get GC'd.
 *
 * Oct 26, 2009
 *
 * @author Alex
 */
public class FlyweightCodingDictionary implements CodingDictionary<String> {

  private HashMap<String, String> flyweight = new HashMap<String, String>();

  /** Returns a canonical code for the word */
  public String encode(String word) {
    return MapUtils.getOrInsert(flyweight, word, word);
  }

  /** Translates a canonical code back to the word */
  public String decode(String code) {
    return flyweight.get(code);
  }

  /** @return the number of unique words in the dictionary */
  public int size() {
    return flyweight.size();
  }
}
