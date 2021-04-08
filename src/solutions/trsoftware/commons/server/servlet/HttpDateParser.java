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

package solutions.trsoftware.commons.server.servlet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Provides basic support for parsing the date/time formats described in the HTTP/1.1 specification
 * (<a href="https://tools.ietf.org/html/rfc2616#section-3.3">RFC 2616, Section 3.3</a>):
 *
 * <pre>
 *   Sun, 06 Nov 1994 08:49:37 GMT  ; RFC 822, updated by RFC 1123
 *   Sunday, 06-Nov-94 08:49:37 GMT ; RFC 850, obsoleted by RFC 1036
 *   Sun Nov  6 08:49:37 1994       ; ANSI C's asctime() format
 * </pre>
 *
 * @author Alex
 * @since 12/19/2020
 */
public class HttpDateParser {
  /*
    NOTE: this code partially duplicates the functionality provided by
       org.eclipse.jetty.http.DateParser and
       org.apache.tomcat.util.http.FastHttpDateFormat / org.apache.catalina.connector.Request
   */

  /*
    According to RFC 2616, HTTP servers are required to support all 3 of the following date formats:

      Sun, 06 Nov 1994 08:49:37 GMT  ; RFC 822, updated by RFC 1123
      Sunday, 06-Nov-94 08:49:37 GMT ; RFC 850, obsoleted by RFC 1036
      Sun Nov  6 08:49:37 1994       ; ANSI C's asctime() format

    (see https://tools.ietf.org/html/rfc2616#section-3.3)
   */

  public enum DateFormats {

    /**
     * RFC 822, updated by RFC 1123
     * <p>
     * Example: {@code Sun, 06 Nov 1994 08:49:37 GMT}
     *
     * @see <a href="https://tools.ietf.org/html/rfc822#section-5">RFC 822, Section 5</a>
     * @see <a href="https://tools.ietf.org/html/rfc1123#page-55">RFC 1123, Section 5.2.14</a>
     */
    RFC_1123("EEE, dd MMM yyyy HH:mm:ss zzz"),

    /**
     * RFC 850, obsoleted by RFC 1036
     * <p>
     * Example: {@code Sunday, 06-Nov-94 08:49:37 GMT}
     *
     * @see <a href="https://tools.ietf.org/html/rfc850#section-5">RFC 850</a>
     * @see <a href="https://tools.ietf.org/html/rfc1036#section-5">RFC 1036</a>
     */
    RFC_850("EEEEEE, dd-MMM-yy HH:mm:ss zzz"),

    /**
     * ANSI C's asctime() format
     * <p>
     * Example:
     * {@code Sun Nov  6 08:49:37 1994}
     */
    ASCTIME("EEE MMM dd HH:mm:ss yyyy");

    
    private final String formatString;

    private final ThreadLocal<SimpleDateFormat> threadLocalSimpleDateFormat;

    DateFormats(String formatString) {
      this.formatString = formatString;
      threadLocalSimpleDateFormat = ThreadLocal.withInitial(this::createSimpleDateFormat);
    }

    public String getFormatString() {
      return formatString;
    }

    public SimpleDateFormat createSimpleDateFormat() {
      SimpleDateFormat sdf = new SimpleDateFormat(formatString, Locale.US);
      sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
      return sdf;
    }

    public SimpleDateFormat getThreadLocalSimpleDateFormat() {
      return threadLocalSimpleDateFormat.get();
    }
  }


  /**
   * Attempts to parse the given date string using one of the 3 formats described in the HTTP/1.1 specification
   * (<a href="https://tools.ietf.org/html/rfc2616#section-3.3">RFC 2616, Section 3.3</a>):
   * <pre>
   *   Sun, 06 Nov 1994 08:49:37 GMT  ; RFC 822, updated by RFC 1123
   *   Sunday, 06-Nov-94 08:49:37 GMT ; RFC 850, obsoleted by RFC 1036
   *   Sun Nov  6 08:49:37 1994       ; ANSI C's asctime() format
   * </pre>
   *
   * @return the date, in epoch milliseconds, represented by the given string, or {@code -1} if the string can't be parsed
   * in any of the above formats
   */
  public static long parseDate(String dateString) {
    if (dateString != null) {
      for (DateFormats fmt : DateFormats.values()) {
        SimpleDateFormat simpleDateFormat = fmt.getThreadLocalSimpleDateFormat();
        try {
          Date date = simpleDateFormat.parse(dateString);
          return date.getTime();
        }
        catch (ParseException e) {
          // ignore, and try the next format
        }
      }
    }
    return -1;
  }

}
