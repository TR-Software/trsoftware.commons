/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.server.io;

import solutions.trsoftware.commons.server.util.ServerStringUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;

/**
 * Oct 2, 2009
 *
 * @author Alex
 */
public class ServerIOUtils {

  /** The size of the buffer used by the stream reading and copying methods in this class */
  public static final int BUFFER_SIZE = 8192;  // this is the default value from Java's BufferedReader class

  /** Value of the {@code line.separator} system property */
  public static final String LINE_SEPARATOR = System.getProperty("line.separator");

  public static Reader readFileUTF8(File file) throws FileNotFoundException {
    try {
      return new InputStreamReader(new FileInputStream(file), StringUtils.UTF8_CHARSET_NAME);
    }
    catch (UnsupportedEncodingException e) {
      // will never happen - all java VM's support UTF-8
      throw new RuntimeException(e);
    }
  }

  /** Can be used for reading a resource text file into a String, using the platform-default encoding. */
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
   * interpreting the input bytes as UTF-8 chars.  Uses an 8K buffer to reduce CPU usage. 
   * Closes the input stream when finished.
   */
  public static String readCharacterStreamIntoStringUtf8(InputStream in) throws IOException {
    return readCharacterStreamIntoString(in, StringUtils.UTF8_CHARSET_NAME);
  }

