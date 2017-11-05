package solutions.trsoftware.commons.shared.text.markovchain.dict;

import solutions.trsoftware.commons.shared.util.MapUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Speeds up insertion into ArrayCodingDictionary by maintaing a HashMap of
 * all the words at the expense of using up more memory.
 *
 * Oct 20, 2009
 *
 * @author Alex
 */
public abstract class HashArrayCodingDictionary<K, V extends Number> extends ArrayCodingDictionary<K, V> {

  /** Maps words to their code (ensures the uniqueness of word->code mappings) */
  private Map<K, V> mapping = new HashMap<K, V>();

  /**
   * Looks up the given word and returns its existing index.  If not found,
   * returns the provided nextIndex and adds it to the lookup table if
   * applicable.
   */
  @Override
  protected final int lookup(K word, int nextIndex) {
    return MapUtils.getOrInsert(mapping, word, makeValue(nextIndex)).intValue();
  }

}