/*
 * Copyright 2018 TR Software Inc.
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
 *
 */

package solutions.trsoftware.commons.server.io;

import solutions.trsoftware.commons.shared.util.StringUtils;

import java.io.*;
import java.util.ArrayList;

/**
 * Oct 2, 2009
 *
 * @author Alex
 */
public final class ServerIOUtils {

  /** The size of the buffer used by the stream reading and copying methods in this class */
  public static final int BUFFER_SIZE = 8192;  // this is the default value from Java's BufferedReader class

  /** Value of the {@code line.separator} system property */
  public static final String LINE_SEPARATOR = System.getProperty("line.separator");

  /**
   * @return an an {@link InputStreamReader} using the UTF-8 charset for the given input stream.
   */
  public static Reader readUTF8(InputStream input) {
    try {
      return new InputStreamReader(input, StringUtils.UTF8_CHARSET_NAME);
    }
    catch (UnsupportedEncodingException e) {
      // will never happen - all java VM's support UTF-8
      throw new RuntimeException(e);
    }
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
    try {
      return new OutputStreamWriter(new FileOutputStream(file, append), StringUtils.UTF8_CHARSET_NAME);
    }
    catch (UnsupportedEncodingException e) {
      // will never happen - all java VM's support UTF-8
      throw new RuntimeException(e);
    }
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
    return readCharactersIntoString(in, StringUtils.UTF8_CHARSET_NAME);
  }

  /**
   * Can be used for reading a text file or another input stream into a String.  Uses an 8K buffer to reduce CPU usage.
   * Closes the input stream when finished.
   */
  public static String readCharactersIntoString(InputStream in, String charsetName) throws IOException {
    StringBuilder s = new StringBuilder(BUFFER_SIZE);
    byte[] buf = new byte[BUFFER_SIZE];
    try {
      int nRead = 0;
      while ((nRead = in.read(buf)) >= 0) {
        s.append(new String(buf, 0, nRead, charsetName));
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
   *   (whose {@link BufferedReader#readLine()} method omits the line-break chars.
   * </p>
   */
  public static String readCharactersIntoString(Reader reader) throws IOException {
    StringBuilder s = new StringBuilder(BUFFER_SIZE);
    char[] buf = new char[BUFFER_SIZE];
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

  /** Copies everything from the reader to the writer, closing both the reader and writer when finished */
  public static void copyReaderToWriter(Reader from, Writer to) throws IOException {
    try {
      char[] buf = new char[BUFFER_SIZE];
      int n;
      do {
        n = from.read(buf);
        if (n > 0)
          to.write(buf, 0, n);
      }
      while (n >= 0);
    }
    finally {
      try {
        from.close();
      }
      finally {
        to.close();
      }
    }
  }

  /** Copies everything from input to output, closing both streams when finished */
  public static void copyInputToOutput(InputStream from, OutputStream to) throws IOException {
    copyInputToOutput(from, to, BUFFER_SIZE);
  }

  /** Copies everything from input to output, closing both streams when finished */
  public static void copyInputToOutput(InputStream from, OutputStream to, int bufferSize) throws IOException {
    try {
      byte[] buf = new byte[bufferSize];
      int n;
      do {
        n = from.read(buf);
        if (n > 0)
          to.write(buf, 0, n);
      }
      while (n >= 0);
    }
    finally {
      try {
        from.close();
      }
      finally {
        to.close();
      }
    }
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
