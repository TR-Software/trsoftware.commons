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

package java.text;

import com.google.gwt.i18n.shared.DateTimeFormat;

import java.util.Date;

/**
 * Emulates {@link java.text.SimpleDateFormat} for GWT.
 *
 * @author Alex
 * @since 8/31/2021
 */
public class SimpleDateFormat {
  // TODO: test the compatibility of pattern strings between Java's SimpleDateFormat and GWT's DateTimeFormat
  // TODO: use this code to implement a SharedDateFormat class, similar to solutions.trsoftware.commons.shared.util.text.SharedNumberFormat

  private DateTimeFormat dateTimeFormat;

  public SimpleDateFormat(String pattern) {
    dateTimeFormat = DateTimeFormat.getFormat(pattern);
  }

  /**
   * Formats a Date into a date/time string.
   *
   * @param date the time value to be formatted into a time string.
   * @return the formatted time string.
   */
  public final String format(Date date) {
    return dateTimeFormat.format(date);
  }

  /**
   * Parses text from the beginning of the given string to produce a date.
   *
   * @param source the string to parse
   * @return date parsed from the string
   * @throws ParseException if the beginning of the specified string cannot be parsed
   */
  public Date parse(String source) throws ParseException {
    try {
      return dateTimeFormat.parse(source);
    }
    catch (IllegalArgumentException e) {
      // unfortunately no way to get the errorOffset from the IAE thrown by the GWT class
      throw new ParseException(e.getMessage(), 0);
    }
  }
}
