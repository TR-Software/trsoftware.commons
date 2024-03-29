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

package solutions.trsoftware.commons.server.io;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

/**
 * Oct 2, 2009
 *
 * @author Alex
 */
public final class ServerIOUtils {

  /**
   * The size of the buffer used by the stream reading and copying methods in this class.
   * (Same as the default size used by {@link BufferedReader})
   */
  public static final int DEFAULT_BUFFER_SIZE = 8192;

  /** Value of the {@code line.separator} system property */
  public static final String LINE_SEPARATOR = System.getProperty("line.separator");

  /**
   * @return an an {@link InputStreamReader} using the UTF-8 charset for the given input stream.
   */
  public static Reader readUTF8(InputStream input) {
    return new InputStreamReader(input, StandardCharsets.UTF_8);
  }

  /**
   * @return a {@code UTF-8} reader for the given file
   */
  public static Reader readFileUTF8(File file) throws FileNotFoundException {
    return readUTF8(new FileInputStream(file));
  }

  /** Can be used for reading a text file into a String, using the {@code UTF-8} charset */
  public static String readFileIntoStringUTF8(File file) throws IOException {
    return readCharactersIntoString(readFileUTF8(file));
  }

  /**
   * Opens a writer that will overwrite the given file.
   *
   * @return A writer which outputs to the given file.  This writer
   * must be closed explicitly by the caller.
   */
  public static Writer writeFileUTF8(File file) throws FileNotFoundException {
    return writeFileUTF8(file, false);
  }

  /**
   * Opens a writer that will append to the given file.
   * @return A writer which outputs to the given file.  This writer
   * must be closed explicitly by the caller.
   */
  public static Writer writeFileUTF8(File file, boolean append) throws FileNotFoundException {
    return new OutputStreamWriter(new FileOutputStream(file, append), StandardCharsets.UTF_8);
  }

