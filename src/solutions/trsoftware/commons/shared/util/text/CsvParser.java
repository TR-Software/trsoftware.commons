package solutions.trsoftware.commons.shared.util.text;

import com.google.gwt.text.shared.Parser;
import solutions.trsoftware.commons.shared.util.StringUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Parses a list of values given as a comma-separated string, using the given {@link Parser}
 * to parse the elements of that list.
 *
 * @author Alex
 * @since 12/2/2017
 */
public class CsvParser<T> implements Parser<List<T>> {

  private Parser<T> valueParser;

  public CsvParser(Parser<T> valueParser) {
    this.valueParser = valueParser;
  }

  @Override
  public List<T> parse(CharSequence text) throws ParseException {
    if (text == null || text.length() == 0)
      return Collections.emptyList();
    ArrayList<T> ret = new ArrayList<T>();
    List<String> values = StringUtils.splitAndTrim(text.toString(), ",");
    for (String value : values) {
      ret.add(valueParser.parse(value));
    }
    return ret;
  }
}
