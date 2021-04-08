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

import solutions.trsoftware.commons.server.util.reflect.ReflectionUtils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static solutions.trsoftware.commons.server.io.ServerIOUtils.readCharactersIntoString;
import static solutions.trsoftware.commons.server.io.ServerIOUtils.readUTF8;
import static solutions.trsoftware.commons.server.io.file.FileUtils.urlToFilepath;

/**
 * Provides a shared abstraction for the 2 ways of accessing resources in Java: {@link Class#getResource} / {@link
 * Class#getResourceAsStream} and {@link ClassLoader#getResource} / {@link ClassLoader#getResourceAsStream}, which are
 * similar but have slight syntactic differences.
 * <p>
 * For example, {@link ClassLoader#getResource(String)} expects an absolute path relative to the application's root
 * directory (and not prefixed with a leading {@code /} char). On the other hand, for {@link Class#getResource(String)}
 * you have to prefix such a path with a leading {@code /} char. Furthermore {@link Class#getResource(String)} will
 * resolve a name that's not prefixed with a leading {@code /} char against its own package, so it facilitates getting
 * resources that are located in the same package as a particular class.
 *
 * @author Alex
 * @see <a href="https://stackoverflow.com/a/6608848">StackOverflow discussion about the difference between the
 *     getResource methods of Class vs. ClassLoader</a>
 * @since 3/19/2018
 */
public class ResourceLocator {

  private final String resourceName;
  private final Class refClass;
  private final ClassLoader classLoader;
  private volatile String canonicalName;

  /**
   * A resource that can be read with {@link ClassLoader#getResourceAsStream(String)}, identified by the given
   * fully-qualified name.
   *
   * @param resourceName a string suitable as an argument to {@link ClassLoader#getResource(String)} (i.e. it should
   *     include the full path relative to the root package without a leading {@code /} char)
   * @param classLoader the {@link ClassLoader} to use for accessing this resource.
   */
  public ResourceLocator(String resourceName, ClassLoader classLoader) {
    this.resourceName = Objects.requireNonNull(resourceName);
    this.classLoader = Objects.requireNonNull(classLoader);
    this.refClass = null;
  }

  /**
   * A resource that can be read with {@link ClassLoader#getResourceAsStream(String)}, identified by the given
   * fully-qualified name.  Will use the {@link ClassLoader} of this class ({@link ResourceLocator}) for locating
   * this resource.  To specify a different {@link ClassLoader}, use the {@link #ResourceLocator(String, ClassLoader)}
   * constructor instead.
   *
   * @param resourceName a string suitable as an argument to {@link ClassLoader#getResource(String)} (i.e. it should
   *     include the full path relative to the root package without a leading {@code /} char)
   * @see ClassLoader#getResource(String)
   * @see #ResourceLocator(String, ClassLoader)
   */
  public ResourceLocator(String resourceName) {
    this(resourceName, ResourceLocator.class.getClassLoader());
  }

  /**
   * A resource that can be read with {@link Class#getResourceAsStream(String)}, identified by the given name.
   * @param resourceName a string suitable as an argument to {@link Class#getResource(String)} (i.e. its path will be resolved against the package of a given class if its name doesn't start with a leading {@code /} char)
   * @param refClass the class to be used when resolving the resource name (via {@link Class#getResourceAsStream(String)} or {@link Class#getResource(String)})
   * @see Class#getResource(String)
   * @see Class#getResourceAsStream(String)
   */
  public ResourceLocator(String resourceName, Class refClass) {
    this.resourceName = Objects.requireNonNull(resourceName);
    this.refClass = Objects.requireNonNull(refClass);
    this.classLoader = null;
  }

  /**
   * @return the URL of this resource, obtained either by {@link Class#getResource(String)} or {@link
   *     ClassLoader#getResource(String)}, depending on which constructor was used. Returns {@code null} if no resource
   *     with this name is found.
   * @see #ResourceLocator(String)
   * @see #ResourceLocator(String, Class)
   */
  public URL getURL() {
    if (refClass != null)
      return refClass.getResource(resourceName);
    else
      return classLoader.getResource(resourceName);
  }

