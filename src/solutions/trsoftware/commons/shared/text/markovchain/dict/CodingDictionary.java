package solutions.trsoftware.commons.shared.text.markovchain.dict;

/**
 * Oct 20, 2009
 *
 * @author Alex
 */
public interface CodingDictionary<V> {

  /** Returns a canonical code for the word */
  V encode(String word);

  /** Translates a canonical code back to the word */
  String decode(V code);

  /** @return the number of unique words in the dictionary */
  int size();

}