  public static void writeStringToFileUTF8(File file, String str, boolean append) {
    Writer writer = null;
    try {
      writer = writeFileUTF8(file, append);
      writer.write(str);
    }
    catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    finally {
      if (writer != null) {
        try {
          writer.close();
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static void writeStringToFileUTF8(File file, String str) {
    writeStringToFileUTF8(file, str, false);
  }

  /** Returns a stream to the given file, catching and wrapping any FileNotFoundException with a RuntimeException */
  public static FileOutputStream getFileOutputStreamUnchecked(File file) {
    try {
      return new FileOutputStream(file);
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  /**
   * Can be used for reading a text file or another input stream into a String,
   * interpreting the input bytes as {@code UTF-8} chars.  Uses an 8K buffer to reduce CPU usage.
   * Closes the input stream when finished.
   */
  public static String readCharactersIntoString(InputStream in) throws IOException {
    return readCharactersIntoString(in, StandardCharsets.UTF_8);
  }

  /**
   * Can be used for reading a text file or another input stream into a String.  Uses an 8K buffer to reduce CPU usage.
   * Closes the input stream when finished.
   */
  public static String readCharactersIntoString(InputStream in, Charset charset) throws IOException {
    StringBuilder s = new StringBuilder(DEFAULT_BUFFER_SIZE);
    byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
    try {
      int nRead = 0;
      while ((nRead = in.read(buf)) >= 0) {
        s.append(new String(buf, 0, nRead, charset));
      }
      return s.toString();
    }
    finally {
      in.close();
    }
  }

  /**
   * Can be used for reading a text file into a String.  Closes the reader when finished.
   * Uses an 8K buffer to reduce CPU usage.
   * <p>
   *   WARNING: if given a file reader, the result will contain platform-specific line break characters
   *   (e.g. {@code "\r\n"} on Windows).  If that's not desired, can use a {@link BufferedReader} instead
   *   (whose {@link BufferedReader#readLine()} method omits the line-break chars).
   * </p>
   */
  public static String readCharactersIntoString(Reader reader) throws IOException {
    StringBuilder s = new StringBuilder(DEFAULT_BUFFER_SIZE);
    char[] buf = new char[DEFAULT_BUFFER_SIZE];
    try (Reader in = reader) {
      int nRead = 0;
      while ((nRead = in.read(buf)) >= 0) {
        s.append(buf, 0, nRead);
      }
      return s.toString();
    }
  }


  public static ArrayList<String> readLines(Reader reader, boolean ignoreBlankLines) {
    ArrayList<String> lines = new ArrayList<>(2048);
    try (BufferedReader br = new BufferedReader(reader)) {
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        if (line.trim().length() > 0 || !ignoreBlankLines)
          lines.add(line);
      }
    }
    catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    return lines;
  }

  /** Copies everything from the reader to the writer, using a {@value #DEFAULT_BUFFER_SIZE}-char buffer */
  public static void copyReaderToWriter(Reader from, Writer to) throws IOException {
    char[] buf = new char[DEFAULT_BUFFER_SIZE];
    int n;
    do {
      n = from.read(buf);
      if (n > 0)
        to.write(buf, 0, n);
    }
    while (n >= 0);
  }

  /**
   * Copies everything from input to output, using a {@value #DEFAULT_BUFFER_SIZE}-byte buffer
   *
   * @return the total number of bytes that were written to the output stream (should be the same as the number
   * of bytes read from the input stream)
   */
  public static long copyInputToOutput(InputStream from, OutputStream to) throws IOException {
    return copyInputToOutput(from, to, DEFAULT_BUFFER_SIZE);
  }

  /**
   * Copies everything from input to output, using a temporary buffer of the given size.
   *
   * @param bufferSize size of the buffer to use for this operation
   * @return the total number of bytes that were written to the output stream (should be the same as the number
   * of bytes read from the input stream)
   */
  public static long copyInputToOutput(InputStream from, OutputStream to, int bufferSize) throws IOException {
    try {
      return copyInputToOutput(from, to, bufferSize, Long.MAX_VALUE);
    }
    catch (InputStreamTooLongException e) {
      // this should never happen in a practical scenario because we're using Long.MAX_VALUE as the limit
      throw new IllegalStateException(e);
    }
  }

  /**
   * Copies bytes from input to output, using a temporary buffer of the given size, until the given limit is reached.
   * When more than the given limit number of bytes have been read, will terminate prematurely by throwing
   * {@link InputStreamTooLongException}, before reaching the end of the input stream.
   * <p>
   * If needed, code that catches this exception can resume the copy operation using the info provided by the
   * exception (see {@link InputStreamTooLongException#continueCopying(InputStream, OutputStream)}).
   * <p>
   * The buffer size argument can be adjusted to control how often the limit will be checked.
   *
   * @param from the input stream
   * @param to the output stream
   * @param bufferSize size of the buffer to use for this operation; the limit will be checked in increments of this size
   * @param inputLengthLimit will stop copying after this number of bytes have been read from the input stream
   * @return the total number of bytes that were written to the output stream
   *
   * @throws InputStreamTooLongException will be thrown as soon as the number of bytes read from the input stream
   * exceeds the given limit.  If this happens, it's possible that the the input stream will not have been read
   * completely, and that not all of the bytes read from the input stream have been written to the output stream.
   * If needed, code that catches this exception can resume the copy operation using the info provided by the
   * exception (see {@link InputStreamTooLongException#continueCopying(InputStream, OutputStream)})
   *
   * @see #copyInputToOutput(InputStream, OutputStream)
   * @see #copyInputToOutput(InputStream, OutputStream, int)
   * @see InputStreamTooLongException#continueCopying(InputStream, OutputStream)
   */
  public static long copyInputToOutput(InputStream from, OutputStream to, int bufferSize, long inputLengthLimit) throws IOException, InputStreamTooLongException {
    long byteCount = 0;
    byte[] buf = new byte[bufferSize];
    int n;
    do {
      n = from.read(buf);
      if (n > 0) {
        byteCount += n;
        if (byteCount > inputLengthLimit) {
          // stop prematurely because limit was reached
          throw new InputStreamTooLongException(byteCount - n, inputLengthLimit, buf, n);
        }
        to.write(buf, 0, n);
      }
    }
    while (n >= 0);
    return byteCount;  // the total number of bytes actually copied to the output stream
  }

  /**
   * Wraps the given output stream with a {@link GZIPOutputStream} initialized to use the given compression level (0-9)
   * and buffer size.
   * <p>
   * We provide this method because {@link GZIPOutputStream} doesn't expose any way to change the compression level
   * from its {@linkplain Deflater#DEFAULT_COMPRESSION default value}.
   *
   * <h3>Performance considerations:</h3>
   * Our past experiments (using JSON data) showed no significant correlation between buffer size and compression ratio
   * (e.g. using 65,536 instead of the default 512 only reduced the file size from 646KB to 645KB), although a larger
   * buffer <i>may</i> improve speed in some cases (depending on the characteristics of the destination device).
   * Using a higher compression level, however does have a noticeable impact on the compression ratio
   * e.g. (in the same experiment as above) we were able to reduce the file size down to 613KB by using
   * {@link Deflater#BEST_COMPRESSION} instead of {@link Deflater#DEFAULT_COMPRESSION}.
   * Disclaimer: we didn't measure how the compression level affects speed.
   *
   * @param outputStream the destination stream
   * @param compressionLevel the desired {@linkplain Deflater#setLevel(int) compression level}
   *     ({@value Deflater#NO_COMPRESSION} - {@value Deflater#BEST_COMPRESSION});
   *     can use the constants provided by the {@link Deflater} class (e.g. {@link Deflater#BEST_COMPRESSION})
   * @param bufferSize the output buffer size passed to {@link GZIPOutputStream#GZIPOutputStream(OutputStream, int)}.
   *     NOTE: our past experiments showed no significant correlation between buffer size and compression ratio
   *     (e.g. using 65,536 instead of 512 only reduced the file size from 646KB to 645KB)
   * @return a new {@link GZIPOutputStream} wrapping the given stream, with its compression level and buffer size
   *     initialized to the given values
   * @throws IllegalArgumentException if the given compression level is invalid (acceptable values:
   *     [{@value Deflater#NO_COMPRESSION} - {@value Deflater#BEST_COMPRESSION}], {@value Deflater#DEFAULT_COMPRESSION})
   *
   * @see Deflater#setLevel(int)
   * @see <a href="https://stackoverflow.com/q/1082320/">StackOverflow question about buffer sizes</a>
   *
   */
  public static GZIPOutputStream newGZIPOutputStream(OutputStream outputStream, int compressionLevel, int bufferSize) throws IOException {
    // NOTE: passing a larger buffer size value to the GZIPOutputStream constructor doesn't yield any significant improvements in output file size (e.g. using 65,536 instead of 512 only reduced the file size from 646KB to 645KB)
    // however, setting a custom compression level (best instead of default) reduces the file size to 613KB
    return new GZIPOutputStream(outputStream, bufferSize) {
      {
        def.setLevel(compressionLevel);
      }
    };
  }

  /**
   * Delegates to {@link #newGZIPOutputStream(OutputStream, int, int)} with bufferSize = {@value #DEFAULT_BUFFER_SIZE}.
   *
   * @param outputStream the destination stream
   * @param compressionLevel the desired {@linkplain Deflater#setLevel(int) compression level}
   *     ({@value Deflater#NO_COMPRESSION} - {@value Deflater#BEST_COMPRESSION});
   *     can use the constants provided by the {@link Deflater} class (e.g. {@link Deflater#BEST_COMPRESSION})
   * @return a new {@link GZIPOutputStream} wrapping the given stream, with its compression level
   *     initialized to the given value
   * @throws IllegalArgumentException if the given compression level is invalid (acceptable values:
   *     [{@value Deflater#NO_COMPRESSION} - {@value Deflater#BEST_COMPRESSION}], {@value Deflater#DEFAULT_COMPRESSION})
   *
   * @see #newGZIPOutputStream(OutputStream, int, int)
   */
  public static GZIPOutputStream newGZIPOutputStream(OutputStream outputStream, int compressionLevel) throws IOException {
    return newGZIPOutputStream(outputStream, compressionLevel, DEFAULT_BUFFER_SIZE);
  }

  /**
   * Spawns a {@link BufferedReaderThread} to print the given input stream, like the <i>stdout</i> of another process
   * (obtained with {@link Process#getInputStream()}) to the given {@link PrintStream}
   *
   * <p style="font-style: italic;">
   *   NOTE: this functionality can't be easily replicated using Java's {@link PipedInputStream} and {@link PipedOutputStream}
   * </p>
   *
   * @param from the input to read
   * @param to where to print the input
   * @param name will be passed to {@link Thread#Thread(String)}
   * @return the spawned thread
   *
   * @see #pipeStreams(Process, PrintStream, PrintStream, String)
   * @see #pipeStreams(Process, String)
   */
  public static BufferedReaderThread pipeStream(InputStream from, final PrintStream to, String name) {
    BufferedReaderThread readerThread = new BufferedReaderThread(from, name) {
      public boolean processLine(String line) {
        to.println(line);
        return true;
      }
    };
    readerThread.start();
    return readerThread;
  }

  /**
   * Spawns threads that will print the <i>stdout</i> and <i>stderr</i> streams from another process, line-by-line,
   * to the given print streams.
   *
   * If the {@code processName} argument is not {@code null}, each line of the output will be prefixed with
   * <nobr><code>"[<i>processName</i> stdout] "</code></nobr> or
   * <nobr><code>"[<i>processName</i> stderr] "</code></nobr>.
   *
   * @param process the process whose output will be read and printed
   * @param stdout where to print the normal output
   * @param stderr where to print the error output
   * @param processName will be used to prefix the printed output; pass {@code null} to disable the prefixing
   *
   * @see #pipeStreams(Process, String)
   * @see #pipeStream(InputStream, PrintStream, String)
   */
  public static void pipeStreams(Process process, PrintStream stdout, PrintStream stderr, String processName) {
    if (processName != null) {
      String stdoutPrefix = String.format("[%s stdout] ", processName);
      String stderrPrefix = String.format("[%s stderr] ", processName);
      pipeStream(process.getInputStream(), new PrefixedPrintStream(stdoutPrefix, stdout), stdoutPrefix + " reader thread");
      pipeStream(process.getErrorStream(), new PrefixedPrintStream(stderrPrefix, stderr), stderrPrefix + " reader thread");
    }
    else {
      pipeStream(process.getInputStream(), stdout, "external process stdout reader thread");
      pipeStream(process.getErrorStream(), stderr, "external process stderr reader thread");
    }
  }

  /**
   * Spawns threads that will print the <i>stdout</i> and <i>stderr</i> streams from another process, line-by-line,
   * to {@link System#out} and {@link System#err}, respectively.
   *
   * If the {@code processName} argument is not {@code null}, each line of the output will be prefixed with
   * <nobr><code>"[<i>processName</i> stdout] "</code></nobr> or
   * <nobr><code>"[<i>processName</i> stderr] "</code></nobr>.
   *
   * @param process the process whose output will be read and printed
   * @param processName will be used to prefix the printed output; pass {@code null} to disable the prefixing
   *
   * @see #pipeStreams(Process, PrintStream, PrintStream, String)
   * @see #pipeStream(InputStream, PrintStream, String)
   */
  public static void pipeStreams(Process process, String processName) {
    pipeStreams(process, System.out, System.err, processName);
  }

}
