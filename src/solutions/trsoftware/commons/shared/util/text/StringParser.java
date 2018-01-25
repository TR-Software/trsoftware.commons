package solutions.trsoftware.commons.shared.util.text;

import com.google.gwt.text.shared.Parser;

/**
 * An implementation of {@link Parser} that simply returns the input string.
 *
 * @author Alex
 * @since 12/2/2017
 */
public class StringParser implements Parser<String> {
  @Override
  public String parse(CharSequence text) {
    if (text == null)
      return "";
    return text.toString();
  }
}
