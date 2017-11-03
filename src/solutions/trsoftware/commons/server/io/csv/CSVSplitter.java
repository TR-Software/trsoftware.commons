package solutions.trsoftware.commons.server.io.csv;

import com.google.common.collect.AbstractIterator;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Splits a large CSV stream into multiple chunks each containing at most the
 * number of lines specified by chunkSizeLines.
 *
 * The files are split along logical line boundaries (logical as opposed
 * to actual, which means taking care not to split on newline characters that
 * belong to a quoted cell).
 *
 * Example:
 *
 * CSVSplitter(1) will treat every logical line as a separate chunk, so
 *
 * "\n1,2,\"a b\"\n2,3,\"a\nc\"\n3,4,5\n\n" Will be split into three lines
 * 1: "\n1,2,"a b"\n"
 * 2: "2,3,"a\nc"\n"
 * 3: "3,4,5\n"
 *
 * Extra newline characters, such as the one at the end are ignored.
 *
 * The original formatting symbols including quotes and line breaks are not preserved,
 * because the input is parsed using CSVReader and written using CSVWriter.
 * You can customize their behavior by passing your own instance of CSVReader
 * into the constructor and overriding the protected method newCSVWriter.
 *
 *
 * Mar 11, 2011
 *
 * @author Alex
 */
public class CSVSplitter extends AbstractIterator<String> {
  private final CSVReader reader;
  private int chunkSizeLines;


  public CSVSplitter(CSVReader inputReader, int chunkSizeLines) {
    this.reader = inputReader;
    this.chunkSizeLines = chunkSizeLines;
  }

  public CSVSplitter(Reader inputReader, int chunkSizeLines) {
    this(new CSVReader(inputReader), chunkSizeLines);
  }

  /**
   * Subclasses can override to customize the behavior of the CSVWriter used
   * to write the output.
   * @param destination The CSVWriter will write to this output stream.
   */
  protected CSVWriter newCSVWriter(Writer destination) {
    return new CSVWriter(destination);
  }


  protected String computeNext() {
    try {
      String[] nextLine;
      StringWriter buffer = new StringWriter(1024);
      CSVWriter out = null;
      for (int i = 1; (nextLine = reader.readNext()) != null; i++) {
        if (nextLine.length == 1 && nextLine[0].isEmpty()) {
           // skip empty lines
          i--;
          continue;
        }
        if (out == null)
          out = newCSVWriter(buffer);
        out.writeNext(nextLine);
        if (i == chunkSizeLines)
          break;
      }
      if (out == null) {
        endOfData();
        reader.close();
        return null;
      }
      out.close();
      return buffer.toString();
    }
    catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

}
