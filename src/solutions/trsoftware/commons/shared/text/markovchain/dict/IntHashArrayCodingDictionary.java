package solutions.trsoftware.commons.shared.text.markovchain.dict;

/**
 * Maps words in the dictionary to unique integers and back again.
 * 
 * Oct 20, 2009
 *
 * @author Alex
 */
public class IntHashArrayCodingDictionary extends HashArrayCodingDictionary<String, Integer> {

  protected String encodeKey(String key) {
    return key;
  }

  protected String decodeKey(String key) {
    return key;
  }

  protected Integer makeValue(int value) {
    return value;
  }
}
