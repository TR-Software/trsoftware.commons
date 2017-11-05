package solutions.trsoftware.commons.shared.text.markovchain.dict;

import solutions.trsoftware.commons.server.util.StringUtf8;

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
public class ShortArrayCodingDictionaryUtf8 extends ArrayCodingDictionary<StringUtf8, Short> {

  protected StringUtf8 encodeKey(String key) {
    return new StringUtf8(key);
  }

  protected String decodeKey(StringUtf8 key) {
    return key.toString();
  }

  protected Short makeValue(int value) {
    return (short)value;
  }
}