  /**
   * Can be used for reading a text file or another input stream into a String,
   * interpreting the input bytes as UTF-8 chars.  Uses an 8K buffer to reduce CPU usage.
   * Closes the input stream when finished.
   */
  public static String readCharacterStreamIntoString(InputStream in, String charsetName) throws IOException {
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
   * WARNING: if given a file reader, the result will contain platform-specific
   * line break characters (e.g. "\r\n" on Windows)
   */
  public static String readCharactersIntoString(Reader in) throws IOException {
    StringBuilder s = new StringBuilder(BUFFER_SIZE);
    char[] buf = new char[BUFFER_SIZE];
    try {
      int nRead = 0;
      while ((nRead = in.read(buf)) >= 0) {
        s.append(buf, 0, nRead);
      }
      return s.toString();
    }
    finally {
      in.close();
    }
  }

  /** Can be used for reading a resource text file into a String, using the platform-default encoding. */
  public static String readResourceFileIntoString(String resourceName) throws IOException {
    return readCharactersIntoString(readResourceFile(resourceName));
  }

  /** Can be used for reading a resource text file into a String, using UTF-8 encoding. */
  public static String readResourceFileIntoStringUTF8(String resourceName) throws IOException {
    return readCharactersIntoString(readResourceFileUTF8(resourceName));
  }

  /**
   * @return a string that can be used to construct a {@code FileReader} or {@code File} for a resource
   * (e.g. a file in a webapp's the WAR) or classpath, given its resource name (e.g. {@code "/example.txt"})
   */
  public static String resourceNameToFilename(String resourceName) {
    return urlToFilepath(getResource(resourceName));
  }

  /**
   * Resolves the given resource name to a file using {@link #getResource(String)}
   * @return the {@link File} interface to the resource with the given name, or {@code null} if no resource with this name is found
   * (e.g. {@code "/example.txt"} &rarr; {@code "/home/foo/bar/example.txt"})
   */
  public static File resourceNameToFile(String resourceName) {
    URL fileUrl = getResource(resourceName);
    if (fileUrl != null)
      return new File(urlToFilepath(fileUrl));
    return null;
  }

  /**
   * Uses the given class to resolve the given resource name to a {@link URL} (which will most likely point to a
   * file on the local system).  The name will be resolved using {@link Class#getResource(String)}.
   * @return the resolved {@link URL} for the given resource name or {@code null} if no resource with this name is found.
   * If the result is not {@code null}, it can be used to create a {@link File} interface for this resource by passing
   * the value of {@link URL#getFile()} to {@link File#File(String)}.
   */
  public static URL getResource(String resourceName, Class refClass) {
    // NOTE: for some reason ServerIOUtils.class.getResource(resourceName)
    // behaves differently from ServerIOUtils.class.getClassLoader().getResource()
    URL fileUrl = refClass.getResource(resourceName);
    if (fileUrl == null)
      fileUrl = refClass.getClassLoader().getResource(resourceName);  // fall back to using the ClassLoader
    return fileUrl;
  }

  /**
   * Uses this class to resolve the given resource name to a {@link URL}
   * @return the resolved {@link URL} for the given resource name or {@code null} if no resource with this name is found
   * @see #getResource(String, Class)
   */
  public static URL getResource(String resourceName) {
    return getResource(resourceName, ServerIOUtils.class);
  }

  /**
   * URL-decodes the result of {@link URL#getFile()}, so that it can be used as an argument to {@link File#File(String)}.
   * This is necessary because the file path might contain spaces, which {@link URL#getFile()} would return as
   * {@code %20}, and the file system won't recognize them.
   *
   * @return The URL-decoded result of {@link URL#getFile()}
   *
   */
  public static String urlToFilepath(URL fileUrl) {
    // there's a bug in the JVM that breaks on file paths with spaces b/c spaces are url-encoded
    return ServerStringUtils.urlDecode(fileUrl.getFile());
  }

  /**
   * @return A {@link File} created from the URL-decoded result of {@link URL#getFile()}
   * @see #urlToFilepath(URL)
   */
  public static File urlToFile(URL fileUrl) {
    // there's a bug in the JVM that breaks on file paths with spaces b/c spaces are url-encoded
    return new File(urlToFilepath(fileUrl));
  }


  /**
   * Converts a simple (un-prefixed) filename to a resource path relative to the path of the given class.
   * This method is useful for unit testing, but should be used with caution in production code (see the deprecation warning)
   *
   * <p>
   *   <b>Example:</b>
   *   Given {@code "myfile.txt"} and {@code com.foo.MyClass}, will return {@code "/com/foo/myfile.txt"}
   * </p>
   *
   * @param filename a simple, unqualified filename like {@code "myfile.txt"} (but not {@code "/foo/bar/myfile.txt"}),
   * naming a file that presumably exists in the same package (or directory) as {@code referenceClass}
   * @param referenceClass the given {@code filename} is presumed to reside in the
   * @return a string that can be used to refer to the given file in the same package as the given class.
   *
   * @deprecated This deprecation warning exists simply to warn the user about a potential pitfall in using this method
   * in production code. For example, when a webapp is packaged into a WAR using some kind of typical build script (like Ant),
   * the compiler doesn't automatically copy non-java files to the bytecode directory (typically {@code /war/WEB-INF/classes/})
   * It is therefore our recommendation that you explicitly add your static resources to some other directory inside the
   * WAR, (e.g. {@code /war/WEB-INF/resources/}) and refer to those resources using resource names relative to the webapp's
   * root instead of the package name of some compiled {@code .class} file.
   */
  public static String resourceNameFromFilenameInSamePackage(String filename, Class referenceClass) {
    String pkgName = referenceClass.getPackage().getName();
    String pkgPrefix = pkgName.replace('.', '/');
    if (pkgPrefix.length() > 0) {
      pkgPrefix += "/";
    }
    return  "/" + pkgPrefix + filename;
  }

  /**
   * @return a fully-qualified path/filename given a simple filename in the package
   * of the referenced class.
   * Example: If "myfile.txt" and MyClass.java ar in the directory
   * "/c:/projects/myproject/src/com/foo/", then
   * filenameInPackageOf("myfile.txt", com.foo.MyClass.class) will return
   * "/c:/projects/myproject/src/com/foo/myfile.txt"
   *
   * WARNING: this method should only be used in unit tests and local code,
   * becuase the deployed WAR file will not include non-class files in its build output.
   * (All such files go in the src/resources directory).
   */
  public static String filenameInPackageOf(String filename, Class referenceClassInSamePackage) {
    return resourceNameToFilename(resourceNameFromFilenameInSamePackage(filename, referenceClassInSamePackage));
  }

  /**
   * Returns the lines in the given file in the WAR, given a resource name
   * like "/example.txt"
   */
   public static ArrayList<String> readLinesFromResource(String resourceName, boolean ignoreBlankLines) {
     try {
       return readLines(readResourceFile(resourceName), ignoreBlankLines);
     }
     catch (FileNotFoundException e) {
       e.printStackTrace();
       throw new RuntimeException(e);
     }
   }

  /**
   * Returns the lines in the given file in the WAR, given a resource name
   * like "/example.txt" using the UTF-8 file encoding.
   */
   public static ArrayList<String> readLinesFromResourceUTF8(String resourceName, boolean ignoreBlankLines) {
     try {
       return readLines(readResourceFileUTF8(resourceName), ignoreBlankLines);
     }
     catch (FileNotFoundException e) {
       e.printStackTrace();
       throw new RuntimeException(e);
     }
   }

  /**
   * Returns a Reader for a file in the WAR, given its resource name like "/example.txt",
   * using the platform-default file encoding.
   */
  public static Reader readResourceFile(String resourceName) throws FileNotFoundException {
    return new FileReader(resourceNameToFilename(resourceName));
  }

  /**
   * Returns a Reader for a file in the WAR, given its resource name like "/example.txt",
   * using the UTF8 file encoding.
   */
  public static Reader readResourceFileUTF8(String resourceName) throws FileNotFoundException {
    return readFileUTF8(new File(resourceNameToFilename(resourceName)));
  }

  public static ArrayList<String> readLines(Reader reader, boolean ignoreBlankLines) {
    ArrayList<String> lines = new ArrayList<>(2048);
    BufferedReader br = null;
    try {
      br = new BufferedReader(reader);
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        if (line.trim().length() > 0 || !ignoreBlankLines)
          lines.add(line);
      }
    }
    catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    finally {
      if (br != null) {
        try {
          br.close();
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return lines;
  }

  /**
   * Creates a new file object by inserting the given suffix string before the
   * extension of the original filename.
   * @param originalFile (e.g. "/home/foo/bar.txt")
   * @param suffix (e.g. "_bak")
   * @return a file like "/home/foo/bar_bak.txt" (based on the above example)
   */
  public static File fileWithSuffix(File originalFile, String suffix) {
    return new File(originalFile.getParent(), filenameWithSuffix(originalFile.getName(), suffix));
  }

  /**
   * Creates a new filename by inserting the given suffix string before the extension
   * of the original file.
   * @param originalName (e.g. "bar.txt")
   * @param suffix (e.g. "_bak")
   * @return a filename like "bar_bak.txt" (based on the above example)
   */
  public static String filenameWithSuffix(String originalName, String suffix) {
    StringBuilder name = new StringBuilder(originalName);
    int extensionStartIndex = name.lastIndexOf(".");
    if (extensionStartIndex >= 0) {
      name.insert(extensionStartIndex, suffix);
    }
    else {
      name.append(suffix);
    }
    return name.toString();
  }

  /**
   * Returns the portion of the given filename preceding any dot characters.
   * @param filename (e.g. "bar.txt")
   * @return the prefix (e.g. "bar")
   */
  public static String filenamePrefix(String filename) {
    int extensionStartIndex = filename.indexOf(".");
    if (extensionStartIndex < 0)
      return filename;  // the name doesn't have an extension
    return filename.substring(0, extensionStartIndex);
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

  /** Given "a","b","c", returns "a/b/c", where '/' is the File.separator */
  public static String joinPath(String... pathElements) {
    return StringUtils.join(File.separator, pathElements);
  }

  /** Given "a","b","c", returns a File representing "a/b/c", where '/' is the File.separator */
  public static File joinPathAsFile(String... pathElements) {
    return new File(joinPath(pathElements));
  }

  /**
   * @return the location of the compiled {@code .class} file for the given class.
   */
  public static File getClassFile(Class cls) {
    return urlToFile(cls.getResource(cls.getSimpleName() + ".class"));
  }
}
