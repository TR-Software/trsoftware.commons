package solutions.trsoftware.commons.shared.text.markovchain.dict;

/**
 * Maps words in the dictionary to unique shorts and back again.
 *
 * Supports dictionaries with up to Short.MAX_VALUE = 32767 entries.
 *
 * Uses StringUtf8 for inner to save memory.
 *
 * Oct 20, 2009
 *
 * @author Alex
 */
public class ShortArrayCodingDictionary extends ArrayCodingDictionary<String, Short> {

  protected String encodeKey(String key) {
    return key;
  }

  protected String decodeKey(String key) {
    return key;
  }

  protected Short makeValue(int value) {
    return (short)value;
  }
}