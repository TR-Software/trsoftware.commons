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

package solutions.trsoftware.commons.shared.io;

import com.google.gwt.regexp.shared.RegExp;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * A simple way to output arrays as lines in a CSV-formatted text output.
 * The default quote, escape, and separator characters can be overridden.
 * An entry is only quoted in the output when it needs to be (i.e. the string
 * contains a separator character).
 *
 * Code mostly borrowed from <a href="http://opencsv.sourceforge.net/">OpenCSV 2.0.1</a> (Apache 2.0 license)
 *
 * @see <a href=http://tools.ietf.org/html/rfc4180>RFC 4180: Common Format and MIME Type for Comma-Separated Values (CSV) Files</a>
 * @since Sep 28, 2009
 * @author Alex
 *
 * @deprecated this implementation could contain bugs; we recommend using the
 * <a href="http://commons.apache.org/proper/commons-csv/">Apache Commons CSV</a> library instead.
 */
public class CSVWriter {

  // TODO: experimental: shared version of solutions.trsoftware.commons.server.io.csv.CSVWriter

  public static final int INITIAL_STRING_SIZE = 128;

  private Appendable writer;

  private char separator;

  private char quotechar;

  private char escapechar;

  private String lineEnd;

  private RegExp fieldNeedsQuotingRegex;

  /** The character used for escaping quotes. */
  public static final char DEFAULT_ESCAPE_CHARACTER = '"';

  /** The default value separator to use if none is supplied to the constructor. */
  public static final char DEFAULT_SEPARATOR = ',';

  /** The default line separator to use if none is supplied to the constructor. */
  public static final String DEFAULT_LINE_SEPARATOR = "\n";

  /**
   * The default quote character to use if none is supplied to the
   * constructor.
   */
  public static final char DEFAULT_QUOTE_CHARACTER = '"';

  /**
   * Constructs CSVWriter using a comma for the separator.
   *
   * @param writer the writer to an underlying CSV source.
   */
  public CSVWriter(Writer writer) {
    this(writer, DEFAULT_SEPARATOR);
  }

  /**
   * Constructs CSVWriter with supplied separator.
   *
   * @param writer the writer to an underlying CSV source.
   * @param separator the delimiter to use for separating entries.
   */
  public CSVWriter(Writer writer, char separator) {
    this(writer, separator, DEFAULT_QUOTE_CHARACTER);
  }

  /**
   * Constructs CSVWriter with supplied separator and quote char.
   *
   * @param writer the writer to an underlying CSV source.
   * @param separator the delimiter to use for separating entries
   * @param quotechar the character to use for quoted elements
   */
  public CSVWriter(Writer writer, char separator, char quotechar) {
    this(writer, separator, quotechar, DEFAULT_ESCAPE_CHARACTER);
  }

  /**
   * Constructs CSVWriter with supplied separator and quote char.
   *
   * @param writer the writer to an underlying CSV source.
   * @param separator the delimiter to use for separating entries
   * @param quotechar the character to use for quoted elements
   * @param escapechar the character to use for escaping quotechars or
   * escapechars
   */
  public CSVWriter(Writer writer, char separator, char quotechar, char escapechar) {
    this(writer, separator, quotechar, escapechar, getDefaultLineSeparator());
  }


  /**
   * Constructs CSVWriter with supplied separator and quote char.
   *
   * @param writer the writer to an underlying CSV source.
   * @param separator the delimiter to use for separating entries
   * @param quotechar the character to use for quoted elements
   * @param lineEnd the line feed terminator to use
   */
  public CSVWriter(Writer writer, char separator, char quotechar, String lineEnd) {
    this(writer, separator, quotechar, DEFAULT_ESCAPE_CHARACTER, lineEnd);
  }

