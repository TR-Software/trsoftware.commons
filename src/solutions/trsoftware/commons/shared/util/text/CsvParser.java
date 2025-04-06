/*
 * Copyright 2021 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

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
    // TODO(8/6/2024): implement quoting support (see solutions.trsoftware.commons.server.io.csv.CSVReader.parseLine)
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
