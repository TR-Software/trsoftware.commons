package solutions.trsoftware.commons.server.testutil;

import solutions.trsoftware.commons.server.io.ServerIOUtils;
import solutions.trsoftware.commons.server.io.SplitterOutputStream;
import solutions.trsoftware.commons.shared.io.TablePrinter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.zip.Deflater;

import static java.lang.String.format;

/**
 * Compares raw size of text output vs. its gzipped size.
 * <p>
 * <b>Note:</b> must call {@link #flush()} or {@link #close()} after writing output
 * to get accurate compression stats.
 *
 * @see #getGzipSize()
 * @see #getCompressionRatio()
 * @see #getSpaceSaving()
 */
public class GzipTester implements TablePrinter.PrintableTableRow {
  final ByteArrayOutputStream rawBytes;
  final ByteArrayOutputStream gzipBytes;
  final PrintStream out;

  public GzipTester() throws IOException {
    rawBytes = new ByteArrayOutputStream();
    gzipBytes = new ByteArrayOutputStream();
    out = new PrintStream(new SplitterOutputStream(
        rawBytes,
        ServerIOUtils.newGZIPOutputStream(gzipBytes, Deflater.BEST_COMPRESSION)
    ));
  }

  /**
   * @param autoFlush If true, the gzip output will be flushed whenever a byte array is written,
   *   one of the {@code println} methods is invoked, or a newline character or byte is written,
   *   to make accurate compression stats available right away
   *
   */
  public GzipTester(boolean autoFlush) throws IOException {
    rawBytes = new ByteArrayOutputStream();
    gzipBytes = new ByteArrayOutputStream();
    out = new PrintStream(new SplitterOutputStream(
        rawBytes,
        ServerIOUtils.newGZIPOutputStream(gzipBytes, Deflater.BEST_COMPRESSION)
    ));
  }

  /**
   * Initializes this instance and calls {@link #println(String)} with the given input,
   * then flushes the stream to complete the compression and make the stats available right away.
   */
  public GzipTester(String rawString) throws IOException {
    this();
    out.println(rawString);
    out.flush();
  }

  public void println(String x) {
    out.println(x);
  }

  public void print(String s) {
    out.print(s);
  }

  public void flush() {
    out.flush();
  }

  public void close() {
    out.close();
  }

  public int getRawSize() {
    return rawBytes.size();
  }

  public int getGzipSize() {
    return gzipBytes.size();
  }

  public double getCompressionRatio() {
    // see https://en.wikipedia.org/wiki/Data_compression_ratio
    return (double)getRawSize() / getGzipSize();
  }

  public double getSpaceSaving() {
    // see https://en.wikipedia.org/wiki/Data_compression_ratio
    return 1d - ((double)getGzipSize() / getRawSize());
  }

  @Override
  public String toString() {
    return rawBytes.toString();
  }

  /**
   * Writes the current compression stats as a new row in the given table.
   */
  @Override
  public TablePrinter printTableRow(TablePrinter tp) {
    return appendStats(tp.newRow());
  }

  /**
   * Adds compression stats columns to the current row of the given table.
   */
  public TablePrinter appendStats(TablePrinter tp) {
    return tp
        .addCol("raw", format("%,d", getRawSize()))
        .addCol("gzip", format("%,d", getGzipSize()))
        .addCol("compressionRatio", format("%,.2f (%,.2f%%)", getCompressionRatio(), getSpaceSaving() * 100));
  }
}