  /**
   * @return the result of {@link #getURL()} converted to a {@link URI}. Returns {@code null} if no resource
   *     with this name is found.
   */
  public URI getURI() {
    URL url = getURL();
    if (url == null)
      return null;
    try {
      return url.toURI();
    }
    catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @return an input stream that can be used to read the data from this resource, obtained either by {@link
   *     Class#getResourceAsStream(String)} or {@link ClassLoader#getSystemResourceAsStream(String)}, depending on which
   *     constructor was used.  Returns {@code null} if no resource with this name is found.
   * @see #ResourceLocator(String)
   * @see #ResourceLocator(String, Class)
   */
  public InputStream getInputStream() {
    if (refClass != null)
      return refClass.getResourceAsStream(resourceName);
    else
      return classLoader.getResourceAsStream(resourceName);
  }

  /**
   * @return the fully-qualified name that can be used to refer to this resource using {@link
   *     ClassLoader#getResource(String)} (but not {@link Class#getResource(String)}).
   */
  public String getCanonicalName() {
    if (canonicalName == null) {
      if (refClass == null)
        canonicalName = resourceName;  // should already be absolute
      else {
        synchronized (this) {
          if (canonicalName == null) {
            canonicalName = resolveResourceName(resourceName, refClass);
          }
        }
      }
    }
    return canonicalName;
  }

  /**
   * @return an {@link InputStreamReader} for this resource, using the default charset.
   * @throws NullPointerException if no resource with this name is found
   */
  public Reader getReader() {
    return new InputStreamReader(getInputStream());
  }

  /**
   * @return an an {@link InputStreamReader} using the UTF-8 charset for this resource
   * @throws NullPointerException if no resource with this name is found
   */
  public Reader getReaderUTF8() {
    return readUTF8(getInputStream());
  }

  /**
   * Loads the text from this resource using the {@code UTF-8} encoding.
   * @return the text of the given resource.
   * @throws IOException if the resource is not found or some other exception occurs while reading the resource
   */
  public String getContentAsString() throws IOException {
    InputStream in = getInputStream();
    if (in == null)
      throw new IOException("Resource not found: " + this);
    return readCharactersIntoString(in);
  }

  /**
   * @return {@code true} iff {@link #getURL()} is not {@code null}
   */
  public boolean exists() {
    return getURL() != null;
  }

  /**
   * Extracts the file path information from the {@link URL} of this resource.
   *
   * <p style="font-style: italic;">
   *   <b>WARNING</b>: resources cannot be treated as {@link File}s if they come from a JAR.
   * </p>
   *
   * @return a string that can be used to construct a {@link File} or {@link Path} object for this resource, or {@code
   *     null} if the resource is not found.
   */
  public String toFilepath() {
    URL url = getURL();
    return url == null ? null : urlToFilepath(url);
  }

  /**
   * Instantiates a {@link File} based the file path information from the {@link URL} of this resource.
   *
   * <p style="font-style: italic;">
   *   <b>WARNING</b>: resources cannot be treated as {@link File}s if they come from a JAR.
   * </p>
   *
   * @return a {@link File} object for this resource, or {@code null} if the resource is not found.
   */
  public File toFile() {
    String filepath = toFilepath();
    return filepath == null ? null : new File(filepath);
  }

  /**
   * @return a {@link Path} object for this resource, or {@code null} if the resource is not found.
   *
   * @throws FileSystemNotFoundException might be thrown by {@link Paths#get(URI)}. NOTE: this exception will be 
   * thrown if the resource is located in a JAR file.  But this can be overcome by creating a new {@link FileSystem}
   * (as described in
   *   <a href="https://stackoverflow.com/a/22605905/1965404">this answer on StackOverflow</a>,
   *   <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/io/fsp/zipfilesystemprovider.html">
   *     Zip File System Provider (Oracle Docs)</a>,
   *   <a href="https://gquintana.github.io/2017/09/02/Java-File-vs-Path.html">this blog post</a>,
   *   <a href="https://www.safaribooksonline.com/library/view/learning-java-4th/9781449372477/ch12s03.html">
   *     Learning Java, 4th Edition</a>)
   */
  public Path toPath() throws FileSystemNotFoundException {
    URI uri = getURI();
    if (uri == null)
      return null;
    return Paths.get(uri);
  }

  /**
   * <p>
   * Implements the same functionality as {@link Class#resolveName(String)} (which is used by {@link Class#getResource(String)})
   * to obtain a resource name suitable for {@link ClassLoader#getResource(String)}. Specifically, this method
   * converts a simple (un-prefixed) name to a qualified resource path, relative to the package of the given class.
   * The resulting string is suitable as an argument to {@link ClassLoader#getResource(String)} (but not {@link
   * Class#getResource(String)}).
   * <p>
   * <b>Example:</b>
   * Given {@code "myfile.txt"} and {@code com.foo.MyClass}, will return {@code "com/foo/myfile.txt"}
   *
   * <p style="font-style: italic;">
   *   <b>WARNING:</b> the resulting name only works if the resource file actually exists in the same directory as the compiled
   *   {@code .class} file for the given class.  The Java compiler (or the Ant's {@code javac} task) doesn't automatically
   *   copy non-java files to the bytecode output directory.  However, a compilation done from an IDE (e.g. IntelliJ),
   *   might do so, in which case your code might appear to work when running within the IDE, but might fail when compiled
   *   by some other means (e.g. packaging a webapp into a {@code WAR} using Ant).
   *   One approach might be to place your static files in some other resources directory (e.g. {@code war/WEB-INF/resources/})
   *   and refer to them using that directory instead of the package name of a class.
   *   Another approach would be to add a build task that copies all your non-{@code .java} files to the compiler output
   *   directory.
   * </p>
   * @param name a simple, unqualified filename (like {@code "myfile.txt"} but not like {@code
   *     "/foo/bar/myfile.txt"}),
   *     naming a file that presumably exists in the same package (or directory) as {@code referenceClass}
   * @param referenceClass a class in the same package where this resource is presumed to reside.
   * @return a string that can be used to refer to the given resource using {@link ClassLoader#getResource(String)}
   *     (but not {@link Class#getResource(String)}).  Returns {@code null} if either argument is {@code null}.
   * @see <a href="https://stackoverflow.com/a/6608848">StackOverflow discussion about the difference between the getResource methods of Class vs. ClassLoader</a>
   */
  public static String resolveResourceName(String name, Class referenceClass) {
    if (name == null || referenceClass == null)
      return null;
    if (name.startsWith("/"))
      return name.substring(1);  // already an absolute name reference
    Class c = referenceClass.isArray() ? ReflectionUtils.getRootComponentTypeOfArray(referenceClass) : referenceClass;
    String baseName = c.getName();
    int index = baseName.lastIndexOf('.');
    if (index != -1)
      name = baseName.substring(0, index).replace('.', '/') + "/" + name;
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    ResourceLocator that = (ResourceLocator)o;

    return getCanonicalName().equals(that.getCanonicalName());
  }

  @Override
  public int hashCode() {
    return getCanonicalName().hashCode();
  }

  @Override
  public String toString() {
    return getCanonicalName();
  }
}