  /**
   * Constructs CSVWriter with supplied separator, quote char, escape char and
   * line ending.
   *
   * @param writer the writer to an underlying CSV source.
   * @param separator the delimiter to use for separating entries
   * @param quotechar the character to use for quoted elements
   * @param escapechar the character to use for escaping quotechars or escapechars
   * @param lineEnd the line feed terminator to use
   */
  public CSVWriter(Writer writer, char separator, char quotechar, char escapechar, String lineEnd) {
    this.writer = writer;
    this.separator = separator;
    this.quotechar = quotechar;
    this.escapechar = escapechar;
    this.lineEnd = lineEnd;
    // RFC states: "Fields containing line breaks (CRLF), double quotes, and commas should be enclosed in double-quotes."
    this.fieldNeedsQuotingRegex = RegExp.compile(new StringBuilder(16)
        .append('[').append(lineEnd).append(quotechar).append(escapechar).append(separator).append(']').toString());
  }

  private static String getDefaultLineSeparator() {
    // "line.separator" property (as well as System.lineSeparator) not available in GWT,
    // but that's okay, "\n" is a reasonable default; see https://stackoverflow.com/a/26526969/1965404
    return System.getProperty("line.separator", DEFAULT_LINE_SEPARATOR);
  }

  /**
   * Writes the entire list to a CSV file. The list is assumed to be a
   * Object[], with each element written out as its default toString() representation
   */
  public void writeAll(List<Object[]> allLines) throws IOException {
    for (Object[] line : allLines) {
      writeNext(line);
    }
  }

  /**
   * Writes the next line to the file.
   *
   * @param nextLine a string array with each comma-separated element as a
   * separate entry.
   */
  public void writeNext(Object... nextLine) throws IOException {
    StringBuilder sb = new StringBuilder(INITIAL_STRING_SIZE);
    writeLine(sb, nextLine);
    if (sb.length() > 0) {
      String s = sb.toString();
      write(s);
    }

  }

  private void write(String s) throws IOException {
    writer.append(s);
  }

  /**
   * Writes a row of values to the given {@link StringBuilder}, terminated by the {@linkplain #lineEnd line separator}.
   *
   * @param sb output destination
   * @param values the values in the row
   * @return
   */
  public void writeLine(StringBuilder sb, Object... values) {
    if (values == null || values.length == 0)
      return;

    for (int i = 0; i < values.length; i++) {
      Object nextEntry = values[i];
      // emit an empty string (empty cell) for null values and toString repr of the object otherwise
      writeNextElement(sb, nextEntry == null ? "" : nextEntry.toString(), i == 0);
    }
    sb.append(lineEnd);
  }

  /**
   * Writes the next cell value to the given {@link StringBuilder}
   *
   * @param isFirst true if this is the first column in a row
   */
  public void writeNextElement(StringBuilder sb, String value, boolean isFirst) {
    if (!isFirst) {
      sb.append(separator);
    }
    if (value == null)
      return;

    boolean needsQuoting = needsQuoting(value);
    if (needsQuoting)
      sb.append(quotechar);

    if (stringContainsSpecialCharacters(value)) {
      // we gotta escape the special characters in this element
      for (int j = 0; j < value.length(); j++) {
        char nextChar = value.charAt(j);
        if (nextChar == quotechar) {
          sb.append(escapechar).append(nextChar);
        }
        else if (nextChar == escapechar) {
          sb.append(escapechar).append(nextChar);
        }
        else {
          sb.append(nextChar);
        }
      }
    }
    else {
      // no special chars in the element, write it out as-is
      sb.append(value);
    }

    if (needsQuoting)
      sb.append(quotechar);
  }

  private boolean stringContainsSpecialCharacters(String line) {
    return line.indexOf(quotechar) != -1 || line.indexOf(escapechar) != -1;
  }

  /** An element only needs to be quoted if it contains either a separator character or a quote character */
  private boolean needsQuoting(String line) {
    // RFC: "Fields containing line breaks (CRLF), double quotes, and commas should be enclosed in double-quotes."
    return fieldNeedsQuotingRegex.test(line);
  }

}
