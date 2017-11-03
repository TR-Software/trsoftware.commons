package solutions.trsoftware.commons.client.util.text;

import solutions.trsoftware.commons.client.util.StringUtils;

/**
 * Treats every char as a word for logographic languages.
 *
 * Oct 19, 2009
 * @author Alex
 */
public class LogographicTokenizer extends TextTokenizer {

  @Override
  public String getDelimiter() {
    return "";
  }

  public String[] tokenize(String text) {
    String[] words = new String[text.length()];
      for (int i = 0; i < words.length; i++) {
        words[i] = text.substring(i, i+1);
      }
    return words;
  }

  public String join(String[] tokens) {
    return StringUtils.join(getDelimiter(), tokens);
  }
}
