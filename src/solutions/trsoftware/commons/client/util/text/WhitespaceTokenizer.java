package solutions.trsoftware.commons.client.util.text;

import solutions.trsoftware.commons.client.util.CollectionUtils;
import solutions.trsoftware.commons.client.util.StringTokenizer;
import solutions.trsoftware.commons.client.util.StringUtils;

import java.util.List;


/**
 * Oct 19, 2009
 * @author Alex
 */
public class WhitespaceTokenizer extends TextTokenizer {

  @Override
  public String getDelimiter() {
    return " ";
  }

  public String[] tokenize(String text) {
    List<String> tokenList = CollectionUtils.asList(new StringTokenizer(text));
    return tokenList.toArray(new String[tokenList.size()]);
  }

  public String join(String[] tokens) {
    return StringUtils.join(getDelimiter(), tokens);
  }

}
