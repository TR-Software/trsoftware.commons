package solutions.trsoftware.commons.shared.text.markovchain.state;

import solutions.trsoftware.commons.shared.text.markovchain.dict.CodingDictionary;

import java.util.Arrays;

/**
 * Oct 26, 2009
 *
 * @author Alex
 */
class NgramStringState extends StringState {

  private String[] wordCodes;

  public NgramStringState(CodingDictionary<String> dict, String... words) {
    wordCodes = new String[words.length];
    for (int i = 0; i < words.length; i++) {
      wordCodes[i] = dict.encode(words[i]);
    }
  }

  @Override
  public String getWord(int index, CodingDictionary<String> dict) {
    return wordCodes[index];
  }

  public int wordCount() {
    return wordCodes.length;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    NgramStringState that = (NgramStringState)o;

    if (!Arrays.equals(wordCodes, that.wordCodes)) return false;

    return true;
  }

  public int hashCode() {
    return wordCodes != null ? Arrays.hashCode(wordCodes) : 0;
  }
}
